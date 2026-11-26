package br.edu.ifpb.didatico.exemplos.decorator;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Exemplo didático do Padrão GoF Decorator com JavaFX.
 * Demonstra a composição dinâmica de funcionalidades, taxas e serviços adicionais
 * sobre um objeto base em tempo de execução sem explosão combinatória de subclasses.
 */
public class ExemploDecoratorTarifacaoDinamica extends Application {

    private static final Locale LOCALE_BR = Locale.of("pt", "BR");
    private static final NumberFormat MOEDA_FORMAT = NumberFormat.getCurrencyInstance(LOCALE_BR);

    // =========================================================================
    // 1. CONTRATO DO COMPONENTE (Component)
    // =========================================================================

    /**
     * Interface base para qualquer serviço ou locação tarifável.
     */
    public interface IServicoPrecificavel {
        String getDescricao();
        BigDecimal getCustoBase();
        BigDecimal calcularCustoTotal();
        int getDiasLocacao();
        List<String> getLinhasDetalhadas();
    }

    // =========================================================================
    // 2. COMPONENTE CONCRETO BASE (ConcreteComponent)
    // =========================================================================

    /**
     * Contrato de locação base sem nenhum opcional adicionado.
     */
    public static class LocacaoBase implements IServicoPrecificavel {
        private final String item;
        private final BigDecimal valorDiaria;
        private final int dias;

        public LocacaoBase(String item, BigDecimal valorDiaria, int dias) {
            if (item == null || item.isBlank()) {
                throw new IllegalArgumentException("Nome do item não pode ser nulo ou vazio.");
            }
            if (valorDiaria == null || valorDiaria.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Valor da diária deve ser estritamente positivo.");
            }
            if (dias <= 0) {
                throw new IllegalArgumentException("Quantidade de dias deve ser maior que zero.");
            }
            this.item = item.trim();
            this.valorDiaria = valorDiaria;
            this.dias = dias;
        }

        @Override
        public String getDescricao() {
            return String.format("Locação de %s (%d dias @ %s/dia)", item, dias, MOEDA_FORMAT.format(valorDiaria));
        }

        @Override
        public BigDecimal getCustoBase() {
            return valorDiaria.multiply(BigDecimal.valueOf(dias));
        }

        @Override
        public BigDecimal calcularCustoTotal() {
            return getCustoBase();
        }

        @Override
        public int getDiasLocacao() {
            return dias;
        }

        @Override
        public List<String> getLinhasDetalhadas() {
            List<String> linhas = new ArrayList<>();
            linhas.add(String.format("1. [BASE] %s -> %s", getDescricao(), MOEDA_FORMAT.format(calcularCustoTotal())));
            return Collections.unmodifiableList(linhas);
        }
    }

    // =========================================================================
    // 3. DECORADOR BASE ABSTRATO (Decorator)
    // =========================================================================

    /**
     * Decorador base que delega chamadas para o serviço envelopado.
     */
    public static abstract class ServicoOpcionalDecorator implements IServicoPrecificavel {
        protected final IServicoPrecificavel decorado;

        public ServicoOpcionalDecorator(IServicoPrecificavel decorado) {
            this.decorado = Objects.requireNonNull(decorado, "O serviço a ser decorado não pode ser nulo.");
        }

        @Override
        public String getDescricao() {
            return decorado.getDescricao();
        }

        @Override
        public BigDecimal getCustoBase() {
            return decorado.getCustoBase();
        }

        @Override
        public int getDiasLocacao() {
            return decorado.getDiasLocacao();
        }

        @Override
        public List<String> getLinhasDetalhadas() {
            return decorado.getLinhasDetalhadas();
        }
    }

    // =========================================================================
    // 4. DECORADORES CONCRETOS (ConcreteDecorators)
    // =========================================================================

    /**
     * Adiciona seguro com franquia contra avarias e quedas (15% do valor base, mín. R$ 25,00).
     */
    public static final class SeguroAvariaDecorator extends ServicoOpcionalDecorator {
        private static final BigDecimal PERCENTUAL = new BigDecimal("0.15");
        private static final BigDecimal VALOR_MINIMO = new BigDecimal("25.00");

        public SeguroAvariaDecorator(IServicoPrecificavel decorado) {
            super(decorado);
        }

        public BigDecimal calcularValorSeguro() {
            BigDecimal calculado = decorado.getCustoBase().multiply(PERCENTUAL).setScale(2, RoundingMode.HALF_UP);
            return calculado.compareTo(VALOR_MINIMO) >= 0 ? calculado : VALOR_MINIMO;
        }

        @Override
        public BigDecimal calcularCustoTotal() {
            return decorado.calcularCustoTotal().add(calcularValorSeguro());
        }

        @Override
        public List<String> getLinhasDetalhadas() {
            List<String> linhas = new ArrayList<>(decorado.getLinhasDetalhadas());
            linhas.add(String.format("+ [SEGURO] Cobertura Contra Avarias e Quedas -> %s", MOEDA_FORMAT.format(calcularValorSeguro())));
            return Collections.unmodifiableList(linhas);
        }
    }

