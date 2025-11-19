document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("loginForm");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value.trim();

    try {
      const response = await csrfFetch("/admin/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ email, password }),
      });

      const result = await response.json();

      if (response.ok && result.success) {
        alert(result.message);
        window.location.href = "/admin";
      } else {
        alert(result.message || "관리자 로그인 실패");
      }
    } catch (err) {
      console.error("로그인 오류:", err);
      alert("서버 오류가 발생했습니다.");
    }
  });
});
