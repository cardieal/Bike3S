package com.urjc.iagroup.bikesurbanfloats.graphs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint3D;
import com.urjc.iagroup.bikesurbanfloats.graphs.exceptions.GeoRouteCreationException;
import com.urjc.iagroup.bikesurbanfloats.graphs.exceptions.GraphHopperImplException;

public class GraphHopperImpl implements GraphManager {

	private final String GRAPHHOPPER_DIR = "graphhopper_files";
	
	private GraphHopper hopper;
	private String locale;
	private GHResponse rsp;
	
	private GeoPoint startPosition;
	private GeoPoint endPosition;
	
	public GraphHopperImpl(String mapDir, String locale) throws IOException {
		FileUtils.deleteDirectory(new File(GRAPHHOPPER_DIR));
		this.hopper = new GraphHopperOSM().forServer();
		this.locale = locale;
		hopper.setDataReaderFile(mapDir);
		hopper.setGraphHopperLocation(GRAPHHOPPER_DIR);
		hopper.setEncodingManager(new EncodingManager("foot"));
		hopper.importOrLoad();
		
	}
	
	private GeoRoute responseGHToRoute(PathWrapper path) throws GeoRouteCreationException{
		List<GeoPoint> geoPointList = new ArrayList<>();
    	PointList ghPointList = path.getPoints();
    	geoPointList.add(startPosition);
    	for(GHPoint3D p: ghPointList) {
    		geoPointList.add(new GeoPoint(p.getLat(), p.getLon()));
    	}
    	geoPointList.add(endPosition);
    	GeoRoute route = new GeoRoute(geoPointList);
    	return route;
	}
	
	public void calculateRoutes(GeoPoint startPosition, GeoPoint endPosition) throws GraphHopperImplException  {
		GHRequest req = new GHRequest(
				startPosition.getLatitude(), startPosition.getLongitude(),
				endPosition.getLatitude(), endPosition.getLongitude()).
			setWeighting("fastest").
    	    setVehicle("foot").
    	    setLocale(Locale.forLanguageTag(locale));
    	GHResponse rsp = hopper.route(req);
    	
    	if(rsp.hasErrors()) {
     	   for(Throwable exception: rsp.getErrors()) {
     		  throw new GraphHopperImplException(exception.getMessage());
     	   }
     	}
    	this.rsp = rsp;
    	this.startPosition = startPosition;
    	this.endPosition = endPosition;
	}
	
	@Override
	public GeoRoute getBestRoute() throws GraphHopperImplException, GeoRouteCreationException {
		if(rsp == null){
			throw new GraphHopperImplException("Route is not calculated");
		}
    	PathWrapper path = rsp.getBest();
    	return responseGHToRoute(path);
	}

	@Override
	public List<GeoRoute> getAllRoutes() throws GraphHopperImplException, GeoRouteCreationException {
		if(rsp == null){
			throw new GraphHopperImplException("Route is not calculated");
		}
		List<GeoRoute> routes = new ArrayList<>();
		for(PathWrapper p: rsp.getAll()) {
			routes.add(responseGHToRoute(p));
		}
		return routes;
	}

	@Override
	public boolean hasAlternativesPath() throws GraphHopperImplException {
		if(rsp == null){
			throw new GraphHopperImplException("Route is not calculated");
		}
		return rsp.hasAlternatives();
	}
	

}
