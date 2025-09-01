package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.user.User;
import io.spring.graphql.types.CommentPayload;
import io.spring.graphql.types.DeletionStatus;
import java.util.Arrays;
import java.util.Optional;
import org.joda.time.DateTime;
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
public class CommentMutationTest {

  @Mock private ArticleRepository articleRepository;
  @Mock private CommentRepository commentRepository;
  @Mock private CommentQueryService commentQueryService;
  @Mock private SecurityContext securityContext;
  @Mock private Authentication authentication;

  @InjectMocks private CommentMutation commentMutation;

  private User user;
  private Article article;
  private Comment comment;
  private CommentData commentData;
  private ProfileData profileData;

  @BeforeEach
  public void setUp() {
    user = new User("test@example.com", "testuser", "password", "bio", "image");
    article = new Article("Test Title", "Test Description", "Test Body", Arrays.asList("java"), user.getId());
    comment = new Comment("Test comment body", user.getId(), article.getId());
    profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
    commentData = new CommentData(comment.getId(), "Test comment body", article.getId(), DateTime.now(), DateTime.now(), profileData);
    
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(user);
  }

  @Test
  public void should_create_comment_successfully() {
    String slug = "test-article";
    String body = "Test comment body";
    
    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));
    when(commentQueryService.findById(any(String.class), eq(user))).thenReturn(Optional.of(commentData));

    DataFetcherResult<CommentPayload> result = commentMutation.createComment(slug, body);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(commentData, result.getLocalContext());
  }

  @Test
  public void should_remove_comment_successfully() {
    String slug = "test-article";
    String commentId = "comment-id";
    
    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));
    when(commentRepository.findById(eq(article.getId()), eq(commentId))).thenReturn(Optional.of(comment));

    DeletionStatus result = commentMutation.removeComment(slug, commentId);

    assertNotNull(result);
    assertTrue(result.getSuccess());
  }
}
