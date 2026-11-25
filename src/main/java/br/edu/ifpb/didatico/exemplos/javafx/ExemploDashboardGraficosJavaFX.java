package br.edu.ifpb.didatico.exemplos.javafx;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Demonstração Didática do Guia 21:
 * Dashboards Executivos, Métricas Agregadas com Java Streams e Gráficos JavaFX (PieChart e BarChart).
 * Isolamento pedagógico estrito: utiliza modelo didático genérico de inventário de ativos.
 */
public class ExemploDashboardGraficosJavaFX extends Application {

    // 1. Modelo de Domínio Genérico Didático
    public static final class ItemPatrimonialAnalitico {
        private final String codigo;
        private final String descricao;
        private final String categoria;
        private final BigDecimal valorEstimado;
        private final boolean emUso;

        public ItemPatrimonialAnalitico(String codigo, String descricao, String categoria, BigDecimal valorEstimado, boolean emUso) {
            this.codigo = Objects.requireNonNull(codigo, "Código não pode ser nulo").trim();
            this.descricao = Objects.requireNonNull(descricao, "Descrição não pode ser nula").trim();
            this.categoria = Objects.requireNonNull(categoria, "Categoria não pode ser nula").trim();
            this.valorEstimado = Objects.requireNonNull(valorEstimado, "Valor não pode ser nulo");
            this.emUso = emUso;
        }

        public String getCodigo() { return codigo; }
        public String getDescricao() { return descricao; }
        public String getCategoria() { return categoria; }
        public BigDecimal getValorEstimado() { return valorEstimado; }
        public boolean isEmUso() { return emUso; }
    }

    // 2. DTOs Analíticos Imutáveis (Java 21 Records)
    public record MetricaCategoria(String categoria, long quantidade, BigDecimal valorTotal) {}

    public record ResumoGeral(
            long totalItens,
            long itensEmUso,
            long itensDisponiveis,
            BigDecimal valorTotalGeral,
            double percentualOcupacao
    ) {}

    // 3. Camada de Serviço Analítico Desacoplada (Pure Domain Service com Streams API)
    public static final class ServicoAnalitico {

        public Map<String, Long> contarPorCategoria(List<ItemPatrimonialAnalitico> itens) {
            if (itens == null) return Collections.emptyMap();
            return itens.stream()
                    .collect(Collectors.groupingBy(
                            ItemPatrimonialAnalitico::getCategoria,
                            Collectors.counting()
                    ));
        }

        public Map<String, BigDecimal> calcularValorTotalPorCategoria(List<ItemPatrimonialAnalitico> itens) {
            if (itens == null) return Collections.emptyMap();
            return itens.stream()
                    .collect(Collectors.groupingBy(
                            ItemPatrimonialAnalitico::getCategoria,
                            Collectors.reducing(
                                    BigDecimal.ZERO,
                                    ItemPatrimonialAnalitico::getValorEstimado,
                                    BigDecimal::add
                            )
                    ));
        }

        public ResumoGeral calcularResumo(List<ItemPatrimonialAnalitico> itens) {
            if (itens == null || itens.isEmpty()) {
                return new ResumoGeral(0, 0, 0, BigDecimal.ZERO, 0.0);
            }

            long total = itens.size();
            Map<Boolean, Long> porStatus = itens.stream()
                    .collect(Collectors.partitioningBy(
                            ItemPatrimonialAnalitico::isEmUso,
                            Collectors.counting()
                    ));

            long emUso = porStatus.getOrDefault(true, 0L);
            long disponiveis = porStatus.getOrDefault(false, 0L);

            BigDecimal valorTotal = itens.stream()
                    .map(ItemPatrimonialAnalitico::getValorEstimado)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            double percentualOcupacao = ((double) emUso / total) * 100.0;

            return new ResumoGeral(total, emUso, disponiveis, valorTotal, percentualOcupacao);
        }
    }

    // 4. Componentes Visuais e Estado da Apresentação
    private final List<ItemPatrimonialAnalitico> acervoEmMemoria = new ArrayList<>();
    private final ServicoAnalitico servicoAnalitico = new ServicoAnalitico();

    private final Locale localeBr = new Locale.Builder().setLanguage("pt").setRegion("BR").build();
    private final NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(localeBr);

    private final Label lblCardTotal = new Label("0");
    private final Label lblCardEmUso = new Label("0");
    private final Label lblCardDisponiveis = new Label("0");
    private final Label lblCardValorTotal = new Label("R$ 0,00");
    private final Label lblCardTaxaOcupacao = new Label("0.0%");

