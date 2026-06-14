package com.eventmgmt.event_management.service;

import com.eventmgmt.event_management.dto.request.EventRequest;
import com.eventmgmt.event_management.dto.response.EventResponse;
import com.eventmgmt.event_management.entity.Event;
import com.eventmgmt.event_management.entity.User;
import com.eventmgmt.event_management.exception.ResourceNotFoundException;
import com.eventmgmt.event_management.repository.EventRepository;
import com.eventmgmt.event_management.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventService(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    public EventResponse createEvent(EventRequest request, String organizerEmail) {
        User organizer = userRepository.findByEmail(organizerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));

        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setEventDate(request.getEventDate());
        event.setCapacity(request.getCapacity());
        event.setCategory(request.getCategory());
        event.setOrganizer(organizer);
        event.setLocationType(request.getLocationType());
        event.setLocationDetails(request.getLocationDetails());

        Event saved = eventRepository.save(event);
        return mapToResponse(saved);
    }

    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        return mapToResponse(event);
    }

    public EventResponse updateEvent(Long id, EventRequest request, String organizerEmail) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        if (!event.getOrganizer().getEmail().equals(organizerEmail)) {
            throw new RuntimeException("You are not authorized to update this event");
        }

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setEventDate(request.getEventDate());
        event.setCapacity(request.getCapacity());
        event.setCategory(request.getCategory());
        event.setLocationType(request.getLocationType());
        event.setLocationDetails(request.getLocationDetails());

        Event updated = eventRepository.save(event);
        return mapToResponse(updated);
    }

    public void deleteEvent(Long id, String organizerEmail) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        if (!event.getOrganizer().getEmail().equals(organizerEmail)) {
            throw new RuntimeException("You are not authorized to delete this event");
        }

        eventRepository.delete(event);
    }

    public List<EventResponse> searchEvents(String keyword) {
        return eventRepository.searchByTitle(keyword)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<EventResponse> getEventsByCategory(String category) {
        return eventRepository.findByCategory(category)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<EventResponse> getMyEvents(String organizerEmail) {
        User organizer = userRepository.findByEmail(organizerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));
        return eventRepository.findByOrganizerId(organizer.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private EventResponse mapToResponse(Event event) {
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
}
