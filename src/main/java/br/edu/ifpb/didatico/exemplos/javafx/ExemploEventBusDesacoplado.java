package br.edu.ifpb.didatico.exemplos.javafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Demonstração Didática do Guia 19:
 * Comunicação 100% Desacoplada entre Painéis e Controladores via Barramento de Eventos
 * (AppEventBus) com Eventos Tipados em Java 21 Records e Despacho Seguro de Thread.
 */
public class ExemploEventBusDesacoplado extends Application {

    // =========================================================================
    // 1. EVENTOS IMUTÁVEIS TIPADOS (JAVA 21 RECORDS)
    // =========================================================================

    public record ItemCadastradoEvent(
            String codigo,
            String descricao,
            LocalDateTime momento
    ) {
        public ItemCadastradoEvent(String codigo, String descricao) {
            this(codigo, descricao, LocalDateTime.now());
        }
    }

    public record ItemExcluidoEvent(
            String codigo,
            LocalDateTime momento
    ) {
        public ItemExcluidoEvent(String codigo) {
            this(codigo, LocalDateTime.now());
        }
    }

    public record NotificacaoAlertaEvent(
            String mensagem,
            String severidade
    ) {}

    // =========================================================================
    // 2. BARRAMENTO DE EVENTOS CENTRAL THREAD-SAFE (APP EVENT BUS)
    // =========================================================================

    public static final class AppEventBus {
        private static final AppEventBus INSTANCIA = new AppEventBus();
        private final Map<Class<?>, List<Consumer<Object>>> ouvintes = new ConcurrentHashMap<>();

        private AppEventBus() {}

        public static AppEventBus getInstance() {
            return INSTANCIA;
        }

        /**
         * Registra um ouvinte para um tipo específico de evento.
         * Retorna um Runnable de cancelamento para prevenir Memory Leaks.
         */
        @SuppressWarnings("unchecked")
        public <T> Runnable inscrever(Class<T> tipoEvento, Consumer<T> consumidor) {
            Objects.requireNonNull(tipoEvento, "Tipo de evento não pode ser nulo");
            Objects.requireNonNull(consumidor, "Consumidor não pode ser nulo");

            Consumer<Object> wrapper = obj -> consumidor.accept((T) obj);
            ouvintes.computeIfAbsent(tipoEvento, k -> new CopyOnWriteArrayList<>()).add(wrapper);

            return () -> {
                List<Consumer<Object>> lista = ouvintes.get(tipoEvento);
                if (lista != null) {
                    lista.remove(wrapper);
                }
            };
        }

        /**
         * Publica um evento imutável. Despacha com segurança para a thread JavaFX.
         */
        public void publicar(Object evento) {
            if (evento == null) return;

            Class<?> tipo = evento.getClass();
            List<Consumer<Object>> lista = ouvintes.get(tipo);

            if (lista != null && !lista.isEmpty()) {
                for (Consumer<Object> consumidor : lista) {
                    despachar(() -> consumidor.accept(evento));
                }
            }
        }

        public void limparTodos() {
            ouvintes.clear();
        }

        private void despachar(Runnable acao) {
            try {
                if (Platform.isFxApplicationThread()) {
                    acao.run();
                } else {
                    Platform.runLater(acao);
                }
            } catch (IllegalStateException e) {
                // Em ambientes de teste unitário sem toolkit gráfico do JavaFX inicializado,
                // executa diretamente na thread chamadora.
                acao.run();
            }
        }
    }

    // =========================================================================
    // 3. INTERFACE GRÁFICA: DOIS PAINÉIS TOTALMENTE DESACOPLADOS
    // =========================================================================

    private static final DateTimeFormatter HORA_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public void start(Stage palco) {
        palco.setTitle("Demonstração Didática — AppEventBus & Desacoplamento");

        // Painel Esquerdo: Formulário Publicador (NÃO conhece o painel direito!)
        VBox painelPublicador = criarPainelFormulario();

        // Painel Direito: Dashboard Ouvinte (NÃO conhece o painel esquerdo!)
        VBox painelOuvinte = criarPainelDashboard();

        HBox raiz = new HBox(20, painelPublicador, painelOuvinte);
        raiz.setPadding(new Insets(20));
        HBox.setHgrow(painelPublicador, Priority.ALWAYS);
        HBox.setHgrow(painelOuvinte, Priority.ALWAYS);
        raiz.setStyle("-fx-background-color: #f4f6f9;");

        palco.setScene(new Scene(raiz, 860, 480));
        palco.show();
    }

