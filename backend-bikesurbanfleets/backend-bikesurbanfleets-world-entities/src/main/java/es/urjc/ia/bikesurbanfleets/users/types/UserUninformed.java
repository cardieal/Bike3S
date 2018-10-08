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
 * This class represents a user who doesn't know anything about the state of the system.
 * This user always chooses the closest destination station and the shortest route to reach it.
 * This user decides to leave the system randomly when a reservation fails if reservations are active
 *
 * @author IAgroup
 *
 */
@UserType("USER_UNINFORMED")
public class UserUninformed extends User {

    @UserParameters
    public class Parameters {

        private GeoPoint destinationPlace;

        private Parameters() {}

        @Override
        public String toString() {
            return "Parameters{" +
                "destinationPlace=" + destinationPlace +
            '}';
        }

    }

    private Parameters parameters;

    public UserUninformed(Parameters parameters, SimulationServices services) {
        super(services);
        this.parameters = parameters;
        this.destinationPlace = parameters.destinationPlace;
    }

    @Override
    public boolean decidesToLeaveSystemAfterTimeout() {
        return infraestructure.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToLeaveSystemAffterFailedReservation() {
        return infraestructure.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable() {
        return infraestructure.getRandom().nextBoolean();
    }

    @Override
    public Station determineStationToRentBike() {
        Station destination = null;
        List<Station> stations = infraestructure.consultStations();
        List<Station> triedStations = getMemory().getStationsWithRentalFailedAttempts();
        List<Station> finalStations = informationSystem.getStationsBikeOrderedByDistanceNoFiltered(this.getPosition());
        finalStations.removeAll(triedStations);

        if (!finalStations.isEmpty()) {
        	destination = finalStations.get(0);
        }
        return destination;
    }

    @Override
    public Station determineStationToReturnBike() {
        Station destination = null;
        List<Station> stations = infraestructure.consultStations();
        List<Station> triedStations = getMemory().getStationsWithReturnFailedAttempts();

        //Remove station if the user is in this station
        System.out.println("List Size" + stations.size());
        GeoPoint destinationPlace = parameters.destinationPlace;
        if(destinationPlace == null) {
            SimulationRandom random = SimulationRandom.getGeneralInstance();
            destinationPlace = this.infraestructure.generateBoundingBoxRandomPoint(random);
        }
        List<Station> finalStations = informationSystem.getStationsBikeOrderedByDistanceNoFiltered(this.destinationPlace);
        finalStations.removeAll(triedStations);
        if (!finalStations.isEmpty()) {
        	destination = finalStations.get(0);
        }
        return destination;
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
    public GeoPoint decidesNextPoint() {
        return infraestructure.generateBoundingBoxRandomPoint(SimulationRandom.getGeneralInstance());
    }

    @Override
    public boolean decidesToReturnBike() {
        return infraestructure.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterTimeout() {
        return false;
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterFailedReservation() {
        return false;
    }

    @Override
    public GeoRoute determineRoute() throws Exception{
        List<GeoRoute> routes = null;
        routes = calculateRoutes(getDestinationPoint());
        if(routes != null) {
            int index = infraestructure.getRandom().nextInt(0, routes.size());
            return routes.get(index);
        }
        else {
            return null;
        }
    }

}