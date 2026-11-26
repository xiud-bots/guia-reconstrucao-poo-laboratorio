# Checklist de Tarefas: Reconstrução Passo a Passo do Projeto

> Este documento contém a sequência ordenada de tarefas para guiar a implementação completa do sistema, dividida em **31 Fases Metodológicas** rigorosamente sequenciadas.

---

## 📋 Resumo do Progresso
- [ ] **Etapa 1: Núcleo de Domínio, Persistência CSV, Inversão de Dependências e Terminal (Fases 1 a 8)**
- [ ] **Etapa 2: Interface Desktop Gráfica em JavaFX, FXML, Concorrência e Data Binding (Fases 9 e 10)**
- [ ] **Etapa 3: Padrões Avançados de Engenharia, Java 21 Moderno, Theming, Navegação e Testabilidade (Fases 11 a 15)**
- [ ] **Etapa 4: Reatividade em Tabelas, Integridade Atômica, Desacoplamento e Regras Temporais (Fases 16 a 19)**
- [ ] **Etapa 5: Inteligência Visual, Reversibilidade e Resiliência Operacional (Fases 20 a 23)**
- [ ] **Etapa 6: Máquina de Estados, Construção Segura, Importação em Lote e Sessão de UI (Fases 24 a 27)**
- [ ] **Etapa 7: Estruturação Hierárquica, Extensibilidade Dinâmica, Pipelines de Regras e Interoperabilidade (Fases 28 a 31)**

---

## Fase 1 — Configuração do Ambiente e Estrutura do Projeto Maven
- [ ] **Tarefa 1.1: Configurar o arquivo `pom.xml` inicial**
  - *Descrição:* Configurar Java 21 LTS (`maven.compiler.source=21`), codificação UTF-8, dependências OpenJFX e JUnit 5 (`junit-jupiter`).
  - *Conceito:* Gerenciamento declarativo de dependências e isolamento de ciclo de vida de compilação.
  - *Guia Relacionado:* `guias/01-modelagem-oo-e-encapsulamento.md`
- [ ] **Tarefa 1.2: Criar a árvore de diretórios padrão Maven**
  - *Descrição:* Criar `src/main/java`, `src/test/java`, `src/main/resources` e subpastas de dados `dados/`.
  - *Critério de Aceitação:* O comando `mvn compile` e `mvn test` devem executar com sucesso gerando a pasta `target/`.

---

## Fase 2 — Modelagem das Entidades Básicas e Encapsulamento
- [ ] **Tarefa 2.1: Modelar a entidade de pessoa/usuário**
  - *Descrição:* Implementar atributos privados (`matricula`, `nome`, `email`/`tipo`), construtor com validação de invariantes e métodos de leitura (getters).
  - *Conceito:* **Encapsulamento e Defensividade.** Nenhuma entidade deve nascer em estado inválido (strings nulas ou vazias devem disparar `IllegalArgumentException`).
  - *Guia Relacionado:* `guias/01-modelagem-oo-e-encapsulamento.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/oo/ExemploEncapsulamentoEValidacao.java`
- [ ] **Tarefa 2.2: Implementar métodos de identidade (`equals`, `hashCode` e `toString`)**
  - *Descrição:* Garantir que a unicidade da pessoa seja determinada pela sua chave de negócio (matrícula).
  - *Critério de Aceitação:* Duas instâncias com a mesma matrícula devem ser consideradas iguais em coleções `Set` ou `List.contains()`.

---

## Fase 3 — Regras Polimórficas e Padrão Strategy
- [ ] **Tarefa 3.1: Criar a interface da regra de cálculo/multa**
  - *Descrição:* Definir a assinatura `BigDecimal calcular(long diasAtraso)` ou equivalente.
  - *Conceito:* **Padrão GoF Strategy.** Substituir cadeias de `if/else` e `switch` por polimorfismo dinâmico.
  - *Guia Relacionado:* `guias/02-heranca-polimorfismo-e-strategy.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/strategy/ExemploStrategyPolimorfico.java`
- [ ] **Tarefa 3.2: Implementar as estratégias concretas**
  - *Descrição:* Implementar as variantes da regra (ex.: regra fixa por dia, regra progressiva com valor inicial maior, e regra de isenção retornando zero).
  - *Critério de Aceitação:* Criar testes unitários para cada estratégia com 0 dias de atraso e com múltiplos dias, validando o cálculo centavo por centavo.

---

