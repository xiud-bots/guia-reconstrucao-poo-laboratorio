package br.edu.ifpb.didatico.exemplos.teste;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Exemplo Didático: Arquitetura Orientada a Testabilidade (Testability Architecture):
 * 1. Interface abstrata de Repositório (DIP).
 * 2. Implementação Fake em Memória para Testes Unitários de Alta Velocidade.
 * 3. Camada de Serviço isolada validando regras de limite e concorrência sem I/O de disco.
 */
public class ExemploTestabilidadeComFake {

    // Entidade de domínio do exemplo
    public record RecursoAuditavel(String codigo, String descricao, String responsavelAtual, boolean ocupado) {
        public RecursoAuditavel comResponsavel(String novoResponsavel) {
            return new RecursoAuditavel(codigo, descricao, novoResponsavel, true);
        }

        public RecursoAuditavel liberar() {
            return new RecursoAuditavel(codigo, descricao, null, false);
        }
    }

    // Exceções de Domínio Checadas
    public static class RecursoIndisponivelException extends Exception {
        public RecursoIndisponivelException(String codigo) {
            super("O recurso com identificador '" + codigo + "' já se encontra em uso.");
        }
    }

    public static class LimiteReservasExcedidoException extends Exception {
        public LimiteReservasExcedidoException(String pessoa, int limite) {
            super("A pessoa '" + pessoa + "' atingiu o limite regulamentar de " + limite + " recursos simultâneos.");
        }
    }

    /**
     * CONTRATO DO REPOSITÓRIO (DIP):
     * O serviço depende exclusivamente desta interface, nunca de arquivos ou bancos.
     */
    public interface IRepositorioRecurso {
        void salvar(RecursoAuditavel recurso);
        Optional<RecursoAuditavel> buscarPorCodigo(String codigo);
        List<RecursoAuditavel> listarTodos();
        long contarPorResponsavel(String responsavel);
    }

    /**
     * REPOSITÓRIO FAKE EM MEMÓRIA (IN-MEMORY TEST DOUBLE):
     * Executa em RAM, permitindo testes unitários em microssegundos sem disco.
     */
    public static class RepositorioRecursoFakeMemoria implements IRepositorioRecurso {

        private final Map<String, RecursoAuditavel> dados = new ConcurrentHashMap<>();

        @Override
        public void salvar(RecursoAuditavel recurso) {
            Objects.requireNonNull(recurso, "Recurso não pode ser nulo.");
            dados.put(recurso.codigo(), recurso);
        }

        @Override
        public Optional<RecursoAuditavel> buscarPorCodigo(String codigo) {
            return Optional.ofNullable(dados.get(codigo));
        }

        @Override
        public List<RecursoAuditavel> listarTodos() {
            return Collections.unmodifiableList(new ArrayList<>(dados.values()));
        }

        @Override
        public long contarPorResponsavel(String responsavel) {
            if (responsavel == null) return 0;
            return dados.values().stream()
                    .filter(r -> r.ocupado() && responsavel.equalsIgnoreCase(r.responsavelAtual()))
                    .count();
        }
    }

    /**
     * CAMADA DE SERVIÇO DE DOMÍNIO:
     * Orquestra regras de negócio e limites de permissão com Injeção por Construtor.
     */
    public static class ServicoReserva {

        public static final int LIMITE_MAXIMO_POR_PESSOA = 2;
        private final IRepositorioRecurso repositorio;

        public ServicoReserva(IRepositorioRecurso repositorio) {
            this.repositorio = Objects.requireNonNull(repositorio, "Repositório obrigatório.");
        }

        public void alocarRecurso(String codigoRecurso, String solicitante)
                throws RecursoIndisponivelException, LimiteReservasExcedidoException {

            Objects.requireNonNull(codigoRecurso, "Código do recurso é obrigatório.");
            Objects.requireNonNull(solicitante, "Solicitante é obrigatório.");

            RecursoAuditavel recurso = repositorio.buscarPorCodigo(codigoRecurso)
                    .orElseThrow(() -> new IllegalArgumentException("Recurso inexistente: " + codigoRecurso));

            // Regra 1: Não pode alocar recurso já ocupado
            if (recurso.ocupado()) {
                throw new RecursoIndisponivelException(codigoRecurso);
            }

            // Regra 2: Limite máximo regulamentar por pessoa
            long reservasAtuais = repositorio.contarPorResponsavel(solicitante);
            if (reservasAtuais >= LIMITE_MAXIMO_POR_PESSOA) {
                throw new LimiteReservasExcedidoException(solicitante, LIMITE_MAXIMO_POR_PESSOA);
            }

            // Transição de estado e persistência
            RecursoAuditavel atualizado = recurso.comResponsavel(solicitante);
            repositorio.salvar(atualizado);
        }

        public void liberarRecurso(String codigoRecurso) {
            RecursoAuditavel recurso = repositorio.buscarPorCodigo(codigoRecurso)
                    .orElseThrow(() -> new IllegalArgumentException("Recurso inexistente: " + codigoRecurso));

            repositorio.salvar(recurso.liberar());
        }

        public List<RecursoAuditavel> listarTodos() {
            return repositorio.listarTodos();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Demonstração: Testabilidade com Repositório Fake em Memória ===");
        IRepositorioRecurso repo = new RepositorioRecursoFakeMemoria();
        repo.salvar(new RecursoAuditavel("REC-01", "Projetor Sala A", null, false));
        repo.salvar(new RecursoAuditavel("REC-02", "Projetor Sala B", null, false));
        repo.salvar(new RecursoAuditavel("REC-03", "Microscópio Óptico", null, false));

        ServicoReserva servico = new ServicoReserva(repo);

        try {
            servico.alocarRecurso("REC-01", "Gabriel");
            servico.alocarRecurso("REC-02", "Gabriel");
            System.out.println("Alocações realizadas com sucesso para Gabriel.");

            System.out.println("Tentando alocar o 3º recurso (deve disparar LimiteReservasExcedidoException)...");
            servico.alocarRecurso("REC-03", "Gabriel");
        } catch (RecursoIndisponivelException | LimiteReservasExcedidoException e) {
            System.out.println("Exceção capturada com sucesso: " + e.getMessage());
        }
    }
}
