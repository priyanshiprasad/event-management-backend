package com.eventmgmt.event_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminStatsResponse {
    private long totalUsers;
    private long totalEvents;
    private long totalBookings;
    private long confirmedBookings;
    private long cancelledBookings;
}