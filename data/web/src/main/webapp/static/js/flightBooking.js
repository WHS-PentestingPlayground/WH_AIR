/**
 * 좌석 선택 및 결제 관련 JavaScript
 */

// 전역 상태 관리
const bookingState = {
    selectedSeats: [],
    maxSeats: 1,
    passengers: [],
    selectedSeatClass: 'economy',
    seatPrice: 0,
    fuelPrice: 0,
    userPoints: 0,
    totalAmount: 0,
    paymentPoints: 0,
    userCoupon: null,
    appliedCoupons: {
        seat: null,
        fuel: null
    },
    discountAmounts: {
        seat: 0,
        fuel: 0
    }
};

// 스텝 토글 함수
function toggleStep(stepId) {
    const step = document.getElementById(stepId);
    const header = step.querySelector('.step-header');
    const content = step.querySelector('.step-content');
    const toggle = step.querySelector('.step-toggle');
    
    const isCollapsed = content.classList.contains('collapsed');
    
    if (isCollapsed) {
        header.classList.remove('collapsed');
        content.classList.remove('collapsed');
        toggle.classList.remove('collapsed');
    } else {
        header.classList.add('collapsed');
        content.classList.add('collapsed');
        toggle.classList.add('collapsed');
    }
}

// 좌석 초기화
function initializeSeats() {
    // 퍼스트 클래스 (1-3열, A-B 배치)
    createSeatSection('first-class-seats', 'first', 1, 3, ['A', 'B']);
    
    // 비즈니스 클래스 (4-10열, A-D 배치)
    createSeatSection('business-class-seats', 'business', 4, 10, ['A', 'B', 'C', 'D']);
    
    // 이코노미 클래스 (11-30열, A-F 배치)
    createSeatSection('economy-class-seats', 'economy', 11, 30, ['A', 'B', 'C', 'D', 'E', 'F']);
    
    // 선택된 좌석 클래스에 따라 제한 적용
    applySeatClassRestrictions();
    
    // 서버에서 좌석 상태 로드
    loadSeatStatus();
}

function createSeatSection(containerId, seatClass, startRow, endRow, seatLetters) {
    const container = document.getElementById(containerId);
    
    for (let row = startRow; row <= endRow; row++) {
        const seatRow = document.createElement('div');
        seatRow.className = 'seat-row';
        
        // 행 번호
        const rowNumber = document.createElement('div');
        rowNumber.className = 'row-number';
        rowNumber.textContent = row;
        seatRow.appendChild(rowNumber);

        // 좌석 그룹 생성
        if (seatClass === 'first') {
            createSeatGroup(seatRow, row, ['A'], seatClass);
            createAisle(seatRow);
            createSeatGroup(seatRow, row, ['B'], seatClass);
        } else if (seatClass === 'business') {
            createSeatGroup(seatRow, row, ['A', 'B'], seatClass);
            createAisle(seatRow);
            createSeatGroup(seatRow, row, ['C', 'D'], seatClass);
        } else {
            createSeatGroup(seatRow, row, ['A', 'B', 'C'], seatClass);
            createAisle(seatRow);
            createSeatGroup(seatRow, row, ['D', 'E', 'F'], seatClass);
        }

        container.appendChild(seatRow);
    }
}

function createSeatGroup(parent, row, letters, seatClass) {
    const group = document.createElement('div');
    group.className = 'seat-group';
    
    letters.forEach(letter => {
        const seat = document.createElement('div');
        const seatId = `${row}${letter}`;
        
        seat.className = 'seat';
        seat.id = seatId;
        seat.textContent = letter;
        seat.dataset.row = row;
        seat.dataset.letter = letter;
        seat.dataset.class = seatClass;
        
        seat.classList.add('disabled');
        seat.onclick = () => toggleSeat(seat);
        
        group.appendChild(seat);
    });
    
    parent.appendChild(group);
}

function createAisle(parent) {
    const aisle = document.createElement('div');
    aisle.className = 'aisle';
    parent.appendChild(aisle);
}

