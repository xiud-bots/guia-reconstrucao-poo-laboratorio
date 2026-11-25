package br.edu.ifpb.didatico.exemplos.javafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Exemplo Didático: Demonstração de Concorrência e Thread Safety em JavaFX.
 * Demonstra como executar processamento em segundo plano utilizando javafx.concurrent.Task,
 * atualizando a barra de progresso e mensagens sem congelar a JavaFX Application Thread.
 */
public class ExemploConcorrenciaJavaFX extends Application {

    private ProgressBar barraProgresso;
    private Label labelStatus;
    private Button btnIniciar;
    private Button btnCancelar;
    private TextArea areaLog;

    private Task<List<String>> tarefaAtual;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Demonstração Didática: Concorrência e Tasks em JavaFX");

        VBox raiz = new VBox(15);
        raiz.setPadding(new Insets(20));
        raiz.setAlignment(Pos.CENTER);

        Label titulo = new Label("Processamento Assíncrono com Task e Thread Safety");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        labelStatus = new Label("Pronto para iniciar.");
        labelStatus.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");

        barraProgresso = new ProgressBar(0.0);
        barraProgresso.setPrefWidth(400);

        btnIniciar = new Button("Iniciar Carga em Segundo Plano");
        btnIniciar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");

        btnCancelar = new Button("Cancelar Processo");
        btnCancelar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        btnCancelar.setDisable(true);

        HBox barraBotoes = new HBox(12, btnIniciar, btnCancelar);
        barraBotoes.setAlignment(Pos.CENTER);

        areaLog = new TextArea();
        areaLog.setEditable(false);
        areaLog.setPrefRowCount(8);
        areaLog.setPromptText("Logs da execução assíncrona aparecerão aqui...");

        btnIniciar.setOnAction(e -> iniciarProcessamentoAssincrono());
        btnCancelar.setOnAction(e -> {
            if (tarefaAtual != null && tarefaAtual.isRunning()) {
                tarefaAtual.cancel();
            }
        });

        raiz.getChildren().addAll(titulo, labelStatus, barraProgresso, barraBotoes, areaLog);

        Scene scene = new Scene(raiz, 550, 420);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void iniciarProcessamentoAssincrono() {
        areaLog.clear();
        areaLog.appendText("[UI Thread] Iniciando tarefa em segundo plano...\n");

        // 1. Cria a Task que executa o processamento pesado fora da UI Thread
        tarefaAtual = new Task<>() {
            @Override
            protected List<String> call() throws Exception {
                List<String> itensProcessados = new ArrayList<>();
                int totalEtapas = 10;

                for (int i = 1; i <= totalEtapas; i++) {
                    // Verifica se o operador solicitou cancelamento
                    if (isCancelled()) {
                        updateMessage("Processamento cancelado pelo operador.");
                        break;
                    }

                    // Simula I/O de disco ou processamento pesado (500ms por etapa)
                    Thread.sleep(500);

                    String item = "Registro de Dados #" + i + " processado com sucesso";
                    itensProcessados.add(item);

                    // Atualiza progresso e mensagem de forma segura para a interface gráfica
                    updateProgress(i, totalEtapas);
                    updateMessage(String.format("Processando etapa %d de %d...", i, totalEtapas));

                    // Demonstração opcional: Platform.runLater para enviar logs pontuais à interface
                    final String logMensagem = String.format("[Worker Thread] Concluída etapa %d/%d%n", i, totalEtapas);
                    Platform.runLater(() -> areaLog.appendText(logMensagem));
                }

                return itensProcessados;
            }
        };

        // 2. Data Binding reativo: a barra de progresso e o label seguem os valores da Task
        barraProgresso.progressProperty().bind(tarefaAtual.progressProperty());
        labelStatus.textProperty().bind(tarefaAtual.messageProperty());

        // 3. Controle de estados dos botões
        btnIniciar.setDisable(true);
        btnCancelar.setDisable(false);

        // 4. Callbacks de Ciclo de Vida (executados na JavaFX Application Thread!)
        tarefaAtual.setOnSucceeded(ev -> {
            liberarBotoes();
            List<String> resultado = tarefaAtual.getValue();
            areaLog.appendText(String.format("%n[Sucesso] Concluído! %d registros processados com êxito.%n", resultado.size()));
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Processamento Concluído");
            alert.setHeaderText("Operação Assíncrona Finalizada");
            alert.setContentText("Todos os dados foram processados sem congelar a interface!");
            alert.showAndWait();
        });

        tarefaAtual.setOnCancelled(ev -> {
            liberarBotoes();
            areaLog.appendText("\n[Cancelado] A tarefa foi interrompida antes da conclusão.\n");
        });

        tarefaAtual.setOnFailed(ev -> {
            liberarBotoes();
            Throwable erro = tarefaAtual.getException();
            areaLog.appendText("\n[Erro] Falha no processamento: " + erro.getMessage() + "\n");
        });

        // 5. Dispara a tarefa em uma nova Thread de background (marcada como Daemon)
        Thread threadTrabalhadora = new Thread(tarefaAtual);
        threadTrabalhadora.setDaemon(true); // Garante que a thread encerra se a janela for fechada
        threadTrabalhadora.start();
    }

    private void liberarBotoes() {
        barraProgresso.progressProperty().unbind();
        labelStatus.textProperty().unbind();
        btnIniciar.setDisable(false);
        btnCancelar.setDisable(true);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
