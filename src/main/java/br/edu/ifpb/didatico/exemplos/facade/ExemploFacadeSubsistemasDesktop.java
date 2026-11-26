package br.edu.ifpb.didatico.exemplos.facade;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Exemplo didático do Padrão GoF Facade (Fachada) integrado a uma interface JavaFX.
 * Demonstra como encapsular múltiplos subsistemas complexos (Validação de Usuário,
 * Controle de Estoque, Cálculo de Prazos, Auditoria Criptográfica e Notificações)
 * atrás de uma única interface limpa e coesa para controladores de apresentação.
 */
public class ExemploFacadeSubsistemasDesktop extends Application {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // =========================================================================
    // 1. DTOs E RECORDS DE COMUNICAÇÃO (Java 21)
    // =========================================================================

    public record SolicitacaoAlocacao(
            String codigoUsuario,
            String codigoRecurso,
            int diasSolicitados,
            String finalidade
    ) {
        public SolicitacaoAlocacao {
            if (codigoUsuario == null || codigoUsuario.isBlank()) {
                throw new IllegalArgumentException("Código de usuário é obrigatório.");
            }
            if (codigoRecurso == null || codigoRecurso.isBlank()) {
                throw new IllegalArgumentException("Código do recurso é obrigatório.");
            }
            if (diasSolicitados <= 0 || diasSolicitados > 60) {
                throw new IllegalArgumentException("Prazo de alocação deve estar entre 1 e 60 dias.");
            }
            codigoUsuario = codigoUsuario.trim();
            codigoRecurso = codigoRecurso.trim();
            finalidade = (finalidade == null || finalidade.isBlank()) ? "Uso Institucional" : finalidade.trim();
        }
    }

    public record ComprovanteAlocacao(
            String idTransacao,
            String codigoUsuario,
            String nomeUsuario,
            String codigoRecurso,
            String descricaoRecurso,
            LocalDate dataInicio,
            LocalDate dataPrevistaDevolucao,
            String hashSeguranca,
            LocalDateTime emitidoEm
    ) {
        public String formatarRecibo() {
            return String.format("""
                    =============================================================
                                COMPROVANTE OFICIAL DE ALOCAÇÃO
                    =============================================================
                    ID Transação  : %s
                    Emitido em    : %s
                    -------------------------------------------------------------
                    Solicitante   : [%s] %s
                    Recurso       : [%s] %s
                    Período       : %s até %s
                    -------------------------------------------------------------
                    Validação SHA : %s
                    =============================================================""",
                    idTransacao,
                    emitidoEm.format(FORMATO_HORA),
                    codigoUsuario, nomeUsuario,
                    codigoRecurso, descricaoRecurso,
                    dataInicio.format(FORMATO_DATA),
                    dataPrevistaDevolucao.format(FORMATO_DATA),
                    hashSeguranca);
        }
    }

    public static class FalhaAlocacaoException extends Exception {
        private final String codigoEtapa;

        public FalhaAlocacaoException(String codigoEtapa, String mensagem) {
            super(mensagem);
            this.codigoEtapa = codigoEtapa;
        }

        public String getCodigoEtapa() {
            return codigoEtapa;
        }
    }

    // =========================================================================
    // 2. SUBSISTEMAS INDEPENDENTES
    // =========================================================================

    public static class SubservicoUsuario {
        public record Usuario(String matricula, String nome, boolean ativo, boolean suspenso) {}

        private final Map<String, Usuario> usuarios = new HashMap<>();

        public SubservicoUsuario() {
            usuarios.put("USR-01", new Usuario("USR-01", "Gabriel Aragão", true, false));
            usuarios.put("USR-02", new Usuario("USR-02", "Ana Paula Silva", true, false));
            usuarios.put("USR-03", new Usuario("USR-03", "Carlos Mendes (Inadimplente)", true, true));
            usuarios.put("USR-04", new Usuario("USR-04", "Mariana Costa (Desativada)", false, false));
        }

        public Usuario validarEObterUsuario(String matricula) throws FalhaAlocacaoException {
            Usuario u = usuarios.get(matricula);
            if (u == null) {
                throw new FalhaAlocacaoException("SUBSYS_USR_NAO_ENCONTRADO", "Usuário não localizado no sistema cadastral: " + matricula);
            }
            if (!u.ativo()) {
                throw new FalhaAlocacaoException("SUBSYS_USR_INATIVO", "O usuário " + u.nome() + " encontra-se desativado no cadastro.");
            }
            if (u.suspenso()) {
                throw new FalhaAlocacaoException("SUBSYS_USR_SUSPENSO", "O usuário " + u.nome() + " possui pendências regulamentares ou débitos em aberto.");
            }
            return u;
        }

