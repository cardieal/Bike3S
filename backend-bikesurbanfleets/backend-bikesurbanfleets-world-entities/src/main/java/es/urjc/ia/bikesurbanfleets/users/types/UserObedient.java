package es.urjc.ia.bikesurbanfleets.users.types;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GeoRouteException;
import es.urjc.ia.bikesurbanfleets.common.util.SimulationRandom;
import es.urjc.ia.bikesurbanfleets.entities.Station;
import es.urjc.ia.bikesurbanfleets.users.AssociatedType;
import es.urjc.ia.bikesurbanfleets.users.UserType;
import es.urjc.ia.bikesurbanfleets.entities.User;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a user who always follows the first system recommendations i. e., that 
 * which consist of renting a bike at the station which has more available bikes and returning 
 * the bike at the station which has more available slots. 
 * This user never reserves neither bikes nor slots at destination stations as he knows that the 
 * system is recommending him that station because it is the station which has more available 
 * bikes or slots, so he knows that, almost certainly, he'll be able to rent or to return a bike. 
  * Moreover, he always chooses the shortest routes to get his destination.
 * 
 * @author IAgroup
 */
@AssociatedType(UserType.USER_OBEDIENT)
public class UserObedient extends User {

    public class UserObedientParameters {
        /**
         * It is the number of times that the user musts try to make a bike reservation before
         * deciding to leave the system.
         */
        private int minReservationAttempts = systemManager.getRandom().nextInt(3, 7);

        /**
         * It is the number of times that a reservation timeout event musts occurs before the
         * user decides to leave the system.
         */
        private int minReservationTimeouts = systemManager.getRandom().nextInt(2, 5);

        /**
         * It is the number of times that the user musts try to rent a bike (without a bike
         * reservation) before deciding to leave the system.
         */
        private int minRentalAttempts = systemManager.getRandom().nextInt(4, 8);

        /**
         * It determines the rate with which the user will decide to go directly to a station
         * in order to return the bike he has just rented.
         */
        private int bikeReturnPercentage;

        /**
         * It determines the rate with which the user will choose a new destination station
         * after a  timeout event happens.
         */
        private int reservationTimeoutPercentage;

        /**
         * It determines the rate with which the user will choose a new destination station
         * after he hasn't been able to make a reservation.
         */
        private int failedReservationPercentage;

        
        /**
         * It determines if the user will make a reservation or not.
         */
        private boolean willReserve;
                
        private UserObedientParameters() {}

    }

    private UserObedientParameters parameters;
    
    public UserObedient(UserObedientParameters parameters) {
        super();
        this.parameters = parameters;
    }
    
    @Override
    public boolean decidesToLeaveSystemAfterTimeout(int instant) {
        return getMemory().getReservationTimeoutsCounter() == parameters.minReservationTimeouts ? true : false;
    }

    @Override
    public boolean decidesToLeaveSystemAffterFailedReservation(int instant) {
        return getMemory().getReservationAttemptsCounter() == parameters.minReservationAttempts ? true : false;
    }

    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable(int instant) {
        return getMemory().getRentalAttemptsCounter() == parameters.minRentalAttempts ? true : false;
    }
    
    @Override
    public Station determineStationToRentBike(int instant) {
        List<Station> stations;
    	if (parameters.willReserve) {
        stations = systemManager.consultStationsWithoutBikeReservationAttempt(this, instant);
    	}
    	else {
    		stations = systemManager.consultStationsWithoutBikeRentalAttempts(this);  
    	}
     
    	Station destination = null;
            if (!stations.isEmpty()) {
                List<Station> recommendedStations = systemManager.getRecommendationSystem()
                    .recommendByAvailableBikesRatio(this.getPosition(), stations);
            if (!recommendedStations.isEmpty()) {
                destination = recommendedStations.get(0);
            }
        }
        return destination;
    }

    @Override
    public Station determineStationToReturnBike(int instant) {

        List<Station> stations;
        if (parameters.willReserve) {
            stations = systemManager.consultStationsWithoutSlotReservationAttempt(this, instant);
        }
        else {
            stations = systemManager.consultStationsWithoutBikeReturnAttempts(this);
        }

        List<Station> recommendedStations;
        Station destination;

        if (stations.isEmpty()) {
               stations = new ArrayList<Station>(systemManager.consultStations());
        }

        recommendedStations = systemManager.getRecommendationSystem()
            .recommendByAvailableSlotsRatio(this.getPosition(), stations);

        if (!recommendedStations.isEmpty()) {
            destination = recommendedStations.get(0);
        } else {
            recommendedStations = systemManager.consultStations();
            int index = systemManager.getRandom().nextInt(0, recommendedStations.size()-1);
            destination = recommendedStations.get(index);
        }

        return destination;
    }
    
    @Override
    public boolean decidesToReserveBikeAtSameStationAfterTimeout() {
        return parameters.willReserve;
    }

    @Override
    public boolean decidesToReserveBikeAtNewDecidedStation() {
    	return parameters.willReserve;
    }

    @Override
    public boolean decidesToReserveSlotAtSameStationAfterTimeout() {
    	return parameters.willReserve;
    }

    @Override
    public boolean decidesToReserveSlotAtNewDecidedStation() {
    	return parameters.willReserve;
    }

    @Override
    public GeoPoint decidesNextPoint() {
        return systemManager.generateBoundingBoxRandomPoint(SimulationRandom.getGeneralInstance());
    }

    @Override
    public boolean decidesToReturnBike() {
        int percentage = systemManager.getRandom().nextInt(0, 100);
        return percentage < parameters.bikeReturnPercentage ? true : false;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterTimeout() {
        int percentage = systemManager.getRandom().nextInt(0, 100);
        return percentage < parameters.reservationTimeoutPercentage ? true : false;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterFailedReservation() {
        int percentage = systemManager.getRandom().nextInt(0, 100);
        return percentage < parameters.failedReservationPercentage ? true : false;
    }
    
    @Override
    public GeoRoute determineRoute(List<GeoRoute> routes) throws GeoRouteException {
        if (routes.isEmpty()) {
            throw new GeoRouteException("Route is not valid");
        }
        // The route in first list position is the shortest.
        return routes.get(0);
    }

    @Override
    public String toString() {
        return super.toString() + "UserDistanceRestriction{" +
                "parameters=" + parameters +
                '}';
    }

}
