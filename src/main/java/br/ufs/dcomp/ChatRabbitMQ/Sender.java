package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

public class Sender implements Runnable
{
    private Channel channel;
    private String usuario;
    private String QUEUE_NAME;
    private Scanner sc;
    
    public Sender(Channel channel, String usuario)
    {
        this.channel = channel;
        this.usuario = usuario;
        QUEUE_NAME = "";
        sc = new Scanner(System.in);
    }

    @Override
    public void run() 
    {

        while(true)
        {
            // Caso ja tenha um usuario "@nome"
            if(QUEUE_NAME != "")
            {
                System.out.print(Chat.getRemetente() + ">> ");

                String message_to_be_sent = sc.nextLine();
                try {
                    handleInput(message_to_be_sent);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else
            {
                System.out.print(">> ");

                String message_to_be_sent = sc.nextLine();
                try {
                    handleInput(message_to_be_sent);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    // determina o que sera feito com o input do usuario
    public void handleInput(String message) throws IOException {
        char inicio = message.charAt(0);
        switch(inicio){
            case '@':
            case '#':
                QUEUE_NAME = message.substring(1);
                Chat.setRemetente(message);
                break;
            case '!':
                handleCommand(message);
                break;
            default:
                sendMessage(message);

        }
    }

    // monta e publica a nova mensagem
    public void sendMessage(String message) {
        String final_message = usuario + " diz: " + message;
        try {
            channel.basicPublish("",       QUEUE_NAME, null,  final_message.getBytes("UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // determina o que fazer em cada comando digitado pelo usuario (comandos iniciam com "!")
    public void handleCommand(String message) throws IOException {
        String[] slpitString = message.split(" ");
        if(message.contains("addGroup")){
            // cria o grupo e adiciona o criador a ele
            String groupName = slpitString[1];
            System.out.println("grupo: " + groupName);
            channel.exchangeDeclare(groupName, "fanout", true);
            channel.queueBind(usuario, groupName, "");
        } else if(message.contains("addUser")){
            String user = slpitString[1];
            String groupName = slpitString[2];
            channel.queueBind(user, groupName, "");
        } else if(message.contains("delFromGroup")) {

        } else if(message.contains("removeGroup")){

        } else{
            System.out.println("Comando invalido");
        }
    }
}
