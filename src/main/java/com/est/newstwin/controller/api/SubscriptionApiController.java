package com.est.newstwin.controller.api;

import com.est.newstwin.service.SubscriptionService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionApiController {

  private final SubscriptionService subscriptionService;

  @PostMapping("/toggle-category")
  public ResponseEntity<?> toggleCategory(@RequestParam Long categoryId,
      @AuthenticationPrincipal UserDetails userDetails){
    if (userDetails == null) return ResponseEntity.status(401).build();
    boolean active = subscriptionService.toggleCategory(categoryId, userDetails.getUsername());
    return ResponseEntity.ok(Map.of("active", active));
  }

  @PostMapping("/subscribe-all")
  public ResponseEntity<?> subscribeAll(@AuthenticationPrincipal UserDetails userDetails){
    if (userDetails == null) return ResponseEntity.status(401).build();
    int activated = subscriptionService.subscribeAll(userDetails.getUsername());
    return ResponseEntity.ok(Map.of("activated", activated));
  }
}