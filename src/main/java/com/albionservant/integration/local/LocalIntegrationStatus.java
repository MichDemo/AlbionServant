package com.albionservant.integration.local;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class LocalIntegrationStatus {

    public enum State {
        DISABLED,
        STARTING,
        RUNNING,
        FAILED,
        STOPPED
    }

    private final AtomicReference<State> state = new AtomicReference<>(State.STOPPED);
    private volatile String message = "Not started";
    private volatile Instant updatedAt = Instant.now();

    public State getState() {
        return state.get();
    }

    public String getMessage() {
        return message;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void update(State newState, String newMessage) {
        state.set(newState);
        message = newMessage;
        updatedAt = Instant.now();
    }
}
