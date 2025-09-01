package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProfileApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ProfileApiTest extends TestWithCurrentUser {
  private User anotherUser;

  @Autowired private MockMvc mvc;

  @MockBean private ProfileQueryService profileQueryService;

  private ProfileData profileData;

  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
    anotherUser = new User("username@test.com", "username", "123", "", "");
    profileData =
        new ProfileData(
            anotherUser.getId(),
            anotherUser.getUsername(),
            anotherUser.getBio(),
            anotherUser.getImage(),
            false);
    when(userRepository.findByUsername(eq(anotherUser.getUsername())))
        .thenReturn(Optional.of(anotherUser));
  }

  @Test
  public void should_get_user_profile_success() throws Exception {
    when(profileQueryService.findByUsername(eq(profileData.getUsername()), eq(null)))
        .thenReturn(Optional.of(profileData));
    RestAssuredMockMvc.when()
        .get("/profiles/{username}", profileData.getUsername())
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("profile.username", equalTo(profileData.getUsername()));
  }

  @Test
  public void should_follow_user_success() throws Exception {
    when(profileQueryService.findByUsername(eq(profileData.getUsername()), eq(user)))
        .thenReturn(Optional.of(profileData));
    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/profiles/{username}/follow", anotherUser.getUsername())
        .prettyPeek()
        .then()
        .statusCode(200);
    verify(userRepository).saveRelation(new FollowRelation(user.getId(), anotherUser.getId()));
  }

  @Test
  public void should_unfollow_user_success() throws Exception {
    FollowRelation followRelation = new FollowRelation(user.getId(), anotherUser.getId());
    when(userRepository.findRelation(eq(user.getId()), eq(anotherUser.getId())))
        .thenReturn(Optional.of(followRelation));
    when(profileQueryService.findByUsername(eq(profileData.getUsername()), eq(user)))
        .thenReturn(Optional.of(profileData));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/profiles/{username}/follow", anotherUser.getUsername())
        .prettyPeek()
        .then()
        .statusCode(200);

    verify(userRepository).removeRelation(eq(followRelation));
  }

  @Test
  public void should_return_404_for_non_existent_profile() throws Exception {
    when(profileQueryService.findByUsername(eq("nonexistent"), eq(null)))
        .thenReturn(Optional.empty());

    RestAssuredMockMvc.when()
        .get("/profiles/nonexistent")
        .then()
        .statusCode(404);
  }

  @Test
  public void should_prevent_self_follow() throws Exception {
    when(profileQueryService.findByUsername(eq(user.getUsername()), eq(user)))
        .thenReturn(Optional.of(new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false)));
    
    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/profiles/{username}/follow", user.getUsername())
        .then()
        .statusCode(200);
  }

  @Test
  public void should_handle_follow_non_existent_user() throws Exception {
    when(userRepository.findByUsername(eq("nonexistent")))
        .thenReturn(Optional.empty());

    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/profiles/nonexistent/follow")
        .then()
        .statusCode(404);
  }

  @Test
  public void should_handle_unfollow_when_not_following() throws Exception {
    when(userRepository.findRelation(any(), any()))
        .thenReturn(Optional.empty());

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/profiles/{username}/follow", anotherUser.getUsername())
        .then()
        .statusCode(404);
  }

  @Test
  public void should_require_authentication_for_follow() throws Exception {
    given()
        .when()
        .post("/profiles/{username}/follow", anotherUser.getUsername())
        .then()
        .statusCode(401);
  }

  @Test
  public void should_require_authentication_for_unfollow() throws Exception {
    given()
        .when()
        .delete("/profiles/{username}/follow", anotherUser.getUsername())
        .then()
        .statusCode(401);
  }

  @Test
  public void should_handle_profile_with_empty_bio() throws Exception {
    ProfileData emptyBioProfile = new ProfileData(
        anotherUser.getId(), 
        anotherUser.getUsername(), 
        "", 
        anotherUser.getImage(), 
        false
    );
    when(profileQueryService.findByUsername(eq(anotherUser.getUsername()), eq(null)))
        .thenReturn(Optional.of(emptyBioProfile));

    RestAssuredMockMvc.when()
        .get("/profiles/{username}", anotherUser.getUsername())
        .then()
        .statusCode(200)
        .body("profile.username", equalTo(anotherUser.getUsername()))
        .body("profile.bio", equalTo(""))
        .body("profile.following", equalTo(false));
  }

  @Test
  public void should_handle_profile_with_null_image() throws Exception {
    ProfileData nullImageProfile = new ProfileData(
        anotherUser.getId(), 
        anotherUser.getUsername(), 
        anotherUser.getBio(), 
        null, 
        false
    );
    when(profileQueryService.findByUsername(eq(anotherUser.getUsername()), eq(null)))
        .thenReturn(Optional.of(nullImageProfile));

    RestAssuredMockMvc.when()
        .get("/profiles/{username}", anotherUser.getUsername())
        .then()
        .statusCode(200)
        .body("profile.username", equalTo(anotherUser.getUsername()))
        .body("profile.following", equalTo(false));
  }

  @Test
  public void should_show_following_status_when_authenticated() throws Exception {
    ProfileData followingProfile = new ProfileData(
        anotherUser.getId(), 
        anotherUser.getUsername(), 
        anotherUser.getBio(), 
        anotherUser.getImage(), 
        true
    );
    when(profileQueryService.findByUsername(eq(anotherUser.getUsername()), eq(user)))
        .thenReturn(Optional.of(followingProfile));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .get("/profiles/{username}", anotherUser.getUsername())
        .then()
        .statusCode(200)
        .body("profile.username", equalTo(anotherUser.getUsername()))
        .body("profile.following", equalTo(true));
  }
}
