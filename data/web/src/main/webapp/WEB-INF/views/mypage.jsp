<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>마이페이지 - WH_AIR</title>
  <link rel="stylesheet" href="/static/css/header.css">
  <link rel="stylesheet" href="/static/css/mypage.css">
  <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;600;700&display=swap" rel="stylesheet">
</head>
<body>
<%@ include file="header.jsp" %>

<div class="mypage-container">
  <div class="mypage-header">
    <h1>마이페이지</h1>
    <p>안녕하세요, ${user.name}님!</p>
  </div>

  <!-- 회원 정보 섹션 -->
  <div class="info-section">
    <h2>회원 정보</h2>
    <div class="info-grid">
      <div class="info-item">
        <label>사용자명</label>
        <span>${user.name}</span>
      </div>
      <div class="info-item">
        <label>이메일</label>
        <span>${user.email}</span>
      </div>
      <div class="info-item">
        <label>전화번호</label>
        <span>${user.phoneNumber}</span>
      </div>
      <div class="info-item">
        <label>가입일</label>
        <span><fmt:formatDate value="${user.createdAt}" pattern="yyyy년 MM월 dd일"/></span>
      </div>
    </div>
  </div>

  <!-- 보유 금액 및 쿠폰 섹션 -->
  <div class="assets-section">
    <h2>보유 자산</h2>
    <div class="assets-grid">
      <div class="asset-card point-card">
        <div class="asset-icon">💰</div>
        <div class="asset-info">
          <h3>포인트</h3>
          <p class="asset-amount"><fmt:formatNumber value="${user.point}" type="number"/> P</p>
        </div>
      </div>
      <div class="asset-card coupon-card">
        <div class="asset-icon">🎫</div>
        <div class="asset-info">
          <h3>쿠폰</h3>
          <c:choose>
            <c:when test="${not empty user.coupon}">
              <p class="asset-amount">${user.coupon}</p>
            </c:when>
            <c:otherwise>
              <p class="asset-amount">보유 쿠폰 없음</p>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>
  </div>

  <!-- 예약 티켓 조회 섹션 -->
  <div class="reservations-section">
    <h2>예약 내역</h2>
    <c:choose>
      <c:when test="${not empty reservations}">
        <div class="reservations-list">
          <c:forEach var="reservation" items="${reservations}">
            <div class="reservation-card">
              <div class="reservation-header">
                <h3>예약 번호: ${reservation.reservationId}</h3>
                <span class="status-badge ${reservation.status.toLowerCase()}">${reservation.status}</span>
              </div>
              <div class="flight-info">
                <div class="route-info">
                  <div class="departure">
                    <span class="airport">${reservation.departureAirport}</span>
                    <span class="time"><fmt:formatDate value="${reservation.departureTime}" pattern="MM/dd HH:mm"/></span>
                  </div>
                  <div class="flight-arrow">✈</div>
                  <div class="arrival">
                    <span class="airport">${reservation.arrivalAirport}</span>
                    <span class="time"><fmt:formatDate value="${reservation.arrivalTime}" pattern="MM/dd HH:mm"/></span>
                  </div>
                </div>
                <div class="flight-details">
                  <span class="flight-number">${reservation.flightNumber}</span>
                  <span class="seat-info">${reservation.seatClass} - ${reservation.seatNumber}</span>
                </div>
              </div>
              <div class="passenger-info">
                <p><strong>승객:</strong> ${reservation.passengerName}</p>
                <p><strong>총 금액:</strong> <fmt:formatNumber value="${reservation.totalPrice}" type="currency" currencySymbol="₩"/></p>
                <p><strong>예약일:</strong> <fmt:formatDate value="${reservation.bookedAt}" pattern="yyyy년 MM월 dd일"/></p>
              </div>
            </div>
          </c:forEach>
        </div>
      </c:when>
      <c:otherwise>
        <div class="no-reservations">
          <div class="no-data-icon">✈</div>
          <p>아직 예약한 항공편이 없습니다.</p>
          <a href="/search" class="search-flight-btn">항공편 검색하기</a>
        </div>
      </c:otherwise>
    </c:choose>
  </div>
</div>
</body>
</html>
