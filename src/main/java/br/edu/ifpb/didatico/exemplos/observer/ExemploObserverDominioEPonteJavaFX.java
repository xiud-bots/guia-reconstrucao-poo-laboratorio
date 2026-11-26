package br.edu.ifpb.didatico.exemplos.observer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Exemplo didático do Padrão GoF Observer no contexto de Clean Architecture e JavaFX.
 * Demonstra a separação estrita de fronteiras: o núcleo de domínio NÃO depende
 * de nenhuma classe de interface gráfica (zero imports de javafx.* no domínio).
 * Uma ponte reativa (Bridge/Adapter) recebe os eventos puros do domínio e faz o
 * despacho seguro via Platform.runLater() para atualizar tabelas e propriedades do JavaFX.
 */
public class ExemploObserverDominioEPonteJavaFX extends Application {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    // =========================================================================
    // 1. NÚCLEO DE DOMÍNIO PURO (ZERO DEPENDÊNCIAS DE JAVAFX / CLEAN ARCHITECTURE)
    // =========================================================================

    public enum TipoEventoDominio {
        RECURSO_CADASTRADO,
        ALOCACAO_CONCLUIDA,
        DEVOLUCAO_CONCLUIDA,
        ALERTA_ESTOQUE_CRITICO
    }

    public record EventoDominio(
            String idEvento,
            TipoEventoDominio tipo,
            String codigoRecurso,
            String descricao,
            LocalDateTime timestamp,
            String autorTransacao
    ) {
        public EventoDominio {
            Objects.requireNonNull(idEvento, "idEvento não pode ser nulo.");
            Objects.requireNonNull(tipo, "tipo não pode ser nulo.");
            Objects.requireNonNull(codigoRecurso, "codigoRecurso não pode ser nulo.");
            Objects.requireNonNull(descricao, "descricao não pode ser nula.");
            Objects.requireNonNull(timestamp, "timestamp não pode ser nulo.");
            Objects.requireNonNull(autorTransacao, "autorTransacao não pode ser nulo.");
        }
    }

    /**
     * Contrato do Observador de Domínio Puro (GoF Observer).
     */
    @FunctionalInterface
    public interface IObservadorDominio {
        void onEventoDominio(EventoDominio evento);
    }

    /**
     * Contrato do Sujeito Observável de Domínio Puro (GoF Subject).
     */
    public interface ISujeitoDominio {
        void registrarObservador(IObservadorDominio observador);
        void removerObservador(IObservadorDominio observador);
        void notificarObservadores(EventoDominio evento);
    }

    public static class RecursoEsgotadoException extends Exception {
        public RecursoEsgotadoException(String mensagem) {
            super(mensagem);
        }
    }

    /**
     * Entidade e Gerenciador de Domínio Puro.
     * Não sabe e não deve saber que existe JavaFX, Web, Console ou Swing.
     */
    public static class CatalogoRecursosDominio implements ISujeitoDominio {

        public static class ItemEstoque {
            private final String codigo;
            private final String nome;
            private final int quantidadeTotal;
            private int quantidadeDisponivel;

            public ItemEstoque(String codigo, String nome, int quantidadeTotal) {
                this.codigo = codigo;
                this.nome = nome;
                this.quantidadeTotal = quantidadeTotal;
                this.quantidadeDisponivel = quantidadeTotal;
            }

            public String getCodigo() { return codigo; }
            public String getNome() { return nome; }
            public int getQuantidadeTotal() { return quantidadeTotal; }
            public int getQuantidadeDisponivel() { return quantidadeDisponivel; }
        }

        private final Map<String, ItemEstoque> inventario = new HashMap<>();
        // CopyOnWriteArrayList garante thread-safety ao iterar durante notificações concorrentes
        private final List<IObservadorDominio> observadores = new CopyOnWriteArrayList<>();

        public CatalogoRecursosDominio() {
            cadastrarItemInterno("OSC-100", "Osciloscópio Digital 100MHz", 3);
            cadastrarItemInterno("MIC-200", "Microscópio Estereoscópico", 2);
            cadastrarItemInterno("GEN-300", "Gerador de Sinais Arbitrários", 1);
        }

        private void cadastrarItemInterno(String codigo, String nome, int qtd) {
            inventario.put(codigo, new ItemEstoque(codigo, nome, qtd));
        }

