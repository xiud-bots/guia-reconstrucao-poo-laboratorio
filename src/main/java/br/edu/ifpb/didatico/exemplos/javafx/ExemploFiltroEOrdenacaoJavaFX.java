package br.edu.ifpb.didatico.exemplos.javafx;

import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

/**
 * Demonstração Didática do Guia 17:
 * Pipeline Reativo com FilteredList, SortedList, Filtros Combinados (Texto + ComboBox)
 * e Coluna Customizada de Ações com Confirmação em TableView.
 */
public class ExemploFiltroEOrdenacaoJavaFX extends Application {

    // Modelo de Domínio Genérico Didático
    public static class DispositivoRecurso {
        private final SimpleStringProperty codigo;
        private final SimpleStringProperty descricao;
        private final SimpleStringProperty categoria;
        private final SimpleBooleanProperty emprestado;
        private final SimpleObjectProperty<BigDecimal> diaria;

        public DispositivoRecurso(String codigo, String descricao, String categoria, boolean emprestado, BigDecimal diaria) {
            this.codigo = new SimpleStringProperty(codigo);
            this.descricao = new SimpleStringProperty(descricao);
            this.categoria = new SimpleStringProperty(categoria);
            this.emprestado = new SimpleBooleanProperty(emprestado);
            this.diaria = new SimpleObjectProperty<>(diaria);
        }

        public String getCodigo() { return codigo.get(); }
        public String getDescricao() { return descricao.get(); }
        public String getCategoria() { return categoria.get(); }
        public boolean isEmprestado() { return emprestado.get(); }
        public void setEmprestado(boolean valor) { this.emprestado.set(valor); }
        public BigDecimal getDiaria() { return diaria.get(); }

        public SimpleStringProperty codigoProperty() { return codigo; }
        public SimpleStringProperty descricaoProperty() { return descricao; }
        public SimpleStringProperty categoriaProperty() { return categoria; }
        public SimpleBooleanProperty emprestadoProperty() { return emprestado; }
        public SimpleObjectProperty<BigDecimal> diariaProperty() { return diaria; }
    }

    private final ObservableList<DispositivoRecurso> dadosMestres = FXCollections.observableArrayList();
    private FilteredList<DispositivoRecurso> dadosFiltrados;
    private SortedList<DispositivoRecurso> dadosOrdenados;
    private final Label lblContador = new Label();
    private final Locale localeBr = new Locale.Builder().setLanguage("pt").setRegion("BR").build();
    private final NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(localeBr);

