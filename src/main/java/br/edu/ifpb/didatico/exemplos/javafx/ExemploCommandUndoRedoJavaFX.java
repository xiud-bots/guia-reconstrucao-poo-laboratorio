package br.edu.ifpb.didatico.exemplos.javafx;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * Demonstração Didática do Guia 22:
 * Padrão GoF Command, Gerenciador de Histórico (Undo / Redo), Atalhos Universais (Ctrl+Z / Ctrl+Y)
 * e Trilha Visual de Auditoria de Operações no JavaFX.
 */
public class ExemploCommandUndoRedoJavaFX extends Application {

    // 1. Modelo de Domínio Genérico Didático
    public static final class ItemInventario {
        private final SimpleStringProperty codigo;
        private final SimpleStringProperty nome;
        private final SimpleStringProperty categoria;
        private final SimpleBooleanProperty emManutencao;

        public ItemInventario(String codigo, String nome, String categoria, boolean emManutencao) {
            this.codigo = new SimpleStringProperty(Objects.requireNonNull(codigo).trim());
            this.nome = new SimpleStringProperty(Objects.requireNonNull(nome).trim());
            this.categoria = new SimpleStringProperty(Objects.requireNonNull(categoria).trim());
            this.emManutencao = new SimpleBooleanProperty(emManutencao);
        }

        public String getCodigo() { return codigo.get(); }
        public String getNome() { return nome.get(); }
        public String getCategoria() { return categoria.get(); }
        public boolean isEmManutencao() { return emManutencao.get(); }
        public void setEmManutencao(boolean valor) { this.emManutencao.set(valor); }

        public SimpleStringProperty codigoProperty() { return codigo; }
        public SimpleStringProperty nomeProperty() { return nome; }
        public SimpleStringProperty categoriaProperty() { return categoria; }
        public SimpleBooleanProperty emManutencaoProperty() { return emManutencao; }

        @Override
        public String toString() {
            return String.format("[%s] %s (%s)", getCodigo(), getNome(), getCategoria());
        }
    }

    // 2. Contrato do Padrão GoF Command
    public interface IComando {
        void executar();
        void desfazer();
        String getDescricao();
    }

    // 3. Comandos Concretos Autocontidos
    public static final class ComandoCadastrarItem implements IComando {
        private final ObservableList<ItemInventario> lista;
        private final ItemInventario item;

        public ComandoCadastrarItem(ObservableList<ItemInventario> lista, ItemInventario item) {
            this.lista = Objects.requireNonNull(lista);
            this.item = Objects.requireNonNull(item);
        }

        @Override
        public void executar() {
            lista.add(item);
        }

        @Override
        public void desfazer() {
            lista.remove(item);
        }

        @Override
        public String getDescricao() {
            return "Cadastrar: " + item.getNome() + " (" + item.getCodigo() + ")";
        }
    }

    public static final class ComandoRemoverItem implements IComando {
        private final ObservableList<ItemInventario> lista;
        private final ItemInventario item;
        private int indiceOriginal = -1;

        public ComandoRemoverItem(ObservableList<ItemInventario> lista, ItemInventario item) {
            this.lista = Objects.requireNonNull(lista);
            this.item = Objects.requireNonNull(item);
        }

        @Override
        public void executar() {
            this.indiceOriginal = lista.indexOf(item);
            if (this.indiceOriginal >= 0) {
                lista.remove(this.indiceOriginal);
            }
        }

        @Override
        public void desfazer() {
            if (indiceOriginal >= 0 && indiceOriginal <= lista.size()) {
                lista.add(indiceOriginal, item);
            } else {
                lista.add(item);
            }
        }

        @Override
        public String getDescricao() {
            return "Remover: " + item.getNome() + " (" + item.getCodigo() + ")";
        }
    }

    public static final class ComandoAlternarManutencao implements IComando {
        private final ItemInventario item;
        private final boolean statusAnterior;

        public ComandoAlternarManutencao(ItemInventario item) {
            this.item = Objects.requireNonNull(item);
            this.statusAnterior = item.isEmManutencao();
        }

        @Override
        public void executar() {
            item.setEmManutencao(!statusAnterior);
        }

        @Override
        public void desfazer() {
            item.setEmManutencao(statusAnterior);
        }

        @Override
        public String getDescricao() {
            return String.format("Alterar Manutenção de '%s' para %s",
                    item.getNome(), (!statusAnterior ? "EM MANUTENÇÃO" : "OPERACIONAL"));
        }
    }

    // 4. Gerenciador de Histórico (Invoker / History Manager)
    public static final class GerenciadorHistorico {
        private final Deque<IComando> pilhaUndo = new ArrayDeque<>();
        private final Deque<IComando> pilhaRedo = new ArrayDeque<>();
        private final int capacidadeMaxima;

