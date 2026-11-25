# Guia 01 — Modelagem OO, Encapsulamento e Invariantes

## 1. O que é Encapsulamento na Prática?
Encapsulamento não é apenas colocar `private` nas variáveis e gerar getters e setters automáticos na IDE. 
Encapsular significa **proteger o estado interno de um objeto**, garantindo que ele **nunca** passe a existir ou permaneça em um estado inválido ou corrompido.

### O Conceito de Invariante de Classe
Uma **invariante** é uma condição que deve ser verdadeira durante toda a vida útil de um objeto:
- Uma data de nascimento não pode estar no futuro.
- Uma descrição não pode ser nula nem vazia (`isBlank()`).
- Um preço ou quantidade não pode ser negativo.
- Um identificador único deve seguir o formato exigido pelas regras de negócio.

---

## 2. Padrões de Implementação Defensiva

### 2.1 Validação no Construtor
O construtor é o guardião da vida do objeto. Se parâmetros inválidos forem fornecidos, o construtor deve imediatamente interromper a instanciação lançando uma exceção apropriada (geralmente `IllegalArgumentException` ou `NullPointerException`):

```java
public class ProdutoExemplo {
    private final String codigo;
    private String nome;
    private double preco;

    public ProdutoExemplo(String codigo, String nome, double preco) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("O código do produto não pode ser nulo ou vazio.");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do produto não pode ser vazio.");
        }
        if (preco < 0.0) {
            throw new IllegalArgumentException("O preço não pode ser negativo.");
        }
        this.codigo = codigo.trim();
        this.nome = nome.trim();
        this.preco = preco;
    }
}
```

### 2.2 Sobrecarga de Construtores com `this(...)`
Quando uma classe oferece valores padrão para alguns parâmetros, a sobrecarga deve ser encadeada chamando o construtor mais completo via `this(...)`, centralizando as validações em um único ponto:

```java
public class VeiculoExemplo {
    private final String placa;
    private final String modelo;
    private boolean alugado;

    // Construtor principal (completo)
    public VeiculoExemplo(String placa, String modelo, boolean alugado) {
        if (placa == null || placa.isBlank()) throw new IllegalArgumentException("Placa obrigatória.");
        if (modelo == null || modelo.isBlank()) throw new IllegalArgumentException("Modelo obrigatório.");
        this.placa = placa;
        this.modelo = modelo;
        this.alugado = alugado;
    }

    // Sobrecarga de conveniência: veiculo novo nasce desocupado (alugado = false)
    public VeiculoExemplo(String placa, String modelo) {
        this(placa, modelo, false);
    }
}
```

### 2.3 Métodos com Nomes Expressivos de Negócio (Tell, Don't Ask)
Em vez de expor `setEmprestado(true)` ou `setDisponivel(false)`, prefira métodos que reflitam a intenção da operação:
- `item.emprestar()`
- `item.devolver()`

Isso impede que código externo force transições de estado ilegais sem passar pelas validações internas do objeto.

---

## 3. Armadilhas Comuns (*Common Pitfalls*)
1. **Setters sem validação:** Se você colocar um `setNome(String nome)` sem checar se é nulo, qualquer classe externa pode quebrar as invariantes do seu objeto a qualquer momento.
2. **Construtores vazios em entidades ricas:** Evite construtores sem parâmetros do tipo `public Item() {}` em modelos de domínio, a menos que uma biblioteca de serialização o exija estritamente.
3. **Identidade mal definida:** Não esqueça de implementar `equals` e `hashCode` baseados no identificador de negócio (patrimônio, matrícula, CPF).
