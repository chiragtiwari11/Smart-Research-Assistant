package com.Research.Smart_Research_Assistant;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/research")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class ResearchController {

    private final ResearchService researchService;

    @PostMapping("/process")
    public ResponseEntity<String> processcontent(
            @RequestBody ResearchRequest request) {

        String result = researchService.processcontent(request);

        return ResponseEntity.ok(result);
    }
}