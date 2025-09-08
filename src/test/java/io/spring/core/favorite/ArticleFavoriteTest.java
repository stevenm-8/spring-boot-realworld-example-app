package io.spring.core.favorite;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteTest {

  @Test
  public void should_create_article_favorite_with_valid_data() {
    ArticleFavorite favorite = new ArticleFavorite("article123", "user456");
    
    assertThat(favorite.getArticleId(), is("article123"));
    assertThat(favorite.getUserId(), is("user456"));
  }

  @Test
  public void should_create_article_favorite_with_empty_strings() {
    ArticleFavorite favorite = new ArticleFavorite("", "");
    
    assertThat(favorite.getArticleId(), is(""));
    assertThat(favorite.getUserId(), is(""));
  }

  @Test
  public void should_create_article_favorite_with_null_values() {
    ArticleFavorite favorite = new ArticleFavorite(null, null);
    
    assertThat(favorite.getArticleId(), is((String) null));
    assertThat(favorite.getUserId(), is((String) null));
  }

  @Test
  public void should_be_equal_when_article_and_user_are_same() {
    ArticleFavorite favorite1 = new ArticleFavorite("article123", "user456");
    ArticleFavorite favorite2 = new ArticleFavorite("article123", "user456");
    
    assertThat(favorite1.equals(favorite2), is(true));
    assertThat(favorite1.hashCode(), is(favorite2.hashCode()));
  }

  @Test
  public void should_not_be_equal_when_article_is_different() {
    ArticleFavorite favorite1 = new ArticleFavorite("article123", "user456");
    ArticleFavorite favorite2 = new ArticleFavorite("article789", "user456");
    
    assertThat(favorite1.equals(favorite2), is(false));
  }

  @Test
  public void should_not_be_equal_when_user_is_different() {
    ArticleFavorite favorite1 = new ArticleFavorite("article123", "user456");
    ArticleFavorite favorite2 = new ArticleFavorite("article123", "user789");
    
    assertThat(favorite1.equals(favorite2), is(false));
  }

  @Test
  public void should_not_be_equal_to_null() {
    ArticleFavorite favorite = new ArticleFavorite("article123", "user456");
    
    assertThat(favorite.equals(null), is(false));
  }

  @Test
  public void should_not_be_equal_to_different_type() {
    ArticleFavorite favorite = new ArticleFavorite("article123", "user456");
    
    assertThat(favorite.equals("article123"), is(false));
  }
}
