# Guia 08 — Modais JavaFX, Ciclo de Diálogos e Tratamento Visual de Erros

## 1. Janelas Modais Bloqueantes (`APPLICATION_MODAL`)

Uma janela **modal** bloqueia a interação com a janela principal até que o usuário conclua o preenchimento ou cancele o diálogo. Isso é essencial para operações críticas (como registrar um novo empréstimo ou cadastrar um equipamento).

### Como Instanciar uma Janela Modal a partir do FXML:
```java
public void abrirModalCadastro(Stage janelaPai, ServicoExemplo servico, Runnable aoConcluir) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ModalFormulario.fxml"));
        Parent root = loader.load();

        ModalFormularioControlador controller = loader.getController();
        controller.setServico(servico);
        controller.setCallbackConclusao(aoConcluir);

        Stage modalStage = new Stage();
        modalStage.setTitle("Novo Cadastro");
        modalStage.initOwner(janelaPai);
        modalStage.initModality(Modality.APPLICATION_MODAL); // Bloqueia a janela de trás!
        modalStage.setResizable(false);
        modalStage.setScene(new Scene(root));
        modalStage.showAndWait(); // Pausa o código até a modal ser fechada pelo operador
        
    } catch (IOException e) {
        mostrarErro("Falha ao abrir tela", "Não foi possível carregar o formulário: " + e.getMessage());
    }
}
```

---

## 2. Diálogos Nativos com a Classe `Alert`

O JavaFX disponibiliza o componente `Alert` para comunicação com o usuário:

### 2.1 Diálogo de Erro (para Exceções de Domínio)
```java
public static void mostrarErro(String titulo, String mensagem) {
    Alert alerta = new Alert(Alert.AlertType.ERROR);
    alerta.setTitle(titulo);
    alerta.setHeaderText("Atenção: Ação Não Concluída");
    alerta.setContentText(mensagem);
    alerta.showAndWait();
}
```

### 2.2 Diálogo de Confirmação com Captura de Resposta
```java
public static boolean confirmarAcao(String titulo, String mensagem) {
    Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
    alerta.setTitle(titulo);
    alerta.setHeaderText(null);
    alerta.setContentText(mensagem);

    Optional<ButtonType> resposta = alerta.showAndWait();
    return resposta.isPresent() && resposta.get() == ButtonType.OK;
}
```

---

## 3. O Ciclo Completo de Captura no Botão Salvar

Dentro do controlador da janela modal:

```java
@FXML
private void handleSalvar() {
    String codigo = campoCodigo.getText();
    String responsavel = comboPessoas.getValue();

    // 1. Validação de campos de entrada obrigatórios
    if (codigo == null || codigo.isBlank() || responsavel == null) {
        AlertaUtil.mostrarErro("Validação", "Por favor, preencha todos os campos obrigatórios.");
        return;
    }

    try {
        // 2. Chamada à camada de serviço
        servico.realizarOperacao(codigo, responsavel);
        
        // 3. Feedback de sucesso e fechamento da modal
        AlertaUtil.mostrarInformacao("Sucesso", "Operação realizada com sucesso!");
        if (callbackConclusao != null) {
            callbackConclusao.run(); // Avisa a tela principal para atualizar suas tabelas
        }
        fecharJanela();

    } catch (ItemIndisponivelException e) {
        // 4. Captura CIRÚRGICA da exceção checada de domínio
        AlertaUtil.mostrarErro("Operação Recusada", e.getMessage());
    } catch (IllegalArgumentException e) {
        // 5. Captura de erro de validação
        AlertaUtil.mostrarErro("Dados Inválidos", e.getMessage());
    }
}
```

Esse padrão atende 100% dos critérios de usabilidade, tratamento defensivo e desacoplamento exigidos por bancas avaliadoras de software.