    /**
     * Adiciona desinfecção técnica e calibração de precisão em sala limpa (taxa fixa de R$ 35,00).
     */
    public static final class HigienizacaoTecnicaDecorator extends ServicoOpcionalDecorator {
        private static final BigDecimal TAXA_FIXA = new BigDecimal("35.00");

        public HigienizacaoTecnicaDecorator(IServicoPrecificavel decorado) {
            super(decorado);
        }

        @Override
        public BigDecimal calcularCustoTotal() {
            return decorado.calcularCustoTotal().add(TAXA_FIXA);
        }

        @Override
        public List<String> getLinhasDetalhadas() {
            List<String> linhas = new ArrayList<>(decorado.getLinhasDetalhadas());
            linhas.add(String.format("+ [HIGIENIZAÇÃO] Limpeza e Calibração Técnica em Sala Limpa -> %s", MOEDA_FORMAT.format(TAXA_FIXA)));
            return Collections.unmodifiableList(linhas);
        }
    }

    /**
     * Adiciona canal de atendimento prioritário e suporte técnico 24h (R$ 10,00 por dia).
     */
    public static final class SuporteTecnico24hDecorator extends ServicoOpcionalDecorator {
        private static final BigDecimal TAXA_DIARIA = new BigDecimal("10.00");

        public SuporteTecnico24hDecorator(IServicoPrecificavel decorado) {
            super(decorado);
        }

        public BigDecimal calcularCustoSuporte() {
            return TAXA_DIARIA.multiply(BigDecimal.valueOf(decorado.getDiasLocacao()));
        }

        @Override
        public BigDecimal calcularCustoTotal() {
            return decorado.calcularCustoTotal().add(calcularCustoSuporte());
        }

        @Override
        public List<String> getLinhasDetalhadas() {
            List<String> linhas = new ArrayList<>(decorado.getLinhasDetalhadas());
            linhas.add(String.format("+ [SUPORTE] Atendimento 24h Prioritário (%d dias @ %s) -> %s",
                    decorado.getDiasLocacao(),
                    MOEDA_FORMAT.format(TAXA_DIARIA),
                    MOEDA_FORMAT.format(calcularCustoSuporte())));
            return Collections.unmodifiableList(linhas);
        }
    }

    // =========================================================================
    // 5. INTERFACE GRÁFICA JAVAFX
    // =========================================================================

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Demonstração GoF Decorator & Tarifação Dinâmica - XIUD Didático");

        // Objeto base de exemplo
        final LocacaoBase base = new LocacaoBase("Kit Osciloscópio Digital + Sondas", new BigDecimal("50.00"), 5);

        Label lblTitulo = new Label("Simulador de Tarifação Dinâmica (Padrão Decorator)");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a365d;");

        Label lblItemBase = new Label(base.getDescricao() + "\nValor Base: " + MOEDA_FORMAT.format(base.getCustoBase()));
        lblItemBase.setStyle("-fx-background-color: #edf2f7; -fx-padding: 10px; -fx-background-radius: 6px;");

        // CheckBoxes que representam a aplicação de decoradores em tempo real
        CheckBox chkSeguro = new CheckBox("Seguro Contra Avarias (+15%, mín. R$ 25,00)");
        CheckBox chkHigienizacao = new CheckBox("Higienização e Calibração Técnica (+ R$ 35,00 fixo)");
        CheckBox chkSuporte = new CheckBox("Suporte Técnico Especializado 24h (+ R$ 10,00/dia)");

        VBox boxOpcionais = new VBox(10, new Label("Selecione os Serviços Opcionais:"), chkSeguro, chkHigienizacao, chkSuporte);
        boxOpcionais.setStyle("-fx-padding: 12px; -fx-border-color: #cbd5e0; -fx-border-radius: 6px;");

        ListView<String> listaFatura = new ListView<>();
        listaFatura.setPrefHeight(160);

        Label lblTotal = new Label("Total: R$ 0,00");
        lblTotal.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2b6cb0;");

        // Rotina de recomposição reativa de decoradores
        Runnable recalcularFatura = () -> {
            IServicoPrecificavel servicoComposto = base;

            if (chkSeguro.isSelected()) {
                servicoComposto = new SeguroAvariaDecorator(servicoComposto);
            }
            if (chkHigienizacao.isSelected()) {
                servicoComposto = new HigienizacaoTecnicaDecorator(servicoComposto);
            }
            if (chkSuporte.isSelected()) {
                servicoComposto = new SuporteTecnico24hDecorator(servicoComposto);
            }

            listaFatura.getItems().setAll(servicoComposto.getLinhasDetalhadas());
            lblTotal.setText("Total Faturado: " + MOEDA_FORMAT.format(servicoComposto.calcularCustoTotal()));
        };

        chkSeguro.setOnAction(e -> recalcularFatura.run());
        chkHigienizacao.setOnAction(e -> recalcularFatura.run());
        chkSuporte.setOnAction(e -> recalcularFatura.run());

        // Inicializa estado
        recalcularFatura.run();

        VBox layout = new VBox(14,
                lblTitulo,
                lblItemBase,
                boxOpcionais,
                new Label("Discriminação Transparente da Fatura:"),
                listaFatura,
                new Separator(),
                lblTotal
        );
        layout.setPadding(new Insets(18));

        primaryStage.setScene(new Scene(new BorderPane(layout), 620, 560));
        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
