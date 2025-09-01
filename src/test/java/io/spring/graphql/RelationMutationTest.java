package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.when;

import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.ProfilePayload;
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
public class RelationMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private ProfileQueryService profileQueryService;
  @Mock private SecurityContext securityContext;
  @Mock private Authentication authentication;

  @InjectMocks private RelationMutation relationMutation;

  private User user;
  private User targetUser;
  private ProfileData profileData;

  @BeforeEach
  public void setUp() {
    user = new User("test@example.com", "testuser", "password", "bio", "image");
    targetUser = new User("target@example.com", "targetuser", "password", "bio", "image");
    profileData = new ProfileData(targetUser.getId(), "targetuser", "bio", "image", true);
    
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(user);
  }

  @Test
  public void should_follow_user_successfully() {
    String username = "targetuser";
    
    when(userRepository.findByUsername(eq(username))).thenReturn(Optional.of(targetUser));
    when(profileQueryService.findByUsername(eq(username), eq(user))).thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.follow(username);

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("targetuser", result.getProfile().getUsername());
  }

  @Test
  public void should_unfollow_user_successfully() {
    String username = "targetuser";
    ProfileData unfollowedProfile = new ProfileData(targetUser.getId(), "targetuser", "bio", "image", false);
    
    when(userRepository.findByUsername(eq(username))).thenReturn(Optional.of(targetUser));
    when(userRepository.findRelation(eq(user.getId()), eq(targetUser.getId()))).thenReturn(Optional.of(new io.spring.core.user.FollowRelation(user.getId(), targetUser.getId())));
    when(profileQueryService.findByUsername(eq(username), eq(user))).thenReturn(Optional.of(unfollowedProfile));

    ProfilePayload result = relationMutation.unfollow(username);

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("targetuser", result.getProfile().getUsername());
  }
}
