# PostgreSQL 14.15 데이터베이스 서버
FROM postgres:14.15

# 타임존 설정
ENV TZ=Asia/Seoul

# PostgreSQL 환경 변수 설정
ENV POSTGRES_DB=wh_air
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres

# 필요한 패키지 설치
RUN apt-get update && apt-get install -y python3 curl && rm -rf /var/lib/apt/lists/*

# 초기화 스크립트 디렉토리 복사
COPY ../../data/db/ /docker-entrypoint-initdb.d/

# Windows에서 생성된 스크립트를 Linux에서 실행하기 위한 줄바꿈 변환
RUN (dos2unix /docker-entrypoint-initdb.d/pghba.sh /docker-entrypoint-initdb.d/*.sql 2>/dev/null || \
     sed -i 's/\r$//' /docker-entrypoint-initdb.d/pghba.sh /docker-entrypoint-initdb.d/*.sql 2>/dev/null) && \
    chmod +x /docker-entrypoint-initdb.d/pghba.sh

# 포트 노출
EXPOSE 5432

# 기본 엔트리포인트 및 명령어는 postgres 이미지 기본값 사용