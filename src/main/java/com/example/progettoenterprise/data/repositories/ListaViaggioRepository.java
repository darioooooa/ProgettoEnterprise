package com.example.progettoenterprise.data.repositories;

import com.example.progettoenterprise.data.entities.ListaViaggio;
import com.example.progettoenterprise.data.entities.Viaggio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListaViaggioRepository extends JpaRepository<ListaViaggio, Long> {
    List<ListaViaggio> findByViaggio(Viaggio viaggio);
    List<ListaViaggio> findByViaggio_Id(Long viaggioId);

    List<ListaViaggio> findByViaggioTitolo(String viaggioTitolo);
    List<ListaViaggio> findByViaggioPrezzoLessThan(Double prezzo);

}
