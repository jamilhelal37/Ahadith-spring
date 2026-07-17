package com.jamil.ahadith.features.admin.controller;

import com.jamil.ahadith.features.hadith.entity.Hadith;

import com.jamil.ahadith.features.user.entity.User;

import com.jamil.ahadith.features.catalog.repository.BookRepository;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import com.jamil.ahadith.features.catalog.repository.MuhaddithRepository;
import com.jamil.ahadith.features.catalog.repository.RawiRepository;
import com.jamil.ahadith.features.catalog.repository.RulingRepository;
import com.jamil.ahadith.features.catalog.repository.TopicRepository;
import com.jamil.ahadith.features.user.repository.UserRepository;
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
