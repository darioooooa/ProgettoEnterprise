package com.example.progettoenterprise.data.entities;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "utente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@EntityListeners(AuditingEntityListener.class)
public abstract class Utente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Lo username è obbligatorio")
    @Size(max = 50, message = "Lo username non può superare i 50 caratteri")
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 50)
    @Size(max = 50, message = "L'email può contenere al massimo 50 caratteri")
    @Email(message = "Inserisci un indirizzo email valido")
    @NotBlank(message = "L'email è obbligatoria")
    private String email;

    @Column(nullable = false)
    @Size(min = 6, message = "La password deve contenere almeno 6 caratteri")
    @NotBlank(message = "La password è obbligatoria")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Permette di escludere la password dalla risposta JSON del server
    private String password;

    @Column(length = 50)
    @Size(max = 50, message = "Il nome non può superare i 50 caratteri")
    private String nome;

    @Column(length = 50)
    @Size(max = 50, message = "Il cognome non può superare i 50 caratteri")
    private String cognome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) // Permette di escludere il ruolo dalla risposta JSON del client
    private Ruolo ruolo;

    public enum Ruolo {
        ROLE_VIAGGIATORE,
        ROLE_ORGANIZZATORE,
        ROLE_ADMIN
    }

    @Column(nullable = false,columnDefinition = "boolean default true")
    private boolean isAttivo = true;

    @Column(length = 500)
    private String motivoSospensione;

    @CreatedDate
    @Column(name = "data_creazione", updatable = false)
    private LocalDateTime dataCreazione;

    @LastModifiedDate
    @Column(name = "data_ultima_modifica")
    private LocalDateTime dataUltimaModifica;

    @OneToMany(mappedBy = "utente", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ListaUtente> listeAccessibili;

    @OneToMany(mappedBy = "richiedente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Amicizia> richiesteInviate;

    @OneToMany(mappedBy = "ricevente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Amicizia> richiesteRicevute;


    @OneToMany(mappedBy = "proprietario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItinerarioPreferito> mieiItinerari;

    @OneToMany(mappedBy = "utente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notifica> notifiche;



}
