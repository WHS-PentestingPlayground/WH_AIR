document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.book-btn').forEach(function(btn) {
        btn.addEventListener('click', function() {
            const tr = btn.closest('tr');
            const nextRow = tr.nextElementSibling;
            if (!nextRow || !nextRow.classList.contains('price-info-row')) return;

            // 현재 행의 토글만 처리 (다른 행은 그대로 유지)
            if (nextRow.style.display === '' || nextRow.style.display === 'table-row') {
                nextRow.style.display = 'none';
                nextRow.querySelector('.price-info-cell').innerHTML = '';
                return;
            }

            // 가격 정보 생성
            const seatPrice = parseInt(btn.dataset.seatPrice);
            const fuelPrice = parseInt(btn.dataset.fuelPrice);
            const total = seatPrice + fuelPrice;
            const flightId = btn.dataset.flightId;
            const seatClass = btn.dataset.seatClass;

            const html = `
                <div class="price-info-box">
                    <span class="price-label">운임비</span>
                    <span class="price-value">${seatPrice.toLocaleString()}원</span>
                    <span class="plus">+</span>
                    <span class="price-label">유류할증료</span>
                    <span class="price-value">${fuelPrice.toLocaleString()}원</span>
                    <span class="equal">=</span>
                    <span class="total-label">지불 예상 총액</span>
                    <span class="total-value">${total.toLocaleString()}원</span>
                    <form action="/flight/booking" method="get" style="display:inline;">
                        <input type="hidden" name="flightId" value="${flightId}">
                        <input type="hidden" name="seatClass" value="${seatClass}">
                        <button type="submit" class="pay-btn">결제</button>
                    </form>
                </div>
            `;
            nextRow.querySelector('.price-info-cell').innerHTML = html;
            nextRow.style.display = 'table-row';
        });
    });
}); 