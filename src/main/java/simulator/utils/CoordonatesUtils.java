package simulator.utils;

public class CoordonatesUtils {
    public static final double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double distance = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        distance = Math.acos(distance);
        distance = rad2deg(distance);
        distance = distance * 111.18957696;

        return (distance);
    }

    private static final double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static final double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }
}
