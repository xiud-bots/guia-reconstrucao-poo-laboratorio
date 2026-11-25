package br.edu.ifpb.didatico.exemplos.excecao;

/**
 * Exemplo Didático: Demonstração de criação e propagação de Exceções Checadas
 * de Domínio (Checked Exceptions) para regras de negócio recuperáveis.
 */
public class ExemploExcecaoChecada {

    // 1. Exceção Checada de Domínio (herda de Exception)
    public static class RecursoOcupadoException extends Exception {
        private final String identificadorRecurso;

        public RecursoOcupadoException(String identificadorRecurso) {
            super(String.format("O recurso '%s' já está em uso por outro processo.", identificadorRecurso));
            this.identificadorRecurso = identificadorRecurso;
        }

        public String getIdentificadorRecurso() {
            return identificadorRecurso;
        }
    }

    // 2. Modelo de Domínio
    public static class VagaEstacionamento {
        private final String numero;
        private boolean ocupada;

        public VagaEstacionamento(String numero) {
            this.numero = numero;
            this.ocupada = false;
        }

        public void ocupar() throws RecursoOcupadoException {
            if (this.ocupada) {
                // A regra de negócio falhou: LANÇA a exceção checada
                throw new RecursoOcupadoException(this.numero);
            }
            this.ocupada = true;
        }

        public void liberar() {
            this.ocupada = false;
        }

        public String getNumero() { return numero; }
        public boolean isOcupada() { return ocupada; }
    }

    // 3. Camada de Apresentação / Consumidor
    public static void main(String[] args) {
        VagaEstacionamento vaga = new VagaEstacionamento("VAGA-A01");

        // Primeira tentativa: Sucesso
        try {
            System.out.println("Tentativa 1: Ocupando a vaga...");
            vaga.ocupar();
            System.out.println("Vaga ocupada com sucesso!");
        } catch (RecursoOcupadoException e) {
            System.err.println("Erro: " + e.getMessage());
        }

        // Segunda tentativa com a vaga já ocupada: Disparo da Exceção
        try {
            System.out.println("\nTentativa 2: Tentando ocupar novamente a mesma vaga...");
            vaga.ocupar();
            System.out.println("Vaga ocupada com sucesso!"); // Não deve alcançar aqui
        } catch (RecursoOcupadoException e) {
            // A camada de interface captura e apresenta mensagem amigável ao usuário
            System.out.println("[CAPTURA DE SUCESSO] Operação recusada de forma elegante:");
            System.out.println(" -> Motivo: " + e.getMessage());
            System.out.println(" -> Recurso afetado: " + e.getIdentificadorRecurso());
        }
    }
}
