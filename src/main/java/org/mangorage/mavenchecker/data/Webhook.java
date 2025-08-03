package org.mangorage.mavenchecker.data;

public interface Webhook {
    String url();
    boolean enabled();

    String username();
    String password();
}
