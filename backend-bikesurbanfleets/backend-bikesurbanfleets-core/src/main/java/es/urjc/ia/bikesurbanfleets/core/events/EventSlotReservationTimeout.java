package es.urjc.ia.bikesurbanfleets.core.events;

import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.common.util.MessageGuiFormatter;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.users.User;
import es.urjc.ia.bikesurbanfleets.users.UserMemory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventSlotReservationTimeout extends EventUser {
    private List<Entity> entities;
    private Reservation reservation;
    private GeoPoint positionTimeOut;

    public EventSlotReservationTimeout(int instant, User user, Reservation reservation, GeoPoint positionTimeOut) {
        super(instant, user);
        this.entities = new ArrayList<>(Arrays.asList(user, reservation));
        this.reservation = reservation;
        this.positionTimeOut = positionTimeOut;
    }

    public Reservation getReservation() {
        return reservation;
    }

    @Override
    public List<Event> execute()  {
        List<Event> newEvents = new ArrayList<>();
        try {
            user.setInstant(getInstant());
            user.setPosition(positionTimeOut);
            reservation.expire();
            user.cancelsSlotReservation(user.getDestinationStation());
            user.getMemory().update(UserMemory.FactType.SLOT_RESERVATION_TIMEOUT);

            debugEventLog();
            if (!user.decidesToDetermineOtherStationAfterTimeout()){
                debugEventLog("User decides to manage slot reservation at other Station");
                newEvents = manageSlotReservationDecisionAtSameStationAfterTimeout();
            } else {
                debugEventLog("User decides to manage slot reservation at the same Station");
                newEvents = manageSlotReservationDecisionAtOtherStation();
            }
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