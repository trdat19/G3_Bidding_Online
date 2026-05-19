package client.state;

public class ClientSession {
    private static String fullName;

    public static String getFullName() {
        return fullName;
    }

    public static void setFullName(String fullName) {
        ClientSession.fullName = fullName;
    }

    public static void clear() {
        fullName = null;
    }
}