        private final ReadOnlyBooleanWrapper podeDesfazer = new ReadOnlyBooleanWrapper(false);
        private final ReadOnlyBooleanWrapper podeRefazer = new ReadOnlyBooleanWrapper(false);
        private final ObservableList<String> trilhaAuditoria = FXCollections.observableArrayList();
        private final DateTimeFormatter formatadorHora = DateTimeFormatter.ofPattern("HH:mm:ss");

        public GerenciadorHistorico(int capacidadeMaxima) {
            this.capacidadeMaxima = Math.max(5, capacidadeMaxima);
        }

        public void executarComando(IComando comando) {
            comando.executar();
            pilhaUndo.push(comando);
            pilhaRedo.clear(); // Nova ação invalida o ramo de refazer!

            if (pilhaUndo.size() > capacidadeMaxima) {
                pilhaUndo.removeLast();
            }

            registrarLog("EXECUTOU", comando.getDescricao());
            atualizarPropriedades();
        }

        public void desfazer() {
            if (!pilhaUndo.isEmpty()) {
                IComando comando = pilhaUndo.pop();
                comando.desfazer();
                pilhaRedo.push(comando);

                registrarLog("DESFEZ (Undo)", comando.getDescricao());
                atualizarPropriedades();
            }
        }

        public void refazer() {
            if (!pilhaRedo.isEmpty()) {
                IComando comando = pilhaRedo.pop();
                comando.executar();
                pilhaUndo.push(comando);

                registrarLog("REFEZ (Redo)", comando.getDescricao());
                atualizarPropriedades();
            }
        }

        public ReadOnlyBooleanProperty podeDesfazerProperty() {
            return podeDesfazer.getReadOnlyProperty();
        }

        public ReadOnlyBooleanProperty podeRefazerProperty() {
            return podeRefazer.getReadOnlyProperty();
        }

        public ObservableList<String> getTrilhaAuditoria() {
            return trilhaAuditoria;
        }

        public int getTamanhoUndo() {
            return pilhaUndo.size();
        }

        public int getTamanhoRedo() {
            return pilhaRedo.size();
        }

        private void registrarLog(String tipo, String descricao) {
            String hora = LocalTime.now().format(formatadorHora);
            trilhaAuditoria.add(0, String.format("[%s] %s: %s", hora, tipo, descricao));
        }

        private void atualizarPropriedades() {
            podeDesfazer.set(!pilhaUndo.isEmpty());
            podeRefazer.set(!pilhaRedo.isEmpty());
        }
    }

    // 5. Interface Gráfica JavaFX
    private final ObservableList<ItemInventario> itens = FXCollections.observableArrayList();
    private final GerenciadorHistorico historico = new GerenciadorHistorico(50);
    private int contadorSequencial = 100;

    @Override
    public void start(Stage palcoPrincipal) {
        palcoPrincipal.setTitle("Demonstração Didática — Padrão Command, Undo/Redo e Atalhos de Teclado");

        popularDadosIniciais();

        // 1. Barra Superior com Controles de Formulário e Botões Undo/Redo
        HBox barraSuperior = criarBarraSuperior();

        // 2. Área Central: Tabela de Itens (Esquerda) e Trilha de Auditoria (Direita)
        HBox painelCentral = criarPainelCentral();

        // 3. Montagem do Layout Principal
        BorderPane layout = new BorderPane();
        layout.setTop(barraSuperior);
        layout.setCenter(painelCentral);

        Scene cena = new Scene(layout, 920, 520);

        // 4. Configuração dos Atalhos Universais no Palco (Ctrl+Z / Ctrl+Y)
        cena.getAccelerators().put(
                new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN),
                historico::desfazer
        );

