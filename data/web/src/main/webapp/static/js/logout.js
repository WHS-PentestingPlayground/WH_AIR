

document.addEventListener('DOMContentLoaded', function () {
    const logoutBtn = document.querySelector('.header-logout-btn');
    if (!logoutBtn) return;

    logoutBtn.addEventListener('click', function (e) {
        e.preventDefault();


        // ✅ 서버에 쿠키 삭제 요청만 보내면 됨
        // 서버 세션 무효화 요청
        fetch('/logout', {
            method: 'GET',
            credentials: 'include' // 쿠키 포함해서 세션 로그아웃 반영
        }).then(() => {
            // 홈으로 이동 (서버에서 user 없는 상태로 렌더링됨)
            window.location.href = '/';
        });
    });
});
