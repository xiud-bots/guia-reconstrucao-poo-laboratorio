package br.edu.ifpb.didatico.exemplos.visitor;

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
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Exemplo didático do Padrão GoF Visitor aplicado a hierarquias de domínio heterogêneas e JavaFX.
 * Demonstra o uso de Double Dispatch (elemento.accept(visitor) -> visitor.visit(this)) para adicionar
 * novas operações (Depreciação Contábil, Auditoria de Conformidade e Relatórios Estruturados)
 * sem violar o Princípio Aberto/Fechado (OCP) e sem poluir as classes de entidades com código de infraestrutura.
 */
public class ExemploVisitorOperacoesPolimorficas extends Application {

    private static final NumberFormat MOEDA_BRL = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));

    // =========================================================================
    // 1. CONTRATOS DO PADRÃO VISITOR (DOUBLE DISPATCH)
    // =========================================================================

    /**
     * Interface do Visitante (Visitor): declara métodos de visitação sobrecarregados
     * para cada tipo concreto da hierarquia de elementos.
     */
    public interface IVisitorPatrimonial {
        void visit(EquipamentoHardware hardware);
        void visit(MobiliarioPatrimonial mobiliario);
        void visit(LicencaSoftware licenca);
    }

    /**
     * Interface do Elemento Visitável (Element): força cada classe concreta a implementar accept(visitor).
     */
    public interface IItemPatrimonial {
        String getCodigo();
        String getDescricao();
        BigDecimal getValorAquisicao();
        LocalDate getDataAquisicao();
        void accept(IVisitorPatrimonial visitor);
    }

    // =========================================================================
    // 2. ELEMENTOS CONCRETOS DA HIERARQUIA DE DOMÍNIO
    // =========================================================================

    public static class EquipamentoHardware implements IItemPatrimonial {
        private final String codigo;
        private final String descricao;
        private final BigDecimal valorAquisicao;
        private final LocalDate dataAquisicao;
        private final int potenciaWatts;
        private final boolean laudoEletricoValido;

        public EquipamentoHardware(String codigo, String descricao, BigDecimal valorAquisicao,
                                   LocalDate dataAquisicao, int potenciaWatts, boolean laudoEletricoValido) {
            this.codigo = Objects.requireNonNull(codigo);
            this.descricao = Objects.requireNonNull(descricao);
            this.valorAquisicao = Objects.requireNonNull(valorAquisicao);
            this.dataAquisicao = Objects.requireNonNull(dataAquisicao);
            this.potenciaWatts = potenciaWatts;
            this.laudoEletricoValido = laudoEletricoValido;
        }

        @Override
        public String getCodigo() { return codigo; }
        @Override
        public String getDescricao() { return descricao; }
        @Override
        public BigDecimal getValorAquisicao() { return valorAquisicao; }
        @Override
        public LocalDate getDataAquisicao() { return dataAquisicao; }
        public int getPotenciaWatts() { return potenciaWatts; }
        public boolean isLaudoEletricoValido() { return laudoEletricoValido; }

        @Override
        public void accept(IVisitorPatrimonial visitor) {
            // Double Dispatch: em tempo de execução, invoca o método exato do tipo Hardware
            visitor.visit(this);
        }
    }

    public static class MobiliarioPatrimonial implements IItemPatrimonial {
        private final String codigo;
        private final String descricao;
        private final BigDecimal valorAquisicao;
        private final LocalDate dataAquisicao;
        private final String tipoMaterial; // ex: Aço, Madeira Nobre, Polímero
        private final boolean ergonomiaAprovada;

        public MobiliarioPatrimonial(String codigo, String descricao, BigDecimal valorAquisicao,
                                     LocalDate dataAquisicao, String tipoMaterial, boolean ergonomiaAprovada) {
            this.codigo = Objects.requireNonNull(codigo);
            this.descricao = Objects.requireNonNull(descricao);
            this.valorAquisicao = Objects.requireNonNull(valorAquisicao);
            this.dataAquisicao = Objects.requireNonNull(dataAquisicao);
            this.tipoMaterial = Objects.requireNonNull(tipoMaterial);
            this.ergonomiaAprovada = ergonomiaAprovada;
        }

        @Override
        public String getCodigo() { return codigo; }
        @Override
        public String getDescricao() { return descricao; }
        @Override
        public BigDecimal getValorAquisicao() { return valorAquisicao; }
        @Override
        public LocalDate getDataAquisicao() { return dataAquisicao; }
        public String getTipoMaterial() { return tipoMaterial; }
        public boolean isErgonomiaAprovada() { return ergonomiaAprovada; }

        @Override
        public void accept(IVisitorPatrimonial visitor) {
            visitor.visit(this);
        }
    }

    public static class LicencaSoftware implements IItemPatrimonial {
        private final String codigo;
        private final String descricao;
        private final BigDecimal valorAquisicao;
        private final LocalDate dataAquisicao;
        private final int assentosAutorizados;
        private final LocalDate dataExpiracao;

        public LicencaSoftware(String codigo, String descricao, BigDecimal valorAquisicao,
                               LocalDate dataAquisicao, int assentosAutorizados, LocalDate dataExpiracao) {
            this.codigo = Objects.requireNonNull(codigo);
            this.descricao = Objects.requireNonNull(descricao);
            this.valorAquisicao = Objects.requireNonNull(valorAquisicao);
            this.dataAquisicao = Objects.requireNonNull(dataAquisicao);
            this.assentosAutorizados = assentosAutorizados;
            this.dataExpiracao = Objects.requireNonNull(dataExpiracao);
        }

        @Override
        public String getCodigo() { return codigo; }
        @Override
        public String getDescricao() { return descricao; }
        @Override
        public BigDecimal getValorAquisicao() { return valorAquisicao; }
        @Override
        public LocalDate getDataAquisicao() { return dataAquisicao; }
        public int getAssentosAutorizados() { return assentosAutorizados; }
        public LocalDate getDataExpiracao() { return dataExpiracao; }

        @Override
        public void accept(IVisitorPatrimonial visitor) {
            visitor.visit(this);
        }
    }

    // =========================================================================
    // 3. VISITORS CONCRETOS (OPERAÇÕES EXTERNAS DESACOPLADAS)
    // =========================================================================

    /**
     * Visitor 1: Cálculo de Depreciação Contábil Acumulada.
     * Hardware: vida útil de 5 anos (20% ao ano). Valor residual mínimo: 10%.
     * Mobiliário: vida útil de 10 anos (10% ao ano). Valor residual mínimo: 5%.
     * Software: amortização linear em 3 anos (33.3% ao ano) ou proporcional até a expiração.
     */
    public static class VisitorCalculoDepreciacao implements IVisitorPatrimonial {
        private final LocalDate dataReferencia;
        private BigDecimal totalAquisicao = BigDecimal.ZERO;
        private BigDecimal totalDepreciado = BigDecimal.ZERO;
        private BigDecimal totalResidual = BigDecimal.ZERO;
        private final List<String> discriminativo = new ArrayList<>();

        public VisitorCalculoDepreciacao(LocalDate dataReferencia) {
            this.dataReferencia = Objects.requireNonNull(dataReferencia);
        }

        @Override
        public void visit(EquipamentoHardware h) {
            long mesesUso = ChronoUnit.MONTHS.between(h.getDataAquisicao(), dataReferencia);
            if (mesesUso < 0) mesesUso = 0;

            // Taxa mensal: 20% anual = ~1.666% ao mês (vida útil 60 meses)
            double proporcao = Math.min(1.0, mesesUso / 60.0);
            BigDecimal taxaEfetiva = BigDecimal.valueOf(proporcao * 0.90); // 10% residual garantido
            BigDecimal depreciado = h.getValorAquisicao().multiply(taxaEfetiva).setScale(2, RoundingMode.HALF_UP);
            BigDecimal residual = h.getValorAquisicao().subtract(depreciado);

            totalAquisicao = totalAquisicao.add(h.getValorAquisicao());
            totalDepreciado = totalDepreciado.add(depreciado);
            totalResidual = totalResidual.add(residual);

            discriminativo.add(String.format("HARDWARE [%s] %s | Uso: %d meses | Depreciado: %s | Residual: %s",
                    h.getCodigo(), h.getDescricao(), mesesUso, MOEDA_BRL.format(depreciado), MOEDA_BRL.format(residual)));
        }

        @Override
        public void visit(MobiliarioPatrimonial m) {
            long mesesUso = ChronoUnit.MONTHS.between(m.getDataAquisicao(), dataReferencia);
            if (mesesUso < 0) mesesUso = 0;

            // Taxa mensal: 10% anual = vida útil 120 meses. Residual 5% garantido
            double proporcao = Math.min(1.0, mesesUso / 120.0);
            BigDecimal taxaEfetiva = BigDecimal.valueOf(proporcao * 0.95);
            BigDecimal depreciado = m.getValorAquisicao().multiply(taxaEfetiva).setScale(2, RoundingMode.HALF_UP);
            BigDecimal residual = m.getValorAquisicao().subtract(depreciado);

            totalAquisicao = totalAquisicao.add(m.getValorAquisicao());
            totalDepreciado = totalDepreciado.add(depreciado);
            totalResidual = totalResidual.add(residual);

            discriminativo.add(String.format("MOBILIÁRIO [%s] %s | Mat: %s | Depreciado: %s | Residual: %s",
                    m.getCodigo(), m.getDescricao(), m.getTipoMaterial(), MOEDA_BRL.format(depreciado), MOEDA_BRL.format(residual)));
        }

        @Override
        public void visit(LicencaSoftware s) {
            long diasTotais = ChronoUnit.DAYS.between(s.getDataAquisicao(), s.getDataExpiracao());
            long diasPassados = ChronoUnit.DAYS.between(s.getDataAquisicao(), dataReferencia);
            if (diasPassados < 0) diasPassados = 0;

            BigDecimal depreciado;
            BigDecimal residual;

            if (diasTotais <= 0 || diasPassados >= diasTotais) {
                depreciado = s.getValorAquisicao();
                residual = BigDecimal.ZERO;
            } else {
                double prop = (double) diasPassados / diasTotais;
                depreciado = s.getValorAquisicao().multiply(BigDecimal.valueOf(prop)).setScale(2, RoundingMode.HALF_UP);
                residual = s.getValorAquisicao().subtract(depreciado);
            }

            totalAquisicao = totalAquisicao.add(s.getValorAquisicao());
            totalDepreciado = totalDepreciado.add(depreciado);
            totalResidual = totalResidual.add(residual);

            discriminativo.add(String.format("SOFTWARE [%s] %s | Assentos: %d | Amortizado: %s | Residual: %s",
                    s.getCodigo(), s.getDescricao(), s.getAssentosAutorizados(), MOEDA_BRL.format(depreciado), MOEDA_BRL.format(residual)));
        }

        public BigDecimal getTotalAquisicao() { return totalAquisicao; }
        public BigDecimal getTotalDepreciado() { return totalDepreciado; }
        public BigDecimal getTotalResidual() { return totalResidual; }
        public List<String> getDiscriminativo() { return Collections.unmodifiableList(discriminativo); }
    }

    /**
     * Visitor 2: Auditoria de Conformidade Técnica e Regulatória.
     * Avalia riscos específicos de cada item sem condicionais feios no chamador.
     */
    public static class VisitorAuditoriaConformidade implements IVisitorPatrimonial {
        private final LocalDate dataAuditoria;
        private int totalConformes = 0;
        private int totalPendencias = 0;
        private final List<String> achadosAuditoria = new ArrayList<>();

        public VisitorAuditoriaConformidade(LocalDate dataAuditoria) {
            this.dataAuditoria = Objects.requireNonNull(dataAuditoria);
        }

        @Override
        public void visit(EquipamentoHardware h) {
            if (!h.isLaudoEletricoValido()) {
                totalPendencias++;
                achadosAuditoria.add("CRÍTICO: Hardware [" + h.getCodigo() + "] " + h.getDescricao()
                        + " com potência de " + h.getPotenciaWatts() + "W está SEM LAUDO ELÉTRICO atualizado.");
            } else {
                totalConformes++;
                achadosAuditoria.add("CONFORME: Hardware [" + h.getCodigo() + "] laudo elétrico em dia.");
            }
        }

        @Override
        public void visit(MobiliarioPatrimonial m) {
            if (!m.isErgonomiaAprovada()) {
                totalPendencias++;
                achadosAuditoria.add("ALERTA: Mobiliário [" + m.getCodigo() + "] " + m.getDescricao()
                        + " reprovado na inspeção de ergonomia (NR-17).");
            } else {
                totalConformes++;
                achadosAuditoria.add("CONFORME: Mobiliário [" + m.getCodigo() + "] aprovado para uso operacional.");
            }
        }

        @Override
        public void visit(LicencaSoftware s) {
            if (dataAuditoria.isAfter(s.getDataExpiracao())) {
                totalPendencias++;
                achadosAuditoria.add("BLOQUEIO: Licença de Software [" + s.getCodigo() + "] EXPIRADA em "
                        + s.getDataExpiracao() + ". Risco de infração de compliance de software.");
            } else {
                long diasRestantes = ChronoUnit.DAYS.between(dataAuditoria, s.getDataExpiracao());
                totalConformes++;
                achadosAuditoria.add("CONFORME: Licença [" + s.getCodigo() + "] ativa (" + diasRestantes + " dias restantes).");
            }
        }

        public int getTotalConformes() { return totalConformes; }
        public int getTotalPendencias() { return totalPendencias; }
        public List<String> getAchadosAuditoria() { return Collections.unmodifiableList(achadosAuditoria); }
    }

    // =========================================================================
    // 4. INTERFACE DESKTOP JAVAFX DEMONSTRATIVA
    // =========================================================================

    private final ObservableList<IItemPatrimonial> catalogo = FXCollections.observableArrayList();
    private TableView<IItemPatrimonial> tabela;
    private TextArea txtResultadoRelatorio;
    private Label lblResumoKpi;

    @Override
    public void start(Stage primaryStage) {
        BorderPane raiz = new BorderPane();
        raiz.setPadding(new Insets(14));
        raiz.setStyle("-fx-font-family: 'Segoe UI', sans-serif;");

        // Topo: Cabeçalho explicativo
        VBox topo = new VBox(6);
        topo.setPadding(new Insets(0, 0, 10, 0));
        Label lblTitulo = new Label("Padrão GoF Visitor: Operações Polimórficas Externas e Double Dispatch");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0d47a1;");
        Label lblSubtitulo = new Label("Adiciona cálculos de depreciação e auditorias sem modificar as classes de domínio e sem usar 'instanceof'.");
        lblSubtitulo.setStyle("-fx-text-fill: #555;");
        topo.getChildren().addAll(lblTitulo, lblSubtitulo);
        raiz.setTop(topo);

        // Centro: Tabela e Painel de Ações
        VBox centro = new VBox(10);
        tabela = construirTabela();
        carregarDados();

        HBox barraBotoes = new HBox(12);
        barraBotoes.setAlignment(Pos.CENTER_LEFT);

        Button btnDepreciacao = new Button("Calcular Depreciação (Visitor 1)");
        btnDepreciacao.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 14;");
        btnDepreciacao.setOnAction(e -> executarVisitorDepreciacao());

        Button btnAuditoria = new Button("Executar Auditoria de Conformidade (Visitor 2)");
        btnAuditoria.setStyle("-fx-background-color: #388e3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 14;");
        btnAuditoria.setOnAction(e -> executarVisitorAuditoria());

        lblResumoKpi = new Label("Clique em uma operação para despachar o Visitor sobre a hierarquia.");
        lblResumoKpi.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        barraBotoes.getChildren().addAll(btnDepreciacao, btnAuditoria, lblResumoKpi);

        txtResultadoRelatorio = new TextArea();
        txtResultadoRelatorio.setEditable(false);
        txtResultadoRelatorio.setPrefRowCount(8);
        txtResultadoRelatorio.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12px;");

        centro.getChildren().addAll(tabela, barraBotoes, txtResultadoRelatorio);
        VBox.setVgrow(tabela, Priority.ALWAYS);
        raiz.setCenter(centro);

        primaryStage.setTitle("Demonstração: GoF Visitor no JavaFX");
        primaryStage.setScene(new Scene(raiz, 940, 560));
        primaryStage.show();
    }

    @SuppressWarnings("unchecked")
    private TableView<IItemPatrimonial> construirTabela() {
        TableView<IItemPatrimonial> tv = new TableView<>();

        TableColumn<IItemPatrimonial, String> colCod = new TableColumn<>("Código");
        colCod.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCodigo()));
        colCod.setPrefWidth(90);

        TableColumn<IItemPatrimonial, String> colTipo = new TableColumn<>("Categoria de Domínio");
        colTipo.setCellValueFactory(c -> {
            IItemPatrimonial item = c.getValue();
            String tipo = item.getClass().getSimpleName().replace("Patrimonial", "");
            return new javafx.beans.property.SimpleStringProperty(tipo);
        });
        colTipo.setPrefWidth(160);

        TableColumn<IItemPatrimonial, String> colDesc = new TableColumn<>("Descrição do Ativo");
        colDesc.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDescricao()));
        colDesc.setPrefWidth(260);

        TableColumn<IItemPatrimonial, String> colValor = new TableColumn<>("Valor de Aquisição");
        colValor.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(MOEDA_BRL.format(c.getValue().getValorAquisicao())));
        colValor.setPrefWidth(140);

        TableColumn<IItemPatrimonial, String> colData = new TableColumn<>("Data Aquisição");
        colData.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDataAquisicao().toString()));
        colData.setPrefWidth(110);

        TableColumn<IItemPatrimonial, String> colDetalhes = new TableColumn<>("Especificidade");
        colDetalhes.setCellValueFactory(c -> {
            IItemPatrimonial item = c.getValue();
            if (item instanceof EquipamentoHardware h) {
                return new javafx.beans.property.SimpleStringProperty(h.getPotenciaWatts() + "W | Laudo: " + (h.isLaudoEletricoValido() ? "SIM" : "NÃO"));
            } else if (item instanceof MobiliarioPatrimonial m) {
                return new javafx.beans.property.SimpleStringProperty(m.getTipoMaterial() + " | Ergonomia: " + (m.isErgonomiaAprovada() ? "OK" : "PEND"));
            } else if (item instanceof LicencaSoftware s) {
                return new javafx.beans.property.SimpleStringProperty(s.getAssentosAutorizados() + " assentos | Expira: " + s.getDataExpiracao());
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        colDetalhes.setPrefWidth(160);

        tv.getColumns().addAll(colCod, colTipo, colDesc, colValor, colData, colDetalhes);
        tv.setItems(catalogo);
        return tv;
    }

    private void executarVisitorDepreciacao() {
        LocalDate hoje = LocalDate.now();
        VisitorCalculoDepreciacao visitor = new VisitorCalculoDepreciacao(hoje);

        // O laço uniforme não sabe qual classe é qual — accept() despacha polimorficamente via Double Dispatch!
        for (IItemPatrimonial item : catalogo) {
            item.accept(visitor);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== RELATÓRIO CONTÁBIL: DEPRECIAÇÃO ACUMULADA (VISITOR PATRIMONIAL) ===\n");
        sb.append(String.format("Data Base: %s | Total Aquisição: %s | Total Depreciado: %s | Valor Residual Total: %s\n",
                hoje, MOEDA_BRL.format(visitor.getTotalAquisicao()), MOEDA_BRL.format(visitor.getTotalDepreciado()), MOEDA_BRL.format(visitor.getTotalResidual())));
        sb.append("-----------------------------------------------------------------------------------------------------\n");
        for (String linha : visitor.getDiscriminativo()) {
            sb.append(linha).append("\n");
        }

        txtResultadoRelatorio.setText(sb.toString());
        lblResumoKpi.setText("Depreciação Total: " + MOEDA_BRL.format(visitor.getTotalDepreciado()) + " | Residual: " + MOEDA_BRL.format(visitor.getTotalResidual()));
    }

    private void executarVisitorAuditoria() {
        LocalDate hoje = LocalDate.now();
        VisitorAuditoriaConformidade visitor = new VisitorAuditoriaConformidade(hoje);

        for (IItemPatrimonial item : catalogo) {
            item.accept(visitor);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== RELATÓRIO DE AUDITORIA E COMPLIANCE TÉCNICO (VISITOR DE AUDITORIA) ===\n");
        sb.append(String.format("Data da Auditoria: %s | Conformes: %d | Pendências / Alertas: %d\n",
                hoje, visitor.getTotalConformes(), visitor.getTotalPendencias()));
        sb.append("-----------------------------------------------------------------------------------------------------\n");
        for (String achado : visitor.getAchadosAuditoria()) {
            sb.append(achado).append("\n");
        }

        txtResultadoRelatorio.setText(sb.toString());
        lblResumoKpi.setText("Auditoria: " + visitor.getTotalConformes() + " conformes / " + visitor.getTotalPendencias() + " pendências");
    }

    private void carregarDados() {
        catalogo.setAll(
                new EquipamentoHardware("HW-01", "Servidor Rack Xeon 32-Cores", new BigDecimal("18500.00"), LocalDate.now().minusMonths(24), 650, true),
                new EquipamentoHardware("HW-02", "Osciloscópio Digital Bancada", new BigDecimal("4200.00"), LocalDate.now().minusMonths(48), 85, false),
                new MobiliarioPatrimonial("MOB-01", "Cadeira Ergonômica Mesh Diretor", new BigDecimal("1200.00"), LocalDate.now().minusMonths(18), "Aço/Polímero", true),
                new MobiliarioPatrimonial("MOB-02", "Bancada Modular de Madeira", new BigDecimal("2500.00"), LocalDate.now().minusMonths(60), "Madeira Nobre", false),
                new LicencaSoftware("SW-01", "JetBrains All Products Pack Team", new BigDecimal("6400.00"), LocalDate.now().minusMonths(8), 10, LocalDate.now().plusMonths(4)),
                new LicencaSoftware("SW-02", "Matlab & Simulink Campus Suite", new BigDecimal("9800.00"), LocalDate.now().minusMonths(14), 25, LocalDate.now().minusDays(10))
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
