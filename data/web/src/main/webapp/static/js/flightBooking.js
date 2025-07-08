/**
 * 좌석 선택 및 결제 관련 JavaScript
 */

class SeatBookingManager {
    constructor() {
        this.selectedSeats = [];
        this.maxSeats = 1; // 최대 선택 가능 좌석 수
        this.passengers = []; // 탑승자 정보
        this.coupons = {}; // 사용 가능한 쿠폰 정보
        this.userPoints = 0; // 사용자 포인트
        this.appliedCoupons = { seat: null, fuel: null }; // 적용된 쿠폰
        
        // HTML 데이터 속성에서 서버 데이터 가져오기 (window 객체 대신)
        const appElement = document.getElementById('flight-booking-app');
        this.flightId = appElement?.dataset.flightId || 0;
        this.selectedSeatClass = appElement?.dataset.selectedSeatClass || 'economy';
        this.seatPrice = parseInt(appElement?.dataset.seatPrice) || 300000;
        this.fuelPrice = parseInt(appElement?.dataset.fuelPrice) || 80000;
        this.userId = appElement?.dataset.userId || 0;
        
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
            const url = `/api/flights/${this.flightId}/seats?seatClass=${this.selectedSeatClass}`;
            
            const response = await fetch(url);
            if (!response.ok) {
                throw new Error(`좌석 정보를 가져오는데 실패했습니다. Status: ${response.status}`);
            }
            
            const seatData = await response.json();
            
            // allSeats, reservedSeats, availableSeats 모두 내려온다고 가정
            this.updateSeatStatus(seatData.allSeats || [], seatData.reservedSeats || [], seatData.availableSeats || []);
        } catch (error) {
            // 에러 발생 시 모든 좌석을 비활성화 상태로 유지
        }
    }

    /**
     * 전체 좌석, 예약된 좌석, 예약 가능한 좌석을 구분해서 표시
     */
    updateSeatStatus(allSeats, reservedSeats, availableSeats) {
        // 1. 전체 좌석을 disabled(회색)로 초기화
        allSeats.forEach(seatId => {
            const seat = document.getElementById(seatId);
            if (seat) {
                seat.classList.remove('available', 'occupied', 'disabled');
                seat.classList.add('disabled');
            }
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
    
    // 쿠폰 정보 로드 및 결제 정보 업데이트
    loadCouponsAndUpdatePayment();
}

function updatePaymentInfo() {
    const passengerCount = seatBookingManager.selectedSeats.length;
    const seatPricePerPerson = seatBookingManager.seatPrice;
    const fuelPricePerPerson = seatBookingManager.fuelPrice;
    
    // 탑승자 수에 따른 총 금액 계산
    const totalSeatPrice = seatPricePerPerson * passengerCount;
    const totalFuelPrice = fuelPricePerPerson * passengerCount;
    
    // 쿠폰 적용 계산
    let finalSeatPrice = totalSeatPrice;
    let finalFuelPrice = totalFuelPrice;
    let seatDiscount = 0;
    let fuelDiscount = 0;
    
    // 운임비 쿠폰 적용
    if (seatBookingManager.appliedCoupons.seat) {
        const couponName = seatBookingManager.appliedCoupons.seat;
        const discountRate = seatBookingManager.coupons[couponName] || 0;
        seatDiscount = Math.floor(totalSeatPrice * discountRate);
        finalSeatPrice = totalSeatPrice - seatDiscount;
    }
    
    // 유류할증료 쿠폰 적용
    if (seatBookingManager.appliedCoupons.fuel) {
        const couponName = seatBookingManager.appliedCoupons.fuel;
        const discountRate = seatBookingManager.coupons[couponName] || 0;
        fuelDiscount = Math.floor(totalFuelPrice * discountRate);
        finalFuelPrice = totalFuelPrice - fuelDiscount;
    }
    
    const totalPayment = finalSeatPrice + finalFuelPrice;
    
    // UI 업데이트
    updatePriceDisplay('seat', totalSeatPrice, seatDiscount, finalSeatPrice);
    updatePriceDisplay('fuel', totalFuelPrice, fuelDiscount, finalFuelPrice);
    document.getElementById('total-payment').textContent = `₩${totalPayment.toLocaleString()}`;
    
    // 포인트 사용 정보 업데이트
    document.getElementById('use-points').textContent = `${totalPayment.toLocaleString()}P`;
    document.getElementById('available-points').textContent = `${seatBookingManager.userPoints.toLocaleString()}P`;
}

function updatePriceDisplay(type, originalPrice, discount, finalPrice) {
    const originalElement = document.getElementById(`${type}-original-price`);
    const discountElement = document.getElementById(`${type}-discount-price`);
    const finalElement = document.getElementById(`${type}-price`);
    
    originalElement.textContent = `₩${originalPrice.toLocaleString()}`;
    discountElement.textContent = `-₩${discount.toLocaleString()}`;
    finalElement.textContent = `₩${finalPrice.toLocaleString()}`;
    
    // 할인이 있을 때만 원가와 할인가 표시
    if (discount > 0) {
        originalElement.style.display = 'inline';
        discountElement.style.display = 'inline';
    } else {
        originalElement.style.display = 'none';
        discountElement.style.display = 'none';
    }
}

async function loadCouponsAndUpdatePayment() {
    try {
        const response = await fetch('/api/user/coupons');
        if (response.ok) {
            const data = await response.json();
            seatBookingManager.coupons = data.availableCoupons || {};
            seatBookingManager.userPoints = data.points || 0;
            
            // 쿠폰 드롭다운 업데이트
            updateCouponDropdowns();
        }
    } catch (error) {
        console.error('쿠폰 정보 로드 실패:', error);
    }
    
    // 결제 정보 업데이트
    updatePaymentInfo();
}

function updateCouponDropdowns() {
    const seatSelect = document.getElementById('seat-coupon-select');
    const fuelSelect = document.getElementById('fuel-coupon-select');
    
    // 운임비 쿠폰 옵션 추가
    seatSelect.innerHTML = '<option value="">쿠폰 선택</option>';
    fuelSelect.innerHTML = '<option value="">쿠폰 선택</option>';
    
    Object.keys(seatBookingManager.coupons).forEach(couponName => {
        const discountRate = seatBookingManager.coupons[couponName];
        const displayText = `${couponName} (${Math.round(discountRate * 100)}% 할인)`;
        
        // 운임비 관련 쿠폰은 운임비 드롭다운에
        if (couponName.includes('운임') || couponName.includes('기본')) {
            const option = new Option(displayText, couponName);
            seatSelect.add(option);
        }
        
        // 유류할증료 관련 쿠폰은 유류할증료 드롭다운에
        if (couponName.includes('유류') || couponName.includes('연료') || couponName.includes('기본')) {
            const option = new Option(displayText, couponName);
            fuelSelect.add(option);
        }
    });
}

function applyCoupon(type) {
    const selectElement = document.getElementById(`${type}-coupon-select`);
    const selectedCoupon = selectElement.value;
    
    if (selectedCoupon) {
        seatBookingManager.appliedCoupons[type] = selectedCoupon;
    } else {
        seatBookingManager.appliedCoupons[type] = null;
    }
    
    // 결제 정보 업데이트
    updatePaymentInfo();
}

async function processPayment() {
    const passengerCount = seatBookingManager.selectedSeats.length;
    const seatPricePerPerson = seatBookingManager.seatPrice;
    const fuelPricePerPerson = seatBookingManager.fuelPrice;
    
    // 쿠폰 적용된 최종 금액 계산
    const totalSeatPrice = seatPricePerPerson * passengerCount;
    const totalFuelPrice = fuelPricePerPerson * passengerCount;
    
    let finalSeatPrice = totalSeatPrice;
    let finalFuelPrice = totalFuelPrice;
    let appliedCouponDetails = [];
    
    // 운임비 쿠폰 적용
    if (seatBookingManager.appliedCoupons.seat) {
        const couponName = seatBookingManager.appliedCoupons.seat;
        const discountRate = seatBookingManager.coupons[couponName] || 0;
        const seatDiscount = Math.floor(totalSeatPrice * discountRate);
        finalSeatPrice = totalSeatPrice - seatDiscount;
        appliedCouponDetails.push(`운임비 쿠폰: ${couponName} (-₩${seatDiscount.toLocaleString()})`);
    }
    
    // 유류할증료 쿠폰 적용
    if (seatBookingManager.appliedCoupons.fuel) {
        const couponName = seatBookingManager.appliedCoupons.fuel;
        const discountRate = seatBookingManager.coupons[couponName] || 0;
        const fuelDiscount = Math.floor(totalFuelPrice * discountRate);
        finalFuelPrice = totalFuelPrice - fuelDiscount;
        appliedCouponDetails.push(`유류할증료 쿠폰: ${couponName} (-₩${fuelDiscount.toLocaleString()})`);
    }
    
    const totalAmount = finalSeatPrice + finalFuelPrice;
    const requiredPoints = totalAmount;
    
    // 포인트 부족 체크
    if (seatBookingManager.userPoints < requiredPoints) {
        alert(`포인트가 부족합니다.\n보유 포인트: ${seatBookingManager.userPoints.toLocaleString()}P\n필요 포인트: ${requiredPoints.toLocaleString()}P`);
        return;
    }
    
    let confirmMessage = `포인트 결제를 진행하시겠습니까?\n\n` +
        `원래 금액: ₩${(totalSeatPrice + totalFuelPrice).toLocaleString()}\n`;
    
    if (appliedCouponDetails.length > 0) {
        confirmMessage += `적용된 쿠폰:\n${appliedCouponDetails.join('\n')}\n`;
    }
    
    confirmMessage += `최종 결제 금액: ₩${totalAmount.toLocaleString()}\n` +
        `사용 포인트: ${requiredPoints.toLocaleString()}P\n\n` +
        `선택된 좌석: ${seatBookingManager.selectedSeats.map(seat => seat.id).join(', ')}\n` +
        `탑승자: ${seatBookingManager.passengers.map(p => p.name).join(', ')}`;
    
    const confirmation = confirm(confirmMessage);
    
    if (confirmation) {
        try {
            // 서버에 예약 요청 전송 (기존 API 엔드포인트 사용)
            const bookingData = {
                userId: seatBookingManager.userId,
                flightId: seatBookingManager.flightId,
                seatNumbers: seatBookingManager.selectedSeats.map(seat => seat.id),
                passengerName: seatBookingManager.passengers[0]?.name || '탑승자',
                passengerBirth: seatBookingManager.passengers[0]?.birth || '1990-01-01',
                usedPoints: totalAmount
            };
            
            const response = await fetch('/api/reservations/finalize', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(bookingData)
            });
            
            const result = await response.json();
            
            if (result.success) {
                alert('예약이 완료되었습니다!');
                window.location.href = '/search';
            } else {
                alert(`예약 실패: ${result.message}`);
            }
        } catch (error) {
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