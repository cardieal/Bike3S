package com.urjc.iagroup.bikesurbanfloats.entities.factory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.urjc.iagroup.bikesurbanfloats.config.EntryPoint;
import com.urjc.iagroup.bikesurbanfloats.config.EntryPointPoisson;
import com.urjc.iagroup.bikesurbanfloats.util.DistributionType;

public class EntryPointFactory {
	
	private Gson gson;
	
	public EntryPointFactory() {
		this.gson = new Gson();
	}
	
	public EntryPoint createEntryPoint(JsonObject json, DistributionType distribution) {
		switch(distribution) {
			case POISSON: return gson.fromJson(json, EntryPointPoisson.class);
			default: throw new JsonParseException("Type of EntryPoint doesn't exists");
		}
	}

}
