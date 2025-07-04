# PostgreSQL 14.15 데이터베이스 서버
FROM postgres:14.15

# 타임존 설정
ENV TZ=Asia/Seoul

# PostgreSQL 환경 변수 설정
ENV POSTGRES_DB=wh_air
ENV POSTGRES_USER=wh_air_user
ENV POSTGRES_PASSWORD=wh_air_password

# 초기화 스크립트 디렉토리 복사
COPY ../../data/db/ /docker-entrypoint-initdb.d/

# 포트 노출
EXPOSE 5432

# PostgreSQL 실행 (기본 엔트리포인트 사용) 