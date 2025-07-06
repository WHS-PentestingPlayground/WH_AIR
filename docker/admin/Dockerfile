FROM python:3.9-slim

WORKDIR /app

# 시스템 패키지 업데이트 및 필요한 패키지 설치
RUN apt-get update && apt-get install -y \
    gcc \
    libpq-dev \
    && rm -rf /var/lib/apt/lists/*

# Python 의존성 파일 복사 및 설치
COPY data/admin/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 애플리케이션 파일 복사
COPY data/admin/ .

# 포트 노출
EXPOSE 8080

# 환경 변수 설정
ENV FLASK_APP=app.py
ENV FLASK_ENV=production
ENV DB_HOST=db-server
ENV DB_PORT=5432
ENV DB_NAME=wh_air
ENV DB_USER=wh_admin
ENV DB_PASSWORD=password

# 애플리케이션 실행
CMD ["python", "app.py"] 