package de.smarties.fte_statistics.batch.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "tbl_mitarbeiter_ultimo")
public class ITEmployee {
	
	@Id
	@Column(length = 36, nullable = false)
	private String id;
	private String personalNummer;
	private String nachname;
	private String vorname;
	private double fte;
	private String mitarbeiterkreis;
	private String mitarbeitergruppe;
	private String personalteilbereich;
	private String bereich;
	private String abteilung;
	private String ressort;
	private String organisationseinheit;
	private LocalDate ersteintritt;
	private String vertragsart;
	private LocalDate austritt;	
	private String personalbereich;
	private LocalDateTime erfassungsdatum;

	
	@PrePersist
	public void prePersist() {
		id = UUID.randomUUID().toString();
		erfassungsdatum = LocalDateTime.now();
	}
	
}
