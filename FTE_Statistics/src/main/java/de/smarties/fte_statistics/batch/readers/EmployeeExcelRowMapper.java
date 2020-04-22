package de.smarties.fte_statistics.batch.readers;

import java.time.LocalDate;

import org.springframework.batch.item.excel.RowMapper;
import org.springframework.batch.item.excel.support.rowset.RowSet;

import de.smarties.fte_statistics.batch.entities.ITEmployee;

public class EmployeeExcelRowMapper implements RowMapper<ITEmployee> {

	private static final int PERSONAL_NUMMER_LENGTH = 8;

	@Override
	public ITEmployee mapRow(RowSet rowSet) throws Exception {
		
		String personalnummer = "000000000"+Integer.toString((int)Double.parseDouble(rowSet.getColumnValue(0)));
		personalnummer = personalnummer.substring(personalnummer.length()- PERSONAL_NUMMER_LENGTH);
		
		return ITEmployee
				.builder()
				.personalNummer(personalnummer)
				.nachname(rowSet.getColumnValue(1))
				.vorname(rowSet.getColumnValue(2))
				.fte(Double.parseDouble(rowSet.getColumnValue(3)))
				.mitarbeiterkreis(rowSet.getColumnValue(4))
				.mitarbeitergruppe(rowSet.getColumnValue(5))
				.personalbereich(rowSet.getColumnValue(6))
				.bereich(rowSet.getColumnValue(7))
				.abteilung(rowSet.getColumnValue(8))
				.ressort(rowSet.getColumnValue(9))
				.organisationseinheit(rowSet.getColumnValue(10))
				.ersteintritt(LocalDate.now())
				.vertragsart(rowSet.getColumnValue(12))
				.austritt(LocalDate.now())
				.personalbereich(rowSet.getColumnValue(14))
				.build();
	}
}