## Fase 4 — Hierarquia de Herança das Entidades Principais
- [ ] **Tarefa 4.1: Criar a superclasse abstrata do acervo**
  - *Descrição:* Declarar atributos comuns (`codigo/patrimonio`, `descricao`, `emprestado`), construtores sobrecarregados e injeção do Strategy da regra de cálculo.
  - *Conceito:* **Abstração e Herança.** Impedir instanciação direta da superclasse (`new Item()` é proibido quando a classe é `abstract`).
  - *Guia Relacionado:* `guias/02-heranca-polimorfismo-e-strategy.md`
- [ ] **Tarefa 4.2: Implementar método de template abstrato para prazo**
  - *Descrição:* Declarar método abstrato `public abstract int getPrazoEmprestimoDias()` que força cada subclasse a definir seu próprio prazo regulamentar.
- [ ] **Tarefa 4.3: Implementar as subclasses especializadas**
  - *Descrição:* Criar cada tipo de equipamento concreto herdando da superclasse e repassando ao `super(...)` os dados e sua regra de multa padrão.
- [ ] **Tarefa 4.4: Modelar a classe de relacionamento (Empréstimo)**
  - *Descrição:* Criar a classe que relaciona a pessoa ao item emprestado com data inicial e prevista.
  - *Conceito:* **Composição sobre Herança.** Um empréstimo *tem um* item e *tem uma* pessoa, nunca deve herdar deles.

---

## Fase 5 — Exceções de Domínio Personalizadas
- [ ] **Tarefa 5.1: Criar exceção checada para estado indisponível**
  - *Descrição:* Criar classe herdando de `Exception` (ex.: `ItemJaEmprestadoException`) para sinalizar falhas de negócio esperadas.
  - *Conceito:* **Checked Exceptions vs Unchecked Exceptions.** O compilador deve forçar a captura ou declaração (`throws`), garantindo que a interface do usuário trate a situação explicitamente.
  - *Guia Relacionado:* `guias/05-excecoes-de-dominio-e-propagacao.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/excecao/ExemploExcecaoChecada.java`
- [ ] **Tarefa 5.2: Criar exceções para itens ou registros não encontrados**
  - *Descrição:* Exceções checadas para buscas por identificador ausente.

---

## Fase 6 — Persistência Manual em Arquivos de Texto (CSV)
- [ ] **Tarefa 6.1: Criar interfaces de Repositório (DAO)**
  - *Descrição:* Declarar contratos de acesso a dados (listar, buscar por id, salvar, atualizar).
  - *Conceito:* **Princípio da Inversão de Dependência (DIP).** A regra de negócio não depende de arquivos ou bancos, mas de uma abstração.
  - *Guia Relacionado:* `guias/10-arquitetura-camadas-e-injecao-dependencias.md`
- [ ] **Tarefa 6.2: Implementar leitor CSV com `BufferedReader`**
  - *Descrição:* Ler arquivo linha a linha com `try-with-resources`, descartar linhas em branco e comentários iniciados por `#`, e fazer parse dos campos com `split(";")`.
  - *Guia Relacionado:* `guias/04-persistencia-csv-sem-frameworks.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/csv/ExemploLeituraGravacaoCSV.java`
- [ ] **Tarefa 6.3: Implementar gravador CSV com `BufferedWriter`**
  - *Descrição:* Gravar novos registros formatando campos delimitados por `;` com `newLine()` e `flush()`.
  - *Critério de Aceitação:* O arquivo de texto resultante deve ser legível por editores e ferramentas de planilha sem corrupção de caracteres UTF-8.

---

## Fase 7 — Camada de Serviço e Inversão de Dependências
- [ ] **Tarefa 7.1: Criar a classe de serviço com injeção de dependências por construtor**
  - *Descrição:* O serviço deve receber as instâncias de repositório em seu construtor, sem nunca executar `new RepositorioArquivo()` dentro de si.
  - *Guia Relacionado:* `guias/10-arquitetura-camadas-e-injecao-dependencias.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/arquitetura/ExemploInjecaoDependenciasCamadas.java`
- [ ] **Tarefa 7.2: Implementar proteção de coleções contra mutação externa**
  - *Descrição:* Métodos que retornam listas (ex.: `listarItens()`, `listarEmprestimos()`) devem retornar `Collections.unmodifiableList(...)`.
  - *Conceito:* **Cópia Defensiva e Encapsulamento.** Evita que consumidores externos manipulem o estado interno do serviço via `lista.clear()` ou `lista.add()`.
  - *Guia Relacionado:* `guias/03-colecoes-e-defensive-copying.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/colecoes/ExemploColecoesProtegidas.java`
- [ ] **Tarefa 7.3: Implementar filtros, buscas e acumuladores**
  - *Descrição:* Criar métodos para buscar por critério, filtrar apenas itens disponíveis e acumular o somatório total de multas em atraso com Streams API.

---

