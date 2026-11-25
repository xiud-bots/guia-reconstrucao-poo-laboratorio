package br.edu.ifpb.didatico.exemplos;

import br.edu.ifpb.didatico.exemplos.arquitetura.ExemploInjecaoDependenciasCamadas.IRepositorioProduto;
import br.edu.ifpb.didatico.exemplos.arquitetura.ExemploInjecaoDependenciasCamadas.Produto;
import br.edu.ifpb.didatico.exemplos.arquitetura.ExemploInjecaoDependenciasCamadas.ProdutoJaCadastradoException;
import br.edu.ifpb.didatico.exemplos.arquitetura.ExemploInjecaoDependenciasCamadas.RepositorioProdutoMemoria;
import br.edu.ifpb.didatico.exemplos.arquitetura.ExemploInjecaoDependenciasCamadas.ServicoCatalogo;
import br.edu.ifpb.didatico.exemplos.colecoes.ExemploColecoesProtegidas.CatalogoSeguro;
import br.edu.ifpb.didatico.exemplos.colecoes.ExemploColecoesProtegidas.ItemEstoque;
import br.edu.ifpb.didatico.exemplos.csv.ExemploLeituraGravacaoCSV;
import br.edu.ifpb.didatico.exemplos.csv.ExemploLeituraGravacaoCSV.RegistroExemplo;
import br.edu.ifpb.didatico.exemplos.excecao.ExemploExcecaoChecada.RecursoOcupadoException;
import br.edu.ifpb.didatico.exemplos.excecao.ExemploExcecaoChecada.VagaEstacionamento;
import br.edu.ifpb.didatico.exemplos.oo.ExemploEncapsulamentoEValidacao.ContaCorrenteExemplo;
import br.edu.ifpb.didatico.exemplos.strategy.ExemploStrategyPolimorfico.Encomenda;
import br.edu.ifpb.didatico.exemplos.strategy.ExemploStrategyPolimorfico.FreteExpresso;
import br.edu.ifpb.didatico.exemplos.strategy.ExemploStrategyPolimorfico.FreteGratis;
import br.edu.ifpb.didatico.exemplos.strategy.ExemploStrategyPolimorfico.FretePadrao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Bateria de Testes Automatizados para validação dos exemplos conceituais e arquiteturais.
 */
public class ExemplosDidaticosTest {

    @Test
    @DisplayName("Guia 01 & OO: Deve validar invariantes e encapsulamento de ContaCorrente")
    void testContaCorrenteInvariantes() {
        assertThrows(IllegalArgumentException.class, () -> new ContaCorrenteExemplo(null, "Gabriel"));
        assertThrows(IllegalArgumentException.class, () -> new ContaCorrenteExemplo("123", "   "));
        assertThrows(IllegalArgumentException.class, () -> new ContaCorrenteExemplo("123", "Gabriel", -10.0, true));

        ContaCorrenteExemplo conta = new ContaCorrenteExemplo("12345-X", "Gabriel");
        assertEquals(0.0, conta.getSaldo());
        assertTrue(conta.isAtiva());

        conta.depositar(300.0);
        assertEquals(300.0, conta.getSaldo());

        conta.sacar(100.0);
        assertEquals(200.0, conta.getSaldo());

        assertThrows(IllegalStateException.class, () -> conta.sacar(500.0));
    }

    @Test
    @DisplayName("Guia 02 & Strategy: Deve calcular valores de frete polimorficamente")
    void testStrategyFrete() {
        Encomenda encPadrao = new Encomenda("BR-01", new FretePadrao(BigDecimal.valueOf(2.50)));
        Encomenda encExpresso = new Encomenda("BR-02", new FreteExpresso());
        Encomenda encGratis = new Encomenda("BR-03", new FreteGratis());

        assertEquals(0, BigDecimal.valueOf(100.00).compareTo(encPadrao.calcularCustoEnvio(40.0)));
        assertEquals(0, BigDecimal.valueOf(95.00).compareTo(encExpresso.calcularCustoEnvio(20.0)));
        assertEquals(0, BigDecimal.ZERO.compareTo(encGratis.calcularCustoEnvio(100.0)));
    }

    @Test
    @DisplayName("Guia 03 & Coleções: Deve garantir imutabilidade e operações com Streams")
    void testColecoesProtegidas() {
        CatalogoSeguro catalogo = new CatalogoSeguro();
        catalogo.adicionar(new ItemEstoque("IT-1", "Mouse", BigDecimal.valueOf(50.00), false));
        catalogo.adicionar(new ItemEstoque("IT-2", "Teclado", BigDecimal.valueOf(150.00), true));

        List<ItemEstoque> itens = catalogo.listarTodos();
        assertThrows(UnsupportedOperationException.class, itens::clear);

        assertEquals(1, catalogo.listarDisponiveis().size());
        assertEquals(0, BigDecimal.valueOf(200.00).compareTo(catalogo.calcularValorTotalEmEstoque()));
        assertTrue(catalogo.buscarPorId("IT-1").isPresent());
    }

    @Test
    @DisplayName("Guia 04 & CSV: Deve gravar e ler registros ignorando comentários e linhas em branco")
    void testGravacaoELeituraCSV() throws IOException {
        File temp = File.createTempFile("teste-didatico-", ".csv");
        temp.deleteOnExit();

        ExemploLeituraGravacaoCSV.gravarRegistro(temp, new RegistroExemplo("REG-1", "Notebook", 3500.00));
        ExemploLeituraGravacaoCSV.gravarRegistro(temp, new RegistroExemplo("REG-2", "Mouse", 120.00));

        List<RegistroExemplo> lidos = ExemploLeituraGravacaoCSV.lerArquivo(temp);
        assertEquals(2, lidos.size());
        assertEquals("REG-1", lidos.get(0).codigo());
        assertEquals("Notebook", lidos.get(0).descricao());
    }

    @Test
    @DisplayName("Guia 05 & Exceções: Deve propagar e capturar exceção checada de domínio")
    void testExcecaoChecada() {
        VagaEstacionamento vaga = new VagaEstacionamento("VAGA-01");
        assertFalse(vaga.isOcupada());

        assertDoesNotThrow(vaga::ocupar);
        assertTrue(vaga.isOcupada());

        RecursoOcupadoException ex = assertThrows(RecursoOcupadoException.class, vaga::ocupar);
        assertEquals("VAGA-01", ex.getIdentificadorRecurso());
    }

    @Test
    @DisplayName("Guia 10 & Arquitetura: Deve testar Serviço com Repositório em Memória desacoplado")
    void testInjecaoManualDependencias() throws ProdutoJaCadastradoException {
        IRepositorioProduto repo = new RepositorioProdutoMemoria();
        ServicoCatalogo servico = new ServicoCatalogo(repo);

        servico.cadastrarProduto(new Produto("P1", "Item Um", 100.0));
        assertEquals(1, servico.listarCatalogo().size());

        assertThrows(ProdutoJaCadastradoException.class, () ->
                servico.cadastrarProduto(new Produto("P1", "Item Duplicado", 50.0))
        );
    }
}
