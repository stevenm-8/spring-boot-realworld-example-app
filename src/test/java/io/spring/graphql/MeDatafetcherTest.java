package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class MeDatafetcherTest {

  @Mock private UserQueryService userQueryService;
  @Mock private JwtService jwtService;
  @Mock private DataFetchingEnvironment dataFetchingEnvironment;
  @Mock private SecurityContext securityContext;
  @Mock private Authentication authentication;

  @InjectMocks private MeDatafetcher meDatafetcher;

  private User user;
  private UserData userData;

  @BeforeEach
  public void setUp() {
    user = new User("test@example.com", "testuser", "password", "bio", "image");
    userData = new UserData(user.getId(), "test@example.com", "testuser", "bio", "image");
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  public void should_get_me_successfully() {
    String authorization = "Token test-token";
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(user);
    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.of(userData));

    DataFetcherResult<io.spring.graphql.types.User> result = meDatafetcher.getMe(authorization, dataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("test@example.com", result.getData().getEmail());
    assertEquals("testuser", result.getData().getUsername());
    assertEquals("test-token", result.getData().getToken());
  }

  @Test
  public void should_return_null_for_anonymous_user() {
    AnonymousAuthenticationToken anonymousAuth = new AnonymousAuthenticationToken("key", "principal", 
        java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    when(securityContext.getAuthentication()).thenReturn(anonymousAuth);

    DataFetcherResult<io.spring.graphql.types.User> result = meDatafetcher.getMe("Token test-token", dataFetchingEnvironment);

    assertNull(result);
  }

  @Test
  public void should_get_user_payload_user_from_context() {
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(user);
    when(jwtService.toToken(eq(user))).thenReturn("generated-token");

    DataFetcherResult<io.spring.graphql.types.User> result = meDatafetcher.getUserPayloadUser(dataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("test@example.com", result.getData().getEmail());
    assertEquals("testuser", result.getData().getUsername());
    assertEquals("generated-token", result.getData().getToken());
    assertEquals(user, result.getLocalContext());
  }
}
