# Blueprint Didático: Guia de Reconstrução em Java 21 e JavaFX

> **Repositório Educacional de Referência Metodológica, Padrões Arquiteturais e Exemplos Práticos de Apoio.**
> 
> Focado em **simplicidade**, **aderência estrita às especificações** e **isolamento pedagógico**.

---

## 1. Propósito e Diretrizes Fundamentais

Este repositório foi concebido como um **Master Blueprint** para desenvolvedores e estudantes sob direção de `@domaragao` que precisam projetar, implementar ou reconstruir sistemas desktop orientados a objetos em **Java 21 LTS** e **JavaFX**, respeitando integralmente os limites da especificação acadêmica do IFPB.

### 🎯 Princípios de Alinhamento Rígido:
1. **Maven apenas na Etapa 2:** A Etapa 1 utiliza **Java 21 puro** com estrutura de pastas tradicional (`src/`, `bin/`, `dados/`), sendo compilada diretamente com `javac` e executada com `java`. O Apache Maven é introduzido unicamente na Etapa 2 para gerenciar os módulos nativos do **OpenJFX 21** e permitir a execução via `mvn javafx:run`.
2. **Sem implementação de testes automatizados:** O projeto foca na validação operacional através do **roteiro determinístico da Seção 7** no terminal (Etapa 1) e da interface visual interativa (Etapa 2), sem a sobrecarga de testes unitários ou frameworks como JUnit/Mockito.
3. **Padrões GoF restritos ao documento de instruções:** Nenhum padrão GoF adicional é implementado (nada de Factory, Builder, Observer, Visitor, etc.). Apenas o padrão **Strategy** exigido explicitamente para o cálculo de multas polimórficas por tipo de item é adotado.
4. **Sem Concorrência:** O sistema opera de forma síncrona e direta, em conformidade com o edital ("não use threads").
5. **Sem Banco de Dados ou Frameworks Externos:** Persistência em arquivo texto CSV manual com `BufferedReader` e `BufferedWriter`. Sem Spring, sem Hibernate/JPA e sem Jackson/Gson.

### 🛡️ Regra de Isolamento Pedagógico
- **Nenhum trecho de código específico do domínio do sistema real está presente neste repositório** (não há classes de acervo de laboratório, itens específicos ou regras de empréstimo do domínio real).
- O repositório contém **exclusivamente**:
  1. **Lista de Tarefas Estruturada (`CHECKLIST_TAREFAS.md`)** dividida em 10 Fases Metodológicas.
  2. **10 Cadernos Didáticos e Metodológicos (`guias/`)** com explicações teóricas, diagramas Mermaid, boas práticas e armadilhas a evitar.
  3. **7 Exemplos de Código Compiláveis em Java 21 (`src/main/java/...`)** demonstrando a sintaxe e o uso correto das APIs nativas.

---

## 2. Estrutura do Repositório

```text
guia-reconstrucao-poo-laboratorio/
├── README.md                      # Visão geral, filosofia e mapa metodológico
├── CHECKLIST_TAREFAS.md           # Roteiro passo a passo com critérios de conclusão (10 Fases)
├── pom.xml                        # Configuração Maven para a Etapa 2 (OpenJFX 21 + javafx-maven-plugin)
├── guias/                         # Cadernos conceituais detalhados (10 Guias Didáticos)
│   ├── 01-modelagem-oo-e-encapsulamento.md
│   ├── 02-heranca-polimorfismo-e-strategy.md
│   ├── 03-colecoes-e-defensive-copying.md
│   ├── 04-persistencia-csv-sem-frameworks.md
│   ├── 05-excecoes-de-dominio-e-propagacao.md
│   ├── 06-camada-de-servico-e-injecao-dependencias.md
│   ├── 07-compilacao-manual-e-execucao-terminal.md
│   ├── 08-configuracao-maven-para-javafx.md
│   ├── 09-javafx-arquitetura-fxml-e-mvc.md
│   └── 10-javafx-tabelas-modais-e-alertas.md
└── src/
    └── main/java/br/edu/ifpb/didatico/exemplos/
        ├── oo/                    # Encapsulamento, validação defensiva e invariantes
        │   └── ExemploEncapsulamentoEValidacao.java
        ├── strategy/              # Padrão GoF Strategy para regras de cálculo polimórficas
        │   └── ExemploStrategyPolimorfico.java
        ├── colecoes/              # Cópia defensiva e Collections.unmodifiableList
        │   └── ExemploColecoesProtegidas.java
        ├── csv/                   # Leitura e escrita manual de arquivos CSV com BufferedReader/Writer
        │   └── ExemploLeituraGravacaoCSV.java
        ├── excecao/               # Exceções checadas de domínio estendendo Exception
        │   └── ExemploExcecaoChecada.java
        ├── arquitetura/           # Camadas desacopladas e Pure Dependency Injection por construtor
        │   └── ExemploInjecaoDependenciasCamadas.java
        └── javafx/                # Interface Desktop com TableView, janelas modais e diálogo Alert
            └── ExemploTabelaEModalJavaFX.java
```

---

## 3. As 10 Fases de Reconstrução

### Etapa 1 — Domínio Orientado a Objetos, Persistência CSV e Execução no Terminal
- **Fase 1:** Organização do Projeto e Estrutura de Pacotes (Java Puro, sem ferramentas de build)
- **Fase 2:** Modelagem das Entidades de Domínio e Encapsulamento
- **Fase 3:** Regras de Multa com Padrão GoF Strategy
- **Fase 4:** Exceções de Domínio Personalizadas (Checked Exceptions)
- **Fase 5:** Persistência Manual em Arquivo Texto (CSV sem bibliotecas)
- **Fase 6:** Camada de Serviço e Proteção de Coleções (Injeção de Dependências manual)
- **Fase 7:** Demonstração do Roteiro no Terminal (Seção 7) e Entrega da Etapa 1

### Etapa 2 — Configuração do Maven e Interface Gráfica JavaFX
- **Fase 8:** Introdução e Configuração do Maven para o Ecossistema JavaFX (`pom.xml`)
- **Fase 9:** Arquitetura JavaFX, FXML Declarativo e MVC Desacoplado
- **Fase 10:** Componentes Visuais (TableView), Modais de Ação, Alertas e Entrega da Etapa 2

---

## 4. Como Compilar e Testar os Exemplos Didáticos

Para compilar os 7 exemplos didáticos contidos neste repositório:

```bash
mvn clean compile
```

Para executar a demonstração gráfica do JavaFX (TableView, Janela Modal e Alerta de Erro):

```bash
mvn javafx:run
```
