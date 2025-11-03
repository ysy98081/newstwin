// ================================
// 마이페이지 회원정보 수정
// ================================

// 프로필 이미지 미리보기 + 파일 크기 제한
const profileInput = document.getElementById("profileImage");
const profilePreview = document.getElementById("profilePreview");

if (profileInput) {
    profileInput.addEventListener("change", (event) => {
        const file = event.target.files[0];
        if (file) {
            if (file.size > 10 * 1024 * 1024) {
                alert("10MB 이하의 이미지만 업로드 가능합니다.");
                profileInput.value = "";
                return;
            }
            profilePreview.src = URL.createObjectURL(file);
        }
    });
}

// 비밀번호 일치 확인
const password = document.getElementById("password");
const passwordConfirm = document.getElementById("passwordConfirm");
const message = document.getElementById("passwordMatchMessage");

function checkPasswordMatch() {
    if (
        password.value &&
        passwordConfirm.value &&
        password.value !== passwordConfirm.value
    ) {
        message.classList.remove("d-none");
    } else {
        message.classList.add("d-none");
    }
}

password.addEventListener("input", checkPasswordMatch);
passwordConfirm.addEventListener("input", checkPasswordMatch);

// 이메일 수신 여부 → 구독 카테고리 섹션 토글
const receiveTrue = document.getElementById("receiveTrue");
const receiveFalse = document.getElementById("receiveFalse");
const categorySection = document.getElementById("categorySection");

function toggleCategorySection() {
    categorySection.style.display = receiveTrue.checked ? "block" : "none";
}

if (receiveTrue && receiveFalse) {
    receiveTrue.addEventListener("change", toggleCategorySection);
    receiveFalse.addEventListener("change", toggleCategorySection);
    toggleCategorySection();
}

// 폼 제출 (PUT 비동기 요청)
const form = document.getElementById("updateForm");
if (form) {
    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        if (
            password.value &&
            passwordConfirm.value &&
            password.value !== passwordConfirm.value
        ) {
            alert("비밀번호가 일치하지 않습니다.");
            return;
        }

        const formData = new FormData(form);

        // 파일 없으면 제거
        const fileInput = document.getElementById("profileImage");
        if (fileInput && !fileInput.files.length) {
            formData.delete("profileImage");
        }

        // ⚠️ 절대 formData.delete()로 비어있는 값 지우지 말 것!

        console.log("전송 데이터 미리보기:", [...formData.entries()]);

        try {
            const response = await fetch("/api/mypage/me", {
                method: "POST",
                body: formData,
            });

            if (response.ok) {
                alert("회원 정보가 성공적으로 수정되었습니다.");
                location.reload();
            } else {
                const errorText = await response.text();
                alert("수정 실패: " + errorText);
            }
        } catch (error) {
            console.error("업데이트 요청 중 오류 발생:", error);
            alert("서버 오류가 발생했습니다.");
        }
    });
}