        cena.getAccelerators().put(
                new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN),
                historico::refazer
        );

        palcoPrincipal.setScene(cena);
        palcoPrincipal.show();
    }

    private HBox criarBarraSuperior() {
        TextField txtNome = new TextField();
        txtNome.setPromptText("Nome do novo item...");
        txtNome.setPrefWidth(220);

        TextField txtCategoria = new TextField();
        txtCategoria.setPromptText("Categoria (ex: TI, Áudio)...");
        txtCategoria.setPrefWidth(160);

        Button btnAdicionar = new Button("+ Adicionar");
        btnAdicionar.setStyle("-fx-background-color: #198754; -fx-text-fill: white; -fx-font-weight: bold;");
        btnAdicionar.setOnAction(e -> {
            String nome = txtNome.getText().trim();
            String cat = txtCategoria.getText().trim();
            if (!nome.isEmpty() && !cat.isEmpty()) {
                contadorSequencial++;
                ItemInventario novo = new ItemInventario("INV-" + contadorSequencial, nome, cat, false);
                historico.executarComando(new ComandoCadastrarItem(itens, novo));
                txtNome.clear();
                txtCategoria.clear();
            }
        });

        Button btnDesfazer = new Button("↶ Desfazer (Ctrl+Z)");
        btnDesfazer.setStyle("-fx-background-color: #0d6efd; -fx-text-fill: white;");
        btnDesfazer.disableProperty().bind(historico.podeDesfazerProperty().not());
        btnDesfazer.setOnAction(e -> historico.desfazer());

        Button btnRefazer = new Button("↷ Refazer (Ctrl+Y)");
        btnRefazer.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        btnRefazer.disableProperty().bind(historico.podeRefazerProperty().not());
        btnRefazer.setOnAction(e -> historico.refazer());

        HBox barra = new HBox(8, txtNome, txtCategoria, btnAdicionar, new Label("|"), btnDesfazer, btnRefazer);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(10, 14, 10, 14));
        barra.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");
        return barra;
    }

    private HBox criarPainelCentral() {
        // Tabela Principal
        TableView<ItemInventario> tabela = new TableView<>(itens);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        HBox.setHgrow(tabela, Priority.ALWAYS);

        TableColumn<ItemInventario, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(cell -> cell.getValue().codigoProperty());
        colCodigo.setMaxWidth(90);

        TableColumn<ItemInventario, String> colNome = new TableColumn<>("Nome do Ativo");
        colNome.setCellValueFactory(cell -> cell.getValue().nomeProperty());

        TableColumn<ItemInventario, String> colCat = new TableColumn<>("Categoria");
        colCat.setCellValueFactory(cell -> cell.getValue().categoriaProperty());
        colCat.setMaxWidth(130);

        TableColumn<ItemInventario, Boolean> colStatus = new TableColumn<>("Condição");
        colStatus.setCellValueFactory(cell -> cell.getValue().emManutencaoProperty());
        colStatus.setMaxWidth(130);
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean emManutencao, boolean vazio) {
                super.updateItem(emManutencao, vazio);
                if (vazio || emManutencao == null) {
                    setText(null);
                    setStyle("");
                } else if (emManutencao) {
                    setText("EM MANUTENÇÃO");
                    setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                } else {
                    setText("OPERACIONAL");
                    setStyle("-fx-text-fill: #198754; -fx-font-weight: bold;");
                }
                setAlignment(Pos.CENTER);
            }
        });

        TableColumn<ItemInventario, Void> colAcoes = new TableColumn<>("Ações Reversíveis");
        colAcoes.setMaxWidth(180);
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnAlternar = new Button("Alternar");
            private final Button btnRemover = new Button("Excluir");
            private final HBox painel = new HBox(6, btnAlternar, btnRemover);

            {
                painel.setAlignment(Pos.CENTER);
                btnAlternar.setStyle("-fx-font-size: 11px;");
                btnRemover.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 11px;");

                btnAlternar.setOnAction(e -> {
                    ItemInventario item = getTableRow().getItem();
                    if (item != null) {
                        historico.executarComando(new ComandoAlternarManutencao(item));
                    }
                });

                btnRemover.setOnAction(e -> {
                    ItemInventario item = getTableRow().getItem();
                    if (item != null) {
                        historico.executarComando(new ComandoRemoverItem(itens, item));
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean vazio) {
                super.updateItem(item, vazio);
                setGraphic(vazio || getTableRow() == null || getTableRow().getItem() == null ? null : painel);
            }
        });

        tabela.getColumns().addAll(colCodigo, colNome, colCat, colStatus, colAcoes);

        // Painel Lateral de Trilha de Auditoria
        VBox painelAuditoria = new VBox(6);
        painelAuditoria.setPrefWidth(300);
        painelAuditoria.setPadding(new Insets(10));
        painelAuditoria.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dee2e6; -fx-border-width: 0 0 0 1;");

        Label lblTituloAuditoria = new Label("Trilha de Operações (Histórico)");
        lblTituloAuditoria.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");

        ListView<String> listaAuditoria = new ListView<>(historico.getTrilhaAuditoria());
        VBox.setVgrow(listaAuditoria, Priority.ALWAYS);
        listaAuditoria.setStyle("-fx-font-size: 11px; -fx-font-family: monospace;");

        painelAuditoria.getChildren().addAll(lblTituloAuditoria, listaAuditoria);

        HBox central = new HBox(tabela, painelAuditoria);
        HBox.setHgrow(tabela, Priority.ALWAYS);
        return central;
    }

    private void popularDadosIniciais() {
        itens.addAll(
                new ItemInventario("INV-001", "Servidor Rack 2U Dell PowerEdge", "Servidores", false),
                new ItemInventario("INV-002", "Projetor Laser Interativo Epson", "Audiovisual", false),
                new ItemInventario("INV-003", "Impressora 3D de Resina Creality", "Prototipagem", true)
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
