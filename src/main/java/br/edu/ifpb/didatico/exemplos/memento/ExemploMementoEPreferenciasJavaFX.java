package br.edu.ifpb.didatico.exemplos.memento;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Exemplo didático do Padrão GoF Memento aplicado à persistência de estado e sessão de UI no JavaFX.
 * Permite capturar as preferências da janela (dimensões, posição, tema, último diretório) em um
 * snapshot imutável (Memento) e restaurá-las entre execuções usando Preferences ou Properties.
 */
public class ExemploMementoEPreferenciasJavaFX {

    /**
     * O MEMENTO (Snapshot Imutável do estado da interface).
     * Modelado como um Record do Java 21, garantindo imutabilidade profunda.
     */
    public record EstadoJanelaMemento(
            double x,
            double y,
            double largura,
            double altura,
            boolean maximizado,
            String temaAtivo,
            String ultimoDiretorio
    ) {
        public static EstadoJanelaMemento padrao() {
            return new EstadoJanelaMemento(
                    100.0,
                    100.0,
                    1024.0,
                    768.0,
                    false,
                    "CLARO",
                    System.getProperty("user.home", ".")
            );
        }
    }

    /**
     * O ORIGINATOR (Gerador e Consumidor do Memento).
     * Mantém o estado ativo da janela durante a execução da aplicação JavaFX.
     */
    public static class OriginatorJanela {
        private double x;
        private double y;
        private double largura;
        private double altura;
        private boolean maximizado;
        private String temaAtivo;
        private String ultimoDiretorio;

        public OriginatorJanela() {
            restaurar(EstadoJanelaMemento.padrao());
        }

        public void atualizarEstado(double x, double y, double largura, double altura,
                                    boolean maximizado, String temaAtivo, String ultimoDiretorio) {
            this.x = x;
            this.y = y;
            this.largura = largura;
            this.altura = altura;
            this.maximizado = maximizado;
            this.temaAtivo = (temaAtivo != null) ? temaAtivo : "CLARO";
            this.ultimoDiretorio = (ultimoDiretorio != null) ? ultimoDiretorio : System.getProperty("user.home", ".");
        }

        /**
         * Cria um novo Memento com o snapshot do estado atual (para persistência no close).
         */
        public EstadoJanelaMemento criarMemento() {
            return new EstadoJanelaMemento(x, y, largura, altura, maximizado, temaAtivo, ultimoDiretorio);
        }

        /**
         * Restaura o estado a partir de um Memento fornecido pelo Caretaker.
         */
        public void restaurar(EstadoJanelaMemento memento) {
            if (memento == null) {
                memento = EstadoJanelaMemento.padrao();
            }
            this.x = memento.x();
            this.y = memento.y();
            this.largura = memento.largura();
            this.altura = memento.altura();
            this.maximizado = memento.maximizado();
            this.temaAtivo = memento.temaAtivo();
            this.ultimoDiretorio = memento.ultimoDiretorio();
        }

        public double getX() { return x; }
        public double getY() { return y; }
        public double getLargura() { return largura; }
        public double getAltura() { return altura; }
        public boolean isMaximizado() { return maximizado; }
        public String getTemaAtivo() { return temaAtivo; }
        public String getUltimoDiretorio() { return ultimoDiretorio; }
    }

    /**
     * O CARETAKER (Guardião do Memento).
     * Responsável por serializar e deserializar os snapshots de estado
     * via java.util.prefs.Preferences ou arquivos .properties em disco.
     */
    public static class CaretakerPreferencias {

        private static final String CHAVE_X = "janela.x";
        private static final String CHAVE_Y = "janela.y";
        private static final String CHAVE_LARGURA = "janela.largura";
        private static final String CHAVE_ALTURA = "janela.altura";
        private static final String CHAVE_MAXIMIZADO = "janela.maximizado";
        private static final String CHAVE_TEMA = "janela.tema";
        private static final String CHAVE_DIR = "janela.ultimoDiretorio";

