package br.edu.ifpb.didatico.exemplos.strategy;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Exemplo Didático: Demonstração do Padrão GoF Strategy
 * para desacoplar regras de cálculo de entidades de negócio,
 * eliminando cadeias de if/else e switch com polimorfismo dinâmico.
 */
public class ExemploStrategyPolimorfico {

    // 1. Interface da Estratégia
    public interface RegraCalculoFrete {
        BigDecimal calcular(double distanciaKm);
    }

    // 2. Estratégia Concreta A: Frete Fixo por Quilômetro
    public static class FretePadrao implements RegraCalculoFrete {
        private final BigDecimal taxaPorKm;

        public FretePadrao(BigDecimal taxaPorKm) {
            this.taxaPorKm = Objects.requireNonNull(taxaPorKm);
        }

        @Override
        public BigDecimal calcular(double distanciaKm) {
            return taxaPorKm.multiply(BigDecimal.valueOf(distanciaKm));
        }
    }

    // 3. Estratégia Concreta B: Frete Expresso (Taxa de Saída + Valor Progressivo)
    public static class FreteExpresso implements RegraCalculoFrete {
        private final BigDecimal taxaSaida = BigDecimal.valueOf(25.00);
        private final BigDecimal taxaKmAdicional = BigDecimal.valueOf(3.50);

        @Override
        public BigDecimal calcular(double distanciaKm) {
            if (distanciaKm <= 0) return BigDecimal.ZERO;
            return taxaSaida.add(taxaKmAdicional.multiply(BigDecimal.valueOf(distanciaKm)));
        }
    }

    // 4. Estratégia Concreta C: Isenção (Frete Grátis)
    public static class FreteGratis implements RegraCalculoFrete {
        @Override
        public BigDecimal calcular(double distanciaKm) {
            return BigDecimal.ZERO;
        }
    }

    // 5. Entidade Consumidora (Composição e Injeção de Dependência)
    public static class Encomenda {
        private final String codigoRastreio;
        private final RegraCalculoFrete regraFrete;

        public Encomenda(String codigoRastreio, RegraCalculoFrete regraFrete) {
            this.codigoRastreio = Objects.requireNonNull(codigoRastreio);
            this.regraFrete = Objects.requireNonNull(regraFrete);
        }

        public BigDecimal calcularCustoEnvio(double distanciaKm) {
            // Despacho Dinâmico de Método da JVM em tempo de execução
            return this.regraFrete.calcular(distanciaKm);
        }

        public String getCodigoRastreio() { return codigoRastreio; }
    }

    public static void main(String[] args) {
        Encomenda enc1 = new Encomenda("BR-001", new FretePadrao(BigDecimal.valueOf(2.00)));
        Encomenda enc2 = new Encomenda("BR-002", new FreteExpresso());
        Encomenda enc3 = new Encomenda("BR-003", new FreteGratis());

        double distancia = 50.0;
        System.out.printf("Encomenda %s (Padrão 50km): R$ %.2f%n", enc1.getCodigoRastreio(), enc1.calcularCustoEnvio(distancia));
        System.out.printf("Encomenda %s (Expresso 50km): R$ %.2f%n", enc2.getCodigoRastreio(), enc2.calcularCustoEnvio(distancia));
        System.out.printf("Encomenda %s (Grátis 50km): R$ %.2f%n", enc3.getCodigoRastreio(), enc3.calcularCustoEnvio(distancia));
    }
}
