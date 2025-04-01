import http from 'k6/http';
import { check } from 'k6';
import { Counter, Trend } from 'k6/metrics';

// 테스트 설정 변수
const CONFIG = {
    BASE_URL: 'http://210.113.121.71:8081',
    PROTEST_IDS: [1, 2, 3, 4, 5],
    POLLING_INTERVAL: 3000,

    // 테스트 단계 설정
    STAGES: [
        { duration: '10s', target: 50 },    // 10초 동안 50명으로 증가
        { duration: '10s', target: 100 },   // 10초 동안 100명으로 증가
        { duration: '10s', target: 200 },   // 10초 동안 200명으로 증가
        { duration: '10s', target: 300 },   // 10초 동안 300명으로 증가
        { duration: '10s', target: 400 },   // 10초 동안 400명으로 증가
        { duration: '10s', target: 500 },   // 10초 동안 500명으로 증가
        { duration: '10s', target: 0 },     // 10초 동안 0명으로 감소
    ]

}

// 커스텀 메트릭 정의
const cheerCallCounter = new Counter('cheer_calls');
const cheerResponseTime = new Trend('cheer_response_time');
const pollCallCounter = new Counter('poll_calls');
const pollResponseTime = new Trend('poll_response_time');

export const options = {
    scenarios: {
        rest_api_test: {
            executor: 'ramping-vus',
            startVUs: 1,
            stages: CONFIG.STAGES,
            gracefulRampDown: '30s',
        }
    }
};

let lastPollTime = 0;

export default function() {
    const protestId = CONFIG.PROTEST_IDS[Math.floor(Math.random() * CONFIG.PROTEST_IDS.length)];

    // 1. POST 요청으로 응원하기
    let cheerRes = http.post(`${CONFIG.BASE_URL}/cheer/protest/${protestId}`);

    // POST 요청 메트릭 기록
    cheerCallCounter.add(1);
    cheerResponseTime.add(cheerRes.timings.duration);

    // 응답 검증
    check(cheerRes, {
        'POST 응원 요청 성공': (r) => r.status === 200,
        '응원 응답에 cheerCount 포함': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.cheerCount !== undefined;
            } catch (e) {
                return false;
            }
        }
    });

    // 2. polling - 3초 마다 모든 시위의 응원 통계 조회
    const currentTime = new Date().getTime();
    if (currentTime - lastPollTime > CONFIG.POLLING_INTERVAL) {
        // 모든 시위의 응원 통계 조회
        const pollRes = http.get(`${CONFIG.BASE_URL}/cheer/protest`);

        // 폴링 호출 카운터 증가
        pollCallCounter.add(1);

        // 폴링 응답 시간 기록
        pollResponseTime.add(pollRes.timings.duration);

        // 폴링 성공 여부 확인
        check(pollRes, {
            'GET 모든 응원 통계 조회 성공': (r) => r.status === 200,
        });

        // 마지막 폴링 시간 업데이트
        lastPollTime = currentTime;
    }
}

