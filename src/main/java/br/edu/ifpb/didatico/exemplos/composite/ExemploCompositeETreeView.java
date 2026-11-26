package br.edu.ifpb.didatico.exemplos.composite;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Exemplo didático do Padrão GoF Composite integrado a um TreeView no JavaFX.
 * Demonstra a modelagem de hierarquias recursivas de domínio (estruturas em árvore)
 * onde nós folha e nós compostos são tratados de forma uniforme e polimórfica.
 */
public class ExemploCompositeETreeView extends Application {

    private static final Locale LOCALE_BR = Locale.of("pt", "BR");
    private static final NumberFormat MOEDA_FORMAT = NumberFormat.getCurrencyInstance(LOCALE_BR);

    // =========================================================================
    // 1. CONTRATO DO PADRÃO COMPOSITE (Component)
    // =========================================================================

    /**
     * Componente base abstrato da hierarquia patrimonial.
     * Define a interface comum tanto para itens individuais (folhas)
     * quanto para grupos/departamentos (compostos).
     */
    public static abstract class ComponentePatrimonial {
        private final String codigo;
        private final String nome;

        protected ComponentePatrimonial(String codigo, String nome) {
            if (codigo == null || codigo.isBlank()) {
                throw new IllegalArgumentException("Código do componente não pode ser nulo ou vazio.");
            }
            if (nome == null || nome.isBlank()) {
                throw new IllegalArgumentException("Nome do componente não pode ser nulo ou vazio.");
            }
            this.codigo = codigo.trim();
            this.nome = nome.trim();
        }

        public String getCodigo() {
            return codigo;
        }

        public String getNome() {
            return nome;
        }

        /**
         * Operação polimórfica que calcula o valor monetário total.
         * Nas folhas retorna o valor próprio; nos compostos agrega os filhos recursivamente.
         */
        public abstract BigDecimal calcularValorTotal();

        /**
         * Operação polimórfica que conta o total de itens físicos.
         */
        public abstract int contarItensFisicos();

        /**
         * Informa se o componente aceita nós filhos.
         */
        public abstract boolean isComposto();

        @Override
        public String toString() {
            return String.format("[%s] %s", codigo, nome);
        }
    }

    // =========================================================================
    // 2. ELEMENTO FOLHA (Leaf)
    // =========================================================================

    /**
     * Nó Folha: representa um item ou equipamento físico individual.
     * Não possui filhos e responde com seus valores atômicos.
     */
    public static final class ItemFolha extends ComponentePatrimonial {
        private final BigDecimal valorIndividual;

        public ItemFolha(String codigo, String nome, BigDecimal valorIndividual) {
            super(codigo, nome);
            if (valorIndividual == null || valorIndividual.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Valor individual não pode ser nulo ou negativo.");
            }
            this.valorIndividual = valorIndividual;
        }

        @Override
        public BigDecimal calcularValorTotal() {
            return valorIndividual;
        }

        @Override
        public int contarItensFisicos() {
            return 1;
        }

        @Override
        public boolean isComposto() {
            return false;
        }
    }

    // =========================================================================
    // 3. ELEMENTO COMPOSTO (Composite)
    // =========================================================================

    /**
     * Nó Composto: representa um departamento, setor, laboratório ou categoria.
     * Armazena filhos que podem ser tanto Folhas quanto outros Compostos (recursão).
     */
    public static final class CategoriaComposta extends ComponentePatrimonial {
        private final List<ComponentePatrimonial> filhos = new ArrayList<>();

        public CategoriaComposta(String codigo, String nome) {
            super(codigo, nome);
        }

        /**
         * Adiciona um componente filho com validação defensiva anti-loop.
         */
        public void adicionar(ComponentePatrimonial componente) {
            Objects.requireNonNull(componente, "Componente filho não pode ser nulo.");
            if (componente == this) {
                throw new IllegalArgumentException("Um composto não pode conter a si mesmo (ciclo infinito).");
            }
            filhos.add(componente);
        }

        /**
         * Remove um componente filho.
         */
        public boolean remover(ComponentePatrimonial componente) {
            return filhos.remove(componente);
        }

        /**
         * Retorna lista imutável dos filhos imediatos.
         */
        public List<ComponentePatrimonial> getFilhos() {
            return Collections.unmodifiableList(filhos);
        }

        @Override
        public BigDecimal calcularValorTotal() {
            BigDecimal total = BigDecimal.ZERO;
            for (ComponentePatrimonial filho : filhos) {
                total = total.add(filho.calcularValorTotal());
            }
            return total;
        }

        @Override
        public int contarItensFisicos() {
            int total = 0;
            for (ComponentePatrimonial filho : filhos) {
                total += filho.contarItensFisicos();
            }
            return total;
        }

        @Override
        public boolean isComposto() {
            return true;
        }
    }

    // =========================================================================
    // 4. INTERFACE GRÁFICA JAVAFX E INTEGRAÇÃO COM TREEVIEW
    // =========================================================================

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Demonstração GoF Composite & TreeView - XIUD Didático");

        // Constrói árvore de dados didática
        CategoriaComposta raizCampus = criarEstruturaCampusExemplo();

