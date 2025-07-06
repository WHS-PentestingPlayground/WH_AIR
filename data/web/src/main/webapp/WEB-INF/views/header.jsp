<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page pageEncoding="UTF-8" %>

<header class="header">
  <nav class="header-nav">
    <div class="header-logo">
      <a href="/" class="header-logo-link">
        <div class="logo-icon">✈</div>
        <span class="header-logo-text">WH_AIR</span>
      </a>
    </div>

    <div class="header-menu">
      <a href="/" class="header-menu-item">홈</a>
      <a href="/flightSearch" class="header-menu-item">항공편 검색</a>
      <c:choose>
        <c:when test="${empty user}">
          <!-- 로그인 전: 로그인, 회원가입 -->
        </c:when>
        <c:otherwise>
          <!-- 로그인 후: 마이페이지 -->
          <a href="/mypage" class="header-menu-item">마이페이지</a>
        </c:otherwise>
      </c:choose>
    </div>

    <div class="header-user-menu" id="userMenu">
      <c:choose>
        <c:when test="${empty user}">
          <a href="/login" class="header-login-btn">로그인</a>
          <a href="/register" class="header-register-btn">회원가입</a>
        </c:when>
        <c:otherwise>
          <span class="header-welcome">안녕하세요, ${user.name}님</span>
          <a href="/logout" class="header-logout-btn">로그아웃</a>
        </c:otherwise>
      </c:choose>
    </div>
  </nav>
</header>
