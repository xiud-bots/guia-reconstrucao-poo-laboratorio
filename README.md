# Blueprint Didático: Guia de Reconstrução em Java 21 e JavaFX

> **Repositório Educacional de Referência Metodológica, Padrões Arquiteturais, Testes Automatizados e Exemplos Práticos de Apoio.**

---

## 1. Propósito e Filosofia do Repositório

Este repositório foi concebido como um **Master Blueprint (Guia Mestre de Construção e Reconstrução)** para desenvolvedores e estudantes sob direção de `@domaragao` que precisam projetar, implementar ou reconstruir sistemas desktop orientados a objetos em **Java 21 LTS** e **JavaFX**.

### ⚠️ Regra de Isolamento Pedagógico
Seguindo as melhores práticas pedagógicas e as diretrizes estabelecidas:
- **Nenhum trecho de código específico do domínio do sistema real está presente neste repositório** (não há classes de acervo de laboratório, itens específicos ou regras de empréstimo do domínio real).
- O repositório contém **exclusivamente**:
  1. **Lista de Tarefas Estruturada (`CHECKLIST_TAREFAS.md`)** com ordem de dependência em 39 fases, critérios de aceitação e armadilhas a evitar.
  2. **Cadernos Didáticos e Metodológicos (`guias/`)** com 40 cadernos cobrindo modelagem pura, persistência manual CSV, JavaFX completo, concorrência, data binding reativo, Java 21 moderno (Records e Sealed Types), theming CSS, navegação com padrão Shell, validação reativa, testabilidade com Fakes, reatividade de tabelas com FilteredList/SortedList, persistência atômica com backup e locks, barramento de eventos (EventBus), manipulação temporal com `java.time` e DatePicker, dashboards analíticos com gráficos (PieChart/BarChart), padrão Command com Undo/Redo, exportação de relatórios/recibos formatados com FileChooser, resiliência com UncaughtExceptionHandler global, máquina de estados finita com padrão State, construção segura com padrão Builder e Wizards, importação resiliente em lote com Template Method, persistência de preferências de sessão com padrão Memento, hierarquias de domínio e TreeView com padrão Composite, composição dinâmica de serviços e tarifação com padrão Decorator, pipelines de validação e auditoria com padrão Chain of Responsibility, interoperabilidade de sistemas legados com padrão Adapter, orquestração de múltiplos microsserviços e compensação com padrão Facade, coordenação desacoplada de formulários e regras visuais com padrão Mediator, carregamento preguiçoso e cache transparente com padrão Proxy, separação de domínio limpo e ponte reativa com padrão Observer, criação polimórfica e famílias de componentes desacoplados com padrão Abstract Factory, operações polimórficas externas e Double Dispatch com padrão Visitor, otimização de memória Heap e pools de estados compartilhados com padrão Flyweight, e navegação sob demanda para grandes volumes com padrão Iterator e paginação virtual em TableView.
  3. **Exemplos de Código Compiláveis em Java 21 (`src/main/java/...`)** demonstrando a sintaxe e o uso correto das APIs nativas do JDK e OpenJFX.
  4. **Suíte de Testes Automatizados (`src/test/java/...`)** com JUnit 5 validando invariantes, imutabilidade, polimorfismo, contratos desacoplados, persistência atômica, eventos tipados, regras temporais, fakes em memória, agregações analíticas, padrão Command, resiliência global, máquina de estados finita, builder com invariantes atômicas, importação em lote, preferências com Memento, hierarquias Composite recursivas, decoradores cumulativos em faturas, pipelines de auditoria com Chain of Responsibility, adaptadores de integração legada, orquestração de fachada com rollback, decisões de mediação em formulários, carregamento virtual e reuso em cache via proxy, emissão de eventos puros de domínio com ponte reativa, famílias polimórficas com Abstract Factory, operações externas com Visitor e Double Dispatch, economia de instâncias e reuso de memória com Flyweight, e controle de limites e paginação bidirecional sob demanda com Iterator (33 testes automatizados).

