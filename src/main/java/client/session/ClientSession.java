package client.session;

import server.model.user.User;

public final class ClientSession {
    private static User currentUser;

    private ClientSession() {
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static String getCurrentUserFullName() {
        return currentUser != null ? currentUser.getFullName() : "";
    }

    public static void clear() {
        currentUser = null;
    }
}