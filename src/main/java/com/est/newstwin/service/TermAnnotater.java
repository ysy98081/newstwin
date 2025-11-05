package com.est.newstwin.service;

import com.hankcs.hanlp.collection.AhoCorasick.AhoCorasickDoubleArrayTrie;
import java.util.ArrayList;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

@Component
public class TermAnnotater {

  private volatile AhoCorasickDoubleArrayTrie<String> trie;

  // TermCacheService.dict() 호출 시마다 갱신되도록 외부에서 reload 호출하게 설계
  public void build(Map<String, String> dict) {
    if (dict == null || dict.isEmpty()) return;

    var sorted = new java.util.TreeMap<String, String>();
    dict.keySet().forEach(k -> sorted.put(k, k));

    var newTrie = new AhoCorasickDoubleArrayTrie<String>();
    newTrie.build(sorted);
    this.trie = newTrie;
  }

  public String annotate(String rawHtml, Map<String, String> dict) {
    System.out.println("dict size = " + dict.size());
    System.out.println("raw = " + rawHtml);
    System.out.println("trie = " + (trie != null));

    if (rawHtml == null || rawHtml.isBlank() || dict.isEmpty()) return rawHtml;
    if (trie == null) build(dict);

    var hits = new ArrayList<int[]>();

    // matches 는 start/end 위치가 필요
    trie.parseText(rawHtml, (begin, end, value) -> {
      hits.add(new int[]{begin, end});
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
          HtmlUtils.htmlEscape(def) + "\">" + matched + "</span>";
      sb.replace(begin, end, repl);
    }
    return sb.toString();
  }
}