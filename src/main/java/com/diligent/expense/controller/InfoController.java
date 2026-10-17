package com.diligent.expense.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Public endpoints used for health checks and as a friendly landing
 * response when the API root is opened in a browser.
 */
@RestController
public class InfoController {

    @GetMapping("/")
    public Map<String, Object> root() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", "Smart Expense Tracker API");
        info.put("status", "running");
        info.put("docs", "/swagger-ui.html");
        return info;
    }

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
