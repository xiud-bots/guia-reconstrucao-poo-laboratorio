# Guia 04 — Persistência Manual em Arquivo de Texto (CSV)

## 1. Por que Sem Frameworks?
Em disciplinas acadêmicas e entrevistas de algoritmos, o objetivo é testar se o desenvolvedor compreende o funcionamento básico de **fluxos de entrada e saída (I/O)**, manipulação de arquivos no sistema operacional e tratamento de recursos de sistema, sem delegar a mágica para bibliotecas de terceiros (como Spring Data, JPA, Hibernate, Gson ou Jackson).

---

## 2. Leitura com `BufferedReader` e `try-with-resources`

### O Padrão `try-with-resources`
Toda classe que implementa `java.lang.AutoCloseable` (como `BufferedReader`, `FileInputStream`, `BufferedWriter`) deve ser aberta dentro dos parênteses do `try`. Ao término do bloco (seja por sucesso ou por exceção), a JVM invoca o `close()` automaticamente, evitando vazamento de descritores de arquivo no sistema operacional:

```java
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class LeitorCSVExemplo {

    public void lerArquivo(Path caminho) throws Exception {
        if (!java.nio.file.Files.exists(caminho)) {
            return; // Trate a ausência do arquivo conforme a regra
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(caminho.toFile()), StandardCharsets.UTF_8))) {
            
            String linha;
            int numeroLinha = 0;
            while ((linha = reader.readLine()) != null) {
                numeroLinha++;
                String linhaLimpa = linha.trim();

                // 1. Ignorar linhas vazias
                if (linhaLimpa.isEmpty()) {
                    continue;
                }

                // 2. Ignorar linhas de comentário iniciadas com #
                if (linhaLimpa.startsWith("#")) {
                    continue;
                }

                // 3. Fatiar campos delimitados por ponto e vírgula
                String[] colunas = linhaLimpa.split(";");
                
                // 4. Validar quantidade mínima de colunas antes de acessar índices
                if (colunas.length < 3) {
                    throw new IllegalArgumentException("Linha " + numeroLinha + " possui formato inválido: " + linha);
                }

                String campo1 = colunas[0].trim();
                String campo2 = colunas[1].trim();
                String campo3 = colunas[2].trim();

                // Processar e instanciar objeto correspondente...
            }
        }
    }
}
```

---

## 3. Escrita com `BufferedWriter`

Para gravar ou anexar registros em um arquivo CSV:
- Para modo *append* (anexar no final sem apagar o arquivo existente): use `new FileWriter(arquivo, StandardCharsets.UTF_8, true)`.
- Para regravação completa do acervo: abra sem o booleano `true`.

```java
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class EscritorCSVExemplo {

    public void salvarRegistro(Path caminho, String id, String descricao, double valor) throws Exception {
        // Modo append (adiciona ao fim do arquivo)
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(caminho.toFile(), true), StandardCharsets.UTF_8))) {
            
            // Monta linha delimitada por ponto e vírgula
            String linha = String.format("%s;%s;%.2f", id, descricao, valor);
            writer.write(linha);
            writer.newLine(); // Insere quebra de linha apropriada ao SO (\n ou \r\n)
            writer.flush();   // Garante a descarga do buffer no disco
        }
    }
}
```

---

## 4. Cuidados Críticos de Engenharia
1. **Charset UTF-8:** Sempre especifique explicitamente `StandardCharsets.UTF_8`. Se você omitir o charset, a JVM usará o padrão da máquina do usuário (que no Windows costuma ser Windows-1252), corrompendo acentos e caracteres especiais como "ç", "ã", "é".
2. **Delimitador Seguro:** O ponto e vírgula (`;`) é preferível à vírgula (`,`) no Brasil porque valores monetários e números decimais em português usam vírgula como separador decimal (ex.: `10,50`). Se usar vírgula como delimitador, o `split(",")` quebrará os números no meio.
3. **Validação de Índices:** Nunca acesse `colunas[3]` sem checar previamente `if (colunas.length > 3)`. Caso contrário, uma linha mal formatada provocará um `ArrayIndexOutOfBoundsException` não tratado.
