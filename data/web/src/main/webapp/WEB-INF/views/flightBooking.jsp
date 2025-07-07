<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>좌석 선택 및 결제 - WH Air</title>
    <link href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css" rel="stylesheet">
    <link rel="stylesheet" href="/static/css/flight.css">
    <link rel="stylesheet" href="/static/css/header.css">
</head>
<body>
    <%@ include file="header.jsp" %>
    <div class="container" id="flight-booking-app">
        <div class="booking-header">
            <h1 class="booking-title">좌석 선택 및 결제</h1>
            <div class="flight-info">
                <div class="flight-details">
                    <strong>항공편 : ${flight.flightNumber}</strong> | 출발지 : ${flight.departureAirport} → 도착지 : ${flight.arrivalAirport}
                </div>
                <div class="flight-time">
                    ${flight.formattedDepartureTime}
                </div>
            </div>
        </div>

        <!-- STEP 1: 좌석 선택 -->
        <div class="step-section" id="step1">
            <div class="step-header" onclick="toggleStep('step1')">
                <div class="step-info">
                    <span class="step-number">STEP 1</span>
                    <span class="step-title">좌석 선택</span>
                </div>
                <span class="step-toggle">▼</span>
            </div>
            <div class="step-content">
                <div class="aircraft-container">
                    <div class="aircraft-body">
                        <div class="cockpit">
                            <span class="cockpit-icon">✈️</span>
                        </div>
                        
                        <!-- 퍼스트 클래스 -->
                        <div class="class-section first-class">
                            <div class="class-title">First Class</div>
                            <div id="first-class-seats" class="seats-container"></div>
                        </div>

                        <!-- 비즈니스 클래스 -->
                        <div class="class-section business-class">
                            <div class="class-title">Business Class</div>
                            <div id="business-class-seats" class="seats-container"></div>
                        </div>

                        <!-- 이코노미 클래스 -->
                        <div class="class-section economy-class">
                            <div class="class-title">Economy Class</div>
                            <div id="economy-class-seats" class="seats-container"></div>
                        </div>
                    </div>

                    <div class="legend">
                        <div class="legend-item">
                            <div class="legend-seat available"></div>
                            <span>선택 가능</span>
                        </div>
                        <div class="legend-item">
                            <div class="legend-seat selected"></div>
                            <span>선택됨</span>
                        </div>
                        <div class="legend-item">
                            <div class="legend-seat disabled"></div>
                            <span>선택 불가</span>
                        </div>
                        <div class="legend-item">
                            <div class="legend-seat occupied"></div>
                            <span>예약됨</span>
                        </div>
                    </div>
                </div>

                <div class="selection-info">
                    <div class="selected-seats">
                        <strong>선택한 좌석:</strong>
                        <div id="selected-seats-display">선택된 좌석이 없습니다.</div>
                    </div>
                    <button class="confirm-btn" id="confirm-seat-btn" onclick="confirmSeatSelection()" disabled>
                        좌석 선택 확정
                    </button>
                </div>
            </div>
        </div>

        <!-- STEP 2: 탑승자 정보 -->
        <div class="step-section" id="step2">
            <div class="step-header" onclick="toggleStep('step2')">
                <div class="step-info">
                    <span class="step-number">STEP 2</span>
                    <span class="step-title">탑승자 정보</span>
                </div>
                <span class="step-toggle">▼</span>
            </div>
            <div class="step-content" style="display: none;">
                <div class="passenger-info-section">
                    <h3>탑승자 정보 입력</h3>
                    <div id="passenger-forms">
                        <!-- 탑승자 정보 폼이 동적으로 생성됩니다 -->
                    </div>
                    <button class="confirm-btn" id="confirm-passenger-btn" onclick="confirmPassengerInfo()" disabled>
                        탑승자 정보 확정
                    </button>
                </div>
            </div>
        </div>

        <!-- STEP 3: 결제 -->
        <div class="step-section" id="step3">
            <div class="step-header" onclick="toggleStep('step3')">
                <div class="step-info">
                    <span class="step-number">STEP 3</span>
                    <span class="step-title">결제</span>
                </div>
                <span class="step-toggle">▼</span>
            </div>
            <div class="step-content" style="display: none;">
                <div class="payment-section">
                    <div class="payment-summary">
                        <h3>결제 정보</h3>
                        <div class="price-breakdown">
                            <div class="price-item">
                                <span class="price-label">기본 운임</span>
                                <span class="price-value" id="seat-price">₩0</span>
                            </div>
                            <div class="price-item">
                                <span class="price-label">유류할증료</span>
                                <span class="price-value" id="fuel-price">₩0</span>
                            </div>
                            <div class="price-item total">
                                <span class="price-label">총 결제 금액</span>
                                <span class="price-value" id="total-payment">₩0</span>
                            </div>
                        </div>
                    </div>
                    <div class="payment-form">
                        <h3>포인트 결제</h3>
                        
                        <!-- 포인트 정보 -->
                        <div class="point-payment">
                            <div class="point-info">
                                <span class="point-label">보유 포인트:</span>
                                <span class="point-value" id="available-points">로딩 중...</span>
                            </div>
                            <div class="point-usage">
                                <label for="use-points-input">사용할 포인트:</label>
                                <input type="number" id="use-points-input" min="0" step="1000" placeholder="사용할 포인트를 입력하세요">
                                <button type="button" onclick="useAllPoints()" class="small-btn">전액 사용</button>
                            </div>
                        </div>
                        
                        <!-- 쿠폰 정보 -->
                        <div class="coupon-payment">
                            <div class="coupon-info">
                                <span class="coupon-label">보유 쿠폰:</span>
                                <span class="coupon-value" id="available-coupon">로딩 중...</span>
                            </div>
                            <div class="coupon-usage">
                                <label for="coupon-code-input">쿠폰 코드:</label>
                                <input type="text" id="coupon-code-input" placeholder="쿠폰 코드를 입력하세요">
                                <button type="button" onclick="applyCoupon()" class="small-btn">적용</button>
                            </div>
                        </div>
                        
                        <!-- 할인 정보 -->
                        <div class="discount-info" id="discount-info" style="display: none;">
                            <div class="discount-item">
                                <span class="discount-label">포인트 할인:</span>
                                <span class="discount-value" id="point-discount">₩0</span>
                            </div>
                            <div class="discount-item">
                                <span class="discount-label">쿠폰 할인:</span>
                                <span class="discount-value" id="coupon-discount">₩0</span>
                            </div>
                        </div>
                        
                        <!-- 최종 결제 금액 -->
                        <div class="final-payment">
                            <div class="final-price-item">
                                <span class="final-price-label">최종 결제 금액:</span>
                                <span class="final-price-value" id="final-payment-amount">₩0</span>
                            </div>
                        </div>
                        
                        <button class="confirm-btn" id="final-payment-btn" onclick="processPayment()">
                            결제 완료
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="/static/js/flightBooking.js"></script>
    <script>
        // 서버 데이터를 HTML 데이터 속성으로 전달 (window 객체 대신)
        document.getElementById('flight-booking-app').dataset.flightId = '${flightId}';
        document.getElementById('flight-booking-app').dataset.selectedSeatClass = '${selectedSeatClass}';
        document.getElementById('flight-booking-app').dataset.seatPrice = '${flight.seatPrice}';
        document.getElementById('flight-booking-app').dataset.fuelPrice = '${flight.fuelPrice}';
        
        // 현재 사용자 정보 (JWT 토큰에서 추출된 정보)
        <c:if test="${not empty user}">
            document.getElementById('flight-booking-app').dataset.userId = '${user.id}';
            document.getElementById('flight-booking-app').dataset.username = '${user.name}';
        </c:if>
    </script>
</body>
</html> 