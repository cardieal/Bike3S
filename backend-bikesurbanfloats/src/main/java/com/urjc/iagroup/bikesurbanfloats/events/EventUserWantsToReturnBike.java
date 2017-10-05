package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

import java.util.List;
import java.util.ArrayList;

public class EventUserWantsToReturnBike extends Event {
    private Person user;
    private GeoPoint actualPosition;

    public EventUserWantsToReturnBike(int instant, Person user, GeoPoint actualPosition) {
        super(instant);
        this.user = user;
        this.actualPosition = actualPosition;
    }

    public Person getUser() {
        return user;
    }

    public void setUser(Person user) {
        this.user = user;
    }

    public GeoPoint getActualPosition() {
		return actualPosition;
	}

	public void setActualPosition(GeoPoint actualPosition) {
		this.actualPosition = actualPosition;
	}

	public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();
        user.setPosition(actualPosition);

        Station destination = user.determineStation();
        int arrivalTime = user.timeToReach(destination.getPosition());
        user.setDestinationStation(destination);

        if (user.decidesToReserveSlot(destination) && SystemInfo.reservationTime < arrivalTime) {
        				user.updatePosition(SystemInfo.reservationTime);
            user.cancelsSlotReservation(destination);
            newEvents.add(new EventSlotReservationTimeout(getInstant() + arrivalTime, user));
        } else {
            newEvents.add(new EventUserArrivesAtStationToReturnBike(getInstant() + arrivalTime, user, destination));
        }

        return newEvents;
    }
    
    public String toString() {
    	String str = super.toString();
    	return str+"User: "+user.getId()+"\n";
    }

}
