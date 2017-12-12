package com.urjc.iagroup.bikesurbanfloats.entities.users.types;

import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.entities.users.AssociatedType;
import com.urjc.iagroup.bikesurbanfloats.entities.users.User;
import com.urjc.iagroup.bikesurbanfloats.entities.users.UserType;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoRoute;
import com.urjc.iagroup.bikesurbanfloats.graphs.exceptions.GeoRouteException;
import com.urjc.iagroup.bikesurbanfloats.util.SimulationRandom;

import java.util.List;
import java.util.ArrayList;

// TODO: add lost documentation
@AssociatedType(UserType.USER_RANDOM)
public class UserRandom extends User {

    public UserRandom() {
        super();
    }

    @Override
    public boolean decidesToLeaveSystemAfterTimeout(int instant) {
        return systemManager.consultStationsWithBikeReservationAttempt(this, instant).size() == systemManager.consultStations().size();
    }


    @Override
    public boolean decidesToLeaveSystemAffterFailedReservation(int instant) {
        return systemManager.consultStationsWithBikeReservationAttempt(this, instant).size() == systemManager.consultStations().size();
    }


    @Override
    public boolean decidesToLeaveSystemWhenBikesUnavailable(int instant) {
        return systemManager.consultStationsWithBikeReservationAttempt(this, instant).size() == systemManager.consultStations().size();
    }

    @Override
    public Station determineStationToRentBike(int instant) {
    	List<Station> stations = systemManager.consultStationsWithoutBikeReservationAttempt(this, instant);
    	
     if (stations.isEmpty()) {
     	stations = new ArrayList<>(systemManager.consultStations());
     }

    	return systemManager.getRecommendationSystem()
    			.recommendByLinearDistance(this.getPosition(), stations).get(0);

    }

    @Override
    public Station determineStationToReturnBike(int instant) {
    	List<Station> stations = systemManager.consultStationsWithoutSlotReservationAttempt(this, instant);
    	
     if (stations.isEmpty()) {
     	stations = new ArrayList<>(systemManager.consultStations());
     }

    	return systemManager.getRecommendationSystem()
    			.recommendByLinearDistance(this.getPosition(), stations).get(0);
    }
    
    @Override
    public boolean decidesToReserveBikeAtSameStationAfterTimeout() {
        return systemManager.getRandom().nextBoolean();
    }
    
    @Override
    public boolean decidesToReserveBikeAtNewDecidedStation() {
        return systemManager.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToReserveSlotAtSameStationAfterTimeout() {
        return systemManager.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToReserveSlotAtNewDecidedStation() {
        return systemManager.getRandom().nextBoolean();
    }

    @Override
    public GeoPoint decidesNextPoint() {
        return systemManager.generateBoundingBoxRandomPoint(SimulationRandom.getGeneralInstance());
    }

    @Override
    public boolean decidesToReturnBike() {
        return systemManager.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterTimeout() {
        return systemManager.getRandom().nextBoolean();
    }

    @Override
    public boolean decidesToDetermineOtherStationAfterFailedReservation() {
        return systemManager.getRandom().nextBoolean();
    }

    @Override
    public GeoRoute determineRoute(List<GeoRoute> routes) throws GeoRouteException {
        if (routes.isEmpty()) {
            throw new GeoRouteException("Route is not valid");
        }
        int index = systemManager.getRandom().nextInt(0, routes.size());
        return routes.get(index);
    }

}