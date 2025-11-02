/**
 * 로그인 페이지 스크립트
 * - 로그인 폼 제출 시 /api/auth/login 요청
 * - JWT는 서버에서 HttpOnly 쿠키로 발급됨
 * - 로그인 성공 시 메인 페이지로 이동
 */

document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector("form");
    const emailInput = form.querySelector('input[type="email"]');
    const passwordInput = form.querySelector('input[type="password"]');

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const email = emailInput.value.trim();
        const password = passwordInput.value.trim();

        if (!email || !password) {
            alert("이메일과 비밀번호를 입력해주세요.");
            return;
        }

        try {
            const response = await fetch("/api/auth/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                credentials: "include", //  HttpOnly 쿠키 자동 포함
                body: JSON.stringify({ email, password }),
            });

            const result = await response.json();

            if (response.ok && result.success) {
                alert("로그인 성공!");
                window.location.href = "/"; // 메인 페이지로 이동
            } else {
                alert(result.message || "로그인에 실패했습니다.");
            }

        } catch (error) {
            console.error("로그인 요청 실패:", error);
            alert("서버와의 연결에 문제가 발생했습니다.");
        }
    });
});
