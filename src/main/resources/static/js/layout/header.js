/**
 *  헤더
 * - 로그아웃시
 */

document.addEventListener("DOMContentLoaded", () => {
    const logoutBtn = document.querySelector(".dropdown-item[href='/logout']");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", async (e) => {
            e.preventDefault();
            await fetch("/api/auth/logout", { method: "POST" });
            window.location.href = "/";
        });
    }
});
