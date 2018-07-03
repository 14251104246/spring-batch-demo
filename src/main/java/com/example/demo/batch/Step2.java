package com.example.demo.batch;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.logging.Logger;

public class Step2 implements Tasklet {
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Logger.getLogger(Step2.class.getName()).info("Tasklet: Step2");
        return RepeatStatus.FINISHED;
    }
}
