package com.fitlink.web.controller;


import com.fitlink.service.TmapPoiService;
import com.fitlink.web.dto.TmapPoiResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/tmap")
@RequiredArgsConstructor

public class TmapController {

    private final TmapPoiService tmapPoiService;

    @GetMapping
    public Object testPoi(@RequestParam String keyword) {
        System.out.println("keyword = " + keyword);
        TmapPoiResultDTO result = tmapPoiService.searchPoi(keyword);
        return result;
    }
}
