// ================================
// 구독 설정 페이지 JS
// ================================

// 구독 여부 선택 시 카테고리 영역 토글
const subscribeTrue = document.getElementById("subscribeTrue");
const subscribeFalse = document.getElementById("subscribeFalse");
const categorySection = document.getElementById("categorySection");

function toggleCategorySection() {
    if (!categorySection) return;
    categorySection.style.display = subscribeTrue.checked ? "block" : "none";
}

if (subscribeTrue && subscribeFalse) {
    subscribeTrue.addEventListener("change", toggleCategorySection);
    subscribeFalse.addEventListener("change", toggleCategorySection);
    toggleCategorySection();
}

//  구독중인 카테고리 체크박스 자동 선택
window.addEventListener("DOMContentLoaded", () => {
    if (typeof subscribedCategories !== "undefined" && Array.isArray(subscribedCategories)) {
        const allCheckboxes = document.querySelectorAll(".btn-check[type='checkbox']");
        allCheckboxes.forEach((checkbox) => {
            const label = document.querySelector(`label[for='${checkbox.id}']`);
            if (subscribedCategories.includes(label.textContent.trim())) {
                checkbox.checked = true;
                label.classList.add("active"); // 선택된 버튼 시각 강조
            }
        });
    }
});

// 구독 설정 저장
const saveBtn = document.getElementById("saveSubscription");
if (saveBtn) {
    saveBtn.addEventListener("click", async (e) => {
        e.preventDefault();

        const selectedCategories = Array.from(
            document.querySelectorAll(".btn-check[type='checkbox']:checked")
        ).map((el) => Number(el.value));

        const data = {
            receiveEmail: subscribeTrue.checked,
            categoryIds: selectedCategories,
        };

        console.log("구독 설정 전송:", data);

        try {
            const res = await fetch("/api/mypage/subscription", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(data),
            });

            if (res.ok) {
                alert("구독 설정이 저장되었습니다.");
                location.reload();
            } else {
                const err = await res.text();
                alert("저장 실패: " + err);
            }
        } catch (error) {
            console.error("요청 오류:", error);
            alert("서버 오류가 발생했습니다.");
        }
    });
}
