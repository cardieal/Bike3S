package es.urjc.ia.bikesurbanfleets.users.types;
import es.urjc.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.common.util.SimulationRandom;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.users.UserParameters;
import es.urjc.ia.bikesurbanfleets.users.UserType;
import es.urjc.ia.bikesurbanfleets.users.User;

import java.util.List;

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
@UserType("USER_DISTANCE_RESTRICTION")
public class UserDistanceRestriction extends User {

    @UserParameters
    public class Parameters {
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
        private int minReservationAttempts = infraestructure.getRandom().nextInt(2, 5);

        /**
         * It is the number of times that a reservation timeout event musts occurs before the
         * user decides to leave the system.
         */
        private int minReservationTimeouts = infraestructure.getRandom().nextInt(2, 4);

        /**
         * It is the number of times that the user musts try to rent a bike (without a bike
         * reservation) before deciding to leave the system.
         */
        private int minRentalAttempts = infraestructure.getRandom().nextInt(3, 6);

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
        private int maxDistance;

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

    private Parameters parameters;

    public UserDistanceRestriction(Parameters parameters, SimulationServices services) {
        super(services);
        this.parameters = parameters;
    }
    
    @Override
    public boolean decidesToLeaveSystemAfterTimeout() {
        return getMemory().getReservationTimeoutsCounter() == parameters.minReservationTimeouts ? true : false;
    }

    @Override
    public boolean decidesToLeaveSystemAffterFailedReservation() {
        return getMemory().getReservationAttemptsCounter() == parameters.minReservationAttempts ? true : false;
    }

    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable() {
        return getMemory().getRentalAttemptsCounter() == parameters.minRentalAttempts ? true : false;
    }
    
    @Override
    public Station determineStationToRentBike() {
        Station destination = null;
        List<Station> recommendedStations = informationSystem.getStationsOrderedByDistanceBikesRatio(this.getPosition(), parameters.maxDistance);
        //Remove station if the user is in this station
        recommendedStations.removeIf(station -> station.getPosition().equals(this.getPosition()) && station.availableBikes() == 0);
        if (!recommendedStations.isEmpty()) {
            destination = recommendedStations.get(0);
        }
        return destination;
    }

    @Override
     public Station determineStationToReturnBike() {
        Station destination = null;
        List<Station> recommendedStations = informationSystem.getStationsOrderedByDistanceSlotsRatio(this.getPosition());
        //Remove station if the user is in this station
        recommendedStations.removeIf(station -> station.getPosition().equals(this.getPosition()));
        if (!recommendedStations.isEmpty()) {
        	destination = recommendedStations.get(0);
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
        return infraestructure.generateBoundingBoxRandomPoint(SimulationRandom.getGeneralInstance());
    }

    @Override
    public boolean decidesToReturnBike() {
        int percentage = infraestructure.getRandom().nextInt(0, 100);
        return percentage < parameters.bikeReturnPercentage ? true : false;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterTimeout() {
        int percentage = infraestructure.getRandom().nextInt(0, 100);
        return percentage < parameters.reservationTimeoutPercentage ? true : false;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterFailedReservation() {
        int percentage = infraestructure.getRandom().nextInt(0, 100);
        return percentage < parameters.failedReservationPercentage ? true : false;
    }
    
    /**
     * The user chooses the shortest route because he wants to arrive at work as fast as possible.
     */
    @Override
    public GeoRoute determineRoute() throws Exception{
        List<GeoRoute> routes = null;
        routes = calculateRoutes(getDestinationPoint());
        return routes != null ? routes.get(0) : null;
    }

    @Override
    public String toString() {
        return super.toString() + "UserDistanceRestriction{" +
                "parameters=" + parameters +
                '}';
    }
}
