const shareBtn = document.getElementById('shareBtn');
const shareMenu = document.getElementById('shareMenu');

shareBtn.addEventListener('click', (e)=>{
  shareMenu.classList.toggle('d-none');
  shareMenu.style.top = (shareBtn.offsetTop + 35) + 'px';
  shareMenu.style.left = shareBtn.offsetLeft + 'px';
});

document.addEventListener('click', (e) => {

  const menu = document.getElementById('shareMenu');
  const shareBtn2 = document.getElementById('shareBtn');

  // menu 열려있는 상태에서
  if (!menu.classList.contains('d-none')) {

    // 클릭한게 shareBtn 또는 menu 내부면 무시
    if (shareBtn2.contains(e.target) || menu.contains(e.target)) return;

    // 아니면 닫기
    menu.classList.add('d-none');
  }
});

document.querySelector('.share-copy').addEventListener('click', async ()=>{
  const url = window.location.href;
  try {
    await navigator.clipboard.writeText(url);
    alert("주소가 복사되었습니다.");
    shareMenu.classList.add('d-none');
  } catch(e){}
});

document.querySelector('.share-sns').addEventListener('click', async ()=>{
  const url = window.location.href;
  const title = document.title;

  if (navigator.share) {
    try {
      await navigator.share({ title, url });
    } catch(e){}
  } else {
    // 네비게이터가 없을때 페이스북 공유
    window.open(`https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(url)}&quote=${encodeURIComponent(title)}`,
        "_blank",
        "width=600,height=500");
  }

  shareMenu.classList.add('d-none');
});