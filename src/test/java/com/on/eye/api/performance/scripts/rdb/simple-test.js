import http from 'k6/http';
import {check, sleep} from 'k6';
import {Counter, Rate, Trend} from 'k6/metrics';

// 테스트 설정 변수
const CONFIG = {
    BASE_URL: `http://${__ENV.API_HOST}:${__ENV.API_PORT}`,
    PROTEST_IDS: [1, 2, 3, 4],
    POLLING_INTERVAL: 3000,
    CHEER_REQUEST_PER_SECOND: 3, // 1초에 3번 응원 요청

    // 테스트 단계 설정
    STAGES: [
        {duration: '1m', target: 100},
        {duration: '5m', target: 3000},
        {duration: '2m', target: 0},
    ]
}

// 커스텀 메트릭 정의
const cheerCallCounter = new Counter('cheer_calls');
const cheerResponseTime = new Trend('cheer_response_time');
const pollCallCounter = new Counter('poll_calls');
const pollResponseTime = new Trend('poll_response_time');

// 성공/실패 카운트를 위한 Rate 메트릭 추가
const cheerSuccessRate = new Rate('cheer_success_rate');
const cheerFailRate = new Rate('cheer_fail_rate');
const pollSuccessRate = new Rate('poll_success_rate');
const pollFailRate = new Rate('poll_fail_rate');

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

export default function () {
    // 1초에 3번의 POST 요청을 수행하기 위한 반복문
    for (let i = 0; i < CONFIG.CHEER_REQUEST_PER_SECOND; i++) {
        const protestId = CONFIG.PROTEST_IDS[Math.floor(Math.random() * CONFIG.PROTEST_IDS.length)];

        // 응원 요청 파라미터 설정
        const params = {
            timeout: '5s', // 요청 타임아웃 설정
            tags: {name: 'cheer-api'} // 요청 태깅
        };

        // 1. POST 요청으로 응원하기
        try {
            let cheerRes = http.post(`${CONFIG.BASE_URL}/api/cheer/protest/${protestId}`, null, params);

            // POST 요청 메트릭 기록
            cheerCallCounter.add(1);
            cheerResponseTime.add(cheerRes.timings.duration);

            // 응답 검증
            const isSuccess = check(cheerRes, {
                'POST 응원 요청 성공': (r) => r.status === 200,
                '응원 응답에 cheerCount 포함': (r) => {
                    try {
                        const body = JSON.parse(r.body);
                        return body.data && body.data.cheerCount !== undefined;
                    } catch (e) {
                        return false;
                    }
                },
                '응답 시간 1000ms 이내': (r) => r.timings.duration < 1000
            });

            // 성공/실패 카운트 업데이트
            cheerSuccessRate.add(isSuccess);
            cheerFailRate.add(!isSuccess);
        } catch (e) {
            cheerFailRate.add(1);
        }

        // 1초를 3등분하여 각 요청 사이에 간격을 둠 (마지막 요청 후에는 쉬지 않음)
        if (i < CONFIG.CHEER_REQUEST_PER_SECOND - 1) {
            sleep(1 / CONFIG.CHEER_REQUEST_PER_SECOND);
        }
    }

    // 2. polling - 3초 마다 모든 시위의 응원 통계 조회
    const currentTime = new Date().getTime();
    if (currentTime - lastPollTime > CONFIG.POLLING_INTERVAL) {
        try {
            // 모든 시위의 응원 통계 조회
            const pollRes = http.get(`${CONFIG.BASE_URL}/api/cheer/protest`, {
                timeout: '5s',
                tags: {name: 'poll-api'}
            });

            // 폴링 호출 카운터 증가
            pollCallCounter.add(1);

            // 폴링 응답 시간 기록
            pollResponseTime.add(pollRes.timings.duration);

            // 폴링 성공 여부 확인
            const isPollSuccess = check(pollRes, {
                'GET 모든 응원 통계 조회 성공': (r) => r.status === 200,
                '유효한 JSON 응답': (r) => {
                    try {
                        JSON.parse(r.body);
                        return true;
                    } catch (e) {
                        return false;
                    }
                }
            });

            // 성공/실패 카운트 업데이트
            pollSuccessRate.add(isPollSuccess);
            pollFailRate.add(!isPollSuccess);
        } catch (e) {
            pollFailRate.add(1);
        }

        // 마지막 폴링 시간 업데이트
        lastPollTime = currentTime;
    }
}