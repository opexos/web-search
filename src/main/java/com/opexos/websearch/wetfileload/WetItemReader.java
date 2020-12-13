package com.opexos.websearch.wetfileload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ReaderNotOpenException;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ClassUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

@Slf4j
public class WetItemReader extends AbstractItemCountingItemStreamItemReader<String> {
    private BufferedReader reader;
    private final StringBuilder stringBuilder = new StringBuilder();

    @Value("#{jobParameters['tempFileName']}")
    private String tempFileName;

    public WetItemReader() {
        setName(ClassUtils.getShortName(WetItemReader.class));
    }

    @Override
    protected void doOpen() throws Exception {
        File file = new File(tempFileName);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
        }

        InputStream inputStream = new FileInputStream(file);
        if (file.getName().endsWith(".gz")) {
            inputStream = new GZIPInputStream(inputStream);
        }
        reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    @Override
    protected String doRead() {

        if (reader == null) {
            throw new ReaderNotOpenException("Reader must be open before it can be read.");
        }

        try {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    //end of file
                    if (stringBuilder.length() == 0) {
                        return null;
                    }

                    //last data block
                    String data = stringBuilder.toString();
                    stringBuilder.setLength(0);
                    return data;
                }

                if ("WARC/1.0".equals(line) && stringBuilder.length() > 0) {
                    String data = stringBuilder.toString();
                    stringBuilder.setLength(0);
                    stringBuilder.append(line);
                    stringBuilder.append("\n");
                    return data;
                }
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }

        } catch (IOException e) {
            // Prevent IOException from recurring indefinitely
            // if client keeps catching and re-calling
            throw new RuntimeException("Unable to read from file: " + tempFileName, e);
        }
    }

    @Override
    protected void doClose() throws Exception {
        if (reader != null) {
            reader.close();
        }
    }

}
