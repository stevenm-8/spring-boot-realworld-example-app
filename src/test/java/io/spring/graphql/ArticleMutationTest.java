package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.NewArticleParam;
import io.spring.application.article.UpdateArticleParam;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.User;
import io.spring.graphql.types.CreateArticleInput;
import io.spring.graphql.types.UpdateArticleInput;
import io.spring.graphql.types.ArticlePayload;
import io.spring.graphql.types.DeletionStatus;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ArticleMutationTest {

  @Mock private ArticleCommandService articleCommandService;
  @Mock private ArticleRepository articleRepository;
  @Mock private ArticleFavoriteRepository articleFavoriteRepository;
  @Mock private SecurityContext securityContext;
  @Mock private Authentication authentication;

  @InjectMocks private ArticleMutation articleMutation;

  private User user;
  private Article article;
  private CreateArticleInput createArticleInput;
  private UpdateArticleInput updateArticleInput;

  @BeforeEach
  public void setUp() {
    user = new User("test@example.com", "testuser", "password", "bio", "image");
    article = new Article("Test Title", "Test Description", "Test Body", Arrays.asList("java", "spring"), user.getId());
    
    createArticleInput = CreateArticleInput.newBuilder()
        .title("Test Title")
        .description("Test Description")
        .body("Test Body")
        .tagList(Arrays.asList("java", "spring"))
        .build();
        
    updateArticleInput = UpdateArticleInput.newBuilder()
        .title("Updated Title")
        .description("Updated Description")
        .body("Updated Body")
        .build();
        
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(user);
  }

  @Test
  public void should_create_article_successfully() {
    when(articleCommandService.createArticle(any(NewArticleParam.class), eq(user)))
        .thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(createArticleInput);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(article, result.getLocalContext());
  }

  @Test
  public void should_update_article_successfully() {
    String slug = "test-title";
    Article updatedArticle = new Article("Updated Title", "Updated Description", "Updated Body", Arrays.asList("java"), user.getId());
    
    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(eq(article), any(UpdateArticleParam.class)))
        .thenReturn(updatedArticle);

    DataFetcherResult<ArticlePayload> result = articleMutation.updateArticle(slug, updateArticleInput);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(updatedArticle, result.getLocalContext());
  }

  @Test
  public void should_favorite_article_successfully() {
    when(articleRepository.findBySlug(eq("test-slug"))).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle("test-slug");

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(article, result.getLocalContext());
  }

  @Test
  public void should_unfavorite_article_successfully() {
    when(articleRepository.findBySlug(eq("test-slug"))).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("test-slug");

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(article, result.getLocalContext());
  }

  @Test
  public void should_delete_article_successfully() {
    when(articleRepository.findBySlug(eq("test-slug"))).thenReturn(Optional.of(article));

    DeletionStatus result = articleMutation.deleteArticle("test-slug");

    assertNotNull(result);
    assertTrue(result.getSuccess());
  }
}
