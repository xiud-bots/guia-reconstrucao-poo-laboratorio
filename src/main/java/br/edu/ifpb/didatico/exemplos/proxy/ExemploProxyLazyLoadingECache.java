package br.edu.ifpb.didatico.exemplos.proxy;

import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

/**
 * Exemplo didático do Padrão GoF Proxy (Virtual Proxy com Lazy Loading e Caching).
 * Demonstra como evitar o consumo excessivo de memória RAM e congelamento da UI
 * ao gerenciar recursos pesados (manuais técnicos, diagramas de circuito e logs periciais).
 * O objeto real só é instanciado no exato instante em que seu conteúdo for solicitado.
 */
public class ExemploProxyLazyLoadingECache extends Application {

    // =========================================================================
    // 1. CONTRATO DO SUJEITO (Subject Interface)
    // =========================================================================

    public interface IRecursoDocumental {
        String getIdentificador();
        String getTitulo();
        String getCategoria();
        long getTamanhoEstimadoKb();
        boolean isCarregadoEmMemoria();
        String obterConteudoIntegral() throws FalhaCargaDocumentoException;
        String obterDigestSha256();
        long getTempoUltimaCargaMs();
    }

    public static class FalhaCargaDocumentoException extends Exception {
        public FalhaCargaDocumentoException(String mensagem, Throwable causa) {
            super(mensagem, causa);
        }
    }

    // =========================================================================
    // 2. SUJEITO REAL PESADO (Real Subject)
    // =========================================================================

    /**
     * Objeto de domínio real pesado. A instanciação ou leitura simula operação intensiva
     * de I/O em disco ou rede, ocupando alocação significativa de memória.
     */
    public static class DocumentoTecnicoReal implements IRecursoDocumental {
        private final String identificador;
        private final String titulo;
        private final String categoria;
        private final long tamanhoKb;
        private final String conteudoIntegral;
        private final String digestSha256;
        private final long tempoCargaMs;

