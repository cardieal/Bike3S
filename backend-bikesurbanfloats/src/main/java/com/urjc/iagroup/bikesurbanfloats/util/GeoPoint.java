package com.urjc.iagroup.bikesurbanfloats.util;

public class GeoPoint {

    /**
     * Earth radius in meters
     */
    public final static double EARTH_RADIUS = 6371e3;
    public final static double DEG_TO_RAD = Math.PI / 180.0;

    private double latitude;
    private double longitude;

    public GeoPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public GeoPoint(GeoPoint geoPoint) {
        this(geoPoint.latitude, geoPoint.longitude);
    }

    public GeoPoint() {
        this(0.0, 0.0);
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    /**
     * Calculates the distance to another point using the haversine formula.
     * @return The distance in meters.
     * @see <a href="https://en.wikipedia.org/wiki/Haversine_formula">Haversine formula on Wikipedia</a>
     */
    public double distanceTo(GeoPoint point) {
        double[] f = {Math.toRadians(this.latitude), Math.toRadians(point.latitude)};
        double[] l = {Math.toRadians(this.longitude), Math.toRadians(point.longitude)};
        double h = haversine(f[1] - f[0]) + Math.cos(f[0]) * Math.cos(f[1]) * haversine(l[1] - l[0]);
        return 2 * EARTH_RADIUS * Math.asin(Math.sqrt(h));
    }
    
    // distnace: travelled distance during a concret time; destination: where user pretended to arrive
    public GeoPoint reachedPoint(double distance, GeoPoint destination) {
    	double totalDistance = this.distanceTo(destination);
    	double percentage = distance * 100 / totalDistance; 
    	
    	double totalX = destination.longitude - this.longitude;
    	double totalY = destination.latitude - this.latitude;

    	double travelledX = totalX * percentage / 100;  
    	double travelledY = totalY * percentage / 100;
    	
    	return new GeoPoint(latitude + travelledY, longitude + travelledX);
   
    }

    private double haversine(double value) {
        return Math.pow(Math.sin(value / 2), 2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeoPoint geoPoint = (GeoPoint) o;

        return (geoPoint.latitude == this.latitude && geoPoint.longitude == this.longitude);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(latitude);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
    	String result = "Latitude: " + Double.toString(latitude);
    	result += "| Longitude: " + Double.toString(longitude) + " \n";
    	return result;
    }
}
