package com.example.progettoenterprise.data.service;

import com.example.progettoenterprise.data.entities.ChatRoom;
import com.example.progettoenterprise.data.entities.MessaggioChat;
import com.example.progettoenterprise.dto.ChatRoomDTO;

import java.util.List;

public interface ChatRoomService {
    ChatRoom ottieniOCreaStanza(Long viaggioId, String viaggiatoreUsername);
    MessaggioChat salvaMessaggio(Long roomId, String mittenteUsername, String testo);
    List<MessaggioChat> ottieniCronologia(Long roomId);
    List<ChatRoomDTO> ottieniStanzePerOrganizzatore(String organizzatoreUsername);
    int ottieniNotificheTotali(String username);
    void segnaMessaggiComeLetti(Long roomId, String username);
    ChatRoom ottieniStanzaPerId(Long roomid);
    List<ChatRoomDTO> ottieniStanzePerViaggiatore(String viaggiatoreUsername);
}
