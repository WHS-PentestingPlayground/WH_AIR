# 멀티스테이지 빌드 - 빌드 스테이지
FROM gradle:jdk17 AS builder

# 작업 디렉토리 설정
WORKDIR /home/gradle/src

# 루트 gradle 설정 복사
COPY build.gradle ./build.gradle
COPY settings.gradle ./settings.gradle
COPY gradle ./gradle
COPY gradlew ./gradlew
COPY gradlew.bat ./gradlew.bat

# Windows에서 생성된 스크립트를 Linux에서 실행하기 위한 필수 단계
RUN chmod +x ./gradlew && \
    (apt-get update && apt-get install -y dos2unix && dos2unix ./gradlew || sed -i 's/\r$//' ./gradlew)

# admin 소스 복사
COPY data/admin ./data/admin

# 빌드 실행 (admin 모듈만 빌드)
RUN ./gradlew :data:admin:clean :data:admin:build --no-daemon -x test

# ---
# 실행 스테이지: Tomcat + Java 17
FROM tomcat:9.0.60-jdk17

# 타임존 설정
ENV TZ=Asia/Seoul

# 빌드 결과물(WAR) 복사 (admin용 war)
COPY --from=builder /home/gradle/src/data/admin/build/libs/*.war /usr/local/tomcat/webapps/ROOT.war

# 포트 8080 노출 (Tomcat 기본 포트)
EXPOSE 8080

# Tomcat 실행
CMD ["catalina.sh", "run"]

# ---
# 이 Dockerfile은 내부망 관리자 서버(JSP+Tomcat) 컨테이너용입니다.
# data/admin 디렉토리에 소스를 두고 빌드하세요. 