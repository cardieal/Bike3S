package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

import java.util.List;

public class EventUserWantsToReturnBike extends EventUser {

    private GeoPoint actualPosition;

    public EventUserWantsToReturnBike(int instant, Person user, GeoPoint actualPosition) {
        super(instant, user);
        this.actualPosition = actualPosition;
    }

    public GeoPoint getActualPosition() {
		return actualPosition;
	}

	public List<Event> execute() {
        user.setPosition(actualPosition);
        return manageSlotReservationDecision();
    }
    
}
