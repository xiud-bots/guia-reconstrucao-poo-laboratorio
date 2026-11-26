# Checklist de Tarefas: Reconstrução Passo a Passo do Projeto

> **Princípio Orientador:** Simplicidade, clareza didática e aderência cirúrgica às exigências da especificação.
> 
> 1. **Maven apenas na Etapa 2:** A Etapa 1 utiliza Java puro (`javac`/`java`) com estrutura de pastas tradicional, sem necessidade de ferramentas de automação externas. O Maven é introduzido unicamente na Etapa 2 para gerenciar os módulos nativos do OpenJFX 21.
> 2. **Sem implementação de testes automatizados:** O projeto não exige e não implementa suítes de testes unitários (JUnit/Mockito). A validação é realizada operacionalmente através do roteiro de console exigido pelo professor na Seção 7 (Etapa 1) e da interface visual interativa (Etapa 2).
> 3. **Padrões GoF restritos ao edital:** Nenhum padrão GoF adicional (Factory, Builder, Observer, Visitor, etc.) é implementado. Apenas o padrão **Strategy** exigido explicitamente para o cálculo de multas por tipo de item é utilizado.

---

## 🗺️ Visão Geral das Etapas e Fases

| Etapa | Fases | Foco Principal | Tecnologia / Execução |
| :--- | :--- | :--- | :--- |
| **Etapa 1** | Fases 1 a 7 | Domínio OO, Strategy de Multas, Persistência CSV e Terminal | Java 21 LTS Puro (`javac` / `java`) |
| **Etapa 2** | Fases 8 a 10 | Maven, Interface Gráfica FXML, TableView, Modais e Alertas | OpenJFX 21 + `mvn javafx:run` |

---

# ETAPA 1 — Domínio Orientado a Objetos, Persistência CSV e Execução no Terminal

---

### Fase 1 — Organização do Projeto e Estrutura de Pacotes (Java Puro)
- [ ] **Tarefa 1.1:** Criar a estrutura básica de diretórios sem ferramentas de build:
  - `src/`: código-fonte em pacotes Java.
  - `dados/`: arquivos de texto CSV para persistência.
  - `bin/`: diretório de destino para classes compiladas pelo `javac`.
- [ ] **Tarefa 1.2:** Criar os arquivos de dados iniciais com codificação UTF-8 e delimitador ponto e vírgula (`;`):
  - `dados/acervo.csv`: itens cadastrados com cabeçalho e suporte a comentários `#`.
  - `dados/pessoas.csv`: usuários cadastrados com matrícula, nome e email.
- [ ] **Tarefa 1.3:** Estruturar a divisão de pacotes respeitando a separação de responsabilidades:
  - `modelo.entidade`: classes de negócio puras.
  - `modelo.multa`: interface e estratégias de cálculo de multa.
  - `modelo.excecao`: exceções checadas de domínio.
  - `persistencia`: contratos e implementações de leitura/escrita em arquivo CSV.
  - `servico`: regras de negócio e orquestração.
  - `terminal`: classe com método `main` para execução da Seção 7.
- 📖 *Caderno Didático:* `guias/01-modelagem-oo-e-encapsulamento.md`

---

### Fase 2 — Modelagem das Entidades de Domínio e Encapsulamento
- [ ] **Tarefa 2.1:** Implementar a entidade `Pessoa`:
  - Atributos privados: `matricula` (String), `nome` (String), `email` (String).
  - Validação no construtor: rejeitar valores nulos ou vazios com `IllegalArgumentException`.
  - Sobrescrever `equals()` e `hashCode()` baseados unicamente na matrícula.
  - Sobrescrever `toString()` formatado para apresentação.
- [ ] **Tarefa 2.2:** Criar a superclasse abstrata `Item`:
  - Atributos protegidos/privados: `codigo` (String), `descricao` (String), `emprestado` (boolean), `regraMulta` (`RegraMulta`).
  - Métodos abstratos: `public abstract int getPrazoEmprestimoDias();`.
  - Métodos concretos de negócio: `emprestar()`, `devolver()`, `isEmprestado()`.
  - Método de cálculo de multa delegando ao Strategy: `calcularMulta(long diasAtraso)`.
- [ ] **Tarefa 2.3:** Implementar as subclasses concretas especializadas:
  - `Notebook`: prazo de 7 dias, associado à estratégia `MultaFixa`.
  - `Projetor`: prazo de 2 dias, associado à estratégia `MultaProgressiva`.
  - `KitComponentes`: prazo de 15 dias, associado à estratégia `IsencaoMulta`.
