# 📁 구글 드라이브 설계
> WebSocket 기반 실시간 동기화를 적용한 클라우드 파일 저장소 시스템

![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791.svg)
![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-blue.svg)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)

## 📺 테스트 영상
> **실제 동작 확인하기**
>
> 아래 영상에서 Google Drive Clone의 주요 기능들이 실제로 동작하는 모습을 확인할 수 있습니다.
> - 📁 폴더 생성: test 폴더 생성 및 하위 폴더(test2) 생성
> - 📤 파일 업로드: 여러 이미지 파일 업로드 (사진1, 사진2, 사진3 등)
> - 🔄 실시간 동기화: 여러 탭에서 동시 작업 시 WebSocket을 통한 즉각 반영
> - 📥 파일 다운로드: 업로드된 파일 다운로드 기능
> - 🗑️ 폴더 삭제: 폴더 및 하위 컨텐츠 일괄 삭제

[![Google Drive Clone Demo](https://img.shields.io/badge/▶️_테스트_영상-보기-red?style=for-the-badge)](https://drive.google.com/file/d/1HOcgErxQGAG3Tydj5fQQG9OFwyTnEJrx/view?usp=sharing)

## 🚀 실행 방법

### Docker Compose 실행
```bash
git clone [repository-url]
cd google-drive-clone
docker-compose up --build
```

### 로컬 실행
```bash
# 1. PostgreSQL 실행
docker-compose up postgres

# 2. 애플리케이션 실행
./gradlew bootRun
```

브라우저에서 `http://localhost:8080` 접속

## 🛠 기술 스택

### Backend
- **Java 21**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **Spring WebSocket (STOMP)**

### Database
- **PostgreSQL 16**

### Frontend
- **Thymeleaf**
- **Vanilla JavaScript**
- **SockJS + STOMP**

## ✨ 주요 기능

### 📤 파일 관리
- **파일 업로드**: 드래그 앤 드롭 지원, 다중 파일 업로드
- **파일 다운로드**: 원본 파일명 유지
- **파일 삭제**: 물리적 파일 + DB 동시 삭제

### 📁 폴더 관리
- **계층 구조**: 무제한 깊이의 폴더 구조
- **폴더 생성**: 현재 위치에 새 폴더 생성
- **폴더 삭제**: Cascade 삭제 (하위 항목 포함)
- **폴더 탐색**: Breadcrumb 네비게이션

### 🔄 실시간 동기화
- **WebSocket 연결**: STOMP 프로토콜
- **실시간 알림**: 파일/폴더 변경 시 즉시 알림
- **자동 새로고침**: 다른 사용자의 변경사항 자동 반영

## 🏗 시스템 설계

### 아키텍처 개요

<img width="1150" height="670" alt="시스템 아키텍처" src="https://github.com/user-attachments/assets/a8eb4898-e2a7-4a0a-9596-279a1a73cbbf" />

### 핵심 컴포넌트

#### 1. 메타데이터 저장 (PostgreSQL)
```sql
files
├── id (PK)
├── fileName (원본 파일명)
├── storedFileName (저장된 파일명)
├── fileSize
├── contentType
├── filePath
├── folderId (FK)
├── uploadedAt
└── modifiedAt

folders
├── id (PK)
├── folderName
├── parentId (FK, 자기참조)
└── createdAt
```

#### 2. 파일 저장 전략
| 전략 | 설명 |
|------|------|
| **파일명 중복 방지** | UUID 기반 고유 파일명 생성 |
| **메타데이터 분리** | 원본 파일명과 저장 파일명 분리 |
| **경로 저장** | 절대 경로 저장으로 빠른 접근 |
| **트랜잭션 처리** | DB + 파일 시스템 동시 처리 |

#### 3. 실시간 동기화 프로토콜

**WebSocket 메시지 포맷**
```json
{
  "type": "FILE_UPLOADED|FILE_DELETED|FOLDER_CREATED|FOLDER_DELETED",
  "file": { /* FileDTO */ },
  "folder": { /* FolderDTO */ },
  "folderId": 1
}
```

**STOMP 토픽**
- `/topic/drive` - 전체 드라이브 이벤트 브로드캐스트

**이벤트 처리 플로우**
<img width="1380" height="792" alt="시퀀스 다이어그램" src="https://github.com/user-attachments/assets/2f589b59-ac47-4fa7-b3f4-4b5ed9ea0803" />

## 📝 API 명세

### 파일 API
```http
POST   /api/files/upload     # 파일 업로드
GET    /api/files/{id}/download  # 파일 다운로드
DELETE /api/files/{id}       # 파일 삭제
```

### 폴더 API
```http
POST   /api/folders          # 폴더 생성
DELETE /api/folders/{id}     # 폴더 삭제
```

### WebSocket
```
CONNECT /ws                  # WebSocket 연결
SUBSCRIBE /topic/drive       # 드라이브 이벤트 구독
```

## 🧪 테스트

### 기능 테스트 시나리오
1. **파일 업로드**
   - 단일 파일 업로드
   - 다중 파일 업로드
   - 드래그 앤 드롭

2. **폴더 관리**
   - 폴더 생성
   - 하위 폴더 생성
   - 폴더 삭제 (Cascade)

3. **실시간 동기화**
   - 2개 브라우저 탭 오픈
   - 한 탭에서 파일 업로드
   - 다른 탭에서 실시간 반영 확인