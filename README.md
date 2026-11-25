# Blueprint Didático: Guia de Reconstrução em Java 21 e JavaFX

> **Repositório Educacional de Referência Metodológica, Padrões Arquiteturais, Testes Automatizados e Exemplos Práticos de Apoio.**

---

## 1. Propósito e Filosofia do Repositório

Este repositório foi concebido como um **Master Blueprint (Guia Mestre de Construção e Reconstrução)** para desenvolvedores e estudantes que precisam projetar, implementar ou reconstruir sistemas desktop orientados a objetos em **Java 21 LTS** e **JavaFX**.

### ⚠️ Regra de Isolamento Pedagógico
Seguindo as melhores práticas pedagógicas e as diretrizes estabelecidas:
- **Nenhum trecho de código específico do domínio do sistema real está presente neste repositório** (não há classes de acervo de laboratório, itens específicos ou regras de empréstimo do domínio real).
- O repositório contém **exclusivamente**:
  1. **Lista de Tarefas Estruturada (`CHECKLIST_TAREFAS.md`)** com ordem de dependência, critérios de aceitação e armadilhas a evitar.
  2. **Cadernos Didáticos e Metodológicos (`guias/`)** cobrindo desde modelagem pura e persistência manual em CSV até concorrência em JavaFX, injeção manual de dependências e data binding reativo.
  3. **Exemplos de Código Compiláveis em Java 21 (`src/main/java/...`)** demonstrando a sintaxe e o uso correto das APIs nativas do JDK e OpenJFX.
  4. **Suíte de Testes Automatizados (`src/test/java/...`)** com JUnit 5 validando invariantes, imutabilidade, polimorfismo e contratos desacoplados.

---

## 2. Estrutura do Repositório

```text
guia-reconstrucao-poo-laboratorio/
├── README.md                      # Visão geral, filosofia e mapa de estudos
├── CHECKLIST_TAREFAS.md           # Roteiro passo a passo com critérios de conclusão
├── pom.xml                        # Configuração Maven com Java 21, OpenJFX, JUnit 5 e Plugins
├── guias/                         # Cadernos conceituais detalhados (11 Guias)
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
│   └── 11-javafx-properties-e-data-binding.md
└── src/
    ├── main/java/br/edu/ifpb/didatico/exemplos/
    │   ├── oo/                    # Encapsulamento, validação e invariantes
    │   │   └── ExemploEncapsulamentoEValidacao.java
    │   ├── strategy/              # Polimorfismo GoF Strategy
    │   │   └── ExemploStrategyPolimorfico.java
    │   ├── colecoes/              # Cópia defensiva e Stream API
    │   │   └── ExemploColecoesProtegidas.java
    │   ├── csv/                   # I/O manual com BufferedReader/Writer em UTF-8
    │   │   └── ExemploLeituraGravacaoCSV.java
    │   ├── excecao/               # Checked Exceptions de domínio
    │   │   └── ExemploExcecaoChecada.java
    │   ├── arquitetura/           # Camadas desacopladas e Pure Dependency Injection
    │   │   └── ExemploInjecaoDependenciasCamadas.java
    │   └── javafx/                # Demonstrações de interface rica
    │       ├── ExemploTabelaEModalJavaFX.java      # TableView, modais e Alerts
    │       ├── ExemploConcorrenciaJavaFX.java       # Task, Worker Threads e Platform.runLater
    │       └── ExemploDataBindingJavaFX.java        # Properties, validação reativa e listeners
    └── test/java/br/edu/ifpb/didatico/exemplos/
        └── ExemplosDidaticosTest.java              # Suíte de testes automatizados com JUnit 5
```

---

## 3. Roteiro de Reconstrução em 2 Etapas

| Etapa | Foco | Objetivos de Aprendizagem |
| :--- | :--- | :--- |
| **Etapa 1** | **Modelo Orientado a Objetos Puro & Terminal** | Encapsulamento, validação de invariantes, herança com superclasse abstrata, polimorfismo via padrão Strategy, composição entre entidades, coleções protegidas com `unmodifiableList`, repositórios CSV manuais (`BufferedReader`/`BufferedWriter`), injeção manual de dependências por construtor e exceções de domínio checadas. |
| **Etapa 2** | **Interface Desktop Gráfica com JavaFX** | Arquitetura MVC desacoplada com arquivos `.fxml`, injeção com `@FXML` e `Initializable`, tabelas `TableView` com células estilizadas, janelas modais (`Modality.APPLICATION_MODAL`), concorrência em UI com `Task<V>` e `Platform.runLater()`, validação reativa com Data Binding e folha de estilos CSS. |

---

## 4. Como Usar Este Guia para Reconstruir seu Projeto

1. Abra o arquivo **`CHECKLIST_TAREFAS.md`** e acompanhe a ordem cronológica recomendada.
2. Para cada fase do checklist:
   - Leia o guia correspondente na pasta **`guias/`** para absorver a fundamentação teórica, diagramas UML/Mermaid e respostas para bancas avaliadoras.
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

### 5.4 Execução das Demonstrações de Terminal

- **Demonstração de Injeção de Dependências e Arquitetura em Camadas:**
  ```bash
  mvn exec:java -Dexec.mainClass="br.edu.ifpb.didatico.exemplos.arquitetura.ExemploInjecaoDependenciasCamadas"
  ```