function applySeatClassRestrictions() {
    const selectedClass = bookingState.selectedSeatClass;
    
    if (selectedClass === 'economy') {
        document.querySelectorAll('.seat[data-class="first"], .seat[data-class="business"]').forEach(seat => {
            seat.classList.remove('available');
            seat.classList.add('disabled');
        });
    } else if (selectedClass === 'business') {
        document.querySelectorAll('.seat[data-class="first"]').forEach(seat => {
            seat.classList.remove('available');
            seat.classList.add('disabled');
        });
    }
}

// 서버에서 좌석 상태 로드
async function loadSeatStatus() {
    try {
        const response = await fetch(`/api/flights/${bookingState.flightId}/seats?seatClass=${bookingState.selectedSeatClass}`);
        if (!response.ok) {
            throw new Error(`좌석 정보를 가져오는데 실패했습니다. Status: ${response.status}`);
        }
        
        const seatData = await response.json();
        updateSeatStatus(seatData.reservedSeats || [], seatData.availableSeats || []);
    } catch (error) {
        console.error('좌석 상태 로드 실패:', error);
        // 에러 발생 시 모든 좌석을 비활성화 상태로 유지
    }
}

function updateSeatStatus(reservedSeats, availableSeats) {
    // 1. 전체 좌석을 disabled(회색)로 초기화
    document.querySelectorAll('.seat').forEach(seat => {
        seat.classList.remove('available', 'occupied', 'disabled');
        seat.classList.add('disabled');
    });
    
    // 2. 예약된 좌석은 occupied(빨간색)로 표시
    reservedSeats.forEach(seatId => {
        const seat = document.getElementById(seatId);
        if (seat) {
            seat.classList.remove('available', 'disabled');
            seat.classList.add('occupied');
        }
    });
    
    // 3. 예약 가능한 좌석은 available(파란색)로 표시
    availableSeats.forEach(seatId => {
        const seat = document.getElementById(seatId);
        if (seat) {
            seat.classList.remove('disabled', 'occupied');
            seat.classList.add('available');
        }
    });
}

function toggleSeat(seatElement) {
    const seatId = seatElement.id;
    
    if (seatElement.classList.contains('selected')) {
        // 선택 해제
        seatElement.classList.remove('selected');
        seatElement.classList.add('available');
        bookingState.selectedSeats = bookingState.selectedSeats.filter(seat => seat.id !== seatId);
    } else if (seatElement.classList.contains('available')) {
        // 새로 선택
        if (bookingState.selectedSeats.length >= bookingState.maxSeats) {
            alert(`최대 ${bookingState.maxSeats}개의 좌석만 선택할 수 있습니다.`);
            return;
        }
        
        seatElement.classList.remove('available');
        seatElement.classList.add('selected');
        bookingState.selectedSeats.push({
            id: seatId,
            row: seatElement.dataset.row,
            letter: seatElement.dataset.letter,
            class: seatElement.dataset.class
        });
    }
    
    updateSeatSelectionDisplay();
}

function updateSeatSelectionDisplay() {
    const selectedSeatsDisplay = document.getElementById('selected-seats-display');
    const confirmBtn = document.getElementById('confirm-seat-btn');
    
    if (bookingState.selectedSeats.length === 0) {
        selectedSeatsDisplay.innerHTML = '선택된 좌석이 없습니다.';
        confirmBtn.disabled = true;
    } else {
        const seatTags = bookingState.selectedSeats.map(seat => 
            `<span class="seat-tag">${seat.id} (${getClassDisplayName(seat.class)})</span>`
        ).join('');
        
        selectedSeatsDisplay.innerHTML = seatTags;
        confirmBtn.disabled = false;
    }
}

function getClassDisplayName(seatClass) {
    const classNames = {
        'first': '퍼스트',
        'business': '비즈니스',
        'economy': '이코노미',
    };
    return classNames[seatClass] || seatClass;
}

