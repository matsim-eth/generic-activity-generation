package ch.ethz.matsim.act_gen.activities;

import ch.ethz.matsim.act_gen.travelzones.TravelZones;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.population.PopulationUtils;
import org.matsim.facilities.ActivityFacility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityGeneratorImpl implements ActivityGenerator {
    Scenario scenario;
    Map<Integer, List<Coord>> zone2coordMap = new HashMap<>();

    public ActivityGeneratorImpl(Scenario scenario, TravelZones travelZones) {
        this.scenario = scenario;
        for (ActivityFacility activityFacility : scenario.getActivityFacilities().getFacilities().values()) {
            Coord coord = activityFacility.getCoord();
            int zone = travelZones.getZone(coord.getX(), coord.getY());
            zone2coordMap.putIfAbsent(zone, new ArrayList<>());
            zone2coordMap.get(zone).add(coord);
        }
    }

    @Override
    public void generate(Person person, int zone) {
        Coord coord = new Coord(0.0,0.0);
        if (!zone2coordMap.get(zone).isEmpty()) {
            coord = zone2coordMap.get(zone).get(0);
        }
        Activity act = PopulationUtils.createActivityFromCoord("generic", coord);
        person.getSelectedPlan().addActivity(act);
    }
}
