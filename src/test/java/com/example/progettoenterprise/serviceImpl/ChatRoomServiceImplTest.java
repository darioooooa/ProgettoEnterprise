package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.data.entities.ChatRoom;
import com.example.progettoenterprise.data.entities.MessaggioChat;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.entities.Viaggio;
import com.example.progettoenterprise.data.repositories.ChatRoomRepository;
import com.example.progettoenterprise.data.repositories.MessaggioChatRepository;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.repositories.ViaggioRepository;
import com.example.progettoenterprise.dto.ChatRoomDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatRoomServiceImplTest {

    @Mock private ChatRoomRepository chatRoomRepository;
    @Mock private MessaggioChatRepository messaggioChatRepository;
    @Mock private ViaggioRepository viaggioRepository;
    @Mock private UtenteRepository utenteRepository;

    @InjectMocks
    private ChatRoomServiceImpl chatRoomService;

    private Viaggio viaggioMock;
    private Utente viaggiatoreMock;
    private Utente organizzatoreMock;

    @BeforeEach
    void setUp() {
        viaggioMock = mock(Viaggio.class);
        viaggiatoreMock = mock(Utente.class);
        organizzatoreMock = mock(Utente.class);
    }

    @Test
    @DisplayName("Ottieni o Crea Stanza: Stanza già esistente")
    void testOttieniOCreaStanza_Esistente() {
        Long idViaggio = 1L;
        String username = "viaggiatore";
        ChatRoom stanzaEsistente = new ChatRoom();
        stanzaEsistente.setId(10L);

        when(chatRoomRepository.findByViaggioIdAndViaggiatoreUsernameIgnoreCase(idViaggio, username))
                .thenReturn(Optional.of(stanzaEsistente));

        ChatRoom risultato = chatRoomService.ottieniOCreaStanza(idViaggio, username);

        assertNotNull(risultato);
        assertEquals(10L, risultato.getId());
        verify(chatRoomRepository, never()).save(any(ChatRoom.class)); // Non deve creare nulla
    }

    @Test
    @DisplayName("Ottieni o Crea Stanza: Nuova Stanza (Successo)")
    void testOttieniOCreaStanza_Nuova_Successo() {
        Long idViaggio = 1L;
        String username = "viaggiatore";

        when(viaggioMock.getOrganizzatore()).thenReturn(organizzatoreMock);

        // Simuliamo che non esista ancora nessuna stanza
        when(chatRoomRepository.findByViaggioIdAndViaggiatoreUsernameIgnoreCase(idViaggio, username))
                .thenReturn(Optional.empty());

        when(viaggioRepository.findById(idViaggio)).thenReturn(Optional.of(viaggioMock));
        when(utenteRepository.findByUsername(username)).thenReturn(Optional.of(viaggiatoreMock));

        ChatRoom stanzaSalvata = new ChatRoom();
        stanzaSalvata.setId(99L);
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(stanzaSalvata);

        ChatRoom risultato = chatRoomService.ottieniOCreaStanza(idViaggio, username);

        assertNotNull(risultato);
        assertEquals(99L, risultato.getId());
        verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
    }

    @Test
    @DisplayName("Ottieni o Crea Stanza: Errore Viaggio Non Trovato")
    void testOttieniOCreaStanza_ErroreViaggio() {
        when(chatRoomRepository.findByViaggioIdAndViaggiatoreUsernameIgnoreCase(1L, "user"))
                .thenReturn(Optional.empty());
        when(viaggioRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException eccezione = assertThrows(RuntimeException.class, () ->
                chatRoomService.ottieniOCreaStanza(1L, "user"));

        assertEquals("Viaggio non trovato", eccezione.getMessage());
    }

    @Test
    @DisplayName("Ottieni o Crea Stanza: Errore Viaggiatore Non Trovato")
    void testOttieniOCreaStanza_ErroreViaggiatore() {
        when(chatRoomRepository.findByViaggioIdAndViaggiatoreUsernameIgnoreCase(1L, "user"))
                .thenReturn(Optional.empty());
        when(viaggioRepository.findById(1L)).thenReturn(Optional.of(viaggioMock));
        when(utenteRepository.findByUsername("user")).thenReturn(Optional.empty());

        RuntimeException eccezione = assertThrows(RuntimeException.class, () ->
                chatRoomService.ottieniOCreaStanza(1L, "user"));

        assertEquals("Viaggiatore non trovato", eccezione.getMessage());
    }

    @Test
    @DisplayName("Salva Messaggio: Successo")
    void testSalvaMessaggio_Successo() {
        Long idStanza = 10L;
        String testo = "Ciao!";
        ChatRoom stanza = new ChatRoom();
        stanza.setId(idStanza);

        when(chatRoomRepository.findById(idStanza)).thenReturn(Optional.of(stanza));

        MessaggioChat messaggioSalvato = new MessaggioChat();
        messaggioSalvato.setTesto(testo);
        when(messaggioChatRepository.save(any(MessaggioChat.class))).thenReturn(messaggioSalvato);

        MessaggioChat risultato = chatRoomService.salvaMessaggio(idStanza, "mario", testo);

        assertNotNull(risultato);
        assertEquals("Ciao!", risultato.getTesto());
        verify(messaggioChatRepository, times(1)).save(any(MessaggioChat.class));
    }

    @Test
    @DisplayName("Salva Messaggio: Errore Stanza Inesistente")
    void testSalvaMessaggio_ErroreStanza() {
        Long idStanza = 99L;
        when(chatRoomRepository.findById(idStanza)).thenReturn(Optional.empty());

        RuntimeException eccezione = assertThrows(RuntimeException.class, () ->
                chatRoomService.salvaMessaggio(idStanza, "mario", "Ciao!"));

        assertEquals("Stanza dei messaggi inesistente", eccezione.getMessage());
    }

    @Test
    @DisplayName("Ottieni Cronologia: Successo")
    void testOttieniCronologia() {
        Long idStanza = 10L;
        MessaggioChat msg = new MessaggioChat();

        when(messaggioChatRepository.findByChatRoomIdOrderByDataInvioAsc(idStanza))
                .thenReturn(List.of(msg));

        List<MessaggioChat> risultati = chatRoomService.ottieniCronologia(idStanza);

        assertEquals(1, risultati.size());
        verify(messaggioChatRepository, times(1)).findByChatRoomIdOrderByDataInvioAsc(idStanza);
    }

    @Test
    @DisplayName("Ottieni Stanze per Organizzatore: Successo con Mapping")
    void testOttieniStanzePerOrganizzatore() {
        String usernameOrg = "organizzatore1";
        when(viaggioMock.getId()).thenReturn(1L);
        when(viaggioMock.getTitolo()).thenReturn("Viaggio a Roma");
        when(viaggiatoreMock.getUsername()).thenReturn("mario_viaggiatore");

        ChatRoom stanza = new ChatRoom();
        stanza.setId(10L);
        stanza.setViaggio(viaggioMock);
        stanza.setViaggiatore(viaggiatoreMock);

        when(chatRoomRepository.findByViaggioOrganizzatoreUsernameIgnoreCase(usernameOrg))
                .thenReturn(List.of(stanza));

        List<ChatRoomDTO> risultati = chatRoomService.ottieniStanzePerOrganizzatore(usernameOrg);
        assertNotNull(risultati);
        assertEquals(1, risultati.size());
        ChatRoomDTO dto = risultati.get(0);
        assertEquals(10L, dto.getId());
        assertEquals(1L, dto.getViaggioId());
        assertEquals("Viaggio a Roma", dto.getTitoloViaggio());
        assertEquals("mario_viaggiatore", dto.getViaggiatoreUsername());
    }
}