function confirmSeatSelection() {
    if (bookingState.selectedSeats.length === 0) {
        alert('좌석을 선택해주세요.');
        return;
    }

    const confirmation = confirm(
        `다음 좌석을 선택하시겠습니까?\n\n` +
        bookingState.selectedSeats.map(seat => 
            `${seat.id} (${getClassDisplayName(seat.class)})`
        ).join('\n')
    );

    if (confirmation) {
        // STEP1 완료 처리
        document.getElementById('step1').classList.add('completed');
        
        // STEP1 닫고 STEP2 열기
        toggleStep('step1');
        setTimeout(() => {
            toggleStep('step2');
        }, 300);
        
        // 탑승자 정보 폼 생성
        generatePassengerForms();
    }
}

function generatePassengerForms() {
    const passengerFormsContainer = document.getElementById('passenger-forms');
    const numPassengers = bookingState.selectedSeats.length;
    
    passengerFormsContainer.innerHTML = '';
    
    for (let i = 0; i < numPassengers; i++) {
        const seat = bookingState.selectedSeats[i];
        const passengerForm = document.createElement('div');
        passengerForm.className = 'passenger-form';
        passengerForm.innerHTML = `
            <h4>탑승자 ${i + 1} (좌석 ${seat.id})</h4>
            <div class="form-row">
                <div class="form-field">
                    <label for="passenger-name-${i}">성명</label>
                    <input type="text" id="passenger-name-${i}" name="passengerName" required>
                </div>
                <div class="form-field">
                    <label for="passenger-birth-${i}">생년월일</label>
                    <input type="date" id="passenger-birth-${i}" name="passengerBirth" required>
                </div>
            </div>
        `;
        passengerFormsContainer.appendChild(passengerForm);
    }
    
    document.getElementById('confirm-passenger-btn').disabled = false;
}

function confirmPassengerInfo() {
    const passengerData = [];
    const numPassengers = bookingState.selectedSeats.length;
    
    for (let i = 0; i < numPassengers; i++) {
        const name = document.getElementById(`passenger-name-${i}`).value.trim();
        const birth = document.getElementById(`passenger-birth-${i}`).value;
        
        if (!name || !birth) {
            alert('모든 탑승자 정보를 입력해주세요.');
            return;
        }
        
        passengerData.push({
            seatId: bookingState.selectedSeats[i].id,
            name: name,
            birth: birth
        });
    }
    
    bookingState.passengers = passengerData;
    
    // STEP2 완료 처리
    document.getElementById('step2').classList.add('completed');
    
    // STEP2 닫고 STEP3 열기
    toggleStep('step2');
    setTimeout(() => {
        toggleStep('step3');
    }, 300);
    
    // 결제 정보 업데이트 및 쿠폰 로드
    loadCouponsAndUpdatePayment();
}

// 쿠폰 정보 로드 및 결제 정보 업데이트
async function loadCouponsAndUpdatePayment() {
    try {
        const response = await fetch(`/api/flights/${bookingState.flightId}/pricing?seatClass=${bookingState.selectedSeatClass}`);
        if (response.ok) {
            const pricingData = await response.json();
            bookingState.seatPrice = Math.floor(Number(pricingData.seatPrice));
            bookingState.fuelPrice = Math.floor(Number(pricingData.fuelPrice));
        }
        
        // 사용자 쿠폰 정보 로드
        const couponResponse = await fetch('/api/user/coupons');
        if (couponResponse.ok) {
            const couponData = await couponResponse.json();
            bookingState.userCoupon = couponData.userCoupon;
            bookingState.userPoints = couponData.points || 0;
            
            // 쿠폰 드롭다운 업데이트
            updateCouponDropdowns();
        }
        
        // 결제 정보 업데이트
        updatePaymentInfo();
    } catch (error) {
        console.error('쿠폰 정보 로드 실패:', error);
        updatePaymentInfo();
    }
}

function updateCouponDropdowns() {
    const seatSelect = document.getElementById('seat-coupon-select');
    const fuelSelect = document.getElementById('fuel-coupon-select');
    
    // 드롭다운 초기화
    seatSelect.innerHTML = '<option value="">쿠폰 선택</option>';
    fuelSelect.innerHTML = '<option value="">쿠폰 선택</option>';
    
    // 사용자가 쿠폰을 보유하고 있을 때만 옵션 추가
    if (bookingState.userCoupon && bookingState.userCoupon.trim() !== '') {
        const couponCode = bookingState.userCoupon.trim();
        
        // 운임비 드롭다운에 쿠폰 추가
        const seatOption = new Option(`쿠폰: ${couponCode}`, couponCode);
        seatSelect.add(seatOption);
        
        // 유류할증료 드롭다운에 쿠폰 추가  
        const fuelOption = new Option(`쿠폰: ${couponCode}`, couponCode);
        fuelSelect.add(fuelOption);
    }
}

