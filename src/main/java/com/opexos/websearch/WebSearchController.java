package com.opexos.websearch;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Controller
@Api(tags = "Web search")
public class WebSearchController {

    private final WebSearchService webSearchService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("searchForm", new SearchForm());
        return "index";
    }

    @PostMapping("/search")
    public String search(@ModelAttribute SearchForm searchForm, Model model) {
        List<String> urls = webSearchService.findPagesByText(searchForm.getText());
        model.addAttribute("searchForm", searchForm);
        model.addAttribute("urls", urls);
        return "search";
    }

    @ApiOperation("Search web pages by text")
    @GetMapping("/api/search")
    @ResponseBody
    public List<String> search(@RequestParam String text) {
        return webSearchService.findPagesByText(text);
    }

}
