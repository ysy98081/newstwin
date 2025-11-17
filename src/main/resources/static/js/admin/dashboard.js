document.addEventListener("DOMContentLoaded", () => {
  const chartOptions = {
    responsive: true,
    plugins: { legend: { display: false } },
    scales: { y: { beginAtZero: true } }
  };

  fetch("/admin/dashboard/monthly-counts?year=2025")
  .then(res => res.json())
  .then(data => {
    const months = data.months;

    new Chart(document.getElementById("chartUsers"), {
      type: "line",
      data: { labels: months, datasets: [{ label: "회원 수", data: data.userCounts, borderColor: "blue" }] },
      options: chartOptions
    });

    new Chart(document.getElementById("chartPosts"), {
      type: "bar",
      data: { labels: months, datasets: [{ label: "게시글", data: data.postCounts, backgroundColor: "green" }] },
      options: chartOptions
    });

    new Chart(document.getElementById("chartUserMails"), {
      type: "line",
      data: { labels: months, datasets: [{ label: "메일구독 수", data: data.userMailCount, borderColor: "orange" }] },
      options: chartOptions
    });

    new Chart(document.getElementById("chartMails"), {
      type: "bar",
      data: { labels: months, datasets: [{ label: "메일 발송", data: data.mailCounts, backgroundColor: "red" }] },
      options: chartOptions
    });
  });
});



// 회원 관리
async function toggleStatus(el, type) {
  const memberId = el.getAttribute("data-user-id");
  const confirmChange = confirm("상태를 변경하시겠습니까?");
  if (!confirmChange) return;

  try {
    let response, data;

    if(type === 'member') {
      response = await fetch(`/admin/users/${memberId}/status`, { method: 'PATCH' });
      data = await response.json();
      el.textContent = data.isActive ? '활성' : '비활성';
      el.className = `badge px-3 py-2 cursor-pointer ${data.isActive ? 'bg-success' : 'bg-danger'}`;
    }
    else if (type === 'receiveEmail') {
      // 메일 수신/거부 토글
      response = await fetch(`/admin/users/${memberId}/receive`, { method: 'PATCH' });
      data = await response.json();
      el.textContent = data.receiveEmail ? '수신' : '거부';
      el.className = `badge px-3 py-2 cursor-pointer ${data.receiveEmail ? 'bg-success' : 'bg-secondary'}`;
    }
    else if (type === 'subscription') {
      const rawIds = el.getAttribute("data-category-ids");
      if (!rawIds || rawIds.trim() === "") {
        alert("구독 가능한 카테고리가 없습니다.");
        return;
      }
      const categoryIds = rawIds.split(',').map(id => Number(id)).filter(id => !isNaN(id));
      if (categoryIds.length === 0) {
        alert("유효한 카테고리가 없습니다.");
        return;
      }
      response = await fetch(`/admin/users/${memberId}/subscriptions`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(categoryIds)
      });
      if (!response.ok) throw new Error("서버 응답 오류");
      data = await response.json();
      el.textContent = data.subscriptionStatus;
      el.className = `badge px-3 py-2 cursor-pointer ${data.subscriptionStatus === '구독중' ? 'bg-success' : 'bg-secondary'}`;

      const categoryCell = el.closest("tr").querySelector("td:nth-child(4)");
      categoryCell.textContent = data.categories?.join(', ') || '';
    }
  } catch (err) {
    console.error(err);
    alert("상태 변경에 실패했습니다.");
  }
}

/**
 * 공용 필터 함수
 * @param {string} selectId - 셀렉트 박스의 id
 * @param {string} paramName - URL 파라미터 이름 (예: role, type 등)
 * @param {string} basePath - 요청할 경로 (예: /admin/users)
 */
function filterBy(selectId, paramName, basePath) {
  const value = document.getElementById(selectId).value;
  const url = new URL(window.location.origin + basePath);

  url.searchParams.set("page", 0);
  url.searchParams.set("size", 10);

  if (value && value.trim() !== "") {
    url.searchParams.set(paramName, value);
  } else {
    url.searchParams.delete(paramName);
  }
  window.location.href = url.toString();
}

function postToggleStatus(element) {
  const postId = element.getAttribute("data-post-id");
  const currentStatus = element.textContent.trim();
  const newStatus = currentStatus === "활성" ? "비활성" : "활성";
  const confirmChange = confirm(`게시글을 "${newStatus}" 상태로 변경하시겠습니까?`);

  if (!confirmChange) {
    return;
  }

  fetch(`/admin/posts/${postId}/status`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json"
    }
  })
  .then(response => {
    if (!response.ok) {
      throw new Error("서버 응답 오류");
    }
    return response.json();
  })
  .then(data => {
    if (data.isActive) {
      element.classList.remove("bg-danger");
      element.classList.add("bg-success");
      element.textContent = "활성";
    } else {
      element.classList.remove("bg-success");
      element.classList.add("bg-danger");
      element.textContent = "비활성";
    }

    alert(`게시글 상태가 "${data.isActive ? '활성' : '비활성'}"으로 변경되었습니다.`);
  })
  .catch(error => {
    console.error("상태 변경 실패:", error);
    alert("상태 변경 중 오류가 발생했습니다.");
  });
}

// 메일
function mailToggleStatus(badge) {
  if (!badge || badge.textContent.trim() !== '재전송') return;

  const mailId = badge.dataset.mailId;
  const confirmChange = confirm('메일을 실제로 재전송 하시겠습니까?');
  if (!confirmChange) return;

  fetch('/admin/mails/resend', {
    method: 'POST',
    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
    body: `mailId=${encodeURIComponent(mailId)}`
  })
  .then(response => {
    if (!response.ok) throw new Error('재전송 실패');
    return response.text();
  })
  .then(msg => {
    alert(msg);
    badge.textContent = '성공';
    badge.classList.remove('bg-danger');
    badge.classList.add('bg-success');
  })
  .catch(error => {
    alert(error.message);
    console.error(error);
  });
}


//댓글 관리
async function commentToggleStatus(el) {
  const commentId = el.getAttribute('data-comment-id');
  const confirmed = confirm("상태를 변경하시겠습니까?");
  if (!confirmed) return;
  const res = await fetch(`/admin/comments/${commentId}/status`, {
    method: 'POST'
  });
  if (res.ok) {
    alert('상태가 변경되었습니다.');
    location.reload();
  } else {
    alert('상태 변경 실패');
  }
}

//로그아웃
async function logoutAdmin() {
  if (!confirm("로그아웃 하시겠습니까?")) return;

  await fetch("/admin/logout", {
    method: "POST",
    credentials: "include"
  });

  alert("로그아웃 되었습니다.");
  window.location.href = "/admin/login";
}
