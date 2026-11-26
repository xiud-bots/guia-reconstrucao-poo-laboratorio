package br.edu.ifpb.didatico.exemplos.chain;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Exemplo didático do Padrão GoF Chain of Responsibility no Java 21 e JavaFX.
 * Demonstra o desacoplamento de múltiplos critérios de validação e aprovação de negócio,
 * onde cada elo da cadeia possui responsabilidade única (SRP) e pode ser composto de forma flexível (OCP).
 */
public class ExemploChainOfResponsibilityValidacao extends Application {

    // =========================================================================
    // 1. MODELO DE DADOS DA REQUISIÇÃO E RESULTADOS
    // =========================================================================

    public enum PerfilSolicitante {
        ESTUDANTE(3, new BigDecimal("3000.00")),
        PESQUISADOR(7, new BigDecimal("15000.00")),
        PROFESSOR(10, new BigDecimal("30000.00"));

        private final int cotaMaximaItens;
        private final BigDecimal tetoPatrimonial;

        PerfilSolicitante(int cotaMaximaItens, BigDecimal tetoPatrimonial) {
            this.cotaMaximaItens = cotaMaximaItens;
            this.tetoPatrimonial = tetoPatrimonial;
        }

        public int getCotaMaximaItens() {
            return cotaMaximaItens;
        }

        public BigDecimal getTetoPatrimonial() {
            return tetoPatrimonial;
        }
    }

    /**
     * DTO imutável representando a requisição de operação a ser auditada pela cadeia.
     */
    public record RequisicaoOperacao(
            String matricula,
            PerfilSolicitante perfil,
            int diasAtrasoAtual,
            int itensEmAberto,
            int itensSolicitados,
            BigDecimal valorPatrimonialSolicitado,
            boolean contemEquipamentoRestrito
    ) {
        public RequisicaoOperacao {
            Objects.requireNonNull(matricula, "Matrícula não pode ser nula");
            Objects.requireNonNull(perfil, "Perfil não pode ser nulo");
            Objects.requireNonNull(valorPatrimonialSolicitado, "Valor patrimonial não pode ser nulo");
        }
    }

    /**
     * Registro imutável de uma regra violada detectada por um elo da cadeia.
     */
    public record ViolacaoRegra(
            String codigoRegra,
            String eloResponsavel,
            String mensagemErro,
            boolean impeditiva
    ) {}

    /**
     * Exceção checada disparada em operações de modo Fail-Fast.
     */
    public static class RegraNegocioVioladaException extends Exception {
        private final String codigoRegra;

        public RegraNegocioVioladaException(String codigoRegra, String mensagem) {
            super(mensagem);
            this.codigoRegra = codigoRegra;
        }

        public String getCodigoRegra() {
            return codigoRegra;
        }
    }

    // =========================================================================
    // 2. CONTRATO BASE DO MANIPULADOR (Handler)
    // =========================================================================

    /**
     * Manipulador abstrato da cadeia de responsabilidade.
     */
    public static abstract class ValidadorRequisicao {
        private ValidadorRequisicao proximo;

        /**
         * Encadeia o próximo validador retornando a referência para permitir fluent chain.
         */
        public ValidadorRequisicao setProximo(ValidadorRequisicao proximo) {
            this.proximo = proximo;
            return proximo;
        }

        public ValidadorRequisicao getProximo() {
            return proximo;
        }

        /**
         * Modo Coleta Completa: Audita e adiciona todas as violações encontradas na lista acumuladora.
         */
        public void auditar(RequisicaoOperacao req, List<ViolacaoRegra> violacoes) {
            executarVerificacao(req, violacoes);
            if (proximo != null) {
                proximo.auditar(req, violacoes);
            }
        }

        /**
         * Modo Fail-Fast: Lança exceção de domínio checada no PRIMEIRO elo violado.
         */
        public void validarFailFast(RequisicaoOperacao req) throws RegraNegocioVioladaException {
            List<ViolacaoRegra> locais = new ArrayList<>();
            executarVerificacao(req, locais);
            if (!locais.isEmpty()) {
                ViolacaoRegra primeira = locais.get(0);
                throw new RegraNegocioVioladaException(primeira.codigoRegra(), primeira.mensagemErro());
            }
            if (proximo != null) {
                proximo.validarFailFast(req);
            }
        }

