package com.urjc.iagroup.bikesurbanfloats.entities;

import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.core.SystemManager;
import com.urjc.iagroup.bikesurbanfloats.entities.models.UserModel;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoRoute;
import com.urjc.iagroup.bikesurbanfloats.graphs.exceptions.GeoRouteCreationException;
import com.urjc.iagroup.bikesurbanfloats.graphs.exceptions.GeoRouteException;
import com.urjc.iagroup.bikesurbanfloats.graphs.exceptions.GraphHopperImplException;
import com.urjc.iagroup.bikesurbanfloats.history.HistoryReference;
import com.urjc.iagroup.bikesurbanfloats.history.entities.HistoricUser;
import com.urjc.iagroup.bikesurbanfloats.util.IdGenerator;
import com.urjc.iagroup.bikesurbanfloats.util.StaticRandom;

/**
 * This is the main entity of the the system
 * It represents the basic behaviour of all users type which can apper in the system
 * It provides an implementation for basic methods which manage common information for all kind of users
 * It provides a behaviour pattern (make decissions) which depends on specific user type properties  
 * @author IAgroup
  */

@HistoryReference(HistoricUser.class)
public abstract class User implements Entity, UserModel<Bike, Station> {

	public enum UserType {
        USER_TEST
	}

	private static IdGenerator idGenerator = new IdGenerator();

    private int id;
    /**
     * Current user position
     */
    private GeoPoint position;
    /**
     * Before user removes a bike or after returns it, this attribute is null
     * While user is cycling, this attribute contains the bike tha user has removed
     */
    private Bike bike;
    /**
     * It is the station to which user has decided to go at this moment
     */
    private Station destinationStation;
    /**
     * Speed at which user walks in meters per second
     */
    private double walkingVelocity;
    /**
     * Speed at which user cycles in meters per second
     */
    private double cyclingVelocity;  
    /**
     * It indicates if user has a reserved bike currently
     */
    private boolean reservedBike;
    /**
     * It indicates if user has a reserved slot currently
     */
    private boolean reservedSlot;
    /**
     * It is the user current (bike or slot) reservation, i. e., the last reservation user has made
     * If user hasn't made a reservation, this attribute is null
     */
    private Reservation reservation;
    
    protected GeoRoute currentRoute;
    
    protected SystemManager systemManager;
   
    public User() {
    	StaticRandom random = StaticRandom.createRandom();
    	
        this.id = idGenerator.next();

        this.position = null;
        this.bike = null;
        // random velocity between 3km/h and 7km/h in m/s
        this.walkingVelocity = random.nextInt(3, 8) / 3.6;
        // random velocity between 10km/h and 20km/h in m/s
        this.cyclingVelocity = random.nextInt(10, 21) / 3.6;
        this.reservedBike = false;
        this.reservedSlot = false;
        this.reservation = null;
        this.destinationStation = null;
        this.systemManager = null;
    }
    
    public void addReservation(Reservation reservation) {
    	systemManager.addReservation(reservation);
    	this.reservation = reservation;
    }

    public void setSystemManager(SystemManager systemManager) {
        this.systemManager = systemManager;
    }
    
	@Override
    public int getId() {
        return id;
    }

	@Override
    public GeoPoint getPosition() {
        return position;
    }

    public void setPosition(GeoPoint position) {
        this.position = position;
    }

    public void setPosition(Double latitude, Double longitude) {
    	this.position.setLatitude(latitude);
        this.position.setLongitude(longitude);
    }

    @Override
    public Bike getBike() {
        return bike;
    }

    public boolean hasBike() {
        return bike != null ? true : false;
    }

    public boolean hasReservedBike() {
        return reservedBike;
    }

    public boolean hasReservedSlot() {
        return reservedSlot;
    }
    
    public Reservation getReservation() {
    	return this.reservation;
    }
    
    public Station getDestinationStation() {
		return destinationStation;
	}

	public void setDestinationStation(Station destinationStation) {
		this.destinationStation = destinationStation;
	}
	
	public GeoRoute getRoute() {
		return this.currentRoute;
	}
	
	public void setRoute(GeoRoute route) {
		this.currentRoute = route;
	} 

    /**
     *  User tries to reserve a bike at the specified station
     * @param station: it is the station for which user wants to make a bike reservation
     * @return the reserved bike if user is able to reserve one at 
     * that station (there're available bikes) and false in other case
     */

