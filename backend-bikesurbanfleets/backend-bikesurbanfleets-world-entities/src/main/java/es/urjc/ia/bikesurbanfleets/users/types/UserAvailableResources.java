package es.urjc.ia.bikesurbanfleets.users.types;

import es.urjc.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.users.UserParameters;
import es.urjc.ia.bikesurbanfleets.users.UserType;
import es.urjc.ia.bikesurbanfleets.users.User;

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
@UserType("USER_AVAILABLE_RESOURCES")
public class UserAvailableResources extends User {

        @UserParameters
    public class Parameters {

        private Parameters() {}
        
        /**
         * It is the number of times that the user will try to rent a bike (without a bike
         * reservation) before deciding to leave the system.
         */
        private int minRentalAttempts = 2;


        @Override
        public String toString() {
            return "Parameters{" +
                    "minRentalAttempts=" + minRentalAttempts+
            '}';
        }

    }

    private Parameters parameters;

    public UserAvailableResources(Parameters parameters, SimulationServices services, GeoPoint finalDestination, long seed) {
        super(services,finalDestination,seed);
        this.parameters = parameters;
    }

    //**********************************************
    //Decision related to reservations
    @Override
    public boolean decidesToLeaveSystemAfterTimeout() {
        return false;
    }
    @Override
    public boolean decidesToLeaveSystemAffterFailedReservation() {
        return false;
    }
    @Override
    public boolean decidesToReserveBikeAtSameStationAfterTimeout() {
        return false;
    }

    @Override
    public boolean decidesToReserveBikeAtNewDecidedStation() {
        return false;
    }

    @Override
    public boolean decidesToReserveSlotAtSameStationAfterTimeout() {
        return false;
    }

    @Override
    public boolean decidesToReserveSlotAtNewDecidedStation() {
        return false;
    }
    @Override
    public boolean decidesToDetermineOtherStationAfterTimeout() {
        return false;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterFailedReservation() {
        return false;
    }

    //**********************************************
    //decisions related to taking and leaving a bike
    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable() {
        if (getMemory().getRentalAttemptsCounter() >= parameters.minRentalAttempts) 
            return true; 
        else return false;
     }


    //**********************************************
    //decisions related to either go directly to the destination or going arround

    @Override
    public boolean decidesToGoToPointInCity() {
        return false;
    }

    @Override
    public GeoPoint getPointInCity() {
        return null;
    }

 
    
    @Override
    public Station determineStationToRentBike() {
        List<Station> recommendedStations = informationSystem.getStationsOrderedByNumberOfBikes();
        Station destination = null;
        //Remove station if the user is in this station
      //  recommendedStations.removeIf(station -> station.getPosition().equals(this.getPosition()) && station.availableBikes() == 0);
        if (!recommendedStations.isEmpty()) {
            destination = recommendedStations.get(0);
        }
        return destination;
    }

    @Override
     public Station determineStationToReturnBike() {
        List<Station> recommendedStations = informationSystem.getStationsOrderedByNumberOfSlots();
        Station destination = null;
        //Remove station if the user is in this station
     //   recommendedStations.removeIf(station -> station.getPosition().equals(this.getPosition()));
        if (!recommendedStations.isEmpty()) {
            destination = recommendedStations.get(0);
        }
       return destination;
    }
    
 
    @Override
    public String toString() {
        return "UserAvailableResources{" +
                "parameters=" + parameters +
                '}';
    }
}
