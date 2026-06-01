package com.example.progettoenterprise.data.repositories;

import com.example.progettoenterprise.data.entities.MessaggioChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessaggioChatRepository extends JpaRepository<MessaggioChat, Long> {

    // prende i messaggi di una chat ordinati dal più vecchio al più recente
    List<MessaggioChat> findByChatRoomIdOrderByDataInvioAsc(Long chatRoomId);
}