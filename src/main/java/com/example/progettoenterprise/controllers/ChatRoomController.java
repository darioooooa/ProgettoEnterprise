package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.entities.ChatRoom;
import com.example.progettoenterprise.data.entities.MessaggioChat;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.service.ChatRoomService;
import com.example.progettoenterprise.dto.ChatRoomDTO;
import com.example.progettoenterprise.dto.MessaggioChatDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ChatRoomController {

    private final ChatRoomService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UtenteRepository utenteRepository;
    private final com.example.progettoenterprise.data.repositories.MessaggioChatRepository messaggioRepository;

    @GetMapping("/api/chat/stanza")
    public ResponseEntity<Long> ottieniOCreaStanza(
            @RequestParam Long viaggioId,
            @RequestParam String viaggiatoreUsername) {

        ChatRoom stanza = chatService.ottieniOCreaStanza(viaggioId, viaggiatoreUsername);
        return ResponseEntity.ok(stanza.getId());
    }


    @GetMapping("/api/chat/stanza/{roomId}/cronologia")
    public ResponseEntity<List<MessaggioChatDTO>> ottieniCronologia(@PathVariable Long roomId) {
        List<MessaggioChatDTO> cronologia = chatService.ottieniCronologia(roomId).stream()
                .map(m -> MessaggioChatDTO.builder()
                        .id(m.getId())
                        .chatRoomId(m.getChatRoom().getId())
                        .mittenteUsername(m.getMittenteUsername())
                        .testo(m.getTesto())
                        .dataInvio(m.getDataInvio())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(cronologia);
    }


    @GetMapping("/api/chat/organizzatore")
    public ResponseEntity<List<ChatRoomDTO>> ottieniStanzePerOrganizzatore(
            @RequestParam String organizzatoreUsername) {

        List<ChatRoomDTO> stanze = chatService.ottieniStanzePerOrganizzatore(organizzatoreUsername);


        for (ChatRoomDTO stanza : stanze) {
            int nonLetti = messaggioRepository.countNotifichePerStanza(stanza.getId(), organizzatoreUsername);
            stanza.setMessaggiNonLetti(nonLetti);
        }

        return ResponseEntity.ok(stanze);
    }

    @MessageMapping("/chat/invia/{roomId}")
    public void riceviEInviaMessaggio(
            @DestinationVariable("roomId") Long roomId,
            @Payload MessaggioChatDTO messaggioDTO) {

        System.out.println("📬 [WEBSOCKET] Messaggio intercettato con successo dal controller per la stanza: " + roomId);

        try {
            // Salvataggio sul DB
            MessaggioChat messaggioSalvato = chatService.salvaMessaggio(
                    roomId,
                    messaggioDTO.getMittenteUsername(),
                    messaggioDTO.getTesto()
            );

            messaggioDTO.setId(messaggioSalvato.getId());
            messaggioDTO.setDataInvio(messaggioSalvato.getDataInvio());

            if (utenteRepository != null) {
                utenteRepository.findByUsername(messaggioDTO.getMittenteUsername())
                        .ifPresent(utente -> messaggioDTO.setMittenteId(utente.getId()));
            }

           // Spedizione standard sul topic della chat room
            messagingTemplate.convertAndSend("/topic/chatroom/" + roomId, messaggioDTO);
            System.out.println("🚀 [WEBSOCKET] Messaggio inoltrato sui canali live della stanza!");


            ChatRoom stanza = chatService.ottieniStanzaPerId(roomId);

            if (stanza != null) {

                String organizzatoreUsername = (stanza.getOrganizzatore() != null) ? stanza.getOrganizzatore().getUsername() : "";
                String viaggiatoreUsername = (stanza.getViaggiatore() != null) ? stanza.getViaggiatore().getUsername() : "";


                String destinatarioUsername = organizzatoreUsername.equals(messaggioDTO.getMittenteUsername())
                        ? viaggiatoreUsername
                        : organizzatoreUsername;

                if (!destinatarioUsername.isEmpty()) {

                    messagingTemplate.convertAndSend("/topic/notifiche/" + destinatarioUsername, messaggioDTO);
                    System.out.println("🔔 [NOTIFICA LIVE] Segnale inviato al canale personale di: " + destinatarioUsername);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Errore nel salvataggio/invio del messaggio:");
            e.printStackTrace();
        }
    }

    @GetMapping("/api/chat/notifiche-totali")
    public ResponseEntity<Integer> ottieniNotificheTotali(@RequestParam String username) {
        int totali = chatService.ottieniNotificheTotali(username);
        return ResponseEntity.ok(totali);
    }


    @PatchMapping("/api/chat/{roomId}/leggi")
    public ResponseEntity<Void> segnaComeLetti(
            @PathVariable Long roomId,
            @RequestParam String username) {

        chatService.segnaMessaggiComeLetti(roomId, username);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/api/chat/viaggiatore")
    public ResponseEntity<List<ChatRoomDTO>> ottieniStanzePerViaggiatore(
            @RequestParam String viaggiatoreUsername) {

        List<ChatRoomDTO> stanze = chatService.ottieniStanzePerViaggiatore(viaggiatoreUsername);


        for (ChatRoomDTO stanza : stanze) {
            int nonLetti = messaggioRepository.countNotifichePerStanza(stanza.getId(), viaggiatoreUsername);
            stanza.setMessaggiNonLetti(nonLetti);
        }

        return ResponseEntity.ok(stanze);
    }
}