---

## 2. Estrutura do Repositório

```text
guia-reconstrucao-poo-laboratorio/
├── README.md                      # Visão geral, filosofia e mapa de estudos
├── CHECKLIST_TAREFAS.md           # Roteiro passo a passo com critérios de conclusão (39 Fases)
├── pom.xml                        # Configuração Maven com Java 21, OpenJFX, JUnit 5 e Plugins
├── guias/                         # Cadernos conceituais detalhados (40 Guias Didáticos)
│   ├── 01-modelagem-oo-e-encapsulamento.md
│   ├── 02-heranca-polimorfismo-e-strategy.md
│   ├── 03-colecoes-e-defensive-copying.md
│   ├── 04-persistencia-csv-sem-frameworks.md
│   ├── 05-excecoes-de-dominio-e-propagacao.md
│   ├── 06-javafx-arquitetura-fxml-e-mvc.md
│   ├── 07-javafx-tabelas-e-cell-factories.md
│   ├── 08-javafx-modais-e-caixas-de-dialogo.md
│   ├── 09-javafx-concorrencia-e-thread-safety.md
│   ├── 10-arquitetura-camadas-e-injecao-dependencias.md
│   ├── 11-javafx-properties-e-data-binding.md
│   ├── 12-java21-records-pattern-matching-e-sealed-types.md
│   ├── 13-javafx-css-seletores-e-temas-dinamicos.md
│   ├── 14-javafx-navegacao-e-troca-de-telas.md
│   ├── 15-javafx-validacao-formularios-e-feedback-visual.md
│   ├── 16-testes-unitarios-e-testabilidade-com-fakes.md
│   ├── 17-javafx-filtros-ordenacao-e-acoes-em-tabelas.md
│   ├── 18-persistencia-atomica-backup-e-locks.md
│   ├── 19-event-bus-e-comunicacao-desacoplada-entre-controladores.md
│   ├── 20-javafx-datas-datepicker-e-regras-temporais.md
│   ├── 21-javafx-dashboards-metricas-e-graficos.md
│   ├── 22-padrao-command-undo-redo-e-historico.md
│   ├── 23-exportacao-relatorios-recibos-e-filechooser.md
│   ├── 24-resiliencia-global-uncaught-exceptions-e-logs.md
│   ├── 25-padrao-state-maquina-de-estados-e-ciclo-de-vida.md
│   ├── 26-padrao-builder-invariantes-e-formularios-wizard.md
│   ├── 27-padrao-template-method-e-importacao-em-lote.md
│   ├── 28-padrao-memento-preferencias-e-sessao-javafx.md
│   ├── 29-padrao-composite-hierarquias-de-dominio-e-treeview.md
│   ├── 30-padrao-decorator-extensao-dinamica-e-tarifacao.md
│   ├── 31-padrao-chain-of-responsibility-pipelines-e-autorizacao.md
│   ├── 32-padrao-adapter-interoperabilidade-e-sistemas-legados.md
│   ├── 33-padrao-facade-fachada-de-subsistemas-e-gateway-desktop.md
│   ├── 34-padrao-mediator-orquestracao-de-componentes-javafx.md
│   ├── 35-padrao-proxy-lazy-loading-e-cache-de-recursos.md
│   ├── 36-padrao-observer-dominio-e-ponte-reativa-javafx.md
│   ├── 37-padrao-factory-method-e-abstract-factory-familias-polimorficas.md
│   ├── 38-padrao-visitor-operacoes-polimorficas-e-double-dispatch.md
│   ├── 39-padrao-flyweight-otimizacao-memoria-e-estados-compartilhados.md
│   └── 40-padrao-iterator-paginacao-virtual-e-navegacao-sob-demanda.md
└── src/
    ├── main/
    │   ├── java/br/edu/ifpb/didatico/exemplos/
    │   │   ├── oo/                    # Encapsulamento, validação e invariantes
    │   │   │   └── ExemploEncapsulamentoEValidacao.java
    │   │   ├── strategy/              # Polimorfismo GoF Strategy
    │   │   │   └── ExemploStrategyPolimorfico.java
    │   │   ├── colecoes/              # Cópia defensiva e Stream API
    │   │   │   └── ExemploColecoesProtegidas.java
    │   │   ├── csv/                   # I/O manual, atomic move, backups e locks
    │   │   │   ├── ExemploLeituraGravacaoCSV.java
    │   │   │   └── ExemploPersistenciaAtomicaComLock.java
    │   │   ├── excecao/               # Checked Exceptions de domínio
    │   │   │   └── ExemploExcecaoChecada.java
    │   │   ├── arquitetura/           # Camadas desacopladas e Pure Dependency Injection
    │   │   │   └── ExemploInjecaoDependenciasCamadas.java
    │   │   ├── moderno/               # Java 21: Records, Sealed Types e Pattern Matching
    │   │   │   └── ExemploJavaModernoRecordESealed.java
    │   │   ├── teste/                 # Testabilidade arquitetural com Fake Repository em RAM
    │   │   │   └── ExemploTestabilidadeComFake.java
    │   │   ├── state/                 # Máquina de estados finita GoF State
    │   │   │   └── ExemploStateCicloVida.java
    │   │   ├── builder/               # GoF Builder imutável com invariantes atômicas
    │   │   │   └── ExemploBuilderInvariantesEWizard.java
    │   │   ├── templatemethod/        # GoF Template Method para processamento em lote
    │   │   │   └── ExemploTemplateMethodImportacaoLote.java
    │   │   ├── memento/               # GoF Memento e persistência de sessão/preferências
    │   │   │   └── ExemploMementoEPreferenciasJavaFX.java
    │   │   ├── composite/             # GoF Composite para estruturas em árvore e TreeView
    │   │   │   └── ExemploCompositeETreeView.java
    │   │   ├── decorator/             # GoF Decorator para tarifação e opcionais dinâmicos
    │   │   │   └── ExemploDecoratorTarifacaoDinamica.java
    │   │   ├── chain/                 # GoF Chain of Responsibility para auditoria em pipeline
    │   │   │   └── ExemploChainOfResponsibilityValidacao.java
    │   │   ├── adapter/               # GoF Adapter para integração de feeds e formatos legados
    │   │   │   └── ExemploAdapterIntegracaoLegada.java
    │   │   ├── facade/                # GoF Facade para orquestração de múltiplos subsistemas
    │   │   │   └── ExemploFacadeSubsistemasDesktop.java
    │   │   ├── mediator/              # GoF Mediator para coordenação de telas complexas
    │   │   │   └── ExemploMediatorFormularioJavaFX.java
    │   │   ├── proxy/                 # GoF Proxy para Virtual Lazy Loading e Caching em RAM
    │   │   │   └── ExemploProxyLazyLoadingECache.java
    │   │   ├── observer/              # GoF Observer Puro + Ponte Reativa com JavaFX
    │   │   │   └── ExemploObserverDominioEPonteJavaFX.java
    │   │   ├── factory/               # GoF Abstract Factory e criação polimórfica de famílias
    │   │   │   └── ExemploAbstractFactoryETemasAmbiente.java
    │   │   ├── visitor/               # GoF Visitor e Double Dispatch para operações externas
    │   │   │   └── ExemploVisitorOperacoesPolimorficas.java
    │   │   ├── flyweight/             # GoF Flyweight para otimização extrema de memória Heap
    │   │   │   └── ExemploFlyweightOtimizacaoMemoria.java
    │   │   ├── iterator/              # GoF Iterator e Paginação Virtual sob demanda em UI
    │   │   │   └── ExemploIteratorPaginacaoVirtualJavaFX.java
    │   │   └── javafx/                # Demonstrações ricas de interface desktop
    │   │       ├── ExemploTabelaEModalJavaFX.java               # TableView, modais e Alerts
    │   │       ├── ExemploConcorrenciaJavaFX.java                # Task, Worker Threads e Platform.runLater
    │   │       ├── ExemploDataBindingJavaFX.java                 # Properties, validação reativa e listeners
    │   │       ├── ExemploCssETemasJavaFX.java                   # PseudoClass e Alternância Light/Dark Mode
    │   │       ├── ExemploNavegacaoJavaFX.java                   # Padrão Shell e Diálogos Tipados Dialog<R>
    │   │       ├── ExemploValidacaoFormularioJavaFX.java         # Safe Parsing e Validação Reativa UX
    │   │       ├── ExemploFiltroEOrdenacaoJavaFX.java            # FilteredList, SortedList e Ações por Linha
    │   │       ├── ExemploEventBusDesacoplado.java               # Pub/Sub centralizado e desacoplamento
    │   │       ├── ExemploDataPickerERegrasTemporaisJavaFX.java  # java.time, DatePicker pt-BR e multas
    │   │       ├── ExemploDashboardGraficosJavaFX.java           # PieChart, BarChart e Métricas Streams
    │   │       ├── ExemploCommandUndoRedoJavaFX.java             # GoF Command, Undo/Redo e Atalhos Ctrl+Z/Y
    │   │       ├── ExemploExportacaoRelatoriosEFileChooserJavaFX.java # Recibos monoespaçados, CSV e FileChooser
    │   │       └── ExemploTratamentoGlobalErrosJavaFX.java       # Resiliência Global e Logs de Diagnóstico
    │   └── resources/
    │       └── css/                   # Estilos visuais e temas desacoplados
    │           ├── tema-claro.css     # Paleta Light moderna com seletores e pseudo-classes
    │           └── tema-escuro.css    # Paleta Dark mode profissional
    └── test/java/br/edu/ifpb/didatico/exemplos/
        └── ExemplosDidaticosTest.java # Bateria com 33 testes automatizados no JUnit 5
    ```

    ---

    ## 3. Roteiro de Reconstrução em 9 Etapas

    | Etapa | Foco | Objetivos de Aprendizagem |
    | :--- | :--- | :--- |
    | **Etapa 1** | **Modelo Orientado a Objetos Puro & Terminal** | Encapsulamento, validação de invariantes, herança com superclasse abstrata, polimorfismo via padrão Strategy, composição entre entidades, coleções protegidas com `unmodifiableList`, repositórios CSV manuais (`BufferedReader`/`BufferedWriter`), injeção manual de dependências por construtor e exceções de domínio checadas. |
    | **Etapa 2** | **Interface Desktop Gráfica com JavaFX** | Arquitetura MVC desacoplada com arquivos `.fxml`, injeção com `@FXML` e `Initializable`, tabelas `TableView` com células estilizadas, janelas modais (`Modality.APPLICATION_MODAL`), concorrência em UI com `Task<V>` e `Platform.runLater()`, e reatividade com Data Binding. |
    | **Etapa 3** | **Padrões Avançados de Engenharia e Java 21** | Records com construtor compacto, Sealed Types com Pattern Matching Switch e Guards (`when`), Theming dinâmico via CSS com `PseudoClass` (sem `setStyle`), Padrão Shell para navegação sem múltiplas janelas, Diálogos tipados `Dialog<R>`, Safe Parsing preventivo e Testabilidade pura com Fakes em RAM. |
    | **Etapa 4** | **Reatividade, Integridade Atômica e Desacoplamento** | Pipeline reativo com `FilteredList` e `SortedList`, botões de ação em linhas de tabelas, gravação atômica com `Files.move(..., ATOMIC_MOVE)` e backup `.bak`, concorrência com `ReentrantReadWriteLock`, EventBus centralizado em memória e manipulação moderna de datas (`java.time`) com `DatePicker` brasileiro e bloqueio via `DayCellFactory`. |
    | **Etapa 5** | **Inteligência Visual, Reversibilidade e Resiliência Operacional** | Dashboards executivos com `PieChart` e `BarChart`, agregações com Java Streams (`groupingBy`, `counting`, `reducing`), Padrão GoF Command com pilhas reversíveis de Undo/Redo e atalhos de teclado (`Ctrl+Z`/`Ctrl+Y`), exportação de recibos tabulares monoespaçados com hash de validação SHA-256 e `FileChooser` nativo, e proteção de resiliência com `UncaughtExceptionHandler` global e gravação append-only em `app-erros.log`. |
    | **Etapa 6** | **Máquina de Estados, Construção Segura, Importação em Lote e Sessão de UI** | Padrão GoF State com ciclo de vida tipado e rejeição de transições ilegais, Padrão GoF Builder com validação de invariantes no `.build()` para formulários multi-etapas (*Wizards*), Padrão GoF Template Method para processamento e importação em lote resiliente com isolamento de falhas por linha, e Padrão GoF Memento com persistência nativa de sessão, dimensões de janela e preferências do usuário (`java.util.prefs.Preferences`). |
    | **Etapa 7** | **Estruturação Hierárquica, Extensibilidade Dinâmica, Pipelines de Regras e Interoperabilidade** | Padrão GoF Composite com navegação hierárquica recursiva e `TreeView`/`TreeCell`, Padrão GoF Decorator com composição dinâmica de serviços e precificação transparente na UI, Padrão GoF Chain of Responsibility com auditoria multinível e checklist visual de conformidade, e Padrão GoF Adapter com integração de fontes arcaicas à `TableView` limpa do domínio. |
    | **Etapa 8** | **Orquestração de Subsistemas, Mediação de Telas, Recursos Virtuais e Observabilidade de Domínio** | Padrão GoF Facade com gateway único e compensação transacional (rollback), Padrão GoF Mediator eliminando acoplamento N:N em formulários JavaFX, Padrão GoF Proxy com Virtual Proxy, Lazy Loading e cache thread-safe (0 ms), e Padrão GoF Observer Puro no núcleo de domínio (zero imports de JavaFX) com ponte reativa via Platform.runLater(). |
    | **Etapa 9** | **Criação Polimórfica, Operações com Double Dispatch, Eficiência de Memória e Paginação Sob Demanda** | Padrão GoF Abstract Factory produzindo famílias completas de regras e componentes gráficos desacoplados, Padrão GoF Visitor executando cálculos contábeis e auditorias técnicas sem poluir entidades, Padrão GoF Flyweight otimizando a memória Heap com pools de estados intrínsecos imutáveis, e Padrão GoF Iterator com paginação virtual sob demanda em `TableView` garantindo 60 FPS com grandes volumes de registros. |

