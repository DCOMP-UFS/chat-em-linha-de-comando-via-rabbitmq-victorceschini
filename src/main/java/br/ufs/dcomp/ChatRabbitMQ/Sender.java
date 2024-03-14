package br.ufs.dcomp.ChatRabbitMQ;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonToken;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.protobuf.ByteString;
import com.google.gson.stream.JsonReader;

public class Sender implements Runnable
{
    private Channel channel;
    private Channel fileChannel;
    private String usuario;
    private String QUEUE_NAME;
    private String data;
    private String hora;
    private String remetente;
    private String root_path;
    private Scanner sc;
    
    public Sender(Channel channel, Channel fileChannel, String usuario)
    {
        this.channel = channel;
        this.fileChannel = fileChannel;
        this.usuario = usuario;
        QUEUE_NAME = "";
        data = "";
        hora = "";
        remetente = "";
        root_path = "/home/ubuntu/environment";
        sc = new Scanner(System.in);
    }

    @Override
    public void run() 
    {
        while(true)
        {
            // Caso ja tenha um usuario "@nome"
            if(QUEUE_NAME != "")
            {
                System.out.print(Chat.getRemetente() + ">> ");

                String message_to_be_sent = sc.nextLine();
                try {
                    handleInput(message_to_be_sent);
                } catch (IOException | InterruptedException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
            else
            {
                System.out.print(">> ");

                String message_to_be_sent = sc.nextLine();
                try {
                    handleInput(message_to_be_sent);
                } catch (IOException | InterruptedException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    // determina o que sera feito com o input do usuario
    public void handleInput(String message) throws IOException, InterruptedException, URISyntaxException {
        char inicio = message.charAt(0);
        switch(inicio){
            // Caso tenha operador @ ou #, atualiza-se o QUEUE_NAME e remetente
            case '@':
            case '#':
                QUEUE_NAME = message.substring(1);
                Chat.setRemetente(message);
                break;
            // Caso tenha operador !, chama-se a funcao para lidar com os comandos
            case '!':
                handleCommand(message);
                break;
            // Caso padrao que corresponde a enviar uma mensagem
            default:
                sendMessage(message);
                break;
        }
    }

    // monta e publica a nova mensagem para um grupo ou usuario
    public void sendMessage(String message) throws IOException {
        byte[] buffer = new byte[0];

        // Serializando a mensagem pelo metodo serializeText
        try{
            buffer = serializeText(message);
        } catch (Exception e){
            System.out.println("Arquivo nao encontrado!");
        }
        
        // verifica se a mensagem eh privada ou para um grupo
        if(Chat.getRemetente().startsWith("@")){
            channel.basicPublish("", QUEUE_NAME, null,  buffer);
        } else{
            channel.basicPublish(QUEUE_NAME, "", null,  buffer);
        }
    }

    public void sendFile(String message) throws  IOException{
        System.out.println("Enviando \"" + message + "\" para " + Chat.getRemetente());

        byte[] buffer = new byte[0];
        buffer = serializeFile(message);

        ExecutorService executor = Executors.newFixedThreadPool(5);
        executor.submit(new fileSender(buffer, QUEUE_NAME, fileChannel));
        //executor.close();
    }

    // determina o que fazer em cada comando digitado pelo usuario (comandos iniciam com "!")
    public void handleCommand(String message) throws IOException, InterruptedException, URISyntaxException {
        String[] splitString = message.split(" ");
        String comando = splitString[0];
        String groupName = "";
        String user = "";
        String path = "";

        switch (comando){
            case "!addGroup":
                // cria o grupo e adiciona o criador a ele
                groupName = splitString[1];
                channel.exchangeDeclare(groupName, "fanout", true);
                channel.queueBind(usuario, groupName, "");
                break;
            case "!addUser":
                user = splitString[1];
                groupName = splitString[2];
                channel.queueBind(user, groupName, "");
                break;
            case "!delFromGroup":
                user = splitString[1];
                groupName = splitString[2];
                channel.queueUnbind(user, groupName, "");
                break;
            case "!removeGroup":
                groupName = splitString[1];
                channel.exchangeDelete(groupName);
                break;
            case "!upload":
                path = splitString[1];
                sendFile(path);
                break;
            case "!listUsers":
                listUsers();
                break;
            case "!listGroups":
                listGroups();
                break;
            default:
                System.out.println("Comando invalido");
                break;
        }
    }
    
    public String getDate()
    {
        // Obtém a data atual
        Date dataAtual = new Date();

        // Define o formato desejado
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy','HH:mm");

        // Formata a data para o formato desejado
        String dataFormatada = formato.format(dataAtual);
        
        return dataFormatada;
    }
    
    public MensagemProto.Conteudo.Builder createConteudo(String tipo, byte[] byteArray, String filename)
    {
        MensagemProto.Conteudo.Builder conteudo = MensagemProto.Conteudo.newBuilder();
        conteudo.setTipo(tipo);
        conteudo.setCorpo(ByteString.copyFrom(byteArray));
        conteudo.setNome(filename);
        
        return conteudo;
    }
    
    public byte[] createMensagem(String data, String hora, MensagemProto.Conteudo.Builder conteudo)
    {
        MensagemProto.Mensagem.Builder builderMensagem = MensagemProto.Mensagem.newBuilder();
        builderMensagem.setEmissor(usuario);
        builderMensagem.setData(data);
        builderMensagem.setHora(hora);
        builderMensagem.setConteudo(conteudo);
        
        remetente = Chat.getRemetente();
        if(remetente.charAt(0) == '#')
        {
            builderMensagem.setGrupo(remetente);
        }
        else
        {
            builderMensagem.setGrupo("");
        }
        
        // Obtendo a mensagem
        MensagemProto.Mensagem mensagemUsuario = builderMensagem.build();
        
        return mensagemUsuario.toByteArray();
    }
    
    // Metodo para serializar uma mensagem que nao contem arquivos ou imagens
    public byte[] serializeText(String message)
    {
        byte[] byteArray = message.getBytes();
        
        // Agrupando dados do conteudo da mensagem
        // sabendo que contem apenas texto, sem imagens ou arquivos
        MensagemProto.Conteudo.Builder conteudo = createConteudo("text/plain", byteArray, "");
        
        // Obtém a data atual
        String dataFormatada = getDate();

        String[] dataSplit = dataFormatada.split(",");

        data = dataSplit[0];
        hora = dataSplit[1];
        
        // Serializando a mensagem
        byte[] buffer = createMensagem(data, hora, conteudo);
        return buffer;
    }
    
    public byte[] serializeFile(String path) throws IOException{
        Path source = Paths.get(root_path + path);
         
        // Lendo os bytes do arquivo
        byte[] byteArray = Files.readAllBytes(source);
        
        // Pegando o Tipo MIME do arquivo
        String tipoMime = Files.probeContentType(source);
        
        // Pegando o nome do arquivo
        String fileName = source.getFileName().toString();
    
    
        // Agrupando dados do conteudo da mensagem
        // sabendo que contem apenas texto, sem imagens ou arquivos
        MensagemProto.Conteudo.Builder conteudo = createConteudo(tipoMime, byteArray, fileName);
        
        // Obtém a data atual
        String dataFormatada = getDate();

        String[] dataSplit = dataFormatada.split(",");

        data = dataSplit[0];
        hora = dataSplit[1];
        
        // Serializando a mensagem
        byte[] buffer = createMensagem(data, hora, conteudo);
        return buffer;
    }

    public void listUsers() throws IOException, InterruptedException, URISyntaxException {
        String url = "http://" + Chat.getHost() + ":15672" + "/api/queues";
        String json = doRequest(url);
        getUsers(json);
    }

    public void listGroups() throws IOException, URISyntaxException, InterruptedException {
        String url = "http://" + Chat.getHost() + ":15672" + "/api/exchanges";
        String json = doRequest(url);
        getGroups(json);
    }

    public String doRequest(String url) throws IOException, InterruptedException, URISyntaxException {
        HttpClient client = HttpClient.newHttpClient();

        String credentials = Chat.getUsername() + ":" + Chat.getPassword();
        String headerValue = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .header("Authorization", headerValue)
                .build();

        HttpResponse<String> resposta = client.send(request, HttpResponse.BodyHandlers.ofString());

        return resposta.body();
    }

    public void getUsers(String json) throws IOException {
        Gson gson = new Gson();
        Type queueListType = new TypeToken<ArrayList<JsonObject>>(){}.getType();
        ArrayList<JsonObject> queues = gson.fromJson(json, queueListType);

        for(JsonObject queue : queues){
            System.out.print(queue.get("name") + ", ");
        }

        System.out.println();
    }

    public void getGroups(String json) throws IOException {
        Gson gson = new Gson();
        Type queueListType = new TypeToken<ArrayList<JsonObject>>(){}.getType();
        ArrayList<JsonObject> queues = gson.fromJson(json, queueListType);

        for(JsonObject queue : queues){
            if(queue.get("user_who_performed_action").toString().contains("admin")) System.out.print(queue.get("name") + ", ");
        }

        System.out.println();
    }
}
