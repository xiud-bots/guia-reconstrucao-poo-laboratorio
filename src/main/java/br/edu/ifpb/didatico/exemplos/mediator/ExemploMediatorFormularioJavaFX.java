package br.edu.ifpb.didatico.exemplos.mediator;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Objects;

/**
 * Exemplo didático do Padrão GoF Mediator (Mediador) integrado a um formulário JavaFX.
 * Demonstra como desacoplar múltiplos componentes visuais interdependentes
 * (ComboBox, CheckBox, TextField, Button, Labels de Validação), eliminando
 * conexões caóticas N:N e centralizando as regras de orquestração de UI em um único mediador.
 */
public class ExemploMediatorFormularioJavaFX extends Application {

    // =========================================================================
    // 1. ENUMS E EVENTOS DE COMUNICAÇÃO
    // =========================================================================

    public enum TipoSolicitante {
        ESTUDANTE("Estudante Graduação / Técnico"),
        PROFESSOR("Professor Efetivo / Pesquisador"),
        PESQUISADOR_VISITANTE("Pesquisador Visitante / Pós-Doc");

        private final String rotulo;

        TipoSolicitante(String rotulo) {
            this.rotulo = rotulo;
        }

        @Override
        public String toString() {
            return rotulo;
        }
    }

    public enum OpcaoPrazo {
        DIAS_3("3 Dias Úteis (Curto Prazo)", 3),
        DIAS_7("7 Dias Corridos (Padrão)", 7),
        DIAS_15("15 Dias Corridos (Estendido)", 15),
        DIAS_30("30 Dias Corridos (Ciclo Completo)", 30);

        private final String descricao;
        private final int dias;

        OpcaoPrazo(String descricao, int dias) {
            this.descricao = descricao;
            this.dias = dias;
        }

        public int getDias() { return dias; }

        @Override
        public String toString() {
            return descricao;
        }
    }

    public enum EventoFormulario {
        TIPO_SOLICITANTE_ALTERADO,
        USO_ESPECIALIZADO_ALTERADO,
        TEXTO_VINCULO_ALTERADO,
        PRAZO_ALTERADO,
        TERMO_RESPONSABILIDADE_ALTERADO,
        RESETAR_FORMULARIO
    }

    // =========================================================================
    // 2. CONTRATO DO MEDIATOR (Mediator Interface) E DECISÃO REATIVA
    // =========================================================================

    public record DecisaoMediator(
            boolean valido,
            String mensagemFeedback,
            boolean exigeVinculo,
            String rotuloVinculo,
            boolean avisoOrientadorVisivel,
            List<OpcaoPrazo> prazosPermitidos
    ) {}

    public interface IFormularioMediator {
        void registrarControles(
                ComboBox<TipoSolicitante> cbTipo,
                CheckBox chkEspecializado,
                TextField txtVinculo,
                Label lblRotuloVinculo,
                Label lblAvisoOrientador,
                ComboBox<OpcaoPrazo> cbPrazo,
                CheckBox chkTermo,
                Button btnSubmeter,
                Label lblFeedbackStatus
        );

        void notificar(Object componenteOrigem, EventoFormulario evento);

        boolean isFormularioValido();
    }

    // =========================================================================
    // 3. MEDIATOR CONCRETO (Concrete Mediator)
    // =========================================================================

    public static class FormularioAlocacaoMediator implements IFormularioMediator {

        private ComboBox<TipoSolicitante> cbTipo;
        private CheckBox chkEspecializado;
        private TextField txtVinculo;
        private Label lblRotuloVinculo;
        private Label lblAvisoOrientador;
        private ComboBox<OpcaoPrazo> cbPrazo;
        private CheckBox chkTermo;
        private Button btnSubmeter;
        private Label lblFeedbackStatus;

        private boolean atualizandoInternamente = false;