- [ ] **Tarefa 2.4:** Implementar a entidade `Emprestimo` utilizando composição:
  - Atributos: `item` (`Item`), `pessoa` (`Pessoa`), `dataEmprestimo` (`LocalDate`), `dataPrevistaDevolucao` (`LocalDate`), `dataRealDevolucao` (`LocalDate`).
  - Construtor calculando a `dataPrevistaDevolucao` somando os dias do `item.getPrazoEmprestimoDias()`.
  - Método `isAtrasado()` comparando a data atual ou data real com a prevista.
  - Método `getDiasAtraso()` calculando a diferença em dias via `ChronoUnit.DAYS`.
- 📖 *Caderno Didático:* `guias/01-modelagem-oo-e-encapsulamento.md`
- 💻 *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/oo/ExemploEncapsulamentoEValidacao.java`

---

### Fase 3 — Regras de Multa com Padrão GoF Strategy
- [ ] **Tarefa 3.1:** Criar a interface funcional `RegraMulta`:
  - Contrato: `BigDecimal calcular(long diasAtraso);`.
- [ ] **Tarefa 3.2:** Implementar as classes concretas de estratégia conforme o documento de requisitos:
  - `MultaFixa`: valor de R$ 5,00 por dia de atraso (`BigDecimal.valueOf(5.00).multiply(...)`).
  - `MultaProgressiva`: R$ 10,00 no 1º dia de atraso + R$ 5,00 por cada dia adicional subsequente.
  - `IsencaoMulta`: retorno fixo de `BigDecimal.ZERO` (isenção para kits).
- [ ] **Tarefa 3.3:** Garantir que o cálculo trate dias negativos ou zero retornando `BigDecimal.ZERO`.
- ⚠️ *Aviso Didático:* Apenas este padrão GoF é permitido no projeto, atendendo cirurgicamente ao edital.
- 📖 *Caderno Didático:* `guias/02-heranca-polimorfismo-e-strategy.md`
- 💻 *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/strategy/ExemploStrategyPolimorfico.java`

---

### Fase 4 — Exceções de Domínio Personalizadas (Checked Exceptions)
- [ ] **Tarefa 4.1:** Criar a classe `ItemJaEmprestadoException` estendendo diretamente `Exception`:
  - Construtor recebendo o código do item e mensagem descritiva.
  - Força a checagem obrigatória em tempo de compilação quando alguém tentar emprestar um item ocupado.
- [ ] **Tarefa 4.2:** Criar exceções checadas auxiliares para tratamento robusto:
  - `ItemNaoEncontradoException`: lançada quando um código de item inexistente for consultado.
  - `PessoaNaoEncontradaException`: lançada quando uma matrícula não constar na base.
  - `FormatoArquivoInvalidoException`: lançada ao encontrar linhas corrompidas no CSV.
- 📖 *Caderno Didático:* `guias/05-excecoes-de-dominio-e-propagacao.md`
- 💻 *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/excecao/ExemploExcecaoChecada.java`

---

### Fase 5 — Persistência Manual em Arquivo Texto (CSV)
- [ ] **Tarefa 5.1:** Definir os contratos das interfaces de repositório:
  - `IRepositorioAcervo`: listar todos, buscar por código, salvar/atualizar item.
  - `IRepositorioPessoas`: listar todas, buscar por matrícula.
- [ ] **Tarefa 5.2:** Implementar `RepositorioAcervoArquivo` com leitura/escrita manual:
  - Utilizar `BufferedReader` com `try-with-resources` e `StandardCharsets.UTF_8`.
  - Processar cada linha com `split(";")`.
  - Ignorar linhas vazias e comentários iniciados com `#`.
  - Instanciar a subclasse correta (`Notebook`, `Projetor`, `KitComponentes`) de acordo com a coluna do tipo.
  - Gravar novos itens no disco utilizando `BufferedWriter`.
- [ ] **Tarefa 5.3:** Implementar `RepositorioPessoasArquivo` para leitura de usuários no CSV.
- ⚠️ *Aviso Didático:* Proibido o uso de banco de dados (SQLite, H2, PostgreSQL) e de bibliotecas externas (Gson, Jackson, Hibernate). Persistência pura via `java.io`.
- 📖 *Caderno Didático:* `guias/04-persistencia-csv-sem-frameworks.md`
- 💻 *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/csv/ExemploLeituraGravacaoCSV.java`

---

### Fase 6 — Camada de Serviço e Proteção de Coleções
- [ ] **Tarefa 6.1:** Criar a interface `IServicoEmprestimo` e a classe `ServicoEmprestimo`:
  - Aplicar Injeção de Dependências pelo construtor (recebendo `IRepositorioAcervo` e `IRepositorioPessoas`).
- [ ] **Tarefa 6.2:** Implementar as operações de negócio no serviço:
  - `registrarEmprestimo(codigoItem, matricula)`: busca o item e a pessoa; verifica se o item já está emprestado; se estiver, lança `ItemJaEmprestadoException`; caso contrário, marca como emprestado e cria o `Emprestimo`.
  - `registrarDevolucao(codigoItem)`: localiza o empréstimo ativo; calcula a multa caso haja atraso utilizando a estratégia do item; marca o item como disponível e atualiza o acervo.
- [ ] **Tarefa 6.3:** Garantir proteção de coleções com Cópia Defensiva:
  - Métodos `listarItens()` e `listarEmprestimos()` devem retornar coleções envelopadas com `Collections.unmodifiableList(...)`.
- 📖 *Cadernos Didáticos:* `guias/03-colecoes-e-defensive-copying.md` e `guias/06-camada-de-servico-e-injecao-dependencias.md`
- 💻 *Exemplos de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/colecoes/ExemploColecoesProtegidas.java` e `src/main/java/br/edu/ifpb/didatico/exemplos/arquitetura/ExemploInjecaoDependenciasCamadas.java`

