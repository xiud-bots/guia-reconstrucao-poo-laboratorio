package br.edu.ifpb.didatico.exemplos.builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Exemplo didático do Padrão GoF Builder com validação de invariantes atômicas.
 * Elimina construtores telescópicos e setters mutáveis, garantindo que objetos
 * complexos (com atributos obrigatórios e opcionais) nasçam 100% íntegros e imutáveis.
 * Adequado para formulários em etapas (Wizards) no JavaFX.
 */
public class ExemploBuilderInvariantesEWizard {

    /**
     * Entidade de Domínio Imutável.
     * Possui construtor estritamente privado, acessível apenas pelo Builder.
     */
    public static final class FichaPatrimonial {
        private final String codigoPatrimonio;
        private final String descricao;
        private final String categoria;
        private final BigDecimal valorAquisicao;
        private final LocalDate dataAquisicao;
        private final String numeroSerie;
        private final String localizacaoFisica;
        private final boolean requerInspecaoPeriodica;
        private final Integer intervaloInspecaoDias;

        private FichaPatrimonial(Builder builder) {
            this.codigoPatrimonio = builder.codigoPatrimonio;
            this.descricao = builder.descricao;
            this.categoria = builder.categoria;
            this.valorAquisicao = builder.valorAquisicao;
            this.dataAquisicao = builder.dataAquisicao;
            this.numeroSerie = builder.numeroSerie;
            this.localizacaoFisica = builder.localizacaoFisica;
            this.requerInspecaoPeriodica = builder.requerInspecaoPeriodica;
            this.intervaloInspecaoDias = builder.intervaloInspecaoDias;
        }

        public static Builder builder() {
            return new Builder();
        }

        public String getCodigoPatrimonio() {
            return codigoPatrimonio;
        }

        public String getDescricao() {
            return descricao;
        }

        public String getCategoria() {
            return categoria;
        }

        public BigDecimal getValorAquisicao() {
            return valorAquisicao;
        }

        public LocalDate getDataAquisicao() {
            return dataAquisicao;
        }

        public Optional<String> getNumeroSerie() {
            return Optional.ofNullable(numeroSerie);
        }

        public String getLocalizacaoFisica() {
            return localizacaoFisica;
        }

        public boolean isRequerInspecaoPeriodica() {
            return requerInspecaoPeriodica;
        }

        public Optional<Integer> getIntervaloInspecaoDias() {
            return Optional.ofNullable(intervaloInspecaoDias);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FichaPatrimonial that = (FichaPatrimonial) o;
            return Objects.equals(codigoPatrimonio, that.codigoPatrimonio);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(codigoPatrimonio);
        }

        @Override
        public String toString() {
            return String.format("FichaPatrimonial[codigo=%s, desc=%s, valor=R$ %s, local=%s]",
                    codigoPatrimonio, descricao, valorAquisicao, localizacaoFisica);
        }

        /**
         * Builder estático aninhado com Fluent API e validação no método build().
         */
        public static final class Builder {
            private String codigoPatrimonio;
            private String descricao;
            private String categoria = "GERAL"; // Padrão seguro
            private BigDecimal valorAquisicao = BigDecimal.ZERO;
            private LocalDate dataAquisicao = LocalDate.now();
            private String numeroSerie;
            private String localizacaoFisica = "Almoxarifado Central";
            private boolean requerInspecaoPeriodica = false;
            private Integer intervaloInspecaoDias;

            public Builder comIdentificacao(String codigo, String descricao) {
                this.codigoPatrimonio = codigo;
                this.descricao = descricao;
                return this;
            }

            public Builder naCategoria(String categoria) {
                if (categoria != null && !categoria.isBlank()) {
                    this.categoria = categoria.trim();
                }
                return this;
            }

            public Builder comValor(BigDecimal valor) {
                this.valorAquisicao = valor;
                return this;
            }

            public Builder adquiridoEm(LocalDate data) {
                this.dataAquisicao = data;
                return this;
            }

            public Builder comNumeroSerie(String serial) {
                this.numeroSerie = (serial != null && !serial.isBlank()) ? serial.trim() : null;
                return this;
            }

            public Builder alocadoEm(String local) {
                if (local != null && !local.isBlank()) {
                    this.localizacaoFisica = local.trim();
                }
                return this;
            }

            public Builder comInspecaoPeriodica(int intervaloDias) {
                if (intervaloDias <= 0) {
                    throw new IllegalArgumentException("Intervalo de inspeção periódica deve ser positivo.");
                }
                this.requerInspecaoPeriodica = true;
                this.intervaloInspecaoDias = intervaloDias;
                return this;
            }

            public Builder semInspecao() {
                this.requerInspecaoPeriodica = false;
                this.intervaloInspecaoDias = null;
                return this;
            }

            /**
             * Constrói e valida a entidade atômica.
             * Garante que o objeto nunca nasça em estado inválido na memória.
             */
            public FichaPatrimonial build() {
                if (codigoPatrimonio == null || codigoPatrimonio.isBlank()) {
                    throw new IllegalStateException("Código patrimonial é obrigatório para construção do recurso.");
                }
                if (descricao == null || descricao.isBlank()) {
                    throw new IllegalStateException("Descrição do recurso não pode ser vazia.");
                }
                if (valorAquisicao == null || valorAquisicao.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalStateException("Valor de aquisição não pode ser nulo ou negativo.");
                }
                if (dataAquisicao == null || dataAquisicao.isAfter(LocalDate.now())) {
                    throw new IllegalStateException("Data de aquisição não pode estar no futuro.");
                }
                if (requerInspecaoPeriodica && (intervaloInspecaoDias == null || intervaloInspecaoDias <= 0)) {
                    throw new IllegalStateException("Recursos que requerem inspeção devem possuir intervalo em dias positivo definido.");
                }

                return new FichaPatrimonial(this);
            }
        }
    }
}
