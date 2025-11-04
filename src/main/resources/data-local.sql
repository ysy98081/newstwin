-- MEMBER
INSERT INTO member (member_name, email, password, role, status, created_at, updated_at)
VALUES ('master', 'master@test.com', '$2b$12$Xq6gldZgCgsec6HLq7bB/e.xOTyFZq4q4eXqwXdBOXS/E2TqVaXy6', 'ROLE_ADMIN', true,NOW(), NOW());
INSERT INTO member (member_name, email, password, role, status, created_at, updated_at)
VALUES ('tester', 'tester@test.com', '$2b$12$Xq6gldZgCgsec6HLq7bB/e.xOTyFZq4q4eXqwXdBOXS/E2TqVaXy6', 'ROLE_USER', true,NOW(), NOW());

INSERT INTO member (member_name, email, password, role, status, created_at, updated_at)
VALUES ('tester1', 'tester1@test.com', '$2b$12$Xq6gldZgCgsec6HLq7bB/e.xOTyFZq4q4eXqwXdBOXS/E2TqVaXy6', 'ROLE_USER', true,NOW(), NOW());
INSERT INTO member (member_name, email, password, role, status, created_at, updated_at)
VALUES ('tester2', 'tester2@test.com', '$2b$12$Xq6gldZgCgsec6HLq7bB/e.xOTyFZq4q4eXqwXdBOXS/E2TqVaXy6', 'ROLE_USER',true, NOW(), NOW());


-- CATEGORY
INSERT INTO category (category_id, category_name) VALUES (1, '금융');
INSERT INTO category (category_id, category_name) VALUES (2, '증권');
INSERT INTO category (category_id, category_name) VALUES (3,'산업');
INSERT INTO category (category_id, category_name) VALUES (4,'부동산');
INSERT INTO category (category_id, category_name) VALUES (5,'글로벌');
INSERT INTO category (category_id, category_name) VALUES (6,'생활');
INSERT INTO category (category_id, category_name) VALUES (7,'일반');

-- USER_SUBSCRIPTION
INSERT INTO user_subscription (member_id, category_id, status, created_at, updated_at)
VALUES (1, 1, TRUE, NOW(), NOW());
INSERT INTO user_subscription (member_id, category_id, status, created_at, updated_at)
VALUES (2, 2, TRUE, NOW(), NOW());
INSERT INTO user_subscription (member_id, category_id, status, created_at, updated_at)
VALUES (3, 3, TRUE, NOW(), NOW());
INSERT INTO user_subscription (member_id, category_id, status, created_at, updated_at)
VALUES (4, 2, TRUE, NOW(), NOW());

-- POST
INSERT INTO post (member_id, category_id, type, title, content, count, thumbnail_url, status, created_at, updated_at)
VALUES (1, 1, 'news', '경제 뉴스 1', 'International Monetary Fund(IMF)가 2025년 세계 경제성장률을 약 3.2%로 상향 조정했으나, 여전히 둔화세가 뚜렷하며 보호무역·정책 불확실성이 리스크로 작용하고 있다. 긍정적 측면 성장률이 하향세에 머무르긴 하지만, 예상보다는 상향돼 경제 회복 가능성에 대한 신호가 존재한다. 기술 투자(특히 AI) 같은 신규 분야가 성장 동력을 제공하며 구조전환의 기회가 될 수 있다. 부정적 측면 여전히 리스크가 크며, 특히 무역마찰·정책 불확실성 등이 투자·고용을 억제할 수 있다. 성장률이 낮은 상태가 장기간 지속되면 구조적 침체로 전환될 우려가 있다.', 0, '', true, NOW(), NOW());

INSERT INTO post (member_id, category_id, type, title, content, count, thumbnail_url, status, created_at, updated_at)
VALUES (1, 3, 'news', '탄소중립 도시 시범사업 본격화', '정부가 탄소중립 도시 구축을 위한 시범사업을 전국 5곳에서 시작했다. 친환경 교통수단 도입과 스마트에너지 관리시스템이 핵심이다.', 6, '', true, '2025-10-13 08:32:47', '2025-10-13 08:32:47');

INSERT INTO post (member_id, category_id, type, title, content, count, thumbnail_url, status, created_at, updated_at)
VALUES (1, 3, 'news', '탄소중립 도시 시범사업 본격화', '정부가 탄소중립 도시 구축을 위한 시범사업을 전국 5곳에서 시작했다. 친환경 교통수단 도입과 스마트에너지 관리시스템이 핵심이다.', 6, '', true, '2025-10-13 08:32:47', '2025-10-13 08:32:47');