---

## 4. Como Usar Este Guia para Reconstruir seu Projeto

1. Abra o arquivo **`CHECKLIST_TAREFAS.md`** e acompanhe a ordem cronológica recomendada.
2. Para cada fase do checklist:
   - Leia o caderno conceitual correspondente na pasta **`guias/`** para absorver a fundamentação teórica, diagramas UML/Mermaid e respostas para bancas avaliadoras.
   - Consulte o exemplo compilável correspondente em **`src/main/java/...`** para verificar a sintaxe correta da linguagem Java moderna (Java 21).
   - Execute a suíte de testes unitários para ver como as regras são verificadas de forma automatizada.
   - Escreva o código do seu sistema real aplicando os conceitos aprendidos.
3. Marque a caixa de seleção da tarefa somente após validar que ela compila e passa nos critérios de aceitação.

---

## 5. Compilação, Testes e Execução dos Exemplos Didáticos

### 5.1 Compilação do Projeto
```bash
mvn clean compile
```

### 5.2 Execução da Suíte de Testes Automatizados (JUnit 5)
```bash
mvn test
```

### 5.3 Execução das Demonstrações Gráficas JavaFX

- **Executar Demonstração Principal (TableView e Janelas Modais):**
  ```bash
  mvn javafx:run
  ```

