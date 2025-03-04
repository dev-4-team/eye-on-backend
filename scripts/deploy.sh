echo "현재 디렉토리: $(pwd)"
echo "docker-compose.yml 확인: $(ls -la docker-compose.yml 2>/dev/null || echo '파일 없음')"

find . -name "docker-compose.yml"

if [ ! -f "docker-compose.yml" ]; then
    echo "오류: docker-compose.yml 파일을 찾을 수 없습니다."
    exit 1
fi

# 환경변수 세팅
echo "DB_URL=${DB_URL}" > .env
echo "DB_USERNAME=${DB_USERNAME}" >> .env
echo "DB_PASSWORD=${DB_PASSWORD}" >> .env
echo "KAKAO_CLIENT_ID=${KAKAO_CLIENT_ID}" >> .env
echo "KAKAO_CLIENT_SECRET=${KAKAO_CLIENT_SECRET}" >> .env
echo "JWT_SECRET_KEY=${JWT_SECRET_KEY}" >> .env
echo "FRONT_BASE_URL=${FRONT_BASE_URL}" >> .env
echo "BASE_URL=${BASE_URL}" >> .env
echo "HASH_SECRET_KEY=${HASH_SECRET_KEY}" >> .env
echo "DOCKER_HUB_USR=${DOCKER_HUB_USR}" >> .env
echo "APP_NAME=${APP_NAME}" >> .env
echo "BUILD_NUMBER=${BUILD_NUMBER}" >> .env
echo "CONTAINER_NAME=${CONTAINER_NAME}" >> .env

# 현재 상태 백업
cp docker-compose.yml docker-compose.yml.backup

# 서비스 중단
docker-compose down

# 새 이미지 가져오기
docker-compose pull

# 서비스 시작
docker-compose up -d

# 서비스 상태 확인
HEALTH_CHECK_RETRIES=30
HEALTH_CHECK_DELAY=5
COUNT=0

while [ ${COUNT} -lt ${HEALTH_CHECK_RETRIES} ]; do
    if docker-compose ps | grep "Up" > /dev/null; then
        echo "서비스가 성공적으로 시작되었습니다."
        exit 0
    fi

    echo "서비스 시작 대기 중... (${COUNT}/${HEALTH_CHECK_RETRIES})"
    sleep ${HEALTH_CHECK_DELAY}
    COUNT=$((COUNT + 1))
done

echo "서비스 시작 실패, 롤백을 수행합니다."
docker-compose down || echo "서비스 중단 실패"

if mv docker-compose.yml.backup docker-compose.yml; then
    if docker-compose up -d; then
        echo "롤백이 성공적으로 완료되었습니다."
    else
        echo "롤백 중 서비스 재시작 실패"
    fi
else
    echo "롤백 파일 복원 실패"
fi
exit 1