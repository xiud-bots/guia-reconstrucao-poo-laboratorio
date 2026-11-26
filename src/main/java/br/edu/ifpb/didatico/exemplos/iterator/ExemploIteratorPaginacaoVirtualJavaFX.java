package br.edu.ifpb.didatico.exemplos.iterator;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Exemplo didático do Padrão GoF Iterator combinado com Paginação Virtual sob demanda no JavaFX.
 * Demonstra a navegação desacoplada em grandes volumes de dados (ex.: milhares de registros)
 * sem saturar a memória Heap da JVM e sem congelar a JavaFX Application Thread.
 */
public class ExemploIteratorPaginacaoVirtualJavaFX extends Application {

    // =========================================================================
    // 1. MODELO DE DOMÍNIO
    // =========================================================================

    public record RegistroAcervoVirtual(
            int idSequencial,
            String codigoTombamento,
            String tituloDescricao,
            String departamentoResponsavel,
            LocalDate dataEntrada,
            boolean ativo
    ) {
        public RegistroAcervoVirtual {
            Objects.requireNonNull(codigoTombamento, "codigoTombamento não pode ser nulo.");
            Objects.requireNonNull(tituloDescricao, "tituloDescricao não pode ser nulo.");
            Objects.requireNonNull(departamentoResponsavel, "departamentoResponsavel não pode ser nulo.");
            Objects.requireNonNull(dataEntrada, "dataEntrada não pode ser nula.");
        }
    }

    // =========================================================================
    // 2. CONTRATOS DO PADRÃO ITERATOR COM PAGINAÇÃO VIRTUAL
    // =========================================================================

    /**
     * Contrato do Iterator Paginado Bidirecional (Virtual Paged Iterator).
     */
    public interface IPaginadorIterator<T> {
        boolean hasNextPage();
        boolean hasPreviousPage();
        List<T> nextPage();
        List<T> previousPage();
        List<T> jumpToPage(int numeroPagina);
        List<T> currentPageItems();
        int getCurrentPage();
        int getTotalPages();
        int getTotalItems();
        int getPageSize();
        void setPageSize(int novoTamanho);
        void aplicarFiltro(Predicate<T> filtro);
        void limparFiltro();
    }

    // =========================================================================
    // 3. IMPLEMENTAÇÃO CONCRETA DO ITERATOR PAGINADO
    // =========================================================================

    public static class PaginadorVirtualMemoria<T> implements IPaginadorIterator<T> {
        private final List<T> fonteDadosCompleta;
        private List<T> dadosFiltrados;
        private int tamanhoPagina;
        private int paginaAtual; // 1-based index

        public PaginadorVirtualMemoria(List<T> fonteDados, int tamanhoPaginaInicial) {
            this.fonteDadosCompleta = new ArrayList<>(Objects.requireNonNull(fonteDados, "fonteDados não pode ser nula."));
            this.dadosFiltrados = new ArrayList<>(this.fonteDadosCompleta);
            if (tamanhoPaginaInicial <= 0) {
                throw new IllegalArgumentException("Tamanho de página deve ser maior que zero.");
            }
            this.tamanhoPagina = tamanhoPaginaInicial;
            this.paginaAtual = 1;
        }

        @Override
        public boolean hasNextPage() {
            return paginaAtual < getTotalPages();
        }

        @Override
        public boolean hasPreviousPage() {
            return paginaAtual > 1;
        }

        @Override
        public List<T> nextPage() {
            if (!hasNextPage()) {
                throw new NoSuchElementException("Não há próxima página disponível.");
            }
            paginaAtual++;
            return currentPageItems();
        }

        @Override
        public List<T> previousPage() {
            if (!hasPreviousPage()) {
                throw new NoSuchElementException("Não há página anterior disponível.");
            }
            paginaAtual--;
            return currentPageItems();
        }

        @Override
        public List<T> jumpToPage(int numeroPagina) {
            int max = Math.max(1, getTotalPages());
            if (numeroPagina < 1 || numeroPagina > max) {
                throw new IllegalArgumentException("Página inválida: " + numeroPagina + ". Intervalo permitido: 1 a " + max);
            }
            this.paginaAtual = numeroPagina;
            return currentPageItems();
        }

        @Override
        public List<T> currentPageItems() {
            if (dadosFiltrados.isEmpty()) {
                return Collections.emptyList();
            }

            int inicio = (paginaAtual - 1) * tamanhoPagina;
            if (inicio >= dadosFiltrados.size()) {
                paginaAtual = Math.max(1, getTotalPages());
                inicio = (paginaAtual - 1) * tamanhoPagina;
            }

            int fim = Math.min(inicio + tamanhoPagina, dadosFiltrados.size());
            return Collections.unmodifiableList(dadosFiltrados.subList(inicio, fim));
        }

        @Override
        public int getCurrentPage() {
            return paginaAtual;
        }

