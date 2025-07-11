<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>좌석 변경 - WH Air</title>
    <link rel="stylesheet" href="/static/css/header.css">
    <link rel="stylesheet" href="/static/css/manager.css">
</head>
<body>
    <jsp:include page="header.jsp" />
    
    <div class="container">
        <h1>좌석 변경</h1>
        
        <div class="reservation-info">
            <h2>예약 정보</h2>
            <table>
                <tr>
                    <th>예약 ID:</th>
                    <td>${reservation.id}</td>
                </tr>
                <tr>
                    <th>승객명:</th>
                    <td>${reservation.passengerName}</td>
                </tr>
                <tr>
                    <th>항공편:</th>
                    <td>${reservation.flight.flightNumber}</td>
                </tr>
                <tr>
                    <th>현재 좌석:</th>
                    <td>
                        <span class="seat-info">
                            ${reservation.seat.seatNumber} 
                            <span class="class-badge ${reservation.seat.seatClass}">${reservation.seat.seatClass}</span>
                        </span>
                    </td>
                </tr>
            </table>
        </div>
        
        <div class="seat-selection">
            <h2>사용 가능한 비즈니스 좌석</h2>
            <div class="seat-grid">
                <c:forEach var="seat" items="${availableSeats}">
                    <div class="seat-item" onclick="selectSeat(${seat.id})">
                        <div class="seat-number">${seat.seatNumber}</div>
                        <div class="seat-price">
                            <fmt:formatNumber value="${seat.seatPrice}" pattern="#,###"/>원
                        </div>
                    </div>
                </c:forEach>
            </div>
        </div>
        
        <div class="actions">
            <button class="btn-secondary" onclick="goBack()">취소</button>
            <button id="changeBtn" class="btn-primary" onclick="changeSeat()" disabled>좌석 변경</button>
        </div>
    </div>

    <script>
        let selectedSeatId = null;
        
        function selectSeat(seatId) {
            // 기존 선택 해제
            document.querySelectorAll('.seat-item').forEach(item => {
                item.classList.remove('selected');
            });
            
            // 새 선택
            event.target.closest('.seat-item').classList.add('selected');
            selectedSeatId = seatId;
            
            // 변경 버튼 활성화
            const changeBtn = document.getElementById('changeBtn');
            if (changeBtn) {
                changeBtn.disabled = false;
            }
        }
        
        function changeSeat() {
            if (!selectedSeatId) {
                alert('좌석을 선택해주세요.');
                return;
            }
            
            if (confirm('선택한 좌석으로 변경하시겠습니까?')) {
                fetch('/manager/change-seat/${reservation.id}', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: 'newSeatId=' + selectedSeatId
                })
                .then(response => response.text())
                .then(result => {
                    if (result.startsWith('success')) {
                        alert('좌석이 성공적으로 변경되었습니다.');
                        window.location.href = '/manager';
                    } else {
                        alert('좌석 변경에 실패했습니다: ' + result);
                    }
                })
                .catch(error => {
                    alert('오류가 발생했습니다: ' + error);
                });
            }
        }
        
        function goBack() {
            window.location.href = '/manager';
        }
    </script>
</body>
</html> 