        @Override
        public void registrarObservador(IObservadorDominio observador) {
            if (observador != null && !observadores.contains(observador)) {
                observadores.add(observador);
            }
        }

        @Override
        public void removerObservador(IObservadorDominio observador) {
            observadores.remove(observador);
        }

        @Override
        public void notificarObservadores(EventoDominio evento) {
            for (IObservadorDominio obs : observadores) {
                try {
                    obs.onEventoDominio(evento);
                } catch (Exception e) {
                    // Protege contra observadores externos com falha
                    System.err.println("Falha ao notificar observador: " + e.getMessage());
                }
            }
        }

        public synchronized void alocarRecurso(String codigo, String solicitante) throws RecursoEsgotadoException {
            ItemEstoque item = inventario.get(codigo);
            if (item == null) {
                throw new IllegalArgumentException("Recurso não encontrado no catálogo: " + codigo);
            }
            if (item.quantidadeDisponivel <= 0) {
                throw new RecursoEsgotadoException("Não há unidades disponíveis para o recurso: " + item.getNome());
            }

            item.quantidadeDisponivel--;

            // Dispara evento de alocação concluída
            EventoDominio eventoAlocacao = new EventoDominio(
                    UUID.randomUUID().toString().substring(0, 8),
                    TipoEventoDominio.ALOCACAO_CONCLUIDA,
                    item.getCodigo(),
                    String.format("Alocado para %s (Disponíveis agora: %d/%d)", solicitante, item.quantidadeDisponivel, item.quantidadeTotal),
                    LocalDateTime.now(),
                    solicitante
            );
            notificarObservadores(eventoAlocacao);

            // Regra de Negócio Pura: Estoque crítico gera segundo evento específico
            if (item.quantidadeDisponivel <= 1) {
                EventoDominio eventoCritico = new EventoDominio(
                        UUID.randomUUID().toString().substring(0, 8),
                        TipoEventoDominio.ALERTA_ESTOQUE_CRITICO,
                        item.getCodigo(),
                        String.format("ATENÇÃO: Recurso '%s' com apenas %d unidade(s) restante(s)!", item.getNome(), item.quantidadeDisponivel),
                        LocalDateTime.now(),
                        "SISTEMA_REGRAS_DOMINIO"
                );
                notificarObservadores(eventoCritico);
            }
        }

        public synchronized void devolverRecurso(String codigo, String solicitante) {
            ItemEstoque item = inventario.get(codigo);
            if (item == null) return;

            if (item.quantidadeDisponivel < item.quantidadeTotal) {
                item.quantidadeDisponivel++;
            }

            EventoDominio eventoDevolucao = new EventoDominio(
                    UUID.randomUUID().toString().substring(0, 8),
                    TipoEventoDominio.DEVOLUCAO_CONCLUIDA,
                    item.getCodigo(),
                    String.format("Devolvido por %s (Disponíveis agora: %d/%d)", solicitante, item.quantidadeDisponivel, item.quantidadeTotal),
                    LocalDateTime.now(),
                    solicitante
            );
            notificarObservadores(eventoDevolucao);
        }

        public synchronized void cadastrarNovoItem(String codigo, String nome, int quantidade, String operador) {
            cadastrarItemInterno(codigo, nome, quantidade);
            EventoDominio evento = new EventoDominio(
                    UUID.randomUUID().toString().substring(0, 8),
                    TipoEventoDominio.RECURSO_CADASTRADO,
                    codigo,
                    String.format("Novo equipamento cadastrado com lote de %d unidades: %s", quantidade, nome),
                    LocalDateTime.now(),
                    operador
            );
            notificarObservadores(evento);
        }

        public List<ItemEstoque> listarItens() {
            return List.copyOf(inventario.values());
        }

        public List<IObservadorDominio> getObservadores() {
            return Collections.unmodifiableList(observadores);
        }
    }

    // =========================================================================
    // 2. CAMADA DE APRESENTAÇÃO: A PONTE REATIVA JAVAFX (Observer Adapter)
    // =========================================================================

    /**
     * Esta classe vive na camada de apresentação (GUI).
     * Ela implementa o contrato puro de domínio (IObservadorDominio),
     * porém traduz os eventos para ObservableList e Properties do JavaFX,
     * garantindo despacho seguro na UI Thread via Platform.runLater().
     */
    public static class PonteReativaJavaFX implements IObservadorDominio {

