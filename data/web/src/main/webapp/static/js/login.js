
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
                body: new URLSearchParams({ name, password })
            });

            const data = await res.json();

            if (res.ok && data.token) {
                localStorage.removeItem('jwtToken'); // 이전 키 정리
                localStorage.setItem('jwt_token', data.token);
                window.location.href = '/';
            } else {
                alert(data.error || '로그인 실패');
            }
        } catch (err) {
            alert('로그인 요청 중 오류 발생');
        }
    });
});
