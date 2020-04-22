package de.smarties.fte_statistics.batch.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "tbl_mitarbeiter_ultimo")
public class ITEmployee {
	
	@Id
	@Column(length = 36, nullable = false) // Muss 36 sein
	private String id;
	
	@Size(min= 0, max = 255) // Validierung
	@Column(length = 255, nullable = false) // Feldgroesse für Create Table 
	private String personalNummer;
	
	@Size(min= 0, max = 255) // Validierung
	@Column(length = 255, nullable = false) // Feldgroesse für Create Table 
	private String nachname;
	
	@Size(min= 0, max = 255) // Validierung
	@Column(length = 255, nullable = false) // Feldgroesse für Create Table 
	private String vorname;
	
	@DecimalMin(value = "0.0", inclusive = false)
	private double fte;
	
	@Size(min= 0, max = 255) // Validierung
	@Column(length = 255, nullable = false) // Feldgroesse für Create Table 
	private String mitarbeiterkreis;
	
	@Size(min= 0, max = 255) // Validierung
	@Column(length = 255, nullable = false) // Feldgroesse für Create Table 
	private String mitarbeitergruppe;
	
	@Size(min= 0, max = 255) // Validierung
	@Column(length = 255, nullable = false) // Feldgroesse für Create Table 
	private String personalteilbereich;
	
	@Size(min= 0, max = 255) // Validierung
	@Column(length = 255, nullable = false) // Feldgroesse für Create Table 
	private String bereich;
	
	@Size(min= 0, max = 255) // Validierung
	@Column(length = 255, nullable = false) // Feldgroesse für Create Table 
	private String abteilung;
	
	@Size(min= 0, max = 255) // Validierung
	@Column(length = 255, nullable = false) // Feldgroesse für Create Table 
	private String ressort;
	
	@Size(min= 0, max = 255) // Validierung
	@Column(length = 255, nullable = false) // Feldgroesse für Create Table 
	private String organisationseinheit;
	
	
	@Column(nullable = false) 
	private LocalDate ersteintritt;
	
	@Size(min= 0, max = 255) // Validierung
	@Column(length = 255, nullable = false) // Feldgroesse für Create Table 
	private String vertragsart;
	
	@Column(nullable = true) 
	private LocalDate austritt;
	
	@Size(min= 0, max = 255) // Validierung
	@Column(length = 255, nullable = false) // Feldgroesse für Create Table 
	private String personalbereich;
	
	@Column(nullable = false) 
	private LocalDateTime erfassungsdatum;

	
	@PrePersist
	public void prePersist() {
		id = UUID.randomUUID().toString();
		erfassungsdatum = LocalDateTime.now();
	}
	
}
