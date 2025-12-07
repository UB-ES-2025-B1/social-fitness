package com.example.backend.service;

import com.example.backend.model.Event;
import com.example.backend.repository.EventRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
import org.springframework.transaction.annotation.Transactional; 
import com.example.backend.repository.UserRepository;
import com.example.backend.model.User;
import com.example.backend.service.AuthService;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventService {
    private static final java.time.format.DateTimeFormatter TIME_FMT =
    java.time.format.DateTimeFormatter.ofPattern("HH:mm");


    private final EventRepository repo;
    private final UserRepository userRepo;
 //   private final AuthService authService;
    private final NotificationService notificationService;
    
    public EventService(EventRepository repo, UserRepository userRepo, NotificationService notificationService) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.notificationService = notificationService;
    }

    private User getUserById(Long userId) {
        return userRepo.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + userId));
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

        List<Map<String, Object>> participants = e.getParticipantUsers().stream()
            .map(user -> {
                Map<String, Object> userMap = new LinkedHashMap<>();
                userMap.put("id", user.getId().toString());
                userMap.put("name", user.getUsername());
                return userMap;
            }).collect(Collectors.toList());

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

    @Transactional
    public void join(Long id, Long userId) {
        User currentUser = getUserById(userId);
        Event e = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Event not found"));
        
        if (e.getParticipants() >= e.getCapacity()) {
            throw new EventFullException();
        }
        if (e.getParticipantUsers().contains(currentUser)) {
            // Ya está unido, no hacemos nada
            return; 
        }
        e.addParticipant(currentUser);
        repo.save(e);

        // lanzar notificaciones
        notificationService.notifyJoinedEvent(userId, id, e.getTitle());

        // Notificar al organizador de nuevo participante
        try {
            User organizer = userRepo.findByUsername(e.getOrganizer()).orElse(null);
            if (organizer != null && !organizer.getId().equals(userId)) {
                notificationService.notifyNewParticipant(organizer.getId(), id, e.getTitle(), userId);
            }
        } catch (Exception ex) {
            // ignore si no encuentra organizador
        }
    }

    @Transactional
    public void leave(Long id, Long userId) {
        User currentUser = getUserById(userId);
        Event e = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Event not found"));

        if (!e.getParticipantUsers().contains(currentUser)) {
            throw new IllegalStateException("Not a participant");
        }
        e.removeParticipant(currentUser);
        repo.save(e);
    }

    @Transactional
    public Event create(Event e, Long userId) throws ValidationException {
        Map<String, String> errors = new LinkedHashMap<>();

        User currentUser = null;
        if (userId != null) {
            currentUser = getUserById(userId);
            // override organizer with authenticated username
            e.setOrganizer(currentUser.getUsername());
        }

        if (e.getTitle() == null || e.getTitle().isBlank()) {
            errors.put("title", "Title is required");
        }
        if (e.getSport() == null || e.getSport().isBlank()) {
            errors.put("sport", "Sport is required");
        }
        if (e.getDate() == null) {
            errors.put("date", "Date is required");
        } else if (e.getDate().isBefore(LocalDate.now())) {
            errors.put("date", "Date cannot be in the past");
        }
        if (e.getTime() == null) {
            errors.put("time", "Time is required");
        }
        if (e.getLocation() == null || e.getLocation().isBlank()) {
            errors.put("location", "Location is required");
        }
        if (e.getOrganizer() == null || e.getOrganizer().isBlank()) {
            errors.put("organizer", "Organizer is required");
        }
        if (e.getCapacity() == null || e.getCapacity() < 2) {
            errors.put("capacity", "Capacity must be greater than or equal to 2");
        }
        if (e.getPrice() == null || e.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            errors.put("price", "Price cannot be negative");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        e.setParticipants(0); // asegurar que empieza en 0
        // ensure participantUsers empty
        if (e.getParticipantUsers() != null) {
            e.getParticipantUsers().clear();
        } else {
            e.setParticipantUsers(new HashSet<>());
        }

        Event saved = repo.save(e);

        // Auto-add creator as participant if authenticated
        if (currentUser != null) {
            saved.addParticipant(currentUser);
            saved = repo.save(saved);
        }

        return saved;
    }

    public static class EventFullException extends RuntimeException {
        public EventFullException() {
            super("Event full");
        }
    }
    
    public static class ValidationException extends Exception {
        private final Map<String, String> errors;
        public ValidationException(Map<String, String> errors) {
            super(errors == null || errors.isEmpty() ? "Validation failed" : String.join(", ", errors.values()));
            this.errors = errors == null ? Map.of() : Map.copyOf(errors);
        }
        public Map<String, String> getErrors() {
            return errors;
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
