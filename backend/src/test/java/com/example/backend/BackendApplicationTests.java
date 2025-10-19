package com.example.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BackendApplicationTests {

  @Autowired ApplicationContext ctx;
  @Autowired DataSource dataSource;

  @Test
  void contextLoads() {
    assertThat(ctx).isNotNull();
  }

  @Test
  void databaseIsReachable() throws Exception {
    // comprueba que la cadena de conexión y credenciales funcionan
    try (Connection c = dataSource.getConnection()) {
      assertThat(c).isNotNull();
      assertThat(c.isValid(2)).isTrue();
    }
  }
}
