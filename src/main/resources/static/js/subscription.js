document.addEventListener('DOMContentLoaded', () => {

  // 별 click
  document.querySelectorAll('[data-role="star"]').forEach(star => {
    star.addEventListener('click', async (e) => {
      e.preventDefault();
      e.stopPropagation();

      if(!IS_LOGGED_IN) {
        location.href = '/login';
        return;
      }

      const categoryId = star.getAttribute('data-category-id');

      try {
        const res = await fetch(`/api/subscription/toggle-category?categoryId=${categoryId}`, {
          method: "POST"
        });

        if(res.status === 401) {
          location.href = '/login';
          return;
        }

        const data = await res.json();
        const active = data.active;

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
      if(!IS_LOGGED_IN){
        location.href = '/login';
        return;
      }

      try{
        const res = await fetch('/api/subscription/subscribe-all', { method:"POST" });
        if(res.status === 401){
          location.href = '/login';
          return;
        }

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