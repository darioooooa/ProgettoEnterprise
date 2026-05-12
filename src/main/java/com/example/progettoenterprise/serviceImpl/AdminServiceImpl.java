package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.data.entities.*;
import com.example.progettoenterprise.data.repositories.OrganizzatoreRepository;
import com.example.progettoenterprise.data.repositories.RichiestaPromozioneRepository;
import com.example.progettoenterprise.data.service.AdminService;
import com.example.progettoenterprise.dto.RichiestaPromozioneDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final RichiestaPromozioneRepository richiestaRepository;
    private final OrganizzatoreRepository organizzatoreRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void approvaRichiesta(Long richiestaId, Long adminIdCorrente) {

        RichiestaPromozione richiesta = richiestaRepository.findById(richiestaId)
                .orElseThrow(() -> new EntityNotFoundException("Richiesta non trovata"));


        Viaggiatore viaggiatore = richiesta.getViaggiatore();


        Organizzatore nuovoOrg = new Organizzatore();
        nuovoOrg.setUsername(viaggiatore.getUsername() + "_org"); // Suffisso
        String nuovaEmail = viaggiatore.getEmail().replace("@", "_org@");
        nuovoOrg.setEmail(nuovaEmail);
        nuovoOrg.setNome(viaggiatore.getNome());
        nuovoOrg.setCognome(viaggiatore.getCognome());
        nuovoOrg.setPassword(viaggiatore.getPassword());// Copiamo la password (criptata)
        nuovoOrg.setRuolo(Utente.Ruolo.ROLE_ORGANIZZATORE);


        organizzatoreRepository.save(nuovoOrg);

        richiesta.setStato(RichiestaPromozione.StatoRichiesta.APPROVATA);
        richiesta.setDataValutazione(LocalDateTime.now());
        richiesta.setAdminId(adminIdCorrente);
        richiestaRepository.save(richiesta);
    }
    @Override
    public List<RichiestaPromozioneDTO> getRichieste() {
        return richiestaRepository.findAll()
                .stream()
                .map(richiesta -> {
                    RichiestaPromozioneDTO dto = new RichiestaPromozioneDTO();
                    dto.setId(richiesta.getId());
                    dto.setUsernameViaggiatore(richiesta.getViaggiatore().getUsername());
                    dto.setEmailViaggiatore(richiesta.getViaggiatore().getEmail());
                    dto.setDataRichiesta(richiesta.getDataRichiesta());
                    dto.setMotivazione(richiesta.getMotivazione());
                    dto.setStato(richiesta.getStato().name());
                    dto.setBiografiaProfessionale(richiesta.getBiografiaProfessionale());
                    dto.setDocumentiLink(richiesta.getDocumentiLink());
                    dto.setAdminId(richiesta.getAdminId());

                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public void rifiutaRichiesta(Long richiestaId, String noteAdmin, Long adminIdCorrente) {
        RichiestaPromozione richiesta = richiestaRepository.findById(richiestaId)
                .orElseThrow(() -> new EntityNotFoundException("Richiesta non trovata"));

        richiesta.setStato(RichiestaPromozione.StatoRichiesta.RIFIUTATA);
        richiesta.setMotivazione(noteAdmin);
        richiesta.setDataValutazione(LocalDateTime.now());
        richiesta.setAdminId(adminIdCorrente);
        richiestaRepository.save(richiesta);
    }
}
