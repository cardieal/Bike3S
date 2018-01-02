package es.urjc.ia.bikesurbanfleets.events;

import es.urjc.ia.bikesurbanfleets.entities.Entity;
import es.urjc.ia.bikesurbanfleets.entities.Station;
import es.urjc.ia.bikesurbanfleets.entities.users.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventUserArrivesAtStationToReturnBikeWithoutReservation extends EventUser {

    private List<Entity> entities;
    private Station station;

    public EventUserArrivesAtStationToReturnBikeWithoutReservation(int instant, User user, Station station) {
        super(instant, user);
        this.entities = Arrays.asList(user, station);
        this.station = station;
    }
    
    public Station getStation() {
        return station;
    }

    @Override
    public List<Event> execute() throws Exception {
        List<Event> newEvents = new ArrayList<>();
        user.setPosition(station.getPosition());
        if(!user.returnBikeWithoutReservationTo(station)) {
            newEvents = manageSlotReservationDecisionAtOtherStation();
        }      
        return newEvents;
    }

    @Override
    public List<Entity> getEntities() {
        return entities;
    }
}
