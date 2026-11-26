# Guia 08 — Configuração do Maven Exclusivamente para o JavaFX (Etapa 2)

## 1. Por Que Introduzir o Maven Apenas na Etapa 2?

Na **Etapa 1**, o sistema utilizou apenas classes nativas da biblioteca padrão do Java (`java.io`, `java.time`, `java.util`), o que permitiu compilar diretamente com `javac` sem nenhuma ferramenta externa.

A partir do Java 11, contudo, o ecossistema gráfico **JavaFX** foi desmembrado da JDK e transformado em um projeto modular independente (**OpenJFX**). Para executar interfaces JavaFX sem gerenciador de dependências, seria necessário:
1. Baixar manualmente arquivos `.zip` do SDK JavaFX para cada sistema operacional (Linux, Windows, macOS);
2. Configurar variáveis de ambiente e parâmetros complexos de módulos na linha de comando (`--module-path` e `--add-modules javafx.controls,javafx.fxml`).

O **Apache Maven** é introduzido na Etapa 2 com um único propósito objetivo: **resolver automaticamente as dependências de plataforma do JavaFX e fornecer o comando unificado `mvn javafx:run`**.

> **Regra de Ouro da Simplicidade:** Não adicione nenhuma dependência desnecessária ao `pom.xml`. Nada de Spring, nada de JUnit (testes automatizados não foram exigidos), nada de bibliotecas externas de persistência ou parsing (Gson, Jackson, Hibernate).

---

## 2. O `pom.xml` Minimalista e Focado

Abaixo está a configuração completa e suficiente do arquivo `pom.xml` para a raiz do projeto na Etapa 2:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>br.edu.ifpb.poo</groupId>
    <artifactId>controle-emprestimos-laboratorio</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <name>Controle de Emprestimos do Laboratorio</name>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <javafx.version>21.0.2</javafx.version>
    </properties>

    <dependencies>
        <!-- Componentes visuais basicos do JavaFX -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>${javafx.version}</version>
        </dependency>

        <!-- Suporte para telas declarativas em FXML -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-fxml</artifactId>
            <version>${javafx.version}</version>
        </dependency>

        <!-- Motor grafico basico -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-graphics</artifactId>
            <version>${javafx.version}</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Compilador Java configurado para Java 21 LTS -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>21</source>
                    <target>21</target>
                    <encoding>UTF-8</encoding>
                </configuration>
            </plugin>

            <!-- Plugin oficial do OpenJFX para inicializacao da UI -->
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>0.0.8</version>
                <configuration>
                    <!-- Aponta para a classe bootstrap da interface grafica -->
                    <mainClass>br.edu.ifpb.poo.gui.AppLauncher</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 3. Organização de Recursos: Código vs. FXML e CSS

Ao adotar a convenção do Maven na Etapa 2, os recursos visuais devem ser organizados em pastas padronizadas para que o compilador os copie automaticamente para o classpath:

```text
projeto/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── br/edu/ifpb/poo/
│       │       ├── modelo/          # Entidades e regras de multa da Etapa 1
│       │       ├── persistencia/    # Repositórios CSV da Etapa 1
│       │       ├── servico/         # Serviço de negócio da Etapa 1
│       │       └── gui/
│       │           ├── AppJavaFX.java
│       │           ├── AppLauncher.java
│       │           ├── JanelaPrincipalControlador.java
│       │           ├── ModalEmprestimoControlador.java
│       │           └── ModalCadastroItemControlador.java
│       └── resources/
│           ├── fxml/
│           │   ├── JanelaPrincipal.fxml
│           │   ├── ModalEmprestimo.fxml
│           │   └── ModalCadastroItem.fxml
│           └── css/
│               └── styles.css
```

---

## 4. O Problema do Módulo JavaFX e o Padrão `AppLauncher`

Ao tentar rodar uma aplicação JavaFX no Java 17/21 sem um arquivo `module-info.java`, o JavaFX verifica se a classe disparada no `main` herda diretamente de `javafx.application.Application`. Se herdar, a JVM pode recusar a inicialização acusando erro de componentes de runtime ausentes.

Para contornar isso com total elegância e sem precisar configurar o sistema de módulos (JPMS), utiliza-se o padrão **Launcher desvinculado**:

```java
package br.edu.ifpb.poo.gui;

// Esta classe NÃO estende Application!
public class AppLauncher {
    public static void main(String[] args) {
        AppJavaFX.main(args);
    }
}
```

E a classe `AppJavaFX`:
```java
package br.edu.ifpb.poo.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppJavaFX extends Application {
    @Override
    public void start(Stage palcoPrincipal) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/JanelaPrincipal.fxml"));
        Parent raiz = loader.load();
        
        Scene cena = new Scene(raiz, 950, 600);
        palcoPrincipal.setTitle("Controle de Empréstimos do Laboratório");
        palcoPrincipal.setScene(cena);
        palcoPrincipal.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

---

## 5. Script de Execução da Etapa 2 (`executar-etapa2.sh`)

Na raiz do projeto, crie o arquivo `executar-etapa2.sh`:

```bash
#!/bin/bash
set -e

echo "=================================================="
echo "  EXECUTANDO ETAPA 2 (INTERFACE GRAFICA JAVAFX)  "
echo "  Gerenciado via Apache Maven & OpenJFX 21       "
echo "=================================================="

mvn clean javafx:run
```

Conceda permissão com `chmod +x executar-etapa2.sh` e execute com `./executar-etapa2.sh`.
