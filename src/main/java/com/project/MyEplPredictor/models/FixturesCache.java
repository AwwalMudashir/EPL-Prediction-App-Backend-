package com.project.MyEplPredictor.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import java.time.LocalDateTime;

@Entity
public class FixturesCache {
    @Id
    private Long id = 1L; // singleton record

    @Lob
    private String jsonPayload;

    private LocalDateTime updatedAt;

    public FixturesCache() {}

    public Long getId() {
        return id;
    }

    public String getJsonPayload() {
        return jsonPayload;
    }

    public void setJsonPayload(String jsonPayload) {
        this.jsonPayload = jsonPayload;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}