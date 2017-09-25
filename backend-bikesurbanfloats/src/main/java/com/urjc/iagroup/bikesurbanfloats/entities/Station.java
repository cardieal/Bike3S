package com.urjc.iagroup.bikesurbanfloats.entities;

import com.sun.istack.internal.NotNull;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

import javax.naming.ServiceUnavailableException;
import java.util.LinkedList;

public class Station {

    private final GeoPoint position;

    private int capacity;
    private LinkedList<Bike> bikes;
    private int reservedBikes;

    public Station(@NotNull final GeoPoint position, int capacity, LinkedList<Bike> bikes) {
        this.position = position;
        this.capacity  = capacity;
        this.bikes = bikes;
        this.reservedBikes = 0;
    }

    public GeoPoint getPosition() {
        return position;
    }
    
    

    public int getReservedBikes() {
		return reservedBikes;
	}

	public void setReservedBikes(int reservedBikes) {
		this.reservedBikes = reservedBikes;
	}

	public int availableBikes() {
        return this.bikes.size();
    }
    
    public int availableSlots() {
        return this.capacity - availableBikes();
    }

    public Bike removeBike() throws ServiceUnavailableException {
        if (this.availableBikes() == 0) {
            throw new ServiceUnavailableException("Trying to remove a bike while there are none available!");
        }
        return this.bikes.removeLast();
    }

    public void returnBike(Bike bike) throws ServiceUnavailableException {
        if (this.availableSlots() == 0) {
            throw new ServiceUnavailableException("Trying to return a bike while there are no free slots!");
        }
        this.bikes.add(bike);
    }
}
