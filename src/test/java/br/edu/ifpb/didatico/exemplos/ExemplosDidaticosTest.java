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
import br.edu.ifpb.didatico.exemplos.javafx.ExemploDashboardGraficosJavaFX.ItemPatrimonialAnalitico;
import br.edu.ifpb.didatico.exemplos.javafx.ExemploDashboardGraficosJavaFX.ResumoGeral;
import br.edu.ifpb.didatico.exemplos.javafx.ExemploDashboardGraficosJavaFX.ServicoAnalitico;
import br.edu.ifpb.didatico.exemplos.javafx.ExemploCommandUndoRedoJavaFX.ComandoAlternarManutencao;
import br.edu.ifpb.didatico.exemplos.javafx.ExemploCommandUndoRedoJavaFX.ComandoCadastrarItem;
import br.edu.ifpb.didatico.exemplos.javafx.ExemploCommandUndoRedoJavaFX.ComandoRemoverItem;
import br.edu.ifpb.didatico.exemplos.javafx.ExemploCommandUndoRedoJavaFX.GerenciadorHistorico;
import br.edu.ifpb.didatico.exemplos.javafx.ExemploCommandUndoRedoJavaFX.ItemInventario;
import br.edu.ifpb.didatico.exemplos.javafx.ExemploExportacaoRelatoriosEFileChooserJavaFX.ExportadorCsvFormatado;
import br.edu.ifpb.didatico.exemplos.javafx.ExemploExportacaoRelatoriosEFileChooserJavaFX.GeradorReciboFormatado;
import br.edu.ifpb.didatico.exemplos.javafx.ExemploExportacaoRelatoriosEFileChooserJavaFX.ItemMovimentado;
import br.edu.ifpb.didatico.exemplos.javafx.ExemploTratamentoGlobalErrosJavaFX.TratadorFalhasGlobal;
import br.edu.ifpb.didatico.exemplos.state.ExemploStateCicloVida.RecursoPatrimonial;
import br.edu.ifpb.didatico.exemplos.state.ExemploStateCicloVida.TransicaoEstadoInvalidaException;
import br.edu.ifpb.didatico.exemplos.builder.ExemploBuilderInvariantesEWizard.FichaPatrimonial;
import br.edu.ifpb.didatico.exemplos.templatemethod.ExemploTemplateMethodImportacaoLote.EquipamentoImportado;
import br.edu.ifpb.didatico.exemplos.templatemethod.ExemploTemplateMethodImportacaoLote.ImportadorEquipamentosCSV;
import br.edu.ifpb.didatico.exemplos.templatemethod.ExemploTemplateMethodImportacaoLote.ResultadoImportacao;
import br.edu.ifpb.didatico.exemplos.memento.ExemploMementoEPreferenciasJavaFX.CaretakerPreferencias;
import br.edu.ifpb.didatico.exemplos.memento.ExemploMementoEPreferenciasJavaFX.EstadoJanelaMemento;
import br.edu.ifpb.didatico.exemplos.memento.ExemploMementoEPreferenciasJavaFX.OriginatorJanela;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

    @Test
    @DisplayName("Guia 21 & Dashboards: Deve calcular métricas agrupadas e resumos analíticos com Streams")
    void testAgregacoesStreamsEDashboard() {
        ServicoAnalitico servico = new ServicoAnalitico();
        List<ItemPatrimonialAnalitico> itens = List.of(
                new ItemPatrimonialAnalitico("IT-01", "Notebook A", "TI", new BigDecimal("3000.00"), true),
                new ItemPatrimonialAnalitico("IT-02", "Notebook B", "TI", new BigDecimal("3500.00"), false),
                new ItemPatrimonialAnalitico("IT-03", "Projetor 4K", "Audiovisual", new BigDecimal("2500.00"), true),
                new ItemPatrimonialAnalitico("IT-04", "Switch 24p", "Redes", new BigDecimal("1200.00"), false)
        );

        // 1. Contagem por Categoria
        Map<String, Long> contagem = servico.contarPorCategoria(itens);
        assertEquals(2L, contagem.get("TI"));
        assertEquals(1L, contagem.get("Audiovisual"));
        assertEquals(1L, contagem.get("Redes"));

        // 2. Valor Total por Categoria
        Map<String, BigDecimal> valores = servico.calcularValorTotalPorCategoria(itens);
        assertEquals(0, new BigDecimal("6500.00").compareTo(valores.get("TI")));
        assertEquals(0, new BigDecimal("2500.00").compareTo(valores.get("Audiovisual")));
        assertEquals(0, new BigDecimal("1200.00").compareTo(valores.get("Redes")));

        // 3. Resumo Geral com Particionamento
        ResumoGeral resumo = servico.calcularResumo(itens);
        assertEquals(4L, resumo.totalItens());
        assertEquals(2L, resumo.itensEmUso());
        assertEquals(2L, resumo.itensDisponiveis());
        assertEquals(0, new BigDecimal("10200.00").compareTo(resumo.valorTotalGeral()));
        assertEquals(50.0, resumo.percentualOcupacao(), 0.001);
    }

    @Test
    @DisplayName("Guia 22 & Command: Deve executar, desfazer, refazer e manter trilha de auditoria")
    void testPadraoCommandUndoRedo() {
        ObservableList<ItemInventario> lista = FXCollections.observableArrayList();
        GerenciadorHistorico historico = new GerenciadorHistorico(10);

        ItemInventario item1 = new ItemInventario("A-1", "Servidor", "TI", false);
        ItemInventario item2 = new ItemInventario("A-2", "Câmera", "Audiovisual", false);

        assertFalse(historico.podeDesfazerProperty().get());
        assertFalse(historico.podeRefazerProperty().get());

        // 1. Cadastra item 1
        historico.executarComando(new ComandoCadastrarItem(lista, item1));
        assertEquals(1, lista.size());
        assertTrue(historico.podeDesfazerProperty().get());
        assertFalse(historico.podeRefazerProperty().get());

        // 2. Cadastra item 2
        historico.executarComando(new ComandoCadastrarItem(lista, item2));
        assertEquals(2, lista.size());

        // 3. Alterna status de manutenção do item 1
        historico.executarComando(new ComandoAlternarManutencao(item1));
        assertTrue(item1.isEmManutencao());

        // 4. Desfaz alternância de manutenção
        historico.desfazer();
        assertFalse(item1.isEmManutencao());
        assertTrue(historico.podeRefazerProperty().get());

        // 5. Refaz alternância
        historico.refazer();
        assertTrue(item1.isEmManutencao());

        // 6. Remove item 2 e desfaz remoção (deve reinserir preservando integridade)
        historico.executarComando(new ComandoRemoverItem(lista, item2));
        assertEquals(1, lista.size());
        assertFalse(lista.contains(item2));

        historico.desfazer();
        assertEquals(2, lista.size());
        assertTrue(lista.contains(item2));

        // 7. Trilha de auditoria preenchida
        assertFalse(historico.getTrilhaAuditoria().isEmpty());
    }

    @Test
    @DisplayName("Guia 23 & Recibos: Deve gerar texto monoespaçado tabulado com hash SHA-256 e CSV")
    void testGeradorReciboFormatadoEAutenticidade() {
        List<ItemMovimentado> itens = List.of(
                new ItemMovimentado("PAT-01", "Notebook Corporativo", new BigDecimal("45.00")),
                new ItemMovimentado("PAT-02", "Projetor Laser", new BigDecimal("35.00"))
        );

        LocalDateTime agora = LocalDateTime.of(2026, 11, 25, 20, 30, 0);
        String recibo = GeradorReciboFormatado.gerarReciboMovimentacao(
                "TX-999", "Prof. Gabriel", "Aluno Lucas", itens, new BigDecimal("10.00"), agora);

        // Validações do Recibo Monoespaçado
        assertTrue(recibo.contains("TX-999"));
        assertTrue(recibo.contains("Prof. Gabriel"));
        assertTrue(recibo.contains("Aluno Lucas"));
        assertTrue(recibo.contains("PAT-01"));
        assertTrue(recibo.contains("TOTAL CONSOLIDADO"));
        assertTrue(recibo.contains("Chave de Autenticação:"));

        // Hash SHA-256 determinístico
        String hash1 = GeradorReciboFormatado.gerarHashAutenticidade("TX-999", "Aluno Lucas", new BigDecimal("90.00"));
        String hash2 = GeradorReciboFormatado.gerarHashAutenticidade("TX-999", "Aluno Lucas", new BigDecimal("90.00"));
        assertEquals(hash1, hash2);
        assertEquals(24, hash1.length());

        // Exportador CSV
        String csv = ExportadorCsvFormatado.gerarCsv(itens);
        assertTrue(csv.contains("PAT-01;Notebook Corporativo;45.00"));
        assertTrue(csv.contains("PAT-02;Projetor Laser;35.00"));
    }

    @Test
    @DisplayName("Guia 24 & Resiliência: Deve formatar log de diagnóstico e registrar falha em disco com stacktrace")
    void testTratadorFalhasELogDiagnostico() throws IOException {
        Path tempLog = Files.createTempFile("teste-diagnostico-", ".log");
        tempLog.toFile().deleteOnExit();

        Exception excecaoSimulada = new IllegalArgumentException("Parâmetro de negócio corrompido");
        Thread threadSimulada = new Thread("Thread-Teste-Automatizado");

        String entradaFormatada = TratadorFalhasGlobal.formatarEntradaLog(threadSimulada, excecaoSimulada);
        assertTrue(entradaFormatada.contains("Thread-Teste-Automatizado"));
        assertTrue(entradaFormatada.contains("IllegalArgumentException"));
        assertTrue(entradaFormatada.contains("Parâmetro de negócio corrompido"));
        assertTrue(entradaFormatada.contains("Rastreamento de Pilha:"));

        // Grava no disco
        TratadorFalhasGlobal.registrarLogEmDisco(tempLog, threadSimulada, excecaoSimulada);
        assertTrue(Files.exists(tempLog));
        String conteudoArquivo = Files.readString(tempLog);
        assertTrue(conteudoArquivo.contains("Thread-Teste-Automatizado"));
        assertTrue(conteudoArquivo.contains("Parâmetro de negócio corrompido"));

        Files.deleteIfExists(tempLog);
    }

    @Test
    @DisplayName("Guia 25 & State: Deve validar ciclo de vida, transições válidas e rejeição de ilegais com FSM")
    void testPadraoStateCicloVida() throws TransicaoEstadoInvalidaException {
        RecursoPatrimonial recurso = new RecursoPatrimonial("REC-100", "Osciloscópio Digital");
        assertEquals("DISPONIVEL", recurso.getEstado().getNomeEstado());
        assertTrue(recurso.getEstado().permiteEmprestimo());
        assertFalse(recurso.getEstado().permiteDevolucao());

        // 1. Empréstimo com sucesso
        recurso.emprestar("Prof. Gabriel");
        assertEquals("EMPRESTADO", recurso.getEstado().getNomeEstado());
        assertEquals("Prof. Gabriel", recurso.getResponsavelAtual());
        assertFalse(recurso.getEstado().permiteEmprestimo());
        assertTrue(recurso.getEstado().permiteDevolucao());

        // 2. Operações ilegais enquanto emprestado
        assertThrows(TransicaoEstadoInvalidaException.class, () -> recurso.emprestar("Outro"));
        assertThrows(TransicaoEstadoInvalidaException.class, () -> recurso.enviarParaManutencao("Defeito"));
        assertThrows(TransicaoEstadoInvalidaException.class, () -> recurso.baixar("Obsolescência"));

        // 3. Devolução
        recurso.devolver();
        assertEquals("DISPONIVEL", recurso.getEstado().getNomeEstado());
        assertNull(recurso.getResponsavelAtual());

        // 4. Envio para manutenção e conclusão
        recurso.enviarParaManutencao("Calibração anual");
        assertEquals("EM_MANUTENCAO", recurso.getEstado().getNomeEstado());
        assertEquals("Calibração anual", recurso.getUltimoMotivoManutencao());
        assertThrows(TransicaoEstadoInvalidaException.class, () -> recurso.emprestar("Aluno"));

        recurso.concluirManutencao();
        assertEquals("DISPONIVEL", recurso.getEstado().getNomeEstado());

        // 5. Baixa patrimonial (Estado terminal)
        recurso.baixar("Inutilização total por descarga elétrica");
        assertEquals("BAIXADO", recurso.getEstado().getNomeEstado());
        assertFalse(recurso.getEstado().permiteEmprestimo());
        assertFalse(recurso.getEstado().permiteDevolucao());
        assertThrows(TransicaoEstadoInvalidaException.class, () -> recurso.emprestar("Tentativa"));
        assertThrows(TransicaoEstadoInvalidaException.class, () -> recurso.devolver());
        assertThrows(TransicaoEstadoInvalidaException.class, () -> recurso.enviarParaManutencao("Tentar reparar"));
    }

    @Test
    @DisplayName("Guia 26 & Builder: Deve construir FichaPatrimonial imutável validando invariantes atômicas")
    void testPadraoBuilderInvariantes() {
        // 1. Construção válida com todos os campos
        FichaPatrimonial ficha = FichaPatrimonial.builder()
                .comIdentificacao("PAT-2026", "Servidor Rack 2U")
                .naCategoria("Servidores")
                .comValor(new BigDecimal("15499.90"))
                .adquiridoEm(LocalDate.of(2026, 1, 10))
                .comNumeroSerie("SRV-998877")
                .alocadoEm("Datacenter Sala 3")
                .comInspecaoPeriodica(90)
                .build();

        assertEquals("PAT-2026", ficha.getCodigoPatrimonio());
        assertEquals("Servidor Rack 2U", ficha.getDescricao());
        assertEquals("Servidores", ficha.getCategoria());
        assertEquals(0, new BigDecimal("15499.90").compareTo(ficha.getValorAquisicao()));
        assertEquals(LocalDate.of(2026, 1, 10), ficha.getDataAquisicao());
        assertTrue(ficha.getNumeroSerie().isPresent());
        assertEquals("SRV-998877", ficha.getNumeroSerie().get());
        assertEquals("Datacenter Sala 3", ficha.getLocalizacaoFisica());
        assertTrue(ficha.isRequerInspecaoPeriodica());
        assertEquals(90, ficha.getIntervaloInspecaoDias().orElse(0));

        // 2. Invariantes: Código ausente
        assertThrows(IllegalStateException.class, () -> FichaPatrimonial.builder()
                .comIdentificacao("", "Descrição válida")
                .comValor(BigDecimal.TEN)
                .build());

        // 3. Invariantes: Descrição ausente
        assertThrows(IllegalStateException.class, () -> FichaPatrimonial.builder()
                .comIdentificacao("COD-1", "   ")
                .comValor(BigDecimal.TEN)
                .build());

        // 4. Invariantes: Valor negativo
        assertThrows(IllegalStateException.class, () -> FichaPatrimonial.builder()
                .comIdentificacao("COD-1", "Descrição")
                .comValor(new BigDecimal("-1.00"))
                .build());

        // 5. Invariantes: Data no futuro
        assertThrows(IllegalStateException.class, () -> FichaPatrimonial.builder()
                .comIdentificacao("COD-1", "Descrição")
                .comValor(BigDecimal.TEN)
                .adquiridoEm(LocalDate.now().plusDays(5))
                .build());

        // 6. Invariantes: Intervalo de inspeção não positivo
        assertThrows(IllegalArgumentException.class, () -> FichaPatrimonial.builder()
                .comInspecaoPeriodica(0));
    }

    @Test
    @DisplayName("Guia 27 & Template Method: Deve executar importação em lote resiliente isolando linhas corrompidas")
    void testPadraoTemplateMethodImportacaoLote() throws IOException {
        Path tempCsv = Files.createTempFile("teste-importacao-lote-", ".csv");
        tempCsv.toFile().deleteOnExit();

        // 1 cabeçalho + 2 linhas válidas + 3 linhas inválidas
        String conteudo = """
                codigo;descricao;valor;categoria
                EQ-01;Multímetro Digital;180.50;Eletrônica
                EQ-02;Fonte de Bancada;450.00;Eletrônica
                EQ-03;Linha Com Valor Invalido;ABC;Eletrônica
                EQ-04;Linha Sem Colunas
                EQ-05;Equipamento Valor Negativo;-50.00;Eletrônica
                """;
        Files.writeString(tempCsv, conteudo);

        List<EquipamentoImportado> acervo = new java.util.ArrayList<>();
        ImportadorEquipamentosCSV importador = new ImportadorEquipamentosCSV(acervo);

        ResultadoImportacao<EquipamentoImportado> resultado = importador.processar(tempCsv, null);

        // Validações do Lote Resiliente
        assertEquals(5, resultado.totalLinhasProcessadas());
        assertEquals(2, resultado.totalSucesso());
        assertEquals(3, resultado.totalFalhas());
        assertEquals(2, acervo.size());
        assertEquals(40.0, resultado.taxaAproveitamento(), 0.001);

        // Verifica os erros catalogados com precisão
        assertEquals(4, resultado.falhas().get(0).numeroLinha());
        assertTrue(resultado.falhas().get(0).motivo().contains("Valor numérico inválido"));

        assertEquals(5, resultado.falhas().get(1).numeroLinha());
        assertTrue(resultado.falhas().get(1).motivo().contains("insuficientes"));

        assertEquals(6, resultado.falhas().get(2).numeroLinha());
        assertTrue(resultado.falhas().get(2).motivo().contains("estritamente positivo"));

        Files.deleteIfExists(tempCsv);
    }

    @Test
    @DisplayName("Guia 28 & Memento: Deve capturar snapshot de janela, salvar em properties e restaurar integridade")
    void testPadraoMementoEPreferencias() throws IOException {
        Path tempProps = Files.createTempFile("teste-preferencias-", ".properties");
        tempProps.toFile().deleteOnExit();

        OriginatorJanela originator = new OriginatorJanela();
        originator.atualizarEstado(250.0, 180.0, 1280.0, 800.0, false, "ESCURO", "/home/gabriel/documentos");

        // 1. Cria Memento
        EstadoJanelaMemento snapshot = originator.criarMemento();
        assertEquals(250.0, snapshot.x());
        assertEquals(1280.0, snapshot.largura());
        assertEquals("ESCURO", snapshot.temaAtivo());
        assertEquals("/home/gabriel/documentos", snapshot.ultimoDiretorio());

        // 2. Caretaker persiste snapshot em arquivo .properties
        CaretakerPreferencias.salvarEmArquivoProperties(tempProps, snapshot);
        assertTrue(Files.exists(tempProps));

        // 3. Caretaker carrega de volta
        EstadoJanelaMemento restaurado = CaretakerPreferencias.carregarDeArquivoProperties(tempProps);
        assertEquals(250.0, restaurado.x());
        assertEquals(180.0, restaurado.y());
        assertEquals(1280.0, restaurado.largura());
        assertEquals(800.0, restaurado.altura());
        assertFalse(restaurado.maximizado());
        assertEquals("ESCURO", restaurado.temaAtivo());
        assertEquals("/home/gabriel/documentos", restaurado.ultimoDiretorio());

        // 4. Originator restaura seu estado a partir do snapshot carregado
        OriginatorJanela novoOriginator = new OriginatorJanela();
        novoOriginator.restaurar(restaurado);
        assertEquals(1280.0, novoOriginator.getLargura());
        assertEquals("ESCURO", novoOriginator.getTemaAtivo());

        Files.deleteIfExists(tempProps);
    }
}
