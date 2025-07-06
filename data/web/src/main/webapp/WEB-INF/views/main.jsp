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
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;600;700&display=swap" rel="stylesheet">
</head>
<body>
    <%@ include file="header.jsp" %>
    
    <section class="hero-section">
        <div class="hero-background">
            <div class="hero-overlay"></div>
        </div>
        <div class="hero-content">
            <div class="container">
                <div class="hero-text">
                    <h1 class="hero-title">WH_AIR</h1>
                    <p class="hero-subtitle">세계를 연결하는 프리미엄 항공사</p>
                    <p class="hero-description">안전하고 편안한 여행을 위한 최고의 서비스를 제공합니다</p>
                    <div class="hero-buttons">
                        <a href="/flightSearch" class="hero-btn hero-btn-primary">
                            <span class="btn-icon">✈</span>
                            항공편 검색
                        </a>
                        <c:if test="${empty user}">
                            <a href="/login" class="hero-btn hero-btn-secondary">
                                <span class="btn-icon">👤</span>
                                로그인
                            </a>
                        </c:if>
                    </div>
                </div>
                <div class="hero-visual">
                    <div class="plane-animation">
                        <div class="plane">✈</div>
                        <div class="clouds">
                            <div class="cloud cloud-1">☁</div>
                            <div class="cloud cloud-2">☁</div>
                            <div class="cloud cloud-3">☁</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>
    
    <section class="features-section">
        <div class="container">
            <div class="section-header">
                <h2 class="section-title">WH_AIR의 특별한 서비스</h2>
                <p class="section-subtitle">고객 만족을 위한 최고의 항공 서비스</p>
            </div>
            <div class="features-grid">
                <div class="feature-card">
                    <div class="feature-icon">
                        <span class="icon">🌍</span>
                    </div>
                    <h3 class="feature-title">글로벌 네트워크</h3>
                    <p class="feature-description">전 세계 주요 도시를 연결하는 광범위한 항공 네트워크로 어디든 편리하게 이동하세요.</p>
                </div>
                <div class="feature-card">
                    <div class="feature-icon">
                        <span class="icon">🛡️</span>
                    </div>
                    <h3 class="feature-title">최고의 안전성</h3>
                    <p class="feature-description">국제 안전 기준을 초과하는 최고 수준의 안전 시스템으로 안전한 여행을 보장합니다.</p>
                </div>
                <div class="feature-card">
                    <div class="feature-icon">
                        <span class="icon">💎</span>
                    </div>
                    <h3 class="feature-title">프리미엄 서비스</h3>
                    <p class="feature-description">고급 객실과 최고 품질의 기내 서비스로 편안하고 럭셔리한 여행을 경험하세요.</p>
                </div>
                <div class="feature-card">
                    <div class="feature-icon">
                        <span class="icon">🎯</span>
                    </div>
                    <h3 class="feature-title">정확한 시간</h3>
                    <p class="feature-description">높은 정시성으로 약속된 시간에 정확히 도착하여 귀중한 시간을 절약하세요.</p>
                </div>
            </div>
        </div>
    </section>

    <section class="stats-section">
        <div class="container">
            <div class="stats-grid">
                <div class="stat-item">
                    <div class="stat-number">150+</div>
                    <div class="stat-label">목적지</div>
                </div>
                <div class="stat-item">
                    <div class="stat-number">500+</div>
                    <div class="stat-label">일일 운항</div>
                </div>
                <div class="stat-item">
                    <div class="stat-number">99.8%</div>
                    <div class="stat-label">정시성</div>
                </div>
                <div class="stat-item">
                    <div class="stat-number">10M+</div>
                    <div class="stat-label">연간 승객</div>
                </div>
            </div>
        </div>
    </section>

    <section class="cta-section">
        <div class="container">
            <div class="cta-content">
                <h2 class="cta-title">지금 바로 여행을 시작하세요</h2>
                <p class="cta-description">WH_AIR와 함께 특별한 여행 경험을 만들어보세요</p>
                <a href="/flights/search" class="cta-button">
                    <span class="btn-icon">🎫</span>
                    항공권 예약하기
                </a>
            </div>
        </div>
    </section>
</body>
</html> 