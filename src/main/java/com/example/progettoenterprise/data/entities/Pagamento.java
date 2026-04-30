package com.example.progettoenterprise.data.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pagamento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pagamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String titolareCarta;

    @Column(length = 16)
    private String numeroCarta;

    @Column(length = 5)
    private String dataScadenza;

    @Column(length = 3)
    private String cvv; //

    private String circuito;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viaggiatore_id")
    private Viaggiatore viaggiatore;





}
