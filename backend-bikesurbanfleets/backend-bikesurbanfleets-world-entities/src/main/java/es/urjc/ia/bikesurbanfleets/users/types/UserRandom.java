package es.urjc.ia.bikesurbanfleets.users.types;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GeoRouteException;
import es.urjc.ia.bikesurbanfleets.common.util.SimulationRandom;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.users.AssociatedType;
import es.urjc.ia.bikesurbanfleets.users.User;
import es.urjc.ia.bikesurbanfleets.users.UserType;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a user who makes most of his decissions randomly.
 * This user always chooses the closest destination station and the shortest route to reach it.
 * Moreover, this type of user only leaves the system when he has tried to make a bike 
 * reservation in all the system's stations and he hasn't been able.
 *   
 * @author IAgroup
 *
 */
@AssociatedType(UserType.USER_RANDOM)
public class UserRandom extends User {

    public UserRandom() {
        super();
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
    	List<Station> stations = new ArrayList(infraestructure.consultStations());
     List<Station> triedStations = getMemory().getStationsWithBikeReservationAttempts(getInstant());
     stations.removeAll(triedStations);
     int index = infraestructure.getRandom().nextInt(0, stations.size());
     return stations.get(index);
    }

    @Override
    public Station determineStationToReturnBike() {
        List<Station> stations = new ArrayList(infraestructure.consultStations());
        List<Station> triedStations = getMemory().getStationsWithSlotReservationAttempts(getInstant());
        stations.removeAll(triedStations);
        int index = infraestructure.getRandom().nextInt(0, stations.size());
        return stations.get(index);    
			}
		
    
    @Override
    public boolean decidesToReserveBikeAtSameStationAfterTimeout() {
        return infraestructure.getRandom().nextBoolean();
    }
    
    @Override
    public boolean decidesToReserveBikeAtNewDecidedStation() {
        return infraestructure.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToReserveSlotAtSameStationAfterTimeout() {
        return infraestructure.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToReserveSlotAtNewDecidedStation() {
        return infraestructure.getRandom().nextBoolean();
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
        return infraestructure.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterFailedReservation() {
        return infraestructure.getRandom().nextBoolean();
    }

    @Override
    public GeoRoute determineRoute(List<GeoRoute> routes) throws GeoRouteException {
        if (routes.isEmpty()) {
            throw new GeoRouteException("Route is not valid");
        }
        int index = infraestructure.getRandom().nextInt(0, routes.size());
        return routes.get(index);
    }

}