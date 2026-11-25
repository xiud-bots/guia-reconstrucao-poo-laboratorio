package br.edu.ifpb.didatico.exemplos.moderno;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Exemplo Didático: Demonstração dos recursos modernos do Java 21 LTS:
 * 1. Records com Construtores Compactos e Imutabilidade Real (List.copyOf)
 * 2. Sealed Interfaces (Hierarquias Controladas em Compilação)
 * 3. Pattern Matching para instanceof
 * 4. Pattern Matching para Switch com Exaustividade e Guard Clauses (when)
 */
public class ExemploJavaModernoRecordESealed {

    /**
     * 1. RECORD MODERNO COM CONSTRUTOR COMPACTO:
     * - Garante imutabilidade e integridade sem código repetitivo.
     * - Valida invariantes antes da atribuição interna.
     */
    public record MovimentacaoEstoque(
            String codigoItem,
            int quantidade,
            BigDecimal valorUnitario,
            List<String> observacoes,
            LocalDateTime dataHora
    ) {
        // Construtor Compacto para validação e cópia defensiva
        public MovimentacaoEstoque {
            if (codigoItem == null || codigoItem.isBlank()) {
                throw new IllegalArgumentException("O código do item não pode ser nulo ou vazio.");
            }
            if (quantidade <= 0) {
                throw new IllegalArgumentException("A quantidade movimentada deve ser estritamente positiva.");
            }
            if (valorUnitario == null || valorUnitario.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("O valor unitário não pode ser nulo ou negativo.");
            }
            // Garante imutabilidade da lista de observações
            observacoes = (observacoes == null) ? List.of() : List.copyOf(observacoes);
            if (dataHora == null) {
                dataHora = LocalDateTime.now();
            }
        }

        // Construtor auxiliar sobrecarregado
        public MovimentacaoEstoque(String codigoItem, int quantidade, BigDecimal valorUnitario) {
            this(codigoItem, quantidade, valorUnitario, List.of(), LocalDateTime.now());
        }

        // Método de domínio calculado no Record
        public BigDecimal calcularValorTotal() {
            return valorUnitario.multiply(BigDecimal.valueOf(quantidade));
        }
    }

    /**
     * 2. HIERARQUIA SELADA (SEALED INTERFACE):
     * Apenas os tipos listados na cláusula 'permits' podem implementar esta interface.
     */
    public sealed interface ResultadoValidacao
            permits ResultadoValidacao.Aprovado,
                    ResultadoValidacao.ReprovadoRegra,
                    ResultadoValidacao.ErroInfraestrutura {

        record Aprovado(String protocolo, BigDecimal valorAprovado) implements ResultadoValidacao {}

        record ReprovadoRegra(String codigoRegra, String justificativa, boolean passivelRecurso) implements ResultadoValidacao {}

        record ErroInfraestrutura(String mensagemFalha, int codigoErroHttp) implements ResultadoValidacao {}
    }

    /**
     * 3. PATTERN MATCHING COM SWITCH EXAUSTIVO E CLÁUSULAS GUARDA (when):
     * Dispensa a cláusula 'default', pois o compilador conhece todos os subtipos permitidos.
     */
    public static String formatarResultado(ResultadoValidacao resultado) {
        return switch (resultado) {
            case ResultadoValidacao.Aprovado a ->
                String.format("[APROVADO] Protocolo %s | Valor: R$ %.2f", a.protocolo(), a.valorAprovado());

            case ResultadoValidacao.ReprovadoRegra r when r.passivelRecurso() ->
                String.format("[REPROVADO COM RECURSO] Regra %s: %s (Solicitar revisão)", r.codigoRegra(), r.justificativa());

            case ResultadoValidacao.ReprovadoRegra r ->
                String.format("[REPROVADO DEFINITIVO] Regra %s: %s", r.codigoRegra(), r.justificativa());

            case ResultadoValidacao.ErroInfraestrutura e ->
                String.format("[ERRO DE SISTEMA %d] %s", e.codigoErroHttp(), e.mensagemFalha());
        };
    }

    /**
     * 4. PATTERN MATCHING PARA INSTANCEOF:
     * Elimina o cast manual '((MovimentacaoEstoque) obj).codigoItem()'.
     */
    public static boolean ehMovimentacaoDeItem(Object objeto, String codigoDesejado) {
        if (objeto instanceof MovimentacaoEstoque mov && Objects.equals(mov.codigoItem(), codigoDesejado)) {
            return mov.quantidade() > 0;
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println("=== Demonstração Didática: Java 21 LTS Moderno ===");

        // Teste de Record com cálculo
        MovimentacaoEstoque mov = new MovimentacaoEstoque("NOTE-01", 3, BigDecimal.valueOf(3200.50));
        System.out.println("Record criado: " + mov);
        System.out.println("Valor Total: R$ " + mov.calcularValorTotal());

        // Teste de Sealed Types e Pattern Matching Switch
        ResultadoValidacao aprovado = new ResultadoValidacao.Aprovado("PROT-2026-X", BigDecimal.valueOf(9601.50));
        ResultadoValidacao reprovadoComRecurso = new ResultadoValidacao.ReprovadoRegra("LIMITE_CREDITO", "Usuário com pendência temporária", true);
        ResultadoValidacao reprovadoDefinitivo = new ResultadoValidacao.ReprovadoRegra("CADASTRO_BLOQUEADO", "Usuário desativado", false);
        ResultadoValidacao erroInfra = new ResultadoValidacao.ErroInfraestrutura("Conexão recusada ao repositório", 503);

        System.out.println(formatarResultado(aprovado));
        System.out.println(formatarResultado(reprovadoComRecurso));
        System.out.println(formatarResultado(reprovadoDefinitivo));
        System.out.println(formatarResultado(erroInfra));
    }
}
