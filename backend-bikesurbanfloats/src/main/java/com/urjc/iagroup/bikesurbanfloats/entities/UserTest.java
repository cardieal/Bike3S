package com.urjc.iagroup.bikesurbanfloats.entities;

import java.util.List;
import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

public class UserTest extends User {
	
	public UserTest(int id, GeoPoint position, SystemInfo systemInfo) {
		super(id, position, systemInfo);
	}

	public boolean decidesToLeaveSystem(int instant) {
		return obtainStationsWithBikeReservationAttempts(instant).size() == systemInfo.stations.size() ? true : false;
	}
	
	public Station determineStationToRentBike(int instant) {
		List<Station> stations = obtainStationsWithoutBikeReservationAttempts(instant);
		double minDistance = Double.MAX_VALUE;
		Station destination = null;
		for(Station currentStation: stations) {
			GeoPoint stationGeoPoint = currentStation.getPosition();
			GeoPoint personGeoPoint =	getPosition();
			double distance = stationGeoPoint.distanceTo(personGeoPoint);
			if(!personGeoPoint.equals(stationGeoPoint) && distance < minDistance) {
				minDistance = distance;
				destination = currentStation;
			}
		}
		if(destination == null) {
			int numberStations = systemInfo.stations.size();
			int indexStation = systemInfo.random.nextInt(0,  numberStations - 1);
			destination = systemInfo.stations.get(indexStation);
		}
		return destination;
	}
	
	public Station determineStationToReturnBike(int instant) {
		List<Station> stations = obtainStationsWithoutSlotReservationAttempts(instant);
		double minDistance = Double.MAX_VALUE;
		Station destination = null;
		for(Station currentStation: stations) {
			GeoPoint stationGeoPoint = currentStation.getPosition();
			GeoPoint personGeoPoint =	getPosition();
			double distance = stationGeoPoint.distanceTo(personGeoPoint);
			if(!personGeoPoint.equals(stationGeoPoint) && distance < minDistance) {
				minDistance = distance;
				destination = currentStation;
			}
		}
		if(destination == null) {
			int numberStations = systemInfo.stations.size();
			int indexStation = systemInfo.random.nextInt(0,  numberStations - 1);
			destination = systemInfo.stations.get(indexStation);
		}
		
		return destination;
	}
	
	public boolean decidesToReserveBike() {
		return systemInfo.random.nextBoolean();
	}

	public boolean decidesToReserveSlot() {
		return systemInfo.random.nextBoolean();
	}
	
	public GeoPoint decidesNextPoint() {
		return systemInfo.boundingBox.randomPoint();
	}
	
	public boolean decidesToReturnBike() {
		return systemInfo.random.nextBoolean();
	}
	
	public boolean decidesToRentBikeAtOtherStation() {
		return systemInfo.random.nextBoolean();
	}
	
	public void updatePosition(int time) {
		double distance = time * getPosition().distanceTo(getDestinationStation().getPosition()) / timeToReach(getDestinationStation().getPosition());
 		GeoPoint newPoint = getPosition().reachedPoint(distance, getDestinationStation().getPosition());
		setPosition(newPoint);
		
	}

	@Override
	public boolean decidesToDetermineOtherStation() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
