package com.dhc.inspection_system.dto;

import java.time.LocalDateTime;

public class InspectionLogResponse {

    private LocalDateTime entryDate;
    private String description;
    private String actor;

    public LocalDateTime getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDateTime entryDate) {
        this.entryDate = entryDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }
}
