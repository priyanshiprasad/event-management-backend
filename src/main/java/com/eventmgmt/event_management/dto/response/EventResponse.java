package com.eventmgmt.event_management.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime eventDate;
    private Integer capacity;
    private Integer registeredCount;
    private String category;
    private String organizerName;
    private String organizerEmail;
    private LocalDateTime createdAt;
    private String locationType;
    private String locationDetails;
}
