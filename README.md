# 📰 NewsTwin — AI 기반 자동 경제 뉴스 분석 플랫폼

![](images/logo.PNG)

**NewsTwin은 경제 뉴스를 자동 수집·분석하여, 사용자가 선택한 카테고리에 맞춰  
매일 아침 개인화된 이메일 뉴스레터를 제공하는 AI 기반 자동화 플랫폼입니다.**  
Alan AI → ChatGPT 기반으로 뉴스의 핵심 요약, 긍정/부정 분석을 수행하며  
Spring Boot + PostgreSQL 기반으로 전체 파이프라인이 자동 운영됩니다.

---

# 👥 Team White Donuts

**White Donuts 팀은 AI 자동화 시스템, 웹 개발, UI 설계 등 전체 기능을 협업하여 구현한 팀입니다.**

## 👨‍💻 팀원 소개

| 이름      | 담당 기능                        |
|----------|----------------------------------|
| 최보윤     | 회원관리 / 마이페이지 / AI 파이프라인 |
| 김재영     | 게시글·피드 / 캐싱 / 용어사전 / 필터링 |
| 최애정     | 관리자(Admin) / 댓글 / 이메일 발송·구독해지 |

---

# 📄 프로젝트 문서 (Notion)

👉 **전체 문서 보기:**  
https://www.notion.so/oreumi/5-NT-NewsTwin-299ebaa8982b80b6b9b6e7ce37a89583

---

# 🚀 Milestones (핵심 성과 요약)

| 단계 | 기간 | 달성 내용 |
|------|--------|-----------|
| **기획 & 설계** | 10/28 ~ 10/30 | ERD, 기능명세, 화면설계(Figma), 브랜치전략 설정 |
| **핵심 기능 구현** | 10/31 ~ 11/07 | 회원·구독·게시글 CRUD, Alan→ChatGPT 파이프라인, 이메일 자동발송 |
| **기능 확장 & UX 개선** | 11/08 ~ 11/14 | 좋아요·북마크·댓글·공유하기, 경제용어 툴팁 기능 |
| **테스트 & 배포** | 11/15 ~ 11/18 | EC2/RDS 배포, CI/CD, 보안환경, 실서버 스케줄러 테스트 |
| **발표 준비** | 11/19 ~ 11/20 | 발표자료 제작, 시연 영상, 데모 시나리오 구성 |
| **최종 발표** | 11/21 | 프로젝트 발표 및 회고 |

---

# ⚙️ 기능 요약 (Feature Summary)

- **회원/인증**
  - JWT 로그인, 회원가입, 소셜 로그인(OAuth2)
  - 마이페이지(정보 수정, 카테고리 설정, 구독수신여부)

- **뉴스 자동 분석 파이프라인**
  - Alan AI: 카테고리별 주요 뉴스 5개 수집 (09시 Scheduler)
  - ChatGPT: 긍정/부정 요인 분석 + 요약 + 키워드 추출
  - 중복 키워드 필터링 & 직렬 분석 구조

- **이메일 뉴스레터**
  - 사용자별 맞춤형 뉴스 구성
  - @Async 비동기 발송
  - UUID 기반 구독 해지 (로그인 불필요)

- **게시글/피드 기능**
  - 카테고리별 뉴스 조회
  - 좋아요 · 북마크 · 댓글 · 대댓글
  - 경제용어 자동 툴팁 (Excel 캐싱)

- **관리자(Admin)**
  - 게시글 관리
  - 회원 관리
  - 메일 로그 조회

---

# 🗂️ 디렉토리 구조

<pre>
templates
  │  index.html
  │
  ├─admin
  │      comments.html
  │      dashboard.html
  │      login.html
  │      mails-contents.html
  │      mails.html
  │      posts-contents.html
  │      posts.html
  │      users.html
  │
  ├─auth
  │      login.html
  │      signup.html
  │      verify-info.html
  │      verify-result.html
  │
  ├─board
  │      detail.html
  │      form.html
  │      list.html
  │
  ├─fragment
  │      modal.html
  │
  ├─layout
  │      footer.html
  │      header.html
  │      header_admin.html
  │      pagination.html
  │      sidebar.html
  │
  ├─mypage
  │      bookmarks.html
  │      edit.html
  │      main.html
  │      subscription.html
  │      withdraw.html
  │
  └─news
          detail.html
          list.html
</pre>

---

## 🛠️ Tech Stack

<p align="center">
  <img src="https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/SpringSecurity-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white"/>
  <img src="https://img.shields.io/badge/JPA-Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white"/>

  <img src="https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white"/>

  <img src="https://img.shields.io/badge/OpenAI-ChatGPT-74AA9C?style=for-the-badge&logo=openai&logoColor=white"/>
  <img src="https://img.shields.io/badge/AlanAI-000000?style=for-the-badge&logoColor=white"/>

  <img src="https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white"/>
  <img src="https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white"/>
  <img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=white"/>

  <img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white"/>
  <img src="https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white"/>
</p>

---

## 🏅 Stats

<p align="center">
  <img src="https://github-readme-stats.vercel.app/api/top-langs/?username=ysy98081&layout=compact&bg_color=180,12a3d3,00000000&title_color=000000&text_color=000000"/>
</p>
