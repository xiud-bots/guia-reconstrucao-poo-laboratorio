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
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstração Didática do Guia 24:
 * Resiliência Operacional, Tratamento Global de Falhas (UncaughtExceptionHandler),
 * Diálogo com Stack Trace Expansível e Persistência de Logs de Diagnóstico.
 */
public class ExemploTratamentoGlobalErrosJavaFX extends Application {

    // 1. Tratador Centralizado de Exceções Não Capturadas
    public static final class TratadorFalhasGlobal implements Thread.UncaughtExceptionHandler {

        private final Path arquivoLog;
        private static final DateTimeFormatter FORMATADOR_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        private final AtomicInteger contadorErros = new AtomicInteger(0);

        public TratadorFalhasGlobal(Path arquivoLog) {
            this.arquivoLog = (arquivoLog != null) ? arquivoLog : Path.of("app-erros.log");
        }

        @Override
        public void uncaughtException(Thread thread, Throwable throwable) {
            contadorErros.incrementAndGet();

            // 1. Grava no log de diagnóstico em disco imediatamente
            registrarLogEmDisco(arquivoLog, thread, throwable);

            // 2. Garante que o diálogo seja exibido na UI Thread do JavaFX
            if (Platform.isFxApplicationThread()) {
                exibirDialogoFalha(thread, throwable);
            } else {
                Platform.runLater(() -> exibirDialogoFalha(thread, throwable));
            }
        }

        public static void registrarLogEmDisco(Path arquivo, Thread thread, Throwable throwable) {
            try {
                String entrada = formatarEntradaLog(thread, throwable);
                if (arquivo.getParent() != null && !Files.exists(arquivo.getParent())) {
                    Files.createDirectories(arquivo.getParent());
                }
                Files.writeString(arquivo, entrada, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (Exception ex) {
                System.err.println("CRÍTICO: Falha ao gravar log em disco: " + ex.getMessage());
            }
        }

        public static String formatarEntradaLog(Thread thread, Throwable throwable) {
            StringWriter sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));

            String nomeThread = (thread != null) ? thread.getName() : "Thread-Desconhecida";
            String classeErro = (throwable != null) ? throwable.getClass().getName() : "Throwable-Desconhecido";
            String mensagem = (throwable != null && throwable.getMessage() != null) ? throwable.getMessage() : "Sem mensagem descritiva";

            return String.format("[%s] [THREAD: %s] [EXCEÇÃO: %s]\nMensagem: %s\nRastreamento de Pilha:\n%s\n-----------------------------------------------------------------\n",
                    LocalDateTime.now().format(FORMATADOR_TIMESTAMP),
                    nomeThread,
                    classeErro,
                    mensagem,
                    sw.toString());
        }

        public static void exibirDialogoFalha(Thread thread, Throwable throwable) {
            try {
                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setTitle("Inconsistência Inesperada no Sistema");
                alerta.setHeaderText("Ocorreu uma falha imprevista durante a operação.");
                alerta.setContentText("A operação foi interrompida para preservar a integridade dos dados. Detalhes técnicos foram registrados no arquivo de auditoria.");

                // Montagem do Painel Expansível de Detalhes Técnicos
                StringWriter sw = new StringWriter();
                throwable.printStackTrace(new PrintWriter(sw));
                String stackTrace = sw.toString();

                Label lblDetalhes = new Label("Detalhes técnicos da falha (Stack Trace):");
                lblDetalhes.setStyle("-fx-font-weight: bold;");

                TextArea txtDetalhes = new TextArea(stackTrace);
                txtDetalhes.setEditable(false);
                txtDetalhes.setWrapText(false);
                txtDetalhes.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");
                txtDetalhes.setMaxWidth(Double.MAX_VALUE);
                txtDetalhes.setMaxHeight(Double.MAX_VALUE);

                GridPane painelExpansivel = new GridPane();
                painelExpansivel.setMaxWidth(Double.MAX_VALUE);
                painelExpansivel.setVgap(6);
                painelExpansivel.add(lblDetalhes, 0, 0);
                painelExpansivel.add(txtDetalhes, 0, 1);
                GridPane.setVgrow(txtDetalhes, Priority.ALWAYS);
                GridPane.setHgrow(txtDetalhes, Priority.ALWAYS);

                alerta.getDialogPane().setExpandableContent(painelExpansivel);
                alerta.showAndWait();
            } catch (Exception ex) {
                System.err.println("Falha ao exibir diálogo de erro: " + ex.getMessage());
            }
        }

        public int getContadorErros() {
            return contadorErros.get();
        }
    }

    // 2. Componentes da Aplicação Didática
    private final Path caminhoLogDemonstracao = Path.of("target", "demonstracao-erros.log");
    private final TratadorFalhasGlobal tratadorGlobal = new TratadorFalhasGlobal(caminhoLogDemonstracao);
    private final Label lblStatusLog = new Label("Nenhum erro interceptado ainda.");
    private final TextArea txtVisualizadorLog = new TextArea();

