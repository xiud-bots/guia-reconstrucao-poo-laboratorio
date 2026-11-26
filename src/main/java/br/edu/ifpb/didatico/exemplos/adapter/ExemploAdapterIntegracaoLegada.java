package br.edu.ifpb.didatico.exemplos.adapter;

import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Exemplo didático do Padrão GoF Adapter (Object Adapter) integrado ao JavaFX.
 * Demonstra como integrar fontes de dados incompatíveis ou sistemas de parceiros legados
 * ao modelo de domínio e à interface gráfica sem acoplamento nem vazamento de abstração.
 */
public class ExemploAdapterIntegracaoLegada extends Application {

    private static final Locale LOCALE_BR = Locale.of("pt", "BR");
    private static final NumberFormat MOEDA_FORMAT = NumberFormat.getCurrencyInstance(LOCALE_BR);

    // =========================================================================
    // 1. CONTRATO DO SISTEMA ALVO (Target Interface - Clean Domain)
    // =========================================================================

    /**
     * Interface canônica esperada pelo sistema interno e pelos componentes da UI JavaFX.
     */
    public interface IItemCatalogoLocal {
        String getCodigoIdentificador();
        String getNomeExibicao();
        BigDecimal getPrecoEstimado();
        boolean isDisponivel();
        String getOrigemRegistro();
    }

    // =========================================================================
    // 2. SISTEMA LEGADO OU DTO EXTERNO INCOMPATÍVEL (Adaptee)
    // =========================================================================

    /**
     * DTO de formato externo/legado com tipos primitivos arcaicos e convenções diferentes:
     * - ID numérico sem prefixo padronizado
     * - Descrição bruta em texto sujo
     * - Preço em centavos inteiros (ex: 29990 centavos para R$ 299,90)
     * - Status com flags numéricas (1=Ativo/Disponível, 2=Reservado, 9=Desativado)
     */
    public record LegacyVendorItemDTO(
            long vendorItemId,
            String rawItemName,
            long priceInCents,
            int legacyStatusCode
    ) {
        public LegacyVendorItemDTO {
            if (vendorItemId <= 0) {
                throw new IllegalArgumentException("ID legado deve ser positivo.");
            }
            Objects.requireNonNull(rawItemName, "Nome bruto não pode ser nulo.");
        }
    }

    // =========================================================================
    // 3. ADAPTADOR CONCRETO (Adapter)
    // =========================================================================

    /**
     * Adaptador de Objeto (GoF Adapter).
     * Implementa o contrato interno {@link IItemCatalogoLocal} e encapsula
     * a instância arcaica {@link LegacyVendorItemDTO}.
     */
    public static final class AdaptadorItemLegado implements IItemCatalogoLocal {
        private final LegacyVendorItemDTO legDTO;

        public AdaptadorItemLegado(LegacyVendorItemDTO legDTO) {
            this.legDTO = Objects.requireNonNull(legDTO, "DTO legado não pode ser nulo.");
        }

        @Override
        public String getCodigoIdentificador() {
            return String.format("LEG-%06d", legDTO.vendorItemId());
        }

        @Override
        public String getNomeExibicao() {
            return legDTO.rawItemName().trim();
        }

        @Override
        public BigDecimal getPrecoEstimado() {
            return BigDecimal.valueOf(legDTO.priceInCents())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN);
        }

        @Override
        public boolean isDisponivel() {
            // Flag 1 = Ativo e liberado
            return legDTO.legacyStatusCode() == 1;
        }

        @Override
        public String getOrigemRegistro() {
            return "Parceiro Externo Legacy (API v1)";
        }

        public LegacyVendorItemDTO getAdaptee() {
            return legDTO;
        }

