// Grupo:
// - Victor Gabriel Ceschini Menezes
// - Gabriel Miranda Oliva

package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;

import java.util.Scanner;

public class Chat 
{
  private static final String HOST = "107.23.173.131"; // Alterar
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
    
    Receiver threadReceiver = new Receiver(channel, usuario);

    new Thread(threadReceiver).start();
  }
}
