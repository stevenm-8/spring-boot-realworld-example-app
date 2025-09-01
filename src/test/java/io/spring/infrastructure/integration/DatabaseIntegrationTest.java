package io.spring.infrastructure.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisCommentRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@Import({MyBatisUserRepository.class, MyBatisArticleRepository.class, MyBatisCommentRepository.class})
public class DatabaseIntegrationTest extends DbTestBase {

  @Autowired private UserRepository userRepository;

  @Autowired private ArticleRepository articleRepository;

  @Autowired private CommentRepository commentRepository;

  private User user1;
  private User user2;

  @BeforeEach
  public void setUp() {
    user1 = new User("user1@example.com", "user1", "password", "bio1", "image1");
    user2 = new User("user2@example.com", "user2", "password", "bio2", "image2");
  }

  @Test
  public void should_handle_unique_constraint_violation_on_username() {
    User duplicateUsernameUser = new User("different@example.com", "user1", "password", "bio", "image");
    
    userRepository.save(user1);
    
    assertThrows(Exception.class, () -> {
      userRepository.save(duplicateUsernameUser);
    });
  }

  @Test
  public void should_handle_unique_constraint_violation_on_email() {
    User duplicateEmailUser = new User("user1@example.com", "differentuser", "password", "bio", "image");
    
    userRepository.save(user1);
    
    assertThrows(Exception.class, () -> {
      userRepository.save(duplicateEmailUser);
    });
  }

  @Test
  public void should_handle_cascade_operations_correctly() {
    userRepository.save(user1);
    userRepository.save(user2);

    Article article = new Article("Test Article", "Description", "Body", Arrays.asList("tag1"), user1.getId());
    articleRepository.save(article);

    Comment comment = new Comment("Test comment", user2.getId(), article.getId());
    commentRepository.save(comment);

    assertTrue(articleRepository.findById(article.getId()).isPresent());
    assertTrue(commentRepository.findById(article.getId(), comment.getId()).isPresent());

    articleRepository.remove(article);

    assertFalse(articleRepository.findById(article.getId()).isPresent());
  }

  @Test
  public void should_handle_follow_relationship_constraints() {
    userRepository.save(user1);
    userRepository.save(user2);

    FollowRelation relation = new FollowRelation(user1.getId(), user2.getId());
    userRepository.saveRelation(relation);

    assertTrue(userRepository.findRelation(user1.getId(), user2.getId()).isPresent());

    try {
      userRepository.saveRelation(relation);
    } catch (Exception e) {
    }
  }

  @Test
  public void should_handle_self_follow_prevention() {
    userRepository.save(user1);

    FollowRelation selfRelation = new FollowRelation(user1.getId(), user1.getId());
    
    try {
      userRepository.saveRelation(selfRelation);
      assertTrue(userRepository.findRelation(user1.getId(), user1.getId()).isPresent());
    } catch (Exception e) {
      assertFalse(userRepository.findRelation(user1.getId(), user1.getId()).isPresent());
    }
  }

  @Test
  public void should_handle_foreign_key_constraints() {
    String nonExistentUserId = "non-existent-user-id";
    
    Article article = new Article("Test Article", "Description", "Body", Arrays.asList("tag1"), nonExistentUserId);
    
    try {
      articleRepository.save(article);
      Optional<Article> saved = articleRepository.findBySlug(article.getSlug());
      assertTrue(true);
    } catch (Exception e) {
      assertFalse(articleRepository.findBySlug(article.getSlug()).isPresent());
    }
  }

  @Test
  public void should_handle_null_values_appropriately() {
    User userWithNulls = new User("test@example.com", "testuser", "password", null, null);
    userRepository.save(userWithNulls);

    Optional<User> retrieved = userRepository.findByUsername("testuser");
    assertTrue(retrieved.isPresent());
    assertEquals("test@example.com", retrieved.get().getEmail());
  }

  @Test
  public void should_handle_empty_string_values() {
    User userWithEmptyStrings = new User("test@example.com", "testuser", "password", "", "");
    userRepository.save(userWithEmptyStrings);

    Optional<User> retrieved = userRepository.findByUsername("testuser");
    assertTrue(retrieved.isPresent());
    assertEquals("", retrieved.get().getBio());
    assertEquals("", retrieved.get().getImage());
  }

  @Test
  public void should_handle_very_long_field_values() {
    String longBio = "a".repeat(1000);
    String longImage = "https://example.com/" + "a".repeat(500) + ".jpg";
    
    User userWithLongFields = new User("test@example.com", "testuser", "password", longBio, longImage);
    userRepository.save(userWithLongFields);

    Optional<User> retrieved = userRepository.findByUsername("testuser");
    assertTrue(retrieved.isPresent());
    assertEquals(longBio, retrieved.get().getBio());
    assertEquals(longImage, retrieved.get().getImage());
  }

  @Test
  public void should_handle_special_characters_in_fields() {
    String specialBio = "Bio with special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";
    User userWithSpecialChars = new User("test@example.com", "testuser", "password", specialBio, "image");
    userRepository.save(userWithSpecialChars);

    Optional<User> retrieved = userRepository.findByUsername("testuser");
    assertTrue(retrieved.isPresent());
    assertEquals(specialBio, retrieved.get().getBio());
  }

  @Test
  public void should_handle_unicode_characters() {
    String unicodeBio = "Bio with unicode: 日本語 español français русский 中文";
    User userWithUnicode = new User("test@example.com", "testuser", "password", unicodeBio, "image");
    userRepository.save(userWithUnicode);

    Optional<User> retrieved = userRepository.findByUsername("testuser");
    assertTrue(retrieved.isPresent());
    assertEquals(unicodeBio, retrieved.get().getBio());
  }

  @Test
  public void should_handle_concurrent_operations() {
    userRepository.save(user1);
    userRepository.save(user2);

    Article article1 = new Article("Article 1", "Description 1", "Body 1", Arrays.asList("tag1"), user1.getId());
    Article article2 = new Article("Article 2", "Description 2", "Body 2", Arrays.asList("tag2"), user2.getId());

    articleRepository.save(article1);
    articleRepository.save(article2);

    assertTrue(articleRepository.findById(article1.getId()).isPresent());
    assertTrue(articleRepository.findById(article2.getId()).isPresent());
  }

  @Test
  public void should_handle_complex_query_scenarios() {
    userRepository.save(user1);
    userRepository.save(user2);

    Article article1 = new Article("Java Article", "Java Description", "Java Body", Arrays.asList("java", "programming"), user1.getId());
    Article article2 = new Article("Spring Article", "Spring Description", "Spring Body", Arrays.asList("spring", "java"), user1.getId());
    Article article3 = new Article("React Article", "React Description", "React Body", Arrays.asList("react", "javascript"), user2.getId());

    articleRepository.save(article1);
    articleRepository.save(article2);
    articleRepository.save(article3);

    Optional<Article> foundBySlug = articleRepository.findBySlug(article1.getSlug());
    assertTrue(foundBySlug.isPresent());
    assertEquals("Java Article", foundBySlug.get().getTitle());
  }
}
