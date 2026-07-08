package com.example.progettoenterprise.data.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "tag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_tag", nullable = false, unique = true, length = 50)
    private String nomeTag;

    @ManyToMany(mappedBy = "tags")
    private Set<Viaggio> viaggi;
}