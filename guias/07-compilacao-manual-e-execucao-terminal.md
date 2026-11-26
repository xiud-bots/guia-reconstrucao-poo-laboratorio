# Guia 07 — Compilação Manual, Execução no Terminal e Roteiro da Seção 7 (Sem Maven)

## 1. Por Que Sem Maven e Sem Frameworks de Teste na Etapa 1?

Na Etapa 1 de projetos acadêmicos e avaliações de Programação Orientada a Objetos, a simplicidade é uma virtude arquitetural:
- **Foco estrito no Modelo de Domínio:** O objetivo é validar o encapsulamento, o polimorfismo, as invariantes, o padrão Strategy para multas e a leitura/escrita manual em CSV.
- **Independência de Ferramentas:** Ferramentas de build como o Apache Maven introduzem convenções de diretórios e plugins que não são necessários quando se está manipulando apenas classes Java padrão. O compilador nativo `javac` e a JVM `java` são mais do que suficientes.
- **Validação por Roteiro Operacional vs. Testes Automatizados:** O enunciado não exige suítes de testes unitários com JUnit 5 ou Mockito. A avaliação da Etapa 1 é conduzida pela execução de um **roteiro determinístico no terminal** (descrito na Seção 7 do documento de requisitos), onde cada requisito funcional é demonstrado no console.

---

## 2. Estrutura de Diretórios em Java Puro (Etapa 1)

Sem o Maven, a raiz do projeto mantém uma estrutura enxuta e intuitiva:

```text
projeto-laboratorio/
├── dados/
│   ├── acervo.csv               # Base inicial de equipamentos
│   └── pessoas.csv              # Base inicial de usuários
├── src/
│   ├── modelo/
│   │   ├── entidade/            # Pessoa, Item, Notebook, Projetor, Kit, Emprestimo
│   │   ├── multa/               # RegraMulta (Strategy), MultaFixa, MultaProgressiva, Isencao
│   │   └── excecao/             # ItemJaEmprestadoException
│   ├── persistencia/            # Interfaces de repositório e leitura/escrita CSV manual
│   ├── servico/                 # ServicoEmprestimo com Injeção de Dependência
│   └── terminal/                # PrincipalTerminal (Ponto de entrada da Etapa 1)
├── executar-etapa1.sh           # Script de compilação e execução automática
└── README.md                    # Documentação do projeto
```

---

## 3. O Roteiro de Demonstração da Seção 7

A classe executável de terminal (`PrincipalTerminal.java`) deve orquestrar sequencialmente os 6 passos exigidos pelo professor:

1. **Apresentar o Acervo Cadastrado:** Listar todos os itens carregados do arquivo `dados/acervo.csv`, exibindo código, descrição, tipo e situação inicial (todos devem constar inicialmente como disponíveis).
2. **Apresentar as Pessoas Cadastradas:** Listar os usuários lidos do arquivo `dados/pessoas.csv` (matrícula, nome, email).
3. **Registrar Empréstimo com Sucesso:** Realizar o empréstimo de um equipamento disponível (ex.: um Notebook) para uma pessoa cadastrada, demonstrando a alteração da situação do item para emprestado.
4. **Demonstrar Tratamento de Exceção (Tentativa Duplicada):** Tentar registrar um novo empréstimo para o **mesmo item** que acabou de ser emprestado. O serviço deve lançar `ItemJaEmprestadoException`, e o método `main` deve capturá-la em um bloco `try/catch`, exibindo a mensagem explicativa no console sem abortar o programa com quebra inesperada (*crash*).
5. **Realizar Devolução com Cálculo de Multa:** Simular a devolução do equipamento com atraso, demonstrando que o cálculo da multa é delegado dinamicamente para o padrão Strategy correspondente ao tipo do item (R$ 5,00/dia para Notebook, cálculo progressivo para Projetor, R$ 0,00 para Kit).
6. **Apresentar o Estado Final do Sistema:** Exibir o acervo atualizado com o item devolvido e a lista de devoluções registradas com valores de multa calculados.

---

## 4. Como Compilar e Executar no Terminal sem Maven

### 4.1 Compilação Direta com `javac`
Para compilar todas as classes Java do projeto enviando os arquivos `.class` para um diretório de saída limpo (`bin/`):

```bash
# Cria o diretório de destino se não existir
mkdir -p bin

# Compila recursivamente todos os arquivos .java dentro de src/
javac -d bin -encoding UTF-8 -cp src $(find src -name "*.java")
```

### 4.2 Execução com `java`
Para rodar a classe principal apontando o classpath para a pasta compilada:

```bash
java -cp bin terminal.PrincipalTerminal
```

---

## 5. Automação com o Script `executar-etapa1.sh`

Crie o arquivo `executar-etapa1.sh` na raiz do projeto com o seguinte conteúdo:

```bash
#!/bin/bash
set -e

echo "=================================================="
echo "  COMPILANDO E EXECUTANDO ETAPA 1 (TERMINAL)     "
echo "  Sem Maven - Java 21 LTS Puro                   "
echo "=================================================="

mkdir -p bin
javac -d bin -encoding UTF-8 -cp src $(find src -name "*.java")

echo "Compilação concluída com sucesso."
echo "Iniciando roteiro da Seção 7..."
echo "--------------------------------------------------"

java -cp bin terminal.PrincipalTerminal
```

Para conceder permissão de execução e rodar:
```bash
chmod +x executar-etapa1.sh
./executar-etapa1.sh
```

---

## 6. Versionamento da Etapa 1
Após validar a execução do roteiro no terminal:
1. Comite as alterações no Git:
   ```bash
   git add .
   git commit -m "feat(etapa-1): implementacao do modelo OO, persistencia CSV e roteiro terminal"
   ```
2. Crie a branch e a tag oficial da entrega:
   ```bash
   git branch etapa-1-terminal
   git tag v1.0-etapa1-modelo
   git push origin main etapa-1-terminal --tags
   ```
