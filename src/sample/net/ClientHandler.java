package sample.net;

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

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            inputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            new Thread(()->{
                try {
                    auth();
                    readMessages();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    closeConnections();
                }
            }).start();

        } catch (SocketException e){
            System.out.println(nickname + " отключился");
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
            if (str.equals("/end")) {
                System.out.println("Client disconnect!");
                break;
            }
            //проверяем на отправку определенному
            if (str.startsWith("/w ")) {
                server.messageToNickname(this, str.split("\\s")[1], str.split("\\s")[2]);
            } else {
                // иначе отправляем всем
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
                if (newNick != null){
                    if (!server.isNickBusy(newNick)){
                        nickname = newNick;
                        message("/authok "+ nickname);
                        server.subscribe(this);
                        server.broadcastMessage(this, "Клиент " + nickname + " подключился!");
                        break;
                    } else {
                        message("Учетная запись уже используется");
                    }
                } else {
                    message("Неверный логин или пароль");
                }
            }
        }
    }

    void message(String message){
        try{
            dataOutputStream.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }
}
