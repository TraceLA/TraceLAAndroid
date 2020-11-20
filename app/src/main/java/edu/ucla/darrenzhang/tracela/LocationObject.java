package edu.ucla.darrenzhang.tracela;

public class LocationObject {
    private double latitude, longitude;

    public LocationObject(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getDistanceInMeters(double otherLatitude, double otherLongitude) {
        double radiusOfEarth = 6378.137; //radius in kilometers
        double diffLat = otherLatitude * Math.PI / 180 - this.getLatitude() * Math.PI / 180;
        double diffLong = otherLongitude * Math.PI / 180 - this.getLongitude() * Math.PI / 180;
        double a = Math.sin(diffLat / 2) * Math.sin(diffLat / 2) +
                Math.cos(this.getLatitude() * Math.PI / 180) * Math.cos(otherLatitude * Math.PI / 180) *
                Math.sin(diffLong / 2) * Math.sin(diffLong / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return radiusOfEarth * c * 1000;  //meters
    }

}
