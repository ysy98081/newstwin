(() => {
  const likeBtn = document.getElementById('likeBtn');
  const heartIcon = document.getElementById('heartIcon');
  const likeCountEl = document.getElementById('likeCount');
  const postId = likeBtn.dataset.postId;

  const bookmarkBtn = document.getElementById('bookmarkBtn');
  const bookmarkIcon = document.getElementById('bookmarkIcon');

  document.addEventListener("DOMContentLoaded", () => {
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    tooltipTriggerList.forEach(el => new bootstrap.Tooltip(el));
  });

  const ensureAuthOrRedirect = (res) => {
    if(res.status === 401 || res.status === 403){
      window.location.href = "/login";
      return false;
    }
    return true;
  };

  document.addEventListener("DOMContentLoaded", () => {
    const deleteBtn = document.getElementById("deleteBtn");
    if (!deleteBtn) return;

    deleteBtn.addEventListener("click", async () => {
      const postId = deleteBtn.dataset.postId;
      const confirmed = confirm("정말 이 글을 삭제하시겠습니까?");
      if (!confirmed) return;

      try {
        const res = await fetch(`/board/delete/${postId}`, { method: 'DELETE' });
        if (res.ok) {
          alert("게시글이 삭제되었습니다.");
          window.location.href = "/board"; // 목록 페이지로 이동
        } else if (res.status === 401) {
          alert("로그인이 필요합니다.");
          window.location.href = "/login";
        } else {
          alert("삭제 중 오류가 발생했습니다.");
        }
      } catch (e) {
        console.error(e);
        alert("삭제 요청 실패");
      }
    });
  });


  document.addEventListener("DOMContentLoaded", async () => {
    //좋아요 count는 무조건 공개
    try {
      const resCount = await fetch(`/api/posts/${postId}/like/count`);
      if (resCount.ok) {
        const { likeCount } = await resCount.json();
        likeCountEl.textContent = likeCount;
      }
    } catch (e) {}

    //Like 상태는 가능할때만
    try {
      const resLike = await fetch(`/api/posts/${postId}/like`);
      if (resLike.ok) {
        const { liked } = await resLike.json();
        heartIcon.textContent = liked ? '❤️' : '🤍';
        likeBtn.dataset.liked = liked;
      }
    } catch (e) {}

    //북마크 상태도 가능할때만
    try {
      const resBookmark = await fetch(`/api/posts/${postId}/bookmark`);
      if (resBookmark.ok) {
        const { bookmarked } = await resBookmark.json();
        bookmarkIcon.className = bookmarked ? 'bi bi-bookmark-fill' : 'bi bi-bookmark';
        bookmarkBtn.dataset.bookmarked = bookmarked;
      }
    } catch (e) {}
  });

  //좋아요 토글
  likeBtn.addEventListener('click', async () => {
  likeBtn.disabled = true;
  try {
    const res = await fetch(`/api/posts/${postId}/like`, { method: 'POST' });
    if(!ensureAuthOrRedirect(res)) return;
    const { liked, likeCount } = await res.json();
    heartIcon.textContent = liked ? '❤️' : '🤍';
    likeCountEl.textContent = likeCount;
    likeBtn.dataset.liked = liked;
  } catch (e) {
    alert('좋아요 실패');
  } finally {
    likeBtn.disabled = false;
  }
  });


  //북마크 토글
  bookmarkBtn.addEventListener('click', async () => {
  bookmarkBtn.disabled = true;
  try {
  const res = await fetch(`/api/posts/${postId}/bookmark`, { method: 'POST' });
  if(!ensureAuthOrRedirect(res)) return;
  const { bookmarked } = await res.json();
  bookmarkIcon.className = bookmarked ? 'bi bi-bookmark-fill' : 'bi bi-bookmark';
  bookmarkBtn.dataset.bookmarked = bookmarked;
} catch (e) {
  alert('북마크 실패');
} finally {
  bookmarkBtn.disabled = false;
}
});

})();