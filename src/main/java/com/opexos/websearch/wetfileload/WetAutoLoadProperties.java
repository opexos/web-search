package com.opexos.websearch.wetfileload;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@Component
@ConfigurationProperties("wet-auto-load")
@Validated
public class WetAutoLoadProperties {
    /**
     * Name of the file that stores URLs to WET files.
     */
    @NotBlank
    private String fileName;

    /**
     * The interval (in milliseconds) at which to check for a file with URLs.
     */
    @Min(1000)
    private int scanFileInterval;
}
