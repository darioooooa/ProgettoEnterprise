package com.example.progettoenterprise.data.repositories;

import com.example.progettoenterprise.data.entities.ListaViaggio;
import com.example.progettoenterprise.data.entities.Viaggio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListaViaggioRepository extends JpaRepository<ListaViaggio, Long> {
    List<ListaViaggio> findByViaggio(Viaggio viaggio);
    List<ListaViaggio> findByViaggio_Id(Long viaggioId);

    List<ListaViaggio> findByViaggioTitolo(String viaggioTitolo);
    List<ListaViaggio> findByViaggioPrezzoLessThan(Double prezzo);

}
