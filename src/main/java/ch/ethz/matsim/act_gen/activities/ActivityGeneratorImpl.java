package ch.ethz.matsim.act_gen.activities;

import ch.ethz.matsim.act_gen.travelzones.TravelZones;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.routes.RouteFactory;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.FacilitiesUtils;
import org.matsim.vehicles.VehicleUtils;

import java.util.*;

public class ActivityGeneratorImpl implements ActivityGenerator {
    private Scenario scenario;
    private Map<Integer, List<Coord>> zone2coordMap = new HashMap<>();
    private Random random;

    public ActivityGeneratorImpl(Scenario scenario, TravelZones travelZones) {
        this.scenario = scenario;
        this.random = new Random();

        for (ActivityFacility activityFacility : scenario.getActivityFacilities().getFacilities().values()) {
            Coord coord = activityFacility.getCoord();
            int zone = travelZones.getZone(coord.getX(), coord.getY());
            zone2coordMap.putIfAbsent(zone, new ArrayList<>());
            zone2coordMap.get(zone).add(coord);
        }
    }

    @Override
    public void generate(Person person, int zone) {
        String actType = "generic";
        Coord coord = selectCoordinate(zone);
        Activity act = PopulationUtils.createActivityFromCoord(actType, coord);

        double startTime = selectStartTime(person);
        double endTime = selectEndTime(person);
        Id<ActivityFacility> facilityId = selectFacilityId(person);
//        String transportMode = "generic";

        act.setStartTime(startTime);
        act.setEndTime(endTime);
        act.setFacilityId(facilityId);

//        Leg leg = PopulationUtils.createLeg(transportMode);
//        leg.setDepartureTime();
//        leg.setRoute(RouteUtils.createGenericRouteImpl());

        person.getSelectedPlan().addActivity(act);

        return 0;
    }

    private Coord selectCoordinate(int zone) {
        Coord coord = new Coord(0.0,0.0);
        if (!zone2coordMap.get(zone).isEmpty()) {
            int coordIndex = this.random.nextInt(zone2coordMap.get(zone).size());
            coord = zone2coordMap.get(zone).get(coordIndex);
        }
        return coord;
    }

    private double selectStartTime(Person person) {
        return 0.0;
    }

    private double selectEndTime(Person person) {
        return 0.0;
    }

    private Id<ActivityFacility> selectFacilityId(Person person) {
        return null;
    }

}
