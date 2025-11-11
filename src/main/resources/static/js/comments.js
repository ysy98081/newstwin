(() => {
  const postId = document.getElementById('likeBtn').dataset.postId;
  const listEl = document.getElementById('commentList');
  const inputEl = document.getElementById('commentInput');
  const submitEl = document.getElementById('commentSubmit');
  const sentinel = document.getElementById('commentSentinel');

  let page = 0;
  const size = 20;
  let loading = false;
  let done = false;

  // 401/403/302 → 로그인 이동
  const ensureAuthOrRedirect = (res) => {
    if (res.status === 401 || res.status === 403) {
      window.location.href = "/login";
      return false;
    }
    return true;
  };

  // HTML escape
  const esc = (s) => {
    if (!s) return "";
    return s.replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;');
  };

  // 루트 + 자식 렌더
  function renderRoot(root) {
    const li = document.createElement('li');
    li.className = 'mb-3';
    li.innerHTML = `
      <div class="p-2 d-flex">
         <img src="${root.profileImage ? root.profileImage : DEFAULT_PROFILE_URL}"
         class="rounded-circle me-2"
         style="width:32px;height:32px;object-fit:cover;">
         <div>
          <div class="small text-secondary">${esc(root.authorName)} · ${root.createdAt}</div>
          <div class="mt-1">${esc(root.content)}</div>
          <div class="mt-1 d-flex gap-2">
          <button class="btn btn-link btn-sm p-0 reply-btn">답글</button>
          ${root.mine && !root.deleted ? `<button class="btn btn-link btn-sm p-0 text-danger delete-btn" data-id="${root.id}">삭제</button>` : ''}
         </div>
        </div>
       </div>
         <div class="reply-box d-none mt-2 ms-5">
            <div class="d-flex align-items-start">
              <img src="${root.profileImage ? root.profileImage : DEFAULT_PROFILE_URL}" 
                class="rounded-circle me-2" style="width:32px;height:32px;object-fit:cover;">
              <textarea class="form-control reply-input" rows="2" placeholder="답글을 입력하세요"></textarea>
            </div>
            <div class="d-flex justify-content-end mt-2">
              <button class="btn btn-primary btn-sm reply-submit">등록</button>
            </div>
         </div>
        </div>
      <ul class="list-unstyled ms-4 border-start ps-3 children"></ul>
    `;

    // 자식들 렌더
    const childrenUl = li.querySelector('.children');
    (root.children || []).forEach(ch => {
      const cli = document.createElement('li');
      cli.className = 'mb-2';
      cli.innerHTML = `
        <div class="p-2 d-flex">
          <img src="${ch.profileImage ? ch.profileImage : DEFAULT_PROFILE_URL}"
             class="rounded-circle me-2"
             style="width:28px;height:28px;object-fit:cover;">
          <div>
            <div class="small text-secondary">${esc(ch.authorName)} · ${ch.createdAt}</div>
            <div class="mt-1">${esc(ch.content)}</div>
            <div class="mt-1 d-flex gap-2">
            ${ch.mine && !ch.deleted ? `<button class="btn btn-link btn-sm p-0 text-danger delete-btn" data-id="${ch.id}">삭제</button>` : ''}
            </div>
          </div>
        </div> 
      `;
      // 자식 삭제
      const childDeleteBtn = cli.querySelector('.delete-btn');
      if (childDeleteBtn) {
        childDeleteBtn.addEventListener('click', async (e) => {
          const id = e.currentTarget.dataset.id;
          if (!confirm('삭제하시겠습니까?')) return;
          const res = await fetch(`/api/posts/comments/${id}`, { method: 'DELETE' });
          if (!ensureAuthOrRedirect(res)) return;
          if (res.ok || res.status === 204) {
            resetAndReload();
          } else {
            alert('삭제 실패');
          }
        });
      }

      childrenUl.appendChild(cli);
    });


    // 루트 댓글 삭제
    const rootDeleteBtn = li.querySelector('.delete-btn');
    if (rootDeleteBtn) {
      rootDeleteBtn.addEventListener('click', async (e) => {
        const id = e.currentTarget.dataset.id;
        if (!confirm('삭제하시겠습니까?')) return;
        const res = await fetch(`/api/posts/comments/${id}`, { method: 'DELETE' });
        if (!ensureAuthOrRedirect(res)) return;
        if (res.ok || res.status === 204) {
          resetAndReload();
        } else {
          alert('삭제 실패');
        }
      });
    }

    // 답글 폼 토글
    li.querySelector('.reply-btn').addEventListener('click', () => {
      li.querySelector('.reply-box').classList.toggle('d-none');
    });

    // 답글 등록
    li.querySelector('.reply-submit').addEventListener('click', async () => {
      const ta = li.querySelector('.reply-input');
      const content = ta.value.trim();
      if (!content) return;
      const res = await fetch(`/api/posts/${postId}/comments`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ content, parentId: root.id })
      });
      if (!ensureAuthOrRedirect(res)) return;
      if (!res.ok) { alert('등록 실패'); return; }
      ta.value = '';
      resetAndReload();
    });

    listEl.appendChild(li);
  }

  async function loadNext() {
    if (loading || done) return;
    loading = true;

    try {
      const res = await fetch(`/api/posts/${postId}/comments?page=${page}&size=${size}`);
      if (!res.ok) return;
      const data = await res.json(); // { items:[root...], hasNext, nextPage }

      if (page === 0) {
        document.getElementById('commentTotal').textContent = data.totalCount;
      }

      (data.items || []).forEach(renderRoot);

      if (data.hasNext) {
        page = data.nextPage;
      } else {
        done = true;
        sentinel.textContent = '댓글을 모두 불러왔습니다.';
      }
    } finally {
      loading = false;
    }
  }

  function resetAndReload() {
    // 간단히 전체 새로고침 or 아래처럼 리로드
    page = 0;
    done = false;
    listEl.innerHTML = '';
    sentinel.textContent = '더 불러오는 중…';
    loadNext();
  }

  // 루트 댓글 등록
  submitEl.addEventListener('click', async () => {
    const content = inputEl.value.trim();
    if (!content) return;
    const res = await fetch(`/api/posts/${postId}/comments`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ content })
    });
    if (!ensureAuthOrRedirect(res)) return;
    if (!res.ok) { alert('등록 실패'); return; }
    inputEl.value = '';
    resetAndReload();
  });

  // 인터섹션 옵저버 (무한스크롤)
  const io = new IntersectionObserver(entries => {
    entries.forEach(e => {
      if (e.isIntersecting) {
        loadNext();
      }
    });
  });
  io.observe(sentinel);

  // 초기 로드
  document.addEventListener('DOMContentLoaded', () => loadNext());
})();