        @Override
        public void registrarControles(
                ComboBox<TipoSolicitante> cbTipo,
                CheckBox chkEspecializado,
                TextField txtVinculo,
                Label lblRotuloVinculo,
                Label lblAvisoOrientador,
                ComboBox<OpcaoPrazo> cbPrazo,
                CheckBox chkTermo,
                Button btnSubmeter,
                Label lblFeedbackStatus
        ) {
            this.cbTipo = Objects.requireNonNull(cbTipo);
            this.chkEspecializado = Objects.requireNonNull(chkEspecializado);
            this.txtVinculo = Objects.requireNonNull(txtVinculo);
            this.lblRotuloVinculo = Objects.requireNonNull(lblRotuloVinculo);
            this.lblAvisoOrientador = Objects.requireNonNull(lblAvisoOrientador);
            this.cbPrazo = Objects.requireNonNull(cbPrazo);
            this.chkTermo = Objects.requireNonNull(chkTermo);
            this.btnSubmeter = Objects.requireNonNull(btnSubmeter);
            this.lblFeedbackStatus = Objects.requireNonNull(lblFeedbackStatus);

            // Estado inicial consistente
            ajustarRegrasTipoSolicitante();
            reavaliarCondicoesGlobais();
        }

        @Override
        public void notificar(Object componenteOrigem, EventoFormulario evento) {
            if (atualizandoInternamente) return;

            switch (evento) {
                case TIPO_SOLICITANTE_ALTERADO -> {
                    ajustarRegrasTipoSolicitante();
                    reavaliarCondicoesGlobais();
                }
                case USO_ESPECIALIZADO_ALTERADO -> {
                    ajustarRegrasUsoEspecializado();
                    reavaliarCondicoesGlobais();
                }
                case TEXTO_VINCULO_ALTERADO, PRAZO_ALTERADO, TERMO_RESPONSABILIDADE_ALTERADO -> {
                    reavaliarCondicoesGlobais();
                }
                case RESETAR_FORMULARIO -> {
                    resetarTudo();
                }
            }
        }

        private void ajustarRegrasTipoSolicitante() {
            TipoSolicitante tipo = cbTipo.getValue();
            if (tipo == null) return;

            atualizandoInternamente = true;
            try {
                switch (tipo) {
                    case PROFESSOR -> {
                        lblRotuloVinculo.setText("Projeto / Laboratório (Opcional):");
                        txtVinculo.setDisable(false);
                        txtVinculo.setPromptText("Ex: Lab de Sistemas Embarcados");
                        lblAvisoOrientador.setVisible(false);
                        lblAvisoOrientador.setManaged(false);

                        // Professores têm acesso a todos os prazos
                        cbPrazo.setItems(FXCollections.observableArrayList(
                                OpcaoPrazo.DIAS_7, OpcaoPrazo.DIAS_15, OpcaoPrazo.DIAS_30
                        ));
                        if (!cbPrazo.getItems().contains(cbPrazo.getValue())) {
                            cbPrazo.setValue(OpcaoPrazo.DIAS_15);
                        }
                    }
                    case ESTUDANTE -> {
                        // Estudantes só podem prazos curtos
                        cbPrazo.setItems(FXCollections.observableArrayList(
                                OpcaoPrazo.DIAS_3, OpcaoPrazo.DIAS_7
                        ));
                        if (!cbPrazo.getItems().contains(cbPrazo.getValue())) {
                            cbPrazo.setValue(OpcaoPrazo.DIAS_7);
                        }

                        if (chkEspecializado.isSelected()) {
                            lblRotuloVinculo.setText("Matrícula do Orientador (Obrigatório)*:");
                            txtVinculo.setDisable(false);
                            txtVinculo.setPromptText("Matrícula do professor responsável");
                            lblAvisoOrientador.setText("⚠ Equipamentos especializados exigem autorização do orientador.");
                            lblAvisoOrientador.setVisible(true);
                            lblAvisoOrientador.setManaged(true);
                        } else {
                            lblRotuloVinculo.setText("Orientador de TCC/IC (Opcional):");
                            txtVinculo.setDisable(false);
                            txtVinculo.setPromptText("Nome ou matrícula do orientador");
                            lblAvisoOrientador.setVisible(false);
                            lblAvisoOrientador.setManaged(false);
                        }
                    }
                    case PESQUISADOR_VISITANTE -> {
                        lblRotuloVinculo.setText("Acordo de Cooperação / Convênio*:");
                        txtVinculo.setDisable(false);
                        txtVinculo.setPromptText("Número do convênio ou termo de estágio");
                        lblAvisoOrientador.setText("ℹ O convênio deve estar vigente junto à diretoria.");
                        lblAvisoOrientador.setVisible(true);
                        lblAvisoOrientador.setManaged(true);

                        cbPrazo.setItems(FXCollections.observableArrayList(
                                OpcaoPrazo.DIAS_7, OpcaoPrazo.DIAS_15
                        ));
                        if (!cbPrazo.getItems().contains(cbPrazo.getValue())) {
                            cbPrazo.setValue(OpcaoPrazo.DIAS_7);
                        }
                    }
                }
            } finally {
                atualizandoInternamente = false;
            }
        }

