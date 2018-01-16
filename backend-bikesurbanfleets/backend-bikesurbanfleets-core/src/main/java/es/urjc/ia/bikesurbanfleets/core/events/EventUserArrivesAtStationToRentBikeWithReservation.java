package es.urjc.ia.bikesurbanfleets.core.events;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.core.entities.Entity;
import es.urjc.ia.bikesurbanfleets.core.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.core.entities.Station;
import es.urjc.ia.bikesurbanfleets.core.entities.users.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventUserArrivesAtStationToRentBikeWithReservation extends EventUser {

    private List<Entity> entities;
    private Station station;
    private Reservation reservation;

    public EventUserArrivesAtStationToRentBikeWithReservation(int instant, User user, Station station, Reservation reservation) {
        super(instant, user);
        this.entities = new ArrayList<>(Arrays.asList(user, station, reservation));
        this.station = station;
        this.reservation = reservation;
    }
    
    public Station getStation() {
        return station;
    }

    public Reservation getReservation() {
        return reservation;
    }

    @Override
    public List<Event> execute() throws Exception {
        List<Event> newEvents = new ArrayList<>();;
        user.setPosition(station.getPosition());
        reservation.resolve(instant);
        user.removeBikeWithReservationFrom(station);
        if (user.decidesToReturnBike()) {  // user goes directly to another station to return his bike
            newEvents = manageSlotReservationDecisionAtOtherStation();
        } else {   // user rides his bike to a point which is not a station
            GeoPoint point = user.decidesNextPoint();
            user.setDestination(point);
            int arrivalTime = user.timeToReach();
            newEvents.add(new EventUserWantsToReturnBike(getInstant() + arrivalTime, user, point));
        }
        return newEvents;
    }

    @Override
    public List<Entity> getEntities() {
        return entities;
    }
}