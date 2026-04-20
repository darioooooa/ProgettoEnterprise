package com.example.progettoenterprise.config;

import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.dto.UtenteDTO;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper getModelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Configurazione per far leggere i campi privati e attivare il matching automatico
        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

        // Definizione della mappatura specifica tra Entity (Utente) e DTO (UtenteDTO)
        modelMapper.createTypeMap(Utente.class, UtenteDTO.class).addMappings(new PropertyMap<Utente, UtenteDTO>() {
            @Override
            protected void configure() {
                // Usiamo un convertitore personalizzato per unire nome e cognome
                using(ctx -> generateFullname(((Utente) ctx.getSource()).getNome(), ((Utente) ctx.getSource()).getCognome()))
                        // Mappiamo il risultato nel campo nomeCompleto del DTO
                        .map(source, destination.getNomeCompleto());
            }
        });

        return modelMapper;
    }

    // Metodo helper per generare la stringa Nome + Cognome
    private String generateFullname(String nome, String cognome) {
        if (nome == null && cognome == null) return "";
        return (nome != null ? nome : "") + " " + (cognome != null ? cognome : "");
    }
}