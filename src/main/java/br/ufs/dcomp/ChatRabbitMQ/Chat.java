package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Chat {

    private static final String HOST = "54.159.23.151"; // Alterar
    private static final String USERNAME = "admin"; // Alterar
    private static final String PASSWORD = "awspass123"; // Alterar
    private static final String VIRTUAL_HOST = "/";
    
    public static void main(String[] args) throws Exception
    {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);
        factory.setVirtualHost(VIRTUAL_HOST);


        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        System.out.print("User: ");
        Scanner sc = new Scanner(System.in);
        String usuario = sc.nextLine();

        // Executor para gerenciar threads
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Thread para recebimento de mensagens
        executor.submit(() -> {
            try {
                receiveMessages(channel, usuario);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Thread para envio de mensagens
        executor.submit(() -> {
            try {
                sendMessages(channel, usuario, sc);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private static void receiveMessages(Channel channel, String usuario) throws IOException 
    {
                          //(queue-name, durable, exclusive, auto-delete, params)
        channel.queueDeclare(usuario, false,   false,     false,       null);
    
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
                
                    System.out.println('\n' + dataFormatada + " " + message);
                    System.out.print(">> ");
                }
            };
                            //(queue-name, autoAck, consumer);    
            channel.basicConsume(usuario, true,    consumer);
        } 
    }

    private static void sendMessages(Channel channel, String usuario, Scanner sc) throws IOException, InterruptedException
    {
                          //(queue-name, durable, exclusive, auto-delete, params); 
        channel.queueDeclare(usuario, false,   false,     false,       null);
    
        String QUEUE_NAME = "";
        String remetente = "";

        while(true)
        {
        
            if(QUEUE_NAME != "")
            {
                System.out.print(remetente + ">> ");

                channel.queueDeclare(QUEUE_NAME, false,   false,     false,       null);

                String message_to_be_sent = sc.nextLine();
            
                if(message_to_be_sent.startsWith("@"))
                {
                    remetente = message_to_be_sent;
                    QUEUE_NAME = remetente.substring(1);
                    continue;
                }

                String final_message = usuario + " diz: " + message_to_be_sent;
                channel.basicPublish("",       QUEUE_NAME, null,  final_message.getBytes("UTF-8"));
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
