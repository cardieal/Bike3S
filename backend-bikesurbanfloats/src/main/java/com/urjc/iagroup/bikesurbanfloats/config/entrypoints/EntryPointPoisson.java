package com.urjc.iagroup.bikesurbanfloats.config.entrypoints;

import java.util.ArrayList;
import java.util.List;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;
import com.urjc.iagroup.bikesurbanfloats.config.distributions.DistributionPoisson;
import com.urjc.iagroup.bikesurbanfloats.entities.Person;
import com.urjc.iagroup.bikesurbanfloats.entities.factories.PersonFactory;
import com.urjc.iagroup.bikesurbanfloats.entities.factories.PersonType;
import com.urjc.iagroup.bikesurbanfloats.events.EventUserAppears;
import com.urjc.iagroup.bikesurbanfloats.util.BoundingCircle;
import com.urjc.iagroup.bikesurbanfloats.util.GeoPoint;
import com.urjc.iagroup.bikesurbanfloats.util.IdGenerator;
import com.urjc.iagroup.bikesurbanfloats.util.RandomUtil;

public class EntryPointPoisson implements EntryPoint {
	
	private GeoPoint position;
	private double radio; //meters
	private DistributionPoisson distribution;
	private PersonType personType;
	private TimeRange timeRange;
	
	public EntryPointPoisson(GeoPoint position, DistributionPoisson distribution, PersonType personType) {
		this.position = position;
		this.distribution = distribution;
		this.personType = personType;
		this.timeRange = null;
		this.radio = 0;
	}
	
	public EntryPointPoisson(GeoPoint position, DistributionPoisson distribution, 
			PersonType personType, double radio) {
		this.position = position;
		this.distribution = distribution;
		this.personType = personType;
		this.radio = radio;
	}
	
	public EntryPointPoisson(GeoPoint position, DistributionPoisson distribution, 
			PersonType personType, TimeRange timeRange) {
		this.position = position;
		this.distribution = distribution;
		this.personType = personType;
		this.timeRange = timeRange;
		this.radio = 0;
	}
	
	public EntryPointPoisson(GeoPoint position, DistributionPoisson distribution, 
			PersonType personType, TimeRange timeRange,  double radio) {
		this.position = position;
		this.distribution = distribution;
		this.personType = personType;
		this.timeRange = timeRange;
		this.radio = radio;
	}

	private Person createUser(IdGenerator personIdGenerator, PersonFactory personFactory, SystemInfo systemInfo) {
		int id = personIdGenerator.next();
		BoundingCircle bcircle = new BoundingCircle(position, radio, systemInfo.random);
		Person person;
		if(radio > 0.0) {
			GeoPoint randomPosition = bcircle.randomPointInCircle();
			person = personFactory.createPerson(id, personType, randomPosition, systemInfo);
		}
		else {
			person = personFactory.createPerson(id, personType, position, systemInfo);
		}
		return person;
	}

	@Override
	public List<EventUserAppears> generateEvents(SystemInfo systemInfo) {
		
		List<EventUserAppears> generatedEvents = new ArrayList<>();
		PersonFactory personFactory = new PersonFactory();
		int actualTime, endTime;
		IdGenerator userIdGenerator = systemInfo.userIdGenerator;
		RandomUtil random = systemInfo.random;
		if(timeRange != null) {
			actualTime = 0;
			endTime = systemInfo.totalTimeSimulation;
		}
		else {
			actualTime = timeRange.getStart();
			endTime = timeRange.getEnd();
		}
		while(actualTime < endTime) {
			Person person = createUser(userIdGenerator, personFactory, systemInfo);
			int timeEvent = distribution.randomInterarrivalDelay(random);
			System.out.println(timeEvent);
			actualTime += timeEvent;
			EventUserAppears newEvent = new EventUserAppears(actualTime, person, systemInfo);
			generatedEvents.add(newEvent);
		}
		return generatedEvents;
	}
	
	@Override
	public String toString() {
		String result = position.toString();
		result += "| Distribution " + distribution.getDistribution();
		result += "| distributionParameter " + distribution.getLambda() + "\n";
		result += "Person Type: " + personType;
		return result;
	}
	
}
