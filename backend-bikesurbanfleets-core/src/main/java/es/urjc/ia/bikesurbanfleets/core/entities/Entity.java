package es.urjc.ia.bikesurbanfleets.core.entities;

/**
 * This interface represents all the objects at the simulation problem domain.  
 * It forces all entity type components of the system to have an identifier.
 * @author IAgroup
 *
 */

public interface Entity {
    int getId();
}
