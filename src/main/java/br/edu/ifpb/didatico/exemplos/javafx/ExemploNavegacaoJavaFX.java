package br.edu.ifpb.didatico.exemplos.javafx;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Exemplo Didático: Demonstração do Padrão Shell (Master-Detail),
 * Navegação Dinâmica desacoplada e Diálogos Tipados (Dialog com ResultConverter).
 */
public class ExemploNavegacaoJavaFX extends Application {

    // DTOs imutáveis usando Records do Java 21
    public record ItemDto(String codigo, String descricao, String categoria, int quantidade) {}

    private final ObservableList<ItemDto> catalogo = FXCollections.observableArrayList(
            new ItemDto("EQ-101", "Notebook Core i7 16GB", "Informática", 5),
            new ItemDto("EQ-102", "Projetor Multimídia 4K", "Audiovisual", 2),
            new ItemDto("EQ-103", "Kit Arduino Avançado", "Robótica", 8)
    );

    private BorderPane shellPrincipal;

    @Override
    public void start(Stage primaryStage) {
        shellPrincipal = new BorderPane();

        // 1. BARRA SUPERIOR (TOP)
        HBox topBar = new HBox(15);
        topBar.setPadding(new Insets(12, 20, 12, 20));
        topBar.setStyle("-fx-background-color: #1e293b;");
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label lblLogo = new Label("🏛️ Sistema Didático de Gestão — Padrão Shell");
        lblLogo.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px;");
        topBar.getChildren().add(lblLogo);
        shellPrincipal.setTop(topBar);

        // 2. MENU LATERAL DE NAVEGAÇÃO (LEFT)
        VBox menuLateral = new VBox(10);
        menuLateral.setPadding(new Insets(15));
        menuLateral.setPrefWidth(200);
        menuLateral.setStyle("-fx-background-color: #334155;");

        Button btnCatalogo = new Button("📦 Catálogo de Itens");
        Button btnMetricas = new Button("📊 Métricas de Acervo");
        Button btnNovoItemModal = new Button("➕ Novo Item (Dialog)");

        btnCatalogo.setMaxWidth(Double.MAX_VALUE);
        btnMetricas.setMaxWidth(Double.MAX_VALUE);
        btnNovoItemModal.setMaxWidth(Double.MAX_VALUE);

        menuLateral.getChildren().addAll(btnCatalogo, btnMetricas, btnNovoItemModal);
        shellPrincipal.setLeft(menuLateral);

        // 3. DEFINIÇÃO DAS TELAS DINÂMICAS
        Node viewCatalogo = criarViewCatalogo();
        Node viewMetricas = criarViewMetricas();

        // Conecta as rotas do menu ao painel central (Center)
        btnCatalogo.setOnAction(e -> shellPrincipal.setCenter(viewCatalogo));
        btnMetricas.setOnAction(e -> shellPrincipal.setCenter(viewMetricas));
        btnNovoItemModal.setOnAction(e -> abrirDialogNovoItem(primaryStage));

        // Inicializa com a tela de Catálogo ativa
        shellPrincipal.setCenter(viewCatalogo);

        // 4. CONFIGURAÇÃO DA CENA
        Scene scene = new Scene(shellPrincipal, 850, 520);
        primaryStage.setTitle("Demonstração: Padrão Shell e Navegação Dinâmica em JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Criação dinâmica da View de Catálogo (Tabela e Ações).
     */
    private Node criarViewCatalogo() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(20));

        Label lblTitulo = new Label("Catálogo de Itens em Inventário");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TableView<ItemDto> tabela = new TableView<>(catalogo);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(tabela, Priority.ALWAYS);

        TableColumn<ItemDto, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().codigo()));

        TableColumn<ItemDto, String> colDescricao = new TableColumn<>("Descrição");
        colDescricao.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().descricao()));

        TableColumn<ItemDto, String> colCategoria = new TableColumn<>("Categoria");
        colCategoria.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().categoria()));

        tabela.getColumns().add(colCodigo);
        tabela.getColumns().add(colDescricao);
        tabela.getColumns().add(colCategoria);

        Button btnVerDetalhes = new Button("🔍 Visualizar Ficha Detalhada");
        btnVerDetalhes.setOnAction(e -> {
            ItemDto selecionado = tabela.getSelectionModel().getSelectedItem();
            if (selecionado != null) {
                // Navega para a tela de detalhes passando o objeto tipado!
                navegarParaDetalhes(selecionado);
            } else {
                Alert alerta = new Alert(Alert.AlertType.WARNING, "Selecione um item da tabela primeiro.");
                alerta.showAndWait();
            }
        });

        container.getChildren().addAll(lblTitulo, tabela, btnVerDetalhes);
        return container;
    }

    /**
     * Criação dinâmica da View de Detalhes com Injeção Tipada de Parâmetros.
     */
    private void navegarParaDetalhes(ItemDto item) {
        VBox container = new VBox(15);
        container.setPadding(new Insets(25));
        container.setStyle("-fx-background-color: #f8fafc;");

        Label lblTitulo = new Label("Ficha Detalhada do Registro");
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.add(new Label("Identificador Patrimonial:"), 0, 0);
        grid.add(new Label(item.codigo()), 1, 0);
        grid.add(new Label("Descrição Completa:"), 0, 1);
        grid.add(new Label(item.descricao()), 1, 1);
        grid.add(new Label("Categoria Técnica:"), 0, 2);
        grid.add(new Label(item.categoria()), 1, 2);
        grid.add(new Label("Estoque Atual Disponível:"), 0, 3);
        grid.add(new Label(item.quantidade() + " unidades"), 1, 3);

        Button btnVoltar = new Button("⬅ Voltar ao Catálogo");
        btnVoltar.setOnAction(e -> shellPrincipal.setCenter(criarViewCatalogo()));

        container.getChildren().addAll(lblTitulo, grid, btnVoltar);
        shellPrincipal.setCenter(container);
    }

    /**
     * Criação dinâmica da View de Métricas.
     */
    private Node criarViewMetricas() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        Label lblTitulo = new Label("Painel de Indicadores e Métricas");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        int totalItens = catalogo.stream().mapToInt(ItemDto::quantidade).sum();
        Label lblTotal = new Label("• Quantidade Total de Itens em Acervo: " + totalItens + " unidades");
        Label lblVariedade = new Label("• Total de Títulos Cadastrados: " + catalogo.size() + " modelos");

        container.getChildren().addAll(lblTitulo, lblTotal, lblVariedade);
        return container;
    }

    /**
     * Diálogo tipado com retorno de Optional via setResultConverter.
     */
    private void abrirDialogNovoItem(Stage parent) {
        Dialog<ItemDto> dialog = new Dialog<>();
        dialog.setTitle("Cadastrar Novo Registro");
        dialog.setHeaderText("Preencha as informações do item:");
        dialog.initOwner(parent);
        dialog.initModality(Modality.APPLICATION_MODAL);

        ButtonType btnSalvar = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSalvar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtCodigo = new TextField();
        txtCodigo.setPromptText("Ex: EQ-200");
        TextField txtDesc = new TextField();
        txtDesc.setPromptText("Ex: Osciloscópio Digital");
        TextField txtCat = new TextField();
        txtCat.setPromptText("Ex: Eletrônica");

        grid.add(new Label("Código:"), 0, 0);
        grid.add(txtCodigo, 1, 0);
        grid.add(new Label("Descrição:"), 0, 1);
        grid.add(txtDesc, 1, 1);
        grid.add(new Label("Categoria:"), 0, 2);
        grid.add(txtCat, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Conversor tipado: mapeia os dados digitados para o Record imutável
        dialog.setResultConverter(btn -> {
            if (btn == btnSalvar) {
                if (txtCodigo.getText().isBlank() || txtDesc.getText().isBlank()) {
                    return null;
                }
                return new ItemDto(txtCodigo.getText().trim(), txtDesc.getText().trim(), txtCat.getText().trim(), 1);
            }
            return null;
        });

        Optional<ItemDto> resultado = dialog.showAndWait();
        resultado.ifPresent(novo -> {
            catalogo.add(novo);
            shellPrincipal.setCenter(criarViewCatalogo());
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
