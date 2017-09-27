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
    private int reservedSlots;

    public Station(@NotNull final GeoPoint position, int capacity, LinkedList<Bike> bikes) {
        this.position = position;
        this.capacity  = capacity;
        this.bikes = bikes;
        this.reservedBikes = 0;
        this.reservedSlots = 0;
    }

    public GeoPoint getPosition() {
        return position;
    }
    
   
    public int getReservedBikes() {
		return reservedBikes;
	}

	public void reservesBike() {
		this.reservedBikes++;
	}
	
	public void cancelsBikeReservation() {
		this.reservedBikes--;
	}
	
	public int getReservedSlots() {
		return reservedSlots;
	}
	
	public void reservesSlot() {
		this.reservedSlots++;
	}
	
	public void cancelsSlotReservation() {
		this.reservedSlots--;
	}

	public int availableBikes() {
        return bikes.size() - reservedBikes;
    }
    
    public int availableSlots() {
        return this.capacity - availableBikes() - reservedSlots;
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
