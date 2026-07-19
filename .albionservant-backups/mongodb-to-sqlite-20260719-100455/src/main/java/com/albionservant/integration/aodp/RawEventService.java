package com.albionservant.integration.aodp;

import com.albionservant.market.model.RawMarketEventDocument;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class RawEventService {

    public enum Decision {
        PROCESS,
        ALREADY_FINISHED
    }

    private final MongoTemplate mongoTemplate;

    public RawEventService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public RawEventContext begin(String subject, byte[] data) {
        String id = hash(subject, data);
        RawMarketEventDocument existing = mongoTemplate.findById(id, RawMarketEventDocument.class);

        if (existing == null) {
            RawMarketEventDocument created = new RawMarketEventDocument();
            created.setId(id);
            created.setSubject(subject);
            created.setPayloadJson(new String(data, StandardCharsets.UTF_8));
            created.setReceivedAt(Instant.now());
            created.setStatus("PENDING");
            created.setAttempts(0);

            try {
                mongoTemplate.insert(created);
                existing = created;
            } catch (DuplicateKeyException ignored) {
                existing = mongoTemplate.findById(id, RawMarketEventDocument.class);
            }
        }

        if (existing != null && ("PROCESSED".equals(existing.getStatus()) || "REJECTED".equals(existing.getStatus()))) {
            return new RawEventContext(id, Decision.ALREADY_FINISHED);
        }

        mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(id)),
                new Update()
                        .set("status", "PROCESSING")
                        .inc("attempts", 1)
                        .unset("lastError"),
                RawMarketEventDocument.class
        );

        return new RawEventContext(id, Decision.PROCESS);
    }

    public void markProcessed(String id) {
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(id)),
                new Update()
                        .set("status", "PROCESSED")
                        .set("processedAt", Instant.now())
                        .unset("lastError"),
                RawMarketEventDocument.class
        );
    }

    public void markRetryableFailure(String id, Throwable error) {
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(id)),
                new Update()
                        .set("status", "FAILED_RETRY")
                        .set("lastError", conciseError(error)),
                RawMarketEventDocument.class
        );
    }

    public void markRejected(String id, Throwable error) {
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(id)),
                new Update()
                        .set("status", "REJECTED")
                        .set("processedAt", Instant.now())
                        .set("lastError", conciseError(error)),
                RawMarketEventDocument.class
        );
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

    public record RawEventContext(String id, Decision decision) {
    }
}