        /**
         * Método abstrato a ser implementado por cada elo concreto da cadeia.
         */
        protected abstract void executarVerificacao(RequisicaoOperacao req, List<ViolacaoRegra> violacoes);
    }

    // =========================================================================
    // 3. ELOS CONCRETOS DA CADEIA (Concrete Handlers)
    // =========================================================================

    /**
     * Elo 1: Verifica inadimplência e atrasos do usuário.
     */
    public static final class ValidadorSuspensaoEAtraso extends ValidadorRequisicao {
        @Override
        protected void executarVerificacao(RequisicaoOperacao req, List<ViolacaoRegra> violacoes) {
            if (req.diasAtrasoAtual() > 0) {
                violacoes.add(new ViolacaoRegra(
                        "SUSP-01",
                        "ValidadorSuspensaoEAtraso",
                        String.format("Usuário possui pendência ativa com %d dia(s) de atraso.", req.diasAtrasoAtual()),
                        true
                ));
            }
        }
    }

    /**
     * Elo 2: Verifica limites de cota quantitativa por perfil de solicitante.
     */
    public static final class ValidadorCotaQuantidade extends ValidadorRequisicao {
        @Override
        protected void executarVerificacao(RequisicaoOperacao req, List<ViolacaoRegra> violacoes) {
            int totalResultante = req.itensEmAberto() + req.itensSolicitados();
            int cota = req.perfil().getCotaMaximaItens();

            if (totalResultante > cota) {
                violacoes.add(new ViolacaoRegra(
                        "COTA-02",
                        "ValidadorCotaQuantidade",
                        String.format("Cota excedida: o perfil %s permite no máximo %d itens (solicitado: %d, ativos: %d).",
                                req.perfil(), cota, req.itensSolicitados(), req.itensEmAberto()),
                        true
                ));
            }
        }
    }

    /**
     * Elo 3: Verifica teto de valor financeiro sob custódia patrimonial.
     */
    public static final class ValidadorTetoFinanceiro extends ValidadorRequisicao {
        @Override
        protected void executarVerificacao(RequisicaoOperacao req, List<ViolacaoRegra> violacoes) {
            BigDecimal teto = req.perfil().getTetoPatrimonial();
            if (req.valorPatrimonialSolicitado().compareTo(teto) > 0) {
                violacoes.add(new ViolacaoRegra(
                        "FIN-03",
                        "ValidadorTetoFinanceiro",
                        String.format("Teto financeiro excedido: valor solicitado R$ %s é maior que o limite de R$ %s para %s.",
                                req.valorPatrimonialSolicitado(), teto, req.perfil()),
                        true
                ));
            }
        }
    }

    /**
     * Elo 4: Verifica autorização para manuseio de equipamentos laboratoriais restritos.
     */
    public static final class ValidadorEquipamentoRestrito extends ValidadorRequisicao {
        @Override
        protected void executarVerificacao(RequisicaoOperacao req, List<ViolacaoRegra> violacoes) {
            if (req.contemEquipamentoRestrito() && req.perfil() == PerfilSolicitante.ESTUDANTE) {
                violacoes.add(new ViolacaoRegra(
                        "PERM-04",
                        "ValidadorEquipamentoRestrito",
                        "Equipamentos de alta precisão/restritos só podem ser requisitados por Docentes ou Pesquisadores.",
                        true
                ));
            }
        }
    }

    // =========================================================================
    // 4. FACTORY DO PIPELINE DE AUDITORIA
    // =========================================================================

    public static ValidadorRequisicao criarCadeiaPadrao() {
        ValidadorRequisicao elo1 = new ValidadorSuspensaoEAtraso();
        ValidadorRequisicao elo2 = new ValidadorCotaQuantidade();
        ValidadorRequisicao elo3 = new ValidadorTetoFinanceiro();
        ValidadorRequisicao elo4 = new ValidadorEquipamentoRestrito();

        elo1.setProximo(elo2).setProximo(elo3).setProximo(elo4);
        return elo1;
    }

    // =========================================================================
    // 5. INTERFACE GRÁFICA JAVAFX COM CHECKLIST DE AUDITORIA
    // =========================================================================

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Demonstração GoF Chain of Responsibility - XIUD Didático");

        Label lblTitulo = new Label("Pipeline de Auditoria de Requisições (Chain of Responsibility)");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c5282;");

