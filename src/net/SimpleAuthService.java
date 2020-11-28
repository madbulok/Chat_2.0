package net;

import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService {

    private class UserData {
        String login;
        String password;
        String nickname;

        UserData(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }

    private List<UserData> users;

    public SimpleAuthService() {
        users = new ArrayList<>();
        users.add(new UserData("qqq", "qqq", "nick1"));
        users.add(new UserData("aaa", "aaa", "nick2"));
        users.add(new UserData("zzz", "zzz", "nick3"));
        users.add(new UserData("www", "www", "nick4"));
        users.add(new UserData("sss", "sss", "nick5"));
    }

    @Override
    public String getNickNameByLoginAndPass(String login, String password) {
        for (UserData o : users){
            if (o.login.equals(login) && o.password.equals(password)) {
                return o.nickname;
            }
        }
        return null;
    }

    @Override
    public boolean doRegistration(String login, String pass, String nick) {
        for (UserData user : users) {
            if (user.login.equals(login) || user.nickname.equals(nick)){
                return false;
            }
        }
        users.add(new UserData(login, pass, nick));
        return true;
    }
}