        private void ajustarRegrasUsoEspecializado() {
            TipoSolicitante tipo = cbTipo.getValue();
            if (tipo == TipoSolicitante.ESTUDANTE) {
                if (chkEspecializado.isSelected()) {
                    lblRotuloVinculo.setText("Matrícula do Orientador (Obrigatório)*:");
                    txtVinculo.setPromptText("Matrícula do professor responsável");
                    lblAvisoOrientador.setText("⚠ Alunos precisam de anuência do orientador para este equipamento.");
                    lblAvisoOrientador.setVisible(true);
                    lblAvisoOrientador.setManaged(true);
                } else {
                    lblRotuloVinculo.setText("Orientador de TCC/IC (Opcional):");
                    txtVinculo.setPromptText("Nome ou matrícula do orientador");
                    lblAvisoOrientador.setVisible(false);
                    lblAvisoOrientador.setManaged(false);
                }
            }
        }

        public static DecisaoMediator avaliar(
                TipoSolicitante tipo,
                boolean especializado,
                String vinculo,
                OpcaoPrazo prazo,
                boolean termoAceito
        ) {
            if (tipo == null) {
                return new DecisaoMediator(false, "Selecione o perfil do solicitante.", false, "Vínculo:", false, List.of());
            }

            List<OpcaoPrazo> prazos = switch (tipo) {
                case ESTUDANTE -> List.of(OpcaoPrazo.DIAS_3, OpcaoPrazo.DIAS_7);
                case PROFESSOR -> List.of(OpcaoPrazo.DIAS_7, OpcaoPrazo.DIAS_15, OpcaoPrazo.DIAS_30);
                case PESQUISADOR_VISITANTE -> List.of(OpcaoPrazo.DIAS_7, OpcaoPrazo.DIAS_15);
            };

            boolean exigeVinculo = false;
            String rotulo = "";
            boolean avisoOrientador = false;

            switch (tipo) {
                case PROFESSOR -> {
                    exigeVinculo = false;
                    rotulo = "Projeto / Laboratório (Opcional):";
                    avisoOrientador = false;
                }
                case ESTUDANTE -> {
                    if (especializado) {
                        exigeVinculo = true;
                        rotulo = "Matrícula do Orientador (Obrigatório)*:";
                        avisoOrientador = true;
                    } else {
                        exigeVinculo = false;
                        rotulo = "Orientador de TCC/IC (Opcional):";
                        avisoOrientador = false;
                    }
                }
                case PESQUISADOR_VISITANTE -> {
                    exigeVinculo = true;
                    rotulo = "Acordo de Cooperação / Convênio*:";
                    avisoOrientador = true;
                }
            }

            String vTrim = (vinculo == null) ? "" : vinculo.trim();

            if (prazo == null) {
                return new DecisaoMediator(false, "Selecione o prazo de alocação desejado.", exigeVinculo, rotulo, avisoOrientador, prazos);
            }

            if (!termoAceito) {
                return new DecisaoMediator(false, "É obrigatório concordar com o Termo de Responsabilidade.", exigeVinculo, rotulo, avisoOrientador, prazos);
            }

            if (exigeVinculo && vTrim.isBlank()) {
                String motivo = (tipo == TipoSolicitante.ESTUDANTE)
                        ? "Informe a matrícula do orientador para equipamento especializado."
                        : "Informe o número do Acordo de Cooperação ou Convênio.";
                return new DecisaoMediator(false, motivo, exigeVinculo, rotulo, avisoOrientador, prazos);
            }

            return new DecisaoMediator(true, "Formulário validado com sucesso! Pronto para submissão.", exigeVinculo, rotulo, avisoOrientador, prazos);
        }

