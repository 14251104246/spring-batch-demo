package com.example.demo.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

@Controller
public class JobLauncherController {

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job importUserJob;

    @RequestMapping("/jobLauncher.html")
    @ResponseBody
    public String handle() throws Exception{
        HashMap<String, JobParameter> parameters = new HashMap<>();
        parameters.put("schedule.Time",new JobParameter(String.valueOf(System.currentTimeMillis())));
        JobParameters jobParameters = new JobParameters(parameters);
        jobLauncher.run(importUserJob, jobParameters);
        return "成功执行任务";
    }
}