package server.service;

public class AuthService {
    public boolean login(String username, String password) {
        // Tạm thời cho phép 2 tài khoản này để test Realtime
        if ((username.equals("user1") && password.equals("123")) ||
                (username.equals("user2") && password.equals("123"))) {

            System.out.println(">>> Đăng nhập ảo thành công cho: " + username);
            return true;
        }

        // Khi nào có DB thì dùng đoạn dưới này
        // return database.checkLogin(username, password);
        return false;
    }
    public void register()
    {

    }
    public void logout()
    {

    }
    public void verifySession()
    {

    }
}
