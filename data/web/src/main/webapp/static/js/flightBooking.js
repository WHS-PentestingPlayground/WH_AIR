/**
 * 좌석 선택 및 결제 관련 JavaScript
 */

class SeatBookingManager {
    constructor() {
        this.selectedSeats = [];
        this.maxSeats = 1; // 최대 선택 가능 좌석 수
        this.passengers = []; // 탑승자 정보
        
        // HTML 데이터 속성에서 서버 데이터 가져오기 (window 객체 대신)
        const appElement = document.getElementById('flight-booking-app');
        this.flightId = appElement?.dataset.flightId || 0;
        this.selectedSeatClass = appElement?.dataset.selectedSeatClass || 'economy';
        this.seatPrice = parseInt(appElement?.dataset.seatPrice) || 300000;
        this.fuelPrice = parseInt(appElement?.dataset.fuelPrice) || 80000;
        
        this.init();
    }

    init() {
        this.initializeSeats();
        this.setupEventListeners();
        this.updateStep1Toggle();
        
        // 페이지 로드 시 STEP1 열기
        this.openStep('step1');
        
        // 서버에서 좌석 상태 로드
        this.loadSeatStatus();
    }

    initializeSeats() {
        // 퍼스트 클래스 (1-3열, A-B 배치)
        this.createSeatSection('first-class-seats', 'first', 1, 3, ['A', 'B']);
        
        // 비즈니스 클래스 (4-10열, A-D 배치)
        this.createSeatSection('business-class-seats', 'business', 4, 10, ['A', 'B', 'C', 'D']);
        
        // 이코노미 클래스 (11-40열, A-F 배치)
        this.createSeatSection('economy-class-seats', 'economy', 11, 30, ['A', 'B', 'C', 'D', 'E', 'F']);
        
        // 선택된 좌석 클래스에 따라 제한 적용
        this.applySeatClassRestrictions();
    }

    createSeatSection(containerId, seatClass, startRow, endRow, seatLetters) {
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
                // 퍼스트: A-B
                this.createSeatGroup(seatRow, row, ['A'], seatClass);
                this.createAisle(seatRow);
                this.createSeatGroup(seatRow, row, ['B'], seatClass);
            } else if (seatClass === 'business') {
                // 비즈니스: A-B | C-D
                this.createSeatGroup(seatRow, row, ['A', 'B'], seatClass);
                this.createAisle(seatRow);
                this.createSeatGroup(seatRow, row, ['C', 'D'], seatClass);
                this.createAisle(seatRow);
            } else {
                // 이코노미: A-B-C | D-E-F
                this.createSeatGroup(seatRow, row, ['A', 'B', 'C'], seatClass);
                this.createAisle(seatRow);
                this.createSeatGroup(seatRow, row, ['D', 'E', 'F'], seatClass);
            }

