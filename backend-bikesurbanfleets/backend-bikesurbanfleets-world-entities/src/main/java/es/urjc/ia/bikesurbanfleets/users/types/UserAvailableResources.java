package es.urjc.ia.bikesurbanfleets.users.types;

import es.urjc.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GeoRouteCreationException;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GeoRouteException;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GraphHopperIntegrationException;
import es.urjc.ia.bikesurbanfleets.common.interfaces.StationInfo;
import es.urjc.ia.bikesurbanfleets.common.util.SimulationRandom;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.users.AssociatedType;
import es.urjc.ia.bikesurbanfleets.users.User;
import es.urjc.ia.bikesurbanfleets.users.UserType;

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
@AssociatedType(UserType.USER_AVAILABLE_RESOURCES)
public class UserAvailableResources extends User {

    public class UserAvailableResourcesParameters {
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
        private int minReservationAttempts = infraestructure.getRandom().nextInt(3, 7);

        /**
         * It is the number of times that a reservation timeout event musts occurs before the
         * user decides to leave the system.
         */
        private int minReservationTimeouts = infraestructure.getRandom().nextInt(2, 5);

        /**
         * It is the number of times that the user musts try to rent a bike (without a bike
         * reservation) before deciding to leave the system.
         */
        private int minRentalAttempts = infraestructure.getRandom().nextInt(4, 8);

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

        private UserAvailableResourcesParameters() {}

        @Override
        public String toString() {
            return "UserStationsBalancerParameters{" +
                    "MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION=" + MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION +
                    ", minReservationAttempts=" + minReservationAttempts +
                    ", minReservationTimeouts=" + minReservationTimeouts +
                    ", minRentalAttempts=" + minRentalAttempts +
                    ", bikeReturnPercentage=" + bikeReturnPercentage +
                    ", reservationTimeoutPercentage=" + reservationTimeoutPercentage +
                    ", failedReservationPercentage=" + failedReservationPercentage +
                    '}';
        }
    }

    private UserAvailableResourcesParameters parameters;
    
    public UserAvailableResources(UserAvailableResourcesParameters parameters, SimulationServices services) {
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
    public StationInfo determineStationToRentBike() {
        List<StationInfo> recommendedStations = informationSystem.getStationsOrderedByNumberOfBikes();
        StationInfo destination = null;
        //Remove station if the user is in this station
        recommendedStations.removeIf(station -> station.getPosition().equals(this.getPosition()));
        if (!recommendedStations.isEmpty()) {
            destination = recommendedStations.get(0);
        }
        return destination;
    }

    @Override
     public StationInfo determineStationToReturnBike() {
        List<StationInfo> recommendedStations = informationSystem.getStationsOrderedByNumberOfSlots();
        StationInfo destination = null;
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
        return false;
    }

    @Override
    public boolean decidesToReserveSlotAtSameStationAfterTimeout() {
        int arrivalTime = timeToReach();
        return arrivalTime < parameters.MIN_ARRIVALTIME_TO_RESERVE_AT_SAME_STATION ? false : true;
    }

    @Override
    public boolean decidesToReserveSlotAtNewDecidedStation() {
        return false;
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
    
    @Override
    public GeoRoute determineRoute() {
        List<GeoRoute> routes = null;
        try {
            routes = calculateRoutes(getDestinationStation().getPosition());
        }
        catch(Exception e) {
            System.err.println("Exception calculating routes \n" + e.toString());
        }
        return routes != null ? routes.get(0) : null;
    }

    @Override
    public String toString() {
        return super.toString() + "UserDistanceRestriction{" +
                "parameters=" + parameters +
                '}';
    }

}
