package br.edu.ifpb.didatico.exemplos.javafx;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Exemplo Didático: Demonstração de Data Binding Reativo e Propriedades Observáveis em JavaFX.
 * Mostra como vincular propriedades (unidirecional e bidirecional) e desabilitar botões
 * de forma declarativa e fluente sem listeners imperativos cheios de if/else.
 */
public class ExemploDataBindingJavaFX extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Demonstração Didática: Data Binding Reativo em JavaFX");

        VBox raiz = new VBox(20);
        raiz.setPadding(new Insets(25));
        raiz.setAlignment(Pos.TOP_CENTER);

        Label lblTitulo = new Label("Formulário Reativo com JavaFX Data Binding");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // 1. Grid do Formulário
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setAlignment(Pos.CENTER);

        TextField txtCodigo = new TextField();
        txtCodigo.setPromptText("Ex: PRD-100");

        TextField txtDescricao = new TextField();
        txtDescricao.setPromptText("Ex: Teclado Mecânico");

        grid.add(new Label("Código:"), 0, 0);
        grid.add(txtCodigo, 1, 0);
        grid.add(new Label("Descrição:"), 0, 1);
        grid.add(txtDescricao, 1, 1);

        // 2. BINDING DECLARATIVO DE VALIDAÇÃO:
        // O botão 'Cadastrar' fica desabilitado se qualquer um dos campos estiver vazio.
        Button btnCadastrar = new Button("Cadastrar Produto");
        btnCadastrar.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold;");
        btnCadastrar.disableProperty().bind(
                txtCodigo.textProperty().isEmpty()
                        .or(txtDescricao.textProperty().isEmpty())
        );

        btnCadastrar.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Cadastro Efetuado");
            alert.setHeaderText("Validação Reativa Aprovada");
            alert.setContentText(String.format("Código: %s%nDescrição: %s", txtCodigo.getText(), txtDescricao.getText()));
            alert.showAndWait();
            txtCodigo.clear();
            txtDescricao.clear();
        });

        // 3. BINDING UNIDIRECIONAL DE ESPELHAMENTO:
        // Exibe um resumo em tempo real do que está sendo digitado
        Label lblResumo = new Label();
        lblResumo.setStyle("-fx-font-style: italic; -fx-text-fill: #7f8c8d;");
        lblResumo.textProperty().bind(
                Bindings.concat("Pré-visualização: [", txtCodigo.textProperty(), "] ", txtDescricao.textProperty())
        );

        // 4. BINDING COM SLIDER E VALORES NUMÉRICOS:
        Label lblSliderTitulo = new Label("Ajuste de Quantidade / Nível:");
        Slider slider = new Slider(1, 100, 10);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);

        Label lblValorSlider = new Label();
        lblValorSlider.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");
        lblValorSlider.textProperty().bind(
                Bindings.format("Quantidade Selecionada: %.0f unidades", slider.valueProperty())
        );

        // 5. Escuta ativa com ChangeListener para fins didáticos
        txtCodigo.textProperty().addListener((obs, antigo, novo) -> {
            if (novo != null && novo.contains(" ")) {
                txtCodigo.setText(novo.replace(" ", "")); // Remove espaços em branco reativamente
            }
        });

        raiz.getChildren().addAll(
                lblTitulo,
                grid,
                btnCadastrar,
                lblResumo,
                new Label("---"),
                lblSliderTitulo,
                slider,
                lblValorSlider
        );

        Scene scene = new Scene(raiz, 480, 420);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
