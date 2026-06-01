package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.entities.ChatRoom;
import com.example.progettoenterprise.data.entities.MessaggioChat;
import com.example.progettoenterprise.data.service.ChatRoomService;
import com.example.progettoenterprise.dto.ChatRoomDTO;
import com.example.progettoenterprise.dto.MessaggioChatDTO;
import lombok.RequiredArgsConstructor;
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
        return ResponseEntity.ok(stanze);
    }

    // parte messaggistica in tempo realee

    @MessageMapping("/chat/invia/{roomId}")
    public void riceviEInviaMessaggio(
            @DestinationVariable Long roomId,
            @Payload MessaggioChatDTO messaggioDTO) {


        MessaggioChat messaggioSalvato = chatService.salvaMessaggio(
                roomId,
                messaggioDTO.getMittenteUsername(),
                messaggioDTO.getTesto()
        );

        messaggioDTO.setId(messaggioSalvato.getId());
        messaggioDTO.setDataInvio(messaggioSalvato.getDataInvio());

        messagingTemplate.convertAndSend("/topic/chatroom/" + roomId, messaggioDTO);
    }
}