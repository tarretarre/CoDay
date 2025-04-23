package com.example.coday.dto;

import java.time.LocalDateTime;

public class VisitResponse {

    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    public VisitResponse(LocalDateTime checkInTime, LocalDateTime checkOutTime) {
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }
}
