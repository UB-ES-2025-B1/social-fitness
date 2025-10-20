package com.example.backend.service;

import com.example.backend.model.Event;
import com.example.backend.repository.EventRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;


import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventService {
    private static final java.time.format.DateTimeFormatter TIME_FMT =
    java.time.format.DateTimeFormatter.ofPattern("HH:mm");


    private final EventRepository repo;

    public EventService(EventRepository repo) {
        this.repo = repo;
    }

    public List<Map<String, Object>> search(
            String q,
            String sportsCsv,
            String location,
            String daysCsv,
            String timeFrom,
            String timeTo
    ) {
        Specification<Event> spec = Specification.where(null);

        if (q != null && !q.isBlank()) {
            String like = "%" + q.toLowerCase() + "%";
            spec = spec.and((root, cq, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("sport")), like),
                    cb.like(cb.lower(root.get("location")), like)
            ));
        }

        if (sportsCsv != null && !sportsCsv.isBlank()) {
            Set<String> sports = Arrays.stream(sportsCsv.split(","))
                    .map(String::trim).map(String::toLowerCase).collect(Collectors.toSet());
            spec = spec.and((root, cq, cb) -> cb.lower(root.get("sport")).in(sports));
        }

        if (location != null && !location.isBlank()) {
            String like = "%" + location.toLowerCase() + "%";
            spec = spec.and((root, cq, cb) -> cb.like(cb.lower(root.get("location")), like));
        }

        if (daysCsv != null && !daysCsv.isBlank()) {
            Set<DayOfWeek> days = Arrays.stream(daysCsv.split(","))
                    .map(String::trim).map(String::toLowerCase)
                    .map(this::dayOfWeekFromId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            spec = spec.and((root, cq, cb) -> root.get("date").in(
                    repo.findAll().stream()
                            .filter(e -> days.contains(e.getDate().getDayOfWeek()))
                            .map(Event::getDate).collect(Collectors.toSet())
            ));
        }

        LocalTime from = parseTimeOrNull(timeFrom);
        LocalTime to = parseTimeOrNull(timeTo);
        if (from != null) {
            spec = spec.and((root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("time"), from));
        }
        if (to != null) {
            spec = spec.and((root, cq, cb) -> cb.lessThanOrEqualTo(root.get("time"), to));
        }

        return repo.findAll(spec).stream().map(this::toListItem).toList();
    }

    public Map<String, Object> detail(Long id) {
        Event e = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Event not found"));

        Map<String, Object> organizer = Map.of(
                "id", "org-" + e.getId(),
                "name", e.getOrganizer()
        );

        List<Map<String, Object>> participants = List.of(); // vacío por ahora

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", e.getId().toString());
        result.put("title", e.getTitle());
        result.put("sport", e.getSport());
        result.put("date", e.getDate().toString());
        result.put("time", e.getTime().format(TIME_FMT));
        result.put("location", e.getLocation());
        result.put("description", Optional.ofNullable(e.getDescription()).orElse(""));
        result.put("organizer", organizer);
        result.put("participants", participants);
        result.put("capacity", e.getCapacity());
        result.put("price", e.getPrice());
        result.put("image", e.getImage());

        return result;
    }

    public void join(Long id) {
        Event e = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Event not found"));
        if (e.getParticipants() >= e.getCapacity()) {
            throw new EventFullException();
        }
        e.setParticipants(e.getParticipants() + 1); // simplificado
        repo.save(e);
    }

    public static class EventFullException extends RuntimeException {
        public EventFullException() {
            super("Event full");
        }
    }

    private Map<String, Object> toListItem(Event e) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", e.getId().toString());
        item.put("title", e.getTitle());
        item.put("sport", e.getSport());
        item.put("date", e.getDate().toString());
        item.put("time", e.getTime().format(TIME_FMT));
        item.put("location", e.getLocation());
        item.put("organizer", e.getOrganizer());
        item.put("participants", e.getParticipants());
        item.put("capacity", e.getCapacity());
        item.put("price", e.getPrice());
        item.put("image", e.getImage());
        return item;
    }

    private LocalTime parseTimeOrNull(String hhmm) {
        try {
            return (hhmm == null || hhmm.isBlank()) ? null : LocalTime.parse(hhmm);
        } catch (Exception e) {
            return null;
        }
    }

    private DayOfWeek dayOfWeekFromId(String id) {
        return switch (id) {
            case "mon" -> DayOfWeek.MONDAY;
            case "tue" -> DayOfWeek.TUESDAY;
            case "wed" -> DayOfWeek.WEDNESDAY;
            case "thu" -> DayOfWeek.THURSDAY;
            case "fri" -> DayOfWeek.FRIDAY;
            case "sat" -> DayOfWeek.SATURDAY;
            case "sun" -> DayOfWeek.SUNDAY;
            default -> null;
        };
    }
}
