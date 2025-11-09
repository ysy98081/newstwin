/**
 * 헤더 스크립트
 * - 로그인 상태일 때 사용자 프로필 이미지 불러오기
 * - 로그아웃 처리
 */
document.addEventListener("DOMContentLoaded", async () => {
    const logoutBtn = document.getElementById("logoutBtn");
    const profileImgEl = document.getElementById("headerProfileImg");

    // 로그아웃 처리
    if (logoutBtn) {
        logoutBtn.addEventListener("click", async (e) => {
            e.preventDefault();
            try {
                await fetch("/api/auth/logout", { method: "POST" });
                window.location.href = "/";
            } catch (error) {
                console.error("로그아웃 실패:", error);
                alert("로그아웃 중 오류가 발생했습니다.");
            }
        });
    }

    // 로그인 상태면 사용자 정보 불러와 헤더 이미지 갱신
    if (profileImgEl) {
        try {
            const res = await fetch("/api/mypage/me");

            if (res.ok) {
                const result = await res.json();
                if (result.success && result.data) {
                    const imgUrl = result.data.profileImage;

                    // null, 빈 문자열, undefined 모두 처리
                    if (imgUrl && imgUrl.trim() !== "") {
                        profileImgEl.src = imgUrl;
                    } else {
                        profileImgEl.src = "/images/basic-profile.png";
                    }
                }
            } else {
                // 로그인 만료 등의 경우
                profileImgEl.src = "/images/basic-profile.png";
            }
        } catch (err) {
            console.error("프로필 이미지 로드 실패:", err);
            profileImgEl.src = "/images/basic-profile.png";
        }
    }
});
