package io.spring.api.security;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.CurrentUserApi;
import io.spring.application.UserQueryService;
import io.spring.application.user.UserService;
import io.spring.core.service.JwtService;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CurrentUserApi.class)
@Import({
  WebSecurityConfig.class,
  JacksonCustomizations.class,
  UserService.class,
  ValidationAutoConfiguration.class,
  BCryptPasswordEncoder.class
})
public class SecurityIntegrationTest {

  @Autowired private MockMvc mvc;

  @MockBean private UserRepository userRepository;

  @MockBean private JwtService jwtService;

  @MockBean private UserReadService userReadService;

  @MockBean private UserQueryService userQueryService;

  @BeforeEach
  public void setUp() throws Exception {
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_reject_expired_jwt_token() throws Exception {
    String expiredToken = "expired.jwt.token";
    when(jwtService.getSubFromToken(eq(expiredToken))).thenReturn(Optional.empty());

    given()
        .header("Authorization", "Token " + expiredToken)
        .when()
        .get("/user")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_reject_malformed_jwt_token() throws Exception {
    String malformedToken = "malformed-token";
    when(jwtService.getSubFromToken(eq(malformedToken))).thenReturn(Optional.empty());

    given()
        .header("Authorization", "Token " + malformedToken)
        .when()
        .get("/user")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_reject_missing_bearer_prefix() throws Exception {
    given()
        .header("Authorization", "invalid-format")
        .when()
        .get("/user")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_reject_empty_authorization_header() throws Exception {
    given()
        .header("Authorization", "")
        .when()
        .get("/user")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_reject_null_token_after_bearer() throws Exception {
    given()
        .header("Authorization", "Token ")
        .when()
        .get("/user")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_reject_token_with_invalid_format() throws Exception {
    given()
        .header("Authorization", "Bearer invalid-token")
        .when()
        .get("/user")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_reject_request_without_authorization_header() throws Exception {
    given()
        .when()
        .get("/user")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_handle_token_with_special_characters() throws Exception {
    String specialToken = "token-with-special!@#$%^&*()characters";
    when(jwtService.getSubFromToken(eq(specialToken))).thenReturn(Optional.empty());

    given()
        .header("Authorization", "Token " + specialToken)
        .when()
        .get("/user")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_handle_very_long_token() throws Exception {
    String longToken = "a".repeat(1000);
    when(jwtService.getSubFromToken(eq(longToken))).thenReturn(Optional.empty());

    given()
        .header("Authorization", "Token " + longToken)
        .when()
        .get("/user")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_handle_token_with_unicode_characters() throws Exception {
    String unicodeToken = "token-with-unicode-日本語-español";
    when(jwtService.getSubFromToken(eq(unicodeToken))).thenReturn(Optional.empty());

    given()
        .header("Authorization", "Token " + unicodeToken)
        .when()
        .get("/user")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_handle_multiple_authorization_headers() throws Exception {
    given()
        .header("Authorization", "Token valid-token")
        .header("Authorization", "Token another-token")
        .when()
        .get("/user")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_handle_case_sensitive_bearer_prefix() throws Exception {
    given()
        .header("Authorization", "token valid-token")
        .when()
        .get("/user")
        .then()
        .statusCode(401);

    given()
        .header("Authorization", "TOKEN valid-token")
        .when()
        .get("/user")
        .then()
        .statusCode(401);
  }
}
