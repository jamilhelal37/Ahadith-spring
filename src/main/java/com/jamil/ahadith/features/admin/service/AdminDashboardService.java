package com.jamil.ahadith.features.admin.service;

import com.jamil.ahadith.features.admin.dto.AdminDashboardResponseDto;
import com.jamil.ahadith.features.catalog.repository.BookRepository;
import com.jamil.ahadith.features.catalog.repository.MuhaddithRepository;
import com.jamil.ahadith.features.catalog.repository.RawiRepository;
import com.jamil.ahadith.features.catalog.repository.RulingRepository;
import com.jamil.ahadith.features.catalog.repository.TopicRepository;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import com.jamil.ahadith.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {
    private final HadithRepository hadithRepository;
    private final BookRepository bookRepository;
    private final RawiRepository rawiRepository;
    private final RulingRepository rulingRepository;
    private final TopicRepository topicRepository;
    private final MuhaddithRepository muhaddithRepository;
    private final UserRepository userRepository;

    public AdminDashboardResponseDto getDashboard() {
        return new AdminDashboardResponseDto(
                hadithRepository.count(),
                bookRepository.count(),
                rawiRepository.count(),
                rulingRepository.count(),
                topicRepository.count(),
                muhaddithRepository.count(),
                userRepository.count());
    }
}
