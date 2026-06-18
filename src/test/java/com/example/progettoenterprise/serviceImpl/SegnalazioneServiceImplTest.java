package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.*;
import com.example.progettoenterprise.data.repositories.*;
import com.example.progettoenterprise.data.repositories.specifications.SegnalazioneSpecification;
import com.example.progettoenterprise.dto.SegnalazioneDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SegnalazioneServiceImplTest {

    @Mock private SegnalazioneRepository segnalazioneRepository;
    @Mock private UtenteRepository utenteRepository;
    @Mock private MessaggioChatRepository messaggioChatRepository;
    @Mock private RecensioneRepository recensioneRepository;
    @Mock private ViaggioRepository viaggioRepository;
    @Mock private ModelMapper modelMapper;
    @Mock private MessageLang messageLang;
    @Mock private Keycloak keycloakAdminClient;
    @Mock private EmailServiceImpl emailService;

    @InjectMocks
    private SegnalazioneServiceImpl segnalazioneService;

    private Segnalazione segnalazioneMock;
    private Utente utenteMock;
    private SegnalazioneDTO dtoMock;

    @BeforeEach
    void setUp() {

        segnalazioneMock = new Segnalazione();
        segnalazioneMock.setId(10L);

        utenteMock = mock(Utente.class);
        dtoMock = new SegnalazioneDTO();
    }


    @Test
    @DisplayName("Crea Segnalazione: Successo")
    void testCreaSegnalazione_Successo() {
        dtoMock.setTipo("UTENTE");

        when(modelMapper.map(dtoMock, Segnalazione.class)).thenReturn(segnalazioneMock);
        when(segnalazioneRepository.save(any(Segnalazione.class))).thenReturn(segnalazioneMock);
        when(modelMapper.map(segnalazioneMock, SegnalazioneDTO.class)).thenReturn(dtoMock);

        SegnalazioneDTO risultato = segnalazioneService.creaSegnalazione(dtoMock, 1L);

        assertNotNull(risultato);
        verify(segnalazioneRepository, times(1)).save(any(Segnalazione.class));
    }

    @Test
    @DisplayName("Crea Segnalazione: Errore Tipo non valido")
    void testCreaSegnalazione_ErroreTipo() {
        dtoMock.setTipo("TIPO_INVENTATO");

        IllegalArgumentException eccezione = assertThrows(IllegalArgumentException.class, () ->
                segnalazioneService.creaSegnalazione(dtoMock, 1L));

        assertTrue(eccezione.getMessage().contains("Tipo di segnalazione non valido"));
        verify(segnalazioneRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cerca Segnalazioni: Successo")
    @SuppressWarnings("unchecked")
    void testCercaSegnalazioni_Successo() {
        Page<Segnalazione> pagina = new PageImpl<>(List.of(segnalazioneMock));
        when(segnalazioneRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(pagina);
        when(modelMapper.map(any(), eq(SegnalazioneDTO.class))).thenReturn(new SegnalazioneDTO());

        List<SegnalazioneDTO> risultati = segnalazioneService.cercaSegnalazioni(new SegnalazioneSpecification.SegnalazioneFilter(), 0);

        assertEquals(1, risultati.size());
    }

    @Test
    @DisplayName("Cerca Segnalazioni: Errore Pagina Invalida")
    @SuppressWarnings("unchecked")
    void testCercaSegnalazioni_PaginaInvalida() {
        Page<Segnalazione> pagina = mock(Page.class);
        when(pagina.getTotalPages()).thenReturn(2);
        when(segnalazioneRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(pagina);
        when(messageLang.getMessage("segnalazione.invalid_page")).thenReturn("Pagina non valida");

        assertThrows(IllegalArgumentException.class, () ->
                segnalazioneService.cercaSegnalazioni(new SegnalazioneSpecification.SegnalazioneFilter(), 5));
    }

    @Test
    @DisplayName("Prendi in Carico: Successo")
    void testPrendiInCarico() {
        when(segnalazioneRepository.findById(10L)).thenReturn(Optional.of(segnalazioneMock));
        when(segnalazioneRepository.save(segnalazioneMock)).thenReturn(segnalazioneMock);
        when(modelMapper.map(segnalazioneMock, SegnalazioneDTO.class)).thenReturn(new SegnalazioneDTO());

        segnalazioneService.prendiInCarico(10L, 1L);

        assertEquals(Segnalazione.StatoSegnalazione.IN_LAVORAZIONE, segnalazioneMock.getStato());
    }

    @Test
    @DisplayName("Rifiuta Segnalazione: Successo")
    void testRifiutaSegnalazione() {
        when(segnalazioneRepository.findById(10L)).thenReturn(Optional.of(segnalazioneMock));
        when(segnalazioneRepository.save(segnalazioneMock)).thenReturn(segnalazioneMock);
        when(modelMapper.map(segnalazioneMock, SegnalazioneDTO.class)).thenReturn(new SegnalazioneDTO());

        segnalazioneService.rifiutaSegnalazione(10L, 1L);

        assertEquals(Segnalazione.StatoSegnalazione.RIFIUTATA, segnalazioneMock.getStato());
    }

    @Test
    @DisplayName("Risolvi Segnalazione: Ban Utente (Con blocco Keycloak)")
    void testRisolviSegnalazione_BanUtente() {
        segnalazioneMock.setTipo(Segnalazione.TipoEntita.UTENTE);
        segnalazioneMock.setIdRiferimento(2L);

        when(segnalazioneRepository.findById(10L)).thenReturn(Optional.of(segnalazioneMock));
        when(utenteRepository.findById(2L)).thenReturn(Optional.of(utenteMock));
        when(utenteMock.getRuolo()).thenReturn(Utente.Ruolo.ROLE_VIAGGIATORE);
        when(utenteMock.getUsername()).thenReturn("mario");
        when(utenteMock.getEmail()).thenReturn("mario@email.it");

        // Simulazione sistema esterno
        RealmResource realmMock = mock(RealmResource.class);
        UsersResource usersMock = mock(UsersResource.class);
        UserResource userResMock = mock(UserResource.class);
        UserRepresentation userRep = new UserRepresentation();
        userRep.setId("id-keycloak");

        when(keycloakAdminClient.realm(anyString())).thenReturn(realmMock);
        when(realmMock.users()).thenReturn(usersMock);
        when(usersMock.search("mario")).thenReturn(List.of(userRep));
        when(usersMock.get("id-keycloak")).thenReturn(userResMock);

        when(segnalazioneRepository.save(segnalazioneMock)).thenReturn(segnalazioneMock);
        when(modelMapper.map(segnalazioneMock, SegnalazioneDTO.class)).thenReturn(new SegnalazioneDTO());

        segnalazioneService.risolviSegnalazione(10L, 1L, true);

        verify(utenteMock).setAttivo(false);
        verify(emailService).inviaEmailBan("mario@email.it", "mario");
    }

    @Test
    @DisplayName("Risolvi Segnalazione: Errore Ban Organizzatore")
    void testRisolviSegnalazione_ErroreBanOrganizzatore() {
        segnalazioneMock.setTipo(Segnalazione.TipoEntita.UTENTE);
        segnalazioneMock.setIdRiferimento(2L);

        when(segnalazioneRepository.findById(10L)).thenReturn(Optional.of(segnalazioneMock));
        when(utenteRepository.findById(2L)).thenReturn(Optional.of(utenteMock));
        when(utenteMock.getRuolo()).thenReturn(Utente.Ruolo.ROLE_ORGANIZZATORE);

        IllegalArgumentException ecc = assertThrows(IllegalArgumentException.class, () ->
                segnalazioneService.risolviSegnalazione(10L, 1L, true));
        assertTrue(ecc.getMessage().contains("Impossibile sanzionare o bannare un Organizzatore"));
    }

    @Test
    @DisplayName("Risolvi Segnalazione: Messaggio (Avvertimento)")
    void testRisolviSegnalazione_Messaggio_Avvertimento() {
        segnalazioneMock.setTipo(Segnalazione.TipoEntita.MESSAGGIO);
        segnalazioneMock.setIdRiferimento(50L);

        MessaggioChat msg = new MessaggioChat();
        msg.setMittenteUsername("luca");
        msg.setTesto("Messaggio brutto");

        when(segnalazioneRepository.findById(10L)).thenReturn(Optional.of(segnalazioneMock));
        when(messaggioChatRepository.findById(50L)).thenReturn(Optional.of(msg));
        when(utenteRepository.findByUsername("luca")).thenReturn(Optional.of(utenteMock));
        when(utenteMock.getEmail()).thenReturn("luca@email.it");
        when(utenteMock.getUsername()).thenReturn("luca");

        when(segnalazioneRepository.save(segnalazioneMock)).thenReturn(segnalazioneMock);
        when(modelMapper.map(segnalazioneMock, SegnalazioneDTO.class)).thenReturn(new SegnalazioneDTO());

        segnalazioneService.risolviSegnalazione(10L, 1L, false);

        verify(messaggioChatRepository).delete(msg);
        verify(emailService).inviaEmailAvvertimento(eq("luca@email.it"), eq("luca"), anyString());
    }
    @Test
    @DisplayName("Risolvi Segnalazione: Recensione (Sospensione)")
    void testRisolviSegnalazione_Recensione_Sospensione() {
        segnalazioneMock.setTipo(Segnalazione.TipoEntita.RECENSIONE);
        segnalazioneMock.setIdRiferimento(80L);

        Recensione rec = new Recensione();
        rec.setVoto(5);
        Viaggio v = new Viaggio();
        v.setId(5L);
        rec.setViaggio(v);
        Utente autoreRecensione = mock(Utente.class);
        rec.setUtente(autoreRecensione);

        when(autoreRecensione.getUsername()).thenReturn("cattivo");
        when(segnalazioneRepository.findById(10L)).thenReturn(Optional.of(segnalazioneMock));
        when(recensioneRepository.findById(80L)).thenReturn(Optional.of(rec));
        when(utenteRepository.findByUsername("cattivo")).thenReturn(Optional.of(utenteMock));

        when(utenteMock.getRuolo()).thenReturn(Utente.Ruolo.ROLE_VIAGGIATORE);
        when(utenteMock.getUsername()).thenReturn("cattivo");

        when(segnalazioneRepository.save(segnalazioneMock)).thenReturn(segnalazioneMock);
        when(modelMapper.map(segnalazioneMock, SegnalazioneDTO.class)).thenReturn(new SegnalazioneDTO());

        segnalazioneService.risolviSegnalazione(10L, 1L, true);

        verify(recensioneRepository).delete(rec);
        verify(viaggioRepository).ricalcolaMediaPerEliminazione(5L, 5);
        verify(utenteMock).setAttivo(false);
    }

    @Test
    @DisplayName("Conta Segnalazioni Aperte")
    void testContaSegnalazioniAperte() {
        when(segnalazioneRepository.countByStato(Segnalazione.StatoSegnalazione.APERTA)).thenReturn(7L);
        assertEquals(7L, segnalazioneService.contaSegnalazioniAperte());
    }
}