        private void reavaliarCondicoesGlobais() {
            TipoSolicitante tipo = cbTipo.getValue();
            OpcaoPrazo prazo = cbPrazo.getValue();
            boolean termoAceito = chkTermo.isSelected();
            String vinculo = txtVinculo.getText();

            DecisaoMediator decisao = avaliar(tipo, chkEspecializado.isSelected(), vinculo, prazo, termoAceito);

            if (decisao.valido()) {
                habilitarSubmissao(decisao.mensagemFeedback());
            } else {
                desabilitarSubmissao(decisao.mensagemFeedback());
            }
        }

        private void habilitarSubmissao(String mensagem) {
            btnSubmeter.setDisable(false);
            btnSubmeter.setStyle("-fx-background-color: #2b6cb0; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");
            lblFeedbackStatus.setText("✔ " + mensagem);
            lblFeedbackStatus.setStyle("-fx-text-fill: #22543d; -fx-font-weight: bold;");
        }

        private void desabilitarSubmissao(String motivo) {
            btnSubmeter.setDisable(true);
            btnSubmeter.setStyle("-fx-background-color: #cbd5e0; -fx-text-fill: #718096; -fx-padding: 8 16;");
            lblFeedbackStatus.setText("✖ " + motivo);
            lblFeedbackStatus.setStyle("-fx-text-fill: #c53030; -fx-font-style: italic;");
        }

        private void resetarTudo() {
            atualizandoInternamente = true;
            try {
                cbTipo.setValue(TipoSolicitante.ESTUDANTE);
                chkEspecializado.setSelected(false);
                txtVinculo.clear();
                chkTermo.setSelected(false);
                ajustarRegrasTipoSolicitante();
                reavaliarCondicoesGlobais();
            } finally {
                atualizandoInternamente = false;
            }
        }

        @Override
        public boolean isFormularioValido() {
            return !btnSubmeter.isDisable();
        }
    }

    // =========================================================================
    // 4. INTERFACE GRÁFICA JAVAFX
    // =========================================================================

    private final IFormularioMediator mediator = new FormularioAlocacaoMediator();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Demonstração GoF Mediator — Formulário Inteligente JavaFX");

        BorderPane raiz = new BorderPane();
        raiz.setPadding(new Insets(20));

        // Topo explicativo
        VBox boxCabecalho = new VBox(6);
        Label lblTitulo = new Label("Padrão GoF Mediator: Orquestração de Formulários");
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a365d;");
        Label lblDescricao = new Label("Os nós visuais não se comunicam diretamente. Qualquer evento de usuário\n" +
                "é direcionado ao Mediator, que coordena visibilidade, opções de prazo e validação global.");
        lblDescricao.setStyle("-fx-text-fill: #4a5568;");
        boxCabecalho.getChildren().addAll(lblTitulo, lblDescricao, new Separator());
        raiz.setTop(boxCabecalho);

