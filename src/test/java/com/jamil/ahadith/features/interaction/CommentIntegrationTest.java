package com.jamil.ahadith.features.interaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamil.ahadith.features.catalog.entity.Book;
import com.jamil.ahadith.features.catalog.repository.BookRepository;
import com.jamil.ahadith.features.hadith.entity.Hadith;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import com.jamil.ahadith.features.interaction.dto.request.CommentTextRequestDto;
import com.jamil.ahadith.features.interaction.entity.Comment;
import com.jamil.ahadith.features.interaction.repository.CommentRepository;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.user.repository.UserRepository;
import com.jamil.ahadith.core.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CommentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private HadithRepository hadithRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private JwtService jwtService;

    private User scholar;
    private User admin;
    private User member;
    private Hadith hadith;
    private Book book;

    @BeforeEach
    void setUp() {
        book = new Book();
        book.setName("Sahih Bukhari");
        book = bookRepository.save(book);

        hadith = new Hadith();
        hadith.setText("Hadith text for testing comments");
        hadith.setHadithNumber(1);
        hadith.setBook(book);
        hadith = hadithRepository.save(hadith);

        scholar = createUser("scholar@test.com", UserType.scholar);
        admin = createUser("admin@test.com", UserType.admin);
        member = createUser("member@test.com", UserType.member);
    }

    private User createUser(String email, UserType type) {
        User user = new User();
        user.setName(type.name() + " User");
        user.setEmail(email);
        user.setPassword("password");
        user.setType(type);
        user.setStatus(UserStatus.active);
        user.setBirthDate(LocalDate.of(2000, 1, 1));
        return userRepository.save(user);
    }

    private String tokenFor(User user) {
        return jwtService.generateAccessToken(user);
    }

    @Test
    void publicViewer_ShouldSeeComments() throws Exception {
        Comment comment = new Comment();
        comment.setText("Public comment");
        comment.setUser(scholar);
        comment.setHadith(hadith);
        commentRepository.save(comment);

        mockMvc.perform(get("/api/v1/ahadith/{hadithId}/comments", hadith.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].text").value("Public comment"))
                .andExpect(jsonPath("$.items[0].scholar.name").value(scholar.getName()))
                .andExpect(jsonPath("$.items[0].scholar.email").doesNotExist());
    }

    @Test
    void scholar_ShouldCreateComment() throws Exception {
        CommentTextRequestDto request = new CommentTextRequestDto();
        request.setText("  New scholar comment  ");

        mockMvc.perform(post("/api/v1/scholar/hadiths/{hadithId}/comments", hadith.getId())
                        .header("Authorization", "Bearer " + tokenFor(scholar))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("New scholar comment"))
                .andExpect(jsonPath("$.hadith.hadithNumber").value(hadith.getHadithNumber()));
    }

    @Test
    void scholar_ShouldViewOwnComments() throws Exception {
        Comment comment = new Comment();
        comment.setText("Scholar's own comment");
        comment.setUser(scholar);
        comment.setHadith(hadith);
        commentRepository.save(comment);

        mockMvc.perform(get("/api/v1/scholar/comments")
                        .header("Authorization", "Bearer " + tokenFor(scholar)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].text").value("Scholar's own comment"))
                .andExpect(jsonPath("$.items[0].hadith.text").value(hadith.getText()));
    }

    @Test
    void admin_ShouldViewAllComments() throws Exception {
        Comment comment = new Comment();
        comment.setText("Some comment");
        comment.setUser(scholar);
        comment.setHadith(hadith);
        commentRepository.save(comment);

        mockMvc.perform(get("/api/v1/admin/comments")
                        .header("Authorization", "Bearer " + tokenFor(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].text").value("Some comment"))
                .andExpect(jsonPath("$.items[0].scholar.email").value(scholar.getEmail()));
    }

    @Test
    void member_ShouldNotCreateComment() throws Exception {
        CommentTextRequestDto request = new CommentTextRequestDto();
        request.setText("Member trying to comment");

        mockMvc.perform(post("/api/v1/scholar/hadiths/{hadithId}/comments", hadith.getId())
                        .header("Authorization", "Bearer " + tokenFor(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void scholar_ShouldUpdateOwnComment() throws Exception {
        Comment comment = new Comment();
        comment.setText("Original text");
        comment.setUser(scholar);
        comment.setHadith(hadith);
        comment = commentRepository.save(comment);

        CommentTextRequestDto request = new CommentTextRequestDto();
        request.setText("Updated text");

        mockMvc.perform(put("/api/v1/scholar/comments/{commentId}", comment.getId())
                        .header("Authorization", "Bearer " + tokenFor(scholar))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated text"));
    }

    @Test
    void scholar_ShouldNotUpdateOthersComment() throws Exception {
        User otherScholar = createUser("other@scholar.com", UserType.scholar);
        Comment comment = new Comment();
        comment.setText("Other's comment");
        comment.setUser(otherScholar);
        comment.setHadith(hadith);
        comment = commentRepository.save(comment);

        CommentTextRequestDto request = new CommentTextRequestDto();
        request.setText("Trying to update");

        mockMvc.perform(put("/api/v1/scholar/comments/{commentId}", comment.getId())
                        .header("Authorization", "Bearer " + tokenFor(scholar))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void admin_ShouldDeleteAnyComment() throws Exception {
        Comment comment = new Comment();
        comment.setText("Comment to delete");
        comment.setUser(scholar);
        comment.setHadith(hadith);
        comment = commentRepository.save(comment);

        mockMvc.perform(delete("/api/v1/admin/comments/{commentId}", comment.getId())
                        .header("Authorization", "Bearer " + tokenFor(admin)))
                .andExpect(status().isNoContent());

        assert !commentRepository.existsById(comment.getId());
    }
}
