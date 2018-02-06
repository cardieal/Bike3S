package es.urjc.ia.bikesurbanfleets.users;

import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoRoute;
import es.urjc.ia.bikesurbanfleets.common.graphs.GraphManager;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GeoRouteCreationException;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GraphHopperIntegrationException;
import es.urjc.ia.bikesurbanfleets.entities.Station;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** 
 * This class is a system which recommends the user the stations that, with respect to a 
 * geographical point, are less than a certain preset distance.
 * The geographical point may be the user position, if he wants to rent or to return a bike 
 * in the closest station to himself, or a place he is going to reach, if he wants to return 
 * the bike in the closest station to that place.    
 * Then, this system gives the user a list of stations ordered ascending or descending (depending 
 *    
 * @author IAgroup
 *
 */
public class RecommendationSystem {
    
    /**
     * It is the maximum distance in meters between the recommended stations and the indicated 
     * geographical point.
     */
    private final int MAX_DISTANCE = 5000;
    
    /**
     * It alloows to manage routes. 
     */
    private GraphManager graph;
    
    /**
     * It establishes the way of recommending by distnace (linear or real distance)
     */
    private boolean linearDistance;
    
    public RecommendationSystem(GraphManager graph, boolean linearDistance) {
        this.graph = graph;
        this.linearDistance = linearDistance;
    }

    /**
     * It verifies which stations are less than MAX_DISTANCE meters in a straight line from 
     * the indicated geographical point. 
     * @param point It's the user current position or the geographical coordinates of a 
     * place the user wants to reach.
     * @param stations It's the initial set of stations within it has to filter those 
     * that don't exceed the preset maximum distance from the specified geographical point.      
     * @return an unordered list of stations from which the system will prepare its recommendations.
     */
    private List<Station> validStationsToRentBike(GeoPoint point, List<Station> stations) {
    	List<Station> validStations;
    	if (linearDistance) {
        validStations = stations.stream().filter(station -> station.getPosition()
        		.distanceTo(point) <= MAX_DISTANCE && station.availableBikes() > 0)
                .collect(Collectors.toList());
    	}
    	else {
    	    validStations = new ArrayList<>();
         List<GeoRoute> routes;            
         for(Station station: stations) {
             try {
                 routes = graph.obtainAllRoutesBetween(station.getPosition(), point);
             }
             catch(Exception e) {
                 continue;
             }
                
             List<GeoRoute> validRoutes = routes.stream().filter(route -> route
            		 .getTotalDistance() <= MAX_DISTANCE && station.availableBikes() > 0)
                 .collect(Collectors.toList());
                
             if (!validRoutes.isEmpty()) {
                 validStations.add(station);
             }
         }
    	}
    	return validStations;
    }
    
    private List<Station> validStationsToReturnBike(GeoPoint point, List<Station> stations) {
    	List<Station> validStations;
     if (linearDistance) {
        validStations = stations.stream().filter(station -> station
        		.getPosition().distanceTo(point) <= MAX_DISTANCE && station.availableSlots() > 0)
                .collect(Collectors.toList());
     }
     else {
    	 validStations = new ArrayList<>();
         List<GeoRoute> routes;            
         for(Station station: stations) {
             try {
                 routes = graph.obtainAllRoutesBetween(station.getPosition(), point);
             }
             catch(Exception e) {
                 continue;
             }
                
             List<GeoRoute> validRoutes = routes.stream().filter(route -> route
            		 .getTotalDistance() <= MAX_DISTANCE && station.availableSlots() > 0)
                 .collect(Collectors.toList());
                
             if (!validRoutes.isEmpty()) {
                 validStations.add(station);
             }
         }
     }
     return validStations;
    }

    /**
     * It recommends stations by the nunmber of available bikes they have: first, it recommends 
     * those which have the most bikes available and finally, those with the least bikes available.
     * @param point It's the user current position or the geographical coordinates of a 
     * place the user wants to reach.
     * @param stations It's the list of stations that has to be ordered by the number of 
     * available bikes. 
     * @return a list of stations ordered descending by the number of available bikes.
     */
    public List<Station> recommendByNumberOfBikes(GeoPoint point, List<Station> stations) {
        Comparator<Station> byNumberOfBikes = (s1, s2) -> Integer.compare(s2.availableBikes(), s1.availableBikes());
        return validStationsToRentBike(point, stations).stream().sorted(byNumberOfBikes).collect(Collectors.toList());
    }
    
    /**
     * It recommends stations by the nunmber of available slots they have: first, it recommends 
     * those which have the most slots available and finally, those with the least slots available.
     * @param point It's the user current position or the geographical coordinates of a 
     * place the user wants to reach.
     * @param stations It's the list of stations that has to be ordered by the number of 
     * available slots. 
     * @return a list of stations ordered descending by the number of available slots.
     */
    public List<Station> recommendByNumberOfSlots(GeoPoint point, List<Station> stations) {
        Comparator<Station> byNumberOfSlots = (s1, s2) -> Integer.compare(s2.availableSlots(), s1.availableSlots());
        return validStationsToReturnBike(point, stations).stream().sorted(byNumberOfSlots).collect(Collectors.toList());
    }
    
    /**
     * It recommends stations by a factor which consists of the quotient between the distance 
     * from each station to the specified geographical point and the number of available bikes 
     * the station contains: first, it recommends those stations which have the smallest proportion 
     * and finally, those with the greatest one (the smallest the quotient, the better the station).
     * @param point It's the user current position or the geographical coordinates of a 
     * place the user wants to reach.
     * @param stations It's the list of stations that has to be ordered by the previosuly 
     * described proportion (distance divided by number of available bikes). 
     * @return a list of stations ordered asscending by the previously described proportion.  
     */
    public List<Station> recommendByProportionBetweenDistanceAndBikes(GeoPoint point, List<Station> stations) {
      Comparator<Station> byProportion = (s1, s2) -> {
        	double distance1, distance2;
        	
        	if (linearDistance) {
        		distance1 = s1.getPosition().distanceTo(point); 
        		distance2 = s2.getPosition().distanceTo(point);
        	}
        	else {
        		distance1 = Double.MAX_VALUE;
	            distance2 = Double.MIN_VALUE;
	            try {
	        					distance1 = graph.obtainShortestRouteBetween(s1.getPosition(), point)
	        							.getTotalDistance();
	        					distance2 = graph.obtainShortestRouteBetween(s2.getPosition(), point)
	        							.getTotalDistance();
	            } catch (GraphHopperIntegrationException | GeoRouteCreationException e) {
	                e.printStackTrace();
	            }
        	}
         return Double.compare(distance1/s1.availableBikes(), distance2/s2.availableBikes());
        }; 
        List<Station> recommendedStations = validStationsToRentBike(point, stations)
        		.stream().sorted(byProportion).collect(Collectors.toList());
        if (recommendedStations.get(0).getPosition().equals(point)) {
        	recommendedStations.remove(0);
        }
        return recommendedStations;
    }
    
    /**
     * It recommends stations by a factor which consists of the quotient between the distance 
     * from each station to the specified geographical point and the number of available slots
     * the station contains: first, it recommends those stations which have the smallest proportion 
     * and finally, those with the greatest one (the smallest the quotient, the better the station).
     * @param point It's the user current position or the geographical coordinates of a 
     * place the user wants to reach.
     * @param stations It's the list of stations that has to be ordered by the previosuly 
     * described proportion (distance divided by number of available slots). 
     * @return a list of stations ordered asscending by the previously described proportion.  
     */
    public List<Station> recommendByProportionBetweenDistanceAndSlots(GeoPoint point, List<Station> stations) {
    	  Comparator<Station> byProportion = (s1, s2) -> {
          	double distance1, distance2;
          	
          	if (linearDistance) {
          		distance1 = s1.getPosition().distanceTo(point); 
          		distance2 = s2.getPosition().distanceTo(point);
          	}
          	else {
          		distance1 = Double.MAX_VALUE;
  	            distance2 = Double.MIN_VALUE;
  	            try {
  	        					distance1 = graph.obtainShortestRouteBetween(s1.getPosition(), point)
  	        							.getTotalDistance();
  	        					distance2 = graph.obtainShortestRouteBetween(s2.getPosition(), point)
  	        							.getTotalDistance();
  	            } catch (GraphHopperIntegrationException | GeoRouteCreationException e) {
  	                e.printStackTrace();
  	            }
          	}
           return Double.compare(distance1/s1.availableSlots(), distance2/s2.availableSlots());
          }; 
          List<Station> recommendedStations = validStationsToReturnBike(point, stations)
          		.stream().sorted(byProportion).collect(Collectors.toList());
          if (recommendedStations.get(0).getPosition().equals(point)) {
          	recommendedStations.remove(0);
          }
          return recommendedStations;
      }
    
    /**
     * It recommends stations by the distance (linear or real depending on a global configuration 
     * parameter) they are from the specified geographical point: first, it recommends 
     * those which are closest to the point and finally, those wich are the most 
     * distant to taht same point.
     * @param point It's the user current position or the geographical coordinates of a 
     * place the user wants to reach.
     * @param stations It's the list of stations that has to be ordered by the linear distance 
     * between them and the specified geographical point.  
     * @return a list of stations ordered asscending by the linear distance from them to 
     * the specified geographical point.
     */
    public List<Station> recommendToRentBikeByDistance(GeoPoint point, List<Station> stations) {
        Comparator<Station> byDistance = (s1, s2) -> {
        	double distance1 = 0.0; 
        	double distance2 = 0.0;
        	if (linearDistance) {
        	}
        	else {
            distance1 = Double.MAX_VALUE;
            distance2 = Double.MIN_VALUE;
            try {
                distance1 = graph.obtainShortestRouteBetween(s1.getPosition(), point).getTotalDistance();
                distance2 = graph.obtainShortestRouteBetween(s2.getPosition(), point).getTotalDistance();
            } catch (GraphHopperIntegrationException | GeoRouteCreationException e) {
                e.printStackTrace();
            }
        	}
         return Double.compare(distance1, distance2);
        };
        List<Station> recommendedStations = validStationsToRentBike(point, stations)
        		.stream().sorted(byDistance).collect(Collectors.toList());
        if (recommendedStations.get(0).getPosition().equals(point)) {
        	recommendedStations.remove(0);
        }
        return recommendedStations;
    }
    
    public List<Station> recommendToReturnBikeByDistance(GeoPoint point, List<Station> stations) {
        Comparator<Station> byDistance = (s1, s2) -> {
        	double distance1, distance2;
        	if (linearDistance) {
        		distance1 = s1.getPosition().distanceTo(point);
        		distance2 = s2.getPosition().distanceTo(point);
        	}
        	else {
            distance1 = Double.MAX_VALUE;
            distance2 = Double.MIN_VALUE;
            try {
                distance1 = graph.obtainShortestRouteBetween(s1.getPosition(), point).getTotalDistance();
                distance2 = graph.obtainShortestRouteBetween(s2.getPosition(), point).getTotalDistance();
            } catch (GraphHopperIntegrationException | GeoRouteCreationException e) {
                e.printStackTrace();
            }
        	}
         return Double.compare(distance1, distance2);
        };
        List<Station> recommendedStations = validStationsToReturnBike(point, stations)
        		.stream().sorted(byDistance).collect(Collectors.toList());
        if (recommendedStations.get(0).getPosition().equals(point) ) {
        	recommendedStations.remove(0);
        }
        return recommendedStations;
    }

}