        /**
         * Salva o Memento em Preferences do sistema operacional.
         */
        public static void salvarEmPreferences(Preferences prefs, EstadoJanelaMemento memento) {
            Objects.requireNonNull(prefs, "Preferences não pode ser nulo.");
            if (memento == null) return;

            // Salva coordenadas apenas se a janela não estiver minimizada e dimensões válidas
            if (!memento.maximizado() && memento.largura() >= 300 && memento.altura() >= 200) {
                prefs.putDouble(CHAVE_X, memento.x());
                prefs.putDouble(CHAVE_Y, memento.y());
                prefs.putDouble(CHAVE_LARGURA, memento.largura());
                prefs.putDouble(CHAVE_ALTURA, memento.altura());
            }

            prefs.putBoolean(CHAVE_MAXIMIZADO, memento.maximizado());
            prefs.put(CHAVE_TEMA, memento.temaAtivo());
            prefs.put(CHAVE_DIR, memento.ultimoDiretorio());

            try {
                prefs.flush();
            } catch (BackingStoreException e) {
                System.err.println("Aviso: Falha ao persistir preferências do Java: " + e.getMessage());
            }
        }

        /**
         * Carrega o Memento a partir de Preferences do sistema operacional.
         */
        public static EstadoJanelaMemento carregarDePreferences(Preferences prefs) {
            Objects.requireNonNull(prefs, "Preferences não pode ser nulo.");
            EstadoJanelaMemento padrao = EstadoJanelaMemento.padrao();

            double x = prefs.getDouble(CHAVE_X, padrao.x());
            double y = prefs.getDouble(CHAVE_Y, padrao.y());
            double largura = prefs.getDouble(CHAVE_LARGURA, padrao.largura());
            double altura = prefs.getDouble(CHAVE_ALTURA, padrao.altura());
            boolean maximizado = prefs.getBoolean(CHAVE_MAXIMIZADO, padrao.maximizado());
            String tema = prefs.get(CHAVE_TEMA, padrao.temaAtivo());
            String dir = prefs.get(CHAVE_DIR, padrao.ultimoDiretorio());

            return new EstadoJanelaMemento(x, y, largura, altura, maximizado, tema, dir);
        }

        /**
         * Persiste o snapshot em arquivo .properties (ideal para testes ou portabilidade sem registro).
         */
        public static void salvarEmArquivoProperties(Path arquivo, EstadoJanelaMemento memento) throws IOException {
            Objects.requireNonNull(arquivo, "Caminho do arquivo não pode ser nulo.");
            if (memento == null) return;

            Properties props = new Properties();
            props.setProperty(CHAVE_X, String.valueOf(memento.x()));
            props.setProperty(CHAVE_Y, String.valueOf(memento.y()));
            props.setProperty(CHAVE_LARGURA, String.valueOf(memento.largura()));
            props.setProperty(CHAVE_ALTURA, String.valueOf(memento.altura()));
            props.setProperty(CHAVE_MAXIMIZADO, String.valueOf(memento.maximizado()));
            props.setProperty(CHAVE_TEMA, memento.temaAtivo());
            props.setProperty(CHAVE_DIR, memento.ultimoDiretorio());

            try (OutputStream out = Files.newOutputStream(arquivo)) {
                props.store(out, "Preferências de Sessão JavaFX");
            }
        }

        /**
         * Carrega o snapshot a partir de arquivo .properties.
         */
        public static EstadoJanelaMemento carregarDeArquivoProperties(Path arquivo) throws IOException {
            if (!Files.exists(arquivo)) {
                return EstadoJanelaMemento.padrao();
            }

            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(arquivo)) {
                props.load(in);
            }

            EstadoJanelaMemento padrao = EstadoJanelaMemento.padrao();
            double x = Double.parseDouble(props.getProperty(CHAVE_X, String.valueOf(padrao.x())));
            double y = Double.parseDouble(props.getProperty(CHAVE_Y, String.valueOf(padrao.y())));
            double largura = Double.parseDouble(props.getProperty(CHAVE_LARGURA, String.valueOf(padrao.largura())));
            double altura = Double.parseDouble(props.getProperty(CHAVE_ALTURA, String.valueOf(padrao.altura())));
            boolean maximizado = Boolean.parseBoolean(props.getProperty(CHAVE_MAXIMIZADO, String.valueOf(padrao.maximizado())));
            String tema = props.getProperty(CHAVE_TEMA, padrao.temaAtivo());
            String dir = props.getProperty(CHAVE_DIR, padrao.ultimoDiretorio());

            return new EstadoJanelaMemento(x, y, largura, altura, maximizado, tema, dir);
        }
    }
}
