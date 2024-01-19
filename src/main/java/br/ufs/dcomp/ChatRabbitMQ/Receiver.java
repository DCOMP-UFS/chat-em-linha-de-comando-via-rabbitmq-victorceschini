package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Receiver implements Runnable{
    Channel channel;
    String usuario;
    Scanner sc;

    public Receiver(Channel channel, String usuario, Scanner sc){
        this.channel = channel;
        this.usuario = usuario;
        this.sc = sc;
    }
    @Override
    public void run() {
        //(queue-name, durable, exclusive, auto-delete, params)
        try {
            channel.queueDeclare(usuario, false,   false,     false,       null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
            try {
                channel.basicConsume(usuario, true,    consumer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
