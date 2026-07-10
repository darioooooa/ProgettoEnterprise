package com.example.progettoenterprise.config;

import com.example.progettoenterprise.data.entities.Tag;
import com.example.progettoenterprise.data.repositories.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class TagDataLoader implements CommandLineRunner {

    private final TagRepository tagRepository;

    @Override
    public void run(String... args) {
        log.info("🚀 ===== AVVIO TAG DATA LOADER =====");

        String[] tagDefault = {
                "Mare", "Montagna", "Città d'arte", "Relax",
                "Avventura", "Cultura", "Enogastronomia",
                "Economico", "Lusso", "Inverno", "Estate"
        };

        int inseriti = 0;
        int saltati = 0;

        for (String tagName : tagDefault) {
            if (tagRepository.findByNomeTag(tagName).isEmpty()) {
                Tag nuovoTag = new Tag();
                nuovoTag.setNomeTag(tagName);
                tagRepository.save(nuovoTag);
                inseriti++;
                log.info("✅ Tag inserito: {}", tagName);
            } else {
                saltati++;
                log.debug("⏭️ Tag già esistente, saltato: {}", tagName);
            }
        }

        log.info("🏷️ ===== TAG DATA LOADER COMPLETATO =====");
        log.info("📊 Totale: {} inseriti, {} saltati", inseriti, saltati);
    }
}
