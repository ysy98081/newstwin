package com.est.newstwin.service;

import com.est.newstwin.repository.TermRepository;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TermCacheService {
  private final TermRepository repo;
  private final TermAnnotater annotator;

  private volatile Map<String,String> dict = Map.of();

  @EventListener(ApplicationReadyEvent.class)
  public void load() {
    dict = repo.findAll().stream()
        .collect(Collectors.toUnmodifiableMap(
            t -> t.getTerm().trim(),
            t -> t.getDefinition().trim(),
            (a,b)->a
        ));
    annotator.build(dict); // ← aho trie build
  }

  public Map<String,String> dict(){return dict;}

  public void reload() { load(); }
}
