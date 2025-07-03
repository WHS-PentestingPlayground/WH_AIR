# PostgreSQL 14.15 데이터베이스 서버
FROM postgres:14.15

# 타임존 설정
ENV TZ=Asia/Seoul

# PostgreSQL 환경 변수 설정
ENV POSTGRES_DB=wh_air
ENV POSTGRES_USER=wh_air_user
ENV POSTGRES_PASSWORD=wh_air_password

# 초기화 스크립트 복사
COPY ../../db/01_init.sql /docker-entrypoint-initdb.d/

# PostgreSQL 설정 최적화
RUN echo "shared_preload_libraries = 'pg_stat_statements'" >> /usr/share/postgresql/postgresql.conf.sample && \
    echo "pg_stat_statements.track = all" >> /usr/share/postgresql/postgresql.conf.sample && \
    echo "log_statement = 'all'" >> /usr/share/postgresql/postgresql.conf.sample && \
    echo "log_min_duration_statement = 1000" >> /usr/share/postgresql/postgresql.conf.sample

# 포트 노출
EXPOSE 5432

# PostgreSQL 실행 (기본 엔트리포인트 사용) 