package com.est.newstwin.service;

import com.est.newstwin.domain.Category;
import com.est.newstwin.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsPipelineService {

    private final AlanApiService alanApiService;
    private final ChatGPTService chatGPTService;
    private final AIPostService aiPostService;

    // 세션 내 중복 뉴스 추적용
    private final Set<String> usedUrls = new HashSet<>();

    // URL 검증용 정규식
    private static final Pattern URL_PATTERN = Pattern.compile("https?://[\\w\\-\\.]+(?:/[^\\s]*)?");

    public void processCategory(Category category, Member aiMember) {
        log.info("🟢 [Pipeline 시작] 카테고리: {}", category.getCategoryName());

        try {
            // 1️⃣ Alan 호출 + URL 검증 + 중복 제거
            String alanText = fetchAlanNewsWithRetry(category, 3); // 최대 3회 재시도

            if (alanText == null || alanText.isBlank()) {
                log.warn("🚫 Alan 응답이 비어 있습니다. category={}", category.getCategoryName());
                return;
            }

            // Alan 결과 미리보기
            log.info("⭐ Alan 응답 미리보기:\n{}", preview(alanText));

            // 2️⃣ ChatGPT - Markdown 분석
            log.info("⭐ ChatGPT 분석 (Markdown) 요청 중...");
            String markdown = chatGPTService.analyzeMarkdown(alanText);
            log.info("✅ Markdown 분석 완료 (길이: {} chars)", markdown != null ? markdown.length() : 0);
            log.info("⭐ Markdown 미리보기:\n{}", preview(markdown));

            // 3️⃣ ChatGPT - JSON 변환
            log.info("⭐ ChatGPT JSON 변환 요청 중...");
            String json = chatGPTService.analyzeJson(markdown);
            log.info("✅ JSON 변환 완료 (길이: {} chars)", json != null ? json.length() : 0);
            log.info("⭐ JSON 미리보기:\n{}", preview(json));

            // 4️⃣ ChatGPT - 제목 생성
            log.info("⭐ ChatGPT 제목 생성 중...");
            String title = chatGPTService.generateTitle(markdown);
            log.info("✅ 제목 생성 완료: {}", title);

            // 5️⃣ 게시글 저장
            log.info("⭐ AI 게시글 저장 시작...");
            aiPostService.saveAiPost(aiMember, category, markdown, json, title);
            log.info("✅ 게시글 저장 성공: [카테고리: {}, 제목: {}]", category.getCategoryName(), title);

        } catch (Exception e) {
            log.error("❌ [Pipeline Error: {}] {}", category.getCategoryName(), e.getMessage(), e);
        }

        log.info("⭐ [Pipeline 종료] 카테고리: {}", category.getCategoryName());
    }

    /**
     * Alan 호출 + URL 검증 + 중복 제거 + 재시도 로직
     */
    private String fetchAlanNewsWithRetry(Category category, int maxRetry) {
        for (int attempt = 1; attempt <= maxRetry; attempt++) {
            log.info("⭐ Alan API 호출 (시도 {} / {}) ...", attempt, maxRetry);
            String alanText = alanApiService.fetchNews(category.getCategoryName(), usedUrls);

            if (alanText == null || alanText.isBlank()) {
                log.warn("⚠️ Alan 응답이 비어 있음 → 재시도");
                continue;
            }

            // 유효 URL 추출
            Set<String> urls = extractUrls(alanText);
            if (urls.isEmpty()) {
                log.warn("⚠️ Alan 응답에 유효한 URL 없음 → 재시도");
                continue;
            }

            // 중복 URL 제거
            Set<String> duplicateUrls = new HashSet<>(urls);
            duplicateUrls.retainAll(usedUrls);

            if (!duplicateUrls.isEmpty()) {
                log.warn("⚠️ 중복된 URL 발견 ({}개): {}", duplicateUrls.size(), duplicateUrls);
                // Alan에게 중복된 키워드(url 일부)를 제외 조건으로 다시 요청
                continue;
            }

            // 중복 없음 → 성공
            usedUrls.addAll(urls);
            log.info("✅ 새로운 URL {}개 수집됨 (누적 총 {}개)", urls.size(), usedUrls.size());
            return alanText;
        }

        log.error("🚫 Alan 뉴스 3회 시도 후에도 유효/비중복 뉴스 확보 실패: {}", category.getCategoryName());
        return null;
    }

    /**
     * Alan 응답 내 URL 목록 추출
     */
    private Set<String> extractUrls(String text) {
        Set<String> urls = new HashSet<>();
        if (text == null || text.isBlank()) return urls;

        Matcher matcher = URL_PATTERN.matcher(text);
        while (matcher.find()) {
            urls.add(matcher.group());
        }
        return urls;
    }

    /**
     * 응답 문자열 미리보기 (길면 앞부분 500자만)
     */
    private String preview(String text) {
        if (text == null) return "(null)";
        return text.length() > 500 ? text.substring(0, 600) + "..." : text;
    }
}
