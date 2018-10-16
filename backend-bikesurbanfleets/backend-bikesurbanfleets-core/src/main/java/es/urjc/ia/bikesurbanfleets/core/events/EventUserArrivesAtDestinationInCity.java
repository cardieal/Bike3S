package es.urjc.ia.bikesurbanfleets.core.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Entity;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.users.User;

public class EventUserArrivesAtDestinationInCity extends EventUser {
	private List<Entity> entities;
        private GeoPoint currentPosition;
	
	public EventUserArrivesAtDestinationInCity(int instant, User user, GeoPoint position) {
		super(instant, user);
		entities = new ArrayList<>(Arrays.asList(user));
                currentPosition=position;
	}
	
	@Override
	public List<Event> execute() {
		debugEventLog("User arrives at his destination in city");
		user.setInstant(this.instant);
                user.setPosition(currentPosition);
 	
		user.leaveSystem();
		debugClose(user, user.getId());
		return new ArrayList<>();
	}
	
	@Override
	public List<Entity> getEntities() {
		return entities;
	}

}
