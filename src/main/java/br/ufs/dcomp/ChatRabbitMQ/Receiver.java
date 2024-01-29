package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Receiver implements Runnable
{
    Channel channel;
    String usuario;

    public Receiver(Channel channel, String usuario)
    {
        this.channel = channel;
        this.usuario = usuario;
    }
    
    @Override
    public void run() 
    {

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
                    
                    // Imprime de volta a parte para enviar mensagem para um remetente
                    System.out.print(Chat.getRemetente() + ">> ");
                }
            };
            
            try {                 //(queue-name, autoAck, consumer);
                channel.basicConsume(usuario, true,    consumer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
