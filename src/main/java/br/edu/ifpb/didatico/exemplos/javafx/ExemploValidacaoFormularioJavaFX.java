package br.edu.ifpb.didatico.exemplos.javafx;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Exemplo Didático: Validação Reativa de Formulários e Safe Parsing em JavaFX:
 * 1. Safe Parsing prevenindo NumberFormatException.
 * 2. Pseudo-classes visuais (:invalido) acionadas na perda de foco (Pristine -> Dirty).
 * 3. BooleanBinding desabilitando botão de ação até o formulário estar matematicamente íntegro.
 * 4. Controle dinâmico de labels de erro (setVisible + setManaged).
 */
public class ExemploValidacaoFormularioJavaFX extends Application {

    public static final PseudoClass INVALIDO_PSEUDO = PseudoClass.getPseudoClass("invalido");

    /**
     * MÉTODOS DE SAFE PARSING:
     * Impedem que NumberFormatException escape e quebre o fluxo da aplicação.
     */
    public static class SafeParser {
        public static Optional<BigDecimal> parseMoeda(String texto) {
            if (texto == null || texto.isBlank()) return Optional.empty();
            try {
                String limpo = texto.trim().replace(".", "").replace(",", ".");
                BigDecimal valor = new BigDecimal(limpo);
                return (valor.compareTo(BigDecimal.ZERO) >= 0) ? Optional.of(valor) : Optional.empty();
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }

        public static Optional<Integer> parseInteiroPositivo(String texto) {
            if (texto == null || texto.isBlank()) return Optional.empty();
            try {
                int valor = Integer.parseInt(texto.trim());
                return (valor > 0) ? Optional.of(valor) : Optional.empty();
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }
    }

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-background-color: #f8fafc;");

        Label lblTitulo = new Label("Formulário de Cadastro com Validação Reativa");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);

        // --- CAMPO CÓDIGO ---
        TextField txtCodigo = new TextField();
        txtCodigo.setPromptText("Ex: PRD-001");
        Label lblErroCodigo = criarLabelErro("Código é obrigatório (mínimo 3 caracteres)");

        grid.add(new Label("Código do Item:"), 0, 0);
        grid.add(txtCodigo, 1, 0);
        grid.add(lblErroCodigo, 1, 1);

        // --- CAMPO QUANTIDADE ---
        TextField txtQuantidade = new TextField();
        txtQuantidade.setPromptText("Ex: 5");
        Label lblErroQuantidade = criarLabelErro("Quantidade deve ser um número inteiro > 0");

        grid.add(new Label("Quantidade:"), 0, 2);
        grid.add(txtQuantidade, 1, 2);
        grid.add(lblErroQuantidade, 1, 3);

        // --- CAMPO PREÇO ---
        TextField txtPreco = new TextField();
        txtPreco.setPromptText("Ex: 149,90");
        Label lblErroPreco = criarLabelErro("Preço deve ser um valor monetário válido (ex: 29,90)");

        grid.add(new Label("Preço Unitário (R$):"), 0, 4);
        grid.add(txtPreco, 1, 4);
        grid.add(lblErroPreco, 1, 5);

        // --- FEEDBACK VISUAL EM PERDA DE FOCO (PRISTINE -> DIRTY) ---
        txtCodigo.focusedProperty().addListener((obs, antigo, focado) -> {
            if (!focado) {
                boolean valido = txtCodigo.getText() != null && txtCodigo.getText().trim().length() >= 3;
                atualizarEstadoCampo(txtCodigo, lblErroCodigo, valido);
            }
        });

        txtQuantidade.focusedProperty().addListener((obs, antigo, focado) -> {
            if (!focado) {
                boolean valido = SafeParser.parseInteiroPositivo(txtQuantidade.getText()).isPresent();
                atualizarEstadoCampo(txtQuantidade, lblErroQuantidade, valido);
            }
        });

        txtPreco.focusedProperty().addListener((obs, antigo, focado) -> {
            if (!focado) {
                boolean valido = SafeParser.parseMoeda(txtPreco.getText()).isPresent();
                atualizarEstadoCampo(txtPreco, lblErroPreco, valido);
            }
        });

        // --- BOOLEAN BINDING COMPOSTO NO BOTÃO SALVAR ---
        BooleanBinding formularioValido = Bindings.createBooleanBinding(() -> {
            boolean codOk = txtCodigo.getText() != null && txtCodigo.getText().trim().length() >= 3;
            boolean qtdOk = SafeParser.parseInteiroPositivo(txtQuantidade.getText()).isPresent();
            boolean precoOk = SafeParser.parseMoeda(txtPreco.getText()).isPresent();
            return codOk && qtdOk && precoOk;
        }, txtCodigo.textProperty(), txtQuantidade.textProperty(), txtPreco.textProperty());

        Button btnSalvar = new Button("💾 Salvar Registro");
        btnSalvar.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");
        btnSalvar.disableProperty().bind(formularioValido.not());

        btnSalvar.setOnAction(e -> {
            String codigo = txtCodigo.getText().trim();
            int quantidade = SafeParser.parseInteiroPositivo(txtQuantidade.getText()).orElseThrow();
            BigDecimal preco = SafeParser.parseMoeda(txtPreco.getText()).orElseThrow();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registro Válido");
            alert.setHeaderText("Formulário Validado com Sucesso!");
            alert.setContentText(String.format("Código: %s%nQuantidade: %d%nPreço Unitário: R$ %.2f",
                    codigo, quantidade, preco));
            alert.showAndWait();
        });

        HBox botoes = new HBox(btnSalvar);
        botoes.setAlignment(Pos.CENTER_RIGHT);
        botoes.setPadding(new Insets(10, 0, 0, 0));

        root.getChildren().addAll(lblTitulo, grid, botoes);

        Scene scene = new Scene(root, 520, 360);
        // Estilo CSS para pseudo-classe de erro
        scene.getStylesheets().add("data:text/css," +
                ".text-field:invalido { -fx-border-color: #ef4444; -fx-border-width: 2px; }");

        primaryStage.setTitle("Demonstração: Validação Reativa de Formulários");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Label criarLabelErro(String mensagem) {
        Label lbl = new Label(mensagem);
        lbl.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");
        lbl.setVisible(false);
        lbl.setManaged(false); // Não ocupa espaço enquanto invisível
        return lbl;
    }

    private void atualizarEstadoCampo(TextField campo, Label labelErro, boolean valido) {
        campo.pseudoClassStateChanged(INVALIDO_PSEUDO, !valido);
        labelErro.setVisible(!valido);
        labelErro.setManaged(!valido);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