---

### Fase 7 — Demonstração do Roteiro no Terminal e Entrega da Etapa 1
- [ ] **Tarefa 7.1:** Criar a classe executável `terminal.PrincipalTerminal` com método `main`:
  - Executar os 6 passos determinísticos da Seção 7 do documento de instruções:
    1. Exibir todos os itens lidos de `dados/acervo.csv` (todos inicialmente disponíveis).
    2. Exibir todas as pessoas lidas de `dados/pessoas.csv`.
    3. Registrar um empréstimo válido (ex.: Notebook para um estudante).
    4. Tentar registrar novo empréstimo para o mesmo Notebook, capturando a `ItemJaEmprestadoException` no bloco `catch` e exibindo mensagem amigável no console sem travar o programa.
    5. Realizar a devolução com simulação de atraso, exibindo o cálculo correto da multa polimórfica.
    6. Exibir o estado final do acervo e histórico de empréstimos.
- [ ] **Tarefa 7.2:** Criar o script shell `executar-etapa1.sh`:
  - Compilar com `javac -d bin -encoding UTF-8 -cp src $(find src -name "*.java")`.
  - Executar com `java -cp bin terminal.PrincipalTerminal`.
- [ ] **Tarefa 7.3:** Formalizar o versionamento Git da Etapa 1:
  - Comitar alterações: `git commit -m "feat(etapa-1): modelo OO, persistencia CSV e roteiro terminal"`.
  - Criar branch: `git branch etapa-1-terminal`.
  - Criar tag oficial: `git tag v1.0-etapa1-modelo`.
- 📖 *Caderno Didático:* `guias/07-compilacao-manual-e-execucao-terminal.md`

---

# ETAPA 2 — Configuração do Maven e Interface Gráfica JavaFX

---

### Fase 8 — Introdução e Configuração do Maven para o JavaFX
- [ ] **Tarefa 8.1:** Criar o arquivo `pom.xml` na raiz do projeto exclusivamente para gerenciar as dependências modulares do OpenJFX 21:
  - Dependências: `javafx-controls`, `javafx-fxml`, `javafx-graphics`, `javafx-base` (versão 21.0.2).
  - Plugin do compilador: `maven-compiler-plugin` com Java 21 LTS (`UTF-8`).
  - Plugin de execução gráfica: `javafx-maven-plugin` apontando para a classe de inicialização.
- [ ] **Tarefa 8.2:** Organizar a estrutura de diretórios para o padrão Maven:
  - `src/main/java/`: código Java (reaproveitando integralmente os pacotes de modelo, persistência e serviço da Etapa 1).
  - `src/main/resources/fxml/`: arquivos de layout de tela `.fxml`.
  - `src/main/resources/css/`: folha de estilo visual `styles.css`.
- ⚠️ *Aviso Didático:* Nenhuma dependência de testes (JUnit) ou framework extra é adicionada ao `pom.xml`.
- 📖 *Caderno Didático:* `guias/08-configuracao-maven-para-javafx.md`

---

### Fase 9 — Arquitetura JavaFX, FXML Declarativo e MVC Desacoplado
- [ ] **Tarefa 9.1:** Criar a classe `AppLauncher` (classe padrão que não herda de `Application` para evitar conflitos de módulo na JVM) que chama `AppJavaFX.main(args)`.
- [ ] **Tarefa 9.2:** Criar a classe `AppJavaFX` estendendo `javafx.application.Application`:
  - No método `init()`, instanciar os repositórios CSV e o `ServicoEmprestimo`.
  - No método `start(Stage primaryStage)`, carregar `JanelaPrincipal.fxml`, aplicar folha de estilo e exibir a janela com título e dimensões adequadas.
- [ ] **Tarefa 9.3:** Desenhar a tela principal declarativa em `src/main/resources/fxml/JanelaPrincipal.fxml`:
  - Barra superior de títulos e ações.
  - Tabela central `TableView` listando os itens do acervo com colunas formatadas.
  - Botões para "Novo Empréstimo", "Devolver Item", "Cadastrar Novo Item" e "Atualizar Tabela".
