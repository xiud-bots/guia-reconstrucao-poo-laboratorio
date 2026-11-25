package br.edu.ifpb.didatico.exemplos.javafx;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Exemplo Didático: Demonstração de Estilização Profissional com CSS no JavaFX:
 * 1. Zero 'setStyle()' inline nos controladores e células.
 * 2. Pseudo-Classes Customizadas (PseudoClass) para linhas com estado dinâmico (atrasado vs. disponível).
 * 3. Alternância dinâmica de Temas (Light / Dark Mode) em tempo real sem recriar janelas.
 */
public class ExemploCssETemasJavaFX extends Application {

    // Pseudo-classes customizadas reutilizáveis
    public static final PseudoClass ATRASADO_PSEUDO = PseudoClass.getPseudoClass("atrasado");
    public static final PseudoClass DISPONIVEL_PSEUDO = PseudoClass.getPseudoClass("disponivel");

    // Modelo didático simples para exibição
    public record ItemEstilizado(String codigo, String descricao, boolean atrasado) {}

    private final ObservableList<ItemEstilizado> itens = FXCollections.observableArrayList(
            new ItemEstilizado("IT-01", "Equipamento Alpha", false),
            new ItemEstilizado("IT-02", "Módulo Beta (Prazo Vencido)", true),
            new ItemEstilizado("IT-03", "Kit Gamma", false),
            new ItemEstilizado("IT-04", "Sensor Delta (Em Atraso Crítico)", true)
    );

    private boolean modoEscuro = false;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("painel-container");

        // --- 1. CABEÇALHO COM TÍTULO E BOTÃO DE TEMA ---
        HBox cabecalho = new HBox(12);
        cabecalho.setAlignment(Pos.CENTER_LEFT);
        cabecalho.setPadding(new Insets(0, 0, 16, 0));

        VBox titulos = new VBox(2);
        Label lblTitulo = new Label("Painel de Monitoramento com CSS & Temas");
        lblTitulo.getStyleClass().add("titulo-secao");
        Label lblSubtitulo = new Label("Demonstração de Pseudo-Classes Customizadas e Alternância Light/Dark");
        lblSubtitulo.getStyleClass().add("subtitulo-secao");
        titulos.getChildren().addAll(lblTitulo, lblSubtitulo);
        HBox.setHgrow(titulos, Priority.ALWAYS);

        Button btnToggleTema = new Button("🌙 Alternar Tema (Dark Mode)");
        btnToggleTema.getStyleClass().addAll("button", "btn-toggle-tema");

        cabecalho.getChildren().addAll(titulos, btnToggleTema);
        root.setTop(cabecalho);

        // --- 2. TABELA ESTILIZADA EXCLUSIVAMENTE POR CSS ---
        VBox cardTabela = new VBox(12);
        cardTabela.getStyleClass().add("card-visao");

        TableView<ItemEstilizado> tabela = new TableView<>();
        tabela.setItems(itens);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<ItemEstilizado, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().codigo()));
        colCodigo.setPrefWidth(120);

        TableColumn<ItemEstilizado, String> colDescricao = new TableColumn<>("Descrição");
        colDescricao.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().descricao()));
        colDescricao.setPrefWidth(300);

        TableColumn<ItemEstilizado, String> colStatus = new TableColumn<>("Estado Operacional");
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().atrasado() ? "PENDÊNCIA DE PRAZO" : "REGULAR / DISPONÍVEL"));
        colStatus.setPrefWidth(200);

        tabela.getColumns().add(colCodigo);
        tabela.getColumns().add(colDescricao);
        tabela.getColumns().add(colStatus);

        // CONFIGURAÇÃO DE PSEUDO-CLASSES NA LINHA DA TABELA (SEM SETSTYLE!)
        tabela.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(ItemEstilizado item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    pseudoClassStateChanged(ATRASADO_PSEUDO, false);
                    pseudoClassStateChanged(DISPONIVEL_PSEUDO, false);
                } else {
                    pseudoClassStateChanged(ATRASADO_PSEUDO, item.atrasado());
                    pseudoClassStateChanged(DISPONIVEL_PSEUDO, !item.atrasado());
                }
            }
        });

        cardTabela.getChildren().add(tabela);
        root.setCenter(cardTabela);

        // --- 3. RODAPÉ COM FORMULÁRIO DE TESTE ---
        HBox rodape = new HBox(10);
        rodape.setAlignment(Pos.CENTER_LEFT);
        rodape.setPadding(new Insets(16, 0, 0, 0));

        TextField txtBusca = new TextField();
        txtBusca.setPromptText("Filtrar registros ou digitar texto...");
        HBox.setHgrow(txtBusca, Priority.ALWAYS);

        Button btnAcao = new Button("Cadastrar Novo");
        btnAcao.getStyleClass().addAll("button", "btn-primario");

        rodape.getChildren().addAll(txtBusca, btnAcao);
        root.setBottom(rodape);

        // CRIAÇÃO DA CENA E VINCULAÇÃO DO TEMA INICIAL (CLARO)
        Scene scene = new Scene(root, 760, 480);
        aplicarTema(scene, false);

        // AÇÃO DO BOTÃO DE ALTERNÂNCIA DE TEMA
        btnToggleTema.setOnAction(e -> {
            modoEscuro = !modoEscuro;
            aplicarTema(scene, modoEscuro);
            btnToggleTema.setText(modoEscuro ? "☀️ Alternar Tema (Light Mode)" : "🌙 Alternar Tema (Dark Mode)");
        });

        primaryStage.setTitle("Demonstração Didática: JavaFX CSS & Dynamic Theming");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void aplicarTema(Scene scene, boolean escuro) {
        scene.getStylesheets().clear();
        String caminho = escuro ? "/css/tema-escuro.css" : "/css/tema-claro.css";
        URL url = getClass().getResource(caminho);
        if (url != null) {
            scene.getStylesheets().add(url.toExternalForm());
        } else {
            System.err.println("Aviso: Folha de estilos não encontrada: " + caminho);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
