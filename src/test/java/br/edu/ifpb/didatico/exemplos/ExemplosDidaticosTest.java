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
import br.edu.ifpb.didatico.exemplos.javafx.ExemploValidacaoFormularioJavaFX.SafeParser;
import br.edu.ifpb.didatico.exemplos.moderno.ExemploJavaModernoRecordESealed;
import br.edu.ifpb.didatico.exemplos.moderno.ExemploJavaModernoRecordESealed.MovimentacaoEstoque;
import br.edu.ifpb.didatico.exemplos.moderno.ExemploJavaModernoRecordESealed.ResultadoValidacao;
import br.edu.ifpb.didatico.exemplos.oo.ExemploEncapsulamentoEValidacao.ContaCorrenteExemplo;
import br.edu.ifpb.didatico.exemplos.strategy.ExemploStrategyPolimorfico.Encomenda;
import br.edu.ifpb.didatico.exemplos.strategy.ExemploStrategyPolimorfico.FreteExpresso;
import br.edu.ifpb.didatico.exemplos.strategy.ExemploStrategyPolimorfico.FreteGratis;
import br.edu.ifpb.didatico.exemplos.strategy.ExemploStrategyPolimorfico.FretePadrao;
import br.edu.ifpb.didatico.exemplos.teste.ExemploTestabilidadeComFake.IRepositorioRecurso;
import br.edu.ifpb.didatico.exemplos.teste.ExemploTestabilidadeComFake.LimiteReservasExcedidoException;
import br.edu.ifpb.didatico.exemplos.teste.ExemploTestabilidadeComFake.RecursoAuditavel;
import br.edu.ifpb.didatico.exemplos.teste.ExemploTestabilidadeComFake.RecursoIndisponivelException;
import br.edu.ifpb.didatico.exemplos.teste.ExemploTestabilidadeComFake.RepositorioRecursoFakeMemoria;
import br.edu.ifpb.didatico.exemplos.teste.ExemploTestabilidadeComFake.ServicoReserva;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Bateria de Testes Automatizados para validação dos exemplos conceituais e arquiteturais.
 * Cobre POO Pura, Strategy, Imutabilidade, CSV, Checked Exceptions, Pure DI,
 * Recursos Modernos do Java 21 LTS (Records & Sealed), Safe Parsing e Fakes em Memória.
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

    @Test
    @DisplayName("Guia 12 & Java 21: Deve validar Records com construtor compacto e imutabilidade")
    void testJava21RecordCompactConstructor() {
        // Validação de invariantes no construtor compacto
        assertThrows(IllegalArgumentException.class, () ->
                new MovimentacaoEstoque(null, 5, BigDecimal.valueOf(100.0)));
        assertThrows(IllegalArgumentException.class, () ->
                new MovimentacaoEstoque("IT-1", 0, BigDecimal.valueOf(100.0)));
        assertThrows(IllegalArgumentException.class, () ->
                new MovimentacaoEstoque("IT-1", 5, BigDecimal.valueOf(-1.0)));

        MovimentacaoEstoque mov = new MovimentacaoEstoque("IT-1", 4, BigDecimal.valueOf(25.50), List.of("Lote A"), null);
        assertEquals(0, BigDecimal.valueOf(102.00).compareTo(mov.calcularValorTotal()));
        assertNotNull(mov.dataHora());

        // Imutabilidade da lista de observações
        assertThrows(UnsupportedOperationException.class, () -> mov.observacoes().add("Hack"));

        // Pattern matching para instanceof
        assertTrue(ExemploJavaModernoRecordESealed.ehMovimentacaoDeItem(mov, "IT-1"));
        assertFalse(ExemploJavaModernoRecordESealed.ehMovimentacaoDeItem(mov, "OUTRO"));
        assertFalse(ExemploJavaModernoRecordESealed.ehMovimentacaoDeItem("uma string qualquer", "IT-1"));
    }

    @Test
    @DisplayName("Guia 12 & Java 21: Deve avaliar Sealed Types com Pattern Matching Switch e Guards")
    void testJava21SealedSwitchPatternMatching() {
        ResultadoValidacao aprovado = new ResultadoValidacao.Aprovado("PROT-100", BigDecimal.valueOf(5000.00));
        ResultadoValidacao reprovadoRecurso = new ResultadoValidacao.ReprovadoRegra("LIMITE", "Excedeu teto", true);
        ResultadoValidacao reprovadoFinal = new ResultadoValidacao.ReprovadoRegra("FRAUDE", "Bloqueio judicial", false);
        ResultadoValidacao erro = new ResultadoValidacao.ErroInfraestrutura("Timeout no cluster", 504);

        String msgAprovado = ExemploJavaModernoRecordESealed.formatarResultado(aprovado);
        assertTrue(msgAprovado.contains("[APROVADO]"));
        assertTrue(msgAprovado.contains("PROT-100"));

        String msgRecurso = ExemploJavaModernoRecordESealed.formatarResultado(reprovadoRecurso);
        assertTrue(msgRecurso.contains("[REPROVADO COM RECURSO]"));

        String msgFinal = ExemploJavaModernoRecordESealed.formatarResultado(reprovadoFinal);
        assertTrue(msgFinal.contains("[REPROVADO DEFINITIVO]"));

        String msgErro = ExemploJavaModernoRecordESealed.formatarResultado(erro);
        assertTrue(msgErro.contains("[ERRO DE SISTEMA 504]"));
    }

    @Test
    @DisplayName("Guia 15 & Validação: SafeParser deve converter moedas e inteiros sem exceções")
    void testSafeParser() {
        // Conversão de Moeda
        Optional<BigDecimal> moeda1 = SafeParser.parseMoeda("1.250,50");
        assertTrue(moeda1.isPresent());
        assertEquals(0, new BigDecimal("1250.50").compareTo(moeda1.get()));

        Optional<BigDecimal> moeda2 = SafeParser.parseMoeda("45,00");
        assertTrue(moeda2.isPresent());
        assertEquals(0, new BigDecimal("45.00").compareTo(moeda2.get()));

        assertTrue(SafeParser.parseMoeda("invalido").isEmpty());
        assertTrue(SafeParser.parseMoeda("-10,00").isEmpty());
        assertTrue(SafeParser.parseMoeda("   ").isEmpty());
        assertTrue(SafeParser.parseMoeda(null).isEmpty());

        // Conversão de Inteiro Positivo
        Optional<Integer> qtd1 = SafeParser.parseInteiroPositivo("10");
        assertTrue(qtd1.isPresent());
        assertEquals(10, qtd1.get());

        assertTrue(SafeParser.parseInteiroPositivo("0").isEmpty());
        assertTrue(SafeParser.parseInteiroPositivo("-5").isEmpty());
        assertTrue(SafeParser.parseInteiroPositivo("abc").isEmpty());
        assertTrue(SafeParser.parseInteiroPositivo(null).isEmpty());
    }

    @Test
    @DisplayName("Guia 16 & Testabilidade: Servico com Repositorio Fake deve validar limites e concorrência")
    void testTestabilidadeComFakeMemoria() throws RecursoIndisponivelException, LimiteReservasExcedidoException {
        IRepositorioRecurso repo = new RepositorioRecursoFakeMemoria();
        repo.salvar(new RecursoAuditavel("REC-01", "Notebook A", null, false));
        repo.salvar(new RecursoAuditavel("REC-02", "Notebook B", null, false));
        repo.salvar(new RecursoAuditavel("REC-03", "Projetor", null, false));

        ServicoReserva servico = new ServicoReserva(repo);

        // 1. Aloca 2 recursos com sucesso (limite máximo regulamentar)
        servico.alocarRecurso("REC-01", "Gabriel");
        servico.alocarRecurso("REC-02", "Gabriel");

        assertEquals(2, repo.contarPorResponsavel("Gabriel"));

        // 2. Tenta alocar um 3º recurso para a mesma pessoa -> LimiteReservasExcedidoException
        assertThrows(LimiteReservasExcedidoException.class, () ->
                servico.alocarRecurso("REC-03", "Gabriel")
        );

        // 3. Tenta alocar recurso já ocupado para outra pessoa -> RecursoIndisponivelException
        assertThrows(RecursoIndisponivelException.class, () ->
                servico.alocarRecurso("REC-01", "OutroUsuario")
        );

        // 4. Libera um recurso e valida nova alocação permitida
        servico.liberarRecurso("REC-01");
        assertEquals(1, repo.contarPorResponsavel("Gabriel"));

        assertDoesNotThrow(() -> servico.alocarRecurso("REC-03", "Gabriel"));
        assertEquals(2, repo.contarPorResponsavel("Gabriel"));
    }
}
