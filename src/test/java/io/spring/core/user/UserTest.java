package io.spring.core.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  public void should_create_user_with_valid_data() {
    User user = new User("test@example.com", "testuser", "password", "bio", "image.jpg");
    
    assertThat(user.getId(), notNullValue());
    assertThat(user.getEmail(), is("test@example.com"));
    assertThat(user.getUsername(), is("testuser"));
    assertThat(user.getPassword(), is("password"));
    assertThat(user.getBio(), is("bio"));
    assertThat(user.getImage(), is("image.jpg"));
  }

  @Test
  public void should_update_user_with_non_empty_values() {
    User user = new User("old@example.com", "olduser", "oldpass", "oldbio", "oldimage.jpg");
    
    user.update("new@example.com", "newuser", "newpass", "newbio", "newimage.jpg");
    
    assertThat(user.getEmail(), is("new@example.com"));
    assertThat(user.getUsername(), is("newuser"));
    assertThat(user.getPassword(), is("newpass"));
    assertThat(user.getBio(), is("newbio"));
    assertThat(user.getImage(), is("newimage.jpg"));
  }

  @Test
  public void should_not_update_user_with_empty_values() {
    User user = new User("test@example.com", "testuser", "password", "bio", "image.jpg");
    
    user.update("", "", "", "", "");
    
    assertThat(user.getEmail(), is("test@example.com"));
    assertThat(user.getUsername(), is("testuser"));
    assertThat(user.getPassword(), is("password"));
    assertThat(user.getBio(), is("bio"));
    assertThat(user.getImage(), is("image.jpg"));
  }

  @Test
  public void should_not_update_user_with_null_values() {
    User user = new User("test@example.com", "testuser", "password", "bio", "image.jpg");
    
    user.update(null, null, null, null, null);
    
    assertThat(user.getEmail(), is("test@example.com"));
    assertThat(user.getUsername(), is("testuser"));
    assertThat(user.getPassword(), is("password"));
    assertThat(user.getBio(), is("bio"));
    assertThat(user.getImage(), is("image.jpg"));
  }

  @Test
  public void should_update_only_non_empty_fields() {
    User user = new User("old@example.com", "olduser", "oldpass", "oldbio", "oldimage.jpg");
    
    user.update("new@example.com", "", "newpass", null, "newimage.jpg");
    
    assertThat(user.getEmail(), is("new@example.com"));
    assertThat(user.getUsername(), is("olduser"));
    assertThat(user.getPassword(), is("newpass"));
    assertThat(user.getBio(), is("oldbio"));
    assertThat(user.getImage(), is("newimage.jpg"));
  }
}
