package br.edu.ifpb.didatico.exemplos.javafx;

import javafx.application.Application;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Demonstração Didática do Guia 23:
 * Exportação de Recibos Formatados Monoespaçados, CSV e Integração com FileChooser Nativo do JavaFX.
 * Zero Leaks de domínio: utiliza modelo didático genérico de movimentação de ativos patrimoniais.
 */
public class ExemploExportacaoRelatoriosEFileChooserJavaFX extends Application {

    // 1. DTO de Domínio Didático Genérico (Java 21 Record)
    public record ItemMovimentado(String codigo, String descricao, BigDecimal valorDiaria) {
        public ItemMovimentado {
            Objects.requireNonNull(codigo, "Código não pode ser nulo");
            Objects.requireNonNull(descricao, "Descrição não pode ser nula");
            Objects.requireNonNull(valorDiaria, "Valor da diária não pode ser nulo");
        }
    }

    // 2. Formatador de Recibos e Comprovantes Monoespaçados
    public static final class GeradorReciboFormatado {

        private static final String LINHA_DIVISORIA = "+---------------+--------------------------------+--------------+";
        private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        public static String gerarReciboMovimentacao(
                String idTransacao,
                String operador,
                String destinatario,
                List<ItemMovimentado> itens,
                BigDecimal valorTaxasMultas,
                LocalDateTime dataHora
        ) {
            StringBuilder sb = new StringBuilder();

            // Cabeçalho Institucional
            sb.append("=================================================================\n");
            sb.append("                 SISTEMA DE CONTROLE PATRIMONIAL                 \n");
            sb.append("                  COMPROVANTE OFICIAL DE OPERAÇÃO                \n");
            sb.append("=================================================================\n\n");

            // Metadados da Transação
            sb.append(String.format("Identificador Único : %s\n", idTransacao));
            sb.append(String.format("Data e Horário      : %s\n", (dataHora != null ? dataHora : LocalDateTime.now()).format(FORMATO_DATA_HORA)));
            sb.append(String.format("Operador Responsável: %s\n", operador));
            sb.append(String.format("Destinatário/Usuário: %s\n", destinatario));
            sb.append("\n");

            // Tabela Discriminada de Itens em Largura Fixa
            sb.append(LINHA_DIVISORIA).append("\n");
            sb.append(String.format("| %-13s | %-30s | %12s |\n", "CÓDIGO", "DISCRIMINAÇÃO DO ITEM", "VALOR DIÁRIA"));
            sb.append(LINHA_DIVISORIA).append("\n");

            BigDecimal somaDiarias = BigDecimal.ZERO;
            if (itens != null) {
                for (ItemMovimentado item : itens) {
                    sb.append(String.format(Locale.forLanguageTag("pt-BR"), "| %-13s | %-30s | R$ %9.2f |\n",
                            item.codigo(),
                            abreviar(item.descricao(), 30),
                            item.valorDiaria().doubleValue()));
                    somaDiarias = somaDiarias.add(item.valorDiaria());
                }
            }
            sb.append(LINHA_DIVISORIA).append("\n\n");

            // Totais
            BigDecimal taxas = (valorTaxasMultas != null) ? valorTaxasMultas : BigDecimal.ZERO;
            BigDecimal totalGeral = somaDiarias.add(taxas);

            sb.append(String.format(Locale.forLanguageTag("pt-BR"), "Quantidade de Itens : %d\n", (itens != null ? itens.size() : 0)));
            sb.append(String.format(Locale.forLanguageTag("pt-BR"), "Subtotal Diárias    : R$ %10.2f\n", somaDiarias.doubleValue()));
            sb.append(String.format(Locale.forLanguageTag("pt-BR"), "Taxas / Penalidades : R$ %10.2f\n", taxas.doubleValue()));
            sb.append(String.format(Locale.forLanguageTag("pt-BR"), "TOTAL CONSOLIDADO   : R$ %10.2f\n", totalGeral.doubleValue()));
            sb.append("\n");

            // Chave Criptográfica SHA-256 de Autenticidade Digital
            String hash = gerarHashAutenticidade(idTransacao, destinatario, totalGeral);
            sb.append("-----------------------------------------------------------------\n");
            sb.append(String.format("Chave de Autenticação: %s\n", hash));
            sb.append("Comprovante eletrônico gerado pelo sistema. Dispensa carimbo.\n");
            sb.append("=================================================================\n");

            return sb.toString();
        }

        public static String gerarHashAutenticidade(String idTransacao, String destinatario, BigDecimal total) {
            try {
                String payload = (idTransacao != null ? idTransacao : "") + "|" +
                        (destinatario != null ? destinatario : "") + "|" +
                        (total != null ? total.toPlainString() : "0.00");
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hashBytes = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
                return HexFormat.of().formatHex(hashBytes).substring(0, 24).toUpperCase();
            } catch (NoSuchAlgorithmException e) {
                return "HASH-INDISPONIVEL-SHA256";
            }
        }

