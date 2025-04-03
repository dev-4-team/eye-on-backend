import { check } from 'k6';
import ws from 'k6/ws';
import { Counter, Trend } from 'k6/metrics';

// 테스트 설정 변수
const CONFIG = {
    WS_URL: `ws://${__ENV.API_HOST}:${__ENV.API_PORT}/ws`,
    PROTEST_IDS: [1, 2, 3, 4],
    CHEER_INTERVAL: 3000,  // 응원 요청 간격

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
};

// 커스텀 메트릭 정의
const cheerCallCounter = new Counter('ws_cheer_calls');              // 응원 요청 횟수
const cheerResponseTime = new Trend('ws_cheer_response_time');      // 응원 응답 시간
const connectionCounter = new Counter('ws_connections');            // 웹소켓 연결 수
const messageCounter = new Counter('ws_messages_received');         // 수신한 메시지 수

export const options = {
    scenarios: {
        websocketTest: {
            executor: 'ramping-vus',
            startVUs: 1,
            stages: CONFIG.STAGES,
            gracefulRampDown: '30s',
        }
    }
};

const RETRY_CONFIG = {
    MAX_RETRIES: 3,
    RETRY_DELAY: 1000,
};

export default function() {
    const protestId = CONFIG.PROTEST_IDS[Math.floor(Math.random() * CONFIG.PROTEST_IDS.length)];

    // 응답 시간 측정을 위한 상태 추적
    const messageState = {
        protestId: protestId,     // 현재 테스트 중인 시위 ID
        responseReceived: false,  // 응답 수신 여부
        sendTime: 0,              // 응원 요청 전송 시간
        receiveTime: 0            // 응원 응답 수신 시간
    };

    let retryCount = 0;
    let connectSuccess = false;

    function connectWithRetry() {
        if (retryCount >= RETRY_CONFIG.MAX_RETRIES) {
            console.error(`최대 재시도 횟수(${RETRY_CONFIG.MAX_RETRIES})에 도달했습니다.`);
            return;
        }

        const res = ws.connect(CONFIG.WS_URL, null, function(socket) {
            connectionCounter.add(1);

            socket.on('open', () => {
                // STOMP CONNECT 프레임 전송(서버에 연결 요청)
                socket.send(buildConnectFrame());
            });

            let connected = false;
            let lastCheerTime = 0;

            socket.on('message', (data) => {
                messageCounter.add(1);

                // STOMP CONNECTED 프레임 수신받음(연결 성립)
                if (data.startsWith('CONNECTED') && !connected) {
                    connected = true;

                    // 응원 정보 토픽 구독
                    const subscriptionId = 'sub-' + Math.random().toString(36).substring(2, 15);
                    socket.send(buildSubscribeFrame('/topic/cheer', subscriptionId));

                    // 에러 토픽 구독
                    const errorSubscriptionId = 'err-' + Math.random().toString(36).substring(2, 15);
                    socket.send(buildSubscribeFrame('/user/queue/errors', errorSubscriptionId));

                    // 응원 요청 전송
                    const currentTime = new Date().getTime();
                    messageState.sendTime = currentTime;
                    lastCheerTime = currentTime;

                    const destination = `/app/cheer/protest/${protestId}`;
                    socket.send(buildSendFrame(destination, {}));
                    cheerCallCounter.add(1);
                }

                // 메시지 수신 처리
                else if (data.startsWith('MESSAGE')) {
                    try {
                        // 메시지 헤더에서 destination 추출
                        const destinationMatch = data.match(/destination:([^\n]*)/);
                        const destination = destinationMatch ? destinationMatch[1].trim() : 'unknown';

                        // 메시지 본문 추출
                        const bodyStart = data.indexOf('\n\n') + 2;
                        const bodyEnd = data.lastIndexOf('\0');
                        if (bodyStart > 1 && bodyEnd > bodyStart) {
                            const body = data.substring(bodyStart, bodyEnd);
                            const response = JSON.parse(body);

                            // 응원 토픽 메시지 처리
                            if (destination === '/topic/cheer') {
                                messageState.receiveTime = new Date().getTime();
                                messageState.responseReceived = true;

                                // 응답 시간 측정
                                const responseTime = messageState.receiveTime - messageState.sendTime;
                                cheerResponseTime.add(responseTime);
                            }
                        }
                    } catch(e) {
                        // 메시지 파싱 오류
                    }
                }
            });

            // 응원 요청 전송
            socket.setInterval(() => {
                if (connected) {
                    const currentTime = new Date().getTime();

                    if (currentTime - lastCheerTime >= CONFIG.CHEER_INTERVAL) {
                        const destination = `/app/cheer/protest/${protestId}`;
                        socket.send(buildSendFrame(destination, {}));

                        cheerCallCounter.add(1);
                        messageState.sendTime = currentTime;
                        lastCheerTime = currentTime;
                        messageState.responseReceived = false;
                    }
                }
            }, 100); // 100ms 간격으로 체크

            // 연결 유지
            socket.setTimeout(function() {
                if (connected) {
                    const receiptId = 'receipt-' + Math.random().toString(36).substring(2, 15);
                    socket.send(buildDisconnectFrame(receiptId));
                }
                socket.close();
            }, 140 * 1000); // 140초 유지
        });

        // 웹소켓 연결 성공 여부 체크
        check(res, { '웹소켓 연결 성공': (r) => r && r.status === 101 });

        if (!connectSuccess && retryCount < RETRY_CONFIG.MAX_RETRIES) {
            retryCount++;
            console.log(`연결 시도 실패, ${retryCount}번째 재시도`);
            setTimeout(connectWithRetry, RETRY_CONFIG.RETRY_DELAY);
        }
    }

    connectWithRetry();
}



// STOMP CONNECT 프레임 생성
function buildConnectFrame() {
    return 'CONNECT\n' +
        'accept-version:1.1,1.0\n' +
        'heart-beat:20000,0\n' +
        '\n\0';
}

// STOMP SUBSCRIBE 프레임 생성
function buildSubscribeFrame(destination, id) {
    return 'SUBSCRIBE\n' +
        'id:' + id + '\n' +
        'destination:' + destination + '\n' +
        'ack:auto\n' +
        '\n' +
        '\0';
}

// STOMP SEND 프레임 생성
function buildSendFrame(destination, message) {
    const body = JSON.stringify(message);
    return 'SEND\n' +
        'destination:' + destination + '\n' +
        'content-type:application/json;charset=utf-8\n' +
        'content-length:' + body.length + '\n' +
        '\n' +
        body +
        '\0';
}

// STOMP DISCONNECT 프레임 생성
function buildDisconnectFrame(receiptId) {
    return 'DISCONNECT\n' +
        'receipt:' + receiptId + '\n' +
        '\n' +
        '\0';
}