- **Executar Demonstração de Concorrência e Thread Safety (Task, ProgressBar, Platform.runLater):**
  ```bash
  mvn javafx:run -Djavafx.mainClass="br.edu.ifpb.didatico.exemplos.javafx.ExemploConcorrenciaJavaFX"
  ```

- **Executar Demonstração de Data Binding Reativo (Properties, Validação Dinâmica e Slider):**
  ```bash
  mvn javafx:run -Djavafx.mainClass="br.edu.ifpb.didatico.exemplos.javafx.ExemploDataBindingJavaFX"
  ```

- **Executar Demonstração de Estilização CSS e Alternância Light/Dark Mode:**
  ```bash
  mvn javafx:run -Djavafx.mainClass="br.edu.ifpb.didatico.exemplos.javafx.ExemploCssETemasJavaFX"
  ```

- **Executar Demonstração do Padrão Shell, Navegação e Diálogos Tipados (`Dialog<R>`):**
  ```bash
  mvn javafx:run -Djavafx.mainClass="br.edu.ifpb.didatico.exemplos.javafx.ExemploNavegacaoJavaFX"
  ```

- **Executar Demonstração de Validação Reativa de Formulários e Safe Parsing:**
  ```bash
  mvn javafx:run -Djavafx.mainClass="br.edu.ifpb.didatico.exemplos.javafx.ExemploValidacaoFormularioJavaFX"
  ```

