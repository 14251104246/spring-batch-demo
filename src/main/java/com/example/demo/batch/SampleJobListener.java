package com.example.demo.batch;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import java.util.logging.Logger;

public class SampleJobListener implements JobExecutionListener {
    @Override
    public void beforeJob(JobExecution jobExecution) {
        Logger.getLogger(SampleJobListener.class.getName()).info("beforeJob");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if( jobExecution.getStatus() == BatchStatus.COMPLETED ){
            //job success
            Logger.getLogger(SampleJobListener.class.getName()).info("afterJob:success");
        }
        else if(jobExecution.getStatus() == BatchStatus.FAILED){
            //job failure
            Logger.getLogger(SampleJobListener.class.getName()).info("afterJob:failure");
        }
    }
}
