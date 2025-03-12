package com.on.eye.api.util;

import java.math.BigDecimal;

public class GeoUtils {

    // 지구 반경 (미터)
    static final double EARTH_RADIUS_METERS = 6371000;

    private GeoUtils() {}

    /**
     * Calculates the distance between two geographic coordinates using the Haversine formula.
     *
     * @param startLatitude The latitude of the starting point
     * @param startLongitude The longitude of the starting point
     * @param endLatitude The latitude of the ending point
     * @param endLongitude The longitude of the ending point
     * @return Distance between the points in meters
     */
    public static double haversineDistance(
            BigDecimal startLatitude,
            BigDecimal startLongitude,
            BigDecimal endLatitude,
            BigDecimal endLongitude) {

        // 위도와 경도 차이를 라디안으로 변환
        double latitudeDiffRadians =
                Math.toRadians(endLatitude.doubleValue() - startLatitude.doubleValue());
        double longitudeDiffRadians =
                Math.toRadians(endLongitude.doubleValue() - startLongitude.doubleValue());

        // 시작점과 종료점의 위도를 라디안으로 변환
        double startLatRadians = Math.toRadians(startLatitude.doubleValue());
        double endLatRadians = Math.toRadians(endLatitude.doubleValue());

        // 하버사인 공식의 중간 계산값
        double haversineFormula =
                Math.sin(latitudeDiffRadians / 2) * Math.sin(latitudeDiffRadians / 2)
                        + Math.cos(startLatRadians)
                                * Math.cos(endLatRadians)
                                * Math.sin(longitudeDiffRadians / 2)
                                * Math.sin(longitudeDiffRadians / 2);

        // 두 지점 사이의 구면 거리 (라디안)
        double angularDistanceRadians =
                2 * Math.atan2(Math.sqrt(haversineFormula), Math.sqrt(1 - haversineFormula));

        // 실제 지표면 거리 (미터)
        return EARTH_RADIUS_METERS * angularDistanceRadians;
    }
}
