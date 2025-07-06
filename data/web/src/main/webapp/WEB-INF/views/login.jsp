<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>로그인 - WH_AIR</title>
  <link rel="stylesheet" href="/static/css/header.css">
  <link rel="stylesheet" href="/static/css/login.css">
  <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;600;700&display=swap" rel="stylesheet">
</head>
<body>
<%@ include file="header.jsp" %>
<div class="login-section">
  <div class="login-background">
    <div class="login-overlay"></div>
  </div>
  <div class="login-container">
    <div class="logo-section">
      <div class="logo-icon">✈</div>
      <h1 class="airline-logo">WH_AIR</h1>
      <p class="airline-slogan">세계를 연결하는 항공사</p>
    </div>
    <h2 class="form-title">로그인</h2>
    
    <% if (request.getAttribute("error") != null) { %>
      <div class="error-message">
        <%= request.getAttribute("error") %>
      </div>
    <% } %>
    
    <% if (request.getAttribute("success") != null) { %>
      <div class="success-message">
        <%= request.getAttribute("success") %>
      </div>
    <% } %>
    
    <form action="/login" method="post" class="login-form">
      <div class="form-group">
        <label for="name" class="form-label">사용자명</label>
        <div class="input-wrapper">
          <input type="text" id="name" name="name" class="form-input" required 
                 placeholder="사용자명을 입력하세요">
        </div>
      </div>
      <div class="form-group">
        <label for="password" class="form-label">비밀번호</label>
        <div class="input-wrapper">
          <input type="password" id="password" name="password" class="form-input" required
                 placeholder="비밀번호를 입력하세요">
        </div>
      </div>
      <button type="submit" class="form-button">
        로그인
      </button>
    </form>
    
    <div class="form-links">
      <a href="/register" class="form-link">회원가입</a>
      <span class="divider">|</span>
      <a href="/" class="form-link">홈으로 돌아가기</a>
    </div>
  </div>
</div>
</body>
</html>
