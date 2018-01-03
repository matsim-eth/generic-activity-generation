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
    private List<Person> personList = new ArrayList<>();
    private ActivityGenerator activityGenerator;

    public DemandDistributorImpl(Scenario scenario, TravelZones travelZones) {
        this.activityGenerator = new ActivityGeneratorImpl(scenario, travelZones);

        for (Person person : scenario.getPopulation().getPersons().values()) {
            Plan plan = person.getSelectedPlan();
            ActivityChain activityChain = new ActivityChainImplFromPlan(plan);
            List<Integer> activityZones = new ArrayList<>();

            for (Activity activity : activityChain.getActivityChain()) {
                int zone = travelZones.getZone(activity.getCoord().getX(),activity.getCoord().getY());
                activityZones.add(zone);
            }
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