- **Executar Demonstração de Filtros Dinâmicos, SortedList e Ações em Linhas de Tabelas:**
  ```bash
  mvn javafx:run -Djavafx.mainClass="br.edu.ifpb.didatico.exemplos.javafx.ExemploFiltroEOrdenacaoJavaFX"
  ```

- **Executar Demonstração de Comunicação Desacoplada com AppEventBus (Pub/Sub):**
  ```bash
  mvn javafx:run -Djavafx.mainClass="br.edu.ifpb.didatico.exemplos.javafx.ExemploEventBusDesacoplado"
  ```

- **Executar Demonstração de DatePicker Brasileiro (dd/MM/yyyy), DayCellFactory e java.time:**
  ```bash
  mvn javafx:run -Djavafx.mainClass="br.edu.ifpb.didatico.exemplos.javafx.ExemploDataPickerERegrasTemporaisJavaFX"
  ```

- **Executar Demonstração de Dashboard Executivo e Gráficos (PieChart, BarChart e KPIs):**
  ```bash
  mvn javafx:run -Djavafx.mainClass="br.edu.ifpb.didatico.exemplos.javafx.ExemploDashboardGraficosJavaFX"
  ```

- **Executar Demonstração de Padrão Command, Undo/Redo e Atalhos de Teclado (Ctrl+Z / Ctrl+Y):**
  ```bash
  mvn javafx:run -Djavafx.mainClass="br.edu.ifpb.didatico.exemplos.javafx.ExemploCommandUndoRedoJavaFX"
  ```

