package com.example;

import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration-test")
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "user")
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Test
    void indexPageHasExistingUsers() throws Exception {
        User user = userRepository.save(user());

        this.mockMvc
                .perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("users", hasItem(user)));
    }

    @Test
    void savesNewUserAndRedirectsToIndexPage() throws Exception {
        User newUser = user();
        newUser.setName(UUID.randomUUID().toString());

        this.mockMvc
                .perform(post("/adduser")
                        .with(csrf())
                        .param("name", newUser.getName())
                        .param("email", newUser.getEmail()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));

        assertThat(
                userRepository.findByNameAndEmail(newUser.getName(), newUser.getEmail()))
                .isPresent();
    }

    @Test
    void returnsUpdateUserPageWithUserFoundById() throws Exception {

        User user = userRepository.save(user());

        this.mockMvc
                .perform(get("/edit/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("update-user"))
                .andExpect(model().attribute("user", is(user)));
    }

    @Test
    void updatesUserAndRedirectsToIndexPage() throws Exception {
        User newUser = user();
        newUser.setEmail(uniqueEmail());
        newUser.setName("Bob");

        this.mockMvc
                .perform(post("/update/{id}", newUser.getId())
                        .with(csrf())
                        .param("name", "Alice")
                        .param("email", newUser.getEmail()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));

        assertThat(
                userRepository.findByEmail(newUser.getEmail()).orElseThrow().getName())
                .as("new name")
                .isEqualTo("Alice");
    }

    private String uniqueEmail() {
        return "%s@email.com".formatted(UUID.randomUUID());
    }

    private User user() {
        User user = new User();
        user.setEmail(email());
        user.setName(name());
        return user;
    }

    private String name() {
        return "John Snow";
    }

    private String email() {
        return "user@email.com";
    }
}