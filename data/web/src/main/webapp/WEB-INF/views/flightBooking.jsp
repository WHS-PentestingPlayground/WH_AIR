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
                    <strong>${flight.flightNumber}</strong> | ${flight.departureAirport} → ${flight.arrivalAirport}
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
                    <!-- 결제 정보 -->
                    <div class="payment-summary">
                        <h3>결제 정보</h3>
                        <div class="price-breakdown">
                            <div class="price-item">
                                <div class="price-row">
                                    <span class="price-label">운임비</span>
                                    <div class="coupon-section">
                                        <select id="seat-coupon-select" class="coupon-dropdown" onchange="applyCoupon('seat')">
                                            <option value="">쿠폰 선택</option>
                                        </select>
                                    </div>
                                </div>
                                <div class="price-values">
                                    <span class="original-price" id="seat-original-price">₩0</span>
                                    <span class="discount-price" id="seat-discount-price" style="display: none;">-₩0</span>
                                    <span class="price-value" id="seat-price">₩0</span>
                                </div>
                            </div>
                            <div class="price-item">
                                <div class="price-row">
                                    <span class="price-label">유류할증료</span>
                                    <div class="coupon-section">
                                        <select id="fuel-coupon-select" class="coupon-dropdown" onchange="applyCoupon('fuel')">
                                            <option value="">쿠폰 선택</option>
                                        </select>
                                    </div>
                                </div>
                                <div class="price-values">
                                    <span class="original-price" id="fuel-original-price">₩0</span>
                                    <span class="discount-price" id="fuel-discount-price" style="display: none;">-₩0</span>
                                    <span class="price-value" id="fuel-price">₩0</span>
                                </div>
                            </div>
                            <div class="price-item total">
                                <span class="price-label">총 결제 금액</span>
                                <span class="price-value" id="total-payment">₩0</span>
                            </div>
                        </div>
                    </div>
                    
                    <!-- 포인트 결제 -->
                    <div class="payment-form">
                        <h3>포인트 결제</h3>
                        <div class="point-payment">
                            <div class="point-info">
                                <span class="point-label">보유 포인트:</span>
                                <span class="point-value" id="available-points">0P</span>
                            </div>
                            <div class="point-usage">
                                <span class="point-label">사용할 포인트:</span>
                                <span class="point-value" id="use-points">0P</span>
                            </div>
                        </div>
                        <button class="confirm-btn" id="final-payment-btn" onclick="processPayment()">
                            포인트 결제하기
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
        document.getElementById('flight-booking-app').dataset.userId = '${user.id}';
    </script>
</body>
</html> 