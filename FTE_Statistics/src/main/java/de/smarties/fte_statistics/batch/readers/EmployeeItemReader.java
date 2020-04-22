package de.smarties.fte_statistics.batch.readers;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.excel.RowMapper;
import org.springframework.batch.item.excel.poi.PoiItemReader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import de.smarties.fte_statistics.batch.entities.ITEmployee;

@Component
@StepScope
public class EmployeeItemReader extends PoiItemReader<ITEmployee> {

	public EmployeeItemReader() {
	
		
		setLinesToSkip(1);
        setResource(new FileSystemResource("src/main/resources/FTE_Report_zum_Januar2020_Bereich_IT_Anonymisiert.xls"));
        setRowMapper(excelRowMapper());
        
	}

	 private RowMapper<ITEmployee> excelRowMapper() {
	        return new EmployeeExcelRowMapper();
	 }

}
//public class EmployeeItemReader extends FlatFileItemReader<Employee> {
//
//	public EmployeeItemReader() {
//	
//		
//		setResource(new FileSystemResource("src/main/resources/employees.xls"));
//		DefaultLineMapper<Employee> defaultLineMapper = new DefaultLineMapper<>();
//		DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
//		delimitedLineTokenizer.setNames(new String[] { "firstName", "lastName", "age", "salary" });
//
//		BeanWrapperFieldSetMapper<Employee> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
//		fieldSetMapper.setTargetType(Employee.class);
//
//		defaultLineMapper.setLineTokenizer(delimitedLineTokenizer);
//		defaultLineMapper.setFieldSetMapper(fieldSetMapper);
//		setLineMapper(defaultLineMapper);
//
//	}
//
//	
//
//}
