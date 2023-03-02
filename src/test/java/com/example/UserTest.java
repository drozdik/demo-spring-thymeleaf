package com.example;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserTest {

    @Test
    void testEquals() {
        User a = new User();
        a.setName("John");
        a.setEmail("john@email.com");
        a.setId(1L);

        User b = new User();
        b.setName("John");
        b.setEmail("john@email.com");
        b.setId(1L);

        assertThat(a).isEqualTo(b);

    }
}
