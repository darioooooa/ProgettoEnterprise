package com.example.progettoenterprise.data.repositories;
import com.example.progettoenterprise.data.entities.Prenotazione;
import com.example.progettoenterprise.data.entities.Viaggio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Date;
import java.util.List;

public interface ViaggioRepository extends JpaRepository<Viaggio,Long> {
    List<Viaggio> findViaggioByDestinazione(Long destinazione);
    List<Viaggio> findViaggioByPrezzo(Long prezzo);
    List<Viaggio> findViaggioByDataInizio(Date dataInizio);
    List<Viaggio> findViaggioByDataFine(Date dataFine);
    List<Viaggio> findViaggioByTitolo(String titolo);
    List<Viaggio> findViaggioByOrganizzatoreId(Long organizzatore_id);


}