// 쿠폰 적용 (새로운 API 구조)
async function applyCoupon(type) {
    const selectElement = document.getElementById(`${type}-coupon-select`);
    const selectedCoupon = selectElement.value;
    
    if (!selectedCoupon) {
        // 쿠폰 선택 해제
        bookingState.appliedCoupons[type] = null;
        bookingState.discountAmounts[type] = 0;
        updateCouponUI(type, 0, 0);
        updateTotalAmount();
        updateCouponDropdownStates();
        return;
    }
    
    // 동일한 쿠폰이 다른 영역에 이미 적용되어 있는지 확인
    const otherType = type === 'seat' ? 'fuel' : 'seat';
    if (bookingState.appliedCoupons[otherType] === selectedCoupon) {
        alert('동일한 쿠폰을 중복으로 사용할 수 없습니다.');
        selectElement.value = '';
        return;
    }
    
    try {
        // 대표 좌석 번호 (첫 번째 선택된 좌석)
        const representativeSeat = bookingState.selectedSeats[0]?.id;
        if (!representativeSeat) {
            alert('좌석을 먼저 선택해주세요.');
            return;
        }
        
        const requestData = {
            couponCode: selectedCoupon,
            targetPriceType: type,
            flightId: bookingState.flightId,
            seatNumber: representativeSeat
        };
        
        const response = await fetch('/api/reservations/apply-coupon', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestData)
        });
        
        const result = await response.json();
        
        if (result.success) {
            // 쿠폰 적용 성공
            bookingState.appliedCoupons[type] = selectedCoupon;
            bookingState.discountAmounts[type] = result.discountAmount;
            
            updateCouponUI(type, result.discountAmount, result.finalPrice);
            updateTotalAmount();
            updateCouponDropdownStates();
            
            // 성공 메시지 표시
            showMessage(result.message, 'success');
        } else {
            // 쿠폰 적용 실패
            alert(result.message);
            selectElement.value = '';
        }
    } catch (error) {
        console.error('쿠폰 적용 오류:', error);
        alert('쿠폰 적용 중 오류가 발생했습니다.');
        selectElement.value = '';
    }
}

// 쿠폰 UI 업데이트
function updateCouponUI(type, discountAmount, finalPrice) {
    const passengerCount = bookingState.selectedSeats.length;
    const basePrice = type === 'seat' ? bookingState.seatPrice : bookingState.fuelPrice;
    const totalBasePrice = basePrice * passengerCount;
    const totalDiscountAmount = discountAmount * passengerCount;
    const totalFinalPrice = finalPrice * passengerCount;
    
    // 원래 가격 표시
    document.getElementById(`${type}-original-display`).textContent = `₩${totalBasePrice.toLocaleString()}`;
    
    // 할인 금액 표시
    const discountElement = document.getElementById(`${type}-discount-display`);
    if (totalDiscountAmount > 0) {
        discountElement.textContent = `-₩${totalDiscountAmount.toLocaleString()}`;
        discountElement.style.display = 'inline';
    } else {
        discountElement.style.display = 'none';
    }
    
    // 최종 가격 표시
    document.getElementById(`${type}-final-display`).textContent = `₩${totalFinalPrice.toLocaleString()}`;
}

