package br.edu.ifpb.didatico.exemplos.flyweight;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Exemplo didático do Padrão GoF Flyweight aplicado a gerenciamento eficiente de memória e JavaFX.
 * Demonstra a segregação estrita entre:
 * 1. Estado Intrínseco (Compartilhado e Imutável): Fabricante, Categoria, Especificação Técnica, Cor do Badge.
 * 2. Estado Extrínseco (Variável por Instância): Código de Patrimônio, Número de Série, Sala/Bancada e Status.
 * Permite exibir milhares de itens em TableView mantendo um pool mínimo de instâncias no Heap da JVM.
 */
public class ExemploFlyweightOtimizacaoMemoria extends Application {

    // =========================================================================
    // 1. ESTADO INTRÍNSECO (FLYWEIGHT COMPARTILHADO E IMUTÁVEL)
    // =========================================================================

    /**
     * O Flyweight Puro: armazena dados comuns, pesados e imutáveis.
     * Milhares de itens no sistema referenciam exatamente a mesma instância na memória RAM.
     */
    public record EspecificacaoEquipamentoFlyweight(
            String modeloChave,
            String fabricante,
            String descricaoTecnica,
            String categoriaGeral,
            String tensaoOperacao,
            String corBadgeHex
    ) {
        public EspecificacaoEquipamentoFlyweight {
            Objects.requireNonNull(modeloChave, "modeloChave não pode ser nulo.");
            Objects.requireNonNull(fabricante, "fabricante não pode ser nulo.");
            Objects.requireNonNull(descricaoTecnica, "descricaoTecnica não pode ser nula.");
            Objects.requireNonNull(categoriaGeral, "categoriaGeral não pode ser nula.");
            Objects.requireNonNull(tensaoOperacao, "tensaoOperacao não pode ser nula.");
            Objects.requireNonNull(corBadgeHex, "corBadgeHex não pode ser nula.");
        }
    }

    // =========================================================================
    // 2. FÁBRICA DE FLYWEIGHTS (FLYWEIGHT FACTORY / POOL THREAD-SAFE)
    // =========================================================================

    /**
     * Gerencia o pool de instâncias compartilhadas. Garante que nunca haverá mais de
     * uma instância de especificação para o mesmo modelo na memória.
     */
    public static class FlyweightEspecificacaoFactory {
        private static final Map<String, EspecificacaoEquipamentoFlyweight> POOL = new ConcurrentHashMap<>();
        private static final AtomicInteger TOTAL_REUSOS = new AtomicInteger(0);

        public static EspecificacaoEquipamentoFlyweight obterEspecificacao(
                String modeloChave,
                String fabricante,
                String descricao,
                String categoria,
                String tensao,
                String corBadge
        ) {
            Objects.requireNonNull(modeloChave, "modeloChave é obrigatória.");

            return POOL.compute(modeloChave, (chave, existente) -> {
                if (existente != null) {
                    TOTAL_REUSOS.incrementAndGet();
                    return existente;
                }
                return new EspecificacaoEquipamentoFlyweight(
                        chave, fabricante, descricao, categoria, tensao, corBadge
                );
            });
        }

        public static int getQuantidadeFlyweightsNoPool() {
            return POOL.size();
        }

        public static int getTotalReusos() {
            return TOTAL_REUSOS.get();
        }

        public static Map<String, EspecificacaoEquipamentoFlyweight> getPoolSnapshot() {
            return Collections.unmodifiableMap(POOL);
        }

        public static void limparPool() {
            POOL.clear();
            TOTAL_REUSOS.set(0);
        }
    }

    // =========================================================================
    // 3. CONTEXTO DO CLIENTE (ESTADO EXTRÍNSECO LEVE)
    // =========================================================================

    /**
     * Objeto de Contexto: armazena somente os atributos únicos e mutáveis de cada item,
     * mantendo uma referência (ponteiro de 64 bits) ao Flyweight compartilhado.
     */
    public static class ItemInventarioContexto {
        private final String codigoPatrimonio;
        private final String numeroSerie;
        private final String localizacaoSala;
        private boolean alocado;
        private final EspecificacaoEquipamentoFlyweight especificacao; // Referência compartilhada

        public ItemInventarioContexto(String codigoPatrimonio, String numeroSerie,
                                      String localizacaoSala, boolean alocado,
                                      EspecificacaoEquipamentoFlyweight especificacao) {
            this.codigoPatrimonio = Objects.requireNonNull(codigoPatrimonio);
            this.numeroSerie = Objects.requireNonNull(numeroSerie);
            this.localizacaoSala = Objects.requireNonNull(localizacaoSala);
            this.alocado = alocado;
            this.especificacao = Objects.requireNonNull(especificacao);
        }