## Fase 8 — Testes Automatizados e Execução no Terminal (Fim da Etapa 1)
- [ ] **Tarefa 8.1: Escrever suíte de testes com JUnit 5**
  - *Descrição:* Testar isoladamente: estratégias de multa, invariantes, empréstimo com sucesso, disparo da exceção checada ao tentar emprestar duplicado, e devolução.
  - *Exemplo de Apoio:* `src/test/java/br/edu/ifpb/didatico/exemplos/ExemplosDidaticosTest.java`
- [ ] **Tarefa 8.2: Implementar classe de demonstração no Terminal**
  - *Descrição:* Implementar o roteiro oficial de testes imprimindo o acervo, executando um empréstimo, demonstrando a captura da exceção e exibindo a devolução.
- [ ] **Tarefa 8.3: Criar tag Git e script de execução**
  - *Descrição:* Gerar tag `v1.0-etapa1-modelo` e script `executar-etapa1.sh`.

---

## Fase 9 — Arquitetura da Interface Gráfica JavaFX e FXML
- [ ] **Tarefa 9.1: Adicionar dependências JavaFX e plugin ao `pom.xml`**
  - *Descrição:* Incluir `javafx-controls`, `javafx-fxml`, `javafx-graphics`, `javafx-base` e o `javafx-maven-plugin`.
- [ ] **Tarefa 9.2: Implementar classe de ciclo de vida `Application` e bootstrap**
  - *Descrição:* Criar classe herdando de `javafx.application.Application` carregando o arquivo `.fxml` principal, e um `AppLauncher` auxiliar para contornar restrições de inicialização em classpath aberto.
  - *Guia Relacionado:* `guias/06-javafx-arquitetura-fxml-e-mvc.md`
- [ ] **Tarefa 9.3: Modelar os arquivos declarativos FXML**
  - *Descrição:* Criar a interface mestre (`JanelaPrincipal.fxml`) contendo tabelas e rodapé de métricas, desacoplada do código Java.

---

## Fase 10 — Componentes Avançados, Modais, Concorrência e Estilos (Fim da Etapa 2)
- [ ] **Tarefa 10.1: Configurar `TableView` com Cell Value Factories e Badges**
  - *Descrição:* Mapear atributos das entidades nas colunas da tabela e customizar células para exibir indicadores visuais (ex.: verde para disponível, vermelho para emprestado).
  - *Guia Relacionado:* `guias/07-javafx-tabelas-e-cell-factories.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/javafx/ExemploTabelaEModalJavaFX.java`
- [ ] **Tarefa 10.2: Implementar janelas modais separadas**
  - *Descrição:* Criar modais bloqueantes com `Modality.APPLICATION_MODAL` para realização de novos empréstimos e cadastro de itens no acervo.
  - *Guia Relacionado:* `guias/08-javafx-modais-e-caixas-de-dialogo.md`
- [ ] **Tarefa 10.3: Tratar exceções de domínio com caixas de diálogo `Alert`**
  - *Descrição:* Ao chamar o serviço a partir do controlador JavaFX, envolver a chamada em bloco `try/catch` e exibir a mensagem de erro através de `Alert(AlertType.ERROR)`.
- [ ] **Tarefa 10.4: Garantir Concorrência e Thread Safety em Operações Longas**
  - *Descrição:* Utilizar `javafx.concurrent.Task<V>` e `Platform.runLater()` para operações assíncronas que leem arquivos ou calculam relatórios, vinculando o progresso a `ProgressBar`.
  - *Guia Relacionado:* `guias/09-javafx-concorrencia-e-thread-safety.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/javafx/ExemploConcorrenciaJavaFX.java`
- [ ] **Tarefa 10.5: Implementar Data Binding Reativo para Validação de Telas**
  - *Descrição:* Desabilitar botões de ação enquanto os campos estiverem em branco via `button.disableProperty().bind(...)`, e conectar sliders e labels reativos.
  - *Guia Relacionado:* `guias/11-javafx-properties-e-data-binding.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/javafx/ExemploDataBindingJavaFX.java`
- [ ] **Tarefa 10.6: Criar folha de estilos CSS e script de execução da Etapa 2**
  - *Descrição:* Desenvolver arquivo `styles.css` customizando botões, tabelas e cabeçalhos, e criar script `executar-etapa2.sh`.
- [ ] **Tarefa 10.7: Gerar tag Git e branch de entrega**
  - *Descrição:* Registrar tag `v2.0-etapa2-javafx` e branch `etapa-2-javafx`.

---

## Fase 11 — Modern Java 21 LTS: Records, Sealed Types e Pattern Matching
- [ ] **Tarefa 11.1: Modelar DTOs e Value Objects com Records e Construtor Compacto**
  - *Descrição:* Substituir DTOs verbosos por `record`, aplicando validação de invariantes no construtor compacto e garantindo imutabilidade defensiva profunda.
  - *Guia Relacionado:* `guias/12-java21-records-pattern-matching-e-sealed-types.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/moderno/ExemploJavaModernoRecordESealed.java`