        @Override
        public int getTotalPages() {
            if (dadosFiltrados.isEmpty()) return 1;
            return (int) Math.ceil((double) dadosFiltrados.size() / tamanhoPagina);
        }

        @Override
        public int getTotalItems() {
            return dadosFiltrados.size();
        }

        @Override
        public int getPageSize() {
            return tamanhoPagina;
        }

        @Override
        public void setPageSize(int novoTamanho) {
            if (novoTamanho <= 0) {
                throw new IllegalArgumentException("Tamanho deve ser positivo.");
            }
            this.tamanhoPagina = novoTamanho;
            this.paginaAtual = 1; // Reseta para a primeira página para evitar índices órfãos
        }

        @Override
        public void aplicarFiltro(Predicate<T> filtro) {
            Objects.requireNonNull(filtro, "Filtro não pode ser nulo.");
            this.dadosFiltrados = this.fonteDadosCompleta.stream().filter(filtro).toList();
            this.paginaAtual = 1;
        }

        @Override
        public void limparFiltro() {
            this.dadosFiltrados = new ArrayList<>(this.fonteDadosCompleta);
            this.paginaAtual = 1;
        }
    }

    // =========================================================================
    // 4. INTERFACE DESKTOP JAVAFX DEMONSTRATIVA
    // =========================================================================

    private IPaginadorIterator<RegistroAcervoVirtual> paginador;
    private final ObservableList<RegistroAcervoVirtual> paginaItensObservable = FXCollections.observableArrayList();
    private TableView<RegistroAcervoVirtual> tabela;

    private Button btnPrimeira;
    private Button btnAnterior;
    private Button btnProxima;
    private Button btnUltima;
    private Label lblIndicadorPagina;
    private Label lblResumoTotal;
    private TextField txtBuscaFiltro;

    @Override
    public void start(Stage primaryStage) {
        // Inicializa o paginador com 5.000 registros sintéticos de grande volume
        List<RegistroAcervoVirtual> base = gerarDadosSinteticos(5000);
        this.paginador = new PaginadorVirtualMemoria<>(base, 15);

        BorderPane raiz = new BorderPane();
        raiz.setPadding(new Insets(14));
        raiz.setStyle("-fx-font-family: 'Segoe UI', sans-serif;");

        // Topo: Cabeçalho e Barra de Filtro Rápido
        VBox topo = new VBox(10);
        Label lblTitulo = new Label("Padrão GoF Iterator: Navegação e Paginação Virtual em JavaFX");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #004d40;");

        HBox barraFiltro = new HBox(12);
        barraFiltro.setAlignment(Pos.CENTER_LEFT);

        Label lblBusca = new Label("Filtrar Título/Código:");
        lblBusca.setStyle("-fx-font-weight: bold;");

        txtBuscaFiltro = new TextField();
        txtBuscaFiltro.setPromptText("Digite para filtrar...");
        txtBuscaFiltro.setPrefWidth(260);

        txtBuscaFiltro.textProperty().addListener((obs, antigo, novo) -> {
            if (novo == null || novo.trim().isEmpty()) {
                paginador.limparFiltro();
            } else {
                String termo = novo.trim().toLowerCase();
                paginador.aplicarFiltro(r ->
                        r.codigoTombamento().toLowerCase().contains(termo)
                                || r.tituloDescricao().toLowerCase().contains(termo)
                                || r.departamentoResponsavel().toLowerCase().contains(termo)
                );
            }
            atualizarVisualizacao();
        });

        lblResumoTotal = new Label();
        lblResumoTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #00796b;");

        barraFiltro.getChildren().addAll(lblBusca, txtBuscaFiltro, lblResumoTotal);
        topo.getChildren().addAll(lblTitulo, barraFiltro);
        raiz.setTop(topo);

        // Centro: TableView contendo APENAS a página atual na memória de renderização
        tabela = construirTabela();
        raiz.setCenter(tabela);

        // Rodapé: Controles de Navegação do Iterator (<<, <, Página X de Y, >, >>, Tamanho)
        HBox barraNavegacao = new HBox(10);
        barraNavegacao.setPadding(new Insets(10, 0, 0, 0));
        barraNavegacao.setAlignment(Pos.CENTER);

        btnPrimeira = new Button("<<");
        btnPrimeira.setStyle("-fx-font-weight: bold;");
        btnPrimeira.setOnAction(e -> {
            paginador.jumpToPage(1);
            atualizarVisualizacao();
        });

        btnAnterior = new Button("< Anterior");
        btnAnterior.setStyle("-fx-font-weight: bold;");
        btnAnterior.setOnAction(e -> {
            if (paginador.hasPreviousPage()) {
                paginador.previousPage();
                atualizarVisualizacao();
            }
        });

        lblIndicadorPagina = new Label("Página 1 de 1");
        lblIndicadorPagina.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 0 8;");

        btnProxima = new Button("Próxima >");
        btnProxima.setStyle("-fx-font-weight: bold;");
        btnProxima.setOnAction(e -> {
            if (paginador.hasNextPage()) {
                paginador.nextPage();
                atualizarVisualizacao();
            }
        });

        btnUltima = new Button(">>");
        btnUltima.setStyle("-fx-font-weight: bold;");
        btnUltima.setOnAction(e -> {
            paginador.jumpToPage(paginador.getTotalPages());
            atualizarVisualizacao();
        });

        Label lblTam = new Label("Itens/Pág:");
        ComboBox<Integer> comboTamanho = new ComboBox<>(FXCollections.observableArrayList(10, 15, 25, 50));
        comboTamanho.setValue(15);
        comboTamanho.setOnAction(e -> {
            if (comboTamanho.getValue() != null) {
                paginador.setPageSize(comboTamanho.getValue());
                atualizarVisualizacao();
            }
        });

        barraNavegacao.getChildren().addAll(
                btnPrimeira, btnAnterior, lblIndicadorPagina, btnProxima, btnUltima,
                new Label(" | "), lblTam, comboTamanho
        );
        raiz.setBottom(barraNavegacao);

        atualizarVisualizacao();

        primaryStage.setTitle("Demonstração: GoF Iterator & Paginação Virtual no JavaFX");
        primaryStage.setScene(new Scene(raiz, 940, 540));
        primaryStage.show();
    }

