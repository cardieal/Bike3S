package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.urjc.iagroup.bikesurbanfloats.entities.Entity;

import javax.validation.constraints.NotNull;

public interface HistoricEntity<E extends Entity> extends Entity {

    static JsonObject propertyChange(@NotNull Object oldProperty, @NotNull Object newProperty) {
        JsonObject property = new JsonObject();

        if (!oldProperty.equals(newProperty)) {
            property.add("old", History.gson.toJsonTree(oldProperty));
            property.add("new", History.gson.toJsonTree(newProperty));
        }

        return property;
    }

    static JsonObject idReferenceChange(Entity oldEntity, Entity newEntity) {
        Object oldId = oldEntity == null ? JsonNull.INSTANCE : oldEntity.getId();
        Object newId = newEntity == null ? JsonNull.INSTANCE : newEntity.getId();
        return propertyChange(oldId, newId);
    }
	
	default JsonObject makeChangeEntryFrom(E previousSelf) {
		if (previousSelf == null) return null;

		if (previousSelf.getId() != this.getId()) {
		    String msg = "The id of previousSelf must be identical! Got: " + previousSelf.getId() + ", expected: " + this.getId();
		    throw new IllegalArgumentException(msg);
        }

		return new JsonObject();
	}
	
}
