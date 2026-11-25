package br.edu.ifpb.didatico.exemplos.oo;

import java.util.Objects;

/**
 * Exemplo Didático: Demonstração de Encapsulamento Rigoroso,
 * Validação Defensiva de Invariantes e Sobrecarga de Construtores com this().
 */
public class ExemploEncapsulamentoEValidacao {

    public static class ContaCorrenteExemplo {
        private final String numeroConta;
        private String titular;
        private double saldo;
        private boolean ativa;

        // Construtor completo e principal
        public ContaCorrenteExemplo(String numeroConta, String titular, double saldoInicial, boolean ativa) {
            if (numeroConta == null || numeroConta.isBlank()) {
                throw new IllegalArgumentException("O número da conta é obrigatório.");
            }
            if (titular == null || titular.isBlank()) {
                throw new IllegalArgumentException("O titular da conta é obrigatório.");
            }
            if (saldoInicial < 0.0) {
                throw new IllegalArgumentException("O saldo inicial não pode ser negativo.");
            }

            this.numeroConta = numeroConta.trim();
            this.titular = titular.trim();
            this.saldo = saldoInicial;
            this.ativa = ativa;
        }

        // Sobrecarga de conveniência: nova conta nasce ativa e com saldo zero
        public ContaCorrenteExemplo(String numeroConta, String titular) {
            this(numeroConta, titular, 0.0, true);
        }

        public void depositar(double valor) {
            if (valor <= 0.0) {
                throw new IllegalArgumentException("O valor do depósito deve ser estritamente positivo.");
            }
            if (!this.ativa) {
                throw new IllegalStateException("Não é permitido depositar em conta inativa.");
            }
            this.saldo += valor;
        }

        public void sacar(double valor) {
            if (valor <= 0.0) {
                throw new IllegalArgumentException("O valor do saque deve ser positivo.");
            }
            if (valor > this.saldo) {
                throw new IllegalStateException("Saldo insuficiente.");
            }
            this.saldo -= valor;
        }

        // Getters protegidos
        public String getNumeroConta() { return numeroConta; }
        public String getTitular() { return titular; }
        public double getSaldo() { return saldo; }
        public boolean isAtiva() { return ativa; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ContaCorrenteExemplo that)) return false;
            return Objects.equals(numeroConta, that.numeroConta);
        }

        @Override
        public int hashCode() {
            return Objects.hash(numeroConta);
        }
    }

    public static void main(String[] args) {
        ContaCorrenteExemplo conta = new ContaCorrenteExemplo("12345-X", "Gabriel");
        conta.depositar(500.0);
        conta.sacar(150.0);
        System.out.printf("Conta %s (%s) - Saldo Atual: R$ %.2f%n", 
                conta.getNumeroConta(), conta.getTitular(), conta.getSaldo());
    }
}
