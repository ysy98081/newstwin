package com.est.newstwin.service;

import com.est.newstwin.domain.Photo;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.images.ImageGenerateParams;
import com.openai.models.images.ImageModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class ChatGPTService {

    private final OpenAIClient openAIClient;
    private final PhotoService photoService;

    /**
     * Alan → ChatGPT: Markdown 버전 분석
     */
    public String analyzeMarkdown(String alanText) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일"));

        String prompt = """
                당신은 한국 경제 뉴스의 '양면적 경제 해석'을 제공하는 전문가입니다.
                아래는 Alan AI가 수집한 오늘의 경제 뉴스 요약입니다.

                각 기사별로 다음 항목을 반드시 포함하여 **Markdown 형식으로 리포트**를 작성하세요.

                ---
                ## 안녕하세요. %s자 NewsTwin 경제 브리핑입니다.

                ### 1) 기사 제목
                - 링크: [뉴스 링크](URL)
                - 요약: 한두 문장으로 핵심 내용
                - 한국 경제에 미치는 **긍정적 측면**:
                    - 2가지 이상, 3~5문장
                - 한국 경제에 미치는 **부정적 측면**:
                    - 2가지 이상, 3~5문장
                - 해석 요약: 전체 맥락을 한 문장으로 정리

                ---
                [Alan 제공 뉴스 요약]
                %s
                """.formatted(today, alanText);

        ResponseCreateParams params = ResponseCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                .input(prompt)
                .maxOutputTokens(4096L)
                .temperature(0.4)
                .build();

        Response response = openAIClient.responses().create(params);

        return response.output().stream()
                .flatMap(o -> o.message().stream())
                .flatMap(m -> m.content().stream())
                .flatMap(c -> c.outputText().stream())
                .map(t -> t.text())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("(응답 없음)");
    }


    /**
     * ChatGPT: Markdown → JSON 변환
     */
    public String analyzeJson(String markdownText) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        String prompt = """
                당신은 이전에 작성한 Markdown 뉴스 리포트를 JSON 구조로 변환하는 데이터 분석가입니다.

                아래 Markdown 내용에는 각 기사의 제목, URL, 요약, 긍정적 측면, 부정적 측면이 포함되어 있습니다.
                이를 아래 스키마에 맞게 **JSON 배열**로 변환하세요.

                출력 형식:
                [
                  {
                    "title": "기사 제목",
                    "url": "기사 URL",
                    "summary": "기사 요약",
                    "positive": ["한국 경제에 긍정적인 이유"],
                    "negative": ["한국 경제에 부정적인 이유"]
                  }
                ]

                규칙:
                - Markdown 외의 텍스트는 포함하지 말 것
                - 핵심 문장만 추출

                [Markdown 원문]
                ----
                %s
                ----
                """.formatted(markdownText);

        ResponseCreateParams params = ResponseCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                .input(prompt)
                .maxOutputTokens(4096L)
                .temperature(0.3)
                .build();

        Response response = openAIClient.responses().create(params);

        String raw = response.output().stream()
                .flatMap(o -> o.message().stream())
                .flatMap(m -> m.content().stream())
                .flatMap(c -> c.outputText().stream())
                .map(t -> t.text())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("[]");

        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("^```(json)?", "")
                    .replaceAll("```$", "")
                    .trim();
        }
        return cleaned;
    }


    /**
     * 뉴스 전체를 아우르는 제목 생성
     */
    public String generateTitle(String markdownText) {
        String prompt = """
                당신은 경제 전문 에디터입니다.
                아래 Markdown 뉴스 리포트를 한 문장으로 요약하여,
                **오늘의 핵심 주제**를 표현하는 제목을 만드세요.

                조건:
                - 한국어 50자 이내
                - 날짜 제외

                [뉴스 리포트]
                ----
                %s
                ----
                """.formatted(markdownText);

        ResponseCreateParams params = ResponseCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                .input(prompt)
                .maxOutputTokens(500L)
                .temperature(0.3)
                .build();

        Response response = openAIClient.responses().create(params);

        return response.output().stream()
                .flatMap(o -> o.message().stream())
                .flatMap(m -> m.content().stream())
                .flatMap(c -> c.outputText().stream())
                .map(t -> t.text().trim().replaceAll("[\\n\\r]+", ""))
                .findFirst()
                .orElse("오늘의 경제 브리핑");
    }


    /**
     * 이미지 생성용 핵심 요약문 (길이 300자 이하)
     */
    public String extractImageKeywords(String markdownText) {

        String prompt = """
        아래 Markdown 경제 리포트에서,
        **이미지 생성에 필요한 핵심 키워드만 300자 이하로 요약하세요.**

        조건:
        - 긍정/부정 분석 절대 포함 X
        - 시장 흐름 / 산업 / 금리 / 수출입 / 투자 심리 등 '이미지화 가능한 요소'만 추출
        - 3~4문장 이내 요약
        - 수치, 날짜, 기업명 등 구체적 값은 제외

        ----
        %s
        ----
        """.formatted(markdownText);

        var params = ResponseCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                .input(prompt)
                .maxOutputTokens(300L)
                .temperature(0.4)
                .build();

        var response = openAIClient.responses().create(params);

        return response.output().stream()
                .flatMap(o -> o.message().stream())
                .flatMap(m -> m.content().stream())
                .flatMap(c -> c.outputText().stream())
                .map(t -> t.text().trim())
                .findFirst()
                .orElse("");
    }

    /**
     * 대표 이미지 생성 (gpt-image-1-mini)
     */
    public String generateRepresentativeImage(String markdownText) {

        // 1) 핵심 요약 생성
        String keywords = extractImageKeywords(markdownText);

        // 2) 이미지 생성 프롬프트
        String prompt = """
        아래 핵심 내용을 기반으로,
        5개 뉴스의 공통된 경제 분위기/흐름을 시각화한 대표 이미지를 생성하세요.

        조건:
        - 긍정/부정 양면성 제외
        - 개별 기사 묘사 금지
        - 산업, 지표, 글로벌 흐름 등 중립적 요소 중심
        - 인포그래픽 / 일러스트 스타일 허용
        - 과도한 공포·과도한 긍정 금지

        [핵심 요약]
        %s
        """.formatted(keywords);

        // 3) 이미지 생성 요청
        var params = ImageGenerateParams.builder()
                .model(ImageModel.GPT_IMAGE_1_MINI)
                .prompt(prompt)
                .size(ImageGenerateParams.Size._1024X1024)
                .n(1)
                .build();

        var result = openAIClient.images().generate(params);

        var image = result.data()
                .orElseThrow(() -> new RuntimeException("이미지 생성 실패"))
                .get(0);

        // 4) URL 먼저 시도
        if (image.url().isPresent()) {
            return image.url().get();
        }

        // 5) Base64 fallback
        if (image.b64Json().isPresent()) {
            String base64 = image.b64Json().get();
            byte[] decoded = Base64.getDecoder().decode(base64);

            MultipartFile file = new MockMultipartFile(
                    "generated.png",
                    "generated.png",
                    "image/png",
                    decoded
            );

            try {
                Photo saved = photoService.savePhoto(file);
                return saved.getS3Url();
            } catch (IOException e) {
                throw new RuntimeException("이미지 저장 실패", e);
            }
        }

        throw new RuntimeException("이미지 URL도 Base64도 없습니다.");
    }
}
