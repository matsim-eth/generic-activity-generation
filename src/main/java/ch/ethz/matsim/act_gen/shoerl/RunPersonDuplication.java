package ch.ethz.matsim.act_gen.shoerl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.router.StageActivityTypes;
import org.matsim.core.router.StageActivityTypesImpl;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.PtConstants;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.utils.objectattributes.ObjectAttributesXmlWriter;

import ch.ethz.matsim.act_gen.travelzones.TravelZones;
import ch.ethz.matsim.act_gen.travelzones.TravelZonesSimpleImpl;
import gurobi.GRBException;

public class RunPersonDuplication {
	/**
	 * Zone-based upsampling of trips via plan duplication.
	 * 
	 * The idea is the following: There are Z zones, each of them has a count of
	 * outgoing trips c_z. However, what we want is to set a certain reference count
	 * r_z >= c_z for each zone, because we want to scale up the travel demand.
	 * While it would be better to have a more elaborate approach, here we simply
	 * try to achieve this goal by copying persons/plans.
	 * 
	 * First, we count the number of outgoing trips for each person and zone. So if a
	 * person leaves a certain zone 3 times, we have n_iz = 3, where i is the index
	 * over the persons and z is the index over the zones.
	 * 
	 * Now, in order to achieve the desired trip counts, we have to make sure that
	 * 
	 * r_z = Sum_i ( n_iz * w_i )
	 * 
	 * Here, w_i is the weight of each agent. Clearly, if all w_i = 1, the
	 * right-hand side reproduces the trip counts of each zone in the baseline case.
	 * By increasing w_i we can now try to reach the desired trips counts on the
	 * left-hand side.
	 * 
	 * We note that w_i > 1 (we do not scale down) and w_i is integer. Finally, to
	 * make the problem tractable, we convert it into a QP:
	 * 
	 * min Sum_z ( r_z - Sum_i( n_iz * w_i ) )^2 s.t. w_i > 1, w_i integer
	 * 
	 * In order to feed this problem to Gurobi (which is used here, maybe GLPK could
	 * be used as an open alternative), it needs to be transformed into a standard
	 * QP of the form J(w) = x' * Q * x + p' * x + J_0
	 * 
	 * This is just an analytical task, at the end one comes up with the following
	 * formulas:
	 * 
	 * J_0 = Sum_z ( r_z^2 ) 
	 * p_i = -2 * Sum_z ( r_z * n_iz )
	 * Q_ij = Sum_z ( n_iz * n_jz )
	 * 
	 * With these information at hand, it is easy to set up a QP in any solver, that
	 * is able to solve an constrained integer QP. Of course, there may not always
	 * be a solution for the initial problem, hence the QP formulation. Even if
	 * there is no exact solution, the solver will find one that is close to the
	 * desired counts.
	 * 
	 * @throws GRBException
	 */
	static public void main(String[] args) throws GRBException {
		String configPath = args[0];

		Config config = ConfigUtils.loadConfig(configPath);
		Scenario scenario = ScenarioUtils.loadScenario(config);

		TravelZones zones = new TravelZonesSimpleImpl(scenario, 20, 20, "home");
		int numberOfZones = zones.getNumberofZones();

		StageActivityTypes stageActivityTypes = new StageActivityTypesImpl(PtConstants.TRANSIT_ACTIVITY_TYPE);

		// Make sure that this is not random access
		List<Person> persons = new LinkedList<>(scenario.getPopulation().getPersons().values());

		// Filter out peoplefor sampling (only those who have at least two activities)
		List<Person> samplingPersons = persons.stream()
				.filter(p -> TripStructureUtils.getTrips(p.getSelectedPlan(), stageActivityTypes).size() > 0)
				.collect(Collectors.toList());

		// Randomize and reduce sampling persons to reduce problem size
		Collections.shuffle(samplingPersons);
		samplingPersons = samplingPersons.subList(0, 1000);

		// Now find:
		// - counts: The trip counts for each zone
		// - samplingCounts: The trip counts for each zone, only for the sampling agents
		// - transitions: For each person count the number of trips from each zone

		double[] counts = new double[numberOfZones];
		double[] samplingCounts = new double[numberOfZones];
		double[][] transitions = new double[samplingPersons.size()][numberOfZones];

		// Counts for all persons
		for (Person person : persons) {
			List<TripStructureUtils.Trip> trips = TripStructureUtils.getTrips(person.getSelectedPlan(),
					stageActivityTypes);

			if (trips.size() > 0) {
				for (TripStructureUtils.Trip trip : trips.subList(0, trips.size() - 1)) {
					Coord originCoord = trip.getOriginActivity().getCoord();
					int zone = zones.getZone(originCoord.getX(), originCoord.getY());
					counts[zone]++;
				}
			}
		}

		// Counts for sampling persons + transitions
		for (int i = 0; i < samplingPersons.size(); i++) {
			Person person = samplingPersons.get(i);

			List<TripStructureUtils.Trip> trips = TripStructureUtils.getTrips(person.getSelectedPlan(),
					stageActivityTypes);

			if (trips.size() > 0) {
				for (TripStructureUtils.Trip trip : trips.subList(0, trips.size() - 1)) {
					Coord originCoord = trip.getOriginActivity().getCoord();
					int zone = zones.getZone(originCoord.getX(), originCoord.getY());

					samplingCounts[zone]++;
					transitions[i][zone]++;
				}
			}
		}

		// Now compute new reference counts
		// Here, just scale them up by two per zone
		double[] reference = new double[counts.length];

		for (int z = 0; z < reference.length; z++) {
			reference[z] = Math.round(counts[z] * 2.0);
		}

		// Substract the "fixed" counts from the reference, because they are not changed
		// by the sampling agents
		double[] samplingReference = new double[counts.length];

		for (int z = 0; z < reference.length; z++) {
			samplingReference[z] = reference[z] - (counts[z] - samplingCounts[z]);
		}

		// Now compute the person weights
		double[] weights = new DuplicationProblemSolver().solve(samplingReference, transitions, false, 120.0, 4);

		TripsPerZoneValidator validator = new TripsPerZoneValidator(zones, stageActivityTypes);
		List<Integer> countsBefore = validator.getCounts(scenario.getPopulation());

		List<Person> duplicates = new LinkedList<>();

		Random random = new Random(0);

		for (int i = 0; i < samplingPersons.size(); i++) {
			Person template = samplingPersons.get(i);

			for (int j = 1; j < Math.ceil(weights[i]); j++) {
				Id<Person> duplcateId = Id.createPersonId(template.getId().toString() + "_duplicate" + j);

				Person duplicate = scenario.getPopulation().getFactory().createPerson(duplcateId);
				Plan plan = scenario.getPopulation().getFactory().createPlan();
				PopulationUtils.copyFromTo(template.getSelectedPlan(), plan);

				duplicate.addPlan(plan);
				duplicates.add(duplicate);

				copyAttributesFromTo(scenario.getPopulation().getPersonAttributes(), template, duplicate);

				double shift = random.nextDouble() * 1800.0 - 900.0;

				for (PlanElement element : plan.getPlanElements()) {
					if (element instanceof Activity) {
						Activity activity = (Activity) element;

						activity.setStartTime(activity.getStartTime() + shift);
						activity.setEndTime(activity.getEndTime() + shift);
					}
				}
			}
		}

		duplicates.forEach(p -> scenario.getPopulation().addPerson(p));
		List<Integer> countsAfter = validator.getCounts(scenario.getPopulation());

		System.out.println(String.format("%10s %10s %10s", "Before", "Reference", "After"));

		for (int z = 0; z < zones.getNumberofZones(); z++) {
			System.out.println(String.format("%10d %10d %10d", (int) countsBefore.get(z), (int) reference[z],
					(int) countsAfter.get(z)));
		}

		String populationOutputPath = config.plans().getInputFile() + ".scaled.xml.gz";
		String populationAttributesOutputPath = config.plans().getInputPersonAttributeFile() + ".scaled.xml.gz";

		new PopulationWriter(scenario.getPopulation()).write(populationOutputPath);
		new ObjectAttributesXmlWriter(scenario.getPopulation().getPersonAttributes())
				.writeFile(populationAttributesOutputPath);
	}

