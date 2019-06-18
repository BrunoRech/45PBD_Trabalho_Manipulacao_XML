/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalhobanco;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

/**
 * @since 23/10/2018
 * @version
 * @author Bruno Galeazzi Rech
 */
public class Control {

    protected static void lerTudo(RandomAccessFile arquivo) {//método para ler todas as tuplas
        try {
            System.out.println("\nMostrando todos os dados do arquivo:\n");
            System.out.println("Posição\t|Chassi\t|Nome\t\t|Valor\t\t|Qt\t|Cor\t\t|Ecluído?\t|Esq\t|Dir");
            long nT = arquivo.length() / Main.nBytes;
            arquivo.seek(0); // posiciona na posicao inicial do arquivo
            for (int i = 0; i < Main.NUMTUPLAS; i++) {
                String chassi = lerString(arquivo, 30);//lê o chassi
                String nome = lerString(arquivo, 45);//lê o nome
                Double valor = arquivo.readDouble();//lê o valor
                int qt = arquivo.readInt();//lê a qt
                String cor = lerString(arquivo, 35);//lê a cor
                boolean excluido = arquivo.readBoolean();//lê EL
                long esquerda = arquivo.readLong();//lê o ponteiro da esq
                long direita = arquivo.readLong();//lê o ponteiro da dir
                if (excluido) {//se o elemento foi excluído
                    System.out.println((i * 249) + " \t| [==============================[ITEM EXCLUÍDO!]==============================]|"+ esquerda + " \t| " + direita);
               } else {
                    System.out.println((i * 249) + " \t| " + chassi.trim() + " \t| " + nome.trim() + " \t| " + valor + " \t| " + qt + " \t| " + cor.trim()
                            + " \t| " + excluido + " \t| " + esquerda + " \t| " + direita); //trim() remove espacos em branco antes e depois da string
               }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    protected static void abrirArquivo(RandomAccessFile arquivo) {//método para abrir o arquivo e mostrar o seu tamanho
        //Prof. Thiago
        try {
            double tamArq = arquivo.length();//recupera o tamanho do arquivo
            System.out.println("depois de reabrir o arquivo, ele tem o tamanho de " + tamArq + " bytes.");
        } catch (IOException ex) {

        }
    }

    protected static void gravar(RandomAccessFile arquivo, String chassi, String nome, String cor, int quantidade, double valor, long pos,
            boolean escreverPonteiros) { //método para efetuar a gravação no arquivo
        try {
            arquivo.seek(pos);//aponta para a pos do arquivo desejada
            arquivo.writeChars(String.format("%1$30s", chassi)); //escreve chassi em hexadecimal
            arquivo.writeChars(String.format("%1$45s", nome));//escreve nome +60
            arquivo.writeDouble(valor); //escreve valor +150
            arquivo.writeInt(quantidade); // escreve quantidade +158
            arquivo.writeChars(String.format("%1$35s", cor)); //escreve a cor +162
            arquivo.writeBoolean(false); // Exclusao lógica +232
            if (escreverPonteiros) {//se for necessário deixar os ponteiros como padrão
                arquivo.writeLong(-1); // apontador Esq
                arquivo.writeLong(-1); // apontador Dir
            }
        } catch (Exception ex) {
        }
    }

    protected static void processar(RandomAccessFile arquivo, String chassi, String nome, String cor, int quantidade, double valor, long inicio) {//método para processar os elementos
        String chassiRaiz;
        boolean p1 = false;//aux
        boolean p2 = false;//aux
        try {
            long tamanhoArquivo = arquivo.length();//recupera posicao do final do arquivo
            if (tamanhoArquivo == 0) {//se não tiver nenhum elemento salvo no arquivo
                gravar(arquivo, chassi, nome, cor, quantidade, valor, tamanhoArquivo, true);
                Main.NUMTUPLAS++; // incrementa nro de tuplas
                System.out.println("O Primeiro elemento foi gravado com sucesso no arquivo!\n");
            } else {//se tiver mais que 1
                if (procurarPorChassi(arquivo, chassi, 0) != -1) {//procura se já existe 1 chassi igual cadastrado
                    System.out.println("Já existe um chassi com esse valor cadastrado!");
                } else {//se for um chassi que ainda não foi cadastrado
                    if (carroExcluido(arquivo, inicio)) {//verifica se a tupla foi excluida
                        arquivo.seek(inicio + Main.nBytes - 8);
                        long posDir = arquivo.readLong();//pega o elemento da direita
                        if (posDir != -1) {//se existe algo no apontador da direita
                            arquivo.seek(posDir);
                            if (chassi.compareTo(lerString(arquivo, 30)) < 0) {//se o elemento da direita for maior
                                p1 = true;
                            }
                        } else {//se não tiver nada no apontador da direita
                            p1 = true;
                        }
                        arquivo.seek(inicio + Main.nBytes - 16);
                        long posEsq = arquivo.readLong();//pega o elemento da esquerda
                        if (posEsq != -1) {//se existe algo no apontador da esquerda
                            arquivo.seek(posEsq);
                            if (chassi.compareTo(lerString(arquivo, 30)) > 0) {//se o elemento da esquerda for menor
                                p2 = true;
                            }
                        } else {//se não tiver nada no apontador da esquerda
                            p2 = true;
                        }
                        if (p1 && p2) {//se as 2 condições forem satisfeitas significa que posso sobreescrever o arquivo excluído pelo novo
                            gravar(arquivo, chassi, nome, cor, quantidade, valor, inicio, false);
                            System.out.println("Gravado com sucesso sobre um arquivo apagado!");
                        } else if (p1 && !p2) {//elemento da esquerda é maior
                            gravar(arquivo, chassi, nome, cor, quantidade, valor, tamanhoArquivo, true);
                            atualizarPonteiros(arquivo, tamanhoArquivo, chassi, posEsq);//vai para a esquerda
                            Main.NUMTUPLAS++;
                        } else if (!p1 && p2) {//elemento da direita é menor
                            gravar(arquivo, chassi, nome, cor, quantidade, valor, tamanhoArquivo, true);
                            atualizarPonteiros(arquivo, tamanhoArquivo, chassi, posDir);//vai para a direita
                            Main.NUMTUPLAS++;
                        }

                    } else { // se a tupla não foi excluída, encontrar um lugar para escrever o arquivo
                        arquivo.seek(inicio);//começa a busca no 0         
                        chassiRaiz = lerString(arquivo, 30);//monta o chassi da raíz
                        if (chassi.compareTo(chassiRaiz) < 0) {//se for menor
                            arquivo.seek(inicio + Main.nBytes - 16);
                            inicio = arquivo.readLong();//pega a pos do elemento do apontador da esquerda
                            if (inicio != -1) {//se existir elemento na esquerda
                                processar(arquivo, chassi, nome, cor, quantidade, valor, inicio);
                            } else {//se a pos da esquerda estiver vago
                                gravar(arquivo, chassi, nome, cor, quantidade, valor, tamanhoArquivo, true);
                                atualizarPonteiros(arquivo, tamanhoArquivo, chassi, 0);
                                Main.NUMTUPLAS++; // incrementa nro de tuplas
                                System.out.println("Gravado com sucesso no fim do arquivo!\n");
                            }
                        } else if (chassi.compareTo(chassiRaiz) > 0) {//se for maior
                            arquivo.seek(inicio + Main.nBytes - 8);
                            inicio = arquivo.readLong();//pega a pos do elemento do apontador da direita
                            if (inicio != -1) {//se existir elemento da direita
                                processar(arquivo, chassi, nome, cor, quantidade, valor, inicio);
                            } else {//se a pos da direita estiver vago
                                gravar(arquivo, chassi, nome, cor, quantidade, valor, tamanhoArquivo, true);
                                atualizarPonteiros(arquivo, tamanhoArquivo, chassi, 0);
                                Main.NUMTUPLAS++; // incrementa nro de tuplas
                                System.out.println("Gravado com sucesso no fim do arquivo!\n");
                            }
                        }

                    }
                }

            }
        } catch (Exception ex) {
            System.out.println("ERRO NA GRAVAÇÃO DE ARQUIVOSSSSS");
        }
    }

    protected static void atualizarPonteiros(RandomAccessFile arquivo, long posicaoNovoElemento, String chassiFilho, long inicioComparador) {//método para atualizar os ponteiros da direita/esquerda
        try {
            arquivo.seek(inicioComparador);//ajusta ponteiro
            String chassiRaiz = lerString(arquivo, 30);//pega o chassi da raiz
            if (chassiFilho.compareTo(chassiRaiz) > 0) {//se for maior 
                arquivo.seek(inicioComparador + Main.nBytes - 8); //ajusta o ponteiro para escrever no apontador da direita
                long aux = arquivo.readLong();//lê o valor que está dentro
                if (aux == -1) {//verifica se está vago
                    arquivo.seek(inicioComparador + Main.nBytes - 8);//ajusta novamente
                    arquivo.writeLong(posicaoNovoElemento);//sobreescreve com a posição do arquivo novo
                } else {//está ocupado
                    inicioComparador = aux;//recebe o valor da casa ocupada
                    atualizarPonteiros(arquivo, posicaoNovoElemento, chassiFilho, inicioComparador);//recorre ao método com um novo parâmetro de início
                }
            } else if (chassiFilho.compareTo(chassiRaiz) < 0) {//se for menor
                arquivo.seek(inicioComparador + Main.nBytes - 16);//ajusta o ponteiro para escrever no apontador da esquerda
                long aux = arquivo.readLong();//lê o valor que está dentro
                if (aux == -1) {//verifica se está vago
                    arquivo.seek(inicioComparador + Main.nBytes - 16);//ajusta novamente
                    arquivo.writeLong(posicaoNovoElemento);//sobreescreve com a posição do arquivo novo
                } else {//está ocupado
                    inicioComparador = aux;//recebe o valor da casa ocupada
                    atualizarPonteiros(arquivo, posicaoNovoElemento, chassiFilho, inicioComparador);//recorre ao método com um novo parâmetro de início
                }
            }
        } catch (Exception ex) {
            System.out.println("erro ");
        }
    }

    protected static long procurarPorChassi(RandomAccessFile arquivo, String chassi, long inicio) {//método para buscar um chassi
        String chassiRaiz = "";
        try {
            arquivo.seek(inicio);//começa a busca no 0            
            chassiRaiz = lerString(arquivo, 30);//monta o chassi da raíz
            if (chassi.compareTo(chassiRaiz) < 0) {//se for menor
                arquivo.seek(inicio + Main.nBytes - 16);
                inicio = arquivo.readLong();//pega a pos do elemento do apontador da esquerda
                inicio = procurarPorChassi(arquivo, chassi, inicio);
            } else if (chassi.compareTo(chassiRaiz) > 0) {//se for maior
                arquivo.seek(inicio + Main.nBytes - 8);
                inicio = arquivo.readLong();//pega a pos do elemento do apontador da direita
                inicio = procurarPorChassi(arquivo, chassi, inicio);
            } else if (chassi.compareTo(chassiRaiz) == 0) {// se for igual
                arquivo.seek(inicio + 232);
                if (arquivo.readBoolean()) { //validar se foi excluido
                    return -1;
                } else {//se o elemento não estiver excluído
                    return inicio;
                }
            }
            return inicio;
        } catch (Exception ex) {//não encontrou o chassi
            return -1;
        }

    }

    protected static void excluirCarro(RandomAccessFile arquivo, String nome, String chassi) {//método para excluir um carro
        try {
            long pos = -1;
            if (!nome.equalsIgnoreCase("null")) {//se a busca será feita pelo nome
                pos = procurarPorNome(arquivo, nome, 0);
            } else if (!chassi.equalsIgnoreCase("null")) {//se a busca será feita pelo chassi
                pos = procurarPorChassi(arquivo, chassi, 0);
            }
            arquivo.seek(pos + Main.nBytes - 17);//ajusta o ponteiro para a pos do EL do elemento
            arquivo.writeBoolean(true);//atualiza o EL
            System.out.println("Tupla excluída com sucesso!\n");
        } catch (Exception ex) {
            System.out.println("Erro na exclusão\n");
        }
    }

    protected static void cadastrarCarro(RandomAccessFile arquivo) {//Método de Input dos atributos do carro
        Scanner sc = new Scanner(System.in);//scanner
        String chassi, nome, cor;
        int quantidade;
        double valor;
        System.out.println("\nEscreva o chassi do carro:");
        chassi = String.format("%1$30s", sc.next());//pega os 30chars do chassi
        System.out.println("Escreva o nome do carro:");
        nome = String.format("%1$45s", sc.next());//pega os 45chars do nome
        System.out.println("Escreva o valor do carro:");
        valor = sc.nextDouble();//pega o valor do carro
        System.out.println("Escreva a quantidade:");
        quantidade = sc.nextInt();//pega a qt
        System.out.println("Escreva a cor do carro:");
        cor = String.format("%1$35s", sc.next());//pega os 35chars da cor do carro

        processar(arquivo, chassi, nome, cor, quantidade, valor, 0);//manda para o processamento
    }

    protected static long procurarPorNome(RandomAccessFile arquivo, String nome, long ponteiro) {//Método para procura do carro pelo nome
        System.out.println("\nProcurando pelo nome: " + nome.trim() + " nos arquivos...");
        try {
            for (int i = 0; i < Main.NUMTUPLAS; i++) {
                arquivo.seek(ponteiro + 60);//ajusta o ponteiro
                String nomeRaiz = lerString(arquivo, 45);//lê o nome
                if (nomeRaiz.trim().equalsIgnoreCase(nome.trim())) {//se o nome for = ao que o usuário procura
                    arquivo.seek(ponteiro + 232);//ajusta o ponteiro
                    if (arquivo.readBoolean()) {//se o carro foi excluído
                        return -1;
                    } else {//senão retorna a pos do elemento no arquivo
                        return ponteiro;
                    }
                } else {
                    ponteiro += Main.nBytes;//pula para o próximo elemento
                }
            }
        } catch (Exception ex) {
            return -1;
        }
        return -1;
    }

    protected static void imprimirPos(RandomAccessFile arquivo, long pos) { //Método para imprimir 1 pos específica
        try {
            arquivo.seek(pos);//ajusta o ponteiro para a pos
            String chassi = lerString(arquivo, 30);//lê o chassi
            String nome = lerString(arquivo, 45);//lê o nome
            Double valor = arquivo.readDouble();//lê o valor
            int qt = arquivo.readInt();//lê a qt
            String cor = lerString(arquivo, 35);//lê a cor
            boolean excluido = arquivo.readBoolean();//lê o EL
            long esquerda = arquivo.readLong();//lê o apontador da esquerda
            long direita = arquivo.readLong();//lê o apontador da direita
            if (excluido) {//se o arquivo foi excluído
                System.out.println("[==============================[ITEM EXCLUÍDO!]==============================][Ponteiro Esq: " + esquerda + "][Ponteiro Dir: " + direita + "]");
            } else {//senão
                System.out.println("[Chassi: " + chassi.trim() + "][Nome: " + nome.trim() + "][Valor: " + valor + "][Quantidade: " + qt + "][Cor: " + cor.trim()
                        + "][Excluído? " + excluido + "][Ponteiro Esq: " + esquerda + "][Ponteiro Dir: " + direita + "]");
            }

        } catch (IOException ex) {
            System.out.println("ERRO NO MÉTODO IMPRIMIR_POS");
        }
    }

    protected static void exibirArvore(RandomAccessFile arquivo, long raiz, int nivel) {//imprime a árvore
        try {
            long esq, dir;//auxiliares
            arquivo.seek(raiz + Main.nBytes - 16);
            esq = arquivo.readLong();//pega a pos da esquerda
            arquivo.seek(raiz + Main.nBytes - 8);
            dir = arquivo.readLong();//pega a pos da direita
            if (nivel == 1) {
                System.out.print("\n- Raiz ->");
            }
            imprimirPos(arquivo, raiz);//printa a raiz
            if (dir != -1) {//se tiver filho na direita
                System.out.println("|");
                for (int i = 0; i < nivel; i++) {
                    System.out.print("------ ");
                }
                System.out.print(nivel + " - Filho Direito ->");
                exibirArvore(arquivo, dir, nivel + 1);//printa filho da direita com recursão
            }
            if (esq != -1) {//se tiver filho na esquerda
                System.out.println("|");
                for (int i = 0; i < nivel; i++) {
                    System.out.print("------ ");
                }
                System.out.print(nivel + " - Filho Esquerdo ->");
                exibirArvore(arquivo, esq, nivel + 1);//printa filho da esquerda com recursão
            }
        } catch (Exception ex) {
            System.out.println("ERRO NA EXIBIÇÃO DA ÁRVORE");
        }

    }

    protected static boolean carroExcluido(RandomAccessFile arquivo, long pos) {//método para validar o carro
        try {
            arquivo.seek(pos + Main.nBytes - 17);//arruma a pos
            boolean excluido = arquivo.readBoolean();//lê o boolean
            if (excluido) {//se o carro foi excluído
                return true;
            }    
        } catch (Exception e) {
            System.out.println("ERRO no método checarIntegridade");
        }
        return false;//se o carro NÃO foi excluido
    }
    protected static String lerString(RandomAccessFile arquivo, int caracteres) {//lê string
        String palavra = "";
        try {
            for (int i = 0; i < caracteres; i++) {
                palavra += arquivo.readChar();//monta a palavra
            }
        } catch (Exception ex) {
            System.out.println("ERRO NA LEITURA DA STRING");
            ex.printStackTrace();
        }
        return palavra;//retorna a palavra
    }
}
