package com.example.progettoenterprise.controllers;

import com.example.progettoenterprise.data.entities.Tag;
import com.example.progettoenterprise.data.repositories.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tag")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Slf4j
public class TagController {

    private final TagRepository tagRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getAllTag() {
        List<String> tags = tagRepository.findAll().stream()
                .map(Tag::getNomeTag)
                .sorted()
                .collect(Collectors.toList());
        return ResponseEntity.ok(tags);
    }
}
