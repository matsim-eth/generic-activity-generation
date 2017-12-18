package ch.ethz.matsim.act_gen;

import ch.ethz.matsim.act_gen.travelzones.TravelZones;
import ch.ethz.matsim.act_gen.travelzones.TravelZonesSimpleImpl;
import ch.ethz.matsim.act_gen.trips.DemandDistributor;
import ch.ethz.matsim.act_gen.trips.DemandDistributorImpl;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

public class SimpleTest {
    public static void main(String[] args){
        Config config = ConfigUtils.loadConfig(args[0]);
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Population population = scenario.getPopulation();
        TravelZones travelZones = new TravelZonesSimpleImpl(scenario, 10, 10);
        DemandDistributor activityGenerator = new DemandDistributorImpl(scenario, travelZones);

        int[] inducedTripsPerZone = new int[travelZones.getNumberofZones()];
        for (int i = 89; i < inducedTripsPerZone.length; i++) {
            inducedTripsPerZone[i] = i;
        }

        activityGenerator.distribute(inducedTripsPerZone);
        System.out.println("End.");

    }
}