        private final ObservableList<EventoDominio> eventosObservable = FXCollections.observableArrayList();
        private final SimpleIntegerProperty totalTransacoesProperty = new SimpleIntegerProperty(0);
        private final SimpleIntegerProperty alertasCriticosProperty = new SimpleIntegerProperty(0);

        @Override
        public void onEventoDominio(EventoDominio evento) {
            Objects.requireNonNull(evento);

            Runnable acaoUi = () -> {
                eventosObservable.add(0, evento); // Adiciona no topo da lista (mais recente primeiro)
                totalTransacoesProperty.set(totalTransacoesProperty.get() + 1);

                if (evento.tipo() == TipoEventoDominio.ALERTA_ESTOQUE_CRITICO) {
                    alertasCriticosProperty.set(alertasCriticosProperty.get() + 1);
                }
            };

            // Se a thread disparadora já for a UI Thread, executa diretamente;
            // Se for uma thread de background/serviço, despacha via Platform.runLater().
            // Em testes unitários sem toolkit inicializado, executa diretamente via catch.
            try {
                if (Platform.isFxApplicationThread()) {
                    acaoUi.run();
                } else {
                    Platform.runLater(acaoUi);
                }
            } catch (IllegalStateException e) {
                acaoUi.run();
            }
        }

        public ObservableList<EventoDominio> getEventosObservable() {
            return eventosObservable;
        }

        public SimpleIntegerProperty totalTransacoesProperty() {
            return totalTransacoesProperty;
        }

        public SimpleIntegerProperty alertasCriticosProperty() {
            return alertasCriticosProperty;
        }
    }

    // =========================================================================
    // 3. INTERFACE GRÁFICA JAVAFX
    // =========================================================================

    private final CatalogoRecursosDominio dominio = new CatalogoRecursosDominio();
    private final PonteReativaJavaFX ponteReativa = new PonteReativaJavaFX();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Demonstração GoF Observer — Clean Architecture & Ponte Reativa JavaFX");

        // Conecta a ponte reativa ao domínio puro
        dominio.registrarObservador(ponteReativa);

        BorderPane raiz = new BorderPane();
        raiz.setPadding(new Insets(15));

        // Topo: Cabeçalho explicativo e Cards de Métricas Reativas
        VBox boxTopo = new VBox(8);
        Label lblTitulo = new Label("Padrão GoF Observer Puro + Ponte Reativa JavaFX");
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a365d;");
        Label lblDescricao = new Label("O núcleo de domínio emite EventoDominio sem conhecer JavaFX.\n" +
                "A PonteReativaJavaFX implementa IObservadorDominio e despacha via Platform.runLater().");
        lblDescricao.setStyle("-fx-text-fill: #4a5568;");

        // Cards Reativos (Data Binding com Properties da Ponte)
        HBox boxCards = new HBox(15);
        boxCards.setPadding(new Insets(8, 0, 8, 0));

        VBox cardTotal = criarCardMetrica("Total Transações", ponteReativa.totalTransacoesProperty(), "#2b6cb0");
        VBox cardCriticos = criarCardMetrica("Alertas de Estoque Crítico", ponteReativa.alertasCriticosProperty(), "#c53030");

        boxCards.getChildren().addAll(cardTotal, cardCriticos);
        boxTopo.getChildren().addAll(lblTitulo, lblDescricao, boxCards);
        raiz.setTop(boxTopo);

