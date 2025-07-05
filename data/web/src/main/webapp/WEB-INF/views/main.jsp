<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>WH_AIR - 세계를 연결하는 항공사</title>
    <link rel="stylesheet" href="/static/css/header.css">
    <link rel="stylesheet" href="/static/css/main.css">
</head>
<body>
    <%@ include file="header.jsp" %>
    
    <div class="hero-section">
        <div class="container">
            <h1 class="hero-title">WH_AIR</h1>
            <p class="hero-subtitle">세계를 연결하는 항공사</p>
            <div class="hero-buttons">
                <a href="/flightSearch" class="hero-btn hero-btn-primary">항공편 검색</a>
                <c:if test="${empty user}">
                    <a href="/login" class="hero-btn hero-btn-secondary">로그인</a>
                </c:if>
            </div>
        </div>
    </div>
    
    <div class="features-section">
        <div class="features-grid">
            <div class="feature-card">
                <div class="feature-icon">✈️</div>
                <h3 class="feature-title">글로벌 네트워크</h3>
                <p class="feature-description">전 세계 주요 도시를 연결하는 광범위한 항공 네트워크를 제공합니다.</p>
            </div>
            <div class="feature-card">
                <div class="feature-icon">🛡️</div>
                <h3 class="feature-title">안전한 여행</h3>
                <p class="feature-description">최고 수준의 안전 기준을 준수하여 안전하고 편안한 여행을 보장합니다.</p>
            </div>
            <div class="feature-card">
                <div class="feature-icon">💎</div>
                <h3 class="feature-title">프리미엄 서비스</h3>
                <p class="feature-description">고객 만족을 위한 최고 품질의 서비스와 편의시설을 제공합니다.</p>
            </div>
        </div>
    </div>
</body>
</html> 