package client.controller;

public interface AdminPageLifecycle {
    void onPageShown();

    default void onPageHidden() {
    }

    default void dispose() {
        onPageHidden();
    }
}
