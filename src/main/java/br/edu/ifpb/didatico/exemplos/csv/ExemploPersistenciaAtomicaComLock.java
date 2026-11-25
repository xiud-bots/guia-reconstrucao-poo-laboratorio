package br.edu.ifpb.didatico.exemplos.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Demonstração Didática do Guia 18:
 * Persistência Segura em Arquivos Planos (CSV) com Gravação Atômica (Safe Atomic Move),
 * Backup Automático (.bak) e Concorrência com ReentrantReadWriteLock.
 */
public class ExemploPersistenciaAtomicaComLock {

    // Record de Domínio Genérico Didático
    public record RegistroPatrimonial(
            String tombamento,
            String descricao,
            BigDecimal valor,
            boolean ativo
    ) {
        public RegistroPatrimonial {
            Objects.requireNonNull(tombamento, "Tombamento não pode ser nulo");
            Objects.requireNonNull(descricao, "Descrição não pode ser nula");
            Objects.requireNonNull(valor, "Valor não pode ser nulo");
        }

        public String toCsvLine() {
            return String.format("%s;%s;%s;%b", tombamento, descricao, valor.toPlainString(), ativo);
        }

        public static RegistroPatrimonial fromCsvLine(String linha) {
            String[] partes = linha.split(";");
            if (partes.length < 4) {
                throw new IllegalArgumentException("Linha CSV com formato inválido: " + linha);
            }
            return new RegistroPatrimonial(
                    partes[0].trim(),
                    partes[1].trim(),
                    new BigDecimal(partes[2].trim()),
                    Boolean.parseBoolean(partes[3].trim())
            );
        }
    }

    /**
     * Repositório de Arquivo com Integridade Atômica e Bloqueio Concorrente.
     */
    public static class RepositorioPatrimonialCSV {

        private final Path caminhoArquivo;
        private final Path caminhoBackup;
        private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        private final Lock readLock = rwLock.readLock();
        private final Lock writeLock = rwLock.writeLock();

        public RepositorioPatrimonialCSV(Path caminhoArquivo) {
            this.caminhoArquivo = Objects.requireNonNull(caminhoArquivo, "Caminho do arquivo não pode ser nulo");
            this.caminhoBackup = caminhoArquivo.resolveSibling(caminhoArquivo.getFileName().toString() + ".bak");
        }

        public Path getCaminhoArquivo() { return caminhoArquivo; }
        public Path getCaminhoBackup() { return caminhoBackup; }

        /**
         * Leitura segura com ReadLock: Múltiplas threads leem em paralelo.
         */
        public List<RegistroPatrimonial> listarTodos() throws IOException {
            readLock.lock();
            try {
                if (!Files.exists(caminhoArquivo)) {
                    return Collections.emptyList();
                }

                List<RegistroPatrimonial> registros = new ArrayList<>();
                try (BufferedReader reader = Files.newBufferedReader(caminhoArquivo, StandardCharsets.UTF_8)) {
                    String linha;
                    while ((linha = reader.readLine()) != null) {
                        String limpa = linha.trim();
                        if (limpa.isEmpty() || limpa.startsWith("#")) {
                            continue; // Ignora linhas em branco e comentários
                        }
                        registros.add(RegistroPatrimonial.fromCsvLine(limpa));
                    }
                }
                return Collections.unmodifiableList(registros);
            } finally {
                readLock.unlock();
            }
        }

