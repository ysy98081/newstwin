-- MEMBER
INSERT INTO member (member_name, email, password, role, created_at, updated_at)
VALUES ('master', 'master@test.com', 'pass', 'ROLE_ADMIN', NOW(), NOW());
INSERT INTO member (member_name, email, password, role, created_at, updated_at)
VALUES ('tester', 'tester@test.com', 'pass', 'ROLE_USER', NOW(), NOW());

-- CATEGORY
INSERT INTO category (category_name) VALUES ('경제');
INSERT INTO category (category_name) VALUES ('IT');
INSERT INTO category (category_name) VALUES ('정치');

-- USER_SUBSCRIPTION
INSERT INTO user_subscription (member_id, category_id, is_active, created_at, updated_at)
VALUES (1, 1, TRUE, NOW(), NOW());
INSERT INTO user_subscription (member_id, category_id, is_active, created_at, updated_at)
VALUES (2, 2, TRUE, NOW(), NOW());

-- POST
INSERT INTO post (member_id, category_id, type, title, content, thumbnail_url, created_at, updated_at)
VALUES (1, 1, 'news', '경제 뉴스 1', 'International Monetary Fund(IMF)가 2025년 세계 경제성장률을 약 3.2%로 상향 조정했으나, 여전히 둔화세가 뚜렷하며 보호무역·정책 불확실성이 리스크로 작용하고 있다.
긍정적 측면
성장률이 하향세에 머무르긴 하지만, 예상보다는 상향돼 경제 회복 가능성에 대한 신호가 존재한다.
기술 투자(특히 AI) 같은 신규 분야가 성장 동력을 제공하며 구조전환의 기회가 될 수 있다.
부정적 측면
여전히 리스크가 크며, 특히 무역마찰·정책 불확실성 등이 투자·고용을 억제할 수 있다.
성장률이 낮은 상태가 장기간 지속되면 구조적 침체로 전환될 우려가 있다.', '', NOW(), NOW());
INSERT INTO post (member_id, category_id, type, title, content, thumbnail_url, created_at, updated_at)
VALUES (1, 2, 'news', 'IT 뉴스', 'Tata Consultancy Services(TCS)가 12,000명 이상 감원하며 인도 아웃소싱 산업이 AI‧자동화 중심으로 구조 재편되고 있음을 나타냈다.
긍정적인 측면
기업이 AI 및 자동화에 투자하며 생산성과 효율성을 제고, 글로벌 IT 서비스 중심 경쟁력을 강화할 기회가 생긴다.
기술 재훈련·고급 역량 개발이 촉진되어 장기적으로 더 강한 인재 생태계가 구축될 수 있다.
부정적인 측면
단기적으로 많은 직원이 일자리를 잃고 중견 경력자에 대한 수요가 줄어들며 사회‧경제적 충격이 클 수 있다.
과도한 자동화 중심 전략은 인간 중심 서비스의 품질 저하나 기술 격차 확대를 초래할 수도 있다.', '', NOW(), NOW());

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