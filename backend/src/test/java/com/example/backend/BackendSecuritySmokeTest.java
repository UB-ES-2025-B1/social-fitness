package com.example.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BackendSecuritySmokeTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void root_shouldRequireAuth() throws Exception {
    // sin credenciales → 401
    mockMvc.perform(get("/"))
           .andExpect(status().isUnauthorized());
  }

  @Test
  void unknownEndpoint_withAuth_shouldNotReturn401() throws Exception {
    // con credenciales básicas configuradas en test
    mockMvc.perform(get("/api/unknown")
            .header("Authorization",
                    "Basic " + java.util.Base64.getEncoder()
                      .encodeToString("dev:dev1234".getBytes())))
           // al menos no 401; lo normal será 404 porque no existe
           .andExpect(result -> {
             int sc = result.getResponse().getStatus();
             if (sc == 401) throw new AssertionError("Should NOT be 401 with auth");
           });
  }
}