    public Bike reservesBike(Station station) {
    	Bike bike = null;
    	if (station.availableBikes() > 0) {
    		this.reservedBike = true;
    		bike = station.reservesBike();
    	}
    	return bike;
    }

    /**
     * User tries to reserve a slot at the specified station
     * @param station: it is the station for which user wants to make a slot reservation
     * @return true if user is able to reserve a slot at 
     * that station (there're available slots) and false in other case
     */

    public boolean reservesSlot(Station station) {
    	if (station.availableSlots() > 0) {
    		this.reservedSlot = true;
    		station.reservesSlot();
    	}
    	return reservedSlot;
    }
    
    /**
     * User cancels his bike reservation at the specified station
     * @param station: it is station for which user wants to cancel his bike reservation  
     */

    public void cancelsBikeReservation(Station station) {
        this.reservedBike = false;
        station.cancelsBikeReservation(reservation);
    }

    /**
     * User cancels his slot reservation at the specified station
     * @param station: it is station for which user wants to cancel his slot reservation
     */

    public void cancelsSlotReservation(Station station) {
        this.reservedSlot = false;
        station.cancelsSlotReservation();
    }

    /**
     * User tries to remove a bike from specified station
     * @param station: it is the station where he wnats to remove (rent) a bike
     * @return true if user has been able to remove a bike (there are available bikes 
     * or he has a bike reservation) and false in other case
     */
    
	public boolean removeBikeWithoutReservationFrom(Station station) {
        if (hasBike()) {
            return false;
        }
        this.bike = station.removeBikeWithoutReservation();
        return hasBike();
    }
	
	/**
	 * User removes the reserved bike from the specified station
	 * @param station: it is the station where user goes to rent a bike
	 */
	
	public boolean removeBikeWithReservationFrom(Station station) {
		if (hasBike()) {
			return false;
		}
		if (hasReservedBike()) {
            // first, reservation is cancelled to let a bike available at station to make sure one bike is available for take away
            cancelsBikeReservation(station);
		}
		this.bike = station.removeBikeWithReservation(reservation);
		return true;
	}

	/**
	 * User tries to return his rented bike to the specified station
	 * @param station: it is the station where user wants to return his bike
	 * @return true if user has been ablo to return his bike (there available slots
	 *  or he has a slot reservation) and false in other case
	 */

    public boolean returnBikeWithoutReservationTo(Station station) {
        boolean returned = false;
    	if (!hasBike()) {
            // TODO: log warning (or throw error?)
            return false;
        }
        if(station.returnBike(this.bike)) {
        	this.bike = null;
        returned = true;
        }
        return returned;
    }
    
    public void returnBikeWithReservationTo(Station station) {
    	if (hasReservedSlot()) {
    		cancelsSlotReservation(station);
    	}
    }

    public double getWalkingVelocity() {
        return walkingVelocity;
    }

    public double getCyclingVelocity() {
        return cyclingVelocity;
    }

    /**
     * The user's average velocity in m/s
     * @return user walking velocity if he hasn't a bike at that moment and cycling velocity in other case
     */
    public double getAverageVelocity() {
        return !hasBike() ? walkingVelocity : cyclingVelocity;
    }
    
    public List<GeoRoute> calculateRouteStation(Station station) throws GeoRouteCreationException, GraphHopperImplException {
    	this.systemManager.getGraphManager().calculateRoutes(position, station.getPosition());
		return this.systemManager.getGraphManager().getAllRoutes();
    }
    
    public List<GeoRoute> calculateRouteByPosition(GeoPoint finalPosition) throws GeoRouteCreationException, GraphHopperImplException{
    	this.systemManager.getGraphManager().calculateRoutes(position, finalPosition);
    	return this.systemManager.getGraphManager().getAllRoutes();
    }
    
    public GeoRoute reachedRouteUntilTimeOut() throws GeoRouteException, GeoRouteCreationException {
    	return currentRoute.calculateRouteByTimeAndVelocity(Reservation.VALID_TIME, this.getAverageVelocity());
    }
    
    /**
     * When user is going to a station and timeout happens, it calculates how far 
     * he has gotten in order to update his position.
     * This position is currently at the last position of the current route
     */
    
