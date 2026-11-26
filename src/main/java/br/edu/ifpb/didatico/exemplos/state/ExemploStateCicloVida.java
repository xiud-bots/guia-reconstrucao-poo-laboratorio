package br.edu.ifpb.didatico.exemplos.state;

import java.util.Objects;

/**
 * Exemplo didático do Padrão GoF State aplicado ao ciclo de vida de recursos e equipamentos.
 * Demonstra a substituição de dezenas de flags booleanas por uma Máquina de Estados Finita (FSM)
 * estritamente tipada com transições seguras e rejeição explícita de operações ilegais.
 */
public class ExemploStateCicloVida {

    /**
     * Exceção checada lançada quando uma transição de estado não é autorizada pelas regras de negócio.
     */
    public static class TransicaoEstadoInvalidaException extends Exception {
        public TransicaoEstadoInvalidaException(String mensagem) {
            super(mensagem);
        }
    }

    /**
     * Contrato do Padrão GoF State para o ciclo de vida do recurso patrimonial.
     * Métodos default fornecem comportamento padrão de rejeição, simplificando os estados concretos.
     */
    public interface IEstadoRecurso {
        String getNomeEstado();

        default void emprestar(RecursoPatrimonial ctx, String responsavel) throws TransicaoEstadoInvalidaException {
            throw new TransicaoEstadoInvalidaException(
                    String.format("Operação de empréstimo não permitida no estado atual: [%s]", getNomeEstado()));
        }

        default void devolver(RecursoPatrimonial ctx) throws TransicaoEstadoInvalidaException {
            throw new TransicaoEstadoInvalidaException(
                    String.format("Operação de devolução não permitida no estado atual: [%s]", getNomeEstado()));
        }

        default void enviarParaManutencao(RecursoPatrimonial ctx, String motivo) throws TransicaoEstadoInvalidaException {
            throw new TransicaoEstadoInvalidaException(
                    String.format("Envio para manutenção não permitido no estado atual: [%s]", getNomeEstado()));
        }

        default void concluirManutencao(RecursoPatrimonial ctx) throws TransicaoEstadoInvalidaException {
            throw new TransicaoEstadoInvalidaException(
                    String.format("Conclusão de manutenção não permitida no estado atual: [%s]", getNomeEstado()));
        }

        default void baixarPatrimonio(RecursoPatrimonial ctx, String justificativa) throws TransicaoEstadoInvalidaException {
            throw new TransicaoEstadoInvalidaException(
                    String.format("Baixa patrimonial não permitida no estado atual: [%s]", getNomeEstado()));
        }

        boolean permiteEmprestimo();
        boolean permiteDevolucao();
    }

    /**
     * Estado Concreto: DISPONÍVEL
     * Permite empréstimo, envio para manutenção preventiva/corretiva ou baixa patrimonial.
     */
    public static final class EstadoDisponivel implements IEstadoRecurso {
        @Override
        public String getNomeEstado() {
            return "DISPONIVEL";
        }

        @Override
        public void emprestar(RecursoPatrimonial ctx, String responsavel) {
            ctx.setResponsavelAtual(responsavel);
            ctx.setEstadoInterno(new EstadoEmprestado());
        }

        @Override
        public void enviarParaManutencao(RecursoPatrimonial ctx, String motivo) {
            ctx.setUltimoMotivoManutencao(motivo);
            ctx.setEstadoInterno(new EstadoEmManutencao());
        }

        @Override
        public void baixarPatrimonio(RecursoPatrimonial ctx, String justificativa) {
            ctx.setJustificativaBaixa(justificativa);
            ctx.setEstadoInterno(new EstadoBaixado());
        }

        @Override
        public boolean permiteEmprestimo() {
            return true;
        }

        @Override
        public boolean permiteDevolucao() {
            return false;
        }
    }

    /**
     * Estado Concreto: EMPRESTADO
     * Permite exclusivamente a devolução para retorno ao estado DISPONÍVEL.
     */
    public static final class EstadoEmprestado implements IEstadoRecurso {
        @Override
        public String getNomeEstado() {
            return "EMPRESTADO";
        }

        @Override
        public void devolver(RecursoPatrimonial ctx) {
            ctx.setResponsavelAtual(null);
            ctx.setEstadoInterno(new EstadoDisponivel());
        }

        @Override
        public boolean permiteEmprestimo() {
            return false;
        }

        @Override
        public boolean permiteDevolucao() {
            return true;
        }
    }