        private static String abreviar(String texto, int tamanhoMaximo) {
            if (texto == null) return "";
            if (texto.length() <= tamanhoMaximo) return texto;
            return texto.substring(0, tamanhoMaximo - 3) + "...";
        }
    }

    // 3. Exportador de Relatório Tabular CSV
    public static final class ExportadorCsvFormatado {
        public static String gerarCsv(List<ItemMovimentado> itens) {
            StringBuilder sb = new StringBuilder();
            sb.append("# Relatório de Itens Movimentados em Formato CSV\n");
            sb.append("# Codigo;Descricao;ValorDiaria\n");
            if (itens != null) {
                for (ItemMovimentado item : itens) {
                    sb.append(item.codigo()).append(";")
                            .append(item.descricao().replace(";", ",")).append(";")
                            .append(item.valorDiaria().toPlainString()).append("\n");
                }
            }
            return sb.toString();
        }
    }

    // 4. Componentes da Apresentação Gráfica JavaFX
    private final ObservableList<ItemMovimentado> itens = FXCollections.observableArrayList();
    private final TextArea txtPreviaRecibo = new TextArea();
    private final TextField txtIdTransacao = new TextField("TX-2026-9842");
    private final TextField txtOperador = new TextField("Prof. Gabriel (Coordenação)");
    private final TextField txtDestinatario = new TextField("Aluno João da Silva");
    private final TextField txtTaxas = new TextField("15.00");

    private final Locale localeBr = new Locale.Builder().setLanguage("pt").setRegion("BR").build();
    private final NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(localeBr);

    @Override
    public void start(Stage palcoPrincipal) {
        palcoPrincipal.setTitle("Demonstração Didática — Recibos Formatados, CSV e FileChooser");

        popularItens();

        // 1. Painel Superior: Metadados da Transação
        HBox painelMetadados = criarPainelMetadados();

        // 2. Painel Central Dividido: Tabela de Itens (Esquerda) e Visualização Prévia do Recibo (Direita)
        HBox painelCentral = criarPainelCentral();

        // 3. Barra Inferior de Ações e Exportação com FileChooser
        HBox barraExportacao = criarBarraExportacao(palcoPrincipal);

        BorderPane layout = new BorderPane();
        layout.setTop(painelMetadados);
        layout.setCenter(painelCentral);
        layout.setBottom(barraExportacao);

        Scene cena = new Scene(layout, 1020, 600);
        palcoPrincipal.setScene(cena);
        palcoPrincipal.show();

        // Gera a prévia inicial do recibo
        atualizarPreviaRecibo();
    }

    private HBox criarPainelMetadados() {
        HBox painel = new HBox(12);
        painel.setPadding(new Insets(10, 14, 10, 14));
        painel.setAlignment(Pos.CENTER_LEFT);
        painel.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");

        painel.getChildren().addAll(
                new Label("Transação:"), txtIdTransacao,
                new Label("Operador:"), txtOperador,
                new Label("Destinatário:"), txtDestinatario,
                new Label("Taxas R$:"), txtTaxas
        );

        txtTaxas.setPrefWidth(80);
        return painel;
    }

