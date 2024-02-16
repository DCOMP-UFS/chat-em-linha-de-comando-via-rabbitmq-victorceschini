package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.nio.file.*;
import com.google.protobuf.ByteString;


public class Receiver implements Runnable
{
    Channel channel;
    String usuario;
    String emissor;
    String data;
    String hora;
    String grupo;
    String filename;
    String mensagem_recebida;
    String root_path;
    byte[] corpoMensagem;

    public Receiver(Channel channel, String usuario)
    {
        this.channel = channel;
        this.usuario = usuario;
        emissor = "";
        data = "";
        hora = "";
        mensagem_recebida = "";
        grupo = "";
        filename = "";
        root_path = "/home/ubuntu/environment/downloads/";
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
                    MensagemProto.Mensagem mensagem = MensagemProto.Mensagem.parseFrom(body);
                    
                    // Extraindo dados da mensagem
                    emissor = mensagem.getEmissor();
                    if(!emissor.equals(usuario))
                    {
                        data = mensagem.getData();
                        hora = mensagem.getHora();
                        grupo = mensagem.getGrupo();
                        MensagemProto.Conteudo conteudo = mensagem.getConteudo();
                        filename = conteudo.getNome();
                        
                        System.out.print("\n(" + data + " às " + hora + ") ");
                        
                        // Se for apenas uma mensagem
                        if(filename.equals(""))
                        {
                            mensagem_recebida = conteudo.getCorpo().toStringUtf8();
                    
                            System.out.println(emissor + grupo + " diz: " + mensagem_recebida);
                        }
                        else // Se for um arquivo
                        {
                            corpoMensagem = conteudo.getCorpo().toByteArray();
                            Path default_path = Paths.get(root_path);
                            
                            // Se o diretorio default nao existe, criar um
                            if(!Files.exists(default_path))
                            {
                                Files.createDirectories(Paths.get(root_path));
                            }
                            
                            
                            Path path = Paths.get(root_path + filename);
                            Files.write(path, corpoMensagem);
                            
                            System.out.println("Arquivo \"" + filename + "\" recebido de " + emissor);
                        }


                        // Imprime de volta a parte para enviar mensagem para um remetente
                        System.out.print(Chat.getRemetente() + ">> "); 
                    }
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
