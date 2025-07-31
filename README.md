# WH_AIR 프로젝트

## 🚀 빠른 시작

### 필수 요구사항
- Docker Desktop
- Git

### 환경 구축
```bash
# 1. 프로젝트 클론
git clone [repository-url]
cd WH_AIR

# 2. Docker Compose로 전체 환경 실행
docker-compose up -d --build
```

### 모듈 구성
- **common**: 공통 엔티티 및 유틸리티
- **data/web**: Spring Boot 웹 애플리케이션 (메인 서비스)
- **data/admin**: JSP + Tomcat 관리자 서버 (내부망 전용)
- **db**: PostgreSQL 14.15 데이터베이스

### 개발 환경
- **Backend**: Spring Boot 2.6.5 (웹 모듈)
- **Admin**: JSP + Tomcat (관리자 모듈)
- **Database**: PostgreSQL 14.15
- **Build Tool**: Gradle (Wrapper 포함)
- **Container**: Docker & Docker Compose

### 주요 명령어
```bash
# 전체 서비스 시작
docker-compose up -d --build

# 전체 서비스 중지
docker-compose down

# 로그 확인
docker-compose logs -f [service-name]

# 특정 모듈만 빌드
docker-compose build [service-name]
```

### 접속 정보
- **웹 애플리케이션**: http://localhost:8080 (Spring Boot)
- **관리자 페이지**: http://localhost:8081 (JSP + Tomcat)
- **데이터베이스**: localhost:5432 (PostgreSQL)

### 네트워크 구성
- **dmz-net**: 외부 접근 가능한 네트워크 (웹 서버)
- **int-net**: 내부망 네트워크 (관리자 서버, DB)

### 개발 팁
- `.gradle`과 `build/` 디렉토리는 gitignore에 포함되어 있음 (Docker 빌드 시 자동 생성)
- Gradle Wrapper가 포함되어 있어 별도 Gradle 설치 불필요
- 환경 변수는 `.env` 파일로 관리 (gitignore에 포함)
- 관리자 서버는 내부망 전용이므로 운영 시 포트 매핑 제거 필요

### 문제 해결
```bash
# 캐시 삭제 후 재빌드
docker-compose down
docker system prune -f
docker-compose up -d --build
``` 

<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_1" src="https://github.com/user-attachments/assets/81dcb98c-6be1-4486-98a3-bbbce885ec41" />
