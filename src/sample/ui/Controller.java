package sample.ui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public TextArea textArea;
    @FXML
    public TextField textField;

    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8189;

    @FXML
    public HBox authPanel;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public HBox messagePanel;

    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream dataOutputStream;

    private boolean autenticated;
    private String nickname;
    private final String TITLE = "GeekChat";

    public void setAutenticated(boolean autenticated) {
        this.autenticated = autenticated;
        authPanel.setVisible(!autenticated);
        authPanel.setManaged(!autenticated);

        messagePanel.setVisible(autenticated);
        messagePanel.setManaged(autenticated);

        if (!autenticated) {
            nickname = "";
        }
        setTitle(nickname);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setAutenticated(false);
    }

    private void connection(){
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            inputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            new Thread(()->{
                try {
                    while (true){
                        String str = inputStream.readUTF();
                        if (str.startsWith("/authok")){
                            nickname = str.split("\\s", 2)[1];
                            setAutenticated(true);
                            break;
                        }
                        textArea.appendText(str + "\n");
                    }

                    while (true) {
                        String str = inputStream.readUTF();

                        if (str.equals("/end")){
                            System.out.println("Client disconnect!");
                            break;
                        }

                        System.out.println("Client: " + str);
                        textArea.appendText(str);
                    }

                } catch (EOFException e) {
                    System.out.println();
                }
                catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    textArea.clear();
                    System.out.println("Client disconnect!");
                    setAutenticated(false);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (ConnectException e) {
            System.out.println("Connection is lose!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg() {
        try {
            dataOutputStream.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth() {
        if (socket == null || socket.isClosed()) {
            connection();
        }
        try {
            dataOutputStream.writeUTF(String.format("/auth %s %s",
                    loginField.getText().trim().toLowerCase(),
                    passwordField.getText().trim().toLowerCase()));
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTitle(String nickname){
        Platform.runLater(()-> ((Stage)messagePanel.getScene().getWindow()).setTitle(TITLE+" "+nickname));
    }
}