- **Executar Demonstração de Recibos Monoespaçados, CSV e Diálogos FileChooser:**
  ```bash
  mvn javafx:run -Djavafx.mainClass="br.edu.ifpb.didatico.exemplos.javafx.ExemploExportacaoRelatoriosEFileChooserJavaFX"
  ```

- **Executar Demonstração de Resiliência Global, UncaughtExceptionHandler e Logs:**
  ```bash
  mvn javafx:run -Djavafx.mainClass="br.edu.ifpb.didatico.exemplos.javafx.ExemploTratamentoGlobalErrosJavaFX"
  ```

- **Executar Demonstração de Padrão Composite e Hierarquias de Domínio no TreeView:**
  ```bash
  mvn javafx:run -Djavafx.mainClass="br.edu.ifpb.didatico.exemplos.composite.ExemploCompositeETreeView"
  ```

- **Executar Demonstração de Padrão Decorator e Tarifação Dinâmica de Opcionais:**
  ```bash
  mvn javafx:run -Djavafx.mainClass="br.edu.ifpb.didatico.exemplos.decorator.ExemploDecoratorTarifacaoDinamica"
  ```

- **Executar Demonstração de Padrão Chain of Responsibility e Pipeline de Auditoria:**
  ```bash
  mvn javafx:run -Djavafx.mainClass="br.edu.ifpb.didatico.exemplos.chain.ExemploChainOfResponsibilityValidacao"
  ```

