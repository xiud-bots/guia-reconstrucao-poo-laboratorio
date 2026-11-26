package br.edu.ifpb.didatico.exemplos.factory;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Exemplo didático dos Padrões GoF Factory Method e Abstract Factory aplicados à arquitetura JavaFX.
 * Demonstra a criação de famílias completas de objetos desacoplados (Tarifação, Políticas de Segurança e
 * Componentes de Apresentação Gráfica) sem expor classes concretas à camada de visualização.
 */
public class ExemploAbstractFactoryETemasAmbiente extends Application {

    private static final NumberFormat MOEDA_BRL = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));

    // =========================================================================
    // 1. MODELO DE DOMÍNIO E REQUISIÇÃO
    // =========================================================================

    public record RequisicaoAlocacao(
            String idRequisicao,
            String codigoAtivo,
            String descricaoAtivo,
            int diasAlocados,
            boolean possuiCertificacaoOperador
    ) {
        public RequisicaoAlocacao {
            Objects.requireNonNull(idRequisicao, "idRequisicao não pode ser nulo.");
            Objects.requireNonNull(codigoAtivo, "codigoAtivo não pode ser nulo.");
            Objects.requireNonNull(descricaoAtivo, "descricaoAtivo não pode ser nula.");
            if (diasAlocados <= 0) {
                throw new IllegalArgumentException("Dias alocados devem ser maiores que zero.");
            }
        }
    }

    public record ParecerConformidade(
            boolean aprovado,
            String justificativa,
            int prazoMaximoPermitido
    ) {}

    // =========================================================================
    // 2. CONTRATOS DA FAMÍLIA DE PRODUTOS (ABSTRACT PRODUCTS)
    // =========================================================================

    /**
     * Produto Abstrato A: Estratégia de cálculo tarifário específico do ambiente.
     */
    public interface ICalculadoraTarifa {
        BigDecimal calcularCustoTotal(int dias, BigDecimal valorBaseDiaria);
        String getDescricaoRegra();
    }

    /**
     * Produto Abstrato B: Política de regras regulamentares e segurança do ambiente.
     */
    public interface IPoliticaSeguranca {
        ParecerConformidade avaliarRequisicao(RequisicaoAlocacao requisicao);
    }

    /**
     * Produto Abstrato C: Renderizador de componentes visuais JavaFX (Badges e Estilos).
     */
    public interface IRenderizadorBadgeUI {
        Label criarBadgeStatus(ParecerConformidade parecer);
        String getEstiloContainer();
    }

    // =========================================================================
    // 3. CONTRATO DA FÁBRICA ABSTRATA (ABSTRACT FACTORY)
    // =========================================================================

    /**
     * Fábrica Abstrata GoF que define o contrato para criar a família completa de componentes.
     */
    public interface IAmbienteOperacionalFactory {
        String getNomeAmbiente();
        ICalculadoraTarifa criarCalculadora();
        IPoliticaSeguranca criarPoliticaSeguranca();
        IRenderizadorBadgeUI criarRenderizadorBadge();
    }

    public enum TipoAmbiente {
        EDUCACIONAL("Ambiente Acadêmico / Educacional"),
        CORPORATIVO("Ambiente Corporativo / Comercial"),
        INDUSTRIAL_CRITICO("Ambiente Industrial / Alta Criticidade");

        private final String descricao;

        TipoAmbiente(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }

        @Override
        public String toString() {
            return descricao;
        }
    }

    // =========================================================================
    // 4. IMPLEMENTAÇÕES CONCRETAS: FAMÍLIA EDUCACIONAL
    // =========================================================================

    public static class CalculadoraTarifaEducacional implements ICalculadoraTarifa {
        @Override
        public BigDecimal calcularCustoTotal(int dias, BigDecimal valorBaseDiaria) {
            // Desconto pedagógico de 50% para apoiar o ensino
            BigDecimal base = valorBaseDiaria.multiply(BigDecimal.valueOf(dias));
            return base.multiply(new BigDecimal("0.50")).setScale(2, RoundingMode.HALF_UP);
        }

        @Override
        public String getDescricaoRegra() {
            return "Subsidio Acadêmico (50% de desconto institucional)";
        }
    }

    public static class PoliticaSegurancaEducacional implements IPoliticaSeguranca {
        @Override
        public ParecerConformidade avaliarRequisicao(RequisicaoAlocacao req) {
            if (req.diasAlocados() > 15) {
                return new ParecerConformidade(false, "Prazo excede limite acadêmico de 15 dias sem aprovação docente.", 15);
            }
            return new ParecerConformidade(true, "Aprovado: alocação sob diretrizes pedagógicas institucionais.", 15);
        }
    }

    public static class RenderizadorBadgeEducacional implements IRenderizadorBadgeUI {
        @Override
        public Label criarBadgeStatus(ParecerConformidade parecer) {
            Label badge = new Label(parecer.aprovado() ? "ACADÊMICO: REGULAR" : "ACADÊMICO: EXCEDIDO");
            String cor = parecer.aprovado() ? "#2e7d32" : "#c62828";
            badge.setStyle("-fx-background-color: " + cor + "; -fx-text-fill: white; "
                    + "-fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 12; -fx-font-size: 11px;");
            return badge;
        }

        @Override
        public String getEstiloContainer() {
            return "-fx-background-color: #f1f8e9; -fx-border-color: #81c784; -fx-border-width: 1; -fx-border-radius: 6;";
        }
    }

    public static class AmbienteEducacionalFactory implements IAmbienteOperacionalFactory {
        @Override
        public String getNomeAmbiente() {
            return "Acadêmico / IFPB";
        }

        @Override
        public ICalculadoraTarifa criarCalculadora() {
            return new CalculadoraTarifaEducacional();
        }

        @Override
        public IPoliticaSeguranca criarPoliticaSeguranca() {
            return new PoliticaSegurancaEducacional();
        }

        @Override
        public IRenderizadorBadgeUI criarRenderizadorBadge() {
            return new RenderizadorBadgeEducacional();
        }
    }

    // =========================================================================
    // 5. IMPLEMENTAÇÕES CONCRETAS: FAMÍLIA CORPORATIVA
    // =========================================================================

    public static class CalculadoraTarifaCorporativa implements ICalculadoraTarifa {
        @Override
        public BigDecimal calcularCustoTotal(int dias, BigDecimal valorBaseDiaria) {
            BigDecimal base = valorBaseDiaria.multiply(BigDecimal.valueOf(dias));
            // Taxa de gestão de SLA comercial (+15%)
            return base.multiply(new BigDecimal("1.15")).setScale(2, RoundingMode.HALF_UP);
        }

        @Override
        public String getDescricaoRegra() {
            return "Tarifa Comercial Padrão com Suporte 24h (+15% SLA)";
        }
    }

    public static class PoliticaSegurancaCorporativa implements IPoliticaSeguranca {
        @Override
        public ParecerConformidade avaliarRequisicao(RequisicaoAlocacao req) {
            if (req.diasAlocados() > 30) {
                return new ParecerConformidade(false, "Prazo comercial máximo contínuo é de 30 dias.", 30);
            }
            return new ParecerConformidade(true, "Aprovado: aderente à política de faturamento corporativo.", 30);
        }
    }

    public static class RenderizadorBadgeCorporativo implements IRenderizadorBadgeUI {
        @Override
        public Label criarBadgeStatus(ParecerConformidade parecer) {
            Label badge = new Label(parecer.aprovado() ? "CORP: SLA OK" : "CORP: BLOQUEADO");
            String cor = parecer.aprovado() ? "#1565c0" : "#d32f2f";
            badge.setStyle("-fx-background-color: " + cor + "; -fx-text-fill: white; "
                    + "-fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 4; -fx-font-size: 11px;");
            return badge;
        }

        @Override
        public String getEstiloContainer() {
            return "-fx-background-color: #e3f2fd; -fx-border-color: #64b5f6; -fx-border-width: 1; -fx-border-radius: 6;";
        }
    }

    public static class AmbienteCorporativoFactory implements IAmbienteOperacionalFactory {
        @Override
        public String getNomeAmbiente() {
            return "Corporativo Enterprise";
        }

        @Override
        public ICalculadoraTarifa criarCalculadora() {
            return new CalculadoraTarifaCorporativa();
        }

        @Override
        public IPoliticaSeguranca criarPoliticaSeguranca() {
            return new PoliticaSegurancaCorporativa();
        }

        @Override
        public IRenderizadorBadgeUI criarRenderizadorBadge() {
            return new RenderizadorBadgeCorporativo();
        }
    }

    // =========================================================================
    // 6. IMPLEMENTAÇÕES CONCRETAS: FAMÍLIA INDUSTRIAL DE ALTA CRITICIDADE
    // =========================================================================

    public static class CalculadoraTarifaIndustrial implements ICalculadoraTarifa {
        @Override
        public BigDecimal calcularCustoTotal(int dias, BigDecimal valorBaseDiaria) {
            BigDecimal base = valorBaseDiaria.multiply(BigDecimal.valueOf(dias));
            // Taxa de seguro de risco e recalibração técnica obrigatória (+40%)
            return base.multiply(new BigDecimal("1.40")).setScale(2, RoundingMode.HALF_UP);
        }

        @Override
        public String getDescricaoRegra() {
            return "Tarifa Industrial Pesada (+40% Seguro e Recalibração)";
        }
    }

    public static class PoliticaSegurancaIndustrial implements IPoliticaSeguranca {
        @Override
        public ParecerConformidade avaliarRequisicao(RequisicaoAlocacao req) {
            if (!req.possuiCertificacaoOperador()) {
                return new ParecerConformidade(false, "REPROVADO: Operador não possui certificado de segurança NR-10/NR-12.", 7);
            }
            if (req.diasAlocados() > 7) {
                return new ParecerConformidade(false, "REPROVADO: Equipamentos industriais exigem inspeção semanal (máx 7 dias).", 7);
            }
            return new ParecerConformidade(true, "APROVADO: Certificação técnica validada e ciclo de segurança em conformidade.", 7);
        }
    }

    public static class RenderizadorBadgeIndustrial implements IRenderizadorBadgeUI {
        @Override
        public Label criarBadgeStatus(ParecerConformidade parecer) {
            Label badge = new Label(parecer.aprovado() ? "NR-12: EM CONFORMIDADE" : "NR-12: NÃO CONFORME");
            String cor = parecer.aprovado() ? "#e65100" : "#b71c1c";
            badge.setStyle("-fx-background-color: " + cor + "; -fx-text-fill: white; "
                    + "-fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 0; -fx-font-size: 11px;");
            return badge;
        }

        @Override
        public String getEstiloContainer() {
            return "-fx-background-color: #fff3e0; -fx-border-color: #ffb74d; -fx-border-width: 1; -fx-border-radius: 6;";
        }
    }

    public static class AmbienteIndustrialFactory implements IAmbienteOperacionalFactory {
        @Override
        public String getNomeAmbiente() {
            return "Industrial / Alta Criticidade";
        }

        @Override
        public ICalculadoraTarifa criarCalculadora() {
            return new CalculadoraTarifaIndustrial();
        }

        @Override
        public IPoliticaSeguranca criarPoliticaSeguranca() {
            return new PoliticaSegurancaIndustrial();
        }

        @Override
        public IRenderizadorBadgeUI criarRenderizadorBadge() {
            return new RenderizadorBadgeIndustrial();
        }
    }

    // =========================================================================
    // 7. PROVEDOR / SELETOR POLIMÓRFICO DE FÁBRICA (FACTORY METHOD / REGISTRY)
    // =========================================================================

    public static class ProvedorFabricaAmbiente {
        public static IAmbienteOperacionalFactory obterFabrica(TipoAmbiente tipo) {
            Objects.requireNonNull(tipo, "Tipo de ambiente não pode ser nulo.");
            return switch (tipo) {
                case EDUCACIONAL -> new AmbienteEducacionalFactory();
                case CORPORATIVO -> new AmbienteCorporativoFactory();
                case INDUSTRIAL_CRITICO -> new AmbienteIndustrialFactory();
            };
        }
    }

    // =========================================================================
    // 8. INTERFACE DESKTOP JAVAFX DEMONSTRATIVA
    // =========================================================================

    private IAmbienteOperacionalFactory fabricaAtiva;
    private final ObservableList<RequisicaoAlocacao> dados = FXCollections.observableArrayList();
    private TableView<RequisicaoAlocacao> tabela;
    private Label lblDescricaoRegra;
    private VBox painelCabecalho;

    @Override
    public void start(Stage primaryStage) {
        fabricaAtiva = ProvedorFabricaAmbiente.obterFabrica(TipoAmbiente.EDUCACIONAL);

        BorderPane raiz = new BorderPane();
        raiz.setPadding(new Insets(14));
        raiz.setStyle("-fx-font-family: 'Segoe UI', sans-serif;");

        // Painel Superior: Seletor de Fábrica Abstrata
        painelCabecalho = new VBox(8);
        painelCabecalho.setPadding(new Insets(12));
        atualizarEstiloCabecalho();

        Label lblTitulo = new Label("Padrão GoF Abstract Factory: Famílias de Regras e UI");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");

        HBox barraSelecao = new HBox(12);
        barraSelecao.setAlignment(Pos.CENTER_LEFT);

        Label lblSeletor = new Label("Selecione o Ambiente Operacional:");
        lblSeletor.setStyle("-fx-font-weight: bold;");

        ComboBox<TipoAmbiente> comboAmbiente = new ComboBox<>(FXCollections.observableArrayList(TipoAmbiente.values()));
        comboAmbiente.setValue(TipoAmbiente.EDUCACIONAL);

        lblDescricaoRegra = new Label();
        atualizarDescricaoRegra();

        comboAmbiente.setOnAction(e -> {
            TipoAmbiente selecionado = comboAmbiente.getValue();
            if (selecionado != null) {
                this.fabricaAtiva = ProvedorFabricaAmbiente.obterFabrica(selecionado);
                atualizarDescricaoRegra();
                atualizarEstiloCabecalho();
                tabela.refresh();
            }
        });

        barraSelecao.getChildren().addAll(lblSeletor, comboAmbiente);
        painelCabecalho.getChildren().addAll(lblTitulo, barraSelecao, lblDescricaoRegra);
        raiz.setTop(painelCabecalho);

        // Centro: TableView desacoplada das implementações concretas
        tabela = construirTabela();
        carregarDadosExemplo();
        raiz.setCenter(tabela);

        // Rodapé Informativo
        HBox rodape = new HBox(10);
        rodape.setPadding(new Insets(10, 0, 0, 0));
        rodape.setAlignment(Pos.CENTER_LEFT);
        Label lblInfo = new Label("A interface gráfica consome apenas contratos abstratos (IAmbienteOperacionalFactory) — Zero 'if/switch' no Controller.");
        lblInfo.setStyle("-fx-font-style: italic; -fx-text-fill: #555;");
        rodape.getChildren().add(lblInfo);
        raiz.setBottom(rodape);

        primaryStage.setTitle("Demonstração: GoF Abstract Factory no JavaFX");
        primaryStage.setScene(new Scene(raiz, 920, 520));
        primaryStage.show();
    }

    private void atualizarDescricaoRegra() {
        ICalculadoraTarifa calc = fabricaAtiva.criarCalculadora();
        lblDescricaoRegra.setText("Família Ativa: [" + fabricaAtiva.getNomeAmbiente() + "] | Regra Tarifária: " + calc.getDescricaoRegra());
        lblDescricaoRegra.setStyle("-fx-font-size: 12px; -fx-text-fill: #333; -fx-font-weight: bold;");
    }

    private void atualizarEstiloCabecalho() {
        IRenderizadorBadgeUI renderizador = fabricaAtiva.criarRenderizadorBadge();
        painelCabecalho.setStyle(renderizador.getEstiloContainer());
    }

    @SuppressWarnings("unchecked")
    private TableView<RequisicaoAlocacao> construirTabela() {
        TableView<RequisicaoAlocacao> tv = new TableView<>();
        VBox.setVgrow(tv, Priority.ALWAYS);

        TableColumn<RequisicaoAlocacao, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().idRequisicao()));
        colId.setPrefWidth(70);

        TableColumn<RequisicaoAlocacao, String> colAtivo = new TableColumn<>("Ativo Solicitado");
        colAtivo.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().codigoAtivo() + " - " + cell.getValue().descricaoAtivo()
        ));
        colAtivo.setPrefWidth(220);

        TableColumn<RequisicaoAlocacao, String> colDias = new TableColumn<>("Dias");
        colDias.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().diasAlocados() + " dias"));
        colDias.setPrefWidth(70);

        TableColumn<RequisicaoAlocacao, String> colCert = new TableColumn<>("Operador NR?");
        colCert.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().possuiCertificacaoOperador() ? "SIM (Certificado)" : "NÃO"
        ));
        colCert.setPrefWidth(120);

        // Coluna Dinâmica de Tarifa Calculada pela Abstract Factory
        TableColumn<RequisicaoAlocacao, String> colCusto = new TableColumn<>("Custo Calculado");
        colCusto.setCellValueFactory(cell -> {
            RequisicaoAlocacao req = cell.getValue();
            BigDecimal baseDiaria = new BigDecimal("80.00");
            BigDecimal total = fabricaAtiva.criarCalculadora().calcularCustoTotal(req.diasAlocados(), baseDiaria);
            return new javafx.beans.property.SimpleStringProperty(MOEDA_BRL.format(total));
        });
        colCusto.setPrefWidth(130);

        // Coluna Dinâmica de Status / Badge gerado pelo Produto de Apresentação da Fábrica
        TableColumn<RequisicaoAlocacao, ParecerConformidade> colStatus = new TableColumn<>("Parecer do Ambiente");
        colStatus.setCellValueFactory(cell -> {
            ParecerConformidade parecer = fabricaAtiva.criarPoliticaSeguranca().avaliarRequisicao(cell.getValue());
            return new javafx.beans.property.SimpleObjectProperty<>(parecer);
        });
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(ParecerConformidade parecer, boolean empty) {
                super.updateItem(parecer, empty);
                if (empty || parecer == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = fabricaAtiva.criarRenderizadorBadge().criarBadgeStatus(parecer);
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
        colStatus.setPrefWidth(180);

        tv.getColumns().addAll(colId, colAtivo, colDias, colCert, colCusto, colStatus);
        tv.setItems(dados);
        return tv;
    }

    private void carregarDadosExemplo() {
        dados.setAll(
                new RequisicaoAlocacao("REQ-001", "OSC-3000", "Osciloscópio Digital 100MHz", 5, true),
                new RequisicaoAlocacao("REQ-002", "GER-400", "Gerador de Funções Arbitrárias", 12, false),
                new RequisicaoAlocacao("REQ-003", "CNC-01", "Fresadora CNC de Alta Precisão", 20, false),
                new RequisicaoAlocacao("REQ-004", "TOR-90", "Torno Mecânico Industrial", 6, true),
                new RequisicaoAlocacao("REQ-005", "EST-50", "Estação de Solda ESD Micro", 35, true)
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
