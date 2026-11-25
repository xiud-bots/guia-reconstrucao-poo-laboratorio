package br.edu.ifpb.didatico.exemplos.javafx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;

/**
 * Demonstração Didática do Guia 20:
 * Manipulação de Datas Modernas (java.time.LocalDate), DatePicker Formatado (dd/MM/yyyy),
 * Restrições de Calendário via DayCellFactory e Cálculo Temporal Reativo de Prazos e Multas.
 */
public class ExemploDataPickerERegrasTemporaisJavaFX extends Application {

    // =========================================================================
    // 1. CONVERSOR SEGURO DE DATAS PARA FORMATO BRASILEIRO (dd/MM/yyyy)
    // =========================================================================

    public static final class DataBrConverter extends StringConverter<LocalDate> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        @Override
        public String toString(LocalDate data) {
            return (data == null) ? "" : formatter.format(data);
        }

        @Override
        public LocalDate fromString(String texto) {
            if (texto == null || texto.trim().isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(texto.trim(), formatter);
            } catch (DateTimeParseException ex) {
                // Em caso de digitação inconsistente, previne NumberFormatException retornando null
                return null;
            }
        }
    }

    // =========================================================================
    // 2. UTILITÁRIO DIDÁTICO DE REGRAS TEMPORAIS PURAS
    // =========================================================================

    public static final class RegrasTemporais {
        private RegrasTemporais() {}

        /**
         * Calcula a quantidade de dias corridos em atraso.
         * Retorna 0 caso a data de entrega seja igual ou anterior à data prevista.
         */
        public static long calcularDiasAtraso(LocalDate dataPrevista, LocalDate dataDevolucao) {
            Objects.requireNonNull(dataPrevista, "Data prevista não pode ser nula");
            Objects.requireNonNull(dataDevolucao, "Data de devolução não pode ser nula");

            if (!dataDevolucao.isAfter(dataPrevista)) {
                return 0;
            }
            return ChronoUnit.DAYS.between(dataPrevista, dataDevolucao);
        }

        /**
         * Calcula o valor da multa com base em dias de atraso e carência regulamentar.
         */
        public static BigDecimal calcularMulta(long diasAtraso, BigDecimal valorDiaria, int diasCarencia) {
            if (diasAtraso <= diasCarencia) {
                return BigDecimal.ZERO;
            }
            long diasCobrados = diasAtraso - diasCarencia;
            return valorDiaria.multiply(BigDecimal.valueOf(diasCobrados));
        }

        /**
         * Adiciona dias úteis (desconsiderando sábados e domingos).
         */
        public static LocalDate adicionarDiasUteis(LocalDate dataBase, int diasUteis) {
            LocalDate cursor = dataBase;
            int adicionados = 0;
            while (adicionados < diasUteis) {
                cursor = cursor.plusDays(1);
                if (cursor.getDayOfWeek() != DayOfWeek.SATURDAY && cursor.getDayOfWeek() != DayOfWeek.SUNDAY) {
                    adicionados++;
                }
            }
            return cursor;
        }
    }

    // =========================================================================
    // 3. INTERFACE GRÁFICA JAVAFX
    // =========================================================================

    private final Locale localeBr = new Locale.Builder().setLanguage("pt").setRegion("BR").build();
    private final NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(localeBr);
    private final BigDecimal VALOR_MULTA_DIARIA = new BigDecimal("5.00");

    @Override
    public void start(Stage palco) {
        palco.setTitle("Demonstração Didática — DatePicker e Regras Temporais (java.time)");

        // 1. Componentes de Entrada
        DatePicker dpInicio = new DatePicker(LocalDate.now());
        dpInicio.setConverter(new DataBrConverter());
        dpInicio.setPromptText("dd/mm/aaaa");

        Spinner<Integer> spPrazoDias = new Spinner<>(1, 60, 7);
        spPrazoDias.setEditable(true);
        spPrazoDias.setPrefWidth(90);

        Label lblDataPrevista = new Label();
        lblDataPrevista.setStyle("-fx-font-weight: bold; -fx-text-fill: #0d6efd;");

        DatePicker dpDevolucao = new DatePicker(LocalDate.now().plusDays(10));
        dpDevolucao.setConverter(new DataBrConverter());
        dpDevolucao.setPromptText("dd/mm/aaaa");

        // 2. Customização do Calendário via DayCellFactory (Bloqueia datas anteriores ao início)
        dpDevolucao.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate inicio = dpInicio.getValue();
                if (inicio != null && date.isBefore(inicio)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #f1f3f5; -fx-text-fill: #adb5bd;");
                }
                if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    setStyle("-fx-text-fill: #d63384;");
                }
            }
        });

        // 3. Painel de Feedback e Cálculo em Tempo Real
        Label lblStatus = new Label();
        Label lblDiasAtraso = new Label();
        Label lblValorMulta = new Label();

        VBox painelCalculo = new VBox(10,
                new Label("Diagnóstico Temporal em Tempo Real:"),
                lblStatus,
                lblDiasAtraso,
                lblValorMulta
        );
        painelCalculo.setPadding(new Insets(14));
        painelCalculo.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-border-color: #dee2e6; -fx-border-radius: 8;");

        // 4. Lógica Reativa de Recálculo
        Runnable recalcular = () -> {
            LocalDate inicio = dpInicio.getValue();
            Integer dias = spPrazoDias.getValue();

            if (inicio == null || dias == null) {
                lblDataPrevista.setText("Aguardando data inicial...");
                return;
            }

            LocalDate prevista = inicio.plusDays(dias);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            lblDataPrevista.setText(prevista.format(fmt) + " (" + prevista.getDayOfWeek().name() + ")");

            LocalDate devolucao = dpDevolucao.getValue();
            if (devolucao == null) {
                lblStatus.setText("Selecione a data efetiva de devolução.");
                lblStatus.setStyle("-fx-text-fill: #6c757d;");
                lblDiasAtraso.setText("");
                lblValorMulta.setText("");
                return;
            }

            long atraso = RegrasTemporais.calcularDiasAtraso(prevista, devolucao);
            BigDecimal multa = RegrasTemporais.calcularMulta(atraso, VALOR_MULTA_DIARIA, 0);

            if (atraso == 0) {
                lblStatus.setText("SITUAÇÃO: DEVOLUÇÃO DENTRO DO PRAZO REGULAMENTAR");
                lblStatus.setStyle("-fx-text-fill: #198754; -fx-font-weight: bold;");
                lblDiasAtraso.setText("Dias de Atraso: 0 dias (Sem penalidade).");
                lblValorMulta.setText("Multa a Cobrar: R$ 0,00");
            } else {
                lblStatus.setText(String.format("SITUAÇÃO: ATRASO IDENTIFICADO (%d DIAS)", atraso));
                lblStatus.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                lblDiasAtraso.setText(String.format("Data Prevista: %s | Devolvido em: %s",
                        prevista.format(fmt), devolucao.format(fmt)));
                lblValorMulta.setText(String.format("Multa Projetada (R$ 5,00/dia): %s", formatoMoeda.format(multa)));
            }
        };

        // Listeners para atualização reativa instantânea
        dpInicio.valueProperty().addListener((obs, ant, n) -> recalcular.run());
        spPrazoDias.valueProperty().addListener((obs, ant, n) -> recalcular.run());
        dpDevolucao.valueProperty().addListener((obs, ant, n) -> recalcular.run());

        // Dispara cálculo inicial
        recalcular.run();

        // 5. Montagem do Formulário
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(16));

        form.add(new Label("1. Data de Início da Reserva:"), 0, 0);
        form.add(dpInicio, 1, 0);

        form.add(new Label("2. Prazo Concedido (Dias):"), 0, 1);
        form.add(spPrazoDias, 1, 1);

        form.add(new Label("3. Data Prevista de Devolução:"), 0, 2);
        form.add(lblDataPrevista, 1, 2);

        form.add(new Label("4. Data Efetiva de Devolução:"), 0, 3);
        form.add(dpDevolucao, 1, 3);

        VBox raiz = new VBox(16,
                new Label("Simulação de Prazos e Multas Regulamentares com java.time"),
                form,
                painelCalculo
        );
        raiz.setPadding(new Insets(20));

        palco.setScene(new Scene(raiz, 600, 420));
        palco.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
