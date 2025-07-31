# ✈WH_AIR

이 프로젝트는 Pentesting playground 플랫폼을 위한 시나리오로, 망분리 환경으로 구축되어 있으며 다양한 CVE와 Chaining 기법을 연습할 수 있도록 구성되어있습니다.
이 문서는 프로젝트의 설치 방법, 기여자 정보, 기술 스택, 협업 방식, 개발 기간, 시스템 아키텍처, ERD, 그리고 시나리오를 설명합니다.
## Technology Stack
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)
![Docker Compose](https://img.shields.io/badge/Docker_Compose-2496ED?style=flat-square&logo=docker&logoColor=white)
![NGINX](https://img.shields.io/badge/NGINX-009639?style=flat-square&logo=nginx&logoColor=white)


---

## 목차
1. [서버 설치 방법](#서버-설치-방법)
2. [기여자 표](#기여자-표)
3. [협업 방식](#협업-방식)
4. [개발 기간](#개발-기간)
5. [시스템 아키텍처](#시스템-아키텍처)
6. [ERD](#erd)
7. [시나리오](#시나리오)

---

<a id="서버-설치-방법"></a>
## 📌 서버 설치 방법

아래 단계를 따라 서버를 설치하고 실행할 수 있습니다.

### 1. 저장소 복제

```bash
# 저장소 복제
git clone https://github.com/WHS-PentestingPlayground/WH_AIR.git

# 빌드 및 실행
docker compose up -d --build 
```
---

<a id="기여자-표"></a>
## 🙌 기여자 표

<h3>Project Team</h3>

<table>
  <thead>
    <tr>
      <th>Profile</th>
      <th>Role</th>
      <th>Materialize</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td align="center">
        <a href="https://github.com/yelin1197">
          <img src="https://github.com/yelin1197.png" width="60"/><br/>
          yelin1197
        </a>
      </td>
      <td align="center">Project Member</td>
      <td align="center">example</td>
    </tr>
    <tr>
      <td align="center">
        <a href="https://github.com/legendwon">
          <img src="https://github.com/legendwon.png" width="60"/><br/>
          legendwon
        </a>
      </td>
      <td align="center">Project Member</td>
      <td align="center">example</td>
    </tr>
    <tr>
      <td align="center">
        <a href="https://github.com/meowyeok">
          <img src="https://github.com/meowyeok.png" width="60"/><br/>
          meowyeok
        </a>
      </td>
      <td align="center">Project Member</td>
      <td align="center">example</td>
    </tr>
    <tr>
      <td align="center">
        <a href="https://github.com/namd0ng">
          <img src="https://github.com/namd0ng.png" width="60"/><br/>
          namd0ng
        </a>
      </td>
      <td align="center">Project Member</td>
      <td align="center">example</td>
    </tr>
  </tbody>
</table>

---

<a id="협업-방식"></a>
## 🔥 협업 방식

| 플랫폼                                                                                                      | 사용 방식                   |
|----------------------------------------------------------------------------------------------------------|-------------------------|
| <img src="https://img.shields.io/badge/discord-5865F2?style=for-the-badge&logo=discord&logoColor=white"> | 매주 토요일 2시 회의    |
| <img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=Github&logoColor=white">   | PR을 통해 변경사항 및 테스트 과정 확인 |<br/>|
| <img src="https://img.shields.io/badge/notion-000000?style=for-the-badge&logo=notion&logoColor=white">   | 시나리오 구성, API, 회의 기록 문서화     |

---

<a id="개발-기간"></a>
## 📆 개발 기간
- 2025.06.29 ~ 2025.07.03 : 팀 규칙 및 코딩 컨벤션 의논, 시나리오 컨셉 정의</br>
- 2025.07.04 ~ 2025.07.07 : 프로젝트 환경 세팅 및 취약점 구현 역할 분배</br>
- 2025.07.08 ~ 2025.07.13 : 각 취약점 및 에러페이지 구현</br>
- 2025.07.13 ~ 2025.07.19 : 시나리오 보고서 작성 및 시나리오 통합 테스트</br>
- 2025.07.29 ~ 2025.08.02 : 플랫폼 오픈</br>

---
<a id="시스템-아키텍처"></a>
## 🛠️ 시스템 아키텍처
(추가에정)

---

<a id="erd"></a>
## 📝 ERD
<img width="1211" height="727" alt="dberd" src="https://github.com/user-attachments/assets/21c6c785-1008-4fd1-ba75-b437a203886c" />

---

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






