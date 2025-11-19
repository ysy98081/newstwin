// ================================
// 마이페이지 회원정보 수정
// ================================

// 프로필 이미지 미리보기 + 파일 크기 제한
const profileInput = document.getElementById("profileImage");
const profilePreview = document.getElementById("profilePreview");

let tempImageUrl = null;

profileInput.addEventListener("change", async (e) => {
  const file = e.target.files[0];
  if (!file) return;

  profilePreview.src = URL.createObjectURL(file);

  // TEMP 업로드
  const formData = new FormData();
  formData.append("file", file);

  const res = await csrfFetch("/api/mypage/profile/temp", {
    method: "POST",
    body: formData
  });

  const data = await res.json();
  tempImageUrl = data.data;

  // form에 hidden 입력 추가
  let hidden = document.getElementById("tempImageUrl");
  if (!hidden) {
    hidden = document.createElement("input");
    hidden.type = "hidden";
    hidden.name = "tempImageUrl";
    hidden.id = "tempImageUrl";
    form.appendChild(hidden);
  }
  hidden.value = tempImageUrl;
});

// 비밀번호 일치 확인
const password = document.getElementById("password");
const passwordConfirm = document.getElementById("passwordConfirm");
const message = document.getElementById("passwordMatchMessage");

function checkPasswordMatch() {
    if (password.value && passwordConfirm.value && password.value !== passwordConfirm.value) {
        message.classList.remove("d-none");
    } else {
        message.classList.add("d-none");
    }
}

if (password && passwordConfirm) {
    password.addEventListener("input", checkPasswordMatch);
    passwordConfirm.addEventListener("input", checkPasswordMatch);
}

// 이메일 수신 여부 → 구독 카테고리 섹션 토글
const receiveTrue = document.getElementById("receiveTrue");
const receiveFalse = document.getElementById("receiveFalse");
const categorySection = document.getElementById("categorySection");

function toggleCategorySection() {
    if (!categorySection) return;
    categorySection.style.display = receiveTrue.checked ? "block" : "none";
}

if (receiveTrue && receiveFalse) {
    receiveTrue.addEventListener("change", toggleCategorySection);
    receiveFalse.addEventListener("change", toggleCategorySection);
    toggleCategorySection();
}

// 구독 중인 카테고리 자동 체크
window.addEventListener("DOMContentLoaded", () => {
    if (typeof subscribedCategories !== "undefined" && Array.isArray(subscribedCategories)) {
        const allCheckboxes = document.querySelectorAll(".btn-check[type='checkbox']");
        allCheckboxes.forEach((checkbox) => {
            const label = document.querySelector(`label[for='${checkbox.id}']`);
            if (subscribedCategories.includes(label.textContent.trim())) {
                checkbox.checked = true;
                label.classList.add("active");
            }
        });
    }
});

// 폼 제출 (회원 정보 + 구독 정보 통합 업데이트)
const form = document.getElementById("updateForm");

if (form) {
    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        if (password.value && passwordConfirm.value && password.value !== passwordConfirm.value) {
            alert("비밀번호가 일치하지 않습니다.");
            return;
        }

        const formData = new FormData(form);

        const selectedCategories = Array.from(
            document.querySelectorAll(".btn-check[type='checkbox']:checked")
        ).map((el) => Number(el.value));

        formData.append("receiveEmail", receiveTrue.checked);

        const fileInput = document.getElementById("profileImage");
        if (fileInput && !fileInput.files.length) {
            formData.delete("profileImage");
        }

        try {
            // --- 회원 정보 수정 ---
            const response = await csrfFetch("/api/mypage/me", {
                method: "POST",
                body: formData,
            });

            if (!response.ok) {
                const errorText = await response.text();
                alert("회원 정보 수정 실패: " + errorText);
                return;
            }

            // --- 구독 설정 수정 ---
            const subscriptionData = {
                receiveEmail: receiveTrue.checked,
                categoryIds: selectedCategories,
            };

            const res2 = await csrfFetch("/api/mypage/subscription", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(subscriptionData),
            });

            if (res2.ok) {
                alert("회원 정보 및 구독 설정이 함께 저장되었습니다.");
                location.reload();
            } else {
                const err = await res2.text();
                alert("회원 정보는 수정되었으나 구독 설정 저장에 실패했습니다.\n" + err);
            }
        } catch (error) {
            console.error("업데이트 요청 중 오류 발생:", error);
            alert("서버 오류가 발생했습니다.");
        }
    });
}
