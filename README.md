# 주변시위 Now 

- 서울 시내 시위 현황 확인(시위명, 주최 단체, 참여규모, 위치 등)
- 위치기반 시위 참여 인증 기능
    - 부정 참여 인증 방지
        - 비정상적 이동패턴
        - 중복인증

### 진행기간 및 프로젝트 참여 인원

- 개발기간: 2024.12 ~ 진행 중
- 5인 프로젝트(FE: 2명, BE: 1명, DevOps: 1명, Data: 1명)

### 서비스 아키텍쳐

<img src="https://github.com/user-attachments/assets/872a6af2-40e9-4133-a001-e534c3b17a31" width="750"/>

### 서비스 화면

<img src="https://github.com/user-attachments/assets/25ff8eea-c82e-48d1-a98b-191e0002449f" width="300"/>
<img src="https://github.com/user-attachments/assets/f360dc6d-eabb-4287-b5b2-25c28162e0b3" width="300"/>

### 프로젝트 목표

- 서비스 주제와 관련된 문제 해결에 필요한 적정기술 선택 경험
- 스트레스 테스트를 통해 수집한 근거에 기반한 성능 개선 경험
- 대규모 유저에게 메세지 발송을 위한 메세지큐 적용 경험
- Agile 방법론 적용 시 개발 효용성 증진을 위한 CI/CD 적용 경험
- 리팩토링에 열려있으며 신뢰도 높은 코드 작성을 위한 테스트 코드 작성
- AWS 비용 절약을 위해 하나의 인스턴스에 집중된 프로세스들을 안정적으로 관리하기 위한 도커 사용 환경 격리

### 주요 기능

- 시위 정보 CRUD
    - 정확한 시위 장소 위경도 관리를 위한 시위장소 텍스트 검색 유사도 기반 관리 로직 적용
- 현재 위치 기반 시위 참여 인증
    - 시위 참여 신고 인원 기준 참여인증 유효 거리 계산
    - 부정 인증 방지 로직 적용
- JWT 기반 OAuth(카카오) 회원가입/로그인

### 트러블 슈팅

**시위 참여인증 부정 방지**

- 시위 참여 인증 수에 따라 client에서 시각적으로 강조되는 시위가 달라짐. 따라서 시위 인증에 대한 높은 정확도와 신뢰도가 요구되었음
- 중복인증 방지(시위 인증 멱등성 유지)를 위해 인증 정보 저장 테이블 생성
    - 시위 Id와 Hashing한 User Id를 Unique key로 설정해 시위 인증 고유성 확보
- 비정상적 인증 패턴 확인
    - 당일 시위 참여 인증 기록이 있는 유저가 추가 인증을 한 경우, 기존 인증 기록과 새 인증 요청의 시간과 거리를 각각 비교.
    - 인간의 이동속도 상 불가능한 패턴의 인증시도 시 무효 처리

**PostgreSQL 텍스트 검색 extension(pg_bigm)을 사용한 시위장소 관리 최적화**

- 서울시 경찰청 일일 집회신고 자료를 크롤링 해 시위장소를 비롯한 정보를 수집 중. 하지만 데이터에 아래와 같은 문제가 있었음
    - 실제 지명과 다른 존재하지 않는 장소명 사용 e.g., 송현공원(신고장소) 앞 인도 → 열린 송현 녹지 광장(실제 장소) 앞 인도
    - 오타 발생 e.g., 명둉역 4번출구(오타) → 명동역 4번출구(실제 장소)
- LIKE 연산자로는 위와 같은 미묘한 차이를 잡아낼 수 없음
- 이를 해결하기 위해 Fuzzy search 기능을 제공하는 PostgreSQL의 pg_bigm을 사용
- 유사도 값을 튜닝하여 잘못된 지명이 입력되더라도, 기존 저장된 올바른 지명과 위경도를 사용하도록 함

![CodeRabbit Pull Request Reviews](https://img.shields.io/coderabbit/prs/github/dev-4-team/eye-on-backend?utm_source=oss&utm_medium=github&utm_campaign=dev-4-team%2Feye-on-backend&labelColor=171717&color=FF570A&link=https%3A%2F%2Fcoderabbit.ai&label=CodeRabbit+Reviews)