        public String getCodigoPatrimonio() { return codigoPatrimonio; }
        public String getNumeroSerie() { return numeroSerie; }
        public String getLocalizacaoSala() { return localizacaoSala; }
        public boolean isAlocado() { return alocado; }
        public void setAlocado(boolean alocado) { this.alocado = alocado; }
        public EspecificacaoEquipamentoFlyweight getEspecificacao() { return especificacao; }
    }

    // =========================================================================
    // 4. INTERFACE DESKTOP JAVAFX DEMONSTRATIVA
    // =========================================================================

    private final ObservableList<ItemInventarioContexto> itensObservable = FXCollections.observableArrayList();
    private TableView<ItemInventarioContexto> tabela;
    private Label lblTotalItens;
    private Label lblFlyweightsPool;
    private Label lblTaxaCompartilhamento;
    private Label lblEconomiaEstimada;

    @Override
    public void start(Stage primaryStage) {
        BorderPane raiz = new BorderPane();
        raiz.setPadding(new Insets(14));
        raiz.setStyle("-fx-font-family: 'Segoe UI', sans-serif;");

        // Topo: Cabeçalho explicativo e Cards de Métricas de Memória
        VBox topo = new VBox(10);
        Label lblTitulo = new Label("Padrão GoF Flyweight: Otimização Extrema de Memória no JavaFX");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1b5e20;");

        GridPane gridKpis = new GridPane();
        gridKpis.setHgap(16);
        gridKpis.setVgap(8);
        gridKpis.setPadding(new Insets(10));
        gridKpis.setStyle("-fx-background-color: #f1f8e9; -fx-border-color: #aed581; -fx-border-radius: 6; -fx-background-radius: 6;");

        lblTotalItens = new Label("Total de Itens: 0");
        lblTotalItens.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        lblFlyweightsPool = new Label("Flyweights no Pool: 0");
        lblFlyweightsPool.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2e7d32;");

        lblTaxaCompartilhamento = new Label("Reusos de Memória: 0");
        lblTaxaCompartilhamento.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1565c0;");

        lblEconomiaEstimada = new Label("Economia de Instâncias: 0%");
        lblEconomiaEstimada.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #e65100;");

        gridKpis.add(lblTotalItens, 0, 0);
        gridKpis.add(lblFlyweightsPool, 1, 0);
        gridKpis.add(lblTaxaCompartilhamento, 0, 1);
        gridKpis.add(lblEconomiaEstimada, 1, 1);

        // Barra de Ações para Carga em Massa
        HBox barraAcoes = new HBox(12);
        barraAcoes.setAlignment(Pos.CENTER_LEFT);

        Button btnAdicionar100 = new Button("Gerar Lote de 100 Itens");
        btnAdicionar100.setStyle("-fx-background-color: #388e3c; -fx-text-fill: white; -fx-font-weight: bold;");
        btnAdicionar100.setOnAction(e -> gerarLoteItens(100));

        Button btnAdicionar1000 = new Button("Gerar Carga Massiva (1.000 Itens)");
        btnAdicionar1000.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold;");
        btnAdicionar1000.setOnAction(e -> gerarLoteItens(1000));

        Button btnLimpar = new Button("Limpar Catálogo e Resetar");
        btnLimpar.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold;");
        btnLimpar.setOnAction(e -> {
            itensObservable.clear();
            FlyweightEspecificacaoFactory.limparPool();
            atualizarKpis();
        });

        barraAcoes.getChildren().addAll(btnAdicionar100, btnAdicionar1000, btnLimpar);

        topo.getChildren().addAll(lblTitulo, gridKpis, barraAcoes);
        raiz.setTop(topo);

        // Centro: TableView
        tabela = construirTabela();
        raiz.setCenter(tabela);

        // Carga inicial ilustrativa
        gerarLoteItens(20);

        primaryStage.setTitle("Demonstração: GoF Flyweight e Heap Optimization no JavaFX");
        primaryStage.setScene(new Scene(raiz, 960, 560));
        primaryStage.show();
    }

