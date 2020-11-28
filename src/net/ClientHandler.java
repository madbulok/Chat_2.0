package net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler {
    private Server server = null;
    private Socket socket = null;
    private DataOutputStream dataOutputStream;
    private DataInputStream inputStream;
    private String nickname ="";
    private String login ="";

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            inputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            socket.setSoTimeout(120000); // 2 min, а можно тут
            new Thread(()->{
                try {
                    auth();
                    readMessages();
                } catch (IOException ex) {
                    System.out.println(ex.getLocalizedMessage());
                } finally {
                    closeConnections();
                }
            }).start();

        } catch (SocketException e){
            System.out.println(nickname + " отключился");
            server.unsubscribe(this);
        }catch (IOException ex) {
            System.out.println("Непредвиденная ошибка");
        }
    }

    private void closeConnections() {
        server.unsubscribe(this);
        server.broadcastMessage(this, nickname+" вышел из чата");
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessages() throws IOException  {
        while (true){
            String str = inputStream.readUTF();
            if(str.startsWith("/")) {

                if (str.equals("/end")) {
                    dataOutputStream.writeUTF("/end");
                    break;
                }

                if (str.startsWith("/w")) {
                    String[] token = str.split("\\s+", 3);
                    if (token.length < 3){
                        continue;
                    }
                    server.messageToNickname(this, token[1], token[2]);
                }

            } else {
                server.broadcastMessage(this, str);
            }
        }
    }

    private void auth() throws IOException {
        while (true){
            String str = inputStream.readUTF();
            if (str.startsWith("/auth")){
                String[] token = str.split("\\s");
                String newNick = server.getAuthService().getNickNameByLoginAndPass(token[1], token[2]);

                login = token[1];

                if (newNick != null){
                    if (!server.isLoginAuthenticated(token[1])){
                        nickname = newNick;
                        sendMessage("/authok "+ nickname);
                        server.subscribe(this);
                        server.broadcastMessage(this, "Клиент " + nickname + " подключился!");
                        break;
                    } else {
                        sendMessage("Учетная запись уже используется");
                    }
                } else {
                    sendMessage("Неверный логин или пароль");
                }
            }

            if (str.startsWith("/reg")){
                String[] token = str.split("\\s");
                if(token.length < 4){
                    continue;
                }
                boolean isRegistration = server.getAuthService()
                        .doRegistration(token[1], token[2], token[3]);
                if(isRegistration){
                    sendMessage("/regok");
                } else {
                    sendMessage("/regno");
                }
            }
        }
    }

    void sendMessage(String message){
        try{
            dataOutputStream.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }

    public String getLogin() {
        return login;
    }
}