    private PieChart graficoPizza;
    private BarChart<String, Number> graficoBarras;
    private int contadorNovosItens = 100;

    @Override
    public void start(Stage palcoPrincipal) {
        palcoPrincipal.setTitle("Demonstração Didática — Dashboard Executivo & Gráficos JavaFX");

        carregarDadosIniciais();

        // 1. Painel Superior: KPI Cards com Métricas Consolidadas
        HBox painelKpis = criarPainelKpis();

        // 2. Painel Central: Gráficos de Pizza e Barras Lado a Lado
        HBox painelGraficos = criarPainelGraficos();

        // 3. Barra de Ações: Simular Injeção de Dados em Tempo Real
        HBox barraAcoes = criarBarraAcoes();

        // 4. Montagem Geral
        BorderPane layoutPrincipal = new BorderPane();
        layoutPrincipal.setTop(painelKpis);
        layoutPrincipal.setCenter(painelGraficos);
        layoutPrincipal.setBottom(barraAcoes);
        layoutPrincipal.setStyle("-fx-background-color: #f4f6f9;");

        // Atualização inicial de todos os componentes
        atualizarDashboard();

        Scene cena = new Scene(layoutPrincipal, 960, 620);
        palcoPrincipal.setScene(cena);
        palcoPrincipal.show();

        // Instalação de tooltips nos nós do PieChart após a renderização
        instalarTooltipsGraficoPizza();
    }

    private HBox criarPainelKpis() {
        HBox painel = new HBox(12);
        painel.setPadding(new Insets(14));
        painel.setAlignment(Pos.CENTER);

        painel.getChildren().addAll(
                criarCard("TOTAL DE ITENS", lblCardTotal, "#0d6efd"),
                criarCard("EM USO", lblCardEmUso, "#e67e22"),
                criarCard("DISPONÍVEIS", lblCardDisponiveis, "#27ae60"),
                criarCard("TAXA OCUPAÇÃO", lblCardTaxaOcupacao, "#8e44ad"),
                criarCard("VALOR DO ACERVO", lblCardValorTotal, "#2c3e50")
        );

        return painel;
    }

    private VBox criarCard(String titulo, Label lblValor, String corDestaque) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(String.format("-fx-background-color: white; -fx-background-radius: 8px; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-border-width: 1px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 8, 0, 0, 2);"));
        HBox.setHgrow(card, Priority.ALWAYS);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6c757d;");

        lblValor.setStyle(String.format("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: %s;", corDestaque));

