package com.est.newstwin.dto.api;

import com.est.newstwin.domain.Category;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryDto {
  private Long id;
  private String name;

  public static CategoryDto fromEntity(Category category) {
    return new CategoryDto(category.getId(), category.getCategoryName());
  }

  public CategoryDto(Long id, String name) {
    this.id = id;
    this.name = name;
  }
}