INSERT INTO post (member_id, category_id, type, title, content, count, thumbnail_url, status, created_at, updated_at)
VALUES (1, 2, 'news', 'IT 뉴스', 'Tata Consultancy Services(TCS)가 12,000명 이상 감원하며 인도 아웃소싱 산업이 AI‧자동화 중심으로 구조 재편되고 있음을 나타냈다. 긍정적인 측면 기업이 AI 및 자동화에 투자하며 생산성과 효율성을 제고, 글로벌 IT 서비스 중심 경쟁력을 강화할 기회가 생긴다. 기술 재훈련·고급 역량 개발이 촉진되어 장기적으로 더 강한 인재 생태계가 구축될 수 있다. 부정적인 측면 단기적으로 많은 직원이 일자리를 잃고 중견 경력자에 대한 수요가 줄어들며 사회‧경제적 충격이 클 수 있다. 과도한 자동화 중심 전략은 인간 중심 서비스의 품질 저하나 기술 격차 확대를 초래할 수도 있다.', 1, '', true, NOW(), NOW());

INSERT INTO post (member_id, category_id, type, title, content, count, thumbnail_url, status, created_at, updated_at)
VALUES (1, 3, 'news', '국제 유가 3개월 연속 상승', '국제 유가가 지정학적 긴장과 공급 감소로 인해 3개월 연속 상승세를 기록했다. 긍정적인 측면에서는 에너지 산업의 수익이 개선되고 관련 투자가 확대될 수 있다. 반면 소비자 물가 상승 압력으로 이어질 가능성이 커졌다.', 12, '', true, '2025-10-03 09:42:31', '2025-10-03 09:42:31');

INSERT INTO post (member_id, category_id, type, title, content, count, thumbnail_url, status, created_at, updated_at)
VALUES (1, 4, 'news', '삼성전자, AI 반도체 신제품 공개', '삼성전자가 차세대 AI 반도체를 공개하며 글로벌 시장 경쟁을 강화하고 있다. 기술력이 향상되면서 데이터센터 효율이 20% 이상 개선될 전망이다.', 5, '', true, '2025-10-06 14:27:12', '2025-10-06 14:27:12');

INSERT INTO post (member_id, category_id, type, title, content, count, thumbnail_url, status, created_at, updated_at)
VALUES (2, 5, 'news', '기후 변화로 전력 수급 불안 심화', '기후 변화로 인해 여름철 전력 수요가 급증하면서 전력 수급 불안이 현실화되고 있다. 정부는 재생에너지 확대와 에너지 절약 캠페인을 병행 추진 중이다.', 23, '', true, '2025-10-08 16:03:45', '2025-10-08 16:03:45');

INSERT INTO post (member_id, category_id, type, title, content, count, thumbnail_url, status, created_at, updated_at)
VALUES (1, 6, 'news', '한국은행, 기준금리 동결 결정', '한국은행이 물가와 경기 불확실성 속에 기준금리를 현 수준으로 동결했다. 시장에서는 내년 초 완화적 통화정책 전환 가능성을 주목하고 있다.', 41, '', true, '2025-10-05 11:21:58', '2025-10-05 11:21:58');

INSERT INTO post (member_id, category_id, type, title, content, count, thumbnail_url, status, created_at, updated_at)
VALUES (1, 7, 'news', '네이버, AI 번역엔진 업그레이드', '네이버가 자체 개발한 AI 번역엔진을 업그레이드해 정확도와 속도를 크게 개선했다. 글로벌 시장 진출을 위한 기술적 기반이 강화됐다.', 9, '', true, '2025-10-10 18:45:22', '2025-10-10 18:45:22');

INSERT INTO post (member_id, category_id, type, title, content, count, thumbnail_url, status, created_at, updated_at)
VALUES (2, 1, 'news', '원/달러 환율, 1400원 재돌파', '미국 금리 인상 기조 유지로 원/달러 환율이 다시 1400원을 돌파했다. 수출 기업에는 긍정적이지만 수입 물가 상승 우려가 크다.', 27, '', true, '2025-10-11 10:16:38', '2025-10-11 10:16:38');

INSERT INTO post (member_id, category_id, type, title, content, count, thumbnail_url, status, created_at, updated_at)
VALUES (1, 2, 'news', 'OpenAI, GPT-5 상용화 예고', 'OpenAI가 GPT-5 모델을 정식 상용화할 계획을 발표했다. 자연어 처리 기술이 더 정교해지며 기업용 AI 솔루션 시장이 확대될 전망이다.', 33, '', true, '2025-10-12 14:58:09', '2025-10-12 14:58:09');