- [ ] **Tarefa 11.2: Implementar Hierarquias Seladas (`sealed` interfaces e classes)**
  - *Descrição:* Fechar a hierarquia de resultados de operações ou estados de negócio com a cláusula `permits`.
- [ ] **Tarefa 11.3: Tratar resultados com Pattern Matching Switch e Guards (`when`)**
  - *Descrição:* Substituir cascades de `if/else` ou switches tradicionais com cláusula `default` por avaliação exaustiva de padrões do Java 21.

---

## Fase 12 — Estilização Profissional com CSS, Pseudo-Classes e Temas
- [ ] **Tarefa 12.1: Eliminar 100% de estilos inline (`setStyle`) dos controladores**
  - *Descrição:* Migrar todas as regras de formatação, cores e fontes para arquivos `.css` dedicados em `src/main/resources/css/`.
  - *Guia Relacionado:* `guias/13-javafx-css-seletores-e-temas-dinamicos.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/javafx/ExemploCssETemasJavaFX.java`
- [ ] **Tarefa 12.2: Implementar Pseudo-Classes Customizadas em Tabelas**
  - *Descrição:* Usar `PseudoClass.getPseudoClass(...)` e `node.pseudoClassStateChanged(...)` para indicar atrasos e status dinâmicos em linhas de `TableView`.
- [ ] **Tarefa 12.3: Implementar Alternância Dinâmica de Temas (Light / Dark Mode)**
  - *Descrição:* Permitir ao usuário alternar instantaneamente entre `tema-claro.css` e `tema-escuro.css` manipulando `scene.getStylesheets()`.

---

## Fase 13 — Arquitetura de Navegação, Padrão Shell e Diálogos Tipados
- [ ] **Tarefa 13.1: Implementar o Layout Mestre Shell com Container Dinâmico**
  - *Descrição:* Estruturar a janela principal em `BorderPane`, mantendo sidebar fixa e trocando o painel `center` sob demanda.
  - *Guia Relacionado:* `guias/14-javafx-navegacao-e-troca-de-telas.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/javafx/ExemploNavegacaoJavaFX.java`
- [ ] **Tarefa 13.2: Implementar Injeção Tipada de Parâmetros entre Controladores**
  - *Descrição:* Transmitir objetos selecionados entre a visão mestre e a visão de detalhes via método tipado (`inicializarComDados(item)`).
- [ ] **Tarefa 13.3: Implementar Diálogos Modais Tipados com `Dialog<R>`**
  - *Descrição:* Substituir modais com estados compartilhados por instâncias de `Dialog<R>` com `setResultConverter` retornando `Optional<R>`.

---

## Fase 14 — Validação Reativa de Formulários e Safe Parsing (UX/UI)
- [ ] **Tarefa 14.1: Implementar Conversão Segura Preventiva (Safe Parsing)**
  - *Descrição:* Criar métodos estáticos defensivos que convertem entradas de texto para `BigDecimal` e inteiros retornando `Optional`, prevenindo `NumberFormatException`.
  - *Guia Relacionado:* `guias/15-javafx-validacao-formularios-e-feedback-visual.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/javafx/ExemploValidacaoFormularioJavaFX.java`
- [ ] **Tarefa 14.2: Implementar Feedback Visual Amigável (Pristine vs. Dirty)**
  - *Descrição:* Validar visualmente os campos apenas após a perda de foco (`focusedProperty()`), evitando telas que nascem todas vermelhas.
- [ ] **Tarefa 14.3: Conectar Validação Global ao Botão de Submissão**
  - *Descrição:* Criar `BooleanBinding` composto para manter o botão `Salvar` desabilitado até que todos os campos estejam matematicamente íntegros.

---

## Fase 15 — Testabilidade Arquitetural com Fakes em Memória
- [ ] **Tarefa 15.1: Criar Repositório Fake em Memória para Testes Unitários**
  - *Descrição:* Implementar o contrato `IRepositorio` com `ConcurrentHashMap`, permitindo suítes de teste sem I/O de disco.
  - *Guia Relacionado:* `guias/16-testes-unitarios-e-testabilidade-com-fakes.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/teste/ExemploTestabilidadeComFake.java`
- [ ] **Tarefa 15.2: Testar Regras de Negócio Críticas em Microssegundos**
  - *Descrição:* Escrever testes automatizados JUnit 5 cobrindo regras de limites máximos por pessoa, colisões de estado e reversão de operações.

---

