# Blueprint Didático: Guia de Reconstrução em Java 21 e JavaFX

> **Repositório Educacional de Referência Metodológica, Padrões Arquiteturais e Exemplos Práticos de Apoio.**

---

## 1. Propósito e Filosofia do Repositório

Este repositório foi concebido como um **Master Blueprint (Guia Mestre de Construção e Reconstrução)** para desenvolvedores e estudantes que precisam projetar, implementar ou reconstruir sistemas desktop orientados a objetos em **Java 21 LTS** e **JavaFX**.

### ⚠️ Regra de Isolamento Pedagógico
Seguindo as melhores práticas pedagógicas e as diretrizes estabelecidas:
- **Nenhum trecho de código específico do domínio do sistema real está presente neste repositório** (não há classes de acervo de laboratório, itens específicos ou regras de empréstimo).
- O repositório contém **exclusivamente**:
  1. **Lista de Tarefas Estruturada (`CHECKLIST_TAREFAS.md`)** com ordem de dependência, critérios de aceitação e armadilhas a evitar.
  2. **Guias Conceituais e Metodológicos (`guias/`)** explicando detalhadamente a teoria de POO, persistência e JavaFX.
  3. **Exemplos de Código Genéricos e Didáticos (`exemplos/`)** demonstrando a sintaxe e o uso correto das APIs Java (ex.: leitura de arquivos CSV, coleções não modificáveis, Strategy Pattern com cálculo de frete, `TableView` no JavaFX, etc.).

---

## 2. Estrutura do Repositório

```text
guia-reconstrucao-poo-laboratorio/
├── README.md                      # Visão geral, filosofia e mapa de estudos
├── CHECKLIST_TAREFAS.md           # Roteiro passo a passo com critérios de conclusão
├── pom.xml                        # Configuração Maven com Java 21 LTS e JavaFX
├── guias/                         # Cadernos conceituais detalhados
│   ├── 01-modelagem-oo-e-encapsulamento.md
│   ├── 02-heranca-polimorfismo-e-strategy.md
│   ├── 03-colecoes-e-defensive-copying.md
│   ├── 04-persistencia-csv-sem-frameworks.md
│   ├── 05-excecoes-de-dominio-e-propagacao.md
│   ├── 06-javafx-arquitetura-fxml-e-mvc.md
│   ├── 07-javafx-tabelas-e-cell-factories.md
│   └── 08-javafx-modais-e-caixas-de-dialogo.md
└── exemplos/                      # Códigos compiláveis de demonstração de sintaxe
    ├── csv/
    │   └── ExemploLeituraGravacaoCSV.java
    ├── oo/
    │   └── ExemploEncapsulamentoEValidacao.java
    ├── strategy/
    │   └── ExemploStrategyPolimorfico.java
    ├── colecoes/
    │   └── ExemploColecoesProtegidas.java
    ├── excecao/
    │   └── ExemploExcecaoChecada.java
    └── javafx/
        └── ExemploTabelaEModalJavaFX.java
```

---

## 3. Roteiro de Reconstrução em 2 Etapas

| Etapa | Foco | Objetivos de Aprendizagem |
| :--- | :--- | :--- |
| **Etapa 1** | **Modelo Orientado a Objetos Puro & Terminal** | Encapsulamento, validação de invariantes, herança com superclasse abstrata, polimorfismo via padrão Strategy, composição entre entidades, coleções protegidas com `unmodifiableList`, repositórios CSV manuais (`BufferedReader`/`BufferedWriter`), injeção de dependências e exceções de domínio checadas. |
| **Etapa 2** | **Interface Desktop Gráfica com JavaFX** | Arquitetura MVC desacoplada com arquivos `.fxml`, injeção com `@FXML` e `Initializable`, tabelas `TableView` com células estilizadas, janelas modais (`Modality.APPLICATION_MODAL`), captura de exceções de negócio exibindo `Alert(AlertType.ERROR)` e folha de estilos CSS corporativa. |

---

## 4. Como Usar Este Guia para Reconstruir seu Projeto

1. Abra o arquivo **`CHECKLIST_TAREFAS.md`** e acompanhe a ordem cronológica recomendada.
2. Para cada fase do checklist:
   - Leia o guia correspondente na pasta **`guias/`** para absorver a fundamentação teórica e as perguntas frequentes de bancas avaliadoras.
   - Consulte o exemplo prático em **`exemplos/`** para verificar a sintaxe correta da linguagem Java moderna (Java 21).
   - Escreva o código do seu sistema real aplicando os conceitos aprendidos.
3. Marque a caixa de seleção da tarefa somente após validar que ela compila e passa nos critérios de aceitação.

---

## 5. Compilação dos Exemplos Didáticos

Para compilar os exemplos genéricos deste repositório, execute:
```bash
mvn clean compile
```
Todos os exemplos utilizam exclusivamente bibliotecas nativas do JDK 21 e OpenJFX 21, sem acoplamento a bancos de dados ou frameworks invasivos.
