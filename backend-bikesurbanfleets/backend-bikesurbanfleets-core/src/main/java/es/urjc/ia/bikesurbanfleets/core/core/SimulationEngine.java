package es.urjc.ia.bikesurbanfleets.core.core;


import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.interfaces.Event;
import es.urjc.ia.bikesurbanfleets.core.config.StationsConfig;
import es.urjc.ia.bikesurbanfleets.core.config.UsersConfig;
import es.urjc.ia.bikesurbanfleets.core.events.EventUser;
import es.urjc.ia.bikesurbanfleets.core.events.EventUserAppears;
import es.urjc.ia.bikesurbanfleets.common.config.GlobalInfo;
import es.urjc.ia.bikesurbanfleets.history.History;
import es.urjc.ia.bikesurbanfleets.history.entities.HistoricReservation;
import es.urjc.ia.bikesurbanfleets.infraestructure.InfraestructureManager;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Reservation;
import es.urjc.ia.bikesurbanfleets.log.Debug;
import es.urjc.ia.bikesurbanfleets.users.User;
import es.urjc.ia.bikesurbanfleets.users.UserFactory;
import es.urjc.ia.bikesurbanfleets.usersgenerator.SingleUser;
import org.apache.commons.math3.util.Precision;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * This is the most important class which makes the simulation possible.
 * It uses a queue to manage the events proccessing.
 * @author IAgroup
 *
 */
public class SimulationEngine {

    private PriorityQueue<Event> eventsQueue = new PriorityQueue<>();
    private GlobalInfo globalInfo;
    private StationsConfig stationsInfo;
    private UsersConfig usersInfo;
    private InfraestructureManager systemManager;

    /**
     * It creates an event queue where its events are sorted by the time instant when they'll occur.
     */
    public SimulationEngine(GlobalInfo globalInfo, StationsConfig stationsInfo, UsersConfig usersInfo,
                            InfraestructureManager systemManager) {
        this.globalInfo = globalInfo;
        this.stationsInfo = stationsInfo;
        this.usersInfo = usersInfo;
        this.systemManager = systemManager;
        this.eventsQueue = new PriorityQueue<>(processUsers());
        Reservation.VALID_TIME = globalInfo.getReservationTime();
        Debug.DEBUG_MODE = globalInfo.isDebugMode();

        //needed if there's no reservations in the system
        History.reservationClass(HistoricReservation.class);
    }

    private List<EventUserAppears> processUsers() {
        List<EventUserAppears> eventUserAppearsList = new ArrayList<>();
        UserFactory userFactory = new UserFactory();
        for (SingleUser singleUser: usersInfo.getUsers()) {
            User user = userFactory.createUser(singleUser.getUserType());
            int instant = singleUser.getTimeInstant();
            GeoPoint position = singleUser.getPosition();
            eventUserAppearsList.add(new EventUserAppears(instant, user, position));
        }

        eventUserAppearsList.stream()
                .map(EventUser::getUser)
                .forEach(user -> user.setSystemManager(systemManager));
        return eventUserAppearsList;
    }

    public void run() throws Exception {

        History.init(globalInfo.getHistoryOutputPath());
        Debug.init();

        // Those variables are used to control de percentage of the simulation done
        int totalUsers = eventsQueue.size();
        double percentage = 0;

            System.out.println("Percentage: " + percentage);

        while (!eventsQueue.isEmpty()) {
            Event event = eventsQueue.poll();  // retrieves and removes first element

            if(event.getClass().getSimpleName().equals(EventUserAppears.class.getSimpleName())) {
                percentage += (((double) 1 /(double) totalUsers) * 100);
                System.out.println("Percentage: " + Precision.round(percentage, 2) + "\n");
            }

            if(Debug.DEBUG_MODE) {
                System.out.println(event.toString());
            }

            List<Event> newEvents = event.execute();
            eventsQueue.addAll(newEvents);
            History.registerEvent(event);
        }

        History.close();
        Debug.closeAllLogs();
    }

}