    /**
     * Estado Concreto: EM MANUTENÇÃO
     * O item está em reparo técnico. Permite concluir manutenção (volta a DISPONÍVEL) ou baixa definitiva.
     */
    public static final class EstadoEmManutencao implements IEstadoRecurso {
        @Override
        public String getNomeEstado() {
            return "EM_MANUTENCAO";
        }

        @Override
        public void concluirManutencao(RecursoPatrimonial ctx) {
            ctx.setUltimoMotivoManutencao(null);
            ctx.setEstadoInterno(new EstadoDisponivel());
        }

        @Override
        public void baixarPatrimonio(RecursoPatrimonial ctx, String justificativa) {
            ctx.setJustificativaBaixa(justificativa);
            ctx.setEstadoInterno(new EstadoBaixado());
        }

        @Override
        public boolean permiteEmprestimo() {
            return false;
        }

        @Override
        public boolean permiteDevolucao() {
            return false;
        }
    }

    /**
     * Estado Concreto: BAIXADO
     * Estado terminal. O patrimônio foi descartado, leiloado ou desativado. Nenhuma operação é permitida.
     */
    public static final class EstadoBaixado implements IEstadoRecurso {
        @Override
        public String getNomeEstado() {
            return "BAIXADO";
        }

        @Override
        public boolean permiteEmprestimo() {
            return false;
        }

        @Override
        public boolean permiteDevolucao() {
            return false;
        }
    }

    /**
     * Entidade de Domínio (Contexto do Padrão State).
     * Delega as ações de negócio para o objeto de estado atual.
     */
    public static class RecursoPatrimonial {
        private final String codigo;
        private final String descricao;
        private IEstadoRecurso estadoAtual;
        private String responsavelAtual;
        private String ultimoMotivoManutencao;
        private String justificativaBaixa;

        public RecursoPatrimonial(String codigo, String descricao) {
            if (codigo == null || codigo.isBlank()) {
                throw new IllegalArgumentException("Código não pode ser nulo ou vazio.");
            }
            if (descricao == null || descricao.isBlank()) {
                throw new IllegalArgumentException("Descrição não pode ser nula ou vazia.");
            }
            this.codigo = codigo.trim();
            this.descricao = descricao.trim();
            this.estadoAtual = new EstadoDisponivel(); // Estado inicial seguro
        }

        // Operações de negócio delegadas ao State
        public void emprestar(String responsavel) throws TransicaoEstadoInvalidaException {
            if (responsavel == null || responsavel.isBlank()) {
                throw new IllegalArgumentException("Responsável pelo empréstimo é obrigatório.");
            }
            this.estadoAtual.emprestar(this, responsavel.trim());
        }

        public void devolver() throws TransicaoEstadoInvalidaException {
            this.estadoAtual.devolver(this);
        }

        public void enviarParaManutencao(String motivo) throws TransicaoEstadoInvalidaException {
            if (motivo == null || motivo.isBlank()) {
                throw new IllegalArgumentException("Motivo da manutenção é obrigatório.");
            }
            this.estadoAtual.enviarParaManutencao(this, motivo.trim());
        }

        public void concluirManutencao() throws TransicaoEstadoInvalidaException {
            this.estadoAtual.concluirManutencao(this);
        }

        public void baixar(String justificativa) throws TransicaoEstadoInvalidaException {
            if (justificativa == null || justificativa.isBlank()) {
                throw new IllegalArgumentException("Justificativa de baixa é obrigatória.");
            }
            this.estadoAtual.baixarPatrimonio(this, justificativa.trim());
        }

        // Métodos de acesso com visibilidade controlada
        void setEstadoInterno(IEstadoRecurso novoEstado) {
            this.estadoAtual = Objects.requireNonNull(novoEstado);
        }

        void setResponsavelAtual(String responsavel) {
            this.responsavelAtual = responsavel;
        }

        void setUltimoMotivoManutencao(String motivo) {
            this.ultimoMotivoManutencao = motivo;
        }

        void setJustificativaBaixa(String justificativa) {
            this.justificativaBaixa = justificativa;
        }

        public String getCodigo() {
            return codigo;
        }

        public String getDescricao() {
            return descricao;
        }

        public IEstadoRecurso getEstado() {
            return estadoAtual;
        }

        public String getResponsavelAtual() {
            return responsavelAtual;
        }

        public String getUltimoMotivoManutencao() {
            return ultimoMotivoManutencao;
        }

        public String getJustificativaBaixa() {
            return justificativaBaixa;
        }
    }
}