    @SuppressWarnings("unchecked")
    private TableView<RegistroAcervoVirtual> construirTabela() {
        TableView<RegistroAcervoVirtual> tv = new TableView<>();
        VBox.setVgrow(tv, Priority.ALWAYS);

        TableColumn<RegistroAcervoVirtual, Number> colId = new TableColumn<>("#");
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().idSequencial()));
        colId.setPrefWidth(60);

        TableColumn<RegistroAcervoVirtual, String> colCod = new TableColumn<>("Código Tombamento");
        colCod.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().codigoTombamento()));
        colCod.setPrefWidth(160);

        TableColumn<RegistroAcervoVirtual, String> colDesc = new TableColumn<>("Descrição / Ativo");
        colDesc.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().tituloDescricao()));
        colDesc.setPrefWidth(280);

        TableColumn<RegistroAcervoVirtual, String> colDept = new TableColumn<>("Departamento");
        colDept.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().departamentoResponsavel()));
        colDept.setPrefWidth(180);

        TableColumn<RegistroAcervoVirtual, String> colData = new TableColumn<>("Data Entrada");
        colData.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().dataEntrada().toString()));
        colData.setPrefWidth(110);

        TableColumn<RegistroAcervoVirtual, String> colAtivo = new TableColumn<>("Situação");
        colAtivo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().ativo() ? "ATIVO" : "INATIVO"));
        colAtivo.setPrefWidth(90);

        tv.getColumns().addAll(colId, colCod, colDesc, colDept, colData, colAtivo);
        tv.setItems(paginaItensObservable);
        return tv;
    }

    private void atualizarVisualizacao() {
        List<RegistroAcervoVirtual> itensPagina = paginador.currentPageItems();
        paginaItensObservable.setAll(itensPagina);

        int totalPaginas = paginador.getTotalPages();
        int paginaAtual = paginador.getCurrentPage();
        int totalItens = paginador.getTotalItems();

        lblIndicadorPagina.setText("Página " + paginaAtual + " de " + totalPaginas);

        int itemInicio = totalItens == 0 ? 0 : ((paginaAtual - 1) * paginador.getPageSize()) + 1;
        int itemFim = totalItens == 0 ? 0 : Math.min(itemInicio + itensPagina.size() - 1, totalItens);

        lblResumoTotal.setText(String.format("Exibindo itens %d a %d de %d registros filtrados", itemInicio, itemFim, totalItens));

        btnPrimeira.setDisable(paginaAtual <= 1);
        btnAnterior.setDisable(!paginador.hasPreviousPage());
        btnProxima.setDisable(!paginador.hasNextPage());
        btnUltima.setDisable(paginaAtual >= totalPaginas);
    }

    private static List<RegistroAcervoVirtual> gerarDadosSinteticos(int quantidade) {
        List<RegistroAcervoVirtual> lista = new ArrayList<>(quantidade);
        String[] depts = {"Engenharia Elétrica", "Telecomunicações", "Informática", "Mecânica", "Física Aplicada"};
        String[] tipos = {"Bancada de Testes", "Analisador Lógico", "Microscópio Eletrônico", "Kit FPGA", "Estação de Retrabalho"};

        LocalDate baseDate = LocalDate.now().minusDays(quantidade);
        for (int i = 1; i <= quantidade; i++) {
            String dept = depts[i % depts.length];
            String tipo = tipos[i % tipos.length];
            lista.add(new RegistroAcervoVirtual(
                    i,
                    String.format("TOMB-2026-%05d", i),
                    tipo + " Série " + (i % 50),
                    dept,
                    baseDate.plusDays(i % 1000),
                    (i % 7 != 0)
            ));
        }
        return lista;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