INSERT INTO post (member_id, category_id, type, title, content, count, thumbnail_url, status, created_at, updated_at)
VALUES (1, 3, 'news', '탄소중립 도시 시범사업 본격화', '정부가 탄소중립 도시 구축을 위한 시범사업을 전국 5곳에서 시작했다. 친환경 교통수단 도입과 스마트에너지 관리시스템이 핵심이다.', 6, '', true, '2025-10-13 08:32:47', '2025-10-13 08:32:47');
INSERT INTO post (member_id, category_id, type, title, content, count, thumbnail_url, status, created_at, updated_at)
VALUES (1, 1, 'news', '경제 뉴스 1', 'International Monetary Fund(IMF)가 2025년 세계 경제성장률을 약 3.2%로 상향 조정했으나, 여전히 둔화세가 뚜렷하며 보호무역·정책 불확실성이 리스크로 작용하고 있다. 긍정적 측면 성장률이 하향세에 머무르긴 하지만, 예상보다는 상향돼 경제 회복 가능성에 대한 신호가 존재한다. 기술 투자(특히 AI) 같은 신규 분야가 성장 동력을 제공하며 구조전환의 기회가 될 수 있다. 부정적 측면 여전히 리스크가 크며, 특히 무역마찰·정책 불확실성 등이 투자·고용을 억제할 수 있다. 성장률이 낮은 상태가 장기간 지속되면 구조적 침체로 전환될 우려가 있다.', 0, '', true, NOW(), NOW());

INSERT INTO post (member_id, category_id, type, title, content, count, thumbnail_url, status, created_at, updated_at)
VALUES (1, 2, 'news', 'IT 뉴스', 'Tata Consultancy Services(TCS)가 12,000명 이상 감원하며 인도 아웃소싱 산업이 AI‧자동화 중심으로 구조 재편되고 있음을 나타냈다. 긍정적인 측면 기업이 AI 및 자동화에 투자하며 생산성과 효율성을 제고, 글로벌 IT 서비스 중심 경쟁력을 강화할 기회가 생긴다. 기술 재훈련·고급 역량 개발이 촉진되어 장기적으로 더 강한 인재 생태계가 구축될 수 있다. 부정적인 측면 단기적으로 많은 직원이 일자리를 잃고 중견 경력자에 대한 수요가 줄어들며 사회‧경제적 충격이 클 수 있다. 과도한 자동화 중심 전략은 인간 중심 서비스의 품질 저하나 기술 격차 확대를 초래할 수도 있다.', 1, '', true, NOW(), NOW());

INSERT INTO post (member_id, category_id, type, title, content, count, thumbnail_url, status, created_at, updated_at)
VALUES (1, 1, 'news', '국제 유가 3개월 연속 상승', '국제 유가가 지정학적 긴장과 공급 감소로 인해 3개월 연속 상승세를 기록했다. 긍정적인 측면에서는 에너지 산업의 수익이 개선되고 관련 투자가 확대될 수 있다. 반면 소비자 물가 상승 압력으로 이어질 가능성이 커졌다.', 12, '', true, '2025-10-03 09:42:31', '2025-10-03 09:42:31');

INSERT INTO post (member_id, category_id, type, title, content, count, thumbnail_url, status, created_at, updated_at)
VALUES (1, 2, 'news', '삼성전자, AI 반도체 신제품 공개', '삼성전자가 차세대 AI 반도체를 공개하며 글로벌 시장 경쟁을 강화하고 있다. 기술력이 향상되면서 데이터센터 효율이 20% 이상 개선될 전망이다.', 5, '', true, '2025-10-06 14:27:12', '2025-10-06 14:27:12');

INSERT INTO post (member_id, category_id, type, title, content, count, thumbnail_url, status, created_at, updated_at)
VALUES (2, 3, 'news', '기후 변화로 전력 수급 불안 심화', '기후 변화로 인해 여름철 전력 수요가 급증하면서 전력 수급 불안이 현실화되고 있다. 정부는 재생에너지 확대와 에너지 절약 캠페인을 병행 추진 중이다.', 23, '', true, '2025-10-08 16:03:45', '2025-10-08 16:03:45');

INSERT INTO post (member_id, category_id, type, title, content, count, thumbnail_url, status, created_at, updated_at)
VALUES (1, 1, 'news', '한국은행, 기준금리 동결 결정', '한국은행이 물가와 경기 불확실성 속에 기준금리를 현 수준으로 동결했다. 시장에서는 내년 초 완화적 통화정책 전환 가능성을 주목하고 있다.', 41, '', true, '2025-10-05 11:21:58', '2025-10-05 11:21:58');

