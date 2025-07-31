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
### 시나리오

<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_1" src="https://github.com/user-attachments/assets/81dcb98c-6be1-4486-98a3-bbbce885ec41" />
<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_2" src="https://github.com/user-attachments/assets/06bc4786-aff1-48f3-b43b-570420ad1e48" />
<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_3" src="https://github.com/user-attachments/assets/0b7c6520-2fef-4d67-bb3b-1455c796084a" />
<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_4" src="https://github.com/user-attachments/assets/1399a806-e257-4a40-9da6-8b135a1cddf2" />
<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_5" src="https://github.com/user-attachments/assets/25567cb3-e52b-45e3-96e1-347af35920ed" />
<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_6" src="https://github.com/user-attachments/assets/f11fb4f6-a252-4dae-b99a-94e03f67ce92" />
<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_7" src="https://github.com/user-attachments/assets/5db17887-efe9-4b35-b974-6b31c0d60aac" />
<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_8" src="https://github.com/user-attachments/assets/ef87c267-111d-4528-a37d-f6e25e4f7126" />
<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_9" src="https://github.com/user-attachments/assets/5741c64d-3641-48b3-b026-99f58a1df1cf" />
<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_10" src="https://github.com/user-attachments/assets/ac88655b-13ff-4874-91ea-a77dfeb4542b" />
<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_11" src="https://github.com/user-attachments/assets/c0c85296-d4ac-4bf5-a961-80128d66a6b7" />
<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_12" src="https://github.com/user-attachments/assets/3a34f4e0-2cf8-497a-a41f-55f7f4422490" />
<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_13" src="https://github.com/user-attachments/assets/426d717e-e2ce-4200-9a51-1c7b9f6bbc25" />
<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_14" src="https://github.com/user-attachments/assets/f6de4a43-e154-4b70-a2b1-39c5bddfa9b7" />
<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_15" src="https://github.com/user-attachments/assets/c22add07-edf8-4512-93dd-15a87c11f5d5" />
<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_16" src="https://github.com/user-attachments/assets/97912398-cd8f-43e1-8760-48965e5d2ce5" />
<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_17" src="https://github.com/user-attachments/assets/2b465752-be70-46b8-9857-9d35639dcb32" />
<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_18" src="https://github.com/user-attachments/assets/bd06f3b1-9196-4826-bb09-210334629931" />
<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_19" src="https://github.com/user-attachments/assets/c469bc3c-e5db-4e11-832f-5f18c31514d9" />
<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_20" src="https://github.com/user-attachments/assets/158037c7-fcbc-4514-8957-8f1367b2004d" />
<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_21" src="https://github.com/user-attachments/assets/f3eff0c2-aca0-46e0-81e5-2d36f72c7357" />
<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_22" src="https://github.com/user-attachments/assets/c0009acb-364a-4fc5-8958-7783ec8b1e92" />
<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_23" src="https://github.com/user-attachments/assets/539b82a9-9b57-48a3-886b-89516cc63a13" />
<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_24" src="https://github.com/user-attachments/assets/9949d593-d0ba-4450-b5c2-dcdfcd5a049d" />
<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_25" src="https://github.com/user-attachments/assets/0787752d-cfda-4677-8473-cd0eba8ac205" />
<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_26" src="https://github.com/user-attachments/assets/b0903f17-e608-47b0-93ef-a13a0c637abe" />
<img width="791" height="1024" alt="1753930287711-253abbfd-ad53-449b-a517-e332ee0a633c_27" src="https://github.com/user-attachments/assets/0701c5ef-1da9-4518-80a9-7ce46c8ea9b2" />
