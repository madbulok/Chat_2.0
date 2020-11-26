package sample.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
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
                System.out.println("Client is connected!");
                new ClientHandler(this, socket);
            }

        } catch (IOException e){
            e.printStackTrace();
        } finally {
            try {
                if (server != null) {
                    server.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void broadcastMessage(ClientHandler clientHandler, String msg){
        String message = String.format("%s :  %s", clientHandler.getNickname(), msg);
        for (ClientHandler client : clients) {
            client.message(message + "\n");
        }
    }

    void messageToNickname(ClientHandler sender, String receiver, String msg){
        String message = String.format("%s :  %s", sender.getNickname(), msg);
        for (ClientHandler client : clients) {
            if (client.getNickname().equals(receiver)){
                client.message(message + "\n");
            }
        }
        sender.message(message +"\n");
    }

    public synchronized boolean isNickBusy(String nick){
        for (ClientHandler o : clients) {
            if (o.getNickname().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
    }

    public synchronized void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
    }


    public AuthService getAuthService() {
        return authService;
    }
}
