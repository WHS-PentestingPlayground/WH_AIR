<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>에러 발생</title>
    <link rel="stylesheet" href="/static/css/error.css">
</head>
<body>
    <div class="error-container">
        <div class="error-code">
            <c:out value="${statusCode}" default="500"/>
        </div>
        <div class="error-message">
            <c:out value="${errorMessage}" default="예기치 않은 오류가 발생했습니다."/>
        </div>
        <c:if test="${not empty exception}">
            <div class="error-details">
                <p>상세 오류: <c:out value="${exception.message}"/></p>
                <c:if test="${not empty exception.cause}">
                    <p>원인: <c:out value="${exception.cause.message}"/></p>
                </c:if>
            </div>
        </c:if>
        <a href="/" class="back-button">홈으로 돌아가기</a>
    </div>
</body>
</html>