## Fase 16 — Filtros Dinâmicos, Ordenação com SortedList e Ações em TableView
- [ ] **Tarefa 16.1: Implementar o Pipeline Reativo em Tabelas**
  - *Descrição:* Envelopar a lista mestre em `FilteredList` e `SortedList`, conectando `sortedList.comparatorProperty().bind(tabela.comparatorProperty())` para preservar a ordenação ao filtrar.
  - *Guia Relacionado:* `guias/17-javafx-filtros-ordenacao-e-acoes-em-tabelas.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/javafx/ExemploFiltroEOrdenacaoJavaFX.java`
- [ ] **Tarefa 16.2: Combinar Filtro Textual e Filtro de Estado (ComboBox)**
  - *Descrição:* Construir predicado composto dinâmico atualizado via listeners nos controles de entrada da UI, sem nunca limpar ou repovoar a lista manualmente.
- [ ] **Tarefa 16.3: Criar Coluna de Ações com Botões Customizados em TableCell**
  - *Descrição:* Implementar botões (ex.: Devolver, Excluir) dentro das linhas da tabela, associando diálogos de confirmação (`Alert.AlertType.CONFIRMATION`) antes de executar mutações.

---

## Fase 17 — Persistência Segura com Gravação Atômica, Backup e Concorrência
- [ ] **Tarefa 17.1: Implementar o Padrão Safe Atomic Write em Repositório CSV**
  - *Descrição:* Escrever registros inicialmente em arquivo temporário (`.tmp`) no mesmo diretório e substituir o arquivo original via `Files.move(..., ATOMIC_MOVE, REPLACE_EXISTING)`.
  - *Guia Relacionado:* `guias/18-persistencia-atomica-backup-e-locks.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/csv/ExemploPersistenciaAtomicaComLock.java`
- [ ] **Tarefa 17.2: Implementar Rotina de Backup Automático (.bak) e Recuperação**
  - *Descrição:* Gerar cópia de segurança antes de sobrescrever o arquivo oficial e prover rotina para restauração em caso de corrupção ou apagamento acidental.
- [ ] **Tarefa 17.3: Orquestrar Concorrência com `ReentrantReadWriteLock`**
  - *Descrição:* Proteger o repositório permitindo leituras simultâneas (`readLock`) e isolamento estrito durante gravações (`writeLock`).

---

## Fase 18 — Barramento de Eventos (EventBus) e Desacoplamento entre Telas
- [ ] **Tarefa 18.1: Implementar o `AppEventBus` Thread-Safe**
  - *Descrição:* Criar barramento centralizado em memória com método `inscrever(tipoEvento, consumidor)` retornando token de cancelamento para prevenir vazamento de memória (Memory Leaks).
  - *Guia Relacionado:* `guias/19-event-bus-e-comunicacao-desacoplada-entre-controladores.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/javafx/ExemploEventBusDesacoplado.java`
- [ ] **Tarefa 18.2: Modelar Eventos de Domínio com Records Imutáveis do Java 21**
  - *Descrição:* Declarar eventos de notificação como records (ex.: `ItemCadastradoEvent`, `ItemExcluidoEvent`), garantindo que o estado não seja corrompido entre consumidores.
- [ ] **Tarefa 18.3: Integrar Despacho Automático na UI Thread**
  - *Descrição:* Fazer com que o barramento verifique `Platform.isFxApplicationThread()` e despache notificações via `Platform.runLater()` automaticamente.

---

## Fase 19 — Manipulação de Datas Modernas (`java.time`) e Regras Temporais
- [ ] **Tarefa 19.1: Integrar `DatePicker` com Conversor Formatado em Padrão Brasileiro**
  - *Descrição:* Configurar `StringConverter<LocalDate>` para exibir e receber datas no formato `dd/MM/yyyy`, tratando parsing defensivamente sem estourar exceções.
  - *Guia Relacionado:* `guias/20-javafx-datas-datepicker-e-regras-temporais.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/javafx/ExemploDataPickerERegrasTemporaisJavaFX.java`
- [ ] **Tarefa 19.2: Bloquear Datas Inválidas no Calendário com `DayCellFactory`**
  - *Descrição:* Desabilitar visualmente no calendário datas anteriores ao início do empréstimo e destacar finais de semana.
- [ ] **Tarefa 19.3: Implementar Cálculo de Atrasos, Dias Úteis e Multas Regulamentares**
  - *Descrição:* Criar utilitário temporal com `ChronoUnit.DAYS.between()`, cálculo de dias úteis e projeção de penalidades com carência regulamentar.

---

