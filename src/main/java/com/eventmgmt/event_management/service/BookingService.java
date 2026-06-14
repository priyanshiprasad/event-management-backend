package com.eventmgmt.event_management.service;

import com.eventmgmt.event_management.dto.request.BookingRequest;
import com.eventmgmt.event_management.dto.response.BookingResponse;
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
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public BookingService(BookingRepository bookingRepository,
                          EventRepository eventRepository,
                          UserRepository userRepository,
                          EmailService emailService) {
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public BookingResponse bookEvent(BookingRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        // check if already booked
        boolean alreadyBooked = bookingRepository.existsByUserIdAndEventIdAndStatus(
                user.getId(), event.getId(), BookingStatus.CONFIRMED);
        if (alreadyBooked) {
            throw new RuntimeException("You have already booked this event");
        }

        // check capacity
        if (event.getRegisteredCount() >= event.getCapacity()) {
            throw new RuntimeException("Event is fully booked");
        }

        // create booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setEvent(event);
        booking.setStatus(BookingStatus.CONFIRMED);

        // increment registered count
        event.setRegisteredCount(event.getRegisteredCount() + 1);
        eventRepository.save(event);

        Booking saved = bookingRepository.save(booking);

        // Send confirmation email with PDF ticket asynchronously
        emailService.sendBookingConfirmation(saved);

        return mapToResponse(saved);
    }

    public List<BookingResponse> getMyBookings(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return bookingRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BookingResponse cancelBooking(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("You are not authorized to cancel this booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);

        // decrement registered count
        Event event = booking.getEvent();
        event.setRegisteredCount(event.getRegisteredCount() - 1);
        eventRepository.save(event);

        Booking updated = bookingRepository.save(booking);
        return mapToResponse(updated);
    }

    public List<BookingResponse> getBookingsForEvent(Long eventId, String organizerEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (!event.getOrganizer().getEmail().equals(organizerEmail)) {
            throw new RuntimeException("You are not authorized to view these bookings");
        }

        return bookingRepository.findByEventId(eventId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private BookingResponse mapToResponse(Booking booking) {
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
