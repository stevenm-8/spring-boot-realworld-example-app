package io.spring.core.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class TagTest {

  @Test
  public void should_create_tag_with_valid_name() {
    Tag tag = new Tag("java");
    
    assertThat(tag.getId(), notNullValue());
    assertThat(tag.getName(), is("java"));
  }

  @Test
  public void should_create_tag_with_empty_name() {
    Tag tag = new Tag("");
    
    assertThat(tag.getId(), notNullValue());
    assertThat(tag.getName(), is(""));
  }

  @Test
  public void should_create_tag_with_special_characters() {
    Tag tag = new Tag("spring-boot");
    
    assertThat(tag.getId(), notNullValue());
    assertThat(tag.getName(), is("spring-boot"));
  }

  @Test
  public void should_generate_unique_ids_for_different_tags() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("spring");
    
    assertThat(tag1.getId().equals(tag2.getId()), is(false));
  }

  @Test
  public void should_be_equal_when_names_are_same() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");
    
    assertThat(tag1.equals(tag2), is(true));
    assertThat(tag1.hashCode(), is(tag2.hashCode()));
  }

  @Test
  public void should_not_be_equal_when_names_are_different() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("spring");
    
    assertThat(tag1.equals(tag2), is(false));
  }

  @Test
  public void should_not_be_equal_to_null() {
    Tag tag = new Tag("java");
    
    assertThat(tag.equals(null), is(false));
  }

  @Test
  public void should_not_be_equal_to_different_type() {
    Tag tag = new Tag("java");
    
    assertThat(tag.equals("java"), is(false));
  }
}
