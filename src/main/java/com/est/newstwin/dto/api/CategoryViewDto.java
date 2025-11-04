package com.est.newstwin.dto.api;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryViewDto {
  private Long id;
  private String name;
  private boolean isActive;
}
