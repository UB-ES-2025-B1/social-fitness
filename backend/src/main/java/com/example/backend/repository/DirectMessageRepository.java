package com.example.backend.repository;

import com.example.backend.model.DirectMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, String> {

    List<DirectMessage> findBySenderIdAndReceiverIdOrReceiverIdAndSenderIdOrderByTimestampAsc(
            Long senderId, Long receiverId,
            Long receiverId2, Long senderId2
    );

    List<DirectMessage> findTop1BySenderIdOrReceiverIdOrderByTimestampDesc(Long senderId, Long receiverId);
}
