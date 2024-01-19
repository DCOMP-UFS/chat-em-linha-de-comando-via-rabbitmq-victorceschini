package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Scanner;

public class Emissor
{
  public static void main(String[] argv) throws Exception 
  {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("54.165.215.142"); // Alterar
    factory.setUsername("admin"); // Alterar
    factory.setPassword("awspass123"); // Alterar
    factory.setVirtualHost("/");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    
    System.out.print("User: ");
    Scanner sc = new Scanner(System.in);
    String usuario = sc.nextLine();
                      //(queue-name, durable, exclusive, auto-delete, params); 
    channel.queueDeclare(usuario, false,   false,     false,       null);
    
    String QUEUE_NAME = "";
    String remetente = "";

    while(true)
    {
        // Caso ja tenha um usuario "@nome"
        if(QUEUE_NAME != "")
        {
            System.out.print(remetente + ">> ");

            channel.queueDeclare(QUEUE_NAME, false,   false,     false,       null);

            String message_to_be_sent = sc.nextLine();
            
            // verifica se o usuario deseja mudar o remetente
            if(message_to_be_sent.startsWith("@"))
            {
                remetente = message_to_be_sent;
                QUEUE_NAME = remetente.substring(1);
                continue;
            }

            // envio da mensagem
            String final_message = usuario + " diz: " + message_to_be_sent;
            channel.basicPublish("",       QUEUE_NAME, null,  final_message.getBytes("UTF-8"));
        }
        else
        {
            System.out.print(">> ");
            remetente = sc.nextLine();
            // So ira sair do ">> " quando for inserido um usuario com "@" no inicio
            if(remetente.startsWith("@"))
            {
                QUEUE_NAME = remetente.substring(1);
            }
        }
    }
  }
}