## Fase 20 — Dashboards Executivos, Métricas Agregadas e Gráficos (`PieChart` e `BarChart`)
- [ ] **Tarefa 20.1: Implementar Agregações com Streams API e Coletores Avançados**
  - *Descrição:* Criar métodos no serviço analítico utilizando `Collectors.groupingBy()`, `Collectors.counting()`, `Collectors.reducing()` e `Collectors.partitioningBy()` para consolidar resumos e taxas de ocupação sem laços imperativos.
  - *Guia Relacionado:* `guias/21-javafx-dashboards-metricas-e-graficos.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/javafx/ExemploDashboardGraficosJavaFX.java`
- [ ] **Tarefa 20.2: Construir Gráfico de Setores (`PieChart`) com Proporções e Tooltips**
  - *Descrição:* Mapear dados agregados em instâncias de `PieChart.Data`, instalando `Tooltip` com percentuais calculados e formatação defensiva para coleções vazias.
- [ ] **Tarefa 20.3: Construir Gráfico de Barras (`BarChart`) para Comparação Patrimonial**
  - *Descrição:* Configurar `CategoryAxis`, `NumberAxis` e `XYChart.Series` para comparar investimentos e quantitativos entre categorias do acervo.

---

## Fase 21 — Padrão GoF Command, Histórico Reversível (Undo / Redo) e Atalhos de Teclado
- [ ] **Tarefa 21.1: Modelar o Contrato `IComando` e Comandos Reversíveis Autocontidos**
  - *Descrição:* Criar interface com `executar()`, `desfazer()` e `getDescricao()`, implementando comandos de cadastro, remoção com preservação de índice e alteração de status.
  - *Guia Relacionado:* `guias/22-padrao-command-undo-redo-e-historico.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/javafx/ExemploCommandUndoRedoJavaFX.java`
- [ ] **Tarefa 21.2: Implementar Gerenciador de Histórico com Pilhas Duplas (`Deque`) e Guarda de Memória**
  - *Descrição:* Orquestrar pilhas `ArrayDeque` para Undo e Redo, limpando Redo a cada nova ação e limitando o tamanho da pilha em memória RAM.
- [ ] **Tarefa 21.3: Conectar Botões com Data Binding e Atalhos Universais no Palco**
  - *Descrição:* Conectar `btnDesfazer.disableProperty()` a `podeDesfazerProperty().not()`, e registrar aceleradores `KeyCodeCombination` com `KeyCombination.SHORTCUT_DOWN` para `Ctrl+Z` e `Ctrl+Y`.

---

## Fase 22 — Exportação de Relatórios, Recibos Formatados e Diálogos Nativos (`FileChooser`)
- [ ] **Tarefa 22.1: Implementar Formatador Textual Monoespaçado Determinístico**
  - *Descrição:* Construir gerador de comprovantes com molduras divisórias em ASCII, colunas em largura fixa (`String.format`), sumário financeiro com `BigDecimal` e chave criptográfica de integridade SHA-256.
  - *Guia Relacionado:* `guias/23-exportacao-relatorios-recibos-e-filechooser.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/javafx/ExemploExportacaoRelatoriosEFileChooserJavaFX.java`
- [ ] **Tarefa 22.2: Implementar Exportação Tabular em CSV**
  - *Descrição:* Exportar dados de movimentação em formato delimitado por `;` legível por planilhas e editores com encoding UTF-8.
- [ ] **Tarefa 22.3: Integrar Diálogo Nativo `FileChooser` com Cancelamento Gracioso**
  - *Descrição:* Configurar filtros de extensão, sugerir nomes com timestamp, lembrar último diretório e proteger a aplicação contra retornos nulos (`arquivoDestino == null`).

---

## Fase 23 — Resiliência Operacional, Tratamento Global de Falhas e Logs de Diagnóstico
- [ ] **Tarefa 23.1: Instalar Tratador Global de Falhas (`UncaughtExceptionHandler`)**
  - *Descrição:* Configurar `Thread.setDefaultUncaughtExceptionHandler` no ponto de entrada da aplicação para interceptar qualquer falha não prevista na JVM.
  - *Guia Relacionado:* `guias/24-resiliencia-global-uncaught-exceptions-e-logs.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/javafx/ExemploTratamentoGlobalErrosJavaFX.java`
- [ ] **Tarefa 23.2: Despachar Diálogo de Erro Expansível na UI Thread**
  - *Descrição:* Apresentar mensagem amigável para o usuário comum e disponibilizar o rastreamento completo de pilha (Stack Trace) em painel colapsável (`TextArea`), garantindo o despacho via `Platform.runLater()`.
- [ ] **Tarefa 23.3: Gravar Logs de Diagnóstico em Disco com Auditoria Concorrente**
  - *Descrição:* Registrar append-only em `app-erros.log` com timestamp ISO-8601, nome da thread causadora, classe da exceção e rastreamento completo para análise pós-morte.

