package com.urjc.iagroup.bikesurbanfloats.entities.users;

/**
 * This class keeps track of the number of times that a same event has happend.
 * It provides the corresponding method to update its counters.  
 * @author IAgroup
 *
 */
public class UserMemory {
	
	public enum FactType {
		BIKE_RESERVATION_TIMEOUT, BIKE_FAILED_RESERVATION, BIKES_UNAVAILABLE
	}
	
	private int counterReservationAttempts;
	private int counterReservationTimeouts;
	private int counterRentingAttempts;
	
	public UserMemory() {
        this.counterReservationAttempts = 0; 
        this.counterReservationTimeouts = 0;
        this.counterRentingAttempts = 0;
	}

	public int getCounterReservationAttempts() {
		return counterReservationAttempts;
	}

	public int getCounterReservationTimeouts() {
		return counterReservationTimeouts;
	}

	public int getCounterRentingAttempts() {
		return counterRentingAttempts;
	}

	public void update(FactType fact) throws IllegalArgumentException {
		switch(fact) {
			case BIKE_RESERVATION_TIMEOUT: counterReservationTimeouts++;
			case BIKE_FAILED_RESERVATION: counterReservationAttempts++;
<<<<<<< HEAD
			case BIKES_UNAVAILABLE: counterAttemptsWhenBikesUnavailable++;
			default: throw new IllegalArgumentException(cause.toString() + "is not defined in update method");
=======
			case BIKES_UNAVAILABLE: counterRentingAttempts++;
			default: throw new IllegalArgumentException(fact.toString() + "is not defined in update method");
>>>>>>> b27b18e58a28d7c63eca0be06ac2ed270efb2077
		}
	}

}
