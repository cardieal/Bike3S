package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;

class HistoricUser extends Person {

    private HistoricBike bike;

    HistoricUser(Person user) {
        super(user.getId(), new GeoPoint(user.getPosition()));

        this.bike = user.getBike() == null ? null : new HistoricBike(user.getBike());
    }

    JsonObject getChanges(HistoricUser previousSelf) {
        if (previousSelf == null) return null;

        JsonObject changes = new JsonObject();
        boolean hasChanges = false;

        changes.add("id", new JsonPrimitive(this.getId()));

        JsonObject bike = History.idChange(previousSelf.bike, this.bike);

        if (bike != null) {
            changes.add("bike", bike);
            hasChanges = true;
        }

        if (!this.getPosition().equals(previousSelf.getPosition())) {
            double dlat = this.getPosition().getLatitude() - previousSelf.getPosition().getLatitude();
            double dlon = this.getPosition().getLongitude() - previousSelf.getPosition().getLongitude();
            changes.add("position", History.gson.toJsonTree(new GeoPoint(dlat, dlon)));
            hasChanges = true;
        }

        // TODO: implement other possible changes like destination

        return hasChanges ? changes : null;
    }

    @Override
    public Bike getBike() {
        return this.bike;
    }

    @Override
    public Station determineStation() {
        return null;
    }

    @Override
    public boolean decidesToReserveBike(Station station) {
        return false;
    }

    @Override
    public boolean decidesToReserveSlot(Station station) {
        return false;
    }

    @Override
    public GeoPoint decidesNextPoint() {
        return null;
    }

    @Override
    public boolean decidesToReturnBike() {
        return false;
    }
}
