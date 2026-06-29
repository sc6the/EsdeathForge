package net.labymod.api.events;

public interface MessageSendEvent {
    /** Return true to cancel the chat send. */
    boolean onSend(String message);
}