        card.getChildren().addAll(lblTitulo, lblValor);
        return card;
    }

    private HBox criarPainelGraficos() {
        HBox painel = new HBox(14);
        painel.setPadding(new Insets(10, 14, 14, 14));

        // 1. PieChart (Distribuição Quantitativa por Categoria)
        graficoPizza = new PieChart();
        graficoPizza.setTitle("Distribuição de Itens por Categoria");
        graficoPizza.setLegendVisible(true);
        graficoPizza.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");
        HBox.setHgrow(graficoPizza, Priority.ALWAYS);

        // 2. BarChart (Valor Financeiro por Categoria)
        CategoryAxis eixoCategorias = new CategoryAxis();
        eixoCategorias.setLabel("Categorias");

        NumberAxis eixoValores = new NumberAxis();
        eixoValores.setLabel("Valor Estimado (R$)");

        graficoBarras = new BarChart<>(eixoCategorias, eixoValores);
        graficoBarras.setTitle("Patrimônio Estimado por Categoria");
        graficoBarras.setLegendVisible(false);
        graficoBarras.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");
        HBox.setHgrow(graficoBarras, Priority.ALWAYS);

        painel.getChildren().addAll(graficoPizza, graficoBarras);
        return painel;
    }

    private HBox criarBarraAcoes() {
        HBox barra = new HBox(12);
        barra.setPadding(new Insets(10, 14, 14, 14));
        barra.setAlignment(Pos.CENTER_RIGHT);

        Button btnAdicionarNotebook = new Button("+ Simular Entrada: Informática");
        btnAdicionarNotebook.setStyle("-fx-background-color: #0d6efd; -fx-text-fill: white; -fx-font-weight: bold;");
        btnAdicionarNotebook.setOnAction(e -> {
            contadorNovosItens++;
            acervoEmMemoria.add(new ItemPatrimonialAnalitico(
                    "ITEM-" + contadorNovosItens,
                    "Estação de Trabalho Simulação " + contadorNovosItens,
                    "Informática",
                    new BigDecimal("3500.00"),
                    false
            ));
            atualizarDashboard();
            instalarTooltipsGraficoPizza();
        });

        Button btnAdicionarAudiovisual = new Button("+ Simular Entrada: Audiovisual");
        btnAdicionarAudiovisual.setStyle("-fx-background-color: #6f42c1; -fx-text-fill: white; -fx-font-weight: bold;");
        btnAdicionarAudiovisual.setOnAction(e -> {
            contadorNovosItens++;
            acervoEmMemoria.add(new ItemPatrimonialAnalitico(
                    "ITEM-" + contadorNovosItens,
                    "Kit Câmera & Microfone " + contadorNovosItens,
                    "Audiovisual",
                    new BigDecimal("2100.00"),
                    true
            ));
            atualizarDashboard();
            instalarTooltipsGraficoPizza();
        });

        barra.getChildren().addAll(new Label("Demonstração de Reatividade:"), btnAdicionarNotebook, btnAdicionarAudiovisual);
        return barra;
    }

    private void atualizarDashboard() {
        // 1. Calcula Resumo Geral
        ResumoGeral resumo = servicoAnalitico.calcularResumo(acervoEmMemoria);
        lblCardTotal.setText(String.valueOf(resumo.totalItens()));
        lblCardEmUso.setText(String.valueOf(resumo.itensEmUso()));
        lblCardDisponiveis.setText(String.valueOf(resumo.itensDisponiveis()));
        lblCardTaxaOcupacao.setText(String.format(localeBr, "%.1f%%", resumo.percentualOcupacao()));
        lblCardValorTotal.setText(formatoMoeda.format(resumo.valorTotalGeral()));

        // 2. Atualiza PieChart
        Map<String, Long> distribuicao = servicoAnalitico.contarPorCategoria(acervoEmMemoria);
        ObservableList<PieChart.Data> dadosPizza = FXCollections.observableArrayList();
        distribuicao.forEach((categoria, qtd) -> dadosPizza.add(new PieChart.Data(categoria, qtd)));
        graficoPizza.setData(dadosPizza);

        // 3. Atualiza BarChart
        Map<String, BigDecimal> valores = servicoAnalitico.calcularValorTotalPorCategoria(acervoEmMemoria);
        graficoBarras.getData().clear();
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        valores.forEach((categoria, total) -> serie.getData().add(new XYChart.Data<>(categoria, total.doubleValue())));
        graficoBarras.getData().add(serie);
    }

    private void instalarTooltipsGraficoPizza() {
        long total = acervoEmMemoria.size();
        for (PieChart.Data fatia : graficoPizza.getData()) {
            if (fatia.getNode() != null) {
                double perc = total > 0 ? (fatia.getPieValue() * 100.0 / total) : 0.0;
                Tooltip tooltip = new Tooltip(String.format(localeBr, "%s: %.0f itens (%.1f%%)",
                        fatia.getName(), fatia.getPieValue(), perc));
                Tooltip.install(fatia.getNode(), tooltip);
            }
        }
    }

    private void carregarDadosIniciais() {
        acervoEmMemoria.add(new ItemPatrimonialAnalitico("ATV-01", "Notebook Corporativo i7", "Informática", new BigDecimal("4500.00"), true));
        acervoEmMemoria.add(new ItemPatrimonialAnalitico("ATV-02", "Monitor Ultrawide 34 pol", "Informática", new BigDecimal("2200.00"), false));
        acervoEmMemoria.add(new ItemPatrimonialAnalitico("ATV-03", "Projetor Laser 4K", "Audiovisual", new BigDecimal("3800.00"), true));
        acervoEmMemoria.add(new ItemPatrimonialAnalitico("ATV-04", "Mesa de Áudio Digital 8 Canais", "Audiovisual", new BigDecimal("1900.00"), false));
        acervoEmMemoria.add(new ItemPatrimonialAnalitico("ATV-05", "Microfone Sem Fio Lapela", "Audiovisual", new BigDecimal("850.00"), true));
        acervoEmMemoria.add(new ItemPatrimonialAnalitico("ATV-06", "Switch Gerenciável 48 Portas", "Redes", new BigDecimal("2700.00"), false));
        acervoEmMemoria.add(new ItemPatrimonialAnalitico("ATV-07", "Roteador Core Enterprise", "Redes", new BigDecimal("3100.00"), false));
        acervoEmMemoria.add(new ItemPatrimonialAnalitico("ATV-08", "Câmera Robótica PTZ para Conferências", "Audiovisual", new BigDecimal("4200.00"), true));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
