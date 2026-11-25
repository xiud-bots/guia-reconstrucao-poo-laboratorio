# Checklist de Tarefas: Reconstrução Passo a Passo do Projeto

> Este documento contém a sequência ordenada de tarefas para guiar a implementação completa do sistema, dividida em **10 Fases Metodológicas**.

---

## 📋 Resumo do Progresso
- [ ] **Etapa 1: Núcleo de Domínio, Persistência CSV e Terminal (Fases 1 a 8)**
- [ ] **Etapa 2: Interface Desktop Gráfica em JavaFX e FXML (Fases 9 e 10)**

---

## Fase 1 — Configuração do Ambiente e Estrutura do Projeto Maven
- [ ] **Tarefa 1.1: Criar o arquivo `pom.xml` inicial**
  - *Descrição:* Configurar Java 21 LTS (`maven.compiler.source=21`), codificação UTF-8 e dependência do JUnit 5 (`junit-jupiter-engine`).
  - *Conceito:* Gerenciamento declarativo de dependências e isolamento de ciclo de vida de compilação.
  - *Guia Relacionado:* `guias/01-modelagem-oo-e-encapsulamento.md`
- [ ] **Tarefa 1.2: Criar a árvore de diretórios padrão Maven**
  - *Descrição:* Criar `src/main/java`, `src/test/java`, `src/main/resources` e subpastas de dados `dados/`.
  - *Critério de Aceitação:* O comando `mvn compile` deve executar com sucesso gerando a pasta `target/`.

---

## Fase 2 — Modelagem das Entidades Básicas e Encapsulamento
- [ ] **Tarefa 2.1: Modelar a entidade de pessoa/usuário**
  - *Descrição:* Implementar atributos privados (`matricula`, `nome`, `email`/`tipo`), construtor com validação de invariantes e métodos de leitura (getters).
  - *Conceito:* **Encapsulamento e Defensividade.** Nenhuma entidade deve nascer em estado inválido (strings nulas ou vazias devem disparar `IllegalArgumentException`).
  - *Exemplo de Apoio:* `exemplos/oo/ExemploEncapsulamentoEValidacao.java`
- [ ] **Tarefa 2.2: Implementar métodos de identidade (`equals`, `hashCode` e `toString`)**
  - *Descrição:* Garantir que a unicidade da pessoa seja determinada pela sua chave de negócio (matrícula).
  - *Critério de Aceitação:* Duas instâncias com a mesma matrícula devem ser consideradas iguais em coleções `Set` ou `List.contains()`.

---

## Fase 3 — Regras Polimórficas e Padrão Strategy
- [ ] **Tarefa 3.1: Criar a interface da regra de cálculo/multa**
  - *Descrição:* Definir a assinatura `BigDecimal calcular(long diasAtraso)` ou equivalente.
  - *Conceito:* **Padrão GoF Strategy.** Substituir cadeias de `if/else` e `switch` por polimorfismo dinâmico.
  - *Guia Relacionado:* `guias/02-heranca-polimorfismo-e-strategy.md`
  - *Exemplo de Apoio:* `exemplos/strategy/ExemploStrategyPolimorfico.java`
- [ ] **Tarefa 3.2: Implementar as estratégias concretas**
  - *Descrição:* Implementar as variantes da regra (ex.: regra fixa por dia, regra progressiva com valor inicial maior, e regra de isenção retornando zero).
  - *Critério de Aceitação:* Criar testes unitários para cada estratégia com 0 dias de atraso e com múltiplos dias, validando o cálculo centavo por centavo.

---

## Fase 4 — Hierarquia de Herança das Entidades Principais
- [ ] **Tarefa 4.1: Criar a superclasse abstrata do acervo**
  - *Descrição:* Declarar atributos comuns (`codigo/patrimonio`, `descricao`, `emprestado`), construtores sobrecarregados e injeção do Strategy da regra de cálculo.
  - *Conceito:* **Abstração e Herança.** Impedir instanciação direta da superclasse (`new Item()` é proibido quando a classe é `abstract`).
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
  - *Exemplo de Apoio:* `exemplos/excecao/ExemploExcecaoChecada.java`
- [ ] **Tarefa 5.2: Criar exceções para itens ou registros não encontrados**
  - *Descrição:* Exceções checadas para buscas por identificador ausente.

---

## Fase 6 — Persistência Manual em Arquivos de Texto (CSV)
- [ ] **Tarefa 6.1: Criar interfaces de Repositório (DAO)**
  - *Descrição:* Declarar contratos de acesso a dados (listar, buscar por id, salvar, atualizar).
  - *Conceito:* **Princípio da Inversão de Dependência (DIP).** A regra de negócio não depende de arquivos ou bancos, mas de uma abstração.
- [ ] **Tarefa 6.2: Implementar leitor CSV com `BufferedReader`**
  - *Descrição:* Ler arquivo linha a linha com `try-with-resources`, descartar linhas em branco e comentários iniciados por `#`, e fazer parse dos campos com `split(";")`.
  - *Guia Relacionado:* `guias/04-persistencia-csv-sem-frameworks.md`
  - *Exemplo de Apoio:* `exemplos/csv/ExemploLeituraGravacaoCSV.java`
