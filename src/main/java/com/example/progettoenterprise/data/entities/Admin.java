package com.example.progettoenterprise.data.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "admin")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@DiscriminatorValue("ROLE_ADMIN")
public class Admin extends Utente{

}