// 쿠폰 드롭다운 상태 업데이트 (중복 사용 방지)
function updateCouponDropdownStates() {
    const seatSelect = document.getElementById('seat-coupon-select');
    const fuelSelect = document.getElementById('fuel-coupon-select');
    
    // 운임비에 쿠폰이 적용된 경우, 유류할증료 드롭다운에서 해당 쿠폰 비활성화
    Array.from(fuelSelect.options).forEach(option => {
        if (option.value === bookingState.appliedCoupons.seat && option.value !== '') {
            option.disabled = true;
            option.textContent = option.textContent.includes('(사용됨)') ? 
                option.textContent : `${option.textContent} (사용됨)`;
        } else if (option.value !== '' && !option.textContent.includes('(사용됨)')) {
            option.disabled = false;
            option.textContent = option.textContent.replace(' (사용됨)', '');
        }
    });
    
    // 유류할증료에 쿠폰이 적용된 경우, 운임비 드롭다운에서 해당 쿠폰 비활성화
    Array.from(seatSelect.options).forEach(option => {
        if (option.value === bookingState.appliedCoupons.fuel && option.value !== '') {
            option.disabled = true;
            option.textContent = option.textContent.includes('(사용됨)') ? 
                option.textContent : `${option.textContent} (사용됨)`;
        } else if (option.value !== '' && !option.textContent.includes('(사용됨)')) {
            option.disabled = false;
            option.textContent = option.textContent.replace(' (사용됨)', '');
        }
    });
}

// 결제 정보 갱신
function updatePaymentInfo() {
    const passengerCount = bookingState.selectedSeats.length;
    const totalSeatPrice = bookingState.seatPrice * passengerCount;
    const totalFuelPrice = bookingState.fuelPrice * passengerCount;
    
    // 쿠폰 적용 없이 기본 가격 표시
    updateCouponUI('seat', 0, bookingState.seatPrice);
    updateCouponUI('fuel', 0, bookingState.fuelPrice);
    
    updateTotalAmount();
}

// 총 금액 업데이트
function updateTotalAmount() {
    const seatFinalText = document.getElementById('seat-final-display').textContent;
    const fuelFinalText = document.getElementById('fuel-final-display').textContent;
    
    const seatFinal = parseInt(seatFinalText.replace(/[₩,]/g, ''));
    const fuelFinal = parseInt(fuelFinalText.replace(/[₩,]/g, ''));
    
    bookingState.totalAmount = seatFinal + fuelFinal;
    
    document.getElementById('total-payment').textContent = `₩${bookingState.totalAmount.toLocaleString()}`;
    
    // 포인트 입력 필드 업데이트
    const inputElem = document.getElementById('payment-points');
    if (!inputElem.value) {
        bookingState.paymentPoints = bookingState.totalAmount;
        inputElem.value = bookingState.paymentPoints.toLocaleString();
    }
    
    updateRemainingPoints();
}

// 모든 포인트 사용
function useAllPoints() {
    const maxPoints = Math.min(bookingState.userPoints, bookingState.totalAmount);
    bookingState.paymentPoints = maxPoints;
    const inputElem = document.getElementById('payment-points');
    inputElem.value = maxPoints.toLocaleString();
    updateRemainingPoints();
}

// 결제 포인트 업데이트
function updatePaymentPoints() {
    const inputElem = document.getElementById('payment-points');
    const inputValue = inputElem.value.replace(/,/g, '');
    const points = parseInt(inputValue) || 0;
    
    if (points > bookingState.userPoints) {
        alert('보유 포인트를 초과할 수 없습니다.');
        bookingState.paymentPoints = bookingState.userPoints;
        inputElem.value = bookingState.userPoints.toLocaleString();
    } else if (points > bookingState.totalAmount) {
        alert('결제 금액을 초과할 수 없습니다.');
        bookingState.paymentPoints = bookingState.totalAmount;
        inputElem.value = bookingState.totalAmount.toLocaleString();
    } else {
        bookingState.paymentPoints = points;
    }
    
    updateRemainingPoints();
}

// 잔여 포인트 업데이트
function updateRemainingPoints() {
    const remaining = bookingState.userPoints - bookingState.paymentPoints;
    document.getElementById('remaining-points').textContent = `${remaining.toLocaleString()}P`;
}