            container.appendChild(seatRow);
        }
    }

    createSeatGroup(parent, row, letters, seatClass) {
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
            
            // 기본적으로 모든 좌석을 비활성화 상태로 설정
            seat.classList.add('disabled');
            
            group.appendChild(seat);
        });
        
        parent.appendChild(group);
    }

    createAisle(parent) {
        const aisle = document.createElement('div');
        aisle.className = 'aisle';
        parent.appendChild(aisle);
    }

    applySeatClassRestrictions() {
        const selectedClass = this.selectedSeatClass;
        
        if (selectedClass === 'economy') {
            // 이코노미 선택 시 퍼스트와 비즈니스 클래스 비활성화
            document.querySelectorAll('.seat[data-class="first"], .seat[data-class="business"]').forEach(seat => {
                seat.classList.remove('available');
                seat.classList.add('disabled');
            });
        } else if (selectedClass === 'business') {
            // 비즈니스 선택 시 퍼스트 클래스 비활성화
            document.querySelectorAll('.seat[data-class="first"]').forEach(seat => {
                seat.classList.remove('available');
                seat.classList.add('disabled');
            });
        }
        // 퍼스트 클래스 선택 시에는 모든 좌석 이용 가능
    }

    setupEventListeners() {
        // 좌석 클릭 이벤트
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('seat') && 
                (e.target.classList.contains('available') || e.target.classList.contains('selected'))) {
                this.toggleSeat(e.target);
            }
        });
    }

    toggleSeat(seatElement) {
        const seatId = seatElement.id;
        
        if (seatElement.classList.contains('selected')) {
            // 선택 해제
            seatElement.classList.remove('selected');
            this.selectedSeats = this.selectedSeats.filter(seat => seat.id !== seatId);
        } else {
            // 새로 선택
            if (this.selectedSeats.length >= this.maxSeats) {
                alert(`최대 ${this.maxSeats}개의 좌석만 선택할 수 있습니다.`);
                return;
            }
            
            seatElement.classList.add('selected');
            this.selectedSeats.push({
                id: seatId,
                row: seatElement.dataset.row,
                letter: seatElement.dataset.letter,
                class: seatElement.dataset.class
            });
        }
        
        this.updateSeatSelectionDisplay();
    }

    updateSeatSelectionDisplay() {
        const selectedSeatsDisplay = document.getElementById('selected-seats-display');
        const confirmBtn = document.getElementById('confirm-seat-btn');
        
        if (this.selectedSeats.length === 0) {
            selectedSeatsDisplay.innerHTML = '선택된 좌석이 없습니다.';
            confirmBtn.disabled = true;
        } else {
            const seatTags = this.selectedSeats.map(seat => 
                `<span class="seat-tag">${seat.id} (${this.getClassDisplayName(seat.class)})</span>`
            ).join('');
            
            selectedSeatsDisplay.innerHTML = seatTags;
            confirmBtn.disabled = false;
        }
    }

    getClassDisplayName(seatClass) {
        const classNames = {
            'first': '퍼스트',
            'business': '비즈니스',
            'economy': '이코노미',
        };
        return classNames[seatClass] || seatClass;
    }

    updateStep1Toggle() {
        const step1Header = document.querySelector('#step1 .step-header');
        const step1Content = document.querySelector('#step1 .step-content');
        
        if (this.selectedSeats.length > 0) {
            step1Header.classList.add('active');
            step1Content.style.display = 'block';
        }
    }

    openStep(stepId) {
        const stepHeader = document.querySelector(`#${stepId} .step-header`);
        const stepContent = document.querySelector(`#${stepId} .step-content`);
        const stepToggle = document.querySelector(`#${stepId} .step-toggle`);
        
        stepContent.style.display = 'block';
        stepHeader.classList.add('active');
        stepToggle.style.transform = 'rotate(180deg)';
    }

    closeStep(stepId) {
        const stepHeader = document.querySelector(`#${stepId} .step-header`);
        const stepContent = document.querySelector(`#${stepId} .step-content`);
        const stepToggle = document.querySelector(`#${stepId} .step-toggle`);
        
        stepContent.style.display = 'none';
        stepHeader.classList.remove('active');
        stepToggle.style.transform = 'rotate(0deg)';
    }

    /**
     * 서버에서 좌석 상태(전체, 예약됨, 예약 가능) 정보 로드
     */
    async loadSeatStatus() {
        try {
            const url = `/api/seats/${this.flightId}/map?seatClass=${this.selectedSeatClass}`;
            console.log('좌석 정보 요청 URL:', url);
            
            const response = await fetch(url);
            console.log('좌석 정보 응답 상태:', response.status);
            
            if (!response.ok) {
                throw new Error(`좌석 정보를 가져오는데 실패했습니다. Status: ${response.status}`);
            }
            
            const seatData = await response.json();
            console.log('좌석 데이터:', seatData);
            
            // reservedSeats, availableSeats 사용
            this.updateSeatStatus([], seatData.reservedSeats || [], seatData.availableSeats || []);
            console.log('좌석 상태 업데이트 완료');
        } catch (error) {
            console.error('좌석 정보 로드 실패:', error);
            // 에러 발생 시 기본 상태로 설정 (모든 좌석을 available로)
            console.log('기본 좌석 상태로 설정합니다.');
            this.updateSeatStatus([], [], []);
        }
    }

    /**
     * 전체 좌석, 예약된 좌석, 예약 가능한 좌석을 구분해서 표시
     */
    updateSeatStatus(allSeats, reservedSeats, availableSeats) {
        console.log('좌석 상태 업데이트 시작');
        console.log('예약된 좌석:', reservedSeats);
        console.log('예약 가능 좌석:', availableSeats);
        
        // 1. 모든 좌석을 기본적으로 disabled로 설정
        const allSeatElements = document.querySelectorAll('.seat');
        allSeatElements.forEach(seat => {
            seat.classList.remove('available', 'occupied', 'disabled');
            seat.classList.add('disabled');
        });
        
        // 2. 선택된 좌석 클래스의 좌석들을 available로 설정
        const selectedClassSeats = document.querySelectorAll(`.seat[data-class="${this.selectedSeatClass}"]`);
        selectedClassSeats.forEach(seat => {
            seat.classList.remove('disabled', 'occupied');
            seat.classList.add('available');
        });
        
        // 3. 예약된 좌석은 occupied(빨간색)로 표시
        reservedSeats.forEach(seatId => {
            const seat = document.getElementById(seatId);
            if (seat) {
                seat.classList.remove('available', 'disabled');
                seat.classList.add('occupied');
                console.log(`좌석 ${seatId}를 occupied로 설정`);
            } else {
                console.warn(`좌석 ${seatId}를 찾을 수 없습니다`);
            }
        });
        
        console.log('좌석 상태 업데이트 완료');
    }
}

