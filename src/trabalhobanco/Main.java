/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalhobanco;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

/**
 *
 * @author Bruno Galeazzi Rech
 */
public class Main {

    protected static int nBytes = 249;
    protected static long NUMTUPLAS = 0; //quantidade tuplas que serao geradas

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String nomearquivo = "trabalhollll.dat"; //nome do arquivo
        File arq = new File(nomearquivo); //objeto para manipulacao fisica do arquivo
        //arq.delete(); // apaga arquivo
        try {
            RandomAccessFile arquivo = new RandomAccessFile(nomearquivo, "rw");
            Control.abrirArquivo(arquivo);//abre o arquivo
            NUMTUPLAS = arquivo.length() / nBytes;//atualiza o nº de tuplas
            menu(arquivo);//abre o menu
        } catch (IOException e) {
            System.exit(1);
        }
    }

    protected static void menu(RandomAccessFile arquivo) {//menu para o usuário
        System.out.println("\nGerenciador de arquivos\n"
                + "1 - Cadastrar novo Carro\n"
                + "2 - Buscar Carro\n"
                + "3 - Alterar Carro\n"
                + "4 - Excluir Carro\n"
                + "5 - Imprimir todos os registros (Formato Árvore)\n"
                + "6 - Imprimir Tuplas\n"
                + "7 - Sair\n");
        Scanner sc = new Scanner(System.in);
        switch (sc.nextInt()) {
            case 1:
                Control.cadastrarCarro(arquivo);
                break;
            case 2:
                System.out.println("1 - Busca por nome\n"
                        + "2 - Busca por chassi\n"
                        + "3 - Voltar\n");
                switch (sc.nextInt()) {
                    case 1:
                        System.out.println("Digite o nome do carro:");
                        String nome = String.format("%1$45s", sc.next());
                        long pos = Control.procurarPorNome(arquivo, nome, 0);
                        if (pos == -1) {
                            System.out.println("Nenhum arquivo encontrado por esse nome\n");
                        } else {
                            Control.imprimirPos(arquivo, pos);
                        }
                        break;
                    case 2:
                        System.out.println("Digite o número do chassi:");
                        String chassi = String.format("%1$30s", sc.next());
                        pos = Control.procurarPorChassi(arquivo, chassi, 0);
                        if (pos == -1) {
                            System.out.println("Nenhum arquivo encontrado por esse número de chassi\n");
                        } else {
                            Control.imprimirPos(arquivo, pos);
                        }
                        break;
                }
                break;
            case 3:
                System.out.println(""
                        + "1 - Editar pelo nome\n"
                        + "2 - Editar pelo chassi\n"
                        + "3 - Voltar\n");
                long pos = -1;
                switch (sc.nextInt()) {
                    case 1:
                        System.out.println("Digite o nome do Carro:");
                        String nome = String.format("%1$45s", sc.next());
                        pos = Control.procurarPorNome(arquivo, nome, 0);
                        break;
                    case 2:
                        System.out.println("Digite o chassi do Carro:");
                        String chassi = String.format("%1$30s", sc.next());
                        pos = Control.procurarPorChassi(arquivo, chassi, 0);
                        break;
                }
                if (pos != -1) {
                    System.out.println("Dados do elemento:\n");
                    Control.imprimirPos(arquivo, pos);
                } else {
                    System.out.println("Arquivo Não encontrado");
                    break;
                }
                System.out.println("1 - Editar nome\n"
                        + "2 - Editar cor\n"
                        + "3 - Editar quantidade\n"
                        + "4 - Editar valor\n"
                        + "5 - Voltar\n");
                try {
                    switch (sc.nextInt()) {
                        case 1:
                            System.out.println("Digite o novo nome: ");
                            arquivo.seek(pos + 60);
                            arquivo.writeChars(String.format("%1$45s", sc.next()));
                            break;
                        case 2:
                            System.out.println("Digite a nova cor: ");
                            arquivo.seek(pos + 162);
                            arquivo.writeChars(String.format("%1$35s", sc.next()));
                            break;
                        case 3:
                            System.out.println("Digite a nova quantidade: ");
                            arquivo.seek(pos + 158);
                            arquivo.writeInt(sc.nextInt());
                            break;
                        case 4:
                            System.out.println("Digite o novo valor: ");
                            arquivo.seek(pos + 150);
                            arquivo.writeDouble(sc.nextDouble());
                            break;
                    }
                    System.out.println("\n Elemento editado com sucesso!\n");
                    Control.imprimirPos(arquivo, pos);
                    break;
                } catch (Exception e) {
                    System.out.println("Erro na edição de arquivos!!");
                    break;
                }
            case 4:
                System.out.println("1 - Excluir pelo nome\n"
                        + "2 - Excluir pelo chassi");
                switch (sc.nextInt()) {
                    case 1:
                        System.out.println("Digite o nome do Carro:");
                        String nome = String.format("%1$45s", sc.next());
                        Control.excluirCarro(arquivo, nome, "null");                        
                        break;
                    case 2:
                        System.out.println("Digite o chassi do Carro:");
                        String chassi = String.format("%1$30s", sc.next());
                        Control.excluirCarro(arquivo, "null", chassi);
                        break;
                }
                break;
            case 5:
                Control.exibirArvore(arquivo, 0, 1);
                break;
            case 6:
                Control.lerTudo(arquivo);
                break;
            case 7:
                System.exit(0);
                break;
        }
        menu(arquivo);
    }
}
