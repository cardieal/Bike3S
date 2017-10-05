package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;

import java.util.List;
import java.util.ArrayList;

public class EventSlotReservationTimeout extends Event {
    private Person user;

    public EventSlotReservationTimeout(int instant, Person user) {
        super(instant);
        this.user = user;
    }

    public Person getUser() {
        return user;
    }

    public void setUser(Person user) {
        this.user = user;
    }

    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();

        Station destination = user.determineStation();
        int arrivalTime = user.timeToReach(destination.getPosition());
        user.setDestinationStation(destination);

        if (user.decidesToReserveSlot(destination) && SystemInfo.reservationTime < arrivalTime) {
        				user.updatePosition(SystemInfo.reservationTime);
            user.cancelsSlotReservation(destination);
            newEvents.add(new EventSlotReservationTimeout(getInstant() + SystemInfo.reservationTime, user));
        } else {
												user.setPosition(destination.getPosition());
            newEvents.add(new EventUserArrivesAtStationToReturnBike(getInstant() + arrivalTime, user, destination));
        }

        return newEvents;
    }
    
    public String toString() {
    	String str = super.toString();
    	return str+"User: "+user.getId()+"\n";

    }
}