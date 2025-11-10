(() => {

  const ensureAuthOrRedirect = (res) => {
    if(res.status === 401 || res.status === 403) {
      window.location.href = "/login";
      return false;
    }
    return true;
  };


document.addEventListener('DOMContentLoaded', () => {

  // 별 click
  document.querySelectorAll('[data-role="star"]').forEach(star => {
    star.addEventListener('click', async (e) => {
      e.preventDefault();
      e.stopPropagation();

      if(!IS_LOGGED_IN) {
        window.location.href = '/login';
        return;
      }

      if (!MEMBER_RECEIVE_EMAIL) {  // ← 템플릿에서 전역으로 박아주는 값
        alert("뉴스레터 수신이 꺼져 있어 구독 변경을 할 수 없습니다.");
        return;
      }

      const categoryId = star.getAttribute('data-category-id');

      try {
        const res = await fetch(`/api/subscription/toggle-category?categoryId=${categoryId}`, {
          method: "POST"
        });

        if(!ensureAuthOrRedirect(res)) return;

        const { active } = await res.json();

        star.classList.remove('bi-star','text-secondary','bi-star-fill','text-warning');
        if(active){
          star.classList.add('bi-star-fill','text-warning');
        } else {
          star.classList.add('bi-star','text-secondary');
        }
      } catch(e){
        alert("구독 변경 중 오류가 발생했습니다.");
      }
    });
  });

  // 전체 구독하기
  const subscribeAllBtn = document.getElementById("subscribeAllBtn");
  if(subscribeAllBtn){
    subscribeAllBtn.addEventListener('click', async (e) => {
      e.preventDefault();

      if(!IS_LOGGED_IN) {
        window.location.href = '/login';
        return;
      }

      if (!MEMBER_RECEIVE_EMAIL) {
        alert("전체 뉴스레터 수신이 꺼져 있어 전체 구독 실행 불가합니다.");
        return;
      }

      try{
        const res = await fetch('/api/subscription/subscribe-all', { method:"POST" });
        if(!ensureAuthOrRedirect(res)) return;

        document.querySelectorAll('[data-role="star"]').forEach(star => {
          star.classList.remove('bi-star','text-secondary');
          star.classList.add('bi-star-fill','text-warning');
        });

      } catch(e){
        alert("전체 구독 처리 중 오류");
      }
    });
  }
});
})();