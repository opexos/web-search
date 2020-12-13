package com.opexos.websearch;

import com.opexos.websearch.data.WebDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class WebSearchService {

    private final WebDataRepository webDataRepository;

    public List<String> findPagesByText(String text) {
        log.info("Search pages by text: {}", text);
        List<String> result = webDataRepository.findUrlsByPageBodyContains(text);
        log.info("Found {} pages", result.size());
        return result;
    }

}
