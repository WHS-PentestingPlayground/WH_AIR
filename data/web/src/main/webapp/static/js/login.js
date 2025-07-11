
//login.js
document.addEventListener('DOMContentLoaded', function () {
    const loginForm = document.querySelector('.login-form');
    if (!loginForm) return;

    loginForm.addEventListener('submit', async function (e) {
        e.preventDefault();

        const name = document.querySelector('#name').value;
        const password = document.querySelector('#password').value;

        try {
            const res = await fetch('/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: new URLSearchParams({ name, password }),
                credentials: 'include' // ✅ 쿠키 자동 수신
            });

            const data = await res.json();

            if (res.ok && data.token) {
                localStorage.removeItem('jwtToken'); // 이전 키 정리
                localStorage.setItem('jwt_token', data.token);
                
                // 쿠키에도 토큰 저장
                document.cookie = `jwt_token=${data.token}; path=/; max-age=86400; SameSite=Strict`;
                
                // manager인 경우 manager 페이지로 리다이렉트
                if (data.redirect) {
                    window.location.href = data.redirect;
                } else {
                    window.location.href = '/';
                }
            } else {
                alert(data.error || '로그인 실패');
            }
        } catch (err) {
            alert('로그인 요청 중 오류 발생');
        }
    });
});
