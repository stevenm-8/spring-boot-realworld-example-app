package io.spring.application.user;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

@Import({UserService.class, MyBatisUserRepository.class, BCryptPasswordEncoder.class})
@TestPropertySource(properties = {"image.default=https://static.productionready.io/images/smiley-cyrus.jpg"})
public class UserServiceTest extends DbTestBase {

  @Autowired private UserService userService;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  private RegisterParam validRegisterParam;

  @BeforeEach
  public void setUp() {
    validRegisterParam = new RegisterParam("test@example.com", "testuser", "password123");
  }

  @Test
  public void should_create_user_with_valid_data() {
    User user = userService.createUser(validRegisterParam);

    Assertions.assertNotNull(user.getId());
    Assertions.assertEquals("test@example.com", user.getEmail());
    Assertions.assertEquals("testuser", user.getUsername());
    Assertions.assertTrue(passwordEncoder.matches("password123", user.getPassword()));
    Assertions.assertEquals("", user.getBio());
    Assertions.assertEquals("https://static.productionready.io/images/smiley-cyrus.jpg", user.getImage());
  }

  @Test
  public void should_save_user_to_repository() {
    User user = userService.createUser(validRegisterParam);

    User savedUser = userRepository.findById(user.getId()).orElse(null);
    Assertions.assertNotNull(savedUser);
    Assertions.assertEquals(user.getEmail(), savedUser.getEmail());
    Assertions.assertEquals(user.getUsername(), savedUser.getUsername());
  }

  @Test
  public void should_throw_exception_for_duplicate_email() {
    userService.createUser(validRegisterParam);

    RegisterParam duplicateEmailParam = new RegisterParam("test@example.com", "differentuser", "password123");
    Assertions.assertThrows(RuntimeException.class, () -> {
      userService.createUser(duplicateEmailParam);
    });
  }

  @Test
  public void should_throw_exception_for_duplicate_username() {
    userService.createUser(validRegisterParam);

    RegisterParam duplicateUsernameParam = new RegisterParam("different@example.com", "testuser", "password123");
    Assertions.assertThrows(RuntimeException.class, () -> {
      userService.createUser(duplicateUsernameParam);
    });
  }

  @Test
  public void should_update_user_with_valid_data() {
    User user = userService.createUser(validRegisterParam);
    UpdateUserParam updateParam = UpdateUserParam.builder()
        .email("updated@example.com")
        .username("updateduser")
        .bio("Updated bio")
        .image("updated-image.jpg")
        .build();

    userService.updateUser(new UpdateUserCommand(user, updateParam));

    User updatedUser = userRepository.findById(user.getId()).orElse(null);
    Assertions.assertNotNull(updatedUser);
    Assertions.assertEquals("updated@example.com", updatedUser.getEmail());
    Assertions.assertEquals("updateduser", updatedUser.getUsername());
    Assertions.assertEquals("Updated bio", updatedUser.getBio());
    Assertions.assertEquals("updated-image.jpg", updatedUser.getImage());
  }

  @Test
  public void should_update_user_password() {
    User user = userService.createUser(validRegisterParam);
    UpdateUserParam updateParam = UpdateUserParam.builder()
        .password("newpassword123")
        .build();

    userService.updateUser(new UpdateUserCommand(user, updateParam));

    User updatedUser = userRepository.findById(user.getId()).orElse(null);
    Assertions.assertNotNull(updatedUser);
    Assertions.assertEquals("newpassword123", updatedUser.getPassword());
  }

  @Test
  public void should_not_update_with_duplicate_email() {
    User user1 = userService.createUser(validRegisterParam);
    User user2 = userService.createUser(new RegisterParam("other@example.com", "otheruser", "password123"));

    UpdateUserParam updateParam = UpdateUserParam.builder()
        .email("other@example.com")
        .build();

    Assertions.assertThrows(RuntimeException.class, () -> {
      userService.updateUser(new UpdateUserCommand(user1, updateParam));
    });
  }

  @Test
  public void should_not_update_with_duplicate_username() {
    User user1 = userService.createUser(validRegisterParam);
    User user2 = userService.createUser(new RegisterParam("other@example.com", "otheruser", "password123"));

    UpdateUserParam updateParam = UpdateUserParam.builder()
        .username("otheruser")
        .build();

    Assertions.assertThrows(RuntimeException.class, () -> {
      userService.updateUser(new UpdateUserCommand(user1, updateParam));
    });
  }

  @Test
  public void should_allow_update_with_same_email() {
    User user = userService.createUser(validRegisterParam);
    UpdateUserParam updateParam = UpdateUserParam.builder()
        .email("test@example.com")
        .bio("Updated bio")
        .build();

    userService.updateUser(new UpdateUserCommand(user, updateParam));

    User updatedUser = userRepository.findById(user.getId()).orElse(null);
    Assertions.assertNotNull(updatedUser);
    Assertions.assertEquals("test@example.com", updatedUser.getEmail());
    Assertions.assertEquals("Updated bio", updatedUser.getBio());
  }

  @Test
  public void should_allow_update_with_same_username() {
    User user = userService.createUser(validRegisterParam);
    UpdateUserParam updateParam = UpdateUserParam.builder()
        .username("testuser")
        .bio("Updated bio")
        .build();

    userService.updateUser(new UpdateUserCommand(user, updateParam));

    User updatedUser = userRepository.findById(user.getId()).orElse(null);
    Assertions.assertNotNull(updatedUser);
    Assertions.assertEquals("testuser", updatedUser.getUsername());
    Assertions.assertEquals("Updated bio", updatedUser.getBio());
  }
}