// 전역 함수들
function toggleStep(stepId) {
    const stepHeader = document.querySelector(`#${stepId} .step-header`);
    const stepContent = document.querySelector(`#${stepId} .step-content`);
    const stepToggle = document.querySelector(`#${stepId} .step-toggle`);
    
    if (stepContent.style.display === 'none' || stepContent.style.display === '') {
        stepContent.style.display = 'block';
        stepHeader.classList.add('active');
        stepToggle.style.transform = 'rotate(180deg)';
    } else {
        stepContent.style.display = 'none';
        stepHeader.classList.remove('active');
        stepToggle.style.transform = 'rotate(0deg)';
    }
}

function openStep(stepId) {
    const stepHeader = document.querySelector(`#${stepId} .step-header`);
    const stepContent = document.querySelector(`#${stepId} .step-content`);
    const stepToggle = document.querySelector(`#${stepId} .step-toggle`);
    
    stepContent.style.display = 'block';
    stepHeader.classList.add('active');
    stepToggle.style.transform = 'rotate(180deg)';
}

function closeStep(stepId) {
    const stepHeader = document.querySelector(`#${stepId} .step-header`);
    const stepContent = document.querySelector(`#${stepId} .step-content`);
    const stepToggle = document.querySelector(`#${stepId} .step-toggle`);
    
    stepContent.style.display = 'none';
    stepHeader.classList.remove('active');
    stepToggle.style.transform = 'rotate(0deg)';
}

function confirmSeatSelection() {
    try {
        if (seatBookingManager.selectedSeats.length === 0) {
            alert('좌석을 선택해주세요.');
            return;
        }

        const confirmation = confirm(
            `다음 좌석을 선택하시겠습니까?\n\n` +
            seatBookingManager.selectedSeats.map(seat => 
                `${seat.id} (${seatBookingManager.getClassDisplayName(seat.class)})`
            ).join('\n')
        );

        if (confirmation) {
            // 선택된 좌석을 occupied 상태로 변경 (시각적 효과)
            seatBookingManager.selectedSeats.forEach(seat => {
                const seatElement = document.getElementById(seat.id);
                seatElement.classList.remove('available');
                seatElement.classList.add('disabled');
            });

            // STEP1 닫고 STEP2 열기
            seatBookingManager.closeStep('step1');
            seatBookingManager.openStep('step2');
            
            // 탑승자 정보 폼 생성
            generatePassengerForms();
        }
    } catch (error) {
        alert('좌석 선택 처리 중 오류가 발생했습니다: ' + error.message);
    }
}

