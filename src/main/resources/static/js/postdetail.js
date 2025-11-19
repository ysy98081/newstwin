(() => {
  const postId = document.getElementById("likeBtn").dataset.postId;

  const likeBtn = document.getElementById("likeBtn");
  const heartIcon = document.getElementById("heartIcon");
  const likeCountEl = document.getElementById("likeCount");

  const bookmarkBtn = document.getElementById("bookmarkBtn");
  const bookmarkIcon = document.getElementById("bookmarkIcon");

  const sideLike = document.getElementById("sideLike");
  const sideLikeCount = document.getElementById("sideLikeCount");
  const sideBookmark = document.getElementById("sideBookmark");
  const sideComment = document.getElementById("sideComment");
  const sideCommentCount = document.getElementById("sideCommentCount");

  const ensureAuthOrRedirect = (res) => {
    if (res.status === 401 || res.status === 403) {
      window.location.href = "/login";
      return false;
    }
    return true;
  };

  /* 댓글 수를 사이드바와 동기화 (comments.js에서 사용) */
  window.updateSideCommentCount = function (count) {
    if (sideCommentCount) {
      sideCommentCount.textContent = count;
    }
  };

  /* 좋아요 UI */
  const updateLikeUI = (liked, count) => {
    heartIcon.textContent = liked ? "❤️" : "🤍";
    likeBtn.dataset.liked = liked;
    likeCountEl.textContent = count;
    sideLike.querySelector(".emoji").textContent = liked ? "❤️" : "🤍";
    sideLikeCount.textContent = count;
  };

  /* 북마크 UI */
  const updateBookmarkUI = (bookmarked) => {
    bookmarkIcon.className = bookmarked ? "bi bi-bookmark-fill" : "bi bi-bookmark";
    bookmarkBtn.dataset.bookmarked = bookmarked;

    sideBookmark.querySelector("i").className =
        bookmarked ? "bi bi-bookmark-fill text-primary" : "bi bi-bookmark";
  };

  document.addEventListener("DOMContentLoaded", async () => {
    /* 댓글 Total 불러오기 */
    const resCommentCount = await fetch(`/api/posts/${postId}/comments?page=0&size=1`);
    if (resCommentCount.ok) {
      const data = await resCommentCount.json();
      const total = data.totalCount || 0;

      const ct = document.getElementById("commentTotal");
      if (ct) ct.textContent = total;

      window.updateSideCommentCount(total);
    }

    /* 좋아요/북마크 불러오기 */
    const [resCount, resLike, resBookmark] = await Promise.all([
      fetch(`/api/posts/${postId}/like/count`),
      fetch(`/api/posts/${postId}/like`),
      fetch(`/api/posts/${postId}/bookmark`)
    ]);

    if (resCount.ok) {
      const { likeCount } = await resCount.json();
      likeCountEl.textContent = likeCount;
      sideLikeCount.textContent = likeCount;
    }
    if (resLike.ok) {
      const { liked, likeCount } = await resLike.json();
      updateLikeUI(liked, likeCount);
    }
    if (resBookmark.ok) {
      const { bookmarked } = await resBookmark.json();
      updateBookmarkUI(bookmarked);
    }
  });

  /* 좋아요 버튼 */
  likeBtn.addEventListener("click", async () => {
    const res = await fetch(`/api/posts/${postId}/like`, { method: "POST" });
    if (!ensureAuthOrRedirect(res)) return;

    const { liked, likeCount } = await res.json();
    updateLikeUI(liked, likeCount);
  });

  sideLike.addEventListener("click", () => likeBtn.click());

  /* 북마크 버튼 */
  bookmarkBtn.addEventListener("click", async () => {
    const res = await fetch(`/api/posts/${postId}/bookmark`, { method: "POST" });
    if (!ensureAuthOrRedirect(res)) return;

    const { bookmarked } = await res.json();
    updateBookmarkUI(bookmarked);
  });

  sideBookmark.addEventListener("click", () => bookmarkBtn.click());

  /* 사이드 댓글 버튼 → 댓글 영역으로 스크롤 */
  if (sideComment) {
    sideComment.addEventListener("click", () => {
      const section = document.querySelector(".comment-section");
      if (section) {
        section.scrollIntoView({ behavior: "smooth", block: "start" });
      }
    });
  }

  /* 공유 메뉴 (본문 아래) */
  const shareBtn = document.getElementById("shareBtn");
  const shareMenu = document.getElementById("shareMenu");
  const reactionBar = document.querySelector(".reaction-bar");

  // 공통 공유 동작 (본문 + 오른쪽 액션바에서 같이 사용)
  const handleShareCopy = async (e) => {
    if (e && e.preventDefault) e.preventDefault();

    const url = window.location.href;
    try {
      await navigator.clipboard.writeText(url);
      alert("주소가 복사되었습니다.");
    } catch (err) {
      alert("복사에 실패했습니다.");
    }
    if (shareMenu) {
      shareMenu.classList.add("d-none");
    }
  };

  const handleShareSNS = async (e) => {
    if (e && e.preventDefault) e.preventDefault();

    const url = window.location.href;
    const title = document.title;

    if (navigator.share) {
      try {
        await navigator.share({ title, url });
      } catch (err) {
        // 사용자가 취소한 경우 등은 무시
      }
    } else {
      window.open(
          `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(
              url
          )}&quote=${encodeURIComponent(title)}`,
          "_blank",
          "width=600,height=500"
      );
    }
    if (shareMenu) {
      shareMenu.classList.add("d-none");
    }
  };

  // 본문 하단 공유 버튼 위치 계산 + 열기/닫기
  if (shareBtn && shareMenu && reactionBar) {
    shareBtn.addEventListener("click", (e) => {
      e.stopPropagation();

      const rect = shareBtn.getBoundingClientRect();
      const barRect = reactionBar.getBoundingClientRect();

      // reaction-bar 기준 좌표로 변환
      shareMenu.style.top = `${rect.bottom - barRect.top + 8}px`;
      shareMenu.style.left = `${rect.left - barRect.left}px`;

      shareMenu.classList.toggle("d-none");
    });

    // 바깥 클릭하면 닫힘
    document.addEventListener("click", (e) => {
      if (
          !shareMenu.classList.contains("d-none") &&
          !shareMenu.contains(e.target) &&
          !shareBtn.contains(e.target)
      ) {
        shareMenu.classList.add("d-none");
      }
    });
  }

  // 공유 메뉴 항목들(본문 + 사이드 액션바 모두)에 공통 핸들러 연결
  document.querySelectorAll(".share-copy").forEach((el) => {
    el.addEventListener("click", handleShareCopy);
  });
  document.querySelectorAll(".share-sns").forEach((el) => {
    el.addEventListener("click", handleShareSNS);
  });
})();