        /**
         * Gravação Segura Atômica com WriteLock Exclusivo:
         * 1. Escreve no arquivo temporário (.tmp) no mesmo diretório.
         * 2. Faz flush e fecha streams.
         * 3. Cria backup do arquivo atual (.bak).
         * 4. Executa Files.move com ATOMIC_MOVE e REPLACE_EXISTING.
         */
        public void salvarTodos(List<RegistroPatrimonial> novosRegistros) throws IOException {
            writeLock.lock();
            Path arquivoTemp = null;
            try {
                Path diretorioPai = caminhoArquivo.getParent();
                if (diretorioPai != null && !Files.exists(diretorioPai)) {
                    Files.createDirectories(diretorioPai);
                }

                // Cria o temporário no mesmo diretório para garantir o mesmo filesystem
                arquivoTemp = (diretorioPai != null)
                        ? Files.createTempFile(diretorioPai, "patrimonio-temp-", ".tmp")
                        : Files.createTempFile("patrimonio-temp-", ".tmp");

                // 1. Grava integralmente no arquivo temporário
                try (BufferedWriter writer = Files.newBufferedWriter(
                        arquivoTemp, StandardCharsets.UTF_8, StandardOpenOption.WRITE)) {

                    writer.write("# DADOS PATRIMONIAIS — FORMATO CSV UTF-8");
                    writer.newLine();
                    writer.write("# tombamento;descricao;valor;ativo");
                    writer.newLine();

                    for (RegistroPatrimonial item : novosRegistros) {
                        writer.write(item.toCsvLine());
                        writer.newLine();
                    }
                    writer.flush();
                }

                // 2. Se o arquivo oficial já existe, cria/atualiza backup de segurança
                if (Files.exists(caminhoArquivo)) {
                    Files.copy(caminhoArquivo, caminhoBackup,
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.COPY_ATTRIBUTES);
                }

                // 3. Substituição atômica no nível do sistema operacional
                try {
                    Files.move(arquivoTemp, caminhoArquivo,
                            StandardCopyOption.ATOMIC_MOVE,
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception exMove) {
                    // Fallback para sistemas de arquivos que não suportam flag ATOMIC_MOVE
                    Files.move(arquivoTemp, caminhoArquivo,
                            StandardCopyOption.REPLACE_EXISTING);
                }

            } catch (Exception ex) {
                // Em caso de qualquer falha na escrita, elimina o arquivo temporário
                if (arquivoTemp != null) {
                    try { Files.deleteIfExists(arquivoTemp); } catch (Exception ignored) {}
                }
                throw new IOException("Falha no salvamento atômico do repositório CSV.", ex);
            } finally {
                writeLock.unlock();
            }
        }

        /**
         * Restaura os dados a partir do arquivo .bak caso o principal tenha sido corrompido ou apagado.
         */
        public boolean restaurarBackup() throws IOException {
            writeLock.lock();
            try {
                if (Files.exists(caminhoBackup)) {
                    Files.copy(caminhoBackup, caminhoArquivo, StandardCopyOption.REPLACE_EXISTING);
                    return true;
                }
                return false;
            } finally {
                writeLock.unlock();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("=== Demonstração Didática: Persistência Atômica com Locks ===");

        Path tempDir = Files.createTempDirectory("teste-persistencia-atomica");
        Path arquivoOficial = tempDir.resolve("patrimonio.csv");

        RepositorioPatrimonialCSV repo = new RepositorioPatrimonialCSV(arquivoOficial);

        // 1. Gravação Inicial
        List<RegistroPatrimonial> loteInicial = List.of(
                new RegistroPatrimonial("PAT-001", "Servidor Rack Dell R740", new BigDecimal("18500.00"), true),
                new RegistroPatrimonial("PAT-002", "Nobreak Senoidal 3000VA", new BigDecimal("4200.00"), true)
        );
        repo.salvarTodos(loteInicial);
        System.out.println("-> Lote 1 salvo com sucesso no arquivo oficial.");
        System.out.println("-> Existe backup? " + Files.exists(repo.getCaminhoBackup())); // False na primeira gravação

        // 2. Segunda Gravação (Gera Backup do Lote 1)
        List<RegistroPatrimonial> loteAtualizado = new ArrayList<>(loteInicial);
        loteAtualizado.add(new RegistroPatrimonial("PAT-003", "Roteador Cisco Enterprise", new BigDecimal("6500.00"), true));
        repo.salvarTodos(loteAtualizado);
        System.out.println("-> Lote 2 salvo com sucesso.");
        System.out.println("-> Existe backup (.bak) agora? " + Files.exists(repo.getCaminhoBackup())); // True!

        // 3. Leitura Segura
        List<RegistroPatrimonial> lidos = repo.listarTodos();
        System.out.printf("-> Total de registros lidos do arquivo: %d%n", lidos.size());
        lidos.forEach(r -> System.out.printf("   [%s] %s - R$ %s (Ativo: %b)%n",
                r.tombamento(), r.descricao(), r.valor(), r.ativo()));

        // Limpeza dos arquivos temporários de teste
        Files.deleteIfExists(arquivoOficial);
        Files.deleteIfExists(repo.getCaminhoBackup());
        Files.deleteIfExists(tempDir);
        System.out.println("=== Concluído com Sucesso ===");
    }
}
