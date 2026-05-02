package com.example.progettoenterprise.serviceImpl;


import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.data.entities.Organizzatore;
import com.example.progettoenterprise.data.repositories.OrganizzatoreRepository;
import com.example.progettoenterprise.data.service.OrganizzatoreService;
import com.example.progettoenterprise.dto.OrganizzatoreDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganizzatoreServiceImpl implements OrganizzatoreService {

    private final OrganizzatoreRepository organizzatoreRepository;
    private final ModelMapper modelMapper;
    private final MessageLang messageLang;

    @Override
    public OrganizzatoreDTO getProfilo(Long id){
        Organizzatore organizzatore=organizzatoreRepository.findById(id).orElseThrow(()->
                new EntityNotFoundException(messageLang.getMessage("organizzatore.notexist",id)));
        return modelMapper.map(organizzatore,OrganizzatoreDTO.class);
    }

    @Override
    @Transactional
    public OrganizzatoreDTO updateProfilo(Long id, OrganizzatoreDTO organizzatoreDTO){
        Organizzatore organizzatore=organizzatoreRepository.findById(id).orElseThrow(()->
                new EntityNotFoundException(messageLang.getMessage("organizzatore.notexist",id)));

        organizzatore.setNome(organizzatoreDTO.getNome());
        organizzatore.setCognome(organizzatoreDTO.getCognome());


        return modelMapper.map(organizzatoreRepository.save(organizzatore),OrganizzatoreDTO.class);
    }

}
