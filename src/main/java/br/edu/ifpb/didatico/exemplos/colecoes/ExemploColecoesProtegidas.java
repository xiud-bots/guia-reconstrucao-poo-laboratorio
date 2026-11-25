package br.edu.ifpb.didatico.exemplos.colecoes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Exemplo Didático: Demonstração de Cópia Defensiva em Coleções,
 * proteção contra vazamento de referências e operações funcionais
 * com Stream API (busca por chave, filtro e somatório acumulado).
 */
public class ExemploColecoesProtegidas {

    public record ItemEstoque(String id, String nome, BigDecimal preco, boolean emFalta) {}

    public static class CatalogoSeguro {
        // Coleção interna mutável e privada
        private final List<ItemEstoque> estoque = new ArrayList<>();

        public void adicionar(ItemEstoque item) {
            if (item == null) throw new IllegalArgumentException("Item não pode ser nulo.");
            this.estoque.add(item);
        }

        // RETORNO SEGURO: Devolve uma visão imutável.
        // Tentativas externas de chamar add() ou clear() lançam UnsupportedOperationException.
        public List<ItemEstoque> listarTodos() {
            return Collections.unmodifiableList(this.estoque);
        }

        // 1. Busca por critério único
        public Optional<ItemEstoque> buscarPorId(String id) {
            if (id == null) return Optional.empty();
            return this.estoque.stream()
                    .filter(i -> i.id().equalsIgnoreCase(id.trim()))
                    .findFirst();
        }

        // 2. Filtro de subconjunto (itens disponíveis)
        public List<ItemEstoque> listarDisponiveis() {
            return this.estoque.stream()
                    .filter(i -> !i.emFalta())
                    .toList();
        }

        // 3. Acumulador: Soma do valor total de estoque
        public BigDecimal calcularValorTotalEmEstoque() {
            return this.estoque.stream()
                    .map(ItemEstoque::preco)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    public static void main(String[] args) {
        CatalogoSeguro catalogo = new CatalogoSeguro();
        catalogo.adicionar(new ItemEstoque("IT-01", "Teclado Mecânico", BigDecimal.valueOf(250.00), false));
        catalogo.adicionar(new ItemEstoque("IT-02", "Mouse Sem Fio", BigDecimal.valueOf(120.00), true));
        catalogo.adicionar(new ItemEstoque("IT-03", "Monitor 24 Pol", BigDecimal.valueOf(890.00), false));

        System.out.println("Total de itens: " + catalogo.listarTodos().size());
        System.out.println("Itens disponíveis: " + catalogo.listarDisponiveis().size());
        System.out.println("Valor patrimonial total: R$ " + catalogo.calcularValorTotalEmEstoque());

        // Testando a proteção contra adulteração externa:
        List<ItemEstoque> listaExterna = catalogo.listarTodos();
        try {
            listaExterna.clear(); // Tentativa de violar o estado interno do catálogo!
        } catch (UnsupportedOperationException e) {
            System.out.println("\n[SUCESSO] O encapsulamento barrou a tentativa de adulterar a lista!");
        }
    }
}
