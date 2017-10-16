package com.urjc.iagroup.bikesurbanfloats.events;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import java.util.List;
import java.util.ArrayList;

public class EventReservationManagement extends Event {
	private Person user;
	private List<Station> stationsReservationAttemps;
	
	
	public List<Event> execute() {
		List<Event> newEvents = new ArrayList<>();
		
		Station destination = user.determineStation();
		user.setDestinationStation(destination);
		int arrivalTime = user.timeToReach(destination.getPosition());
		
        
        if (user.decidesToReserveBike()) {
        	boolean reserved = user.reservesBike(destination);
                   	
            if (reserved) { 
            	if (SystemInfo.reservationTime < arrivalTime) {
            		user.cancelsBikeReservation(destination);
            		newEvents.add(new EventBikeReservationTimeout(this.getInstant() + SystemInfo.reservationTime, user));
            	}
            	else {
            	    newEvents.add(new EventUserArrivesAtStationToRentBike(this.getInstant() + arrivalTime, user, destination));
            	}
            }
            else {  // user can't reserve
            	if (!user.decidesToLeaveSystem()) {
            		if (!user.decidesToDetermineOtherStation()) {
                newEvents.add(new EventUserArrivesAtStationToRentBike(this.getInstant() + arrivalTime, user, destination));
            		}
            		else {
            			newEvents.add(new EventReservationManagement());
            		}
            		
            	}
            	
            }
          
        }
        else {   // user decides not to reserve
            newEvents.add(new EventUserArrivesAtStationToRentBike(this.getInstant() + arrivalTime, user, destination));
        }
        return newEvents;

	}

}