// 결제 처리 (새로운 API 구조)
async function processPayment() {
    if (bookingState.paymentPoints < bookingState.totalAmount) {
        alert(`결제 포인트가 부족합니다.\n필요 포인트: ${bookingState.totalAmount.toLocaleString()}P\n입력 포인트: ${bookingState.paymentPoints.toLocaleString()}P`);
        return;
    }
    
    let confirmMessage = `포인트 결제를 진행하시겠습니까?\n\n`;
    confirmMessage += `선택된 좌석: ${bookingState.selectedSeats.map(seat => seat.id).join(', ')}\n`;
    confirmMessage += `탑승자: ${bookingState.passengers.map(p => p.name).join(', ')}\n`;
    confirmMessage += `결제 금액: ₩${bookingState.totalAmount.toLocaleString()}\n`;
    confirmMessage += `사용 포인트: ${bookingState.paymentPoints.toLocaleString()}P\n`;
    confirmMessage += `결제 후 잔여: ${(bookingState.userPoints - bookingState.paymentPoints).toLocaleString()}P`;
    
    if (confirm(confirmMessage)) {
        try {
            // 서버에 예약 요청 전송 (새로운 API 엔드포인트)
            const bookingData = {
                flightId: bookingState.flightId,
                seatNumbers: bookingState.selectedSeats.map(seat => seat.id),
                passengerName: bookingState.passengers[0]?.name || '탑승자',
                passengerBirth: bookingState.passengers[0]?.birth || '1990-01-01',
                usedPoints: bookingState.paymentPoints,
                seatCoupon: bookingState.appliedCoupons.seat,
                fuelCoupon: bookingState.appliedCoupons.fuel
            };
            
            const response = await fetch('/api/reservations/create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(bookingData)
            });
            
            const result = await response.json();
            
            if (result.success) {
                // STEP 3 완료 처리
                document.getElementById('step3').classList.add('completed');
                
                alert('🎉 결제가 완료되었습니다!\n예약 확인서를 이메일로 발송했습니다.');
                
                // 예약 완료 후 검색 페이지로 이동
                setTimeout(() => {
                    window.location.href = '/search';
                }, 2000);
            } else {
                alert(`예약 실패: ${result.message}`);
            }
        } catch (error) {
            console.error('예약 처리 오류:', error);
            alert('예약 처리 중 오류가 발생했습니다. 다시 시도해주세요.');
        }
    }
}

// 메시지 표시 함수
function showMessage(message, type = 'info') {
    // 기존 메시지 제거
    const existingMessage = document.querySelector('.message-toast');
    if (existingMessage) {
        existingMessage.remove();
    }
    
    // 새 메시지 생성
    const messageElement = document.createElement('div');
    messageElement.className = `message-toast ${type}`;
    messageElement.textContent = message;
    messageElement.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 12px 20px;
        border-radius: 4px;
        color: white;
        font-weight: bold;
        z-index: 1000;
        max-width: 300px;
        background-color: ${type === 'success' ? '#28a745' : type === 'error' ? '#dc3545' : '#17a2b8'};
    `;
    
    document.body.appendChild(messageElement);
    
    // 3초 후 자동 제거
    setTimeout(() => {
        if (messageElement.parentNode) {
            messageElement.remove();
        }
    }, 3000);
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 서버 데이터 가져오기
    const appElement = document.querySelector('.container');
    if (appElement) {
        bookingState.flightId = parseInt(appElement.dataset.flightId) || 0;
        bookingState.selectedSeatClass = appElement.dataset.selectedSeatClass || 'economy';
        bookingState.seatPrice = parseInt(appElement.dataset.seatPrice) || 0;
        bookingState.fuelPrice = parseInt(appElement.dataset.fuelPrice) || 0;
        bookingState.userId = parseInt(appElement.dataset.userId) || 0;
        bookingState.userPoints = parseInt(appElement.dataset.userPoints) || 0;
    }
    
    // 초기 가격 정보 표시
    document.getElementById('available-points').textContent = `${bookingState.userPoints.toLocaleString()}P`;
    document.getElementById('remaining-points').textContent = `${bookingState.userPoints.toLocaleString()}P`;
    
    initializeSeats();
    
    // 쿠폰 적용 버튼 이벤트 연결
    document.getElementById('seat-coupon-apply-btn').onclick = () => applyCoupon('seat');
    document.getElementById('fuel-coupon-apply-btn').onclick = () => applyCoupon('fuel');
}); 