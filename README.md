# Blueprint Didático: Guia de Reconstrução em Java 21 e JavaFX

> **Repositório Educacional de Referência Metodológica, Padrões Arquiteturais, Testes Automatizados e Exemplos Práticos de Apoio.**

---

## 1. Propósito e Filosofia do Repositório

Este repositório foi concebido como um **Master Blueprint (Guia Mestre de Construção e Reconstrução)** para desenvolvedores e estudantes sob direção de `@domaragao` que precisam projetar, implementar ou reconstruir sistemas desktop orientados a objetos em **Java 21 LTS** e **JavaFX**.

### ⚠️ Regra de Isolamento Pedagógico
Seguindo as melhores práticas pedagógicas e as diretrizes estabelecidas:
- **Nenhum trecho de código específico do domínio do sistema real está presente neste repositório** (não há classes de acervo de laboratório, itens específicos ou regras de empréstimo do domínio real).
- O repositório contém **exclusivamente**:
  1. **Lista de Tarefas Estruturada (`CHECKLIST_TAREFAS.md`)** com ordem de dependência em 23 fases, critérios de aceitação e armadilhas a evitar.
  2. **Cadernos Didáticos e Metodológicos (`guias/`)** com 24 cadernos cobrindo modelagem pura, persistência manual CSV, JavaFX completo, concorrência, data binding reativo, Java 21 moderno (Records e Sealed Types), theming CSS, navegação com padrão Shell, validação reativa, testabilidade com Fakes, reatividade de tabelas com FilteredList/SortedList, persistência atômica com backup e locks, barramento de eventos (EventBus), manipulação temporal com `java.time` e DatePicker, dashboards analíticos com gráficos (PieChart/BarChart), padrão Command com Undo/Redo, exportação de relatórios/recibos formatados com FileChooser e resiliência com UncaughtExceptionHandler global.
  3. **Exemplos de Código Compiláveis em Java 21 (`src/main/java/...`)** demonstrando a sintaxe e o uso correto das APIs nativas do JDK e OpenJFX.
  4. **Suíte de Testes Automatizados (`src/test/java/...`)** com JUnit 5 validando invariantes, imutabilidade, polimorfismo, contratos desacoplados, persistência atômica, eventos tipados, regras temporais, fakes em memória, agregações analíticas, padrão Command e tratamento global de falhas (17 testes automatizados).

---

## 2. Estrutura do Repositório

```text
guia-reconstrucao-poo-laboratorio/
├── README.md                      # Visão geral, filosofia e mapa de estudos
├── CHECKLIST_TAREFAS.md           # Roteiro passo a passo com critérios de conclusão (23 Fases)
├── pom.xml                        # Configuração Maven com Java 21, OpenJFX, JUnit 5 e Plugins
├── guias/                         # Cadernos conceituais detalhados (24 Guias Didáticos)
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
│   └── 24-resiliencia-global-uncaught-exceptions-e-logs.md
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
        └── ExemplosDidaticosTest.java # Bateria com 17 testes automatizados no JUnit 5
```

---

## 3. Roteiro de Reconstrução em 5 Etapas

| Etapa | Foco | Objetivos de Aprendizagem |
| :--- | :--- | :--- |
| **Etapa 1** | **Modelo Orientado a Objetos Puro & Terminal** | Encapsulamento, validação de invariantes, herança com superclasse abstrata, polimorfismo via padrão Strategy, composição entre entidades, coleções protegidas com `unmodifiableList`, repositórios CSV manuais (`BufferedReader`/`BufferedWriter`), injeção manual de dependências por construtor e exceções de domínio checadas. |
| **Etapa 2** | **Interface Desktop Gráfica com JavaFX** | Arquitetura MVC desacoplada com arquivos `.fxml`, injeção com `@FXML` e `Initializable`, tabelas `TableView` com células estilizadas, janelas modais (`Modality.APPLICATION_MODAL`), concorrência em UI com `Task<V>` e `Platform.runLater()`, e reatividade com Data Binding. |
| **Etapa 3** | **Padrões Avançados de Engenharia e Java 21** | Records com construtor compacto, Sealed Types com Pattern Matching Switch e Guards (`when`), Theming dinâmico via CSS com `PseudoClass` (sem `setStyle`), Padrão Shell para navegação sem múltiplas janelas, Diálogos tipados `Dialog<R>`, Safe Parsing preventivo e Testabilidade pura com Fakes em RAM. |
| **Etapa 4** | **Reatividade, Integridade Atômica e Desacoplamento** | Pipeline reativo com `FilteredList` e `SortedList`, botões de ação em linhas de tabelas, gravação atômica com `Files.move(..., ATOMIC_MOVE)` e backup `.bak`, concorrência com `ReentrantReadWriteLock`, EventBus centralizado em memória e manipulação moderna de datas (`java.time`) com `DatePicker` brasileiro e bloqueio via `DayCellFactory`. |
| **Etapa 5** | **Inteligência Visual, Reversibilidade e Resiliência Operacional** | Dashboards executivos com `PieChart` e `BarChart`, agregações com Java Streams (`groupingBy`, `counting`, `reducing`), Padrão GoF Command com pilhas reversíveis de Undo/Redo e atalhos de teclado (`Ctrl+Z`/`Ctrl+Y`), exportação de recibos tabulares monoespaçados com hash de validação SHA-256 e `FileChooser` nativo, e proteção de resiliência com `UncaughtExceptionHandler` global e gravação append-only em `app-erros.log`. |

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
