package com.albionservant.integration.aodp;

import com.albionservant.market.sqlite.SqliteMarketStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class RawEventService {

    public enum Decision { PROCESS, ALREADY_FINISHED }

    private final SqliteMarketStore store;

    public RawEventService(SqliteMarketStore store) {
        this.store = store;
    }

    @Transactional
    public RawEventContext begin(String subject, byte[] data) {
        String id = hash(subject, data);
        store.insertRawEventIfAbsent(id, subject, new String(data, StandardCharsets.UTF_8), Instant.now());

        String status = store.findRawEventStatus(id);
        if ("PROCESSED".equals(status) || "REJECTED".equals(status)) {
            return new RawEventContext(id, Decision.ALREADY_FINISHED);
        }

        store.markRawProcessing(id);
        return new RawEventContext(id, Decision.PROCESS);
    }

    public void markProcessed(String id) {
        store.markRawProcessed(id, Instant.now());
    }

    public void markRetryableFailure(String id, Throwable error) {
        store.markRawRetryableFailure(id, conciseError(error));
    }

    public void markRejected(String id, Throwable error) {
        store.markRawRejected(id, Instant.now(), conciseError(error));
    }

    private String conciseError(Throwable error) {
        String message = error.getMessage();
        String text = error.getClass().getSimpleName() + (message == null ? "" : ": " + message);
        return text.length() > 2000 ? text.substring(0, 2000) : text;
    }

    private String hash(String subject, byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(subject.getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            digest.update(data);
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    public record RawEventContext(String id, Decision decision) {}
}
