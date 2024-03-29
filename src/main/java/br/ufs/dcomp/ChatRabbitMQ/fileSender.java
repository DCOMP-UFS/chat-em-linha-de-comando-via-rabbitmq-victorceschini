package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;

import java.io.IOException;

public class fileSender implements Runnable{
    private final String queueName;
    private byte[] buffer = new byte[0];
    private Channel fileChannel;

    public fileSender(byte[] buffer, String queueName, Channel fileChannel){
        this.buffer = buffer;
        this.queueName = queueName;
        this.fileChannel = fileChannel;
    }

    @Override
    public void run() {
        // verifica se a mensagem eh privada ou para um grupo
        if(Chat.getRemetente().startsWith("@")){
            try {
                fileChannel.basicPublish("", queueName, null,  buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else{
            try {
                fileChannel.basicPublish(queueName, "", null,  buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
