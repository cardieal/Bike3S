package com.urjc.iagroup.bikesurbanfloats.entities.users.types;

import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.entities.users.User;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoRoute;
import com.urjc.iagroup.bikesurbanfloats.graphs.exceptions.GeoRouteException;
import com.urjc.iagroup.bikesurbanfloats.util.SimulationRandom;

/**
 * This class represents a user who always follows the frist system recommendation with 
 * respect a proportion, i. e., he weighs what is the best recommendation (the most little 
 * value) in relation to the quotient between the distance from his position to the station 
 * and the number of available bikes or slots which it contains.   
 * This user always reserves bikes and slots at destination stations to ensure his service 
 * as he knows the stations he chooses as destination may not have too many bikes or slots. 
 * Moreover, he always chooses the shortest routes to get his destination.
 * 
 * @author IAgroup
 *
 */
public class UserWeigher extends User {
	
	/**
	 * It indicates the size of the set of stations closest to the user within which the 
	 * destination will be chossen randomly.  
	 */
	private final int SELECTION_STATIONS_SET = 3;
	
	/**
	 * It is the time in seconds until which the user will decide to continue walking 
	 * or cycling towards the previously chosen station without making a new reservation 
	 * after a reservation timeout event has happened.  
	 */
	private final int MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION = 180;
	
	/**
	 * It contains the attributes which characterizes the typical behaviour of this user type. 
	 */
	private UserTypeParameters parameters; 
	
    public UserWeigher() {
        super();
    }
    
    @Override
    public boolean decidesToLeaveSystemAfterTimeout(int instant) {
        return getMemory().getCounterReservationTimeouts() == parameters.getMinReservationTimeouts() ? true : false;
    }

    @Override
    public boolean decidesToLeaveSystemAffterFailedReservation(int instant) {
        return false;
    }

    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable(int instant) {
        return false;
    }
    
    @Override
    public Station determineStationToRentBike(int instant) {
    	List<Station> stations = systemManager.consultStationsWithoutBikeReservationAttempt(this, instant);
    	return systemManager.getRecommendationSystem()
    			.recommendByProportionBikesDistance(this.getPosition(), stations).get(0);
    }

    @Override
     public Station determineStationToReturnBike(int instant) {
        List<Station> stations = systemManager.consultStationsWithoutBikeReservationAttempt(this, instant);
        return systemManager.getRecommendationSystem()
        		.recommendByProportionSlotsDistance(this.getPosition(), stations).get(0);
    }
    
    @Override
    public boolean decidesToReserveBikeAtSameStationAfterTimeout() {
    	int arrivalTime = timeToReach();
    	return arrivalTime < MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION ? false : true;
    }

    
    @Override
    public boolean decidesToReserveBikeAtNewDecidedStation() {
    	return true;
    }

    @Override
    public boolean decidesToReserveSlotAtSameStationAfterTimeout() {
    	int arrivalTime = timeToReach();
    	return arrivalTime < MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION ? false : true;
    }

    @Override
    public boolean decidesToReserveSlotAtNewDecidedStation() {
    	return true;
    }

    @Override
    public GeoPoint decidesNextPoint() {
        return systemManager.generateBoundingBoxRandomPoint(SimulationRandom.getGeneralInstance());
    }

    @Override
    public boolean decidesToReturnBike() {
    	int percentage = systemManager.getRandom().nextInt(0, 100);
    	return percentage < parameters.getBikeReturnPercentage() ? true : false;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterTimeout() {
    	int percentage = systemManager.getRandom().nextInt(0, 100);
    	return percentage < parameters.getReservationTimeoutPercentage() ? true : false;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterFailedReservation() {
    	int percentage = systemManager.getRandom().nextInt(0, 100);
    	return percentage < parameters.getFailedReservationPercentage() ? true : false;
    }
    
    /**
     * The user chooses the shortest route because he wants to arrive at work as fast as possible.
     */
    @Override
    public GeoRoute determineRoute(List<GeoRoute> routes) throws GeoRouteException {
        if (routes.isEmpty()) {
            throw new GeoRouteException("Route is not valid");
        }
        // The route in first list position is the shortest.
        return routes.get(0);
    }
	

}
