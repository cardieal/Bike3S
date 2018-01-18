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
import java.util.stream.Collectors;

/**
 * This class represents a user whose behaviour is the same of UserReasonable with the 
 * exception that this user doesn't accept recommended stations which are farer that a 
 * certain distance. 
 * Then, if there are no stations to rent a bike which are nearer than the specified 
 * distance, he'll leave the system.
 * If there aren't any stations to return the bike nearer than the restrictive distance,
 * the user will go to the closest station.  
 * 
 * @author IAgroup
 *
 */
@AssociatedType(UserType.USER_DISTANCE_RESTRICTION)
public class UserDistanceRestriction extends User {

    public class UserDistanceRestrictionParameters {
        /**
         * It is the time in seconds until which the user will decide to continue walking
         * or cycling towards the previously chosen station without making a new reservation
         * after a reservation timeout event has happened.
         */
        private final int MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION = 180;

        /**
         * It is the number of times that the user musts try to make a bike reservation before
         * deciding to leave the system.
         */
        private int minReservationAttempts = systemManager.getRandom().nextInt(2, 5);

        /**
         * It is the number of times that a reservation timeout event musts occurs before the
         * user decides to leave the system.
         */
        private int minReservationTimeouts = systemManager.getRandom().nextInt(2, 4);

        /**
         * It is the number of times that the user musts try to rent a bike (without a bike
         * reservation) before deciding to leave the system.
         */
        private int minRentalAttempts = systemManager.getRandom().nextInt(3, 6);

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
         * It is a distance restriction: this user dosn't go to destination stations which are
         * farer than this distance.
         */
        private double maxDistance;

        @Override
        public String toString() {
            return "UserDistanceRestrictionParameters{" +
                    "MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION=" + MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION +
                    ", minReservationAttempts=" + minReservationAttempts +
                    ", minReservationTimeouts=" + minReservationTimeouts +
                    ", minRentalAttempts=" + minRentalAttempts +
                    ", bikeReturnPercentage=" + bikeReturnPercentage +
                    ", reservationTimeoutPercentage=" + reservationTimeoutPercentage +
                    ", failedReservationPercentage=" + failedReservationPercentage +
                    ", maxDistance=" + maxDistance +
                    '}';
        }
    }

    private UserDistanceRestrictionParameters parameters;

    public UserDistanceRestriction(UserDistanceRestrictionParameters parameters) {
        super();
        this.parameters = parameters;
    }
    
    @Override
    public boolean decidesToLeaveSystemAfterTimeout(int instant) {
        return getMemory().getCounterReservationTimeouts() == parameters.minReservationTimeouts ? true : false;
    }

    @Override
    public boolean decidesToLeaveSystemAffterFailedReservation(int instant) {
        return getMemory().getCounterReservationAttempts() == parameters.minReservationAttempts ? true : false;
    }

    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable(int instant) {
        return getMemory().getCounterRentingAttempts() == parameters.minRentalAttempts ? true : false;
    }
    
    @Override
    public Station determineStationToRentBike(int instant) {
        List<Station> stations = systemManager.consultStationsWithoutBikeReservationAttempt(this, instant);
        
     if (stations.isEmpty()) {
         stations = new ArrayList<>(systemManager.consultStations());
     }

     List<Station> recommendedStations = systemManager.getRecommendationSystem()
             .recommendByProportionBetweenDistanceAndBikes(this.getPosition(), stations);
     
     if (recommendedStations.get(0).getPosition().equals(this.getPosition( ))) {
    	 recommendedStations.remove(0);
     }
     
     // TODO: revise this code
     Station destination;
     try {
     destination = recommendedStations.stream().filter(station -> station
             .getPosition().distanceTo(this.getPosition()) <= parameters.maxDistance).findFirst().get();
     }
     catch (NullPointerException e) {
        destination = null;
     }
     
     return destination;
    }

    @Override
     public Station determineStationToReturnBike(int instant) {
        List<Station> stations = systemManager.consultStationsWithoutBikeReservationAttempt(this, instant);
        
        if (stations.isEmpty()) {
            stations = new ArrayList<>(systemManager.consultStations());
        }

        List<Station> recommendedStations = systemManager.getRecommendationSystem()
                .recommendByProportionBetweenDistanceAndSlots(this.getPosition(), stations);
        
        Station destination;
        List<Station> restrictedStations = recommendedStations.stream().filter(station -> station.getPosition()
                .distanceTo(this.getPosition()) <= parameters.maxDistance).collect(Collectors.toList());
        
        if (restrictedStations.size() > 0) {
        	destination = restrictedStations.get(0).getPosition().equals(this.getPosition()) && restrictedStations.size() > 1
                    ? restrictedStations.get(1) : restrictedStations.get(0);
        }
        else {
            recommendedStations = systemManager.getRecommendationSystem()
                .recommendByLinearDistance(this.getPosition(), stations);
            destination = recommendedStations.get(0).getPosition().equals(this.getPosition())
                    ? recommendedStations.get(1) : recommendedStations.get(0);
        }
        return destination;
    }
    
    @Override
    public boolean decidesToReserveBikeAtSameStationAfterTimeout() {
        int arrivalTime = timeToReach();
        return arrivalTime < parameters.MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION ? false : true;
    }

    @Override
    public boolean decidesToReserveBikeAtNewDecidedStation() {
        return true;
    }

    @Override
    public boolean decidesToReserveSlotAtSameStationAfterTimeout() {
        int arrivalTime = timeToReach();
        return arrivalTime < parameters.MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION ? false : true;
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

    @Override
    public String toString() {
        return super.toString() + "UserDistanceRestriction{" +
                "parameters=" + parameters +
                '}';
    }
}
