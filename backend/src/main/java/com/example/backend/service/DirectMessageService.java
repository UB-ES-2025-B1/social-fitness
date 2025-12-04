package com.example.backend.service;

import com.example.backend.model.DirectMessage;
import com.example.backend.model.User;
import com.example.backend.repository.DirectMessageRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DirectMessageService {

    private final DirectMessageRepository repo;
    private final UserRepository userRepo;

    public DirectMessageService(DirectMessageRepository repo, UserRepository userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    private User getUser(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    public List<DirectMessage> getConversation(Long user1, Long user2) {
        return repo.findBySenderIdAndReceiverIdOrReceiverIdAndSenderIdOrderByTimestampAsc(
                user1, user2,
                user1, user2
        );
    }

    @Transactional
    public DirectMessage sendMessage(Long senderId, Long receiverId, String text) {
        User sender = getUser(senderId);
        User receiver = getUser(receiverId);

        DirectMessage msg = new DirectMessage();
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setText(text);
        msg.setTimestamp(Instant.now());
        msg.setRead(false);

        return repo.save(msg);
    }

    @Transactional
    public void markConversationRead(Long viewerId, Long otherId) {
        List<DirectMessage> list = repo.findBySenderIdAndReceiverIdOrReceiverIdAndSenderIdOrderByTimestampAsc(
                viewerId, otherId, viewerId, otherId
        );
        list.stream().filter(m -> m.getReceiver().getId().equals(viewerId))
                .forEach(m -> m.setRead(true));
        repo.saveAll(list);
    }
}
