# 🔔 Halli-Galli-Battle: Item Smash
네트워크 기반 할리갈리 카드 게임 with 특수 아이템

<br>

## 📚 목차
- [🎯 프로젝트 개요](#-프로젝트-개요-about)
- [✨ 주요 기능](#-주요-기능-features)
- [🎮 게임 규칙](#-게임-규칙-game-rules)
- [📸 Demo](#-demo)
- [🚀 실행 방법](#-실행-방법-getting-started)

<br>

## 🎯 프로젝트 개요 (About)

Halli-Galli-Battle은 전통적인 할리갈리 게임에 특수 아이템을 추가한 네트워크 기반 멀티플레이어 카드 게임입니다. <br>
Java Swing을 활용한 GUI와 Socket 통신을 통해 실시간 4인 대전을 구현했습니다.

🎮 4명 동시 플레이 → 🃏 카드 뒤집기 → 🔔 종 치기 → ⭐ 특수 아이템 → 🏆 10점 먼저 획득

<br>

## ✨ 주요 기능 (Features)

- 🎮 **4인 멀티플레이** - 최대 4명까지 동시 접속 가능
- 🔄 **턴제 시스템** - topLeft → topRight → bottomLeft → bottomRight 순서로 진행
- 🃏 **5종 과일 카드** - 라임, 바나나, 사과, 오렌지, 포도 (각 1~5 숫자)
- ⭐ **특수 아이템 카드**
  - ➕ Plus 카드: 종 치면 +1점
  - ➖ Minus 카드: 종 치면 -1점
- 🔔 **종 치기 규칙**
  - 같은 과일 합 = 5 → +1점
  - 같은 과일 합 > 5 → -1점
  - 같은 과일 합 < 5 → -1점
- 🏆 **승리 조건** - 먼저 10점 달성한 플레이어 승리
- 💬 **실시간 동기화** - 모든 플레이어에게 게임 상태 실시간 전달
- 🚪 **중도 참여 가능** - 게임 진행 중에도 새로운 플레이어 접속 가능

<br>

## 🎮 게임 규칙 (Game Rules)

### 기본 규칙
1. **참여 인원**: 4명 (각 위치: topLeft, topRight, bottomLeft, bottomRight)
2. **게임 진행**: 자신의 차례에만 카드 뒤집기 가능
3. **종 치기**: 언제든지 가능 (단, 조건에 따라 점수 증감)
4. **승리 조건**: 10점을 먼저 획득한 플레이어 승리

### 점수 계산
| 상황 | 점수 변화 |
|---|---|
| 같은 과일 합 = 5 | +1점 |
| 같은 과일 합 > 5 | -1점 |
| 같은 과일 합 < 5 | -1점 |
| Plus 카드 + 종 치기 | +1점 |
| Minus 카드 + 종 치기 | -1점 |
| Plus + Minus 동시 존재 | +1점 |

### 조작법
| 버튼 | 기능 | 사용 가능 시점 |
|---|---|---|
| **카드뒤집기** | 자신의 카드 뒤집기 | 자신의 차례일 때만 |
| **종치기** | 종을 쳐서 점수 획득/차감 | 언제든지 가능 |
| **접속 종료** | 게임에서 퇴장 | 언제든지 가능 |

<br>

## 📸 Demo

### 1. 로그인 화면
IP주소, 포트번호, 사용자 이름을 입력하여 서버에 접속

<img width="700" alt="image" src="https://github.com/user-attachments/assets/ded13902-08a5-4b27-beeb-5a7d9d326735" />

### 2. 서버 시작
서버 시작 및 클라이언트 접속 대기

<img width="300" alt="image" src="https://github.com/user-attachments/assets/9b0a4484-22f1-4334-8913-42f6e45790d1" />
<br>
<img width="700" alt="image" src="https://github.com/user-attachments/assets/8a13f5c3-5d3e-41af-baf7-af91654ee98d" />

### 3. 게임 진행 - 카드 뒤집기 및 종 치기
- 자신의 차례에만 카드 뒤집기 버튼 활성화
- 카드 뒤집기 시 모든 클라이언트에 실시간 동기화
  
**조건에 틀리면 -1점**

<img width="300" alt="image" src="https://github.com/user-attachments/assets/700eec84-9b9f-4408-af87-7176d621d087" />
<br>
<img width="700" alt="image" src="https://github.com/user-attachments/assets/cf678d38-a406-4206-b0bb-56a00fb712e1" />

**조건에 맞으면 +1점**

<img width="300" alt="image" src="https://github.com/user-attachments/assets/6bcb84ab-f9f3-4da0-8499-ef904fc9a1c9" />
<br>
<img width="700" alt="image" src="https://github.com/user-attachments/assets/20f4cd96-18e2-4a60-a5e5-cdc0e45a266f" />

### 4. 특수 카드
**Plus 카드**: 종 치면 무조건 +1점

<img width="300" alt="image" src="https://github.com/user-attachments/assets/0e994fd8-fc2a-4c15-9dbd-b60b503519d1" />
<br>
<img width="700" alt="image" src="https://github.com/user-attachments/assets/f7c81974-ec19-46ee-97f0-66e5a586782c" />

**Minus 카드**: 종 치면 -1점

<img width="300" alt="image" src="https://github.com/user-attachments/assets/f7910530-a486-4686-93d5-a1b4c3da7518" />
<br>
<img width="700" alt="image" src="https://github.com/user-attachments/assets/60c802af-558b-45f4-873f-e6a5934ec160" />

### 5. 게임 종료
10점 달성 시 승리자 알림 및 게임 종료

<img width="300" alt="image" src="https://github.com/user-attachments/assets/3028d451-769e-4260-ae4e-728da0b7ca4e" />
<br>
<img width="700" alt="image" src="https://github.com/user-attachments/assets/d7e664e2-d2bc-49ee-b38d-fd6e1cea3377" />

### 6. 서버 종료
서버 중지 시 모든 클라이언트 접속 종료

<img width="300" alt="image" src="https://github.com/user-attachments/assets/083ca00d-cbdf-4285-8ee7-78e97dab3146" />
<br>
<img width="700" alt="image" src="https://github.com/user-attachments/assets/4da138c4-ee2b-4c44-975a-e5c822c5f020" />

<br>

## 🚀 실행 방법 (Getting Started)

### 1. 사전 준비

**Java JDK 11 이상 필요**

Eclipse Temurin 설치 (추천):
```
https://adoptium.net/
```

설치 확인:
```bash
java -version
javac -version
```

### 2. VSCode 설정

**Extension Pack for Java 설치:**

1. VSCode 실행
2. 확장(Extensions) 탭 열기 (`Ctrl + Shift + X`)
3. "Extension Pack for Java" 검색 후 설치
4. VSCode 재시작

### 3. 서버 설정

`server_info.txt` 파일 수정:
```
localhost
54321
```
- 첫 번째 줄: 서버 IP (같은 컴퓨터면 `localhost`, 다른 컴퓨터면 실제 IP)
- 두 번째 줄: 포트 번호 (기본값: 54321)

### 4. 실행

#### 서버 실행 (먼저 실행)
1. `NetworkCardGameServer.java` 파일 열기
2. 파일 우클릭 → **"Run Java"** 선택
3. 서버 GUI에서 **"서버 시작"** 버튼 클릭

#### 클라이언트 실행 (최대 4개)
1. `CardGameLogin.java` 파일 열기
2. 파일 우클릭 → **"Run Java"** 선택
3. IP, 포트, 사용자 이름 입력 후 **"접속하기"**
4. 총 4명이 접속하면 게임 시작!
