package com.eventmgmt.event_management.service;

import com.eventmgmt.event_management.dto.response.AdminStatsResponse;
import com.eventmgmt.event_management.dto.response.BookingResponse;
import com.eventmgmt.event_management.dto.response.EventResponse;
import com.eventmgmt.event_management.dto.response.UserResponse;
import com.eventmgmt.event_management.entity.Booking;
import com.eventmgmt.event_management.entity.Event;
import com.eventmgmt.event_management.entity.User;
import com.eventmgmt.event_management.enums.BookingStatus;
import com.eventmgmt.event_management.exception.ResourceNotFoundException;
import com.eventmgmt.event_management.repository.BookingRepository;
import com.eventmgmt.event_management.repository.EventRepository;
import com.eventmgmt.event_management.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;

    public AdminService(UserRepository userRepository,
                        EventRepository eventRepository,
                        BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
    }

    public AdminStatsResponse getStats() {
        long totalUsers = userRepository.count();
        long totalEvents = eventRepository.count();
        long totalBookings = bookingRepository.count();
        long confirmedBookings = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
        long cancelledBookings = bookingRepository.countByStatus(BookingStatus.CANCELLED);

        return new AdminStatsResponse(
                totalUsers,
                totalEvents,
                totalBookings,
                confirmedBookings,
                cancelledBookings
        );
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapUserToResponse)
                .collect(Collectors.toList());
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
    }

    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(this::mapEventToResponse)
                .collect(Collectors.toList());
    }

    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        eventRepository.delete(event);
    }

    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(this::mapBookingToResponse)
                .collect(Collectors.toList());
    }

    private UserResponse mapUserToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    private EventResponse mapEventToResponse(Event event) {
        EventResponse response = new EventResponse();
        response.setId(event.getId());
        response.setTitle(event.getTitle());
        response.setDescription(event.getDescription());
        response.setEventDate(event.getEventDate());
        response.setCapacity(event.getCapacity());
        response.setRegisteredCount(event.getRegisteredCount());
        response.setCategory(event.getCategory());
        response.setCreatedAt(event.getCreatedAt());
        response.setLocationType(event.getLocationType());
        response.setLocationDetails(event.getLocationDetails());
        if (event.getOrganizer() != null) {
            response.setOrganizerName(event.getOrganizer().getName());
            response.setOrganizerEmail(event.getOrganizer().getEmail());
        }
        return response;
    }

    private BookingResponse mapBookingToResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setStatus(booking.getStatus());
        response.setBookedAt(booking.getBookedAt());
        if (booking.getEvent() != null) {
            response.setEventId(booking.getEvent().getId());
            response.setEventTitle(booking.getEvent().getTitle());
            response.setEventDate(booking.getEvent().getEventDate());
        }
        if (booking.getUser() != null) {
            response.setUserName(booking.getUser().getName());
            response.setUserEmail(booking.getUser().getEmail());
        }
        return response;
    }
}
