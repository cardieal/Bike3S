package com.urjc.iagroup.bikesurbanfloats.util;

import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;

/**
 * This class represents a rectangle and is used to delimit the simulation area.
 * @author IAgroup
 *
 */
public class BoundingBox {
	/**
	 * It is upper left corner of the rectangle.
	*/
	private GeoPoint northWest;

	/**
	 * It is the lower right corner of the rectangle.
	 */
	private GeoPoint southEast;

	
	public BoundingBox(GeoPoint northWest, GeoPoint southEast) {
		this.northWest = northWest;
		this.southEast = southEast;
	}
	
	public BoundingBox(double north, double west, double south, double east) {
		this.northWest = new GeoPoint(north, west);
		this.southEast = new GeoPoint(south, east);
	}
	
	public GeoPoint getNorthWest() {
		return northWest;
	}
	
	public GeoPoint getSouthEast() {
		return southEast;
	}
	
	public double getWidth() {
		GeoPoint northEast = new GeoPoint(northWest.getLatitude(), southEast.getLongitude());
		return northWest.distanceTo(northEast);
	}
	
	public double getHeight() {
		GeoPoint southWest = new GeoPoint(southEast.getLatitude(), northWest.getLongitude());
		return northWest.distanceTo(southWest);
	}
	
	/**
	 * It calculates a random point inside the simulation area delimited by the boundingBox object. 
	 * @param random It is the general random instance of the system. 
	 * @return a random point which belongs to thhe bounding box object. 
	 */
	public GeoPoint randomPoint(SimulationRandom random) {
		double newLatitude = random.nextDouble(northWest.getLatitude(), southEast.getLatitude());
		double newLongitude = random.nextDouble(northWest.getLongitude(), southEast.getLongitude());
		return new GeoPoint(newLatitude, newLongitude);
	}

}
