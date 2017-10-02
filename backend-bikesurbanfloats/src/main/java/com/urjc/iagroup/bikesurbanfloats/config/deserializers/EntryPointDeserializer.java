package com.urjc.iagroup.bikesurbanfloats.config.deserializers;

import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.EntryPoint;
import com.urjc.iagroup.bikesurbanfloats.config.entrypoints.EntryPointPoisson;
import com.urjc.iagroup.bikesurbanfloats.entities.factories.EntryPointFactory;
import com.urjc.iagroup.bikesurbanfloats.util.DistributionType;

public class EntryPointDeserializer implements JsonDeserializer<EntryPoint>  {

	private EntryPointFactory entryPointFactory;
	
	public EntryPointDeserializer() {
		this.entryPointFactory = new EntryPointFactory();
	}
	
	@Override
	public EntryPoint deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		
		Gson gson = new Gson();
		JsonObject	jsonElementEntryP = json.getAsJsonObject();
		DistributionType distribution = null;
		
		// if entryPoint does'nt contain a distribution attribute, it's of type single (one person)
		if (jsonElementEntryP.has(distribution.name())) {
			String distributionStr = jsonElementEntryP.get("distribution")
					.getAsJsonObject().get("distributionType").getAsString();
			distribution = DistributionType.valueOf(distributionStr);
		}
		else {
			distribution = DistributionType.SINGLE;		
			}
		
		return entryPointFactory.createEntryPoint(jsonElementEntryP, distribution);
		
	}

}
