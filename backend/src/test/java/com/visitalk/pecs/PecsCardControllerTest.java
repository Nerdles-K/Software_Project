package com.visitalk.pecs;

import com.visitalk.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class PecsCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    private String childToken;

    @BeforeEach
    void setUp() {
        childToken = jwtUtil.generateToken(2L, "child", "FAM001");
    }

    @Test
    void listCards_returnsSeededEatCards() throws Exception {
        mockMvc.perform(get("/api/cards")
                .param("category", "eat")
                .header("Authorization", "Bearer " + childToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(6))
            .andExpect(jsonPath("$[0].category").value("eat"))
            .andExpect(jsonPath("$[0].imageUrl").value("🍎"));
    }

    @Test
    void listCards_withoutToken_isRejected() throws Exception {
        mockMvc.perform(get("/api/cards").param("category", "eat"))
            .andExpect(status().isForbidden());
    }
}
