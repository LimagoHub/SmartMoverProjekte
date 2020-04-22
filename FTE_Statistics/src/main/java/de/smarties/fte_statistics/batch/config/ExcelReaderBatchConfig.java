package de.smarties.fte_statistics.batch.config;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import de.smarties.fte_statistics.batch.entities.ITEmployee;
import de.smarties.fte_statistics.batch.readers.EmployeeItemReader;

@Configuration
@EnableBatchProcessing
public class ExcelReaderBatchConfig {

	@Autowired
	private EmployeeItemReader employeeItemReader;

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

//	@Value("classpath:*.csv")
//	private Resource[] inputResources;

	@Bean
	public Step step1() {
		return stepBuilderFactory.get("step1").<ITEmployee, ITEmployee>chunk(2)
				//.listener(new MyListener())
				.reader(employeeItemReader)
				.faultTolerant().skipPolicy(createSkipPolicy())
				.processor(employeeItemProcessor())
				.writer(employeeItemWriter()).build();
	}

	@Bean
	public Job listEmployeesJob(Step step1) throws Exception {
		return jobBuilderFactory.get("listEmployeesJob").incrementer(new RunIdIncrementer())

				.start(step1).build();
	}

//	@Bean
//    public MultiResourceItemReader<Employee> multiResourceItemReader() 
//    {
//        MultiResourceItemReader<Employee> resourceItemReader = new MultiResourceItemReader<Employee>() ;
//        resourceItemReader.setResources( inputResources);
//        resourceItemReader.setDelegate(employeeItemReader);
//        
//        return resourceItemReader;
//    }
//


	@Bean
	ItemProcessor<ITEmployee, ITEmployee> employeeItemProcessor() {
		return new ItemProcessor<ITEmployee, ITEmployee>() {
			@Override
			public ITEmployee process(ITEmployee employee) throws Exception {
					return employee;
			}
		};
	}

	@Bean
	ItemWriter<ITEmployee> employeeItemWriter() {
		return new ItemWriter<ITEmployee>() {
			@Override
			public void write(List<? extends ITEmployee> employeesList) throws Exception {
				for (ITEmployee employee : employeesList) {
					System.out.println(employee);
				}
			}
		};
	}

	@Bean
	SkipPolicy createSkipPolicy() {

		return new SkipPolicy() {
			private final Logger logger = LoggerFactory.getLogger("badRecordLogger");

			
			
			@Override
			public boolean shouldSkip(Throwable exception, int skipCount) throws SkipLimitExceededException {
				if (exception instanceof FileNotFoundException) {
					return false;
				} else if (exception instanceof FlatFileParseException && skipCount <= 5) {
					FlatFileParseException ffpe = (FlatFileParseException) exception;
					
					StringBuilder errorMessage = new StringBuilder();
					errorMessage.append("An error occured while processing the ");
					errorMessage.append(ffpe.getLineNumber());
					errorMessage.append(" line of the file '");
					
					Pattern pattern = Pattern.compile(".*(\\W\\w+\\.csv).*");
					Matcher matcher = pattern.matcher(ffpe.toString());
					if(matcher.matches())
						errorMessage.append(matcher.group(1));
					else
						errorMessage.append("unknown");
					errorMessage.append("'. Below was the faulty input.");
					errorMessage.append("\n");
					errorMessage.append(ffpe.getInput());
					errorMessage.append("\n");
					logger.error("{}", errorMessage.toString());
					return true;
				} else {
					return false;
				}
			}

		};
	}
}