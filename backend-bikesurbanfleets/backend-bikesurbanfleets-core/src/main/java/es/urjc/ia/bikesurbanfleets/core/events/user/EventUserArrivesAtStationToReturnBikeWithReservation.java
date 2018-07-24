package es.urjc.ia.bikesurbanfleets.core.events.user;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.common.util.MessageGuiFormatter;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.users.User;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventUserArrivesAtStationToReturnBikeWithReservation extends EventUser {

    private List<Entity> entities;
    private Station station;
    private Reservation reservation;


    public EventUserArrivesAtStationToReturnBikeWithReservation(int instant, User user, Station station, Reservation reservation) {
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
    public List<Event> execute() {
        List<Event> newEvents = new ArrayList<>();
        try {
            user.setInstant(getInstant());
            user.returnBikeWithReservationTo(station);
            user.leaveSystem();
            debugEventLog("User returns the bike");
            debugClose(user, user.getId());
        }
        catch(Exception e) {
            exceptionTreatment(e);
        }
        return newEvents;
    }

    @Override
    public List<Entity> getEntities() {
        return entities;
    }
}