package com.opexos.websearch.wetfileload;

import com.opexos.websearch.data.WebData;
import com.opexos.websearch.data.WebDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class JobConfiguration {
    private final WebDataRepository webDataRepository;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;


    @Bean
    @StepScope
    public WetItemReader wetItemReader() {
        return new WetItemReader();
    }

    @Bean
    @StepScope
    public WetFileDownloader wetFileDownloader() {
        return new WetFileDownloader();
    }

    @Bean
    public WetItemProcessor wetItemProcessor() {
        return new WetItemProcessor();
    }

    @Bean
    public RepositoryItemWriter<WebData> webDataWriter() {
        return new RepositoryItemWriterBuilder<WebData>()
                .repository(webDataRepository)
                .build();
    }


    @Bean
    public Job loadWetFile() {
        return jobBuilderFactory.get("loadWetFile")
                .start(downloadFile())
                .next(parseAndSaveToDb())
                .build();
    }

    @Bean
    public Step downloadFile() {
        return stepBuilderFactory.get("downloadFile")
                .tasklet(wetFileDownloader())
                .build();
    }

    @Bean
    public Step parseAndSaveToDb() {
        return stepBuilderFactory.get("parseAndSaveToDb")
                .<String, WebData>chunk(10)
                .reader(wetItemReader())
                .processor(wetItemProcessor())
                .writer(webDataWriter())
                .faultTolerant()
                .skipPolicy(new DuplicateSkipper())
                .build();
    }

}
