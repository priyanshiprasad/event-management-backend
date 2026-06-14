package com.eventmgmt.event_management.dto.response;

import com.eventmgmt.event_management.enums.BookingStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookingResponse {
    private Long id;
    private Long eventId;
    private String eventTitle;
    private String eventVenue;
    private LocalDateTime eventDate;
    private String userName;
    private String userEmail;
    private BookingStatus status;
    private LocalDateTime bookedAt;
}
