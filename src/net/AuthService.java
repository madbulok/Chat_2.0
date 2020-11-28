package net;

public interface AuthService {
    String getNickNameByLoginAndPass(String login, String password);
    boolean doRegistration(String login, String pass, String nick);
}
