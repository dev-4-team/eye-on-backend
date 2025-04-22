# 주변시위 Now 

- 서울 시내 시위 현황 확인(시위명, 주최 단체, 참여규모, 위치 등)
- 위치기반 시위 참여 인증 기능
    - 부정 참여 인증 방지
        - 비정상적 이동패턴
        - 중복인증
- 시위 응원하기 기능

### 진행기간 및 프로젝트 참여 인원

- 개발기간: 2024.12 ~ 2025.04
- 4인 프로젝트(FE: 2명, BE: 2명)

### 서비스 아키텍쳐
<img src="https://github.com/user-attachments/assets/f36de375-2493-4019-8100-ae9c8c60fce8" width="750"/>

### 서비스 화면

<img src="https://github.com/user-attachments/assets/25ff8eea-c82e-48d1-a98b-191e0002449f" width="300"/>
<img src="https://github.com/user-attachments/assets/606066b2-c590-4f76-95ec-f739d9674271" width="300"/>

### 프로젝트 목표

- 서비스 주제와 관련된 문제 해결에 필요한 적정기술 선택 경험
- 성능 테스트를 통해 수집한 근거에 기반한 성능 개선 경험
- Agile 방법론 적용 시 개발 효용성 증진을 위한 CI/CD 적용 경험
- 리팩토링에 열려있으며 신뢰도 높은 코드 작성을 위한 테스트 코드 작성

### 트러블 슈팅

**[K6를 사용한 성능 테스트로 병목 식별 및 개선(대규모 트래픽 간접 경험)]**

문제 상황

- 많은 부하가 예상되는 [실시간 시위 응원하기] 기능에 대한 성능 검증 부족

성능 테스트 준비
- Docker를 사용해 Test Server용 Mini PC에  성능 테스트 환경 구성 (Spring, Prometheus, Grafana, K6)
    - 배포 instance인 t3.small과 유사한 환경 제약을 걸기 위해 Spring instance에 2 Core, 2G RAM의 제약을 설정
    - 환경 구성 시간 대폭 절약

[실시간 시위 응원하기] 기능 성능 테스트 시나리오
- 유저가 1초에 3회 씩 랜덤한 시위를 응원하고, Polling 방식 3초에 1번씩 응원 수를 조회. 유저 1명 당 RPS = 3.3회

병목 개선 및 성과
- 과도한 Hikaricp Connection Pool Size로 인한 병목 개선
    - 10개 → 2개로 조절
    - 요청 실패율 47% → 4% (91.5% 개선)
- 과도한 Tomcat Thread Pool Size로 인한 병목 개선
    - 200개 → 10개로 조절
    - 요청 실패율 4% → 0.6% (85% 개선)
- DB 병목 제거
    - 응원 데이터에 Redis Cache 적용
    - 요청 실패율 0.6% → 0%
    - 최대 RPS 17개 → 2560개 (150배 증가)
    - 평균 요청 응답시간 4.5s → 1.24s (72.4% 개선)

결과 요약

병목 개선 전

<img src="https://github.com/user-attachments/assets/9bd002ce-cdc7-4952-9f21-64658403823b" width="300" />

병목 개선 후

<img src="https://github.com/user-attachments/assets/9d258969-c31b-4e2e-9d48-2af1b4e4a72c" width="300" />



**[Production Server 환경과 동일한 Staging Server 환경 구축]**

문제 상황
- 안정적인 E2E 서비스 테스트를 위한 환경 부족
- 실제 유저가 있는 서비스이기 때문에 Production 배포 전 철저한 실제 사용 테스트 환경이 필요
- Localhost 환경은 Production과 다르기 때문에 신뢰할 수 없음
- SSL 인증서 미적용, 하드웨어 자원 차이 등

개선 및 성과

개선
- 홈 서버 Mini PC를 Staging 서버로 환경 구축
- DDNS, NAT Forwarding 설정을 통해 동료들의 Staging에서 동작 중인 서비스 접근 허용
- Production 환경과 동일한 Staging 환경 구축
- 실제 환경과 동일하게 Nginx에 SSL 인증서 적용
- Docker를 사용해 Production 환경과의 차이를 제거. 하드웨어 제약 설정까지 추가

성과
- Production에서 기능 장애 발생 횟수 0회 기록

**[리팩토링을 통한 코드 결합도 및 가독성 개선]**

문제 상황
- 동료가 이해하기 어려운 난잡한 코드.  method 이해에 최고 20m 소요
- Transactional Script pattern + Anemic Domain model의  사용으로 단일 메소드가 하나 이상의 책임을 수행
- 비즈니스 로직이 캡슐화 되어있지 않고 script 방식 실행
- Domain간 분리 미적용
- 하나의 Service Class에 5개의 Domain Repository class 사용

개선 및 성과

개선
- Rich Domain Model 적용
    - 비즈니스 로직을 각 Domain Class로 분산
- GoF Design Pattern 적용
    - Facade Pattern, Aggregate Pattern 적용
- Layer 중심 모듈 구조 → Domain 중심 모듈구조로 변경

성과
- method 이해에 최고 20m → 5m 소요(75% 개선)
- 평균 method 길이 62% 감소
- 코드의 단일책임원칙 준수 정도 증가
- Layer 중심 이해 → Domain 중심 이해

![CodeRabbit Pull Request Reviews](https://img.shields.io/coderabbit/prs/github/dev-4-team/eye-on-backend?utm_source=oss&utm_medium=github&utm_campaign=dev-4-team%2Feye-on-backend&labelColor=171717&color=FF570A&link=https%3A%2F%2Fcoderabbit.ai&label=CodeRabbit+Reviews)
