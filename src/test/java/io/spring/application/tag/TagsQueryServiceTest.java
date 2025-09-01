package io.spring.application.tag;

import io.spring.application.TagsQueryService;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({TagsQueryService.class, MyBatisArticleRepository.class})
public class TagsQueryServiceTest extends DbTestBase {
  @Autowired private TagsQueryService tagsQueryService;

  @Autowired private ArticleRepository articleRepository;

  @Test
  public void should_get_all_tags() {
    articleRepository.save(new Article("test", "test", "test", Arrays.asList("java"), "123"));
    Assertions.assertTrue(tagsQueryService.allTags().contains("java"));
  }

  @Test
  public void should_return_empty_list_when_no_tags() {
    List<String> tags = tagsQueryService.allTags();
    Assertions.assertTrue(tags.isEmpty());
  }

  @Test
  public void should_handle_duplicate_tags() {
    articleRepository.save(new Article("test1", "test", "test", Arrays.asList("java", "java"), "123"));
    articleRepository.save(new Article("test2", "test", "test", Arrays.asList("java", "spring"), "123"));
    
    List<String> tags = tagsQueryService.allTags();
    Assertions.assertTrue(tags.contains("java"));
    Assertions.assertTrue(tags.contains("spring"));
    
    long javaCount = tags.stream().filter(tag -> tag.equals("java")).count();
    Assertions.assertEquals(1, javaCount);
  }

  @Test
  public void should_handle_special_characters_in_tags() {
    articleRepository.save(new Article("test", "test", "test", Arrays.asList("c++", "c#", "node.js"), "123"));
    
    List<String> tags = tagsQueryService.allTags();
    Assertions.assertTrue(tags.contains("c++"));
    Assertions.assertTrue(tags.contains("c#"));
    Assertions.assertTrue(tags.contains("node.js"));
  }

  @Test
  public void should_handle_empty_tag_list() {
    articleRepository.save(new Article("test", "test", "test", Arrays.asList(), "123"));
    
    List<String> tags = tagsQueryService.allTags();
    Assertions.assertTrue(tags.isEmpty());
  }

  @Test
  public void should_handle_null_tags_gracefully() {
    try {
      articleRepository.save(new Article("test", "test", "test", null, "123"));
    } catch (Exception e) {
    }
    
    List<String> tags = tagsQueryService.allTags();
    Assertions.assertNotNull(tags);
  }

  @Test
  public void should_handle_tags_with_whitespace() {
    articleRepository.save(new Article("test", "test", "test", Arrays.asList(" java ", "spring boot", "  react  "), "123"));
    
    List<String> tags = tagsQueryService.allTags();
    Assertions.assertTrue(tags.contains(" java "));
    Assertions.assertTrue(tags.contains("spring boot"));
    Assertions.assertTrue(tags.contains("  react  "));
  }

  @Test
  public void should_handle_case_sensitive_tags() {
    articleRepository.save(new Article("test1", "test", "test", Arrays.asList("Java"), "123"));
    articleRepository.save(new Article("test2", "test", "test", Arrays.asList("java"), "123"));
    
    List<String> tags = tagsQueryService.allTags();
    Assertions.assertTrue(tags.contains("Java"));
    Assertions.assertTrue(tags.contains("java"));
    Assertions.assertEquals(2, tags.size());
  }

  @Test
  public void should_handle_unicode_tags() {
    articleRepository.save(new Article("test", "test", "test", Arrays.asList("日本語", "español", "français"), "123"));
    
    List<String> tags = tagsQueryService.allTags();
    Assertions.assertTrue(tags.contains("日本語"));
    Assertions.assertTrue(tags.contains("español"));
    Assertions.assertTrue(tags.contains("français"));
  }

  @Test
  public void should_handle_very_long_tag_names() {
    String longTag = "a".repeat(255);
    articleRepository.save(new Article("test", "test", "test", Arrays.asList(longTag), "123"));
    
    List<String> tags = tagsQueryService.allTags();
    Assertions.assertTrue(tags.contains(longTag));
  }

  @Test
  public void should_handle_multiple_articles_with_same_tags() {
    articleRepository.save(new Article("test1", "test", "test", Arrays.asList("java", "spring"), "123"));
    articleRepository.save(new Article("test2", "test", "test", Arrays.asList("java", "react"), "456"));
    articleRepository.save(new Article("test3", "test", "test", Arrays.asList("spring", "boot"), "789"));
    
    List<String> tags = tagsQueryService.allTags();
    Assertions.assertTrue(tags.contains("java"));
    Assertions.assertTrue(tags.contains("spring"));
    Assertions.assertTrue(tags.contains("react"));
    Assertions.assertTrue(tags.contains("boot"));
    
    long javaCount = tags.stream().filter(tag -> tag.equals("java")).count();
    long springCount = tags.stream().filter(tag -> tag.equals("spring")).count();
    Assertions.assertEquals(1, javaCount);
    Assertions.assertEquals(1, springCount);
  }
}
