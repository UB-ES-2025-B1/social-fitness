package com.example.backend.service;

import com.example.backend.model.Event;
import com.example.backend.model.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

  @Mock
  EventRepository repo;

  @Mock
  UserRepository userRepo;

  @Mock
  NotificationService notificationService;

  @InjectMocks
  EventService service;

  @Test
  void create_throwsValidationException_withAllDetectedErrors() {
    Event e = new Event();
    e.setTitle(" ");
    e.setSport(null);
    e.setDate(LocalDate.now().minusDays(1));
    e.setTime(null);
    e.setLocation("");
    e.setOrganizer("");
    e.setCapacity(1);
    e.setPrice(new BigDecimal("-1.00"));

    EventService.ValidationException ex = assertThrows(EventService.ValidationException.class, () -> service.create(e, null));
    Map<String, String> errors = ex.getErrors();
    assertTrue(errors.containsKey("title"));
    assertTrue(errors.containsKey("sport"));
    assertTrue(errors.containsKey("date"));
    assertTrue(errors.containsKey("time"));
    assertTrue(errors.containsKey("location"));
    assertTrue(errors.containsKey("organizer"));
    assertTrue(errors.containsKey("capacity"));
    assertTrue(errors.containsKey("price"));
    verify(repo, never()).save(any());
  }

  @Test
  void create_savesEventOnce_andResetsParticipants_whenNoUser() throws Exception {
    Event e = validEvent();
    e.setParticipants(10);
    e.setParticipantUsers(new HashSet<>(Set.of(new User("someone", "s@b.com", "p"))));

    when(repo.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0, Event.class));

    Event saved = service.create(e, null);

    assertEquals(0, saved.getParticipants());
    assertNotNull(saved.getParticipantUsers());
    assertTrue(saved.getParticipantUsers().isEmpty());
    verify(repo, times(1)).save(any(Event.class));
  }

  @Test
  void create_overridesOrganizer_andAutoAddsCreator_whenUserProvided() throws Exception {
    User user = new User("alice", "alice@b.com", "p");
    user.setId(1L);
    when(userRepo.findById(1L)).thenReturn(Optional.of(user));

    Event e = validEvent();
    e.setOrganizer("will-be-overridden");
    e.setParticipantUsers(null);

    when(repo.save(any(Event.class))).thenAnswer(inv -> {
      Event arg = inv.getArgument(0, Event.class);
      if (arg.getId() == null) arg.setId(123L);
      return arg;
    });

    Event saved = service.create(e, 1L);

    assertEquals("alice", saved.getOrganizer());
    assertEquals(1, saved.getParticipants());
    assertTrue(saved.getParticipantUsers().contains(user));
    verify(repo, times(2)).save(any(Event.class));
  }

  @Test
  void join_throwsEventFullException_whenAtCapacity() {
    User user = new User("u", "u@b.com", "p");
    user.setId(1L);
    when(userRepo.findById(1L)).thenReturn(Optional.of(user));

    Event e = validEvent();
    e.setId(10L);
    e.setCapacity(2);
    e.setParticipants(2);
    when(repo.findById(10L)).thenReturn(Optional.of(e));

    assertThrows(EventService.EventFullException.class, () -> service.join(10L, 1L));
    verify(repo, never()).save(any());
    verifyNoInteractions(notificationService);
  }

  @Test
  void join_returnsEarly_whenAlreadyParticipant() {
    User user = new User("u", "u@b.com", "p");
    user.setId(1L);
    when(userRepo.findById(1L)).thenReturn(Optional.of(user));

    Event e = validEvent();
    e.setId(10L);
    e.setCapacity(5);
    e.addParticipant(user);
    when(repo.findById(10L)).thenReturn(Optional.of(e));

    service.join(10L, 1L);

    verify(repo, never()).save(any());
    verifyNoInteractions(notificationService);
  }

  @Test
  void join_savesEvent_andNotifiesUser_andOrganizer() {
    User user = new User("participant", "p@b.com", "p");
    user.setId(1L);
    when(userRepo.findById(1L)).thenReturn(Optional.of(user));

    User organizer = new User("organizer", "o@b.com", "p");
    organizer.setId(2L);
    when(userRepo.findByUsername("organizer")).thenReturn(Optional.of(organizer));

    Event e = validEvent();
    e.setId(10L);
    e.setOrganizer("organizer");
    e.setCapacity(10);
    when(repo.findById(10L)).thenReturn(Optional.of(e));
    when(repo.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0, Event.class));

    service.join(10L, 1L);

    verify(repo).save(any(Event.class));
    verify(notificationService).notifyJoinedEvent(1L, 10L, e.getTitle());
    verify(notificationService).notifyNewParticipant(2L, 10L, e.getTitle(), 1L);
  }

  @Test
  void join_ignoresOrganizerLookupErrors() {
    User user = new User("participant", "p@b.com", "p");
    user.setId(1L);
    when(userRepo.findById(1L)).thenReturn(Optional.of(user));
    when(userRepo.findByUsername(anyString())).thenThrow(new RuntimeException("boom"));

    Event e = validEvent();
    e.setId(10L);
    e.setOrganizer("organizer");
    when(repo.findById(10L)).thenReturn(Optional.of(e));
    when(repo.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0, Event.class));

    assertDoesNotThrow(() -> service.join(10L, 1L));
    verify(notificationService).notifyJoinedEvent(1L, 10L, e.getTitle());
  }

  @Test
  void leave_throwsIllegalState_whenNotParticipant() {
    User user = new User("u", "u@b.com", "p");
    user.setId(1L);
    when(userRepo.findById(1L)).thenReturn(Optional.of(user));

    Event e = validEvent();
    e.setId(10L);
    when(repo.findById(10L)).thenReturn(Optional.of(e));

    assertThrows(IllegalStateException.class, () -> service.leave(10L, 1L));
    verify(repo, never()).save(any());
  }

  @Test
  void leave_removesParticipant_andSaves() {
    User user = new User("u", "u@b.com", "p");
    user.setId(1L);
    when(userRepo.findById(1L)).thenReturn(Optional.of(user));

    Event e = validEvent();
    e.setId(10L);
    e.addParticipant(user);
    when(repo.findById(10L)).thenReturn(Optional.of(e));

    service.leave(10L, 1L);

    assertFalse(e.getParticipantUsers().contains(user));
    assertEquals(0, e.getParticipants());
    verify(repo).save(any(Event.class));
  }

  @Test
  void detail_throwsNoSuchElement_whenEventMissing() {
    when(repo.findById(999L)).thenReturn(Optional.empty());
    assertThrows(NoSuchElementException.class, () -> service.detail(999L));
  }

  private static Event validEvent() {
    Event e = new Event();
    e.setTitle("Morning Run");
    e.setSport("running");
    e.setDate(LocalDate.now().plusDays(5));
    e.setTime(LocalTime.of(8, 0));
    e.setLocation("Park");
    e.setOrganizer("organizer");
    e.setCapacity(10);
    e.setPrice(BigDecimal.ZERO);
    return e;
  }
}

