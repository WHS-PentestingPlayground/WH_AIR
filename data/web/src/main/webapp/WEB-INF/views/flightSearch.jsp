<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <title>항공권 검색 - WH Air</title>
    <meta charset="UTF-8">
    <link href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css" rel="stylesheet">
    <link rel="stylesheet" href="/static/css/flightSearch.css">
    <link rel="stylesheet" href="/static/css/header.css">
    <script src="/static/js/flightSearch.js"></script>
</head>
<body>
    <%@ include file="header.jsp" %>
<div class="container">
    <div class="search-section">
        <h1 class="search-title">항공권 검색</h1>
        <form class="search-form" action="/search" method="post">
            <div class="search-row">
                <div class="search-field search-departure">
                    <label for="departure-airport" class="search-label">출발지</label>
                    <input type="text" id="departure-airport" name="departure_airport" class="search-input" value="ICN" readonly>
                </div>
                <div class="search-field search-arrival">
                    <label for="arrival-airport" class="search-label">도착지</label>
                    <input type="text" id="arrival-airport" name="arrival_airport" class="search-input" value="YVR" readonly>
                </div>
                <div class="search-field search-departure-date">
                    <label for="departure-date" class="search-label">출발 일시</label>
                    <input type="date" id="departure-date" name="departure_date" class="search-input" value="2025-08-02" readonly>
                </div>
                <div class="search-field search-arrival-date">
                    <label for="arrival-date" class="search-label">도착 일시</label>
                    <input type="date" id="arrival-date" name="arrival_date" class="search-input" value="2025-08-02" readonly>
                </div>
                <div class="search-field search-seat-class">
                    <label for="seat-class" class="search-label">여행자 및 좌석 등급</label>
                    <select id="seat-class" name="class" class="search-select" disabled>
                        <option value="economy">이코노미</option>
                        <option value="business">비즈니스</option>
                        <option value="first">퍼스트</option>
                    </select>
                    <input type="hidden" name="class" value="economy">
                </div>
                <button type="submit" class="search-btn">검색하기</button>
            </div>
        </form>
    </div>
    <div class="results-section">
        <h2 class="results-title">검색 결과</h2>
        <c:if test="${searchPerformed and not empty searchResults}">
            <table class="results-table">
                <thead>
                    <tr>
                        <th>항공편 번호</th>
                        <th>출발</th>
                        <th>도착</th>
                        <th>출발 시간</th>
                        <th>도착 시간</th>
                        <th>항공사</th>
                        <th>항공기</th>
                        <th>좌석 등급</th>
                        <th>예매</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="flight" items="${searchResults}">
                        <tr>
                            <td>${flight.flightNumber}</td>
                            <td>${flight.departureAirport}</td>
                            <td>${flight.arrivalAirport}</td>
                            <td>${flight.formattedDepartureTime}</td>
                            <td>${flight.formattedArrivalTime}</td>
                            <td>${flight.airline}</td>
                            <td>${flight.aircraftModel}</td>
                            <td>${flight.seatClass.substring(0,1).toUpperCase()}${flight.seatClass.substring(1)}</td>
                            <td>
                                <button type="button" class="book-btn"
                                        data-seat-price="${flight.seatPrice}"
                                        data-fuel-price="${flight.fuelPrice}"
                                        data-flight-id="${flight.flightId}"
                                        data-seat-class="${flight.seatClass}">
                                    조회
                                </button>
                            </td>
                        </tr>
                        <tr class="price-info-row" style="display:none;">
                            <td colspan="9" class="price-info-cell"></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:if>
        <c:if test="${searchPerformed and (empty searchResults or noResults)}">
            <div class="no-results">
                <p>${not empty message ? message : '검색 결과가 없습니다.'}</p>
            </div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="no-results" style="color: red;">
                <p>오류: ${error}</p>
            </div>
        </c:if>
    </div>
</div>
</body>
</html> 
