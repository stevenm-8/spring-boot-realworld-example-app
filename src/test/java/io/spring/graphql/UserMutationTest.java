package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import io.spring.application.user.RegisterParam;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.UserResult;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private UserService userService;

  @InjectMocks private UserMutation userMutation;

  private User user;
  private CreateUserInput createUserInput;

  @BeforeEach
  public void setUp() {
    user = new User("test@example.com", "testuser", "password", "bio", "image");
    createUserInput = CreateUserInput.newBuilder()
        .email("test@example.com")
        .username("testuser")
        .password("password")
        .build();
  }

  @Test
  public void should_create_user_successfully() {
    when(userService.createUser(any(RegisterParam.class))).thenReturn(user);

    DataFetcherResult<UserResult> result = userMutation.createUser(createUserInput);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(user, result.getLocalContext());
  }

  @Test
  public void should_handle_validation_errors_on_create_user() {
    ConstraintViolationException exception = new ConstraintViolationException("Validation failed", java.util.Collections.emptySet());
    when(userService.createUser(any(RegisterParam.class))).thenThrow(exception);

    DataFetcherResult<UserResult> result = userMutation.createUser(createUserInput);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  public void should_login_successfully() {
    when(userRepository.findByEmail(eq("test@example.com"))).thenReturn(java.util.Optional.of(user));
    when(passwordEncoder.matches(eq("password"), eq(user.getPassword()))).thenReturn(true);

    var result = userMutation.login("password", "test@example.com");

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(user, result.getLocalContext());
  }

  @Test
  public void should_fail_login_with_invalid_credentials() {
    when(userRepository.findByEmail(eq("test@example.com"))).thenReturn(java.util.Optional.of(user));
    when(passwordEncoder.matches(eq("wrongpassword"), eq(user.getPassword()))).thenReturn(false);

    assertThrows(io.spring.api.exception.InvalidAuthenticationException.class, () -> {
      userMutation.login("wrongpassword", "test@example.com");
    });
  }

  @Test
  public void should_fail_login_with_non_existent_user() {
    when(userRepository.findByEmail(eq("nonexistent@example.com"))).thenReturn(java.util.Optional.empty());

    assertThrows(io.spring.api.exception.InvalidAuthenticationException.class, () -> {
      userMutation.login("password", "nonexistent@example.com");
    });
  }
}
