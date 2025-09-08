package io.spring.core.comment;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class CommentTest {

  @Test
  public void should_create_comment_with_valid_data() {
    Comment comment = new Comment("This is a comment body", "user123", "article456");
    
    assertThat(comment.getId(), notNullValue());
    assertThat(comment.getBody(), is("This is a comment body"));
    assertThat(comment.getUserId(), is("user123"));
    assertThat(comment.getArticleId(), is("article456"));
    assertThat(comment.getCreatedAt(), notNullValue());
  }

  @Test
  public void should_create_comment_with_empty_body() {
    Comment comment = new Comment("", "user123", "article456");
    
    assertThat(comment.getId(), notNullValue());
    assertThat(comment.getBody(), is(""));
    assertThat(comment.getUserId(), is("user123"));
    assertThat(comment.getArticleId(), is("article456"));
    assertThat(comment.getCreatedAt(), notNullValue());
  }

  @Test
  public void should_create_comment_with_long_body() {
    String longBody = "This is a very long comment body that contains multiple sentences and should be handled properly by the Comment constructor without any issues.";
    Comment comment = new Comment(longBody, "user123", "article456");
    
    assertThat(comment.getId(), notNullValue());
    assertThat(comment.getBody(), is(longBody));
    assertThat(comment.getUserId(), is("user123"));
    assertThat(comment.getArticleId(), is("article456"));
    assertThat(comment.getCreatedAt(), notNullValue());
  }

  @Test
  public void should_generate_unique_ids_for_different_comments() {
    Comment comment1 = new Comment("First comment", "user123", "article456");
    Comment comment2 = new Comment("Second comment", "user123", "article456");
    
    assertThat(comment1.getId().equals(comment2.getId()), is(false));
  }
}
