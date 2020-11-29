package ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
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

    @FXML
    public ListView<String> clientList;

    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream dataOutputStream;

    private boolean autenticated;
    private String nickname;
    private final String TITLE = "GeekChat";

    private Stage stage;
    private Stage registrationStage;
    private RegController regController;

    public void setAuthenticated(boolean authenticated) {
        this.autenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);

        messagePanel.setVisible(authenticated);
        messagePanel.setManaged(authenticated);

        clientList.setVisible(authenticated);
        clientList.setManaged(authenticated);

        if (!authenticated) {
            nickname = "";
        }
        textArea.clear();
        setTitle(nickname);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setAuthenticated(false);
        createRegWindow();
        Platform.runLater(()->{
            stage = (Stage)textField.getScene().getWindow();
            stage.setOnCloseRequest(windowEvent -> {
                try {
                    disconnect();
                } catch (UserNotLoginedException e) {
                    stage.close();
                }
                stage.close();
            });
        });

        // таймер на закрытие если в течении 2 минут не авторизовались
        closeProgramTimeOut(120000);
    }

    private void closeProgramTimeOut(long timeout) {
        new Thread(() -> {
            try {
                Thread.sleep(timeout);
                if (socket == null || socket.isClosed()) {
                    Platform.runLater(() -> stage.close());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Platform.runLater(() -> stage.close());
            }
        }).start();
    }

    private void disconnect() throws UserNotLoginedException {
        try {
            if (dataOutputStream == null) throw new UserNotLoginedException("Ползователь ещё не открыл соединение!");
            if (!socket.isClosed()) dataOutputStream.writeUTF("/end");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createRegWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("reg_form.fxml"));
            Parent root = fxmlLoader.load();
            registrationStage = new Stage();
            registrationStage.setTitle("Registration");
            registrationStage.setScene(new Scene(root, 400, 250));

            regController = fxmlLoader.getController();
            regController.setController(this);

            registrationStage.initModality(Modality.APPLICATION_MODAL);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                            setAuthenticated(true);
                            break;
                        }
                        if (str.startsWith("/regok")){
                            regController.addMsgToTextArea("Регистрация прошла успешно!");
                        }
                        if (str.startsWith("/regno")){
                            regController.addMsgToTextArea("В регистрации отказано!");
                        }

                        textArea.appendText(str + "\n");
                    }

                    while (true) {
                        String str = inputStream.readUTF();

                        // служебные команды
                        if (str.startsWith("/")){
                            if (str.equals("/end")){
                                System.out.println("Client disconnect!");
                                // таймер на закрытие если в течении 2 минут не авторизовались
                                closeProgramTimeOut(120000);
                                break;
                            }
                            if (str.startsWith("/clientList")){
                                String[] token = str.split("\\s+");
                                Platform.runLater(() -> {
                                    clientList.getItems().clear();
                                    for (int i = 1; i < token.length; i++) {
                                        clientList.getItems().add(token[i]);
                                        System.out.println(token[i]);
                                    }
                                });
                            }
                        } else {
                            textArea.appendText(str + "\n");
                        }
                    }
                } catch (EOFException e) {
                    System.out.println();
                }
                catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    textArea.clear();
                    System.out.println("Client disconnect!");
                    setAuthenticated(false);
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

    public void tryToReg(String login, String password, String nickname){
        String msg = String.format("/reg %s %s %s", login, password, nickname);

        if (socket == null || socket.isClosed()) {
            connection();
        }

        try {
            dataOutputStream.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registration() {
        registrationStage.show();
    }

    public void clickClientList() {
        String receiver = clientList.getSelectionModel().getSelectedItem();
        textField.setText("/w " + receiver + " ");
    }
}
