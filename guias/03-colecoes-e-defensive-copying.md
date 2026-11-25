# Guia 03 — Coleções, Encapsulamento e Cópia Defensiva

## 1. O Problema do Vazamento de Referência
Imagine que a sua classe de serviço guarde internamente uma lista de empréstimos:
```java
public class ServicoInseguro {
    private final List<Registro> lista = new ArrayList<>();

    // ANTI-PATTERN PERIGOSO:
    public List<Registro> getLista() {
        return this.lista;
    }
}
```
Se um código consumidor fizer:
```java
List<Registro> dados = servico.getLista();
dados.clear(); // O ESTADO INTERNO DO SEU SISTEMA FOI DESTRUÍDO DE FORA PRA DENTRO!
```
Isso é uma quebra catastrófica de **encapsulamento**. Qualquer parte externa do sistema pode adicionar itens duplicados, remover registros ou reordenar a lista sem que o serviço saiba.

---

## 2. A Solução: `Collections.unmodifiableList`

Para proteger suas coleções, nunca retorne a referência crua. Retorne uma **visão imutável** (*unmodifiable view*):

```java
public class ServicoSeguro {
    private final List<Registro> registros = new ArrayList<>();

    public List<Registro> listarRegistros() {
        // Retorna uma view protegida. Se alguém chamar add() ou remove(),
        // a JVM lança imediatamente UnsupportedOperationException!
        return Collections.unmodifiableList(this.registros);
    }
}
```

### Diferença entre `Collections.unmodifiableList` e `List.copyOf`:
- `Collections.unmodifiableList(lista)`: É uma visão sobre a lista original. Se o serviço adicionar um item internamente, a lista externa reflete a mudança, mas ninguém de fora pode modificá-la.
- `List.copyOf(lista)`: Cria uma cópia estática independente no momento da chamada (Java 10+).

---

## 3. Buscas, Filtros e Acumuladores com Streams e Loops

O enunciado de sistemas orientados a objetos frequentemente exige três operações sobre coleções:

### 3.1 Busca por Critério Único (ex.: buscar por código)
```java
public Optional<ItemExemplo> buscarPorCodigo(String codigo) {
    if (codigo == null || codigo.isBlank()) return Optional.empty();
    return this.itens.stream()
            .filter(item -> item.getCodigo().equalsIgnoreCase(codigo.trim()))
            .findFirst();
}
```

### 3.2 Filtro de Subconjunto (ex.: listar apenas disponíveis)
```java
public List<ItemExemplo> listarDisponiveis() {
    return this.itens.stream()
            .filter(ItemExemplo::isDisponivel)
            .toList(); // Retorna lista imutável no Java 16+
}
```

### 3.3 Acumulador / Redução (ex.: soma total de valores)
```java
public BigDecimal calcularTotalAcumulado() {
    return this.registros.stream()
            .map(Registro::getValor)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
}
```

---

## 4. Testando a Imutabilidade no JUnit 5
Para garantir que seu encapsulamento funciona, escreva um teste unitário:
```java
@Test
void deveImpedirModificacaoExternaDaColecao() {
    ServicoSeguro servico = new ServicoSeguro();
    List<Registro> lista = servico.listarRegistros();
    
    assertThrows(UnsupportedOperationException.class, () -> {
        lista.clear();
    });
}
```
Essa validação demonstra domínio sênior de boas práticas de segurança de memória e encapsulamento em Java.
