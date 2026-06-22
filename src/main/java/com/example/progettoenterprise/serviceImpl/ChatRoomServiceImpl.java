package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.data.entities.ChatRoom;
import com.example.progettoenterprise.data.entities.MessaggioChat;
import com.example.progettoenterprise.data.entities.Viaggio;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.repositories.ChatRoomRepository;
import com.example.progettoenterprise.data.repositories.MessaggioChatRepository;
import com.example.progettoenterprise.data.repositories.ViaggioRepository;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.service.ChatRoomService;
import com.example.progettoenterprise.dto.ChatRoomDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessaggioChatRepository messaggioChatRepository;
    private final ViaggioRepository viaggioRepository;
    private final UtenteRepository utenteRepository;

    @Transactional
    public ChatRoom ottieniOCreaStanza(Long viaggioId, String viaggiatoreUsername) {
        //controllo sw stanza già presente
        return chatRoomRepository.findByViaggioIdAndViaggiatoreUsernameIgnoreCase(viaggioId, viaggiatoreUsername)
                .orElseGet(() -> {

                    Viaggio viaggio = viaggioRepository.findById(viaggioId)
                            .orElseThrow(() -> new RuntimeException("Viaggio non trovato"));

                    Utente viaggiatore = utenteRepository.findByUsername(viaggiatoreUsername)
                            .orElseThrow(() -> new RuntimeException("Viaggiatore non trovato"));


                    Utente organizzatore = viaggio.getOrganizzatore();

                    ChatRoom nuovaStanza = ChatRoom.builder()
                            .viaggio(viaggio)
                            .viaggiatore(viaggiatore)
                            .organizzatore(organizzatore)
                            .build();

                    return chatRoomRepository.save(nuovaStanza);
                });
    }

    @Transactional
    public MessaggioChat salvaMessaggio(Long roomId, String mittenteUsername, String testo) {
        ChatRoom stanza = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Stanza dei messaggi inesistente"));

        MessaggioChat nuovoMessaggio = MessaggioChat.builder()
                .chatRoom(stanza)
                .mittenteUsername(mittenteUsername)
                .testo(testo)
                .dataInvio(LocalDateTime.now())
                .build();

        return messaggioChatRepository.save(nuovoMessaggio);
    }

    public List<MessaggioChat> ottieniCronologia(Long roomId) {
        return messaggioChatRepository.findByChatRoomIdOrderByDataInvioAsc(roomId);
    }

    @Override
    public List<ChatRoomDTO> ottieniStanzePerOrganizzatore(String organizzatoreUsername) {
        return chatRoomRepository.findByViaggioOrganizzatoreUsernameIgnoreCase(organizzatoreUsername).stream()
                .map(stanza -> {
                    // Recupera la data dell'ultimo messaggio inviato
                    LocalDateTime dataUltimo = messaggioChatRepository.findDataUltimoMessaggioPerStanza(stanza.getId());

                    return ChatRoomDTO.builder()
                            .id(stanza.getId())
                            .viaggioId(stanza.getViaggio().getId())
                            .titoloViaggio(stanza.getViaggio().getTitolo())
                            .viaggiatoreUsername(stanza.getViaggiatore().getUsername())
                            .dataUltimoMessaggio(dataUltimo)
                            .build();
                })
                .collect(Collectors.toList());
    }
    @Override
    public int ottieniNotificheTotali(String username) {
        return messaggioChatRepository.countNotificheTotaliUtente(username);
    }
    @Override
    public void segnaMessaggiComeLetti(Long roomId,String Username){
        messaggioChatRepository.segnaComeLetti(roomId,Username);
    }
    @Override
    public ChatRoom ottieniStanzaPerId(Long roomid){
        return chatRoomRepository.findById(roomid).orElseThrow(()->new RuntimeException("Stanza non trovata"));
    }
    @Override
    public List<ChatRoomDTO> ottieniStanzePerViaggiatore(String viaggiatoreUsername) {
        return chatRoomRepository.findByViaggiatoreUsernameIgnoreCase(viaggiatoreUsername).stream()
                .map(stanza -> {
                    // Recupera la data dell'ultimo messaggio inviato
                    LocalDateTime dataUltimo = messaggioChatRepository.findDataUltimoMessaggioPerStanza(stanza.getId());

                    return ChatRoomDTO.builder()
                            .id(stanza.getId())
                            .viaggioId(stanza.getViaggio().getId())
                            .titoloViaggio(stanza.getViaggio().getTitolo())
                            .organizzatoreUsername(stanza.getOrganizzatore().getUsername())
                            .dataUltimoMessaggio(dataUltimo)
                            .build();
                })
                .collect(Collectors.toList());
    }
}