INSERT INTO post (member_id, category_id, type, title, content, count, thumbnail_url, status, created_at, updated_at)
VALUES (1, 2, 'news', '네이버, AI 번역엔진 업그레이드', '네이버가 자체 개발한 AI 번역엔진을 업그레이드해 정확도와 속도를 크게 개선했다. 글로벌 시장 진출을 위한 기술적 기반이 강화됐다.', 9, '', true, '2025-10-10 18:45:22', '2025-10-10 18:45:22');

INSERT INTO post (member_id, category_id, type, title, content, count, thumbnail_url, status, created_at, updated_at)
VALUES (2, 1, 'news', '원/달러 환율, 1400원 재돌파', '미국 금리 인상 기조 유지로 원/달러 환율이 다시 1400원을 돌파했다. 수출 기업에는 긍정적이지만 수입 물가 상승 우려가 크다.', 27, '', true, '2025-10-11 10:16:38', '2025-10-11 10:16:38');

INSERT INTO post (member_id, category_id, type, title, content, count, thumbnail_url, status, created_at, updated_at)
VALUES (1, 2, 'news', 'OpenAI, GPT-5 상용화 예고', 'OpenAI가 GPT-5 모델을 정식 상용화할 계획을 발표했다. 자연어 처리 기술이 더 정교해지며 기업용 AI 솔루션 시장이 확대될 전망이다.', 33, '', true, '2025-10-12 14:58:09', '2025-10-12 14:58:09');

INSERT INTO post (member_id, category_id, type, title, content, count, thumbnail_url, status, created_at, updated_at)
VALUES (1, 3, 'news', '탄소중립 도시 시범사업 본격화', '정부가 탄소중립 도시 구축을 위한 시범사업을 전국 5곳에서 시작했다. 친환경 교통수단 도입과 스마트에너지 관리시스템이 핵심이다.', 6, '', true, '2025-10-13 08:32:47', '2025-10-13 08:32:47');


INSERT INTO post (member_id, category_id, type, title, content, count, thumbnail_url, status, created_at, updated_at)
VALUES
    (3, 2, 'community', '오늘 점심 뭐 드셨어요?',
     '회사 근처에 새로 생긴 국밥집 다녀왔어요! 고기도 많고 국물도 진하네요. 혹시 근처에 맛집 아시는 분 추천 좀 해주세요',
     14, '', true, '2025-10-10 12:45:22', '2025-10-10 12:45:22'),

    (3, 1, 'community', '갤럭시 S24 써보신 분 계신가요?',
     '이번에 폰 바꾸려는데 S24 카메라랑 배터리 괜찮은가요? 후기 좀 부탁드려요',
     23, '', true, '2025-10-11 09:16:38', '2025-10-11 09:16:38'),

    (4, 2, 'community', 'GPT-5 써보니까 진짜 신기하네요',
     '어제 처음 써봤는데, 진짜 사람처럼 대화하네요. 프로그래밍 코드도 척척 써주고요. 다들 써보셨나요?',
     31, '', true, '2025-10-12 14:58:09', '2025-10-12 14:58:09'),

    (4, 3, 'community', '주말 캠핑 다녀왔어요',
     '가평 근처로 캠핑 다녀왔는데 날씨도 좋고 공기도 맑았어요. 사진은 못 찍었지만 힐링 제대로 했네요',
     18, '', true, '2025-10-13 08:32:47', '2025-10-13 08:32:47');

-- COMMENT
INSERT INTO comment (member_id, post_id, parent_id, content, created_at, updated_at)
VALUES (2, 1, NULL, '좋은 글이에요', NOW(), NOW());
INSERT INTO comment (member_id, post_id, parent_id, content, created_at, updated_at)
VALUES (1, 2, NULL, '흥미롭네요', NOW(), NOW());

-- LIKE
INSERT INTO likes (member_id, post_id, created_at) VALUES (1,1, NOW());
INSERT INTO likes (member_id, post_id, created_at) VALUES (2,1, NOW());

--Bookmark
INSERT INTO bookmark (member_id, post_id, created_at) VALUES (1, 1,NOW());
INSERT INTO bookmark (member_id, post_id, created_at) VALUES (2, 1,NOW());

-- MAIL_LOG
INSERT INTO mail_log (member_id, post_id, status, retry_count, last_attempt_at, error_message, created_at)
VALUES (1, 1, 'PENDING', 0, NULL, NULL, NOW());
INSERT INTO mail_log (member_id, post_id, status, retry_count, last_attempt_at, error_message, created_at)
VALUES (2, 2, 'PENDING', 0, NULL, NULL, NOW());
