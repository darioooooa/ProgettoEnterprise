package com.example.progettoenterprise.data.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admin")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Admin extends Utente{

}
