package com.example.progettoenterprise.data.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "viaggiatore")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true) // serve ad includere i campi di Utente
@NoArgsConstructor
@AllArgsConstructor
public class Viaggiatore extends Utente {

    @OneToMany(mappedBy = "viaggiatore", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prenotazione> miePrenotazioni;


    @OneToMany(mappedBy = "viaggiatore", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pagamento> carteDiCredito = new ArrayList<>();

}