- **Executar Demonstração de Padrão Adapter e Integração de Catálogo Legado em TableView:**
  ```bash
  mvn javafx:run -Djavafx.mainClass="br.edu.ifpb.didatico.exemplos.adapter.ExemploAdapterIntegracaoLegada"
  ```

- **Executar Demonstração de Padrão Facade e Orquestração de Subsistemas Desktop:**
  ```bash
  mvn javafx:run -Djavafx.mainClass="br.edu.ifpb.didatico.exemplos.facade.ExemploFacadeSubsistemasDesktop"
  ```

- **Executar Demonstração de Padrão Mediator e Coordenação de Formulários Complexos:**
  ```bash
  mvn javafx:run -Djavafx.mainClass="br.edu.ifpb.didatico.exemplos.mediator.ExemploMediatorFormularioJavaFX"
  ```

- **Executar Demonstração de Padrão Proxy, Lazy Loading e Caching de Recursos Pesados:**
  ```bash
  mvn javafx:run -Djavafx.mainClass="br.edu.ifpb.didatico.exemplos.proxy.ExemploProxyLazyLoadingECache"
  ```

- **Executar Demonstração de Padrão Observer Puro e Ponte Reativa com JavaFX:**
  ```bash
  mvn javafx:run -Djavafx.mainClass="br.edu.ifpb.didatico.exemplos.observer.ExemploObserverDominioEPonteJavaFX"
  ```

- **Executar Demonstração de Padrão Abstract Factory e Famílias de Regras/Componentes:**
  ```bash
  mvn javafx:run -Djavafx.mainClass="br.edu.ifpb.didatico.exemplos.factory.ExemploAbstractFactoryETemasAmbiente"
  ```

- **Executar Demonstração de Padrão Visitor e Operações Polimórficas Externas:**
  ```bash
  mvn javafx:run -Djavafx.mainClass="br.edu.ifpb.didatico.exemplos.visitor.ExemploVisitorOperacoesPolimorficas"
  ```

- **Executar Demonstração de Padrão Flyweight e Otimização Extrema de Memória Heap:**
  ```bash
  mvn javafx:run -Djavafx.mainClass="br.edu.ifpb.didatico.exemplos.flyweight.ExemploFlyweightOtimizacaoMemoria"
  ```

- **Executar Demonstração de Padrão Iterator e Paginação Virtual sob Demanda:**
  ```bash
  mvn javafx:run -Djavafx.mainClass="br.edu.ifpb.didatico.exemplos.iterator.ExemploIteratorPaginacaoVirtualJavaFX"
  ```

### 5.4 Execução das Demonstrações de Console / Terminal

- **Demonstração de Injeção de Dependências e Arquitetura em Camadas:**
  ```bash
  mvn exec:java -Dexec.mainClass="br.edu.ifpb.didatico.exemplos.arquitetura.ExemploInjecaoDependenciasCamadas"
  ```

- **Demonstração de Java 21 Moderno (Records, Sealed Types e Pattern Matching):**
  ```bash
  mvn exec:java -Dexec.mainClass="br.edu.ifpb.didatico.exemplos.moderno.ExemploJavaModernoRecordESealed"
  ```

- **Demonstração de Testabilidade com Repositório Fake em Memória:**
  ```bash
  mvn exec:java -Dexec.mainClass="br.edu.ifpb.didatico.exemplos.teste.ExemploTestabilidadeComFake"
  ```

- **Demonstração de Persistência Atômica (Safe Atomic Move), Backup (.bak) e Locks Concorrentes:**
  ```bash
  mvn exec:java -Dexec.mainClass="br.edu.ifpb.didatico.exemplos.csv.ExemploPersistenciaAtomicaComLock"
  ```
