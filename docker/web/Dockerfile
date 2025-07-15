# 멀티스테이지 빌드 - 빌드 스테이지
FROM gradle:jdk17 AS builder

# 작업 디렉토리 설정
WORKDIR /home/gradle/src

# 루트 gradle 설정 복사 (멀티모듈 빌드용)
COPY build.gradle ./build.gradle
COPY settings.gradle ./settings.gradle
COPY gradle ./gradle
COPY gradlew ./gradlew
COPY gradlew.bat ./gradlew.bat

# Windows에서 생성된 스크립트를 Linux에서 실행하기 위한 필수 단계
RUN chmod +x ./gradlew && \
    (apt-get update && apt-get install -y dos2unix && dos2unix ./gradlew || sed -i 's/\r$//' ./gradlew)

# web 소스 복사
COPY data/web ./data/web

# 빌드 실행 (web 모듈 빌드)
RUN ./gradlew :data:web:clean :data:web:build --no-daemon -x test

# ---
# 실행 스테이지: Tomcat + Java 17
FROM tomcat:9.0.60-jdk17
ENV TZ=Asia/Seoul

# tomcat 유저 생성 
RUN id -u tomcat 2>/dev/null || useradd -u 1000 -d /usr/local/tomcat -s /bin/bash tomcat

# psql(PostgreSQL client) 설치
RUN apt-get update && \
    apt-get install -y postgresql-client && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 빌드 결과물(WAR) 복사 (web용 war)
COPY --from=builder /home/gradle/src/data/web/build/libs/*.war /usr/local/tomcat/webapps/ROOT.war

# tomcat 유저에게 소유권 부여
RUN chown -R tomcat:tomcat /usr/local/tomcat

# 포트 8080 노출
EXPOSE 8080

# tomcat 유저로 실행
USER tomcat

# Tomcat 실행
CMD ["catalina.sh", "run"]