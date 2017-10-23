package com.urjc.iagroup.bikesurbanfloats.config.entrypoints;

import java.util.ArrayList;
import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.config.SystemConfiguration;
import com.urjc.iagroup.bikesurbanfloats.config.distributions.DistributionPoisson;
import com.urjc.iagroup.bikesurbanfloats.entities.User.*;
import com.urjc.iagroup.bikesurbanfloats.entities.User;
import com.urjc.iagroup.bikesurbanfloats.entities.factories.UserFactory;
import com.urjc.iagroup.bikesurbanfloats.events.EventUserAppears;
import com.urjc.iagroup.bikesurbanfloats.util.BoundingCircle;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.util.IdGenerator;

public class EntryPointPoisson implements EntryPoint {
	
	private GeoPoint position;
	private double radio; //meters
	private DistributionPoisson distribution;
	private UserType userType;
	private TimeRange timeRange;
	
	public EntryPointPoisson(GeoPoint position, DistributionPoisson distribution, UserType userType) {
		this.position = position;
		this.distribution = distribution;
		this.userType = userType;
		this.timeRange = null;
		this.radio = 0;
	}
	
	public EntryPointPoisson(GeoPoint position, DistributionPoisson distribution, 
			UserType userType, double radio) {
		this.position = position;
		this.distribution = distribution;
		this.userType = userType;
		this.radio = radio;
	}
	
	public EntryPointPoisson(GeoPoint position, DistributionPoisson distribution, 
			UserType userType, TimeRange timeRange) {
		this.position = position;
		this.distribution = distribution;
		this.userType = userType;
		this.timeRange = timeRange;
		this.radio = 0;
	}
	
	public EntryPointPoisson(GeoPoint position, DistributionPoisson distribution, 
			UserType userType, TimeRange timeRange,  double radio) {
		this.position = position;
		this.distribution = distribution;
		this.userType = userType;
		this.timeRange = timeRange;
		this.radio = radio;
	}


	private User createUser(IdGenerator userIdGenerator, UserFactory userFactory, SystemConfiguration systemConfig) {
		int id = userIdGenerator.next();
		BoundingCircle bcircle = new BoundingCircle(position, radio);
		User user;
		if(radio > 0.0) {
			GeoPoint randomPosition = bcircle.randomPointInCircle();
			user = userFactory.createUser(id, userType, randomPosition, systemConfig);
		}
		else {
			user = userFactory.createUser(id, userType, position, systemConfig);
		}
		return user;
	}

	@Override
	public List<EventUserAppears> generateEvents(SystemConfiguration systemConfig) {
		
		List<EventUserAppears> generatedEvents = new ArrayList<>();
		UserFactory userFactory = new UserFactory();
		int actualTime, endTime;
		IdGenerator userIdGenerator = systemConfig.getUserIdGenerator();
		if(timeRange == null) {
			actualTime = 0;
			endTime = systemConfig.getTotalTimeSimulation();
		}
		else {
			actualTime = timeRange.getStart();
			endTime = timeRange.getEnd();
		}
		while(actualTime < endTime) {
			User user = createUser(userIdGenerator, userFactory, systemConfig);
			int timeEvent = distribution.randomInterarrivalDelay();
			actualTime += timeEvent;
			EventUserAppears newEvent = new EventUserAppears(actualTime, user, systemConfig);
			generatedEvents.add(newEvent);
		}
		return generatedEvents;
	}
	
	@Override
	public String toString() {
		String result = position.toString();
		result += "| Distribution " + distribution.getDistribution();
		result += "| distributionParameter " + distribution.getLambda() + "\n";
		result += "user Type: " + userType;
		return result;
	}
	
}