        // Centro: Tabela de Eventos Reativos
        TableView<EventoDominio> tabela = new TableView<>();
        tabela.setItems(ponteReativa.getEventosObservable());
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<EventoDominio, String> colHora = new TableColumn<>("Hora");
        colHora.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().timestamp().format(FORMATO_HORA)));
        colHora.setPrefWidth(95);

        TableColumn<EventoDominio, TipoEventoDominio> colTipo = new TableColumn<>("Tipo de Evento");
        colTipo.setCellValueFactory(e -> new javafx.beans.property.SimpleObjectProperty<>(e.getValue().tipo()));
        colTipo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(TipoEventoDominio item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.name());
                    switch (item) {
                        case ALOCACAO_CONCLUIDA -> setStyle("-fx-background-color: #ebf8ff; -fx-text-fill: #2b6cb0; -fx-font-weight: bold;");
                        case DEVOLUCAO_CONCLUIDA -> setStyle("-fx-background-color: #f0fff4; -fx-text-fill: #22543d; -fx-font-weight: bold;");
                        case ALERTA_ESTOQUE_CRITICO -> setStyle("-fx-background-color: #fff5f5; -fx-text-fill: #c53030; -fx-font-weight: bold;");
                        case RECURSO_CADASTRADO -> setStyle("-fx-background-color: #faf5ff; -fx-text-fill: #6b46c1; -fx-font-weight: bold;");
                    }
                }
            }
        });
        colTipo.setPrefWidth(180);

        TableColumn<EventoDominio, String> colRecurso = new TableColumn<>("Código");
        colRecurso.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().codigoRecurso()));
        colRecurso.setPrefWidth(85);

        TableColumn<EventoDominio, String> colDescricao = new TableColumn<>("Descrição do Evento");
        colDescricao.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().descricao()));
        colDescricao.setPrefWidth(350);

        TableColumn<EventoDominio, String> colAutor = new TableColumn<>("Autor / Operador");
        colAutor.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().autorTransacao()));
        colAutor.setPrefWidth(150);

        tabela.getColumns().addAll(List.of(colHora, colTipo, colRecurso, colDescricao, colAutor));
        raiz.setCenter(tabela);

        // Rodapé: Painel de Simulação de Ações do Domínio
        HBox boxBotoes = new HBox(10);
        boxBotoes.setPadding(new Insets(12, 0, 0, 0));
        boxBotoes.setAlignment(Pos.CENTER_LEFT);

        Button btnAlocarUI = new Button("Alocar Osciloscópio (UI Thread)");
        btnAlocarUI.setStyle("-fx-background-color: #2b6cb0; -fx-text-fill: white; -fx-font-weight: bold;");
        btnAlocarUI.setOnAction(e -> {
            try {
                dominio.alocarRecurso("OSC-100", "Gabriel (UI)");
            } catch (RecursoEsgotadoException ex) {
                new Alert(Alert.AlertType.WARNING, ex.getMessage()).showAndWait();
            }
        });

        Button btnDevolverUI = new Button("Devolver Osciloscópio (UI Thread)");
        btnDevolverUI.setStyle("-fx-background-color: #38a169; -fx-text-fill: white;");
        btnDevolverUI.setOnAction(e -> dominio.devolverRecurso("OSC-100", "Gabriel (UI)"));

        Button btnAlocacaoBackground = new Button("⚡ Disparar Alocação em Background Thread");
        btnAlocacaoBackground.setStyle("-fx-background-color: #805ad5; -fx-text-fill: white; -fx-font-weight: bold;");
        btnAlocacaoBackground.setOnAction(e -> {
            // Demonstra que uma thread separada que altera o domínio notifica a UI de forma 100% segura
            new Thread(() -> {
                try {
                    Thread.sleep(150); // Simula trabalho assíncrono
                    dominio.alocarRecurso("GEN-300", "Worker-Thread-Async");
                } catch (Exception ex) {
                    System.err.println("Erro na thread secundária: " + ex.getMessage());
                }
            }, "Worker-Alocacao").start();
        });

        Button btnCadastrar = new Button("Cadastrar Novo Lote");
        btnCadastrar.setStyle("-fx-background-color: #4a5568; -fx-text-fill: white;");
        btnCadastrar.setOnAction(e -> {
            String codigo = "LOTE-" + (int)(Math.random() * 900 + 100);
            dominio.cadastrarNovoItem(codigo, "Multímetro True-RMS " + codigo, 5, "Coordenação");
        });

        boxBotoes.getChildren().addAll(btnAlocarUI, btnDevolverUI, btnAlocacaoBackground, btnCadastrar);
        raiz.setBottom(boxBotoes);

        Scene scene = new Scene(raiz, 950, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox criarCardMetrica(String titulo, SimpleIntegerProperty property, String corDestaque) {
        VBox card = new VBox(4);
        card.setStyle(String.format(
                "-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 6; " +
                        "-fx-background-radius: 6; -fx-padding: 8 16; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 4, 0, 0, 1);"
        ));
        Label lblNome = new Label(titulo);
        lblNome.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096; -fx-font-weight: bold;");

        Label lblValor = new Label();
        lblValor.textProperty().bind(property.asString());
        lblValor.setStyle(String.format("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: %s;", corDestaque));

        card.getChildren().addAll(lblNome, lblValor);
        return card;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
