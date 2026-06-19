package com.example.progettoenterprise.data.repositories;

import com.example.progettoenterprise.data.entities.MessaggioChat;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessaggioChatRepository extends JpaRepository<MessaggioChat, Long> {

    // prende i messaggi di una chat ordinati dal più vecchio al più recente
    List<MessaggioChat> findByChatRoomIdOrderByDataInvioAsc(Long chatRoomId);
    @Query("""
        SELECT COUNT(m) FROM MessaggioChat m 
        WHERE (m.chatRoom.organizzatore.username = :username OR m.chatRoom.viaggiatore.username = :username) 
        AND m.letto = false 
        AND m.mittenteUsername != :username
    """)
    int countNotificheTotaliUtente(@Param("username") String username);
    @Modifying
    @Transactional
    @Query("UPDATE MessaggioChat m SET m.letto = true WHERE m.chatRoom.id = :roomId AND m.mittenteUsername != :username AND m.letto = false")
    void segnaComeLetti(@Param("roomId") Long roomId, @Param("username") String username);

}