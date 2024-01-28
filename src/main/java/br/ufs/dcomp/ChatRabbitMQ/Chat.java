// Grupo:
// - Victor Gabriel Ceschini Menezes
// - Gabriel Miranda Oliva

package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Chat
{
  private static final String HOST = "54.173.7.197"; // Alterar
  private static final String USERNAME = "admin"; // Alterar
  private static final String PASSWORD = "password"; // Alterar
  private static final String VIRTUAL_HOST = "/";
  private static String remetente = ""; // variavel global para guardar o remetente atual

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

    // cria e executa as threads de sender receiver
    ExecutorService executor = Executors.newFixedThreadPool(2);
    executor.submit(new Receiver(channel, usuario));
    executor.submit(new Sender(channel, usuario));
  }

  public static String getRemetente() {
    return remetente;
  }

  public static void setRemetente(String r){
    remetente = r;
  }

}
