package br.edu.ifpb.didatico.exemplos.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Exemplo Didático: Demonstração de leitura e gravação de arquivos CSV
 * com delimitador ponto e vírgula (;), ignorando linhas vazias e comentários (#),
 * utilizando apenas classes nativas do JDK (BufferedReader e BufferedWriter).
 */
public class ExemploLeituraGravacaoCSV {

    // Modelo genérico para fins de demonstração
    public record RegistroExemplo(String codigo, String descricao, double valor) {}

    public static List<RegistroExemplo> lerArquivo(File arquivo) throws IOException {
        List<RegistroExemplo> registros = new ArrayList<>();

        if (!arquivo.exists()) {
            return registros;
        }

        try (BufferedReader leitor = new BufferedReader(
                new InputStreamReader(new FileInputStream(arquivo), StandardCharsets.UTF_8))) {

            String linha;
            int numeroLinha = 0;
            while ((linha = leitor.readLine()) != null) {
                numeroLinha++;
                String linhaLimpa = linha.trim();

                // Ignora linhas vazias ou comentários
                if (linhaLimpa.isEmpty() || linhaLimpa.startsWith("#")) {
                    continue;
                }

                String[] partes = linhaLimpa.split(";");
                if (partes.length < 3) {
                    System.err.println("Aviso: Linha " + numeroLinha + " ignorada por formato inválido: " + linha);
                    continue;
                }

                String codigo = partes[0].trim();
                String descricao = partes[1].trim();
                double valor = Double.parseDouble(partes[2].trim().replace(",", "."));

                registros.add(new RegistroExemplo(codigo, descricao, valor));
            }
        }

        return registros;
    }

    public static void gravarRegistro(File arquivo, RegistroExemplo registro) throws IOException {
        // Segundo parâmetro true ativa o modo append (anexa no final do arquivo)
        try (BufferedWriter escritor = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(arquivo, true), StandardCharsets.UTF_8))) {

            String linha = String.format("%s;%s;%.2f", 
                    registro.codigo(), 
                    registro.descricao(), 
                    registro.valor());

            escritor.write(linha);
            escritor.newLine();
            escritor.flush();
        }
    }

    public static void main(String[] args) throws IOException {
        File arquivoTeste = File.createTempFile("exemplo-csv-", ".csv");
        arquivoTeste.deleteOnExit();

        // 1. Gravando dados
        gravarRegistro(arquivoTeste, new RegistroExemplo("REG-01", "Equipamento A", 150.00));
        gravarRegistro(arquivoTeste, new RegistroExemplo("REG-02", "Equipamento B", 280.50));

        // 2. Lendo dados de volta
        List<RegistroExemplo> carregados = lerArquivo(arquivoTeste);
        System.out.println("Itens carregados do CSV:");
        for (RegistroExemplo r : carregados) {
            System.out.printf(" - [%s] %s -> R$ %.2f%n", r.codigo(), r.descricao(), r.valor());
        }
    }
}