        // Formulário Central
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(15, 0, 15, 0));

        // 1. Tipo de Solicitante
        grid.add(new Label("Perfil do Solicitante:"), 0, 0);
        ComboBox<TipoSolicitante> cbTipo = new ComboBox<>();
        cbTipo.getItems().addAll(TipoSolicitante.values());
        cbTipo.setValue(TipoSolicitante.ESTUDANTE);
        cbTipo.setMaxWidth(Double.MAX_VALUE);
        grid.add(cbTipo, 1, 0);

        // 2. Equipamento Especializado Checkbox
        CheckBox chkEspecializado = new CheckBox("Equipamento de Alta Precisão / Calibração Especial");
        chkEspecializado.setStyle("-fx-font-weight: bold;");
        grid.add(chkEspecializado, 1, 1);

        // 3. Matrícula / Vínculo
        Label lblRotuloVinculo = new Label("Orientador de TCC/IC (Opcional):");
        TextField txtVinculo = new TextField();
        grid.add(lblRotuloVinculo, 0, 2);
        grid.add(txtVinculo, 1, 2);

        // 3.1 Aviso Orientador
        Label lblAvisoOrientador = new Label();
        lblAvisoOrientador.setStyle("-fx-text-fill: #dd6b20; -fx-font-size: 11px;");
        grid.add(lblAvisoOrientador, 1, 3);

        // 4. Prazo
        grid.add(new Label("Prazo Solicitado:"), 0, 4);
        ComboBox<OpcaoPrazo> cbPrazo = new ComboBox<>();
        cbPrazo.setMaxWidth(Double.MAX_VALUE);
        grid.add(cbPrazo, 1, 4);

        // 5. Termo de Responsabilidade
        CheckBox chkTermo = new CheckBox("Li e concordo com os termos de salvaguarda patrimonial e biossegurança.");
        grid.add(chkTermo, 1, 5);

        // 6. Feedback de Status
        Label lblFeedback = new Label();
        grid.add(new Label("Status do Formulário:"), 0, 6);
        grid.add(lblFeedback, 1, 6);

        // 7. Ações
        Button btnSubmeter = new Button("Confirmar Solicitação");
        Button btnLimpar = new Button("Limpar Formulário");
        btnLimpar.setStyle("-fx-padding: 8 16;");

        HBox boxAcoes = new HBox(12, btnSubmeter, btnLimpar);
        grid.add(boxAcoes, 1, 7);

        raiz.setCenter(grid);

        // REGISTRO NO MEDIATOR
        mediator.registrarControles(
                cbTipo,
                chkEspecializado,
                txtVinculo,
                lblRotuloVinculo,
                lblAvisoOrientador,
                cbPrazo,
                chkTermo,
                btnSubmeter,
                lblFeedback
        );

        // LIGAÇÃO DOS LISTENERS EXCLUSIVAMENTE COM O MEDIATOR (SEM NÓ FALANDO COM OUTRO NÓ)
        cbTipo.setOnAction(e -> mediator.notificar(cbTipo, EventoFormulario.TIPO_SOLICITANTE_ALTERADO));
        chkEspecializado.setOnAction(e -> mediator.notificar(chkEspecializado, EventoFormulario.USO_ESPECIALIZADO_ALTERADO));
        txtVinculo.textProperty().addListener((obs, oldV, newV) -> mediator.notificar(txtVinculo, EventoFormulario.TEXTO_VINCULO_ALTERADO));
        cbPrazo.setOnAction(e -> mediator.notificar(cbPrazo, EventoFormulario.PRAZO_ALTERADO));
        chkTermo.setOnAction(e -> mediator.notificar(chkTermo, EventoFormulario.TERMO_RESPONSABILIDADE_ALTERADO));
        btnLimpar.setOnAction(e -> mediator.notificar(btnLimpar, EventoFormulario.RESETAR_FORMULARIO));

        btnSubmeter.setOnAction(e -> {
            if (mediator.isFormularioValido()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Solicitação Registrada");
                alert.setHeaderText(null);
                alert.setContentText(String.format("Solicitação de alocação confirmada para %s!\nPrazo selecionado: %s",
                        cbTipo.getValue(), cbPrazo.getValue()));
                alert.showAndWait();
            }
        });

        Scene scene = new Scene(raiz, 750, 480);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