    @Override
    public void init() {
        // Registra o tratador global no nível da JVM
        Thread.setDefaultUncaughtExceptionHandler(tratadorGlobal);
    }

    @Override
    public void start(Stage palcoPrincipal) {
        palcoPrincipal.setTitle("Demonstração Didática — Resiliência Global e Tratador de Falhas");

        // Painel Superior com Botões de Simulação
        VBox painelControles = criarPainelControles();

        // Painel Central com Visualizador do Arquivo de Log em Tempo Real
        VBox painelVisualizador = criarPainelVisualizador();

        BorderPane layout = new BorderPane();
        layout.setTop(painelControles);
        layout.setCenter(painelVisualizador);

        Scene cena = new Scene(layout, 860, 520);
        palcoPrincipal.setScene(cena);
        palcoPrincipal.show();
    }

    private VBox criarPainelControles() {
        VBox painel = new VBox(10);
        painel.setPadding(new Insets(14));
        painel.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");

        Label lblInstrucoes = new Label("Simule cenários de falha imprevista para observar a interceptação defensiva:");
        lblInstrucoes.setStyle("-fx-font-weight: bold; -fx-text-fill: #343a40;");

        HBox linhaBotoes = new HBox(10);
        linhaBotoes.setAlignment(Pos.CENTER_LEFT);

        // 1. Simula NullPointerException na UI Thread
        Button btnErroUi = new Button("💥 Erro na UI Thread (NPE)");
        btnErroUi.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold;");
        btnErroUi.setOnAction(e -> {
            String objetoNulo = null;
            // Força lançamento na JavaFX Application Thread
            objetoNulo.length();
        });

        // 2. Simula Falha em Background Task
        Button btnErroTask = new Button("⚡ Falha em Background Thread (Task)");
        btnErroTask.setStyle("-fx-background-color: #fd7e14; -fx-text-fill: white; -fx-font-weight: bold;");
        btnErroTask.setOnAction(e -> {
            Task<Void> taskAssincrona = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    Thread.sleep(300);
                    throw new IllegalStateException("Falha simulada no pool assíncrono: Timeout na comunicação com o banco!");
                }
            };
            // Intercepta a falha e repassa ao tratador global
            taskAssincrona.setOnFailed(ev -> tratadorGlobal.uncaughtException(
                    Thread.currentThread(), taskAssincrona.getException()));
            new Thread(taskAssincrona, "Worker-Simulacao-Thread").start();
        });

        // 3. Simula Falha Encadeada (Nested Exception)
        Button btnErroEncadeado = new Button("🔗 Exceção com Causa Raiz Encadeada");
        btnErroEncadeado.setStyle("-fx-background-color: #6f42c1; -fx-text-fill: white; -fx-font-weight: bold;");
        btnErroEncadeado.setOnAction(e -> {
            IOException causaOriginal = new IOException("Setor de disco físico danificado ao gravar estado.");
            throw new RuntimeException("Falha catastrófica ao tentar sincronizar o acervo patrimonial.", causaOriginal);
        });

        Button btnRecarregarLog = new Button("↻ Atualizar Visualizador de Log");
        btnRecarregarLog.setOnAction(e -> atualizarVisualizadorLog());

        linhaBotoes.getChildren().addAll(btnErroUi, btnErroTask, btnErroEncadeado, btnRecarregarLog);

        lblStatusLog.setStyle("-fx-font-size: 11px; -fx-text-fill: #495057;");
        painel.getChildren().addAll(lblInstrucoes, linhaBotoes, lblStatusLog);
        return painel;
    }

    private VBox criarPainelVisualizador() {
        VBox painel = new VBox(6);
        painel.setPadding(new Insets(12));

        Label lblTitulo = new Label("Conteúdo Atual do Arquivo de Log de Diagnóstico (target/demonstracao-erros.log):");
        lblTitulo.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");

        txtVisualizadorLog.setEditable(false);
        txtVisualizadorLog.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");
        VBox.setVgrow(txtVisualizadorLog, Priority.ALWAYS);

        painel.getChildren().addAll(lblTitulo, txtVisualizadorLog);
        return painel;
    }

    private void atualizarVisualizadorLog() {
        lblStatusLog.setText(String.format("Total de incidentes interceptados pelo tratador: %d", tratadorGlobal.getContadorErros()));
        if (Files.exists(caminhoLogDemonstracao)) {
            try {
                String log = Files.readString(caminhoLogDemonstracao, StandardCharsets.UTF_8);
                txtVisualizadorLog.setText(log);
            } catch (IOException ex) {
                txtVisualizadorLog.setText("Falha ao ler log: " + ex.getMessage());
            }
        } else {
            txtVisualizadorLog.setText("Nenhum log gravado até o momento.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
