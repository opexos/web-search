package com.opexos.websearch.wetfileload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.dao.DuplicateKeyException;

@Slf4j
public class DuplicateSkipper implements SkipPolicy {

    @Override
    public boolean shouldSkip(Throwable exception, int skipCount) throws SkipLimitExceededException {
        if (exception instanceof DuplicateKeyException) {
            return true;
        }
        return false;
    }
}
