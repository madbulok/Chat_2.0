package net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class Server {
    private List<ClientHandler> clients;
    private int PORT = 8189;
    private ServerSocket server = null;
    private Socket socket = null;

    private AuthService authService;

    public Server(){
        clients = new Vector<>();
        try {
            server = new ServerSocket(PORT);
            authService = new SimpleAuthService();
            System.out.println("Server is running...");

            while (true){
                socket = server.accept();
//                socket.setSoTimeout(120000); // 2 min до отключения можно тут установить
                System.out.println("Client is connected!");
                new ClientHandler(this, socket);
            }

        } catch (IOException e){
            e.printStackTrace();
        } finally {
            try {
                if (server != null && !server.isClosed()) {
                    server.close();
                }
                System.out.println("Вы отключены от чата!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void broadcastMessage(ClientHandler clientHandler, String msg){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

        String message = String.format("|%s| %s :  %s", formatter.format(new Date()), clientHandler.getNickname(), msg);
        for (ClientHandler client : clients) {
            client.sendMessage(message + "\n");
        }
    }

    void messageToNickname(ClientHandler sender, String receiver, String msg){
        String message = String.format("[%s] private [%s] : %s",
                sender.getNickname(), receiver,  msg);
        for (ClientHandler client : clients) {
            if (client.getNickname().equals(receiver)){
                client.sendMessage(message + "\n");
            }
        }
        sender.sendMessage(message +"\n");
    }

    public synchronized boolean isLoginAuthenticated(String nick){
        for (ClientHandler o : clients) {
            if (o.getLogin().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
        broadcastClientList();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
        broadcastClientList();
    }


    public void broadcastClientList(){
        StringBuilder sb = new StringBuilder("/clientList ");
        for(ClientHandler c: clients){
            sb.append(c.getNickname()).append(" ");
        }
        String msg = sb.toString();
        for (ClientHandler c: clients){
            c.sendMessage(msg);
        }
    }

    public AuthService getAuthService() {
        return authService;
    }
}
