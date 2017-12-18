package ch.ethz.matsim.act_gen.trips;

import ch.ethz.matsim.act_gen.activities.ActivityChain;
import ch.ethz.matsim.act_gen.activities.ActivityChainImplFromPlan;
import ch.ethz.matsim.act_gen.activities.ActivityGenerator;
import ch.ethz.matsim.act_gen.activities.ActivityGeneratorImpl;
import ch.ethz.matsim.act_gen.travelzones.TravelZones;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.api.core.v01.population.Activity;

import java.util.*;

public class DemandDistributorImpl implements DemandDistributor {
//    private TravelZones travelZones;
//    private Scenario scenario;
//    private Map<Id<Person>, List<Integer>> person2ActivityZonesMap = new HashMap<>();
//    private Map<Id<Person>, List<Integer>> person2genericActivityZonesMap = new HashMap<>();
    private List<Person> personList = new ArrayList<>();
//    private Map<Integer, Id<ActivityFacility>> zone2activityFacilitiesMap = new HashMap<>();
    private ActivityGenerator activityGenerator;

    public DemandDistributorImpl(Scenario scenario, TravelZones travelZones) {
//        this.travelZones = travelZones;
//        this.scenario = scenario;
        this.activityGenerator = new ActivityGeneratorImpl(scenario, travelZones);

        for (Person person : scenario.getPopulation().getPersons().values()) {
            Plan plan = person.getSelectedPlan();
            ActivityChain activityChain = new ActivityChainImplFromPlan(plan);
            List<Integer> activityZones = new ArrayList<>();

            for (Activity activity : activityChain.getActivityChain()) {
                int zone = travelZones.getZone(activity.getCoord().getX(),activity.getCoord().getY());
                activityZones.add(zone);
            }
//            person2ActivityZonesMap.putIfAbsent(person.getId(), activityZones);
//            person2genericActivityZonesMap.putIfAbsent(person.getId(), new ArrayList<>());
            personList.add(person);
        }
    }

    @Override
    public void distribute(int[] inducedTripsPerZone) {
        Random random = new Random();

        for (int zone = 0; zone<inducedTripsPerZone.length; zone++) {
            while (inducedTripsPerZone[zone] > 0) {
                int personIndex = random.nextInt(personList.size());
                Person person = personList.get(personIndex);
                activityGenerator.generate(person, zone);
                inducedTripsPerZone[zone] -= 1;
            }
        }
    }

}
