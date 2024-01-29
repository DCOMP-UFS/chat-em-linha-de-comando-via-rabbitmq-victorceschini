package br.ufs.dcomp.ChatRabbitMQ;

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
    private String message_to_be_sent;
    
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
        try {                 //(queue-name, durable, exclusive, auto-delete, params);
            channel.queueDeclare(usuario, false,   false,     false,       null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while(true)
        {
            // Caso ja tenha um usuario "@nome"
            if(QUEUE_NAME != "")
            {
                System.out.print(Chat.getRemetente() + ">> ");

                try {
                    channel.queueDeclare(QUEUE_NAME, false,   false,     false,       null);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                message_to_be_sent = sc.nextLine();
                handleInput(message_to_be_sent);
            }
            else
            {
                System.out.print(">> ");

                message_to_be_sent = sc.nextLine();
                handleInput(message_to_be_sent);
            }
        }
    }

    // determina o que sera feito com o input do usuario
    public void handleInput(String message){
        char inicio = message.charAt(0);
        switch(inicio){
            case '@':
                QUEUE_NAME = message.substring(1);
                Chat.setRemetente(message);
                break;
            case '!':

                break;
            case '#':

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
}
