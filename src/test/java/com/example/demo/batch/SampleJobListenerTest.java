package com.example.demo.batch;

import org.junit.Test;

import java.util.logging.Logger;

import static org.junit.Assert.*;

public class SampleJobListenerTest {

    @Test
    public void beforeJob() {
        Logger.getLogger(SampleJobListener.class.getName()).info("beforeJob");
    }

    @Test
    public void afterJob() {
    }
}