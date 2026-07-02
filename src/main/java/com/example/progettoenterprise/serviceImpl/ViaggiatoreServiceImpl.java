package com.example.progettoenterprise.serviceImpl;

import com.example.progettoenterprise.config.CacheConfig;
import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.RichiestaPromozione;
import com.example.progettoenterprise.data.entities.Viaggiatore;
import com.example.progettoenterprise.data.repositories.OrganizzatoreRepository;
import com.example.progettoenterprise.data.repositories.RichiestaPromozioneRepository;
import com.example.progettoenterprise.data.repositories.UtenteRepository;
import com.example.progettoenterprise.data.repositories.ViaggiatoreRepository;
import com.example.progettoenterprise.data.service.ViaggiatoreService;
import com.example.progettoenterprise.dto.RichiestaPromozioneDTO;
import com.example.progettoenterprise.dto.ViaggiatoreDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViaggiatoreServiceImpl implements ViaggiatoreService {

    private final ViaggiatoreRepository viaggiatoreRepository;
    private final ModelMapper modelMapper;
    private final MessageLang messageLang;
    private final UtenteRepository utenteRepository;
    private final Keycloak keycloak;
    private final OrganizzatoreRepository organizzatoreRepository;
    private final RichiestaPromozioneRepository richiestaPromozioneRepository;
    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;


    @Override
    @Transactional(readOnly = true)
    public ViaggiatoreDTO getProfiloViaggiatore(Long id) {
        Viaggiatore viaggiatore=  viaggiatoreRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Recupero fallito: Impossibile recuperare il viaggiatore con id {}", id);
                    return new EntityNotFoundException( messageLang.getMessage("utente.notexist",id));
                });
        return modelMapper.map(viaggiatore, ViaggiatoreDTO.class);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_UTENTI_AUTH, key = "#result.email")
    public ViaggiatoreDTO aggiornaProfilo(Long id, ViaggiatoreDTO viaggiatoreDTO) {
        Viaggiatore viaggiatore= viaggiatoreRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Aggiornamento fallito: Impossibile recuperare il profilo del viaggiatore con id {}", id);
                    return new EntityNotFoundException( messageLang.getMessage("utente.notexist",id));
                });
        viaggiatore.setNome(viaggiatoreDTO.getNome());
        viaggiatore.setCognome(viaggiatoreDTO.getCognome());
        Viaggiatore salvato= viaggiatoreRepository.save(viaggiatore);
        return modelMapper.map(salvato,ViaggiatoreDTO.class);

    }

    @Override
    @Transactional(readOnly = true)
    public List<ViaggiatoreDTO> cercaViaggiatori(String query) {
        return viaggiatoreRepository.findByUsernameContainingIgnoreCase(query)
                .stream()
                .map(v -> modelMapper.map(v, ViaggiatoreDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RichiestaPromozioneDTO creaRichiestaPromozione(Long viaggiatoreId, RichiestaPromozioneDTO dto, MultipartFile file) {

        Viaggiatore utenteAttuale = viaggiatoreRepository.findById(viaggiatoreId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));

        if (richiestaPromozioneRepository.existsByViaggiatoreIdAndStato(viaggiatoreId, RichiestaPromozione.StatoRichiesta.IN_ATTESA)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Hai già una richiesta in fase di valutazione.");
        }
        if (richiestaPromozioneRepository.existsByViaggiatoreIdAndStato(viaggiatoreId, RichiestaPromozione.StatoRichiesta.APPROVATA)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sei già un organizzatore approvato!");
        }

        if (richiestaPromozioneRepository.existsByUsernameRichiestoAndStatoNot(dto.getUsernameRichiesto(), RichiestaPromozione.StatoRichiesta.RIFIUTATA)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Questo username è attualmente richiesto da un'altra candidatura in corso.");
        }
        if (!utenteAttuale.getUsername().equals(dto.getUsernameRichiesto()) && utenteRepository.existsByUsername(dto.getUsernameRichiesto())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Questo username è già in uso.");
        }
        if (richiestaPromozioneRepository.existsByEmailProfessionaleAndStatoNot(dto.getEmailProfessionale(), RichiestaPromozione.StatoRichiesta.RIFIUTATA)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Questa email è in fase di valutazione in un'altra richiesta.");
        }

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "È obbligatorio allegare un documento.");
        }
        String contentType = file.getContentType();

        List<String> tipiConsentiti = List.of(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        );

        if (contentType == null || !tipiConsentiti.contains(contentType)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sono consentiti solo file PDF, DOC e DOCX."
            );
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Il file non può superare i 5 MB."
            );
        }

        String linkDocumentoSalvato;
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1) // -1 per partSize auto
                            .contentType(file.getContentType())
                            .build()
            );

            linkDocumentoSalvato = fileName;
        } catch (Exception e) {
            log.error("Errore salvataggio file su MinIO", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore salvataggio documento");
        }

        RichiestaPromozione richiesta = richiestaPromozioneRepository
                .findFirstByViaggiatoreIdAndStatoOrderByDataRichiestaDesc(viaggiatoreId, RichiestaPromozione.StatoRichiesta.RIFIUTATA)
                .orElse(new RichiestaPromozione());

        richiesta.setViaggiatore(utenteAttuale);
        richiesta.setUsernameRichiesto(dto.getUsernameRichiesto());
        richiesta.setEmailProfessionale(dto.getEmailProfessionale());
        richiesta.setMotivazione(dto.getMotivazione());
        richiesta.setBiografiaProfessionale(dto.getBiografiaProfessionale());
        richiesta.setDocumentiLink(linkDocumentoSalvato); // Usiamo il percorso del file salvato
        richiesta.setStato(RichiestaPromozione.StatoRichiesta.IN_ATTESA); // Torna in attesa
        richiesta.setDataRichiesta(LocalDateTime.now());

        // Reset dei campi di valutazione precedenti
        richiesta.setAdminId(null);
        richiesta.setDataValutazione(null);

        RichiestaPromozione salvata = richiestaPromozioneRepository.save(richiesta);

        return modelMapper.map(salvata, RichiestaPromozioneDTO.class);
    }

    public RichiestaPromozione trovaRichiestaPendente(Long viaggiatoreId) {
        return richiestaPromozioneRepository.findByViaggiatoreIdAndStato(viaggiatoreId, RichiestaPromozione.StatoRichiesta.IN_ATTESA)
                .orElse(null);
    }
}