package io.spring.core.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class FollowRelationTest {

  @Test
  public void should_create_follow_relation_with_valid_data() {
    FollowRelation followRelation = new FollowRelation("user123", "target456");
    
    assertThat(followRelation.getUserId(), is("user123"));
    assertThat(followRelation.getTargetId(), is("target456"));
  }

  @Test
  public void should_create_follow_relation_with_same_user_and_target() {
    FollowRelation followRelation = new FollowRelation("user123", "user123");
    
    assertThat(followRelation.getUserId(), is("user123"));
    assertThat(followRelation.getTargetId(), is("user123"));
  }

  @Test
  public void should_create_follow_relation_with_empty_strings() {
    FollowRelation followRelation = new FollowRelation("", "");
    
    assertThat(followRelation.getUserId(), is(""));
    assertThat(followRelation.getTargetId(), is(""));
  }

  @Test
  public void should_create_follow_relation_with_null_values() {
    FollowRelation followRelation = new FollowRelation(null, null);
    
    assertThat(followRelation.getUserId(), is((String) null));
    assertThat(followRelation.getTargetId(), is((String) null));
  }

  @Test
  public void should_be_equal_when_user_and_target_are_same() {
    FollowRelation relation1 = new FollowRelation("user123", "target456");
    FollowRelation relation2 = new FollowRelation("user123", "target456");
    
    assertThat(relation1.equals(relation2), is(true));
    assertThat(relation1.hashCode(), is(relation2.hashCode()));
  }

  @Test
  public void should_not_be_equal_when_user_is_different() {
    FollowRelation relation1 = new FollowRelation("user123", "target456");
    FollowRelation relation2 = new FollowRelation("user789", "target456");
    
    assertThat(relation1.equals(relation2), is(false));
  }

  @Test
  public void should_not_be_equal_when_target_is_different() {
    FollowRelation relation1 = new FollowRelation("user123", "target456");
    FollowRelation relation2 = new FollowRelation("user123", "target789");
    
    assertThat(relation1.equals(relation2), is(false));
  }
}
