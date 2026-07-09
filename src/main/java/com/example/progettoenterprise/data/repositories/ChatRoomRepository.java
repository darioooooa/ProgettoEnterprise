package com.example.progettoenterprise.data.repositories;

import com.example.progettoenterprise.data.entities.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // Cerca la stanza usando gli ID degli oggetti correlati
    Optional<ChatRoom> findByViaggioIdAndViaggiatoreUsernameIgnoreCase(Long viaggioId, String viaggiatoreUsername);

    // mostra le chat attive per organizzatore
    List<ChatRoom> findByOrganizzatoreId(Long organizzatoreId);

    //mostra le chat attive per viaggiatore
    List<ChatRoom> findByViaggiatoreId(Long viaggiatoreId);

    List<ChatRoom> findByViaggioOrganizzatoreUsernameIgnoreCase(String username);
   List<ChatRoom> findByViaggiatoreUsernameIgnoreCase(String viaggiatoreUsername);
    void deleteByViaggioId(Long viaggioId);
}
