package com.example;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
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
    void signupReturnsAddUserPage() throws Exception {

        this.mockMvc
                .perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-user"));
    }

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
    void savingInvalidUserReturnsAddUserPage() throws Exception {
        String blank = "  ";
        this.mockMvc
                .perform(post("/adduser")
                        .with(csrf())
                        .param("name", "Bob")
                        .param("email", blank))
                .andExpect(status().isOk())
                .andExpect(view().name("add-user"));
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
        newUser.setPhone("111");
        userRepository.save(newUser);

        this.mockMvc
                .perform(post("/update/{id}", newUser.getId())
                        .with(csrf())
                        .param("name", "Alice")
                        .param("email", newUser.getEmail())
                        .param("phone", "222")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));

        User storedUser = userRepository.findByEmail(newUser.getEmail()).orElseThrow();
        assertThat(storedUser.getName()).as("new name").isEqualTo("Alice");
        assertThat(storedUser.getPhone()).as("new phone").isEqualTo("222");
    }

    @Test
    void updatingUserWithInvalidEmailReturnsUpdateUserPage() throws Exception {
        User user = user();
        user.setEmail(uniqueEmail());
        user.setName("Bob");
        userRepository.save(user);
        String blank = "  ";

        this.mockMvc
                .perform(post("/update/{id}", user.getId())
                        .with(csrf())
                        .param("name", "Alice")
                        .param("email", blank))
                .andExpect(status().isOk())
                .andExpect(view().name("update-user"));
    }

    @Test
    void deletesUserAndRedirectsToIndexPage() throws Exception {
        User user = user();
        user.setEmail(uniqueEmail());
        user = userRepository.save(user);

        this.mockMvc
                .perform(get("/delete/{id}", user.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));

        assertThat(
                userRepository.findByEmail(user.getEmail())).isEmpty();
    }

    @Test
    void deletesByNotExistingIdWillThrow() {
        assertThatThrownBy(() -> this.mockMvc
                .perform(get("/delete/{id}", Long.MAX_VALUE))
                .andExpect(status().is4xxClientError())).hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getUpdateFormWithNonExistingUserIdWillThrow() {
        assertThatThrownBy(() -> this.mockMvc
                .perform(get("/edit/{id}", Long.MAX_VALUE))
                .andExpect(status().is4xxClientError())).hasCauseInstanceOf(IllegalArgumentException.class);
    }

    private String uniqueEmail() {
        return "%s@email.com".formatted(UUID.randomUUID());
    }

    private User user() {
        User user = new User();
        user.setEmail(email());
        user.setName(name());
        user.setPhone(phone());
        return user;
    }

    private String phone() {
        return "123123";
    }

    private String name() {
        return "John Snow";
    }

    private String email() {
        return "user@email.com";
    }
}