document.addEventListener("DOMContentLoaded", () => {
  const chartOptions = {
    responsive: true,
    plugins: { legend: { display: false } },
    scales: { y: { beginAtZero: true } }
  };

  new Chart(document.getElementById("chartUsers"), {
    type: "line",
    data: {
      labels: ["1월", "2월", "3월", "4월", "5월"],
      datasets: [{ label: "회원 수", data: [100, 110, 120, 125, 128], borderColor: "blue" }]
    },
    options: chartOptions
  });

  new Chart(document.getElementById("chartPosts"), {
    type: "bar",
    data: {
      labels: ["1월", "2월", "3월", "4월", "5월"],
      datasets: [{ label: "게시글", data: [400, 450, 480, 500, 532], backgroundColor: "green" }]
    },
    options: chartOptions
  });

  new Chart(document.getElementById("chartSubscribers"), {
    type: "line",
    data: {
      labels: ["1월", "2월", "3월", "4월", "5월"],
      datasets: [{ label: "구독자", data: [200, 230, 260, 290, 320], borderColor: "orange" }]
    },
    options: chartOptions
  });

  new Chart(document.getElementById("chartMails"), {
    type: "bar",
    data: {
      labels: ["1월", "2월", "3월", "4월", "5월"],
      datasets: [{ label: "메일 발송", data: [300, 340, 360, 390, 412], backgroundColor: "red" }]
    },
    options: chartOptions
  });
});




let clickedBadge = null;

const statusModal = document.getElementById("statusModal");
const statusText = document.getElementById("statusModalText");

// 모달 열릴 때 클릭한 배지 저장
statusModal.addEventListener("show.bs.modal", (event) => {
  clickedBadge = event.relatedTarget;
  const user = clickedBadge.getAttribute("data-user");
  statusText.textContent = `${user}님의 상태를 변경하시겠습니까?`;
});

// 예 버튼 클릭 시 상태 토글
document.getElementById("confirmStatusBtn").addEventListener("click", () => {
  if (!clickedBadge) return;

  const toggleMap = { "활성": "비활성", "비활성": "활성", "정상": "탈퇴", "탈퇴": "정상" };
  clickedBadge.textContent = toggleMap[clickedBadge.textContent] || clickedBadge.textContent;

  clickedBadge.classList.toggle("bg-success");
  clickedBadge.classList.toggle("bg-secondary");

  alert(`${clickedBadge.getAttribute("data-user")}님의 상태가 변경되었습니다.`);

  bootstrap.Modal.getInstance(statusModal).hide();
});
