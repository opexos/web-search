package com.opexos.websearch.wetfileload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
public class WetFileDownloader implements StoppableTasklet {
    @Value("#{jobParameters['url']}")
    private String url;

    @Value("#{jobParameters['tempFileName']}")
    private String tempFileName;

    private boolean stopRequested = false;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("Downloading {}", url);

        URL url = new URL(this.url);
        File tempFile = new File(tempFileName);
        long localFileSize = 0;
        boolean append = false;

        if (tempFile.exists()) {
            localFileSize = tempFile.length();
            append = true;
            log.info("File {} already exist. Size: {} Resume download.", tempFile, localFileSize);
        }

        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setRequestMethod("HEAD");
        long remoteFileSize = httpConnection.getContentLengthLong();
        log.info("Remote file size: {}", remoteFileSize);

        if (localFileSize == remoteFileSize) {
            log.info("File {} already downloaded", tempFile);
            return RepeatStatus.FINISHED;
        }

        httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setRequestProperty("Range", "bytes=" + localFileSize + "-" + remoteFileSize);


        try (BufferedInputStream in = new BufferedInputStream(httpConnection.getInputStream());
             BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile, append))) {
            byte[] buffer = new byte[4096];
            for (int bytesRead; (bytesRead = in.read(buffer)) != -1 && !stopRequested; ) {
                out.write(buffer, 0, bytesRead);
            }
        }

        return RepeatStatus.FINISHED;
    }

    @Override
    public void stop() {
        stopRequested = true;
    }
}
