package com.example.progettoenterprise.data.repositories;

import com.example.progettoenterprise.data.entities.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

   //per trovare le carte di un viaggiatore
    List<Pagamento> findByViaggiatoreId(Long viaggiatoreId);
}