        public List<Usuario> listarTodos() {
            return List.copyOf(usuarios.values());
        }
    }

    public static class SubservicoEstoque {
        public static class ItemEstoque {
            private final String codigo;
            private final String descricao;
            private boolean disponivel;

            public ItemEstoque(String codigo, String descricao, boolean disponivel) {
                this.codigo = codigo;
                this.descricao = descricao;
                this.disponivel = disponivel;
            }

            public String getCodigo() { return codigo; }
            public String getDescricao() { return descricao; }
            public boolean isDisponivel() { return disponivel; }
            public void setDisponivel(boolean disponivel) { this.disponivel = disponivel; }
        }

        private final Map<String, ItemEstoque> itens = new HashMap<>();

        public SubservicoEstoque() {
            itens.put("EQP-101", new ItemEstoque("EQP-101", "Osciloscópio Digital 100MHz", true));
            itens.put("EQP-102", new ItemEstoque("EQP-102", "Gerador de Funções RF 20MHz", true));
            itens.put("EQP-103", new ItemEstoque("EQP-103", "Fonte Bancada DC Ajustável", false));
            itens.put("EQP-104", new ItemEstoque("EQP-104", "Estação de Solda Profissional", true));
        }

        public synchronized ItemEstoque alocarRecurso(String codigo) throws FalhaAlocacaoException {
            ItemEstoque item = itens.get(codigo);
            if (item == null) {
                throw new FalhaAlocacaoException("SUBSYS_EST_NAO_ENCONTRADO", "Recurso não cadastrado no inventário: " + codigo);
            }
            if (!item.isDisponivel()) {
                throw new FalhaAlocacaoException("SUBSYS_EST_INDISPONIVEL", "Recurso [" + codigo + "] " + item.getDescricao() + " já está alocado para outro usuário.");
            }
            item.setDisponivel(false);
            return item;
        }

        public synchronized void estornarAlocacao(String codigo) {
            ItemEstoque item = itens.get(codigo);
            if (item != null) {
                item.setDisponivel(true);
            }
        }

        public List<ItemEstoque> listarTodos() {
            return List.copyOf(itens.values());
        }
    }

    public static class SubservicoCalculoPrazo {
        public LocalDate calcularDataPrevistaDevolucao(LocalDate inicio, int diasUteisOuCorridos) {
            LocalDate base = (inicio != null) ? inicio : LocalDate.now();
            return base.plusDays(diasUteisOuCorridos);
        }
    }

    public static class SubservicoAuditoriaCriptografica {
        private final List<String> trilhaAuditoria = new ArrayList<>();

        public synchronized String registrarEvento(String acao, String detalhes) {
            LocalDateTime agora = LocalDateTime.now();
            String payload = String.format("%s|%s|%s|%s", agora, acao, detalhes, UUID.randomUUID());
            String hash = gerarHashSha256(payload);
            String registroFormatado = String.format("[%s] AÇÃO: %-15s | HASH: %s | DADOS: %s",
                    agora.format(FORMATO_HORA), acao, hash.substring(0, 16) + "...", detalhes);
            trilhaAuditoria.add(registroFormatado);
            return hash;
        }

        public synchronized List<String> getHistorico() {
            return Collections.unmodifiableList(new ArrayList<>(trilhaAuditoria));
        }