    public void updatePositionAfterTimeOut() {
    	List<GeoPoint> pointList = currentRoute.getPointList();
    	position = pointList.get(pointList.size() - 1);
    }

    /**
     * Time in seconds that user takes in arriving to a GeoPoint
     * time = distance/velocity
     * @throws Exception 
     */
    
    public int timeToReach() {
		return (int) (currentRoute.getDistance()/getAverageVelocity());
    }

    /**
     * User decides if he'll leave the system when bike reservation timeout happens 
     * @param instant: itt is the time instant when h'll make this decision
     * @return true if he decides to leave the system and false in other case (he decides to continue at system)
     */
    
    public abstract boolean decidesToLeaveSystemAfterTimeout(int instant);
    
    /**
    * User decides if he'll leave the system after not being able to make a bike reservation  
    * @param instant: itt is the time instant when h'll make this decision
    * @return true if he decides to leave the system and false in other case (he decides to continue at system)
    */
    
    public abstract boolean decidesToLeaveSystemAffterFailedReservation(int instant);
    
    /**
     * User decides if he'll leave the system when there're no avalable bikes at station    
     * @param instant: itt is the time instant when h'll make this decision
     * @return true if he decides to leave the system and false in other case (he decides to continue at system)
     */
    
    public abstract boolean decidesToLeaveSystemWhenBikesUnavailable(int instant);

    /**
     * User decides to which station he wnats to go next to rent a bike
     * @param instant: it is the time instant when he needs to make this decision
     * @return station where user has decided to go
     */

    public abstract Station determineStationToRentBike(int instant);

    /**
     * User decides to which station he wnats to go next to return his bike
     * @param instant is the time instant when he needs to make this decision
     * @return station where user has decided to go
     */

    public abstract Station determineStationToReturnBike(int instant);
    
    /**
     * User decides if he'll try to make again a bike reservation at the previosly chosen station after timeout happens 
     * @return true if user decides to reserve a bike at the initially chosen station
     */
    
    public abstract boolean decidesToReserveBikeAtSameStationAfterTimeout();
    
    /**
     * User decides if he'll try to make a bike reservation at a new chosen station

     * @return true if user decides to reserve a bike at that new station and false in other case
     */
        
    public abstract boolean decidesToReserveBikeAtNewDecidedStation();
    
    /**
     * User decides if he'll try to make again a slot reservation at the previosly chosen station 
     * @return true if user decides to reserve a slot at the initially chosen station
     */
    
    public abstract boolean decidesToReserveSlotAtSameStationAfterTimeout();
    
    /**
     * User decides if he'll try to make a slot reservation at a new chosen station
     * @return true if user decides to reserve a slot at that new station and false in other case
     */
    
    public abstract boolean decidesToReserveSlotAtNewDecidedStation();

    /**
     * User decides the point (it is not a station) to which he wants to cycle the rented bike just after removing it from station
     * @return the point where he wants to go after making his decision
     * @throws Exception 
     */

    public abstract GeoPoint decidesNextPoint();

    /**
     * User decides if he'll ride the bike to a station, just after removing it, in order to return it 
     * @return true if user decides to cycle directly to a station in order to return 
     * his bike and false in other case (he decides to ride it to another point before returning it)
     */

    public abstract boolean decidesToReturnBike(); 
    

    /**
     * When timeout happens, he decides to continue going to that chosen station or to go to another one
     * @return true if user chooses a new station to go and false if he continues to the previously chosen one
     */

    public abstract boolean decidesToDetermineOtherStationAfterTimeout();
    
    public abstract GeoRoute determineRoute(List<GeoRoute> routes) throws GeoRouteException;
        
/**
 * When user hasn't been able to make a reservation at the destination station, he decides if he wants to choose another destination station
 * @return true if he decides to determine another destination station and false in other case (he keeps his previously decision) 
 */
        
        public abstract boolean decidesToDetermineOtherStationAfterFailedReservation();
    @Override
    public String toString() {
        String result = "| Id: " + getId();
        result += "| Actual Position: " + position.toString();
        result += " | Has Bike: " + hasBike();
        result += " | Actual velocity: " + getAverageVelocity();
        result += 	" | Has reserved bike: "+hasReservedBike();
        result += " | Has reserved slot: "+hasReservedSlot()+"\n";
        return result;
    }
}