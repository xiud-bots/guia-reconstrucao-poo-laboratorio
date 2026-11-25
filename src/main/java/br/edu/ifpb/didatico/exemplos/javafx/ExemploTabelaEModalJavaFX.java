package br.edu.ifpb.didatico.exemplos.javafx;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Exemplo Didático: Demonstração de interface gráfica JavaFX contendo:
 * 1. TableView com CellValueFactory adaptando POJOs puros
 * 2. CellFactory estilizando células dinamicamente (cores condicionais)
 * 3. Janela modal bloqueante (Modality.APPLICATION_MODAL)
 * 4. Caixa de diálogo de erro Alert(AlertType.ERROR)
 */
public class ExemploTabelaEModalJavaFX extends Application {

    // POJO puro para o exemplo
    public static class LivroExemplo {
        private final String isbn;
        private final String titulo;
        private boolean emprestado;

        public LivroExemplo(String isbn, String titulo, boolean emprestado) {
            this.isbn = isbn;
            this.titulo = titulo;
            this.emprestado = emprestado;
        }

        public String getIsbn() { return isbn; }
        public String getTitulo() { return titulo; }
        public boolean isEmprestado() { return emprestado; }
        public void setEmprestado(boolean emprestado) { this.emprestado = emprestado; }
    }

    private final ObservableList<LivroExemplo> dados = FXCollections.observableArrayList(
            new LivroExemplo("978-01", "Clean Code", false),
            new LivroExemplo("978-02", "Design Patterns", true),
            new LivroExemplo("978-03", "Effective Java", false)
    );

    @Override
    public void start(Stage primaryStage) {
        BorderPane raiz = new BorderPane();
        raiz.setPadding(new Insets(15));

        // 1. Tabela
        TableView<LivroExemplo> tabela = new TableView<>();
        
        TableColumn<LivroExemplo, String> colIsbn = new TableColumn<>("ISBN");
        colIsbn.setCellValueFactory(dado -> new SimpleStringProperty(dado.getValue().getIsbn()));
        colIsbn.setPrefWidth(120);

        TableColumn<LivroExemplo, String> colTitulo = new TableColumn<>("Título");
        colTitulo.setCellValueFactory(dado -> new SimpleStringProperty(dado.getValue().getTitulo()));
        colTitulo.setPrefWidth(250);

        TableColumn<LivroExemplo, String> colStatus = new TableColumn<>("Disponibilidade");
        colStatus.setCellValueFactory(dado -> 
                new SimpleStringProperty(dado.getValue().isEmprestado() ? "Emprestado" : "Disponível"));
        colStatus.setPrefWidth(150);

        // Customização visual da célula (CellFactory)
        colStatus.setCellFactory(col -> new TableCell<LivroExemplo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equalsIgnoreCase("Disponível")) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });

        tabela.getColumns().addAll(colIsbn, colTitulo, colStatus);
        tabela.setItems(dados);
        raiz.setCenter(tabela);

        // 2. Barra de Botões
        Button btnNovo = new Button("Cadastrar Novo Livro (Modal)");
        btnNovo.setOnAction(e -> abrirModalCadastro(primaryStage));

        Button btnSimularErro = new Button("Simular Exceção com Alert");
        btnSimularErro.setOnAction(e -> {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Simulação de Erro");
            alerta.setHeaderText("Regra de Negócio Violada");
            alerta.setContentText("Não foi possível prosseguir: o item selecionado já está em uso.");
            alerta.showAndWait();
        });

        HBox barraBotoes = new HBox(10, btnNovo, btnSimularErro);
        barraBotoes.setPadding(new Insets(10, 0, 0, 0));
        barraBotoes.setAlignment(Pos.CENTER_LEFT);
        raiz.setBottom(barraBotoes);

        Scene scene = new Scene(raiz, 600, 400);
        primaryStage.setTitle("Demonstração Didática: TableView e Modais JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void abrirModalCadastro(Stage janelaPai) {
        Stage modal = new Stage();
        modal.initOwner(janelaPai);
        modal.initModality(Modality.APPLICATION_MODAL); // Bloqueia a janela de fundo
        modal.setTitle("Cadastro Modal de Livro");

        VBox painel = new VBox(10);
        painel.setPadding(new Insets(20));

        TextField txtIsbn = new TextField();
        txtIsbn.setPromptText("ISBN do Livro");
        TextField txtTitulo = new TextField();
        txtTitulo.setPromptText("Título do Livro");

        Button btnSalvar = new Button("Salvar e Fechar");
        btnSalvar.setOnAction(ev -> {
            if (txtIsbn.getText().isBlank() || txtTitulo.getText().isBlank()) {
                Alert erro = new Alert(Alert.AlertType.WARNING);
                erro.setContentText("Preencha todos os campos.");
                erro.showAndWait();
                return;
            }
            dados.add(new LivroExemplo(txtIsbn.getText(), txtTitulo.getText(), false));
            modal.close();
        });

        painel.getChildren().addAll(
                new Label("Preencha os dados do livro:"),
                txtIsbn, txtTitulo, btnSalvar
        );

        modal.setScene(new Scene(painel, 320, 200));
        modal.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
