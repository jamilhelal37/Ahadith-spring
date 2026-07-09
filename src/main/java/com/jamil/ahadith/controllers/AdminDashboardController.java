package com.jamil.ahadith.controllers;

import com.jamil.ahadith.repositories.BookRepository;
import com.jamil.ahadith.repositories.HadithRepository;
import com.jamil.ahadith.repositories.MuhaddithRepository;
import com.jamil.ahadith.repositories.RawiRepository;
import com.jamil.ahadith.repositories.RulingRepository;
import com.jamil.ahadith.repositories.TopicRepository;
import com.jamil.ahadith.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {
    private final HadithRepository hadithRepository;
    private final BookRepository bookRepository;
    private final RawiRepository rawiRepository;
    private final RulingRepository rulingRepository;
    private final TopicRepository topicRepository;
    private final MuhaddithRepository muhaddithRepository;
    private final UserRepository userRepository;

    @GetMapping
    public Map<String, Long> getDashboard() {
        return Map.of(
                "ahadith", hadithRepository.count(),
                "books", bookRepository.count(),
                "rawis", rawiRepository.count(),
                "rulings", rulingRepository.count(),
                "topics", topicRepository.count(),
                "muhaddiths", muhaddithRepository.count(),
                "users", userRepository.count()
        );
    }
}
