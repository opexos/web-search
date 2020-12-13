package com.opexos.websearch.wetfileload;

import com.opexos.websearch.data.WebData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

import java.util.Arrays;


@Slf4j
public class WetItemProcessor implements ItemProcessor<String, WebData> {

    private final static String TARGET_URI = "WARC-Target-URI:";
    private final static String WARC_TYPE = "WARC-Type:";

    @Override
    public WebData process(String item) {
        String[] lines = item.split("\n");

        String warcType = Arrays.stream(lines).filter(it -> it.startsWith(WARC_TYPE)).findFirst().orElse(null);
        if (warcType == null) {
            log.error("{} header is not found. WET block: {}",
                    WARC_TYPE, item.substring(0, Math.min(item.length(), 500)));
            return null;
        }

        warcType = warcType.substring(warcType.indexOf(":") + 1).trim();
        if (!"conversion".equals(warcType)) {
            return null;
        }

        String uriHeader = Arrays.stream(lines).filter(it -> it.startsWith(TARGET_URI)).findFirst().orElse(null);
        if (uriHeader == null) {
            log.error("{} header is not found. WET block: {}",
                    TARGET_URI, item.substring(0, Math.min(item.length(), 500)));
            return null;
        }

        String url = uriHeader.substring(uriHeader.indexOf(":") + 1).trim();
        String body = item.substring(item.indexOf("\n\n") + 2).trim().replaceAll("\u0000", "");

        return WebData.builder()
                .url(url)
                .pageBody(body)
                .build();
    }
}