        public DocumentoTecnicoReal(String identificador, String titulo, String categoria, long tamanhoKb) {
            long inicio = System.currentTimeMillis();
            this.identificador = Objects.requireNonNull(identificador);
            this.titulo = Objects.requireNonNull(titulo);
            this.categoria = Objects.requireNonNull(categoria);
            this.tamanhoKb = tamanhoKb;

            // Simula I/O pesado de disco ou decodificação de arquivo PDF de 50 páginas
            try {
                Thread.sleep(300); // 300ms de latência simulada de hardware/rede
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            StringBuilder sb = new StringBuilder();
            sb.append("=================================================================\n");
            sb.append("  MANUAL TÉCNICO OFICIAL DE CALIBRAÇÃO E SALVAGUARDA PATRIMONIAL\n");
            sb.append("=================================================================\n");
            sb.append("ID REGULAMENTAR : ").append(identificador).append("\n");
            sb.append("EQUIPAMENTO     : ").append(titulo).append("\n");
            sb.append("CATEGORIA       : ").append(categoria).append("\n");
            sb.append("TAMANHO EM DISCO: ").append(tamanhoKb).append(" KB\n");
            sb.append("-----------------------------------------------------------------\n");
            sb.append("1. ESPECIFICAÇÕES ELÉTRICAS E TOLERÂNCIAS\n");
            sb.append("   - Tensão nominal de entrada: 110V/220V comutada com PFC ativo.\n");
            sb.append("   - Faixa de sensibilidade: 1mV/div até 10V/div calibrada em bancada.\n");
            sb.append("   - Norma de conformidade: IEC 61010-1 (Grau de Poluição 2, CAT II).\n\n");
            sb.append("2. PROTOCOLO DE INSPEÇÃO PERIÓDICA\n");
            sb.append("   - Calibração de zero a cada 180 dias ou após transporte externo.\n");
            sb.append("   - Higienização com álcool isopropílico 99,8% nas conexões BNC.\n\n");
            sb.append("3. TERMO DE RESPONSABILIDADE OPERACIONAL\n");
            sb.append("   - Qualquer anomalia visual no display exige desarme imediato.\n");
            sb.append("=================================================================\n");
            this.conteudoIntegral = sb.toString();
            this.digestSha256 = calcularHash(conteudoIntegral);
            this.tempoCargaMs = System.currentTimeMillis() - inicio;
        }

        @Override
        public String getIdentificador() { return identificador; }
        @Override
        public String getTitulo() { return titulo; }
        @Override
        public String getCategoria() { return categoria; }
        @Override
        public long getTamanhoEstimadoKb() { return tamanhoKb; }
        @Override
        public boolean isCarregadoEmMemoria() { return true; }
        @Override
        public String obterConteudoIntegral() { return conteudoIntegral; }
        @Override
        public String obterDigestSha256() { return digestSha256; }
        @Override
        public long getTempoUltimaCargaMs() { return tempoCargaMs; }

        private static String calcularHash(String texto) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest(texto.getBytes(StandardCharsets.UTF_8));
                return HexFormat.of().formatHex(hash);
            } catch (NoSuchAlgorithmException e) {
                return Integer.toHexString(texto.hashCode());
            }
        }
    }

    // =========================================================================
    // 3. O VIRTUAL PROXY COM CACHE E LAZY LOADING
    // =========================================================================

    /**
     * Proxy Virtual: representa o documento no catálogo inicial mantendo consumo mínimo de memória.
     * O objeto real só é criado quando o conteúdo integral for expressamente acessado.
     */
    public static class ProxyDocumentoVirtual implements IRecursoDocumental {

        private final String identificador;
        private final String titulo;
        private final String categoria;
        private final long tamanhoEstimadoKb;

        // Referência volátil ao objeto real (Carregamento Preguiçoso / Lazy Loading)
        private volatile DocumentoTecnicoReal documentoReal = null;
        private final Object lock = new Object();
        private long tempoUltimaOperacaoMs = 0;

        public ProxyDocumentoVirtual(String identificador, String titulo, String categoria, long tamanhoEstimadoKb) {
            this.identificador = Objects.requireNonNull(identificador);
            this.titulo = Objects.requireNonNull(titulo);
            this.categoria = Objects.requireNonNull(categoria);
            this.tamanhoEstimadoKb = tamanhoEstimadoKb;
        }

        @Override
        public String getIdentificador() {
            return identificador;
        }

        @Override
        public String getTitulo() {
            return titulo;
        }

        @Override
        public String getCategoria() {
            return categoria;
        }

        @Override
        public long getTamanhoEstimadoKb() {
            return tamanhoEstimadoKb;
        }

        @Override
        public boolean isCarregadoEmMemoria() {
            return documentoReal != null;
        }

        @Override
        public String obterConteudoIntegral() throws FalhaCargaDocumentoException {
            long inicio = System.currentTimeMillis();
            if (documentoReal == null) {
                synchronized (lock) {
                    if (documentoReal == null) {
                        try {
                            // Instanciação sob demanda (Lazy Loading)
                            this.documentoReal = new DocumentoTecnicoReal(
                                    identificador, titulo, categoria, tamanhoEstimadoKb
                            );
                        } catch (Exception e) {
                            throw new FalhaCargaDocumentoException("Falha ao materializar documento real via Proxy: " + identificador, e);
                        }
                    }
                }
            }
            this.tempoUltimaOperacaoMs = System.currentTimeMillis() - inicio;
            return documentoReal.obterConteudoIntegral();
        }

        @Override
        public String obterDigestSha256() {
            if (documentoReal == null) {
                try {
                    obterConteudoIntegral();
                } catch (FalhaCargaDocumentoException e) {
                    return "ERRO_CARGA_HASH";
                }
            }
            return documentoReal.obterDigestSha256();
        }

        @Override
        public long getTempoUltimaCargaMs() {
            return tempoUltimaOperacaoMs;
        }

        /**
         * Permite purgar o cache em memória para testes didáticos de re-execução do Lazy Loading.
         */
        public void invalidarCache() {
            synchronized (lock) {
                this.documentoReal = null;
                this.tempoUltimaOperacaoMs = 0;
            }
        }
    }

    // =========================================================================
    // 4. INTERFACE GRÁFICA JAVAFX
    // =========================================================================

    private TableView<IRecursoDocumental> tabelaDocumentos;
    private TextArea txtExibicaoConteudo;
    private ProgressBar progressBar;
    private Label lblStatusOperacao;
    private Button btnCarregar;
    private Button btnPurgarCache;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Demonstração GoF Proxy — Virtual Proxy & Lazy Loading em JavaFX");

        BorderPane raiz = new BorderPane();
        raiz.setPadding(new Insets(15));

        // Cabeçalho
        VBox boxTopo = new VBox(6);
        Label lblTitulo = new Label("Padrão GoF Proxy: Carregamento Preguiçoso & Cache Transparente");
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a365d;");
        Label lblSub = new Label("Todos os itens na tabela abaixo iniciam como Proxies Virtuais leves.\n" +
                "O conteúdo integral pesado (I/O) só é materializado quando inspecionado sob demanda.");
        lblSub.setStyle("-fx-text-fill: #4a5568;");
        boxTopo.getChildren().addAll(lblTitulo, lblSub);
        raiz.setTop(boxTopo);

        // Centro: Tabela de Proxies e Painel de Leitura
        tabelaDocumentos = new TableView<>();
        tabelaDocumentos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<IRecursoDocumental, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdentificador()));
        colId.setPrefWidth(90);

        TableColumn<IRecursoDocumental, String> colTitulo = new TableColumn<>("Título do Manual / Recurso");
        colTitulo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitulo()));
        colTitulo.setPrefWidth(220);

        TableColumn<IRecursoDocumental, String> colCat = new TableColumn<>("Categoria");
        colCat.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategoria()));
        colCat.setPrefWidth(130);

        TableColumn<IRecursoDocumental, Number> colTam = new TableColumn<>("Tamanho");
        colTam.setCellValueFactory(d -> new SimpleLongProperty(d.getValue().getTamanhoEstimadoKb()));
        colTam.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item + " KB");
                }
            }
        });

        TableColumn<IRecursoDocumental, Boolean> colStatus = new TableColumn<>("Estado em RAM");
        colStatus.setCellValueFactory(d -> new SimpleBooleanProperty(d.getValue().isCarregadoEmMemoria()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean carregado, boolean empty) {
                super.updateItem(carregado, empty);
                if (empty || carregado == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else if (carregado) {
                    setText("✔ EM CACHE (0ms)");
                    setStyle("-fx-background-color: #c6f6d5; -fx-text-fill: #22543d; -fx-alignment: CENTER; -fx-font-weight: bold;");
                } else {
                    setText("⏳ VIRTUAL (Lazy)");
                    setStyle("-fx-background-color: #edf2f7; -fx-text-fill: #4a5568; -fx-alignment: CENTER;");
                }
            }
        });

        tabelaDocumentos.getColumns().addAll(List.of(colId, colTitulo, colCat, colTam, colStatus));

        // Dados Iniciais: coleção de Proxies Virtuais leves
        ObservableList<IRecursoDocumental> catalogo = FXCollections.observableArrayList(
                new ProxyDocumentoVirtual("DOC-101", "Manual Osciloscópio Digital 100MHz", "Eletrônica", 8450),
                new ProxyDocumentoVirtual("DOC-102", "Guia Técnico Microscópio Confocal", "Óptica", 14200),
                new ProxyDocumentoVirtual("DOC-103", "Especificação Analisador de Espectro RF", "Telecom", 9300),
                new ProxyDocumentoVirtual("DOC-104", "Calibração Estação Solda SMD Infravermelho", "Hardware", 4120),
                new ProxyDocumentoVirtual("DOC-105", "Procedimento de Câmara Climática Térmica", "Ensaios", 12800)
        );
        tabelaDocumentos.setItems(catalogo);

        // Painel Inferior: Ações e Visualização
        txtExibicaoConteudo = new TextArea();
        txtExibicaoConteudo.setEditable(false);
        txtExibicaoConteudo.setPromptText("Selecione um manual na tabela acima e clique em 'Carregar via Proxy' para acionar o Lazy Loading.");
        txtExibicaoConteudo.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 11px;");
        txtExibicaoConteudo.setPrefRowCount(10);

        btnCarregar = new Button("Carregar Conteúdo via Proxy (Lazy Load)");
        btnCarregar.setStyle("-fx-background-color: #2b6cb0; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");
        btnCarregar.setOnAction(e -> handleCarregarConteudo());

        btnPurgarCache = new Button("Limpar Cache do Item Selecionado");
        btnPurgarCache.setStyle("-fx-background-color: #718096; -fx-text-fill: white; -fx-padding: 8 16;");
        btnPurgarCache.setOnAction(e -> handlePurgarCache());

        progressBar = new ProgressBar(0);
        progressBar.setVisible(false);
        progressBar.setPrefWidth(180);

        lblStatusOperacao = new Label("Selecione um item para inspecionar.");
        lblStatusOperacao.setStyle("-fx-text-fill: #4a5568; -fx-font-style: italic;");

        HBox boxAcoes = new HBox(12, btnCarregar, btnPurgarCache, progressBar, lblStatusOperacao);
        boxAcoes.setAlignment(Pos.CENTER_LEFT);
        boxAcoes.setPadding(new Insets(10, 0, 10, 0));

        VBox centro = new VBox(10, tabelaDocumentos, boxAcoes, txtExibicaoConteudo);
        VBox.setVgrow(tabelaDocumentos, Priority.ALWAYS);
        VBox.setVgrow(txtExibicaoConteudo, Priority.ALWAYS);
        raiz.setCenter(centro);

        Scene scene = new Scene(raiz, 900, 620);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleCarregarConteudo() {
        IRecursoDocumental selecionado = tabelaDocumentos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Selecione um documento na tabela para inspecionar.");
            alert.showAndWait();
            return;
        }

        boolean jaEstavaEmCache = selecionado.isCarregadoEmMemoria();

        progressBar.setVisible(true);
        btnCarregar.setDisable(true);
        lblStatusOperacao.setText(jaEstavaEmCache ? "Acessando cache em RAM instantaneamente..." : "Disparando I/O pesado via Proxy (Background Task)...");

        // Regra de Ouro da UI: Carga pesada deve rodar em segundo plano sem congelar a tela!
        Task<String> taskCarga = new Task<>() {
            @Override
            protected String call() throws Exception {
                return selecionado.obterConteudoIntegral();
            }
        };

        taskCarga.setOnSucceeded(e -> {
            progressBar.setVisible(false);
            btnCarregar.setDisable(false);
            txtExibicaoConteudo.setText(taskCarga.getValue());

            tabelaDocumentos.refresh(); // Força atualização do badge de status na tabela

            long duracao = selecionado.getTempoUltimaCargaMs();
            if (jaEstavaEmCache) {
                lblStatusOperacao.setText(String.format("✔ Conteúdo obtido do CACHE em %d ms (Zero I/O) | SHA-256: %s...",
                        duracao, selecionado.obterDigestSha256().substring(0, 16)));
                lblStatusOperacao.setStyle("-fx-text-fill: #22543d; -fx-font-weight: bold;");
            } else {
                lblStatusOperacao.setText(String.format("✔ Lazy Loading concluído em %d ms (I/O materializado no Proxy) | SHA-256: %s...",
                        duracao, selecionado.obterDigestSha256().substring(0, 16)));
                lblStatusOperacao.setStyle("-fx-text-fill: #2b6cb0; -fx-font-weight: bold;");
            }
        });

        taskCarga.setOnFailed(e -> {
            progressBar.setVisible(false);
            btnCarregar.setDisable(false);
            lblStatusOperacao.setText("✖ Erro ao carregar: " + taskCarga.getException().getMessage());
            lblStatusOperacao.setStyle("-fx-text-fill: #c53030;");
        });

        new Thread(taskCarga, "Thread-ProxyLazyLoad").start();
    }

    private void handlePurgarCache() {
        IRecursoDocumental selecionado = tabelaDocumentos.getSelectionModel().getSelectedItem();
        if (selecionado instanceof ProxyDocumentoVirtual proxy) {
            proxy.invalidarCache();
            tabelaDocumentos.refresh();
            txtExibicaoConteudo.clear();
            lblStatusOperacao.setText("Cache do documento " + proxy.getIdentificador() + " purgado com sucesso. Voltou a ser LAZY.");
            lblStatusOperacao.setStyle("-fx-text-fill: #718096;");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