    @SuppressWarnings("unchecked")
    private TableView<ItemInventarioContexto> construirTabela() {
        TableView<ItemInventarioContexto> tv = new TableView<>();
        VBox.setVgrow(tv, Priority.ALWAYS);

        TableColumn<ItemInventarioContexto, String> colPat = new TableColumn<>("Patrimônio (Extrínseco)");
        colPat.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCodigoPatrimonio()));
        colPat.setPrefWidth(160);

        TableColumn<ItemInventarioContexto, String> colSerie = new TableColumn<>("Nº de Série (Extrínseco)");
        colSerie.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNumeroSerie()));
        colSerie.setPrefWidth(150);

        TableColumn<ItemInventarioContexto, String> colSala = new TableColumn<>("Localização");
        colSala.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getLocalizacaoSala()));
        colSala.setPrefWidth(110);

        TableColumn<ItemInventarioContexto, String> colStatus = new TableColumn<>("Disponibilidade");
        colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().isAlocado() ? "ALOCADO" : "DISPONÍVEL"));
        colStatus.setPrefWidth(110);

        // Colunas alimentadas pelo Flyweight compartilhado (Intrínseco)
        TableColumn<ItemInventarioContexto, String> colModelo = new TableColumn<>("Modelo (Flyweight)");
        colModelo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEspecificacao().modeloChave()));
        colModelo.setPrefWidth(120);

        TableColumn<ItemInventarioContexto, String> colFabricante = new TableColumn<>("Fabricante");
        colFabricante.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEspecificacao().fabricante()));
        colFabricante.setPrefWidth(120);

        TableColumn<ItemInventarioContexto, EspecificacaoEquipamentoFlyweight> colBadge = new TableColumn<>("Categoria & Tensão");
        colBadge.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getEspecificacao()));
        colBadge.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(EspecificacaoEquipamentoFlyweight spec, boolean empty) {
                super.updateItem(spec, empty);
                if (empty || spec == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(spec.categoriaGeral() + " (" + spec.tensaoOperacao() + ")");
                    badge.setStyle("-fx-background-color: " + spec.corBadgeHex() + "; -fx-text-fill: white; "
                            + "-fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 11px;");
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
        colBadge.setPrefWidth(170);

        tv.getColumns().addAll(colPat, colSerie, colSala, colStatus, colModelo, colFabricante, colBadge);
        tv.setItems(itensObservable);
        return tv;
    }

    private void gerarLoteItens(int quantidade) {
        // Modelos padronizados de laboratório que serão reusados via Flyweight
        String[][] catalogoModelos = {
                {"DS1054Z", "Rigol Technologies", "Osciloscópio Digital 4 Canais 50MHz", "Medição", "Bivolt 110/220V", "#1565c0"},
                {"AFG-1022", "GW Instek", "Gerador de Funções Arbitrárias 25MHz", "Eletrônica", "110V 60Hz", "#2e7d32"},
                {"UT-61E", "Uni-Trend", "Multímetro Digital True RMS Auto-range", "Bancada", "Bateria 9V", "#e65100"},
                {"TS-100", "Miniware", "Ferro de Solda Inteligente Termocontrolado", "Soldagem", "DC 12-24V", "#6a1b9a"},
                {"PS-3005", "Minipa", "Fonte de Alimentação DC Regulável 30V 5A", "Alimentação", "220V", "#c62828"}
        };

        int indiceInicial = itensObservable.size() + 1;
        for (int i = 0; i < quantidade; i++) {
            int seq = indiceInicial + i;
            String[] specData = catalogoModelos[seq % catalogoModelos.length];

            // Pede o Flyweight à Fábrica (retorna instância compartilhada já existente no pool!)
            EspecificacaoEquipamentoFlyweight flyweight = FlyweightEspecificacaoFactory.obterEspecificacao(
                    specData[0], specData[1], specData[2], specData[3], specData[4], specData[5]
            );

            // Cria o contexto leve (estado extrínseco)
            ItemInventarioContexto item = new ItemInventarioContexto(
                    String.format("PAT-%05d", seq),
                    String.format("SN-%s-%04d", specData[0], seq),
                    "Laboratório " + ((seq % 4) + 1),
                    (seq % 3 == 0),
                    flyweight
            );
            itensObservable.add(item);
        }

        atualizarKpis();
    }

    private void atualizarKpis() {
        int totalItens = itensObservable.size();
        int totalFlyweights = FlyweightEspecificacaoFactory.getQuantidadeFlyweightsNoPool();
        int totalReusos = FlyweightEspecificacaoFactory.getTotalReusos();

        lblTotalItens.setText("Total de Itens no Catálogo: " + totalItens);
        lblFlyweightsPool.setText("Flyweights Únicos no Pool: " + totalFlyweights);
        lblTaxaCompartilhamento.setText("Reusos de Instâncias: " + totalReusos);

        if (totalItens > 0 && totalFlyweights > 0) {
            double economia = (1.0 - ((double) totalFlyweights / totalItens)) * 100.0;
            lblEconomiaEstimada.setText(String.format("Economia de Instâncias Pesadas: %.1f%%", economia));
        } else {
            lblEconomiaEstimada.setText("Economia de Instâncias: 0%");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