---

## Fase 24 — Padrão GoF State e Ciclo de Vida de Entidades de Domínio
- [ ] **Tarefa 24.1: Modelar a interface de estado `IEstadoRecurso` com métodos default**
  - *Descrição:* Definir ações do ciclo de vida (`emprestar`, `devolver`, `enviarParaManutencao`, `concluirManutencao`, `baixarPatrimonio`) com rejeição padrão via `TransicaoEstadoInvalidaException`.
  - *Guia Relacionado:* `guias/25-padrao-state-maquina-de-estados-e-ciclo-de-vida.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/state/ExemploStateCicloVida.java`
- [ ] **Tarefa 24.2: Implementar Estados Concretos e Entidade de Contexto**
  - *Descrição:* Criar estados `Disponivel`, `Emprestado`, `EmManutencao` e `Baixado` (terminal), e a entidade de contexto delegando todas as operações para o estado ativo.
- [ ] **Tarefa 24.3: Conectar a Máquina de Estados a Botões Reativos da UI JavaFX**
  - *Descrição:* Vincular a habilitação de botões (`btnEmprestar.setDisable(!estado.permiteEmprestimo())`) e badges visuais ao estado da entidade selecionada na `TableView`.

---

## Fase 25 — Padrão GoF Builder, Invariantes e Formulários Wizard
- [ ] **Tarefa 25.1: Modelar Entidade com Construtor Privado e Builder Estático Aninhado**
  - *Descrição:* Estruturar classe de domínio com múltiplos atributos (obrigatórios e opcionais) garantindo que ninguém consiga instanciá-la sem passar pelo Builder.
  - *Guia Relacionado:* `guias/26-padrao-builder-invariantes-e-formularios-wizard.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/builder/ExemploBuilderInvariantesEWizard.java`
- [ ] **Tarefa 25.2: Implementar Fluent API e Validação Atômica no `.build()`**
  - *Descrição:* Encadear métodos `.comIdentificacao()`, `.comValor()`, `.alocadoEm()` e validar todas as invariantes e regras de consistência mútua no método `.build()`.
- [ ] **Tarefa 25.3: Integrar o Builder a Formulários Multi-etapas (*Wizards*) no JavaFX**
  - *Descrição:* Acumular dados preenchidos em diferentes abas ou telas sequenciais na instância do Builder antes de construir o objeto de domínio de forma atômica no botão de conclusão.

---

## Fase 26 — Padrão GoF Template Method e Processamento em Lote (Batch Import)
- [ ] **Tarefa 26.1: Criar a Superclasse Abstrata `ImportadorLoteAbstrato<T>`**
  - *Descrição:* Definir o esqueleto invariante do algoritmo de importação em método `final`, abrindo arquivo, iterando sobre linhas e gerenciando persistência e auditoria.
  - *Guia Relacionado:* `guias/27-padrao-template-method-e-importacao-em-lote.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/templatemethod/ExemploTemplateMethodImportacaoLote.java`
- [ ] **Tarefa 26.2: Implementar Hooks Abstratos para Parse e Validação de Domínio**
  - *Descrição:* Delegar para subclasses especializadas os métodos `validarCabecalho()`, `parseLinha()` e `validarEntidade()`.
- [ ] **Tarefa 26.3: Coletar Falhas Linha a Linha de Forma Resiliente e Conectar com JavaFX**
  - *Descrição:* Tratar erros de parsing individuais sem abortar o processamento do restante do arquivo, catalogando `FalhaLinha` e emitindo callbacks para `ProgressBar` em `Task<V>`.

---

## Fase 27 — Padrão GoF Memento, Persistência de Preferências e Sessão de Usuário
- [ ] **Tarefa 27.1: Modelar o Snapshot Imutável `EstadoJanelaMemento` como Record Java 21**
  - *Descrição:* Capturar coordenadas (X, Y), largura, altura, estado maximizado, tema ativo e último diretório aberto.
  - *Guia Relacionado:* `guias/28-padrao-memento-preferencias-e-sessao-javafx.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/memento/ExemploMementoEPreferenciasJavaFX.java`
- [ ] **Tarefa 27.2: Implementar o `CaretakerPreferencias` Nativo**
  - *Descrição:* Salvar e restaurar snapshots usando `java.util.prefs.Preferences` da plataforma Java e/ou arquivos `.properties` no diretório do usuário sem bibliotecas externas.
- [ ] **Tarefa 27.3: Integrar ao Ciclo de Vida do JavaFX (`start()` e `setOnCloseRequest()`)**
  - *Descrição:* Restaurar layout e tema ao iniciar a aplicação e gravar snapshot automaticamente ao fechar o palco principal.

