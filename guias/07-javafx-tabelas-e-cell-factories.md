# Guia 07 — Tabelas JavaFX, CellValueFactory e Customização Visual

## 1. O Funcionamento do `TableView<T>`

O `TableView` do JavaFX é um dos componentes mais poderosos para aplicações de gestão. Ele opera associando uma lista observável de itens (`ObservableList<T>`) a colunas (`TableColumn<T, TipoDaColuna>`).

### 1.1 Configuração Declarativa no FXML
```xml
<TableView fx:id="tabelaItens" VBox.vgrow="ALWAYS">
    <columns>
        <TableColumn fx:id="colunaId" text="Código" prefWidth="120" />
        <TableColumn fx:id="colunaDescricao" text="Descrição" prefWidth="250" />
        <TableColumn fx:id="colunaSituacao" text="Situação" prefWidth="150" />
    </columns>
</TableView>
```

---

## 2. CellValueFactory: Ligando POJOs às Colunas

Se o seu modelo de domínio for composto por **POJOs puros** (sem propriedades do JavaFX para manter o núcleo limpo e desacoplado), configure o `CellValueFactory` usando lambdas que empacotam os dados em `SimpleStringProperty` ou `SimpleObjectProperty`:

```java
@FXML private TableView<ItemExemplo> tabelaItens;
@FXML private TableColumn<ItemExemplo, String> colunaId;
@FXML private TableColumn<ItemExemplo, String> colunaDescricao;
@FXML private TableColumn<ItemExemplo, String> colunaSituacao;

@FXML
public void initialize() {
    // Configura como cada coluna extrai o dado da linha
    colunaId.setCellValueFactory(dado -> 
            new SimpleStringProperty(dado.getValue().getCodigo()));
            
    colunaDescricao.setCellValueFactory(dado -> 
            new SimpleStringProperty(dado.getValue().getDescricao()));
            
    colunaSituacao.setCellValueFactory(dado -> 
            new SimpleStringProperty(dado.getValue().isOcupado() ? "Ocupado" : "Disponível"));
}
```

---

## 3. CellFactory: Customização Visual com Badges e Cores

Para alterar o estilo visual de uma célula (ex.: pintar um texto de verde se estiver disponível e vermelho se estiver ocupado, ou desenhar um selo/badge arredondado):

```java
colunaSituacao.setCellFactory(coluna -> new TableCell<ItemExemplo, String>() {
    @Override
    protected void updateItem(String situacao, boolean vazio) {
        super.updateItem(situacao, vazio);

        if (vazio || situacao == null) {
            setText(null);
            setStyle("");
            getStyleClass().removeAll("badge-disponivel", "badge-ocupado");
        } else {
            setText(situacao);
            if (situacao.equalsIgnoreCase("Disponível")) {
                setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            } else {
                setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        }
    }
});
```

---

## 4. Atualização Reativa de Listas

Para abastecer a tabela, utilize sempre uma `ObservableList`:
```java
// Converte a lista do domínio em lista observável do JavaFX
ObservableList<ItemExemplo> itensObservaveis = FXCollections.observableArrayList(servico.listarTodos());
tabelaItens.setItems(itensObservaveis);

// Quando houver uma nova inclusão ou alteração de estado:
itensObservaveis.setAll(servico.listarTodos()); // Atualiza a tabela na tela instantaneamente!
```
