import { check, sleep } from 'k6';
import ws from 'k6/ws';
import { Counter, Trend } from 'k6/metrics';

// 테스트 설정 변수
const CONFIG = {
    WS_URL: `ws://${__ENV.API_HOST}:${__ENV.API_PORT}/api/ws/123/abc123/websocket`,
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

// SockJS를 위한 STOMP 프레임 JSON 래핑 함수
function wrapSockJsMessage(message) {
    return JSON.stringify([message]);
}

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
            connectSuccess = true;

            socket.on('open', () => {
                console.log('WebSocket 연결 열림');

                // SockJS 초기화 메시지 수신 대기
                // 이 단계는 실제 메시지 처리 이전에 필요함
            });

            let sockJsOpened = false;
            let stompConnected = false;
            let lastCheerTime = 0;

            socket.on('message', (data) => {
                console.log('메시지 수신:', data);
                messageCounter.add(1);

                // SockJS 초기화 메시지 ('o') 처리
                if (data === 'o' && !sockJsOpened) {
                    sockJsOpened = true;
                    console.log('SockJS 연결 초기화됨');

                    // STOMP CONNECT 프레임 전송(SockJS 형식으로 래핑)
                    const connectFrame = buildConnectFrame();
                    socket.send(wrapSockJsMessage(connectFrame));
                    console.log('STOMP CONNECT 프레임 전송');
                }
                // SockJS 메시지 처리 ('a[...]')
                else if (data.startsWith('a') && data.length > 1) {
                    try {
                        // SockJS 메시지 형식: a["MESSAGE\n..."]
                        const jsonStart = data.indexOf('[');
                        const jsonEnd = data.lastIndexOf(']') + 1;

                        if (jsonStart !== -1 && jsonEnd !== -1) {
                            const jsonData = data.substring(jsonStart, jsonEnd);
                            const messages = JSON.parse(jsonData);

                            // 메시지 배열의 각 항목 처리
                            for (const message of messages) {
                                handleStompMessage(socket, message, messageState);
                            }
                        }
                    } catch (e) {
                        console.error('SockJS 메시지 파싱 오류:', e);
                    }
                }
                // SockJS 종료 메시지 ('c[...]')
                else if (data.startsWith('c')) {
                    console.log('SockJS 연결 종료됨');
                }
                // 기타 메시지 처리
                else {
                    console.log('알 수 없는 메시지 형식:', data);
                }
            });

            // 일정 시간마다 응원 요청 전송
            socket.setInterval(() => {
                if (sockJsOpened && stompConnected) {
                    const currentTime = new Date().getTime();

                    if (currentTime - lastCheerTime >= CONFIG.CHEER_INTERVAL) {
                        const destination = `/app/cheer/protest/${protestId}`;
                        const sendFrame = buildSendFrame(destination, {});
                        socket.send(wrapSockJsMessage(sendFrame));

                        cheerCallCounter.add(1);
                        messageState.sendTime = currentTime;
                        lastCheerTime = currentTime;
                        messageState.responseReceived = false;
                        console.log(`응원 요청 전송: ${destination}`);
                    }
                }
            }, 100); // 100ms 간격으로 체크

            // STOMP 메시지 처리 함수
            function handleStompMessage(socket, message, messageState) {
                // STOMP CONNECTED 프레임 처리
                if (message.startsWith('CONNECTED') && !stompConnected) {
                    stompConnected = true;
                    console.log('STOMP 연결 성공');

                    // 응원 정보 토픽 구독
                    const subscriptionId = 'sub-' + Math.random().toString(36).substring(2, 15);
                    const subscribeFrame = buildSubscribeFrame('/topic/cheer', subscriptionId);
                    socket.send(wrapSockJsMessage(subscribeFrame));
                    console.log('토픽 구독: /topic/cheer');

                    // 에러 토픽 구독
                    const errorSubscriptionId = 'err-' + Math.random().toString(36).substring(2, 15);
                    const errorSubscribeFrame = buildSubscribeFrame('/user/queue/errors', errorSubscriptionId);
                    socket.send(wrapSockJsMessage(errorSubscribeFrame));
                    console.log('에러 토픽 구독: /user/queue/errors');

                    // 응원 요청 전송
                    const currentTime = new Date().getTime();
                    messageState.sendTime = currentTime;
                    lastCheerTime = currentTime;

                    const destination = `/app/cheer/protest/${protestId}`;
                    const sendFrame = buildSendFrame(destination, {});
                    socket.send(wrapSockJsMessage(sendFrame));
                    cheerCallCounter.add(1);
                    console.log(`첫 응원 요청 전송: ${destination}`);
                }
                // STOMP MESSAGE 프레임 처리
                else if (message.startsWith('MESSAGE')) {
                    try {
                        // 메시지 헤더에서 destination 추출
                        const destinationMatch = message.match(/destination:([^\n]*)/);
                        const destination = destinationMatch ? destinationMatch[1].trim() : 'unknown';

                        // 메시지 본문 추출
                        const bodyStart = message.indexOf('\n\n') + 2;
                        const bodyEnd = message.lastIndexOf('\0');
                        if (bodyStart > 1 && bodyEnd > bodyStart) {
                            const body = message.substring(bodyStart, bodyEnd);
                            console.log(`메시지 본문 (${destination}): ${body}`);

                            try {
                                const response = JSON.parse(body);

                                // 응원 토픽 메시지 처리
                                if (destination === '/topic/cheer') {
                                    messageState.receiveTime = new Date().getTime();
                                    messageState.responseReceived = true;

                                    // 응답 시간 측정
                                    const responseTime = messageState.receiveTime - messageState.sendTime;
                                    cheerResponseTime.add(responseTime);
                                    console.log(`응답 시간: ${responseTime}ms`);
                                }
                            } catch (e) {
                                console.error('JSON 파싱 오류:', e);
                            }
                        }
                    } catch (e) {
                        console.error('메시지 파싱 오류:', e);
                    }
                }
                // 기타 STOMP 프레임 처리
                else {
                    console.log('기타 STOMP 메시지:', message.substring(0, 50));
                }
            }

            // 연결 유지
            socket.setTimeout(function() {
                if (stompConnected) {
                    const receiptId = 'receipt-' + Math.random().toString(36).substring(2, 15);
                    const disconnectFrame = buildDisconnectFrame(receiptId);
                    socket.send(wrapSockJsMessage(disconnectFrame));
                    console.log('STOMP 연결 종료 요청');
                }
                socket.close();
                console.log('WebSocket 연결 종료');
            }, 60 * 1000); // 60초 유지
        });

        // 웹소켓 연결 성공 여부 체크
        check(res, { '웹소켓 연결 성공': (r) => r && r.status === 101 });

        if (!connectSuccess && retryCount < RETRY_CONFIG.MAX_RETRIES) {
            retryCount++;
            console.log(`연결 시도 실패, ${retryCount}번째 재시도`);
            sleep(RETRY_CONFIG.RETRY_DELAY / 1000);
            connectWithRetry();
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