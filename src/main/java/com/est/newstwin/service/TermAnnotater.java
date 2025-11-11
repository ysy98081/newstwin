package com.est.newstwin.service;

import com.hankcs.hanlp.collection.AhoCorasick.AhoCorasickDoubleArrayTrie;
import java.util.ArrayList;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

@Component
public class TermAnnotater {

  private volatile AhoCorasickDoubleArrayTrie<String> trie;

  // TermCacheService.dict() 호출 시마다 갱신되도록 외부에서 reload 호출
  public void build(Map<String, String> dict) {
    if (dict == null || dict.isEmpty()) return;

    var sorted = new java.util.TreeMap<String, String>();
    dict.keySet().forEach(k -> sorted.put(k, k));

    var newTrie = new AhoCorasickDoubleArrayTrie<String>();
    newTrie.build(sorted);
    this.trie = newTrie;
  }

  public String annotate(String rawHtml, Map<String, String> dict) {
    if (rawHtml == null || rawHtml.isBlank() || dict.isEmpty()) return rawHtml;
    if (trie == null) build(dict);

    var hits = new ArrayList<int[]>();

    // matches 는 start/end 위치가 필요
    trie.parseText(rawHtml, (begin, end, value) -> {
      // 앞뒤 문자 확인 (스페이스·점·쉼표·따옴표)
      char before = begin > 0 ? rawHtml.charAt(begin - 1) : ' ';
      char after = end < rawHtml.length() ? rawHtml.charAt(end) : ' ';

      if (isBoundary(before) && (isBoundary(after) || isParticle(after))) {
        hits.add(new int[]{begin, end});
      }
    });


    if (hits.isEmpty()) return rawHtml;

    var sb = new StringBuilder(rawHtml);

    for (int i = hits.size() - 1; i >= 0; i--) {
      int[] be = hits.get(i);
      int begin = be[0], end = be[1];
      String matched = rawHtml.substring(begin, end); // 원문 텍스트
      String def = dict.get(matched); // 여기서 definition lookup
      if(def == null) continue;
      String repl = "<span class=\"nt-term\" data-bs-toggle=\"tooltip\" title=\"" +
              HtmlUtils.htmlEscape(def) + "\">" +
              matched + "<i class='bi bi-question-circle-fill term-icon'></i></span>";
      sb.replace(begin, end, repl);
    }
    return sb.toString();
  }

  private boolean isBoundary(char c) {
    return Character.isWhitespace(c) || c == '.' || c == ',' || c == '"';
  }

  private boolean isParticle(char c) {
    return "이가을를은는의로과와도만".indexOf(c) >= 0;
  }
}