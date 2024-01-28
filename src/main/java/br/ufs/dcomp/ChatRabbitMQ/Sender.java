package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.Channel;

import java.io.IOException;
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

                if(message_to_be_sent.startsWith("@"))
                {
                    Chat.setRemetente(message_to_be_sent);
                    QUEUE_NAME = message_to_be_sent.substring(1);
                    continue;
                }

                String final_message = usuario + " diz: " + message_to_be_sent;
                try {
                    channel.basicPublish("",       QUEUE_NAME, null,  final_message.getBytes("UTF-8"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else
            {
                System.out.print(">> ");

                message_to_be_sent = sc.nextLine();
                if(message_to_be_sent.startsWith("@"))
                {
                    QUEUE_NAME = message_to_be_sent.substring(1);
                    Chat.setRemetente(message_to_be_sent);
                }
            }
        }
    }

}
