package br.edu.ifpb.didatico.exemplos.arquitetura;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Exemplo Didático: Demonstração de Arquitetura de Camadas e Injeção Manual de Dependências (Pure DI).
 * Mostra como desacoplar a Camada de Serviço da Camada de Persistência através de Interfaces
 * de Repositório, permitindo testes unitários imediatos e total inversão de dependência (SOLID DIP).
 */
public class ExemploInjecaoDependenciasCamadas {

    // ==========================================
    // 1. CAMADA DE DOMÍNIO (Entidade e Exceções)
    // ==========================================
    public record Produto(String codigo, String descricao, double preco) {
        public Produto {
            if (codigo == null || codigo.isBlank()) throw new IllegalArgumentException("Código obrigatório.");
            if (descricao == null || descricao.isBlank()) throw new IllegalArgumentException("Descrição obrigatória.");
            if (preco < 0.0) throw new IllegalArgumentException("Preço não pode ser negativo.");
            codigo = codigo.trim();
            descricao = descricao.trim();
        }
    }

    public static class ProdutoJaCadastradoException extends Exception {
        public ProdutoJaCadastradoException(String codigo) {
            super("Já existe um produto registrado com o código: " + codigo);
        }
    }

    // ==========================================
    // 2. CAMADA DE PERSISTÊNCIA (Contrato & Implementação)
    // ==========================================
    public interface IRepositorioProduto {
        void salvar(Produto produto);
        Optional<Produto> buscarPorCodigo(String codigo);
        List<Produto> listarTodos();
        boolean remover(String codigo);
    }

    /**
     * Implementação em memória (ideal para testes unitários ou simulação desacoplada).
     */
    public static class RepositorioProdutoMemoria implements IRepositorioProduto {
        private final Map<String, Produto> mapa = new HashMap<>();

        @Override
        public void salvar(Produto produto) {
            mapa.put(produto.codigo().toUpperCase(), produto);
        }

        @Override
        public Optional<Produto> buscarPorCodigo(String codigo) {
            if (codigo == null) return Optional.empty();
            return Optional.ofNullable(mapa.get(codigo.trim().toUpperCase()));
        }

        @Override
        public List<Produto> listarTodos() {
            return new ArrayList<>(mapa.values());
        }

        @Override
        public boolean remover(String codigo) {
            return mapa.remove(codigo.trim().toUpperCase()) != null;
        }
    }

    // ==========================================
    // 3. CAMADA DE SERVIÇO (Regras de Negócio e Orquestração)
    // ==========================================
    public static class ServicoCatalogo {
        private final IRepositorioProduto repositorio;

        // INJEÇÃO MANUAL DE DEPENDÊNCIA POR CONSTRUTOR (Constructor Injection)
        // O serviço depende da abstração IRepositorioProduto, NUNCA de implementações de disco ou SQL.
        public ServicoCatalogo(IRepositorioProduto repositorio) {
            this.repositorio = Objects.requireNonNull(repositorio, "O repositório de produtos é obrigatório.");
        }

        public void cadastrarProduto(Produto produto) throws ProdutoJaCadastradoException {
            Objects.requireNonNull(produto, "Produto não pode ser nulo.");

            // 1. Regra de Negócio: Unicidade de código
            if (repositorio.buscarPorCodigo(produto.codigo()).isPresent()) {
                throw new ProdutoJaCadastradoException(produto.codigo());
            }

            // 2. Orquestração da persistência através do contrato
            repositorio.salvar(produto);
        }

        public List<Produto> listarCatalogo() {
            // Proteção de coleção defensiva
            return Collections.unmodifiableList(repositorio.listarTodos());
        }

        public double calcularValorTotalEstoque() {
            return repositorio.listarTodos().stream()
                    .mapToDouble(Produto::preco)
                    .sum();
        }
    }

    // ==========================================
    // 4. DEMONSTRAÇÃO DE EXECUÇÃO E COMPOSITION ROOT
    // ==========================================
    public static void main(String[] args) {
        System.out.println("=== Início da Demonstração: Inversão de Dependências ===");

        // Ponto de Composição (Composition Root):
        // 1. Criamos a implementação concreta de persistência
        IRepositorioProduto repositorio = new RepositorioProdutoMemoria();

        // 2. Injetamos o repositório no serviço
        ServicoCatalogo servico = new ServicoCatalogo(repositorio);

        // 3. Executamos as operações de negócio
        try {
            servico.cadastrarProduto(new Produto("PROD-001", "Cadeira Ergonômica", 450.00));
            servico.cadastrarProduto(new Produto("PROD-002", "Mesa Ajustável", 1200.00));
            System.out.println("[OK] Produtos cadastrados com sucesso!");

            System.out.printf("Total de itens no catálogo: %d%n", servico.listarCatalogo().size());
            System.out.printf("Valor patrimonial total: R$ %.2f%n", servico.calcularValorTotalEstoque());

            // Tentativa de cadastrar produto duplicado para demonstrar exceção checada
            System.out.println("\nTentando cadastrar produto duplicado (PROD-001)...");
            servico.cadastrarProduto(new Produto("PROD-001", "Cadeira Duplicada", 500.00));

        } catch (ProdutoJaCadastradoException e) {
            System.out.println("[EXCEÇÃO CHECADA CAPTURADA COM SUCESSO] " + e.getMessage());
        }
    }
}
