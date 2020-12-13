package com.opexos.websearch.wetfileload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

@Slf4j
@RequiredArgsConstructor
@Component
public class AutoLoadWetFile {

    private final WetAutoLoadProperties properties;
    private final JobLauncher jobLauncher;
    private final Job loadWetFileJob;

    @Scheduled(fixedDelayString = "#{@wetAutoLoadProperties.scanFileInterval}")
    public void execute() throws Exception {

        File file = new File(properties.getFileName());
        if (!file.exists()) {
            log.debug("File {} doesn't exist. Nothing to load.", file.getAbsolutePath());
            return;
        }

        log.info("File {} exist. Creating jobs.", file.getAbsolutePath());
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            for (String url; (url = br.readLine()) != null; ) {
                url = url.trim();
                if (url.isEmpty()) {
                    continue;
                }
                log.info("Creating a task to load a file at {}", url);
                startJob(url);
            }
        }

        if (file.delete()) {
            log.info("File {} processed and deleted", file.getAbsolutePath());
        } else {
            log.warn("File {} processed but NOT deleted (can't delete)", file.getAbsolutePath());
        }
    }

    public void startJob(String url) {
        try {
            JobExecution jobExecution = jobLauncher.run(loadWetFileJob,
                    new JobParametersBuilder()
                            .addString("url", url)
                            .addString("tempFileName", url.substring(url.lastIndexOf("/") + 1), false)
                            .toJobParameters());
            log.info("Started new job execution {} URL {}", jobExecution.getJobId(), url);
        } catch (JobExecutionAlreadyRunningException e) {
            log.info("Already running. URL {}", url);
        } catch (JobInstanceAlreadyCompleteException e) {
            log.info("Already completed. URL {}", url);
        } catch (JobRestartException e) {
            log.error("Restart exception. URL {}", url, e);
        } catch (JobParametersInvalidException e) {
            log.error("Invalid parameters. URL {}", url, e);
        }
    }

}
