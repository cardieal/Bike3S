package com.urjc.iagroup.bikesurbanfloats.core;

import com.urjc.iagroup.bikesurbanfloats.events.*;
import com.urjc.iagroup.bikesurbanfloats.config.*;
import java.util.PriorityQueue;
import java.util.List;


public class SimulationEngine {
	
	private PriorityQueue<Event> eventsQueue = new PriorityQueue<>();
	
	public SimulationEngine() {
		eventsQueue = new PriorityQueue<Event>();
	}
	
	public void processConfig() {
		List<EntryPoint> entryPoints = ConfigInfo.entryPoints;
		for(EntryPoint entryPoint: entryPoints) {
			List<Event> events = entryPoint.generateEvents();
			for(Event event: events) {
				eventsQueue.add(event);
				//Temporal println
				System.out.println("Added person at instant " + event.getInstant());
			}
				
		}
		
	}
	
	public void run() {
		while (!eventsQueue.isEmpty()) {
			Event event = eventsQueue.poll();  // retrieves and removes first element
			List<Event> newEvents = event.execute();
			System.out.println(event.toString());

			if (!newEvents.isEmpty()) {
				for(Event newEvent: newEvents) {
					eventsQueue.add(newEvent);
				}
			}
		}
	}

}