---

## Fase 28 — Padrão GoF Composite e Hierarquias de Domínio no JavaFX (`TreeView` e `TreeCell`)
- [ ] **Tarefa 28.1: Modelar a Hierarquia de Domínio em Árvore com Padrão Composite**
  - *Descrição:* Criar superclasse abstrata `ComponentePatrimonial`, elemento folha `ItemFolha` e nó composto `CategoriaComposta` com validação defensiva anti-loop e coleções imutáveis.
  - *Guia Relacionado:* `guias/29-padrao-composite-hierarquias-de-dominio-e-treeview.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/composite/ExemploCompositeETreeView.java`
- [ ] **Tarefa 28.2: Implementar Operações Recursivas Uniformes de Agregação**
  - *Descrição:* Desenvolver métodos polimórficos `calcularValorTotal()` e `contarItensFisicos()` executados recursivamente sem `instanceof`.
- [ ] **Tarefa 28.3: Integrar com `TreeView` e `TreeCell` Customizado**
  - *Descrição:* Mapear recursivamente para `TreeItem<ComponentePatrimonial>`, estilizando nós compostos (pastas) e folhas (itens) com badges e valores monetários.

---

## Fase 29 — Padrão GoF Decorator e Composição Dinâmica de Tarifas e Regras em UI
- [ ] **Tarefa 29.1: Definir Contrato do Componente Precificável e Implementação Base**
  - *Descrição:* Criar interface `IServicoPrecificavel` e componente base `LocacaoBase` com cálculo de custo base e lista de discriminativos.
  - *Guia Relacionado:* `guias/30-padrao-decorator-extensao-dinamica-e-tarifacao.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/decorator/ExemploDecoratorTarifacaoDinamica.java`
- [ ] **Tarefa 29.2: Implementar Decorador Abstrato e Decoradores Concretos Especializados**
  - *Descrição:* Construir `ServicoOpcionalDecorator`, `SeguroAvariaDecorator`, `HigienizacaoTecnicaDecorator` e `SuporteTecnico24hDecorator` acumulando custos e discriminativo transparente.
- [ ] **Tarefa 29.3: Integrar com Seleção Interativa de Opcionais na UI JavaFX**
  - *Descrição:* Conectar CheckBoxes para empilhar decoradores dinamicamente e recalcular totalizador e discriminativo via Data Binding reativo.

---

## Fase 30 — Padrão GoF Chain of Responsibility e Pipelines de Auditoria e Validação
- [ ] **Tarefa 30.1: Modelar o Manipulador Abstrato `ValidadorRequisicao` com Modos Duplos**
  - *Descrição:* Implementar suporte a auditoria completa (coleta de todas as violações em `List<ViolacaoRegra>`) e modo Fail-Fast com exceção checada `RegraNegocioVioladaException`.
  - *Guia Relacionado:* `guias/31-padrao-chain-of-responsibility-pipelines-e-autorizacao.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/chain/ExemploChainOfResponsibilityValidacao.java`
- [ ] **Tarefa 30.2: Implementar Elos Concretos Especializados da Cadeia**
  - *Descrição:* Construir validadores independentes para inadimplência/atraso, limite quantitativo de cota, teto financeiro patrimonial e equipamento restrito por perfil.
- [ ] **Tarefa 30.3: Integrar ao Checklist Visual de Conformidade no JavaFX**
  - *Descrição:* Desenvolver painel reativo com feedback visual verde/vermelho por critério regulamentar antes de liberar ações transacionais.

---

## Fase 31 — Padrão GoF Adapter e Interoperabilidade com Fontes Legadas e Externas
- [ ] **Tarefa 31.1: Modelar a Interface Canônica Local do Domínio (Target)**
  - *Descrição:* Definir contrato `IItemCatalogoLocal` com métodos idiomáticos limpos desacoplados de fornecedores externos.
  - *Guia Relacionado:* `guias/32-padrao-adapter-interoperabilidade-e-sistemas-legados.md`
  - *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/adapter/ExemploAdapterIntegracaoLegada.java`
- [ ] **Tarefa 31.2: Implementar o Adaptador de Objeto (`AdaptadorItemLegado`)**
  - *Descrição:* Encapsular `LegacyVendorItemDTO`, convertendo códigos primitivos, sanitizando texto, traduzindo centavos para `BigDecimal` e mapeando flags numéricas para booleanos.
- [ ] **Tarefa 31.3: Conectar Catálogo Adaptado à `TableView` do JavaFX**
  - *Descrição:* Exibir os dados legados adaptados na tabela consumindo exclusivamente o contrato canônico, preservando o isolamento arquitetural da interface gráfica.