    private HBox criarPainelCentral() {
        HBox painel = new HBox(12);
        painel.setPadding(new Insets(12));

        // Lado Esquerdo: Tabela de Itens
        TableView<ItemMovimentado> tabela = new TableView<>(itens);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        HBox.setHgrow(tabela, Priority.ALWAYS);

        TableColumn<ItemMovimentado, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().codigo()));
        colCodigo.setMaxWidth(100);

        TableColumn<ItemMovimentado, String> colDescricao = new TableColumn<>("Descrição");
        colDescricao.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().descricao()));

        TableColumn<ItemMovimentado, BigDecimal> colValor = new TableColumn<>("Diária");
        colValor.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().valorDiaria()));
        colValor.setMaxWidth(120);
        colValor.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean vazio) {
                super.updateItem(item, vazio);
                if (vazio || item == null) {
                    setText(null);
                } else {
                    setText(formatoMoeda.format(item));
                    setAlignment(Pos.CENTER_RIGHT);
                }
            }
        });

        tabela.getColumns().addAll(colCodigo, colDescricao, colValor);

        // Lado Direito: Prévia do Recibo em Fonte Monoespaçada
        VBox painelPrevia = new VBox(6);
        painelPrevia.setPrefWidth(480);
        HBox.setHgrow(painelPrevia, Priority.ALWAYS);

        Label lblTituloPrevia = new Label("Visualização Prévia do Recibo Oficial (Monoespaçado)");
        lblTituloPrevia.setStyle("-fx-font-weight: bold; -fx-text-fill: #343a40;");

        txtPreviaRecibo.setEditable(false);
        txtPreviaRecibo.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 11px;");
        VBox.setVgrow(txtPreviaRecibo, Priority.ALWAYS);

        painelPrevia.getChildren().addAll(lblTituloPrevia, txtPreviaRecibo);

        painel.getChildren().addAll(tabela, painelPrevia);
        return painel;
    }

    private HBox criarBarraExportacao(Stage palco) {
        HBox barra = new HBox(12);
        barra.setPadding(new Insets(10, 14, 10, 14));
        barra.setAlignment(Pos.CENTER_RIGHT);
        barra.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1 0 0 0;");

        Button btnAtualizarPrevia = new Button("↻ Atualizar Prévia");
        btnAtualizarPrevia.setOnAction(e -> atualizarPreviaRecibo());

        Button btnExportarTxt = new Button("💾 Salvar Recibo (.txt)...");
        btnExportarTxt.setStyle("-fx-background-color: #0d6efd; -fx-text-fill: white; -fx-font-weight: bold;");
        btnExportarTxt.setOnAction(e -> {
            atualizarPreviaRecibo();
            String nomeArquivoSugerido = "recibo-" + txtIdTransacao.getText().trim().toLowerCase() + ".txt";
            exportarViaFileChooser(palco, txtPreviaRecibo.getText(), nomeArquivoSugerido, "Documento de Texto (*.txt)", "*.txt");
        });

        Button btnExportarCsv = new Button("📊 Exportar Tabela (.csv)...");
        btnExportarCsv.setStyle("-fx-background-color: #198754; -fx-text-fill: white; -fx-font-weight: bold;");
        btnExportarCsv.setOnAction(e -> {
            String csv = ExportadorCsvFormatado.gerarCsv(itens);
            String nomeArquivoSugerido = "itens-" + txtIdTransacao.getText().trim().toLowerCase() + ".csv";
            exportarViaFileChooser(palco, csv, nomeArquivoSugerido, "Planilha CSV (*.csv)", "*.csv");
        });

        barra.getChildren().addAll(btnAtualizarPrevia, btnExportarCsv, btnExportarTxt);
        return barra;
    }

    private void atualizarPreviaRecibo() {
        BigDecimal taxa = BigDecimal.ZERO;
        try {
            taxa = new BigDecimal(txtTaxas.getText().trim().replace(",", "."));
        } catch (Exception ignored) {}

        String recibo = GeradorReciboFormatado.gerarReciboMovimentacao(
                txtIdTransacao.getText().trim(),
                txtOperador.getText().trim(),
                txtDestinatario.getText().trim(),
                itens,
                taxa,
                LocalDateTime.now()
        );
        txtPreviaRecibo.setText(recibo);
    }

    private void exportarViaFileChooser(Stage janelaPai, String conteudo, String nomeSugerido, String descricaoFiltro, String extensao) {
        FileChooser seletor = new FileChooser();
        seletor.setTitle("Exportar Documento");
        seletor.getExtensionFilters().add(new FileChooser.ExtensionFilter(descricaoFiltro, extensao));
        seletor.setInitialFileName(nomeSugerido);

        File userHome = new File(System.getProperty("user.home"));
        if (userHome.exists()) {
            seletor.setInitialDirectory(userHome);
        }

        File arquivoDestino = seletor.showSaveDialog(janelaPai);
        if (arquivoDestino == null) {
            return; // Usuário cancelou graciosamente
        }

        try {
            Files.writeString(arquivoDestino.toPath(), conteudo, StandardCharsets.UTF_8);

            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Exportação Concluída");
            alerta.setHeaderText("Documento salvo com sucesso!");
            alerta.setContentText("Local do arquivo:\n" + arquivoDestino.getAbsolutePath());
            alerta.showAndWait();

        } catch (IOException ex) {
            Alert alertaErro = new Alert(Alert.AlertType.ERROR);
            alertaErro.setTitle("Erro de I/O");
            alertaErro.setHeaderText("Não foi possível gravar o arquivo.");
            alertaErro.setContentText(ex.getMessage());
            alertaErro.showAndWait();
        }
    }

    private void popularItens() {
        itens.addAll(
                new ItemMovimentado("PAT-101", "Notebook Corporativo Dell XPS 15", new BigDecimal("45.00")),
                new ItemMovimentado("PAT-102", "Kit Adaptadores Vídeo HDMI/USB-C", new BigDecimal("10.00")),
                new ItemMovimentado("PAT-103", "Projetor Portátil Laser 3000L", new BigDecimal("35.00"))
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