	/**
	 * Really useless method to copy attributes ... Should be possible to do that
	 * much easier. Especially we cannot generically know which attributes are
	 * available per person.
	 */
	static public void copyAttributesFromTo(ObjectAttributes attributes, Person from, Person to) {
		String[] activityTypes = new String[] { "home", "work", "education", "leisure", "shop", "escort_other",
				"escort_kids" };

		String[] names = new String[] { "earliestEndTime", "latestStartTime", "minimalDuration", "typicalDuration" };

		for (String activityType : activityTypes) {
			for (String name : names) {
				for (int i = 0; i < 40; i++) {
					String attributeName = String.format("%s_%s_%d", name, activityType, i);

					if (attributes.getAttribute(from.getId().toString(), attributeName) != null) {
						attributes.putAttribute(to.getId().toString(), attributeName,
								attributes.getAttribute(from.getId().toString(), attributeName));
					}
				}
			}
		}

		String[] otherNames = new String[] { "mz_id", "has_outside_activity", "season_ticket" };

		for (String otherName : otherNames) {
			if (attributes.getAttribute(from.getId().toString(), otherName) != null) {
				attributes.putAttribute(to.getId().toString(), otherName,
						attributes.getAttribute(from.getId().toString(), otherName));
			}
		}
	}
}
