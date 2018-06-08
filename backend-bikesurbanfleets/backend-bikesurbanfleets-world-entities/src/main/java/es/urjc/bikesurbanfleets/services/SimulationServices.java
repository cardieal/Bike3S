package es.urjc.bikesurbanfleets.services;

import es.urjc.ia.bikesurbanfleets.common.graphs.GraphHopperIntegration;
import es.urjc.ia.bikesurbanfleets.common.graphs.GraphManager;
import es.urjc.ia.bikesurbanfleets.common.graphs.exceptions.GraphHopperIntegrationException;
import es.urjc.ia.bikesurbanfleets.common.util.BoundingBox;
import es.urjc.ia.bikesurbanfleets.comparators.StationComparator;
import es.urjc.ia.bikesurbanfleets.consultSystems.InformationSystem;
import es.urjc.ia.bikesurbanfleets.consultSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.consultSystems.recommendationSystemTypes.RecommendationSystemByAvailableResourcesRatio;
import es.urjc.ia.bikesurbanfleets.infraestructure.InfraestructureManager;
import es.urjc.ia.bikesurbanfleets.infraestructure.entities.Station;

import java.io.IOException;
import java.util.List;

public class SimulationServices {

    private final String INIT_EXCEPTION_MESSAGE = "Simulation Service is not started correctly." +
            " You should init all the services";
    private final String GRAPH_HOPPER_EXC_MESSAGE = "You need to set a map for " +
            "Graphhopper integration";
    private final String GRAPH_MANAGER_EXC_MESSAGE = "Graph manager is not initialized correctly";
    private final String RECOM_SYSTEM_EXC_MESSAGE = "Recommendation System is not initialized correctly";

    private InfraestructureManager infrastructureManager;
    private RecommendationSystem recommendationSystem;
    private InformationSystem informationSystem;
    private GraphManager graphManager;
    private StationComparator stationComparator;

    public SimulationServices(SimulationServiceConfigData configData)
            throws IOException, GraphHopperIntegrationException {
        this.infrastructureManager = new InfraestructureManager(configData.getStations(), configData.getBbox());
        this.informationSystem = new InformationSystem(this.infrastructureManager);
        this.recommendationSystem = initRecommendationSystem(configData.getRecomSystemType(), configData.getMaxDistance());
        this.graphManager = initGraphManager(configData.getGraphManagerType(), configData.getMapDir());
        this.stationComparator = new StationComparator();
    }

    private RecommendationSystem initRecommendationSystem(RecomSystemType type, Integer maxDistance) throws IllegalStateException {
        switch(type) {
            case AVAILABLE_RESOURCES_RATIO:
                if(maxDistance == null) {
                    return new RecommendationSystemByAvailableResourcesRatio(this.infrastructureManager);
                }
                return new RecommendationSystemByAvailableResourcesRatio(this.infrastructureManager, maxDistance);
        }
        throw new IllegalStateException(RECOM_SYSTEM_EXC_MESSAGE);
    }

    private GraphManager initGraphManager(GraphManagerType type, String mapDir)
            throws IOException, GraphHopperIntegrationException, IllegalStateException {
        switch(type) {
            case GRAPH_HOPPER:
                if(mapDir == null) {
                    throw new GraphHopperIntegrationException(GRAPH_HOPPER_EXC_MESSAGE);
                }
                return new GraphHopperIntegration(mapDir);
        }
        throw new IllegalStateException(GRAPH_MANAGER_EXC_MESSAGE);
    }

    public InfraestructureManager getInfrastructureManager() throws IllegalStateException {
        checkService();
        return this.infrastructureManager;
    }

    public RecommendationSystem getRecommendationSystem() {
        checkService();
        return this.recommendationSystem;
    }

    public InformationSystem getInformationSystem() {
        checkService();
        return this.informationSystem;
    }

    public GraphManager getGraphManager() {
        checkService();
        return graphManager;
    }

    public StationComparator getStationComparator() {
        checkService();
        return stationComparator;
    }


    private void checkService() throws IllegalStateException {
        if(recommendationSystem == null || graphManager == null) {
            throw new IllegalStateException(INIT_EXCEPTION_MESSAGE);
        }
    }



}
