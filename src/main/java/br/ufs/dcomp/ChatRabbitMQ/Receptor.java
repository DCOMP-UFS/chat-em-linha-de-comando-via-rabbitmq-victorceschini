package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Scanner;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Receptor
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
    String QUEUE_NAME = sc.nextLine();
                      //(queue-name, durable, exclusive, auto-delete, params); 
    channel.queueDeclare(QUEUE_NAME, false,   false,     false,       null);
    
    while(true)
    {
        Consumer consumer = new DefaultConsumer(channel) 
        {
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException
            {
                String message = new String(body, "UTF-8");
                
                // Obtém a data atual
                Date dataAtual = new Date();

                // Define o formato desejado
                SimpleDateFormat formato = new SimpleDateFormat("(dd/MM/yyyy 'às' HH:mm)");

                // Formata a data para o formato desejado
                String dataFormatada = formato.format(dataAtual);
                
                System.out.println(dataFormatada + " " + message);
            }
        };
                          //(queue-name, autoAck, consumer);    
        channel.basicConsume(QUEUE_NAME, true,    consumer);
    } 
  }
}
