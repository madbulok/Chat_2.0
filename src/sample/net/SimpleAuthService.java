package sample.net;

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
        users.add(new UserData("www", "www", "nick3"));
        users.add(new UserData("sss", "sss", "nick3"));
    }

    @Override
    public String getNickNameByLoginAndPass(String login, String password) {
        for (UserData o : users){
            if (o.login.equals(login) && o.password.equals(password)) {
                return o.login;
            }
        }
        return null;
    }
}
