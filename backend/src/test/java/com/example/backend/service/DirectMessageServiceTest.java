package com.example.backend.service;

import com.example.backend.model.DirectMessage;
import com.example.backend.model.User;
import com.example.backend.repository.DirectMessageRepository;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DirectMessageServiceTest {

  @Mock
  DirectMessageRepository repo;

  @Mock
  UserRepository userRepo;

  @InjectMocks
  DirectMessageService service;

  @Test
  void getAllMessagesOfUser_delegatesToRepository() {
    when(repo.findBySenderIdOrReceiverIdOrderByTimestampDesc(1L, 1L)).thenReturn(List.of());

    service.getAllMessagesOfUser(1L);

    verify(repo).findBySenderIdOrReceiverIdOrderByTimestampDesc(1L, 1L);
  }

  @Test
  void sendMessage_throws_whenSenderNotFound() {
    when(userRepo.findById(1L)).thenReturn(Optional.empty());

    NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> service.sendMessage(1L, 2L, "hi"));
    assertEquals("User not found", ex.getMessage());
    verify(repo, never()).save(any());
  }

  @Test
  void sendMessage_savesMessageWithExpectedFields() {
    User sender = new User("sender", "s@b.com", "p");
    sender.setId(1L);
    User receiver = new User("receiver", "r@b.com", "p");
    receiver.setId(2L);

    when(userRepo.findById(1L)).thenReturn(Optional.of(sender));
    when(userRepo.findById(2L)).thenReturn(Optional.of(receiver));
    when(repo.save(any(DirectMessage.class))).thenAnswer(inv -> inv.getArgument(0, DirectMessage.class));

    DirectMessage saved = service.sendMessage(1L, 2L, "Hello!");

    assertEquals("Hello!", saved.getText());
    assertSame(sender, saved.getSender());
    assertSame(receiver, saved.getReceiver());
    assertFalse(saved.isRead());
    assertNotNull(saved.getTimestamp());

    verify(repo).save(any(DirectMessage.class));
  }

  @Test
  void markConversationRead_marksOnlyMessagesReceivedByViewer() {
    User viewer = new User("viewer", "v@b.com", "p");
    viewer.setId(1L);
    User other = new User("other", "o@b.com", "p");
    other.setId(2L);

    DirectMessage receivedByViewer = new DirectMessage();
    receivedByViewer.setSender(other);
    receivedByViewer.setReceiver(viewer);
    receivedByViewer.setRead(false);

    DirectMessage sentByViewer = new DirectMessage();
    sentByViewer.setSender(viewer);
    sentByViewer.setReceiver(other);
    sentByViewer.setRead(false);

    when(repo.findBySenderIdAndReceiverIdOrReceiverIdAndSenderIdOrderByTimestampAsc(1L, 2L, 1L, 2L))
        .thenReturn(List.of(receivedByViewer, sentByViewer));

    service.markConversationRead(1L, 2L);

    assertTrue(receivedByViewer.isRead());
    assertFalse(sentByViewer.isRead());

    ArgumentCaptor<List<DirectMessage>> captor = ArgumentCaptor.forClass(List.class);
    verify(repo).saveAll(captor.capture());
    assertEquals(2, captor.getValue().size());
  }
}
