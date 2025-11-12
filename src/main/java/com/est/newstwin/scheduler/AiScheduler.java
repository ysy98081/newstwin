package com.est.newstwin.scheduler;

import com.est.newstwin.domain.Category;
import com.est.newstwin.domain.Member;
import com.est.newstwin.repository.CategoryRepository;
import com.est.newstwin.repository.MemberRepository;
import com.est.newstwin.service.NewsPipelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI 뉴스 자동 생성 스케줄러
 * - Alan → ChatGPT → Post 저장 자동 수행
 * - 15분마다 실행 (서울 기준)
 * - Member ID=1L ("AI Writer") 기준으로 실행
 * - 동시에 중복 실행되지 않도록 락 플래그(isRunning) 적용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiScheduler {

    private final NewsPipelineService newsPipelineService;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;

    /** 현재 실행 중인지 여부 (중복 방지용 플래그) */
    private boolean isRunning = false;

    /**
     * 15분마다 자동 실행 (서울 기준)
     * Cron 표현식: 초 분 시 일 월 요일
     * → 0, 15, 30, 45분마다 실행
     */
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public synchronized void runPipelineEvery15Min() {
        // 중복 실행 방지
        if (isRunning) {
            log.warn("🚫 [Scheduler] 이전 작업이 아직 종료되지 않았습니다. 실행 건너뜀.");
            return;
        }

        try {
            isRunning = true;
            log.info("🕒 [Scheduler] 15분 간격 자동 실행 시작");

            // AI 계정 불러오기 (id=1L)
            Member aiMember = memberRepository.findById(1L)
                    .orElseThrow(() -> new IllegalStateException("AI Writer 계정을 찾을 수 없습니다."));

            // 카테고리 전체 불러오기
            List<Category> categories = categoryRepository.findAll();
            if (categories.isEmpty()) {
                log.warn("🚫 [Scheduler] 등록된 카테고리가 없습니다.");
                return;
            }

            // 각 카테고리별 뉴스 생성
            categories.forEach(cat -> {
                try {
                    log.info("📢 [Scheduler] 카테고리 처리 시작: {}", cat.getCategoryName());
                    newsPipelineService.processCategory(cat, aiMember);
                } catch (Exception e) {
                    log.error("❌ [Scheduler Error] {}: {}", cat.getCategoryName(), e.getMessage(), e);
                }
            });

            log.info("✅ [Scheduler] 모든 카테고리 처리 완료");

        } catch (Exception e) {
            log.error("❌ [Scheduler] 실행 중 오류 발생: {}", e.getMessage(), e);
        } finally {
            isRunning = false; // 반드시 플래그 해제
        }
    }
}