        // Converte a hierarquia do Composite em TreeItem do JavaFX
        TreeItem<ComponentePatrimonial> rootItem = construirTreeItemRecursivo(raizCampus);
        rootItem.setExpanded(true);

        TreeView<ComponentePatrimonial> treeView = new TreeView<>(rootItem);

        // Customização de Célula (TreeCell) com estilização diferenciada
        treeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(ComponentePatrimonial item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    if (item.isComposto()) {
                        setText(String.format("📁 %s (%d itens | %s)",
                                item.getNome(),
                                item.contarItensFisicos(),
                                MOEDA_FORMAT.format(item.calcularValorTotal())));
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #1a365d;");
                    } else {
                        setText(String.format("📄 [%s] %s — %s",
                                item.getCodigo(),
                                item.getNome(),
                                MOEDA_FORMAT.format(item.calcularValorTotal())));
                        setStyle("-fx-font-weight: normal; -fx-text-fill: #2d3748;");
                    }
                }
            }
        });

        // Painel de detalhes reativo
        Label lblDetalheTitulo = new Label("Selecione um nó da hierarquia para inspecionar");
        lblDetalheTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label lblDetalheTipo = new Label("Tipo: -");
        Label lblDetalheCodigo = new Label("Código: -");
        Label lblDetalheQtd = new Label("Quantidade de Itens Físicos: -");
        Label lblDetalheValor = new Label("Valor Total Acumulado: -");

        VBox painelDetalhes = new VBox(12, lblDetalheTitulo, lblDetalheTipo, lblDetalheCodigo, lblDetalheQtd, lblDetalheValor);
        painelDetalhes.setPadding(new Insets(20));
        painelDetalhes.setStyle("-fx-background-color: #f7fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1px;");

        // Escuta a seleção do TreeView
        treeView.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null && novo.getValue() != null) {
                ComponentePatrimonial comp = novo.getValue();
                lblDetalheTitulo.setText(comp.getNome());
                lblDetalheTipo.setText("Tipo: " + (comp.isComposto() ? "Categoria Composta (Grupo)" : "Item Folha (Equipamento Individual)"));
                lblDetalheCodigo.setText("Código: " + comp.getCodigo());
                lblDetalheQtd.setText("Quantidade de Itens Físicos: " + comp.contarItensFisicos() + " unidade(s)");
                lblDetalheValor.setText("Valor Total Acumulado: " + MOEDA_FORMAT.format(comp.calcularValorTotal()));
            }
        });

        SplitPane splitPane = new SplitPane(treeView, painelDetalhes);
        splitPane.setDividerPositions(0.55);

        BorderPane root = new BorderPane(splitPane);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 880, 520);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Mapeia recursivamente a estrutura GoF Composite para nós TreeItem do JavaFX.
     */
    public static TreeItem<ComponentePatrimonial> construirTreeItemRecursivo(ComponentePatrimonial componente) {
        TreeItem<ComponentePatrimonial> treeNode = new TreeItem<>(componente);
        if (componente instanceof CategoriaComposta composta) {
            for (ComponentePatrimonial filho : composta.getFilhos()) {
                TreeItem<ComponentePatrimonial> childNode = construirTreeItemRecursivo(filho);
                if (filho.isComposto()) {
                    childNode.setExpanded(true);
                }
                treeNode.getChildren().add(childNode);
            }
        }
        return treeNode;
    }

    /**
     * Cria uma árvore didática de exemplo para testes e demonstrações visuais.
     */
    public static CategoriaComposta criarEstruturaCampusExemplo() {
        CategoriaComposta campus = new CategoriaComposta("CAMPUS-01", "Campus Central XIUD");

        CategoriaComposta labInformatica = new CategoriaComposta("DEP-INF", "Laboratório de Informática");
        labInformatica.adicionar(new ItemFolha("PC-01", "Workstation Dell i7 32GB", new BigDecimal("7500.00")));
        labInformatica.adicionar(new ItemFolha("PC-02", "Workstation Dell i7 32GB", new BigDecimal("7500.00")));
        labInformatica.adicionar(new ItemFolha("SRV-01", "Servidor PowerEdge Rack", new BigDecimal("28000.00")));

        CategoriaComposta labRedes = new CategoriaComposta("SUB-REDES", "Infraestrutura de Conectividade");
        labRedes.adicionar(new ItemFolha("SW-01", "Switch Gerenciável 48p PoE", new BigDecimal("4200.00")));
        labRedes.adicionar(new ItemFolha("RT-01", "Roteador de Borda Mikrotik", new BigDecimal("2300.00")));
        labInformatica.adicionar(labRedes);

        CategoriaComposta labFisica = new CategoriaComposta("DEP-FIS", "Laboratório de Física Experimental");
        labFisica.adicionar(new ItemFolha("OSC-01", "Osciloscópio Digital 100MHz", new BigDecimal("5400.00")));
        labFisica.adicionar(new ItemFolha("FNT-01", "Fonte DC Ajustável 30V 5A", new BigDecimal("1200.00")));

        campus.adicionar(labInformatica);
        campus.adicionar(labFisica);

        return campus;
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