function generatePassengerForms() {
    const passengerFormsContainer = document.getElementById('passenger-forms');
    const numPassengers = seatBookingManager.selectedSeats.length;
    
    passengerFormsContainer.innerHTML = '';
    
    for (let i = 0; i < numPassengers; i++) {
        const seat = seatBookingManager.selectedSeats[i];
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
    
    // 탑승자 정보 확정 버튼 활성화
    document.getElementById('confirm-passenger-btn').disabled = false;
}

function confirmPassengerInfo() {
    const passengerData = [];
    const numPassengers = seatBookingManager.selectedSeats.length;
    
    // 탑승자 정보 유효성 검사
    for (let i = 0; i < numPassengers; i++) {
        const name = document.getElementById(`passenger-name-${i}`).value.trim();
        const birth = document.getElementById(`passenger-birth-${i}`).value;
        
        if (!name || !birth) {
            alert('모든 탑승자 정보를 입력해주세요.');
            return;
        }
        
        passengerData.push({
            seatId: seatBookingManager.selectedSeats[i].id,
            name: name,
            birth: birth
        });
    }
    
    // 탑승자 정보 저장
    seatBookingManager.passengers = passengerData;
    
    // STEP2 닫고 STEP3 열기
    seatBookingManager.closeStep('step2');
    seatBookingManager.openStep('step3');
    
    // 결제 정보 업데이트
    updatePaymentInfo();
}

function updatePaymentInfo() {
    const passengerCount = seatBookingManager.selectedSeats.length;
    const seatPricePerPerson = seatBookingManager.seatPrice;
    const fuelPricePerPerson = seatBookingManager.fuelPrice;
    
    // 탑승자 수에 따른 총 금액 계산
    const totalSeatPrice = seatPricePerPerson * passengerCount;
    const totalFuelPrice = fuelPricePerPerson * passengerCount;
    const totalPayment = totalSeatPrice + totalFuelPrice;
    
    document.getElementById('seat-price').textContent = `₩${totalSeatPrice.toLocaleString()}`;
    document.getElementById('fuel-price').textContent = `₩${totalFuelPrice.toLocaleString()}`;
    document.getElementById('total-payment').textContent = `₩${totalPayment.toLocaleString()}`;
    
    // 결제 정보 초기화
    seatBookingManager.totalAmount = totalPayment;
    seatBookingManager.usedPoints = 0;
    seatBookingManager.couponCode = null;
    seatBookingManager.couponDiscount = 0;
    
    // 사용자 정보 로드
    loadUserPaymentInfo();
}

// 사용자 포인트/쿠폰 정보 로드
async function loadUserPaymentInfo() {
    try {
        // JSP에서 전달받은 사용자 정보 확인
        const appElement = document.getElementById('flight-booking-app');
        const userId = appElement.dataset.userId;
        
        if (!userId) {
            alert('로그인이 필요합니다. 로그인 페이지로 이동합니다.');
            window.location.href = '/login';
            return;
        }
        
        seatBookingManager.currentUserId = parseInt(userId);
        
        // 포인트 조회
        const pointsResponse = await fetch(`/api/reservations/users/${userId}/points`);
        if (pointsResponse.ok) {
            const pointsData = await pointsResponse.json();
            document.getElementById('available-points').textContent = `${pointsData.points.toLocaleString()}P`;
            seatBookingManager.availablePoints = pointsData.points;
        }
        
        // 쿠폰 조회
        const couponResponse = await fetch(`/api/reservations/users/${userId}/coupon`);
        if (couponResponse.ok) {
            const couponData = await couponResponse.json();
            if (couponData.coupon) {
                document.getElementById('available-coupon').textContent = couponData.coupon;
                document.getElementById('coupon-code-input').value = couponData.coupon;
                seatBookingManager.availableCoupon = couponData.coupon;
            } else {
                document.getElementById('available-coupon').textContent = '보유 쿠폰 없음';
            }
        }
        
        updateFinalPayment();
    } catch (error) {
        console.error('사용자 정보 로드 실패:', error);
        document.getElementById('available-points').textContent = '로드 실패';
        document.getElementById('available-coupon').textContent = '로드 실패';
    }
}

// 포인트 전액 사용
function useAllPoints() {
    const availablePoints = seatBookingManager.availablePoints || 0;
    const totalAmount = seatBookingManager.totalAmount || 0;
    const maxUsablePoints = Math.min(availablePoints, totalAmount);
    
    document.getElementById('use-points-input').value = maxUsablePoints;
    seatBookingManager.usedPoints = maxUsablePoints;
    updateFinalPayment();
}

// 쿠폰 적용
async function applyCoupon() {
    const couponCode = document.getElementById('coupon-code-input').value.trim();
    if (!couponCode) {
        alert('쿠폰 코드를 입력해주세요.');
        return;
    }
    
    try {
        const response = await fetch('/api/reservations/calculate-price', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                totalAmount: seatBookingManager.totalAmount,
                usedPoints: seatBookingManager.usedPoints,
                couponCode: couponCode
            })
        });
        
        if (response.ok) {
            const result = await response.json();
            if (result.success) {
                seatBookingManager.couponCode = couponCode;
                seatBookingManager.couponDiscount = result.couponDiscount || 0;
                updateFinalPayment();
                alert('쿠폰이 적용되었습니다.');
            } else {
                alert(result.message || '쿠폰 적용에 실패했습니다.');
            }
        } else {
            alert('쿠폰 적용 중 오류가 발생했습니다.');
        }
    } catch (error) {
        alert('쿠폰 적용 중 오류가 발생했습니다.');
    }
}