        // Formulário de simulação
        TextField txtMatricula = new TextField("20261001-A");
        ComboBox<PerfilSolicitante> cbPerfil = new ComboBox<>();
        cbPerfil.getItems().addAll(PerfilSolicitante.values());
        cbPerfil.setValue(PerfilSolicitante.ESTUDANTE);

        TextField txtAtraso = new TextField("0");
        TextField txtItensAbertos = new TextField("2");
        TextField txtItensSolicitados = new TextField("2");
        TextField txtValorSolicitado = new TextField("3500.00");
        ComboBox<Boolean> cbRestrito = new ComboBox<>();
        cbRestrito.getItems().addAll(false, true);
        cbRestrito.setValue(false);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.addRow(0, new Label("Matrícula:"), txtMatricula);
        grid.addRow(1, new Label("Perfil Solicitante:"), cbPerfil);
        grid.addRow(2, new Label("Dias de Atraso Atual:"), txtAtraso);
        grid.addRow(3, new Label("Itens Já em Aberto:"), txtItensAbertos);
        grid.addRow(4, new Label("Itens Solicitados:"), txtItensSolicitados);
        grid.addRow(5, new Label("Valor Total Solicitado (R$):"), txtValorSolicitado);
        grid.addRow(6, new Label("Contém Equipamento Restrito:"), cbRestrito);

        Button btnExecutar = new Button("Auditar Requisição na Cadeia");
        btnExecutar.setStyle("-fx-background-color: #3182ce; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 16px;");

        VBox painelChecklist = new VBox(8);
        painelChecklist.setStyle("-fx-background-color: #f7fafc; -fx-padding: 14px; -fx-border-color: #e2e8f0; -fx-border-radius: 6px;");

        Label lblResultado = new Label("Clique em Auditar para iniciar a verificação da cadeia.");
        lblResultado.setStyle("-fx-font-weight: bold;");

        btnExecutar.setOnAction(e -> {
            try {
                RequisicaoOperacao req = new RequisicaoOperacao(
                        txtMatricula.getText(),
                        cbPerfil.getValue(),
                        Integer.parseInt(txtAtraso.getText().trim()),
                        Integer.parseInt(txtItensAbertos.getText().trim()),
                        Integer.parseInt(txtItensSolicitados.getText().trim()),
                        new BigDecimal(txtValorSolicitado.getText().trim()),
                        cbRestrito.getValue()
                );

                ValidadorRequisicao cadeia = criarCadeiaPadrao();
                List<ViolacaoRegra> violacoes = new ArrayList<>();
                cadeia.auditar(req, violacoes);

                painelChecklist.getChildren().clear();
                if (violacoes.isEmpty()) {
                    lblResultado.setText("✅ REQUISIÇÃO APROVADA: Todos os elos da cadeia foram satisfeitos com sucesso!");
                    lblResultado.setStyle("-fx-text-fill: #22543d; -fx-font-weight: bold;");
                    Label itemOk = new Label("• Nenhum impedimento detectado nos validadores regulamentares.");
                    itemOk.setStyle("-fx-text-fill: #276749;");
                    painelChecklist.getChildren().addAll(lblResultado, itemOk);
                } else {
                    lblResultado.setText(String.format("❌ REQUISIÇÃO REJEITADA (%d violação(ões) detectada(s)):", violacoes.size()));
                    lblResultado.setStyle("-fx-text-fill: #9b2c2c; -fx-font-weight: bold;");
                    painelChecklist.getChildren().add(lblResultado);

                    for (ViolacaoRegra v : violacoes) {
                        Label lblV = new Label(String.format("⛔ [%s] %s: %s", v.codigoRegra(), v.eloResponsavel(), v.mensagemErro()));
                        lblV.setStyle("-fx-text-fill: #c53030; -fx-font-size: 13px;");
                        painelChecklist.getChildren().add(lblV);
                    }
                }
            } catch (Exception ex) {
                lblResultado.setText("Erro ao ler formulário: " + ex.getMessage());
                lblResultado.setStyle("-fx-text-fill: #e53e3e;");
            }
        });

        VBox layout = new VBox(14, lblTitulo, grid, btnExecutar, new Separator(), painelChecklist);
        layout.setPadding(new Insets(16));

        primaryStage.setScene(new Scene(new BorderPane(layout), 680, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
