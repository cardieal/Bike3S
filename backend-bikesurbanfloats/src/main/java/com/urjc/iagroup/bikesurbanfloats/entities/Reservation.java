package com.urjc.iagroup.bikesurbanfloats.entities;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.urjc.iagroup.bikesurbanfloats.entities.models.ReservationModel;
import com.urjc.iagroup.bikesurbanfloats.history.IdReferenceAdapter;
import com.urjc.iagroup.bikesurbanfloats.history.entities.HistoricReservation;
import com.urjc.iagroup.bikesurbanfloats.history.HistoryReference;
import com.urjc.iagroup.bikesurbanfloats.util.IdGenerator;

@HistoryReference(HistoricReservation.class)
public class Reservation implements Entity, ReservationModel<Bike, Station, User> {

    public enum ReservationType {
        SLOT, BIKE
    }

    public enum ReservationState {
        FAILED, ACTIVE, EXPIRED, SUCCESSFUL
    }

    public static int VALID_TIME;

    private static IdGenerator idGenerator = new IdGenerator();

    @Expose
    private int id;

    @Expose
    private int startInstant;  // instant when user makes the reservation

    @Expose
    private int endInstant;  // instant when reservation is resolved or expired

    @Expose
    private ReservationType type;

    @Expose
    private ReservationState state;

    @Expose @JsonAdapter(IdReferenceAdapter.class)
    private User user;

    @Expose @JsonAdapter(IdReferenceAdapter.class)
    private Station station;

    @Expose @JsonAdapter(IdReferenceAdapter.class)
    private Bike bike;  // bike which user has reserved or wants to return

    /**
     * As it receives a bike param, it creates an active reservation 
     */
    
    public Reservation(int startInstant, ReservationType type, User user, Station station, Bike bike) {
        this.id = idGenerator.next();
        this.startInstant = startInstant;
        this.endInstant = -1; // reservation has'nt ended
        this.type = type;
        this.state = ReservationState.ACTIVE;
        this.user = user;
        this.station = station;
        this.bike = bike;
    }
    
    /**
     * As it doesn't receive a bike param, it creates a failed reservation 
     */

    public Reservation(int startInstant, ReservationType type, User user, Station station) {
        this(startInstant, type, user, station, null);
        this.endInstant = startInstant;
        this.state = ReservationState.FAILED;
    }

    public int getId() {
        return id;
    }

    public int getStartInstant() {
        return startInstant;
    }

    public int getEndInstant() {
        return endInstant;
    }

    public ReservationType getType() {
        return type;
    }

    public ReservationState getState() {
        return state;
    }

    public User getUser() {
        return user;
    }

    public Station getStation() {
        return station;
    }

    public Bike getBike() {
        return bike;
    }

    public void expire() {
        this.state = ReservationState.EXPIRED;
        this.endInstant = this.startInstant + VALID_TIME;
    }

    public void resolve(int endInstant) {
        this.state = ReservationState.SUCCESSFUL;
        this.endInstant = endInstant;
    }
}