    @Override
    public void start(Stage palcoPrincipal) {
        palcoPrincipal.setTitle("Demonstração Didática — Filtros, SortedList e Ações em Tabela");

        // 1. Carga inicial de dados didáticos
        popularDadosIniciais();

        // 2. Construção do Pipeline Reativo: ObservableList -> FilteredList -> SortedList -> TableView
        dadosFiltrados = new FilteredList<>(dadosMestres, p -> true);
        dadosOrdenados = new SortedList<>(dadosFiltrados);

        // 3. Barra de Controles (Busca Textual + Filtro por ComboBox)
        TextField txtBusca = new TextField();
        txtBusca.setPromptText("Filtrar por código ou descrição...");
        HBox.setHgrow(txtBusca, Priority.ALWAYS);

        ComboBox<String> cbFiltroStatus = new ComboBox<>();
        cbFiltroStatus.getItems().addAll("Todos os Status", "Disponíveis Apenas", "Emprestados Apenas");
        cbFiltroStatus.getSelectionModel().selectFirst();

        Button btnResetar = new Button("Limpar Filtros");
        btnResetar.setOnAction(e -> {
            txtBusca.clear();
            cbFiltroStatus.getSelectionModel().selectFirst();
        });

        HBox barraFiltros = new HBox(10, new Label("Buscar:"), txtBusca, new Label("Status:"), cbFiltroStatus, btnResetar);
        barraFiltros.setAlignment(Pos.CENTER_LEFT);
        barraFiltros.setPadding(new Insets(10));
        barraFiltros.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");

        // 4. Criação da Tabela e Colunas
        TableView<DispositivoRecurso> tabela = new TableView<>();
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<DispositivoRecurso, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(cell -> cell.getValue().codigoProperty());
        colCodigo.setPrefWidth(90);

        TableColumn<DispositivoRecurso, String> colDescricao = new TableColumn<>("Descrição do Recurso");
        colDescricao.setCellValueFactory(cell -> cell.getValue().descricaoProperty());
        colDescricao.setPrefWidth(220);

        TableColumn<DispositivoRecurso, String> colCategoria = new TableColumn<>("Categoria");
        colCategoria.setCellValueFactory(cell -> cell.getValue().categoriaProperty());
        colCategoria.setPrefWidth(120);

        TableColumn<DispositivoRecurso, BigDecimal> colDiaria = new TableColumn<>("Diária Estimada");
        colDiaria.setCellValueFactory(cell -> cell.getValue().diariaProperty());
        colDiaria.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                if (empty || valor == null) {
                    setText(null);
                } else {
                    setText(formatoMoeda.format(valor));
                    setAlignment(Pos.CENTER_RIGHT);
                }
            }
        });

        TableColumn<DispositivoRecurso, Boolean> colStatus = new TableColumn<>("Situação");
        colStatus.setCellValueFactory(cell -> cell.getValue().emprestadoProperty());
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean emprestado, boolean empty) {
                super.updateItem(emprestado, empty);
                if (empty || emprestado == null) {
                    setText(null);
                    setStyle("");
                } else {
                    if (emprestado) {
                        setText("EMPRESTADO");
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setText("DISPONÍVEL");
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    }
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // 5. Coluna de Ações Customizada com Botões de Alternância e Exclusão
        TableColumn<DispositivoRecurso, Void> colAcoes = new TableColumn<>("Ações");
        colAcoes.setPrefWidth(180);
        colAcoes.setSortable(false);
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnAlternar = new Button();
            private final Button btnExcluir = new Button("Excluir");
            private final HBox painel = new HBox(6, btnAlternar, btnExcluir);

            {
                painel.setAlignment(Pos.CENTER);
                btnExcluir.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 11px;");
                btnAlternar.setStyle("-fx-background-color: #0d6efd; -fx-text-fill: white; -fx-font-size: 11px;");

                btnExcluir.setOnAction(e -> {
                    DispositivoRecurso item = getTableRow().getItem();
                    if (item != null) {
                        confirmarExclusao(item);
                    }
                });

                btnAlternar.setOnAction(e -> {
                    DispositivoRecurso item = getTableRow().getItem();
                    if (item != null) {
                        item.setEmprestado(!item.isEmprestado());
                        atualizarPredicado(txtBusca.getText(), cbFiltroStatus.getValue());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    DispositivoRecurso atual = getTableRow().getItem();
                    btnAlternar.setText(atual.isEmprestado() ? "Devolver" : "Emprestar");
                    setGraphic(painel);
                }
            }
        });

        tabela.getColumns().addAll(colCodigo, colDescricao, colCategoria, colDiaria, colStatus, colAcoes);

        // 6. Conexão do Comparator da SortedList ao da TableView
        dadosOrdenados.comparatorProperty().bind(tabela.comparatorProperty());
        tabela.setItems(dadosOrdenados);

        // 7. Configuração dos Ouvintes Reativos de Busca e Filtro
        txtBusca.textProperty().addListener((obs, antigo, novo) ->
                atualizarPredicado(novo, cbFiltroStatus.getValue()));

        cbFiltroStatus.valueProperty().addListener((obs, antigo, novo) ->
                atualizarPredicado(txtBusca.getText(), novo));

        atualizarContador();

        // 8. Rodapé Informativo
        lblContador.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
        HBox rodape = new HBox(lblContador);
        rodape.setPadding(new Insets(8, 12, 8, 12));
        rodape.setStyle("-fx-background-color: #e9ecef;");

        // 9. Montagem do Layout Principal
        BorderPane layout = new BorderPane();
        layout.setTop(barraFiltros);
        layout.setCenter(tabela);
        layout.setBottom(rodape);

        Scene cena = new Scene(layout, 820, 480);
        palcoPrincipal.setScene(cena);
        palcoPrincipal.show();
    }

    private void atualizarPredicado(String termoBusca, String statusFiltro) {
        final String termo = (termoBusca == null) ? "" : termoBusca.trim().toLowerCase();

        dadosFiltrados.setPredicate(dispositivo -> {
            boolean bateTexto = termo.isEmpty()
                    || dispositivo.getCodigo().toLowerCase().contains(termo)
                    || dispositivo.getDescricao().toLowerCase().contains(termo)
                    || dispositivo.getCategoria().toLowerCase().contains(termo);

            boolean bateStatus = true;
            if ("Disponíveis Apenas".equals(statusFiltro)) {
                bateStatus = !dispositivo.isEmprestado();
            } else if ("Emprestados Apenas".equals(statusFiltro)) {
                bateStatus = dispositivo.isEmprestado();
            }

            return bateTexto && bateStatus;
        });

        atualizarContador();
    }

    private void atualizarContador() {
        lblContador.setText(String.format("Exibindo %d de %d registros filtrados dinamicamente.",
                dadosFiltrados.size(), dadosMestres.size()));
    }

    private void confirmarExclusao(DispositivoRecurso item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Deseja realmente remover o dispositivo?");
        alert.setContentText(String.format("Código: %s\nDescrição: %s", item.getCodigo(), item.getDescricao()));

        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            // Remove da lista mestre; o FilteredList e SortedList atualizam instantaneamente!
            dadosMestres.remove(item);
            atualizarContador();
        }
    }

    private void popularDadosIniciais() {
        dadosMestres.addAll(
                new DispositivoRecurso("DISP-001", "Notebook Corporativo Dell XPS 15", "Informática", false, new BigDecimal("45.00")),
                new DispositivoRecurso("DISP-002", "Projetor Laser Epson WUXGA 4000 Lumens", "Audiovisual", true, new BigDecimal("35.00")),
                new DispositivoRecurso("DISP-003", "Câmera Canon EOS 4K Mirrorless", "Fotografia", false, new BigDecimal("60.00")),
                new DispositivoRecurso("DISP-004", "Gravador de Áudio Zoom H6 Pro", "Audiovisual", false, new BigDecimal("25.00")),
                new DispositivoRecurso("DISP-005", "Tablet iPad Pro M2 com Caneta", "Informática", true, new BigDecimal("40.00")),
                new DispositivoRecurso("DISP-006", "Switch Gerenciável 24 Portas Gigabit", "Redes", false, new BigDecimal("30.00"))
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
