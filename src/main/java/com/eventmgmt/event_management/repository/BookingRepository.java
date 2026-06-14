package com.eventmgmt.event_management.repository;

import com.eventmgmt.event_management.entity.Booking;
import com.eventmgmt.event_management.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findByEventId(Long eventId);
    Optional<Booking> findByUserIdAndEventId(Long userId, Long eventId);
    boolean existsByUserIdAndEventIdAndStatus(Long userId, Long eventId, BookingStatus status);
    long countByStatus(BookingStatus status);
}
