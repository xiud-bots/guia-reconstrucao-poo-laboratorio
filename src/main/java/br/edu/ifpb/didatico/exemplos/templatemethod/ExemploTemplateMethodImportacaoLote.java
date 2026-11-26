package br.edu.ifpb.didatico.exemplos.templatemethod;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Exemplo didático do Padrão GoF Template Method aplicado ao processamento em lote (Batch Processing).
 * Define o algoritmo invariante de leitura, isolamento de falhas por linha, notificação de progresso
 * para JavaFX e persistência atômica, delegando as etapas variáveis (parse e validação) para subclasses.
 */
public class ExemploTemplateMethodImportacaoLote {

    /**
     * Registro imutável de uma falha detectada durante a importação de uma linha.
     */
    public record FalhaLinha(int numeroLinha, String linhaOriginal, String motivo) {}

    /**
     * Resumo consolidado do resultado da importação em lote.
     */
    public record ResultadoImportacao<T>(
            int totalLinhasProcessadas,
            List<T> registrosImportados,
            List<FalhaLinha> falhas,
            long tempoExecucaoMs
    ) {
        public int totalSucesso() {
            return registrosImportados.size();
        }

        public int totalFalhas() {
            return falhas.size();
        }

        public double taxaAproveitamento() {
            return totalLinhasProcessadas == 0 ? 0.0 : ((double) totalSucesso() / totalLinhasProcessadas) * 100.0;
        }
    }

    /**
     * Superclasse abstrata contendo o Template Method invariante.
     */
    public static abstract class ImportadorLoteAbstrato<T> {

        /**
         * O TEMPLATE METHOD (marcado como final para proteger o fluxo de controle).
         * Executa o esqueleto invariante do algoritmo de importação resiliente.
         */
        public final ResultadoImportacao<T> processar(Path arquivo, Consumer<Double> callbackProgresso) throws IOException {
            Objects.requireNonNull(arquivo, "Arquivo de importação não pode ser nulo.");
            if (!Files.exists(arquivo)) {
                throw new FileNotFoundException("Arquivo de importação não encontrado: " + arquivo.toAbsolutePath());
            }

            long inicio = System.currentTimeMillis();
            List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
            if (linhas.isEmpty()) {
                return new ResultadoImportacao<>(0, List.of(), List.of(), 0);
            }

            // 1. Validação do cabeçalho delegada à subclasse
            validarCabecalho(linhas.getFirst());

            List<T> registrosValidos = new ArrayList<>();
            List<FalhaLinha> falhas = new ArrayList<>();
            int totalLinhasDados = Math.max(0, linhas.size() - 1);

            // 2. Processamento resiliente linha a linha
            for (int i = 1; i < linhas.size(); i++) {
                int numLinha = i + 1;
                String linha = linhas.get(i).trim();

                if (linha.isBlank() || linha.startsWith("#")) {
                    continue;
                }

                try {
                    // Hook A: Parsing específico do formato da linha
                    T entidade = parseLinha(linha, numLinha);

                    // Hook B: Validação semântica das regras de negócio
                    validarEntidade(entidade);

                    registrosValidos.add(entidade);
                } catch (Exception ex) {
                    // O erro de uma linha NÃO aborta o arquivo! Fica catalogado.
                    falhas.add(new FalhaLinha(numLinha, linha, ex.getMessage()));
                }

                // Notificação de progresso para a UI (Worker Thread do JavaFX)
                if (callbackProgresso != null && totalLinhasDados > 0) {
                    callbackProgresso.accept((double) i / totalLinhasDados);
                }
            }

            // 3. Persistência em lote apenas das entidades válidas
            if (!registrosValidos.isEmpty()) {
                persistirLote(registrosValidos);
            }

            long tempoGasto = System.currentTimeMillis() - inicio;
            return new ResultadoImportacao<>(
                    totalLinhasDados,
                    Collections.unmodifiableList(registrosValidos),
                    Collections.unmodifiableList(falhas),
                    tempoGasto
            );
        }

        // Métodos abstratos e hooks a serem implementados pelas subclasses
        protected abstract void validarCabecalho(String cabecalho) throws IOException;
        protected abstract T parseLinha(String linha, int numeroLinha) throws Exception;
        protected abstract void validarEntidade(T entidade) throws Exception;
        protected abstract void persistirLote(List<T> registros) throws IOException;
    }

    /**
     * DTO de domínio para dados importados.
     */
    public record EquipamentoImportado(String codigo, String descricao, BigDecimal valor, String categoria) {}

    /**
     * Subclasse concreta especializada na importação de arquivos CSV delimitados por ponto e vírgula (;).
     */
    public static class ImportadorEquipamentosCSV extends ImportadorLoteAbstrato<EquipamentoImportado> {

        private final List<EquipamentoImportado> repositorioDestino;

        public ImportadorEquipamentosCSV(List<EquipamentoImportado> repositorioDestino) {
            this.repositorioDestino = Objects.requireNonNull(repositorioDestino);
        }

        @Override
        protected void validarCabecalho(String cabecalho) throws IOException {
            if (cabecalho == null || !cabecalho.toLowerCase().contains("codigo") || !cabecalho.toLowerCase().contains("descricao")) {
                throw new IOException("Cabeçalho CSV inválido. Esperado contendo 'codigo;descricao;valor;categoria'");
            }
        }

        @Override
        protected EquipamentoImportado parseLinha(String linha, int numeroLinha) throws Exception {
            String[] tokens = linha.split(";");
            if (tokens.length < 4) {
                throw new IllegalArgumentException("Linha possui colunas insuficientes (esperado 4, recebido " + tokens.length + ")");
            }

            String codigo = tokens[0].trim();
            String descricao = tokens[1].trim();
            String valorTexto = tokens[2].trim().replace(",", ".");
            String categoria = tokens[3].trim();

            if (codigo.isBlank()) {
                throw new IllegalArgumentException("Coluna 'codigo' vazia.");
            }
            if (descricao.isBlank()) {
                throw new IllegalArgumentException("Coluna 'descricao' vazia.");
            }

            BigDecimal valor;
            try {
                valor = new BigDecimal(valorTexto);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Valor numérico inválido: '" + valorTexto + "'");
            }

            return new EquipamentoImportado(codigo, descricao, valor, categoria);
        }

        @Override
        protected void validarEntidade(EquipamentoImportado entidade) throws Exception {
            if (entidade.valor().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Valor do equipamento deve ser estritamente positivo (recebido: " + entidade.valor() + ")");
            }
            // Valida duplicidade dentro do próprio lote já importado
            boolean duplicado = repositorioDestino.stream()
                    .anyMatch(item -> item.codigo().equalsIgnoreCase(entidade.codigo()));
            if (duplicado) {
                throw new IllegalStateException("Código já cadastrado no acervo: " + entidade.codigo());
            }
        }

        @Override
        protected void persistirLote(List<EquipamentoImportado> registros) {
            repositorioDestino.addAll(registros);
        }
    }
}
