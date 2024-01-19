package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Chat {

  private static final String HOST = "54.226.130.243"; // Alterar
  private static final String USERNAME = "admin"; // Alterar
  private static final String PASSWORD = "password"; // Alterar
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
    executor.submit(new Receiver(channel, usuario, sc));

    // Thread para envio de mensagens
    executor.submit(new Sender(channel, usuario, sc));
  }

}