package de.smarties.fte_statistics;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.smarties.fte_statistics.mail.MailSender;

//@Component
public class Demo {

//	@Autowired
//	private MailSender sender;
	@Autowired
	private JobLauncher jobLauncher;
	@Autowired
	private Job job;
	@PostConstruct
	public void init() {
		
        JobParameters jobParameters = new JobParametersBuilder().addString("ID", UUID.randomUUID().toString()).toJobParameters();
    
        try {
            JobExecution jobExecution = jobLauncher.run(job, jobParameters);
        }
        catch (JobExecutionAlreadyRunningException e) {
            e.printStackTrace();
        }
        catch (JobRestartException e) {
            e.printStackTrace();
        }
        catch (JobInstanceAlreadyCompleteException e) {
            e.printStackTrace();
        }
        catch (JobParametersInvalidException e) {
            e.printStackTrace();
        }
    
	}
}
