# Guia 05 — Exceções de Domínio e Propagação de Erros

## 1. Checked vs. Unchecked Exceptions no Java

| Característica | Checked Exceptions | Unchecked Exceptions |
| :--- | :--- | :--- |
| **Herança Direta** | Herdam de `java.lang.Exception` | Herdam de `java.lang.RuntimeException` |
| **Verificação do Compilador** | **Obrigatória.** O código nem compila se não houver `try/catch` ou declaração `throws` na assinatura. | **Opcional.** O compilador permite que passem sem tratamento explícito. |
| **Significado Semântico** | Falhas esperadas de **regra de negócio** das quais o sistema ou o usuário pode se recuperar (ex.: item já ocupado, saldo insuficiente). | Erros de programação, bugs de código ou violações de contrato (ex.: `NullPointerException`, `IllegalArgumentException`). |

---

## 2. Como Criar uma Exceção de Domínio Checada

Uma exceção de domínio deve ser autoexplicativa, herdar de `Exception` e disponibilizar construtores que recebam mensagens claras ou referências aos objetos envolvidos:

```java
public class ItemIndisponivelException extends Exception {
    private final String codigoItem;

    public ItemIndisponivelException(String codigoItem) {
        super("O item com código '" + codigoItem + "' já se encontra ocupado no momento.");
        this.codigoItem = codigoItem;
    }

    public ItemIndisponivelException(String codigoItem, String mensagemAdicional) {
        super(mensagemAdicional);
        this.codigoItem = codigoItem;
    }

    public String getCodigoItem() {
        return codigoItem;
    }
}
```

---

## 3. A Regra de Ouro da Propagação em Camadas

```text
[ Camada de Interface (View / Controller) ]
   │
   │  Chama servico.registrar(...)
   ▼
[ Camada de Serviço (Regras de Negócio) ]
   │  Detecta que o item está indisponível
   │  Lança: throw new ItemIndisponivelException(codigo);
   ▼
[ Camada de Interface (View / Controller) ]
   │
   ▲  Captura: catch (ItemIndisponivelException e)
   │
   └─── Exibe: Alert(AlertType.ERROR, e.getMessage()) ou mensagem no Terminal
```

### Na Camada de Domínio / Serviço:
O serviço **NÃO** deve exibir caixas de diálogo na tela e **NÃO** deve imprimir no console (`System.out.println`). Ele deve apenas validar e lançar o erro:
```java
public void realizarEmprestimo(String codigoItem, String usuario) throws ItemIndisponivelException {
    Item item = buscarItem(codigoItem);
    if (item.isEmprestado()) {
        throw new ItemIndisponivelException(codigoItem);
    }
    item.emprestar();
}
```

### Na Camada de Apresentação (JavaFX / Terminal):
A interface captura a exceção checada e traduz em feedback compreensível para o ser humano:
```java
// No Controller JavaFX:
try {
    servico.realizarEmprestimo(codigo, usuario);
    AlertaUtil.mostrarSucesso("Empréstimo registrado com sucesso!");
} catch (ItemIndisponivelException e) {
    // Alerta visual imediato para o operador
    AlertaUtil.mostrarErro("Operação Recusada", e.getMessage());
}
```

---

## 4. Anti-Patterns Que Custam Pontos na Avaliação
1. **O Catch Fantasma (`Empty Catch Block`):**
   ```java
   // ERRO GRAVE: O erro acontece, é ignorado silenciosamente e o sistema se corrompe
   try { ... } catch (Exception e) {}
   ```
2. **Engolir o Erro com `printStackTrace()`:**
   ```java
   // ERRO: Imprime no log escuro do terminal e finge para o usuário que deu tudo certo
   try { ... } catch (ItemIndisponivelException e) {
       e.printStackTrace();
   }
   ```
3. **Captura Genérica Demais (`catch (Exception e)`):**
   Sempre capture as exceções específicas do seu domínio antes de capturar qualquer exceção genérica.
