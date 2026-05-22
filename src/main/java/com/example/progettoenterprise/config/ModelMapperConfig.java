package com.example.progettoenterprise.config;

import com.example.progettoenterprise.data.entities.ItinerarioPreferito;
import com.example.progettoenterprise.data.entities.ListaViaggio;
import com.example.progettoenterprise.data.entities.Utente;
import com.example.progettoenterprise.data.entities.Viaggio;
import com.example.progettoenterprise.dto.ItinerarioPreferitoDTO;
import com.example.progettoenterprise.dto.UtenteDTO;
import com.example.progettoenterprise.dto.ViaggioDTO;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
        org.modelmapper.Converter<Set<ListaViaggio>, List<ViaggioDTO>> convertitoreViaggi = ctx -> {
            if (ctx.getSource() == null) return java.util.Collections.emptyList();
            return ctx.getSource().stream()
                    .map(collegamento -> {
                        Viaggio viaggioReale = collegamento.getViaggio();
                        ViaggioDTO viaggioDto = new ViaggioDTO();
                        viaggioDto.setId(viaggioReale.getId());
                        viaggioDto.setTitolo(viaggioReale.getTitolo());
                        viaggioDto.setDestinazione(viaggioReale.getDestinazione());
                        viaggioDto.setDataInizio(LocalDate.from(viaggioReale.getDataInizio()));
                        viaggioDto.setDataFine(LocalDate.from(viaggioReale.getDataFine()));
                        return viaggioDto;
                    })
                    .collect(Collectors.toList());
        };

        modelMapper.createTypeMap(ItinerarioPreferito.class, ItinerarioPreferitoDTO.class)
                .addMappings(mapper -> mapper.using(convertitoreViaggi)
                        .map(ItinerarioPreferito::getContenuti, ItinerarioPreferitoDTO::setViaggiContenuti));

        return modelMapper;
    }

    // Metodo helper per generare la stringa Nome + Cognome
    private String generateFullname(String nome, String cognome) {
        if (nome == null && cognome == null) return "";
        return (nome != null ? nome : "") + " " + (cognome != null ? cognome : "");
    }
}