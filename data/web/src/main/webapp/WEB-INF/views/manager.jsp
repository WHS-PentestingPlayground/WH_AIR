<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Manager - WH Air</title>
    <link rel="stylesheet" href="/static/css/header.css">
    <link rel="stylesheet" href="/static/css/manager.css">
</head>
<body>
    <jsp:include page="header.jsp" />
    
    <div class="container">
        <h1>예약 관리</h1>
        
        <div class="reservation-list">
            <table>
                <thead>
                    <tr>
                        <th>예약 ID</th>
                        <th>승객명</th>
                        <th>항공편</th>
                        <th>출발</th>
                        <th>도착</th>
                        <th>현재 좌석</th>
                        <th>예약일</th>
                        <th>작업</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="reservation" items="${reservations}">
                        <tr>
                            <td>${reservation.id}</td>
                            <td>${reservation.passengerName}</td>
                            <td>${reservation.flight.flightNumber}</td>
                            <td>${reservation.flight.departureAirport}</td>
                            <td>${reservation.flight.arrivalAirport}</td>
                            <td>
                                <span class="seat-info">
                                    ${reservation.seat.seatNumber} 
                                    <span class="class-badge ${reservation.seat.seatClass}">${reservation.seat.seatClass}</span>
                                </span>
                            </td>
                            <td>
                                ${reservation.bookedAt}
                            </td>
                            <td>
                                <c:if test="${reservation.seat.seatClass eq 'economy'}">
                                    <button class="change-seat-btn" onclick="changeSeat(${reservation.id})">
                                        좌석 변경
                                    </button>
                                </c:if>
                                <c:if test="${reservation.seat.seatClass ne 'economy'}">
                                    <span class="no-change">변경 불가</span>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <script>
        function changeSeat(reservationId) {
            window.location.href = '/manager/change-seat/' + reservationId;
        }
    </script>
</body>
</html> 