    /**
     * Painel 1 (Publicador): Apenas coleta dados da tela e emite eventos no AppEventBus.
     */
    private VBox criarPainelFormulario() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(16));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #dee2e6; -fx-border-radius: 8;");

        Label titulo = new Label("Painel de Ações (Publicador)");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a73e8;");

        TextField txtCodigo = new TextField();
        txtCodigo.setPromptText("Ex: LAB-104");

        TextField txtDescricao = new TextField();
        txtDescricao.setPromptText("Ex: Kit Arduino Sensor Pack");

        Button btnPublicarCadastro = new Button("Cadastrar Item");
        btnPublicarCadastro.setStyle("-fx-background-color: #198754; -fx-text-fill: white; -fx-font-weight: bold;");
        btnPublicarCadastro.setMaxWidth(Double.MAX_VALUE);

        btnPublicarCadastro.setOnAction(e -> {
            String cod = txtCodigo.getText().trim();
            String desc = txtDescricao.getText().trim();

            if (cod.isEmpty() || desc.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Preencha o código e a descrição antes de emitir.");
                alert.show();
                return;
            }

            // PUBLICA NO EVENT BUS — Sem conhecer quem escutará!
            AppEventBus.getInstance().publicar(new ItemCadastradoEvent(cod, desc));

            txtCodigo.clear();
            txtDescricao.clear();
        });

        TextField txtExcluirCodigo = new TextField();
        txtExcluirCodigo.setPromptText("Código para remover...");

        Button btnPublicarExclusao = new Button("Remover Item");
        btnPublicarExclusao.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold;");
        btnPublicarExclusao.setMaxWidth(Double.MAX_VALUE);

        btnPublicarExclusao.setOnAction(e -> {
            String cod = txtExcluirCodigo.getText().trim();
            if (!cod.isEmpty()) {
                AppEventBus.getInstance().publicar(new ItemExcluidoEvent(cod));
                txtExcluirCodigo.clear();
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.add(new Label("Código:"), 0, 0);
        grid.add(txtCodigo, 1, 0);
        grid.add(new Label("Descrição:"), 0, 1);
        grid.add(txtDescricao, 1, 1);

        container.getChildren().addAll(
                titulo,
                new Label("Preencha e dispare eventos no barramento:"),
                grid,
                btnPublicarCadastro,
                new Label("Operação de Exclusão:"),
                txtExcluirCodigo,
                btnPublicarExclusao
        );

        return container;
    }

    /**
     * Painel 2 (Ouvinte/Consumidor): Escuta eventos do AppEventBus e atualiza contadores e log.
     */
    private VBox criarPainelDashboard() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(16));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #dee2e6; -fx-border-radius: 8;");

        Label titulo = new Label("Dashboard Reativo (Consumidor)");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #202124;");

        Label lblTotal = new Label("Itens em Acervo: 0");
        lblTotal.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0d6efd;");

        ListView<String> listaAtividades = new ListView<>();
        VBox.setVgrow(listaAtividades, Priority.ALWAYS);

        final int[] contador = {0};

        // INSCRIÇÃO 1: Escuta ItemCadastradoEvent
        AppEventBus.getInstance().inscrever(ItemCadastradoEvent.class, evento -> {
            contador[0]++;
            lblTotal.setText("Itens em Acervo: " + contador[0]);
            String registro = String.format("[%s] INCLUÍDO: %s (%s)",
                    evento.momento().format(HORA_FORMAT), evento.codigo(), evento.descricao());
            listaAtividades.getItems().add(0, registro);
        });

        // INSCRIÇÃO 2: Escuta ItemExcluidoEvent
        AppEventBus.getInstance().inscrever(ItemExcluidoEvent.class, evento -> {
            if (contador[0] > 0) {
                contador[0]--;
            }
            lblTotal.setText("Itens em Acervo: " + contador[0]);
            String registro = String.format("[%s] EXCLUÍDO: %s",
                    evento.momento().format(HORA_FORMAT), evento.codigo());
            listaAtividades.getItems().add(0, registro);
        });

        container.getChildren().addAll(
                titulo,
                lblTotal,
                new Label("Feed de Eventos em Tempo Real:"),
                listaAtividades
        );

        return container;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