        private String gerarHashSha256(String texto) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] bytes = md.digest(texto.getBytes(StandardCharsets.UTF_8));
                return HexFormat.of().formatHex(bytes);
            } catch (NoSuchAlgorithmException e) {
                return Integer.toHexString(texto.hashCode());
            }
        }
    }

    public static class SubservicoNotificacaoDigital {
        private final List<String> mensagensEnviadas = new ArrayList<>();

        public synchronized void emitirNotificacao(String destinatario, String titulo, String resumo) {
            String envio = String.format("NOTIFICAÇÃO DISPARADA -> Para: %s | Título: '%s' | Detalhe: %s",
                    destinatario, titulo, resumo);
            mensagensEnviadas.add(envio);
        }

        public synchronized List<String> getMensagensEnviadas() {
            return Collections.unmodifiableList(new ArrayList<>(mensagensEnviadas));
        }
    }

    // =========================================================================
    // 3. O PADRÃO GOF FACADE (Fachada Unificada)
    // =========================================================================

    public interface IFacadeGestaoDesktop {
        ComprovanteAlocacao efetivarAlocacaoCompleta(SolicitacaoAlocacao solicitacao) throws FalhaAlocacaoException;
        void cancelarAlocacaoComEstorno(String codigoRecurso, String matriculaUsuario, String motivo) throws FalhaAlocacaoException;
        List<String> obterTrilhaAuditoria();
        List<SubservicoUsuario.Usuario> obterUsuariosCadastrados();
        List<SubservicoEstoque.ItemEstoque> obterRecursosCadastrados();
    }

    /**
     * Fachada central do subsistema de alocação de recursos corporativos/acadêmicos.
     * Os controladores JavaFX conversam exclusivamente com esta classe.
     */
    public static class GestaoAlocacaoFacade implements IFacadeGestaoDesktop {

        private final SubservicoUsuario servicoUsuario;
        private final SubservicoEstoque servicoEstoque;
        private final SubservicoCalculoPrazo servicoPrazo;
        private final SubservicoAuditoriaCriptografica servicoAuditoria;
        private final SubservicoNotificacaoDigital servicoNotificacao;

        public GestaoAlocacaoFacade(
                SubservicoUsuario servicoUsuario,
                SubservicoEstoque servicoEstoque,
                SubservicoCalculoPrazo servicoPrazo,
                SubservicoAuditoriaCriptografica servicoAuditoria,
                SubservicoNotificacaoDigital servicoNotificacao
        ) {
            this.servicoUsuario = Objects.requireNonNull(servicoUsuario);
            this.servicoEstoque = Objects.requireNonNull(servicoEstoque);
            this.servicoPrazo = Objects.requireNonNull(servicoPrazo);
            this.servicoAuditoria = Objects.requireNonNull(servicoAuditoria);
            this.servicoNotificacao = Objects.requireNonNull(servicoNotificacao);
        }

        public GestaoAlocacaoFacade() {
            this(
                    new SubservicoUsuario(),
                    new SubservicoEstoque(),
                    new SubservicoCalculoPrazo(),
                    new SubservicoAuditoriaCriptografica(),
                    new SubservicoNotificacaoDigital()
            );
        }

        @Override
        public ComprovanteAlocacao efetivarAlocacaoCompleta(SolicitacaoAlocacao solicitacao) throws FalhaAlocacaoException {
            Objects.requireNonNull(solicitacao, "Solicitação não pode ser nula.");

            // 1. Orquestração da etapa de identificação de usuário
            SubservicoUsuario.Usuario usuario = servicoUsuario.validarEObterUsuario(solicitacao.codigoUsuario());

            // 2. Orquestração da etapa de reserva de inventário
            SubservicoEstoque.ItemEstoque item = servicoEstoque.alocarRecurso(solicitacao.codigoRecurso());

            try {
                // 3. Orquestração da política temporal
                LocalDate dataInicio = LocalDate.now();
                LocalDate dataDevolucao = servicoPrazo.calcularDataPrevistaDevolucao(dataInicio, solicitacao.diasSolicitados());

                // 4. Orquestração de auditoria com hash criptográfico
                String transacaoId = "TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                String detalhesAuditoria = String.format("Alocação %s do item %s para %s por %d dias",
                        transacaoId, item.getCodigo(), usuario.matricula(), solicitacao.diasSolicitados());
                String hash = servicoAuditoria.registrarEvento("ALOCACAO_RECURSO", detalhesAuditoria);

                // 5. Orquestração de notificação ao usuário
                servicoNotificacao.emitirNotificacao(
                        usuario.matricula(),
                        "Alocação Confirmada - " + item.getDescricao(),
                        "Seu pedido " + transacaoId + " foi emitido até " + dataDevolucao.format(FORMATO_DATA)
                );

                // 6. Retorna o comprovante final consolidado
                return new ComprovanteAlocacao(
                        transacaoId,
                        usuario.matricula(),
                        usuario.nome(),
                        item.getCodigo(),
                        item.getDescricao(),
                        dataInicio,
                        dataDevolucao,
                        hash,
                        LocalDateTime.now()
                );
            } catch (Exception e) {
                // Em caso de falha nas etapas pós-reserva, efetua compensação/rollback
                servicoEstoque.estornarAlocacao(solicitacao.codigoRecurso());
                servicoAuditoria.registrarEvento("ROLLBACK_ALOCACAO", "Falha pós-reserva do item " + solicitacao.codigoRecurso());
                throw new FalhaAlocacaoException("FALHA_ORQUESTRACAO", "Erro ao concluir transação: " + e.getMessage());
            }
        }

        @Override
        public void cancelarAlocacaoComEstorno(String codigoRecurso, String matriculaUsuario, String motivo) throws FalhaAlocacaoException {
            servicoEstoque.estornarAlocacao(codigoRecurso);
            servicoAuditoria.registrarEvento("CANCELAMENTO", String.format("Item %s estornado por %s. Motivo: %s",
                    codigoRecurso, matriculaUsuario, motivo));
            servicoNotificacao.emitirNotificacao(matriculaUsuario, "Cancelamento de Alocação", "Item " + codigoRecurso + " foi liberado.");
        }

        @Override
        public List<String> obterTrilhaAuditoria() {
            return servicoAuditoria.getHistorico();
        }

        @Override
        public List<SubservicoUsuario.Usuario> obterUsuariosCadastrados() {
            return servicoUsuario.listarTodos();
        }

        @Override
        public List<SubservicoEstoque.ItemEstoque> obterRecursosCadastrados() {
            return servicoEstoque.listarTodos();
        }

        public SubservicoNotificacaoDigital getServicoNotificacao() {
            return servicoNotificacao;
        }
    }

    // =========================================================================
    // 4. INTERFACE GRÁFICA JAVAFX INTEGRADA
    // =========================================================================

    private final IFacadeGestaoDesktop facade = new GestaoAlocacaoFacade();

    private ComboBox<SubservicoUsuario.Usuario> cbUsuarios;
    private ComboBox<SubservicoEstoque.ItemEstoque> cbRecursos;
    private Spinner<Integer> spDias;
    private TextField txtFinalidade;
    private TextArea txtReciboEmitido;
    private ListView<String> lvAuditoria;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Demonstração GoF Facade — Fachada de Subsistemas Desktop");

        BorderPane raiz = new BorderPane();
        raiz.setPadding(new Insets(15));

        // Cabeçalho didático
        VBox boxHeader = new VBox(6);
        Label lblTitulo = new Label("Padrão GoF Facade: Gateway de Subsistemas");
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a365d;");
        Label lblDescricao = new Label("O controlador JavaFX dialoga com um único ponto de entrada (Facade),\n" +
                "o qual orquestra Validação de Usuário, Inventário, Prazos, Auditoria SHA-256 e Notificações.");
        lblDescricao.setStyle("-fx-text-fill: #4a5568;");
        boxHeader.getChildren().addAll(lblTitulo, lblDescricao, new Separator());
        raiz.setTop(boxHeader);

        // Centro: Formulário e Recibo
        GridPane gridForm = new GridPane();
        gridForm.setHgap(10);
        gridForm.setVgap(10);
        gridForm.setPadding(new Insets(10, 0, 10, 0));

        gridForm.add(new Label("Usuário Solicitante:"), 0, 0);
        cbUsuarios = new ComboBox<>();
        cbUsuarios.getItems().addAll(facade.obterUsuariosCadastrados());
        cbUsuarios.getSelectionModel().selectFirst();
        cbUsuarios.setMaxWidth(Double.MAX_VALUE);
        gridForm.add(cbUsuarios, 1, 0);

        gridForm.add(new Label("Equipamento / Recurso:"), 0, 1);
        cbRecursos = new ComboBox<>();
        cbRecursos.getItems().addAll(facade.obterRecursosCadastrados());
        cbRecursos.getSelectionModel().selectFirst();
        cbRecursos.setMaxWidth(Double.MAX_VALUE);
        gridForm.add(cbRecursos, 1, 1);

        gridForm.add(new Label("Prazo Solicitado (Dias):"), 0, 2);
        spDias = new Spinner<>(1, 30, 7);
        spDias.setEditable(true);
        gridForm.add(spDias, 1, 2);

        gridForm.add(new Label("Finalidade / Projeto:"), 0, 3);
        txtFinalidade = new TextField("Projeto de Pesquisa em Robótica");
        gridForm.add(txtFinalidade, 1, 3);

        Button btnExecutarFacade = new Button("Efetivar Transação via Facade");
        btnExecutarFacade.setStyle("-fx-background-color: #2b6cb0; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");
        btnExecutarFacade.setOnAction(e -> handleEfetivarOperacao());

        Button btnEstornar = new Button("Estornar Recurso Selecionado");
        btnEstornar.setStyle("-fx-background-color: #718096; -fx-text-fill: white; -fx-padding: 8 16;");
        btnEstornar.setOnAction(e -> handleEstornar());

        HBox boxBotoes = new HBox(10, btnExecutarFacade, btnEstornar);
        gridForm.add(boxBotoes, 1, 4);

        txtReciboEmitido = new TextArea();
        txtReciboEmitido.setEditable(false);
        txtReciboEmitido.setPromptText("O comprovante consolidado emitido pela Fachada aparecerá aqui...");
        txtReciboEmitido.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 11px;");
        txtReciboEmitido.setPrefRowCount(10);

        VBox boxEsquerda = new VBox(10, gridForm, new Label("Comprovante Consolidado:"), txtReciboEmitido);
        VBox.setVgrow(txtReciboEmitido, Priority.ALWAYS);

        // Lado Direito: Trilha de Auditoria em Tempo Real
        VBox boxDireita = new VBox(6);
        Label lblAudit = new Label("Logs de Auditoria Interna (Subserviços):");
        lblAudit.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748;");
        lvAuditoria = new ListView<>();
        lvAuditoria.setPrefWidth(380);
        boxDireita.getChildren().addAll(lblAudit, lvAuditoria);
        VBox.setVgrow(lvAuditoria, Priority.ALWAYS);

        HBox centro = new HBox(15, boxEsquerda, boxDireita);
        HBox.setHgrow(boxEsquerda, Priority.ALWAYS);
        raiz.setCenter(centro);

        atualizarAuditoria();

        Scene scene = new Scene(raiz, 950, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleEfetivarOperacao() {
        SubservicoUsuario.Usuario usuario = cbUsuarios.getValue();
        SubservicoEstoque.ItemEstoque item = cbRecursos.getValue();

        if (usuario == null || item == null) {
            exibirAlerta(Alert.AlertType.WARNING, "Seleção Incompleta", "Selecione o usuário e o equipamento.");
            return;
        }

        try {
            SolicitacaoAlocacao solicitacao = new SolicitacaoAlocacao(
                    usuario.matricula(),
                    item.getCodigo(),
                    spDias.getValue(),
                    txtFinalidade.getText()
            );

            // A MÁGICA DO FACADE: Uma única chamada orquestra todos os subsistemas!
            ComprovanteAlocacao comprovante = facade.efetivarAlocacaoCompleta(solicitacao);

            txtReciboEmitido.setText(comprovante.formatarRecibo());
            atualizarAuditoria();
            recarregarCombos();

            exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso via Fachada",
                    "Transação " + comprovante.idTransacao() + " efetivada com sucesso em todos os subsistemas!");
        } catch (FalhaAlocacaoException e) {
            txtReciboEmitido.setText("FALHA NA TRANSAÇÃO:\nEtapa: " + e.getCodigoEtapa() + "\nMotivo: " + e.getMessage());
            atualizarAuditoria();
            exibirAlerta(Alert.AlertType.ERROR, "Transação Rejeitada", "[" + e.getCodigoEtapa() + "] " + e.getMessage());
        } catch (Exception e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro Inesperado", e.getMessage());
        }
    }

    private void handleEstornar() {
        SubservicoEstoque.ItemEstoque item = cbRecursos.getValue();
        SubservicoUsuario.Usuario usuario = cbUsuarios.getValue();
        if (item == null || usuario == null) return;

        try {
            facade.cancelarAlocacaoComEstorno(item.getCodigo(), usuario.matricula(), "Liberação manual via interface");
            txtReciboEmitido.setText("Recurso " + item.getCodigo() + " estornado e liberado no estoque.");
            atualizarAuditoria();
            recarregarCombos();
            exibirAlerta(Alert.AlertType.INFORMATION, "Estorno Realizado", "Item devolvido com sucesso ao estoque.");
        } catch (FalhaAlocacaoException e) {
            exibirAlerta(Alert.AlertType.ERROR, "Falha no Estorno", e.getMessage());
        }
    }

    private void atualizarAuditoria() {
        lvAuditoria.getItems().clear();
        lvAuditoria.getItems().addAll(facade.obterTrilhaAuditoria());
    }

    private void recarregarCombos() {
        SubservicoEstoque.ItemEstoque selecionado = cbRecursos.getValue();
        cbRecursos.getItems().clear();
        cbRecursos.getItems().addAll(facade.obterRecursosCadastrados());
        if (selecionado != null) {
            for (SubservicoEstoque.ItemEstoque i : cbRecursos.getItems()) {
                if (i.getCodigo().equals(selecionado.getCodigo())) {
                    cbRecursos.setValue(i);
                    break;
                }
            }
        }
    }

    private void exibirAlerta(Alert.AlertType tipo, String titulo, String corpo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(corpo);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
