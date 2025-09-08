package io.spring.application.article;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import java.util.Arrays;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({ArticleCommandService.class, MyBatisArticleRepository.class, MyBatisUserRepository.class})
public class ArticleCommandServiceTest extends DbTestBase {

  @Autowired private ArticleCommandService articleCommandService;
  @Autowired private ArticleRepository articleRepository;
  @Autowired private UserRepository userRepository;

  private User author;
  private NewArticleParam validNewArticleParam;

  @BeforeEach
  public void setUp() {
    author = new User("author@example.com", "author", "password", "", "");
    userRepository.save(author);

    validNewArticleParam = NewArticleParam.builder()
        .title("Test Article")
        .description("Test Description")
        .body("Test Body Content")
        .tagList(Arrays.asList("java", "spring"))
        .build();
  }

  @Test
  public void should_create_article_with_valid_data() {
    Article article = articleCommandService.createArticle(validNewArticleParam, author);

    Assertions.assertNotNull(article.getId());
    Assertions.assertEquals("Test Article", article.getTitle());
    Assertions.assertEquals("Test Description", article.getDescription());
    Assertions.assertEquals("Test Body Content", article.getBody());
    Assertions.assertEquals(author.getId(), article.getUserId());
    Assertions.assertNotNull(article.getCreatedAt());
    Assertions.assertNotNull(article.getUpdatedAt());
  }

  @Test
  public void should_save_article_to_repository() {
    Article article = articleCommandService.createArticle(validNewArticleParam, author);

    Article savedArticle = articleRepository.findById(article.getId()).orElse(null);
    Assertions.assertNotNull(savedArticle);
    Assertions.assertEquals(article.getTitle(), savedArticle.getTitle());
    Assertions.assertEquals(article.getDescription(), savedArticle.getDescription());
    Assertions.assertEquals(article.getBody(), savedArticle.getBody());
  }

  @Test
  public void should_generate_slug_for_article() {
    Article article = articleCommandService.createArticle(validNewArticleParam, author);

    Assertions.assertEquals("test-article", article.getSlug());
  }

  @Test
  public void should_throw_exception_for_duplicate_title() {
    articleCommandService.createArticle(validNewArticleParam, author);

    NewArticleParam duplicateParam = NewArticleParam.builder()
        .title("Test Article")
        .description("Different Description")
        .body("Different Body")
        .tagList(Arrays.asList("different"))
        .build();

    Assertions.assertThrows(RuntimeException.class, () -> {
      articleCommandService.createArticle(duplicateParam, author);
    });
  }

  @Test
  public void should_create_article_with_empty_tag_list() {
    NewArticleParam emptyTagParam = NewArticleParam.builder()
        .title("Empty Tag Article")
        .description("Test Description")
        .body("Test Body Content")
        .tagList(Arrays.asList())
        .build();

    Article article = articleCommandService.createArticle(emptyTagParam, author);

    Assertions.assertNotNull(article.getId());
    Assertions.assertEquals("Empty Tag Article", article.getTitle());
  }

  @Test
  public void should_create_article_with_null_tag_list() {
    NewArticleParam nullTagParam = NewArticleParam.builder()
        .title("Null Tag Article")
        .description("Test Description")
        .body("Test Body Content")
        .tagList(null)
        .build();

    Article article = articleCommandService.createArticle(nullTagParam, author);

    Assertions.assertNotNull(article.getId());
    Assertions.assertEquals("Null Tag Article", article.getTitle());
  }

  @Test
  public void should_update_article_with_valid_data() {
    Article article = articleCommandService.createArticle(validNewArticleParam, author);

    UpdateArticleParam updateParam = new UpdateArticleParam("Updated Title", "Updated Body Content", "Updated Description");

    Article updatedArticle = articleCommandService.updateArticle(article, updateParam);

    Assertions.assertEquals("Updated Title", updatedArticle.getTitle());
    Assertions.assertEquals("Updated Description", updatedArticle.getDescription());
    Assertions.assertEquals("Updated Body Content", updatedArticle.getBody());
    Assertions.assertEquals("updated-title", updatedArticle.getSlug());
  }

  @Test
  public void should_save_updated_article_to_repository() {
    Article article = articleCommandService.createArticle(validNewArticleParam, author);

    UpdateArticleParam updateParam = new UpdateArticleParam("Updated Title", "Updated Body Content", "Updated Description");

    articleCommandService.updateArticle(article, updateParam);

    Article savedArticle = articleRepository.findById(article.getId()).orElse(null);
    Assertions.assertNotNull(savedArticle);
    Assertions.assertEquals("Updated Title", savedArticle.getTitle());
    Assertions.assertEquals("Updated Description", savedArticle.getDescription());
    Assertions.assertEquals("Updated Body Content", savedArticle.getBody());
  }

  @Test
  public void should_update_article_with_partial_data() {
    Article article = articleCommandService.createArticle(validNewArticleParam, author);

    UpdateArticleParam updateParam = new UpdateArticleParam("Updated Title Only", "", "");

    Article updatedArticle = articleCommandService.updateArticle(article, updateParam);

    Assertions.assertEquals("Updated Title Only", updatedArticle.getTitle());
    Assertions.assertEquals("Test Description", updatedArticle.getDescription());
    Assertions.assertEquals("Test Body Content", updatedArticle.getBody());
  }

  @Test
  public void should_throw_exception_for_duplicate_title_on_update() {
    Article article1 = articleCommandService.createArticle(validNewArticleParam, author);

    NewArticleParam secondArticleParam = NewArticleParam.builder()
        .title("Second Article")
        .description("Second Description")
        .body("Second Body")
        .tagList(Arrays.asList("test"))
        .build();

    Article article2 = articleCommandService.createArticle(secondArticleParam, author);

    UpdateArticleParam updateParam = new UpdateArticleParam("Test Article", "", "");

    Assertions.assertThrows(RuntimeException.class, () -> {
      articleCommandService.updateArticle(article2, updateParam);
    });
  }
}
