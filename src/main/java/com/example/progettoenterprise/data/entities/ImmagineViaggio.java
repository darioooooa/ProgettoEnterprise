package com.example.progettoenterprise.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "immagine_viaggio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImmagineViaggio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // I link potrebbero essere molto lunghi
    @Column(nullable = false, length = 500)
    @Size(max = 500, message = "Limite di caratteri del link superato")
    @NotBlank(message = "L'URL è obbligatorio")
    private String url;

    @Column(nullable = false)
    @NotNull(message = "Specificare se l'immagine deve essere pubblica o privata")
    private boolean pubblica = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viaggio_id", nullable = false)
    private Viaggio viaggio;
}
