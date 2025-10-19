package com.example.backend;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BackendSecuritySmokeTest {

  @Autowired MockMvc mvc;

  @Test
  void root_shouldReturn404_whenNoMappingExists() throws Exception {
    mvc.perform(get("/"))
       .andExpect(status().isNotFound());
  }

  @Disabled("Activar cuando haya un GET protegido (p.ej. /me) y se endurezca Security")
  @Test
  void protectedEndpoint_shouldReturn401_withoutAuth() throws Exception {
    mvc.perform(get("/me"))
       .andExpect(status().isUnauthorized());
  }
}