// 최종 결제 금액 업데이트
function updateFinalPayment() {
    const totalAmount = seatBookingManager.totalAmount || 0;
    const usedPoints = seatBookingManager.usedPoints || 0;
    const couponDiscount = seatBookingManager.couponDiscount || 0;
    
    // 할인 정보 표시
    const discountInfo = document.getElementById('discount-info');
    if (usedPoints > 0 || couponDiscount > 0) {
        discountInfo.style.display = 'block';
        document.getElementById('point-discount').textContent = `-₩${usedPoints.toLocaleString()}`;
        document.getElementById('coupon-discount').textContent = `-₩${couponDiscount.toLocaleString()}`;
    } else {
        discountInfo.style.display = 'none';
    }
    
    // 최종 결제 금액 계산
    const finalAmount = Math.max(0, totalAmount - usedPoints - couponDiscount);
    document.getElementById('final-payment-amount').textContent = `₩${finalAmount.toLocaleString()}`;
    
    // 포인트 사용량 입력 필드 이벤트 리스너
    const pointsInput = document.getElementById('use-points-input');
    pointsInput.oninput = function() {
        const inputValue = parseInt(this.value) || 0;
        const availablePoints = seatBookingManager.availablePoints || 0;
        const totalAmount = seatBookingManager.totalAmount || 0;
        
        // 입력값 검증
        if (inputValue > availablePoints) {
            this.value = availablePoints;
            seatBookingManager.usedPoints = availablePoints;
        } else if (inputValue > totalAmount) {
            this.value = totalAmount;
            seatBookingManager.usedPoints = totalAmount;
        } else {
            seatBookingManager.usedPoints = inputValue;
        }
        
        updateFinalPayment();
    };
}

async function processPayment() {
    // 입력값 검증
    const usedPoints = parseInt(document.getElementById('use-points-input').value) || 0;
    const couponCode = document.getElementById('coupon-code-input').value.trim();
    
    if (seatBookingManager.selectedSeats.length === 0) {
        alert('좌석을 선택해주세요.');
        return;
    }
    
    if (!seatBookingManager.passengers || seatBookingManager.passengers.length === 0) {
        alert('탑승자 정보를 입력해주세요.');
        return;
    }
    
    const finalAmount = parseInt(document.getElementById('final-payment-amount').textContent.replace(/[^\d]/g, ''));
    
    const confirmation = confirm(
        `결제를 진행하시겠습니까?\n\n` +
        `선택된 좌석: ${seatBookingManager.selectedSeats.map(seat => seat.id).join(', ')}\n` +
        `탑승자: ${seatBookingManager.passengers.map(p => p.name).join(', ')}\n` +
        `사용 포인트: ${usedPoints.toLocaleString()}P\n` +
        `쿠폰: ${couponCode || '없음'}\n` +
        `최종 결제 금액: ₩${finalAmount.toLocaleString()}`
    );
    
    if (confirmation) {
        try {
            // 현재 사용자 ID 확인
            if (!seatBookingManager.currentUserId) {
                alert('사용자 정보를 가져올 수 없습니다. 다시 시도해주세요.');
                return;
            }
            
            // 서버에 예약 요청 전송
            const bookingData = {
                flightId: seatBookingManager.flightId,
                selectedSeats: seatBookingManager.selectedSeats.map(seat => seat.id),
                passengers: seatBookingManager.passengers,
                usedPoints: usedPoints,
                couponCode: couponCode || null,
                totalAmount: seatBookingManager.totalAmount,
                paymentMethod: 'point'
            };
            
            const response = await fetch(`/api/reservations/process?userId=${seatBookingManager.currentUserId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(bookingData)
            });
            
            const result = await response.json();
            
            if (result.success) {
                alert('예약이 완료되었습니다!');
                window.location.href = '/flights/search';
            } else {
                alert(`예약 실패: ${result.message}`);
            }
        } catch (error) {
            console.error('예약 처리 오류:', error);
            alert('예약 처리 중 오류가 발생했습니다. 다시 시도해주세요.');
        }
    }
}

// 전역 변수
let seatBookingManager;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    seatBookingManager = new SeatBookingManager();
}); 