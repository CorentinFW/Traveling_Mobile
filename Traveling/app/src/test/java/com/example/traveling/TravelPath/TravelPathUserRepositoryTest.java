package com.example.traveling.TravelPath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Map;

public class TravelPathUserRepositoryTest {

    private final TravelPathUserRepository repository =
            new TravelPathUserRepository(true);

    @Test
    public void buildUserDocumentPath_usesUsersCollection() {
        String path = repository.buildUserDocumentPath("uid_123");
        assertEquals("users/uid_123", path);
    }

    @Test
    public void buildUserSubCollectionPath_appendsSubCollection() {
        String path = repository.buildUserSubCollectionPath("uid_123", "tasks");
        assertEquals("users/uid_123/tasks", path);
    }

    @Test
    public void createProfilePayload_containsExpectedFields() {
        Map<String, Object> payload = repository.createProfilePayload("uid_123", null, "email_password");

        assertEquals("uid_123", payload.get("uid"));
        assertEquals("", payload.get("email"));
        assertEquals("email_password", payload.get("provider"));
        assertTrue(payload.containsKey("createdAt"));
        assertTrue(payload.containsKey("updatedAt"));

        Number createdAtValue = (Number) payload.get("createdAt");
        Number updatedAtValue = (Number) payload.get("updatedAt");
        assertNotNull(createdAtValue);
        assertNotNull(updatedAtValue);

        long createdAt = createdAtValue.longValue();
        long updatedAt = updatedAtValue.longValue();
        assertTrue(createdAt > 0L);
        assertTrue(updatedAt > 0L);
        assertFalse(updatedAt < createdAt - 10_000L);
    }
}


