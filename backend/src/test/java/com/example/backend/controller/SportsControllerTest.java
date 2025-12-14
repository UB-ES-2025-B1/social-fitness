package com.example.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SportsController.class)
@AutoConfigureMockMvc(addFilters = false)
class SportsControllerTest {

  @Autowired
  MockMvc mvc;

  @Test
  void getSports_returnsSportsAndLevels() throws Exception {
    mvc.perform(get("/sports"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sports").isArray())
        .andExpect(jsonPath("$.levels").isArray())
        .andExpect(jsonPath("$.total_sports").value(10))
        .andExpect(jsonPath("$.total_levels").value(4));
  }

  @Test
  void getSportLevels_returnsLevelsOnly() throws Exception {
    mvc.perform(get("/sports/levels"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.levels").isArray())
        .andExpect(jsonPath("$.levels.length()").value(4));
  }

  @Test
  void getAvailableSports_returnsSportsOnly() throws Exception {
    mvc.perform(get("/sports/available"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sports").isArray())
        .andExpect(jsonPath("$.sports.length()").value(10));
  }
}

