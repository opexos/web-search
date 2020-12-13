package com.opexos.websearch.wetfileload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class JobStopResume {

    private static final int STOP_WAIT_TIMEOUT = 10000;
    private final JobOperator jobOperator;
    private final JobExplorer jobExplorer;

    @EventListener(ApplicationReadyEvent.class)
    public void resumeJobs() {
        //@PostConstruct not suitable in this situation:
        //1. the batch data schema may still not be initialized
        //2. JobRegistryBeanPostProcessor still may not initialize all the job beans

        log.info("Resume jobs");

        jobExplorer.getJobNames().forEach(jobName -> {
            log.info("Process job instances of job {}", jobName);
            jobExplorer.getJobInstances(jobName, 0, Integer.MAX_VALUE).forEach(jobInstance -> {
                JobExecution jobExecution = jobExplorer.getLastJobExecution(jobInstance);
                if (jobExecution == null) {
                    return;
                }
                Long executionId = jobExecution.getId();
                log.info("Status of last execution {} of job instance {} is {}",
                        executionId, jobInstance.getInstanceId(), jobExecution.getStatus());
                if (jobExecution.getStatus().equals(BatchStatus.STOPPED)) {
                    try {
                        log.info("Restart job execution id {}", executionId);
                        jobOperator.restart(executionId);
                    } catch (Exception e) {
                        log.error("Can't restart job execution id {}", executionId, e);
                    }
                }
            });
        });

    }

    @PreDestroy
    public void stopJobs() {
        log.info("Stop running jobs");

        List<Long> executionIds = new ArrayList<>();

        jobExplorer.getJobNames().forEach(jobName -> {
            jobExplorer.findRunningJobExecutions(jobName).forEach(execution -> {
                Long executionId = execution.getId();
                log.info("Stopping {} execution id {}", jobName, executionId);
                try {
                    //Send a stop signal to the JobExecution with the supplied id. The signal is
                    // successfully sent if this method returns true, but that doesn't mean that
                    // the job has stopped. The only way to be sure of that is to poll the job execution status.
                    jobOperator.stop(executionId);
                    executionIds.add(executionId);
                } catch (Exception e) {
                    log.error("An error occurred while stop execution id {}", executionId, e);
                }
            });
        });

        //waiting for all jobs to stop
        for (long startWait = System.currentTimeMillis();
             System.currentTimeMillis() < startWait + STOP_WAIT_TIMEOUT; ) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
            Iterator<Long> it = executionIds.iterator();
            while (it.hasNext()) {
                Long executionId = it.next();
                if (jobExplorer.getJobExecution(executionId).getStatus().equals(BatchStatus.STOPPED)) {
                    log.info("Job execution id {} was stopped", executionId);
                    it.remove();
                }
            }
            if (executionIds.isEmpty()) {
                break;
            }
        }

        if (!executionIds.isEmpty()) {
            log.warn("Job execution id {} was not stopped",
                    executionIds.stream().map(String::valueOf).collect(Collectors.joining(",")));

        }


    }

}