- [ ] **Tarefa 6.3: Implementar gravador CSV com `BufferedWriter`**
  - *Descrição:* Gravar novos registros formatando campos delimitados por `;` com `newLine()` e `flush()`.
  - *Critério de Aceitação:* O arquivo de texto resultante deve ser legível por editores e ferramentas de planilha sem corrupção de caracteres.

---

## Fase 7 — Camada de Serviço e Encapsulamento de Coleções
- [ ] **Tarefa 7.1: Criar a classe de serviço com injeção de dependências por construtor**
  - *Descrição:* O serviço deve receber as instâncias de repositório em seu construtor, sem nunca executar `new RepositorioArquivo()` dentro de si.
- [ ] **Tarefa 7.2: Implementar proteção de coleções contra mutação externa**
  - *Descrição:* Métodos que retornam listas (ex.: `listarItens()`, `listarEmprestimos()`) devem retornar `Collections.unmodifiableList(...)`.
  - *Conceito:* **Cópia Defensiva e Encapsulamento.** Evita que consumidores externos manipulem o estado interno do serviço via `lista.clear()` ou `lista.add()`.
  - *Guia Relacionado:* `guias/03-colecoes-e-defensive-copying.md`
  - *Exemplo de Apoio:* `exemplos/colecoes/ExemploColecoesProtegidas.java`
- [ ] **Tarefa 7.3: Implementar filtros, buscas e acumuladores**
  - *Descrição:* Criar métodos para buscar por critério, filtrar apenas itens disponíveis e acumular o somatório total de multas em atraso.

---

## Fase 8 — Testes Automatizados e Execução no Terminal (Fim da Etapa 1)
- [ ] **Tarefa 8.1: Escrever suíte de testes com JUnit 5**
  - *Descrição:* Testar isoladamente: estratégias de multa, invariantes, empréstimo com sucesso, disparo da exceção checada ao tentar emprestar duplicado, e devolução.
- [ ] **Tarefa 8.2: Implementar classe de demonstração no Terminal**
  - *Descrição:* Implementar o roteiro oficial de testes da Seção 7 imprimindo o acervo, executando um empréstimo, demonstrando a captura da exceção e exibindo a devolução.
- [ ] **Tarefa 8.3: Criar tag Git e script de execução**
  - *Descrição:* Gerar tag `v1.0-etapa1-modelo` e script `executar-etapa1.sh`.

---

## Fase 9 — Arquitetura da Interface Gráfica JavaFX e FXML
- [ ] **Tarefa 9.1: Adicionar dependências JavaFX ao `pom.xml`**
  - *Descrição:* Incluir `javafx-controls`, `javafx-fxml`, `javafx-graphics`, `javafx-base` e o `javafx-maven-plugin`.
- [ ] **Tarefa 9.2: Implementar classe de ciclo de vida `Application` e bootstrap**
  - *Descrição:* Criar classe herdando de `javafx.application.Application` carregando o arquivo `.fxml` principal, e um `AppLauncher` auxiliar para contornar restrições de inicialização em classpath aberto.
  - *Guia Relacionado:* `guias/06-javafx-arquitetura-fxml-e-mvc.md`
- [ ] **Tarefa 9.3: Modelar os arquivos declarativos FXML**
  - *Descrição:* Criar a interface mestre (`JanelaPrincipal.fxml`) contendo tabelas e rodapé de métricas, desacoplada do código Java.

---

## Fase 10 — Componentes Avançados, Modais e Estilos (Fim da Etapa 2)
- [ ] **Tarefa 10.1: Configurar `TableView` com Cell Value Factories e Badges**
  - *Descrição:* Mapear atributos das entidades nas colunas da tabela e customizar células para exibir indicadores visuais (ex.: verde para disponível, vermelho para emprestado).
  - *Guia Relacionado:* `guias/07-javafx-tabelas-e-cell-factories.md`
  - *Exemplo de Apoio:* `exemplos/javafx/ExemploTabelaEModalJavaFX.java`
- [ ] **Tarefa 10.2: Implementar janelas modais separadas**
  - *Descrição:* Criar modais bloqueantes com `Modality.APPLICATION_MODAL` para realização de novos empréstimos e cadastro de itens no acervo.
  - *Guia Relacionado:* `guias/08-javafx-modais-e-caixas-de-dialogo.md`
- [ ] **Tarefa 10.3: Tratar exceções de domínio com caixas de diálogo `Alert`**
  - *Descrição:* Ao chamar o serviço a partir do controlador JavaFX, envolver a chamada em bloco `try/catch` e exibir a mensagem de erro através de `Alert(AlertType.ERROR)`.
- [ ] **Tarefa 10.4: Criar folha de estilos CSS e script de execução da Etapa 2**
  - *Descrição:* Desenvolver arquivo `styles.css` customizando botões, tabelas e cabeçalhos, e criar script `executar-etapa2.sh`.
- [ ] **Tarefa 10.5: Gerar tag Git e branch de entrega**
  - *Descrição:* Registrar tag `v2.0-etapa2-javafx` e branch `etapa-2-javafx`.