        @Override
        public String toString() {
            return String.format("%s: %s (%s)", getCodigoIdentificador(), getNomeExibicao(), MOEDA_FORMAT.format(getPrecoEstimado()));
        }
    }

    // =========================================================================
    // 4. PARSER E SERVIÇO DE IMPORTAÇÃO ADAPTADA
    // =========================================================================

    public static class ImportadorFeedLegado {
        /**
         * Realiza o parse de registros no formato legado separado por pipe ("|"):
         * vendorId|nome|precoCentavos|statusCode
         */
        public static List<IItemCatalogoLocal> importarLinhas(List<String> linhas) {
            List<IItemCatalogoLocal> adaptados = new ArrayList<>();
            for (String linha : linhas) {
                if (linha == null || linha.isBlank() || linha.startsWith("#")) {
                    continue;
                }
                String[] tokens = linha.split("\\|");
                if (tokens.length >= 4) {
                    try {
                        long id = Long.parseLong(tokens[0].trim());
                        String nome = tokens[1].trim();
                        long centavos = Long.parseLong(tokens[2].trim());
                        int status = Integer.parseInt(tokens[3].trim());

                        LegacyVendorItemDTO dto = new LegacyVendorItemDTO(id, nome, centavos, status);
                        adaptados.add(new AdaptadorItemLegado(dto));
                    } catch (NumberFormatException ignored) {
                        // Linha mal formatada é ignorada defensivamente
                    }
                }
            }
            return Collections.unmodifiableList(adaptados);
        }
    }

    // =========================================================================
    // 5. INTERFACE GRÁFICA JAVAFX
    // =========================================================================

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Demonstração GoF Adapter & Interoperabilidade - XIUD Didático");

        Label lblTitulo = new Label("Catálogo Unificado com Dados Adaptados de Sistema Legado");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a365d;");

        Label lblSubtitulo = new Label("A TableView consome exclusivamente a interface canônica 'IItemCatalogoLocal'. "
                + "A estrutura 'LegacyVendorItemDTO' permanece totalmente oculta.");
        lblSubtitulo.setWrapText(true);
        lblSubtitulo.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 12px;");

        // Configura a Tabela JavaFX desacoplada
        TableView<IItemCatalogoLocal> tabela = new TableView<>();

        TableColumn<IItemCatalogoLocal, String> colCodigo = new TableColumn<>("Código Adaptado");
        colCodigo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCodigoIdentificador()));
        colCodigo.setPrefWidth(120);

        TableColumn<IItemCatalogoLocal, String> colNome = new TableColumn<>("Nome do Item");
        colNome.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeExibicao()));
        colNome.setPrefWidth(220);

        TableColumn<IItemCatalogoLocal, String> colPreco = new TableColumn<>("Preço Estimado");
        colPreco.setCellValueFactory(d -> new SimpleStringProperty(MOEDA_FORMAT.format(d.getValue().getPrecoEstimado())));
        colPreco.setPrefWidth(130);

        TableColumn<IItemCatalogoLocal, String> colDisponivel = new TableColumn<>("Disponível");
        colDisponivel.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isDisponivel() ? "✅ Sim" : "❌ Indisponível"));
        colDisponivel.setPrefWidth(110);

        TableColumn<IItemCatalogoLocal, String> colOrigem = new TableColumn<>("Origem dos Dados");
        colOrigem.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOrigemRegistro()));
        colOrigem.setPrefWidth(180);

        tabela.getColumns().addAll(colCodigo, colNome, colPreco, colDisponivel, colOrigem);

        // Gera dados legados brutos para adaptação
        List<String> linhasBrutasLegadas = List.of(
                "101|Multímetro Fluke True-RMS|145000|1",
                "102|Estação de Solda Hikari Digital|42990|1",
                "103|Gerador de Sinais Arbitrários 25MHz|389000|2",
                "104|Cabo Coaxial BNC 50 Ohms Antigo|1550|9",
                "105|Fonte de Alimentação DC Minipa 32V|119900|1"
        );

        List<IItemCatalogoLocal> itensAdaptados = ImportadorFeedLegado.importarLinhas(linhasBrutasLegadas);
        tabela.getItems().addAll(itensAdaptados);

        VBox layout = new VBox(12, lblTitulo, lblSubtitulo, tabela);
        layout.setPadding(new Insets(16));

        primaryStage.setScene(new Scene(new BorderPane(layout), 800, 480));
        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
