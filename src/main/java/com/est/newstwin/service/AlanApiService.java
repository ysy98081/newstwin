package com.est.newstwin.service;

import com.est.newstwin.config.AlanAIConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AlanApiService {

    private final RestTemplate restTemplate;
    private final AlanAIConfig alanAIConfig;

    /**
     *  Alan AI에서 뉴스 텍스트 가져오기
     */
    public String fetchNews(String category, Set<String> excludeKeywords) {
        try {
            String prompt = buildPrompt(category, excludeKeywords);
            String encodedPrompt = URLEncoder.encode(prompt, StandardCharsets.UTF_8);

            String url = alanAIConfig.getBaseUrl()
                    + "/question?client_id=" + alanAIConfig.getClientId()
                    + "&content=" + encodedPrompt;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class
            );

            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Alan API 호출 실패: " + e.getMessage(), e);
        }
    }

    /**
     *  Alan 세션 상태 초기화
     */
    public String resetAlanState() {
        try {
            String url = alanAIConfig.getBaseUrl() + "/reset-state";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String body = "{\"client_id\":\"" + alanAIConfig.getClientId() + "\"}";
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.DELETE, entity, String.class
            );

            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Alan 상태 초기화 실패: " + e.getMessage(), e);
        }
    }

    /**
     *  Alan에게 전달할 프롬프트 생성
     */
    private String buildPrompt(String category, Set<String> excludeKeywords) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일"));

        StringBuilder sb = new StringBuilder();

        sb.append("%s자에 발행된 '%s' 관련 주요 경제 뉴스를 5개 선정해주세요.\n"
                        .formatted(today, category))
                .append("각 뉴스는 기사 제목과 간단한 요약만 포함해주세요.\n")
                .append("반드시 **공식 언론사(예: 한겨레, 조선일보, 연합뉴스, 한국경제, 매일경제, 블룸버그 등)** 에서 발행된 실제 뉴스 기사만 포함해주세요.\n")
                .append("다음과 같은 사이트의 콘텐츠는 절대 포함하지 마세요:\n")
                .append("- 블로그, 개인 투자 칼럼, 네이버 프리미엄콘텐츠, KBThink, 브런치, 카페, .html 파일, 인플루언서 글\n")
                .append("- 기업 홍보성 보도자료나 사설 칼럼\n")
                .append("중복 기사나 광고성 뉴스도 제외해주세요.\n");

        if (excludeKeywords != null && !excludeKeywords.isEmpty()) {
            sb.append("제외 키워드: ").append(String.join(", ", excludeKeywords)).append("\n");
        }

        sb.append("\n출력 형식 예시:\n")
                .append("1. **[기사 제목]** (출처: 매일경제)\n")
                .append("   - 요약: 한두 문장으로 핵심 내용을 작성\n\n")
                .append("이 형식을 따라 5개의 뉴스를 작성해주세요.");

        return sb.toString();
    }



}
