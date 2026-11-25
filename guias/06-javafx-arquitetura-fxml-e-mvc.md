# Guia 06 — Arquitetura JavaFX, FXML Declarativo e MVC Desacoplado

## 1. O Triângulo Fundamental do JavaFX: Stage, Scene e Nós (Nodes)

A metáfora visual do JavaFX é inspirada no teatro:
- **`Stage` (Palco):** Representa a janela do sistema operacional (com barra de título, botões de minimizar, maximizar e fechar).
- **`Scene` (Cena):** Representa o conteúdo visível dentro da janela. Um palco pode trocar de cena dinamicamente.
- **`Scene Graph` (Árvore de Nós):** É a árvore hierárquica de elementos visuais dentro da cena (`VBox`, `HBox`, `TableView`, `Button`, `Label`).

```text
+-----------------------------------------------------------+
| Stage (Janela do Sistema Operacional)                    |
|  +-----------------------------------------------------+  |
|  | Scene (Cena Ativa)                                  |  |
|  |   +-----------------------------------------------+ |  |
|  |   | Root Node (ex.: BorderPane / VBox)            | |  |
|  |   |   ├── Top: ToolBar                            | |  |
|  |   |   ├── Center: TableView                       | |  |
|  |   |   └── Bottom: HBox (Rodapé de Métricas)       | |  |
|  |   +-----------------------------------------------+ |  |
|  +-----------------------------------------------------+  |
+-----------------------------------------------------------+
```

---

## 2. Por que Usar FXML Desacoplado (Pontuação Bônus)?

O JavaFX permite construir telas de duas formas:
1. **Via Código Java:** Instanciando `new Button()`, `new TableView()` e adicionando em layouts manualmente no código.
2. **Via FXML Declarativo:** Escrevendo um arquivo XML (`.fxml`) que descreve a estrutura visual, enquanto a classe controladora Java contém apenas os tratadores de eventos (`@FXML`).

### Vantagens do FXML:
- **Separação Real entre Visão e Lógica (MVC Puro):** O designer ou desenvolvedor pode editar o layout no **Scene Builder** sem tocar em código Java.
- **Manutenibilidade:** Modificar o visual, as cores ou o espaçamento não exige recompilar classes de lógica.
- **Critério de Avaliação Acadêmica:** O uso de FXML desacoplado frequentemente rende até 5 pontos adicionais na avaliação de projetos desktop.

---

## 3. O Ciclo de Carregamento e Injeção do `FXMLLoader`

Para carregar um FXML e injetar uma camada de serviço existente no controlador:

```java
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ExemploAppJavaFX extends Application {

    private ServicoExemplo servico;

    @Override
    public void init() {
        // Inicialização de repositórios e serviços fora da thread visual
        this.servico = new ServicoExemplo();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MinhaTela.fxml"));
        Parent root = loader.load();

        // Obtendo a instância do controlador criada pelo FXMLLoader
        MinhaTelaControlador controller = loader.getController();
        // Injeção de Dependência no Controlador
        controller.setServico(this.servico);

        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/css/estilo.css").toExternalForm());

        primaryStage.setTitle("Sistema Desktop - Exemplo Didático");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
```

---

## 4. O Padrão `AppLauncher` (Truque de Compatibilidade de JVM)

Ao empacotar projetos JavaFX modernos como JARs executáveis em Java 17/21 sem arquivos de módulo `module-info.java`, a JVM pode recusar a inicialização caso a classe com o método `main` herde diretamente de `javafx.application.Application`.

A solução canônica da comunidade JavaFX é criar uma classe `AppLauncher` intermediária:
```java
public class AppLauncher {
    public static void main(String[] args) {
        // Delega a inicialização para a classe Application real
        Application.launch(ExemploAppJavaFX.class, args);
    }
}
```
Isso garante compatibilidade imediata em qualquer ambiente Linux, macOS ou Windows.
