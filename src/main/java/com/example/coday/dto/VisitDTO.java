package com.example.coday.dto;

import com.example.coday.model.Visit;

import java.time.Duration;

public class VisitDTO {

    private final Visit visit;

    public VisitDTO(Visit visit) {
        this.visit = visit;
    }

    public Visit getVisit() {
        return visit;
    }

    public long getDurationMinutes() {
        if (visit.getCheckInTime() != null && visit.getCheckOutTime() != null) {
            return Duration.between(visit.getCheckInTime(), visit.getCheckOutTime()).toMinutes();
        }
        return 0;
    }

    public int getPoints() {
        return (int) getDurationMinutes();
    }
}
