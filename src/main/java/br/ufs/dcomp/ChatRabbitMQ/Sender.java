package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.Scanner;

public class Sender implements Runnable{
    Channel channel;
    String usuario;
    Scanner sc;

    public Sender(Channel channel, String usuario){
        this.channel = channel;
        this.usuario = usuario;
    }

    @Override
    public void run() {
        //(queue-name, durable, exclusive, auto-delete, params);
        try {
            channel.queueDeclare(usuario, false,   false,     false,       null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        sc = new Scanner(System.in);

        String QUEUE_NAME = "";
        String remetente = "";

        while(true)
        {
            if(QUEUE_NAME != "")
            {
                System.out.print(remetente + ">> ");

                try {
                    channel.queueDeclare(QUEUE_NAME, false,   false,     false,       null);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                String message_to_be_sent = sc.nextLine();

                if(message_to_be_sent.startsWith("@"))
                {
                    remetente = message_to_be_sent;
                    QUEUE_NAME = remetente.substring(1);
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

                remetente = sc.nextLine();
                if(remetente.startsWith("@"))
                {
                    QUEUE_NAME = remetente.substring(1);
                }
            }
        }

    }

}