- [ ] **Tarefa 9.4:** Implementar o controlador `JanelaPrincipalControlador`:
  - Injetar nós da interface com a anotação `@FXML`.
  - Implementar método `initialize()` configurando as colunas e associando a lista observável (`ObservableList`).
  - Vincular eventos de clique dos botões (`onAction`).
- 📖 *Caderno Didático:* `guias/09-javafx-arquitetura-fxml-e-mvc.md`

---

### Fase 10 — Componentes Visuais (TableView), Modais de Ação, Alertas e Entrega da Etapa 2
- [ ] **Tarefa 10.1:** Configurar a `TableView` com `CellValueFactory`:
  - Mapear código, descrição, tipo e situação do equipamento.
  - Atualizar os dados automaticamente após cada operação de empréstimo ou devolução.
- [ ] **Tarefa 10.2:** Implementar telas modais com bloqueio da janela de fundo:
  - `ModalEmprestimo.fxml` e `ModalEmprestimoControlador`: seleção do item e usuário para registro de novo empréstimo com `Modality.APPLICATION_MODAL`.
  - `ModalCadastroItem.fxml` e `ModalCadastroItemControlador`: formulário para cadastrar novos equipamentos (Notebook, Projetor ou Kit) no arquivo `dados/acervo.csv`.
- [ ] **Tarefa 10.3:** Implementar captura gráfica de exceções com diálogo `Alert`:
  - Ao tentar emprestar um item ocupado, capturar a exceção checada `ItemJaEmprestadoException`.
  - Exibir imediatamente um alerta amigável na tela usando `new Alert(AlertType.ERROR)` com título, cabeçalho e descrição do erro.
  - Tratar dados inválidos nos formulários exibindo `Alert(AlertType.WARNING)`.
- [ ] **Tarefa 10.4:** Estilizar a interface com CSS limpo em `src/main/resources/css/styles.css`:
  - Customização de botões, cabeçalhos de tabela e destaque de status (disponível vs. emprestado).
- [ ] **Tarefa 10.5:** Criar o script facilitador `executar-etapa2.sh`:
  - Comando: `mvn clean javafx:run`.
- [ ] **Tarefa 10.6:** Formalizar o versionamento Git da Etapa 2:
  - Comitar alterações: `git commit -m "feat(etapa-2): interface desktop JavaFX, FXML desacoplado, modais e alertas"`.
  - Criar branch: `git branch etapa-2-javafx`.
  - Criar tag oficial: `git tag v2.0-etapa2-javafx`.
- 📖 *Caderno Didático:* `guias/10-javafx-tabelas-modais-e-alertas.md`
- 💻 *Exemplo de Apoio:* `src/main/java/br/edu/ifpb/didatico/exemplos/javafx/ExemploTabelaEModalJavaFX.java`

---

## 🎯 Rastreabilidade Direta com os Parâmetros da Instrução

| Exigência do Professor no PDF | Atendimento no Checklist | Fase |
| :--- | :--- | :--- |
| **JavaFX (não use Swing)** | Interface implementada em FXML e OpenJFX 21 | Fases 8, 9 e 10 |
| **Sem banco de dados (persistência em texto)** | Repositórios manuais com `BufferedReader` e `BufferedWriter` lendo CSV | Fase 5 |
| **Sem concorrência (não use threads)** | Chamadas síncronas diretas sem threads ou pools de tarefas | Fases 6, 9 e 10 |
| **Sem frameworks além do JavaFX** | Sem Spring, sem Hibernate/JPA, sem Jackson/Gson e sem Lombok | Todas as Fases |
| **Código e comentários em português** | Nomenclatura descritiva idiomática sem variáveis genéricas | Todas as Fases |
| **Padrão Strategy para cálculo de multas** | Interface `RegraMulta` com `MultaFixa`, `MultaProgressiva` e `IsencaoMulta` | Fase 3 |
| **Exceção checada `ItemJaEmprestadoException`** | Exceção checada capturada no terminal e tratada via `Alert(AlertType.ERROR)` | Fases 4, 7 e 10 |
| **Proteção de coleções contra mutação externa** | Retornos protegidos com `Collections.unmodifiableList(...)` | Fase 6 |
| **Roteiro de teste funcional (Seção 7)** | Classe `PrincipalTerminal` cumprindo os 6 passos determinísticos | Fase 7 |
| **Maven apenas onde necessário** | Configuração do `pom.xml` exclusivamente na Etapa 2 para o JavaFX | Fase 8 |
| **Sem testes automatizados** | Nenhuma dependência JUnit; validação via terminal e GUI | Todas as Fases |
