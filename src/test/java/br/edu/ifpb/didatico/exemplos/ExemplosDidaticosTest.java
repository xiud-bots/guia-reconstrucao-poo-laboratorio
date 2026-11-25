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
import br.edu.ifpb.didatico.exemplos.csv.ExemploPersistenciaAtomicaComLock.RegistroPatrimonial;
import br.edu.ifpb.didatico.exemplos.csv.ExemploPersistenciaAtomicaComLock.RepositorioPatrimonialCSV;
import br.edu.ifpb.didatico.exemplos.excecao.ExemploExcecaoChecada.RecursoOcupadoException;
import br.edu.ifpb.didatico.exemplos.excecao.ExemploExcecaoChecada.VagaEstacionamento;
import br.edu.ifpb.didatico.exemplos.javafx.ExemploDataPickerERegrasTemporaisJavaFX.DataBrConverter;
import br.edu.ifpb.didatico.exemplos.javafx.ExemploDataPickerERegrasTemporaisJavaFX.RegrasTemporais;
import br.edu.ifpb.didatico.exemplos.javafx.ExemploEventBusDesacoplado.AppEventBus;
import br.edu.ifpb.didatico.exemplos.javafx.ExemploEventBusDesacoplado.ItemCadastradoEvent;
import br.edu.ifpb.didatico.exemplos.javafx.ExemploEventBusDesacoplado.ItemExcluidoEvent;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    @Test
    @DisplayName("Guia 18 & Persistência Atômica: Deve gravar com atomicidade, gerar backup e permitir restauração")
    void testPersistenciaAtomicaComLockEBackup() throws IOException {
        Path tempDir = Files.createTempDirectory("teste-repo-atomico-");
        Path arquivoOficial = tempDir.resolve("acervo.csv");
        RepositorioPatrimonialCSV repo = new RepositorioPatrimonialCSV(arquivoOficial);

        try {
            // 1. Gravação do Lote 1
            List<RegistroPatrimonial> lote1 = List.of(
                    new RegistroPatrimonial("PAT-01", "Notebook Core i7", new BigDecimal("4500.00"), true),
                    new RegistroPatrimonial("PAT-02", "Projetor HDMI", new BigDecimal("2200.00"), true)
            );
            repo.salvarTodos(lote1);

            assertTrue(Files.exists(arquivoOficial));
            assertFalse(Files.exists(repo.getCaminhoBackup())); // Primeiro salvamento: sem backup anterior
            assertEquals(2, repo.listarTodos().size());

            // 2. Gravação do Lote 2 -> Deve gerar backup automático contendo o Lote 1
            List<RegistroPatrimonial> lote2 = List.of(
                    new RegistroPatrimonial("PAT-01", "Notebook Core i7", new BigDecimal("4500.00"), true),
                    new RegistroPatrimonial("PAT-02", "Projetor HDMI", new BigDecimal("2200.00"), true),
                    new RegistroPatrimonial("PAT-03", "Câmera 4K", new BigDecimal("3800.00"), false)
            );
            repo.salvarTodos(lote2);

            assertTrue(Files.exists(repo.getCaminhoBackup()), "Backup (.bak) deve existir após reescrita");
            assertEquals(3, repo.listarTodos().size());

            // 3. Simula corrupção/remoção do arquivo oficial e restauração via backup
            Files.deleteIfExists(arquivoOficial);
            assertFalse(Files.exists(arquivoOficial));

            boolean restaurou = repo.restaurarBackup();
            assertTrue(restaurou, "Restauração deve ter sucesso a partir do backup");
            assertTrue(Files.exists(arquivoOficial));

            // O backup restaurado continha o estado anterior (Lote 1)
            assertEquals(2, repo.listarTodos().size());
            assertEquals("PAT-01", repo.listarTodos().get(0).tombamento());
        } finally {
            Files.deleteIfExists(arquivoOficial);
            Files.deleteIfExists(repo.getCaminhoBackup());
            Files.deleteIfExists(tempDir);
        }
    }

    @Test
    @DisplayName("Guia 19 & EventBus: Deve despachar eventos tipados para ouvintes e respeitar cancelamento")
    void testEventBusDesacoplado() {
        AppEventBus bus = AppEventBus.getInstance();
        bus.limparTodos();

        AtomicInteger cadastrosRecebidosOuvinte1 = new AtomicInteger(0);
        AtomicInteger cadastrosRecebidosOuvinte2 = new AtomicInteger(0);
        AtomicInteger exclusoesRecebidas = new AtomicInteger(0);
        AtomicReference<String> ultimoCodigoCadastrado = new AtomicReference<>();

        // Inscrição 1: Ouvinte 1 de Cadastros
        Runnable cancelaOuvinte1 = bus.inscrever(ItemCadastradoEvent.class, evt -> {
            cadastrosRecebidosOuvinte1.incrementAndGet();
            ultimoCodigoCadastrado.set(evt.codigo());
        });

        // Inscrição 2: Ouvinte 2 de Cadastros (Dashboard)
        Runnable cancelaOuvinte2 = bus.inscrever(ItemCadastradoEvent.class, evt ->
                cadastrosRecebidosOuvinte2.incrementAndGet());

        // Inscrição 3: Ouvinte de Exclusões
        Runnable cancelaExclusoes = bus.inscrever(ItemExcluidoEvent.class, evt ->
                exclusoesRecebidas.incrementAndGet());

        // Dispara primeiro evento de cadastro
        bus.publicar(new ItemCadastradoEvent("ITEM-99", "Mesa Digitalizadora"));
        assertEquals(1, cadastrosRecebidosOuvinte1.get());
        assertEquals(1, cadastrosRecebidosOuvinte2.get());
        assertEquals(0, exclusoesRecebidas.get());
        assertEquals("ITEM-99", ultimoCodigoCadastrado.get());

        // Cancela Ouvinte 1
        cancelaOuvinte1.run();

        // Dispara segundo evento de cadastro
        bus.publicar(new ItemCadastradoEvent("ITEM-100", "Microfone Condensador"));
        assertEquals(1, cadastrosRecebidosOuvinte1.get(), "Ouvinte 1 cancelado não deve mais receber eventos");
        assertEquals(2, cadastrosRecebidosOuvinte2.get(), "Ouvinte 2 ativo deve receber o novo evento");

        // Dispara evento de exclusão
        bus.publicar(new ItemExcluidoEvent("ITEM-99"));
        assertEquals(1, exclusoesRecebidas.get());

        // Limpeza
        cancelaOuvinte2.run();
        cancelaExclusoes.run();
        bus.limparTodos();
    }

    @Test
    @DisplayName("Guia 20 & Datas Modernas: Deve calcular prazos, atrasos, multas e converter pt-BR com segurança")
    void testRegrasTemporaisEDatePickerConverter() {
        LocalDate inicio = LocalDate.of(2026, 11, 10); // Terça-feira
        LocalDate prazoPrevisto = LocalDate.of(2026, 11, 17); // 7 dias depois

        // 1. Devolução no prazo ou antecipada -> 0 dias de atraso
        assertEquals(0, RegrasTemporais.calcularDiasAtraso(prazoPrevisto, prazoPrevisto));
        assertEquals(0, RegrasTemporais.calcularDiasAtraso(prazoPrevisto, LocalDate.of(2026, 11, 15)));

        // 2. Devolução com 5 dias de atraso
        LocalDate devolucaoTardia = LocalDate.of(2026, 11, 22);
        long diasAtraso = RegrasTemporais.calcularDiasAtraso(prazoPrevisto, devolucaoTardia);
        assertEquals(5, diasAtraso);

        // 3. Cálculo de multa (R$ 3,50 por dia, carência 0 dias)
        BigDecimal multaSemCarencia = RegrasTemporais.calcularMulta(diasAtraso, new BigDecimal("3.50"), 0);
        assertEquals(0, new BigDecimal("17.50").compareTo(multaSemCarencia));

        // 4. Cálculo de multa com carência de 2 dias (cobra apenas 3 dias)
        BigDecimal multaComCarencia = RegrasTemporais.calcularMulta(diasAtraso, new BigDecimal("3.50"), 2);
        assertEquals(0, new BigDecimal("10.50").compareTo(multaComCarencia));

        // 5. Adição de dias úteis (Inicia na sexta 13/11/2026 + 3 dias úteis -> Seg 16, Ter 17, Qua 18)
        LocalDate sexta = LocalDate.of(2026, 11, 13);
        LocalDate resultadoDiasUteis = RegrasTemporais.adicionarDiasUteis(sexta, 3);
        assertEquals(LocalDate.of(2026, 11, 18), resultadoDiasUteis);

        // 6. DataBrConverter
        DataBrConverter converter = new DataBrConverter();
        assertEquals("25/11/2026", converter.toString(LocalDate.of(2026, 11, 25)));
        assertEquals("", converter.toString(null));

        LocalDate parsed = converter.fromString("25/11/2026");
        assertNotNull(parsed);
        assertEquals(2026, parsed.getYear());
        assertEquals(11, parsed.getMonthValue());
        assertEquals(25, parsed.getDayOfMonth());

        // Parse de entradas inválidas não deve lançar exceção, deve retornar null com segurança
        assertNull(converter.fromString("invalido"));
        assertNull(converter.fromString("32/13/2026"));
        assertNull(converter.fromString(""));
        assertNull(converter.fromString(null));
    }
}
