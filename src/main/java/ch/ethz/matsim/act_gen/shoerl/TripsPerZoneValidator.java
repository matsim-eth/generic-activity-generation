package ch.ethz.matsim.act_gen.shoerl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.router.StageActivityTypes;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.router.TripStructureUtils.Trip;

import ch.ethz.matsim.act_gen.travelzones.TravelZones;

public class TripsPerZoneValidator {
	final private StageActivityTypes stageActivityTypes;
	final private TravelZones travelZones;

	public TripsPerZoneValidator(TravelZones travelZones, StageActivityTypes stageActivityTypes) {
		this.travelZones = travelZones;
		this.stageActivityTypes = stageActivityTypes;
	}

	public List<Integer> getCounts(Population population) {
		List<Integer> counts = new LinkedList<>(Collections.nCopies(travelZones.getNumberofZones(), 0));

		for (Person person : population.getPersons().values()) {
			List<TripStructureUtils.Trip> trips = TripStructureUtils.getTrips(person.getSelectedPlan(),
					stageActivityTypes);

			if (trips.size() > 0) {
				for (Trip trip : trips.subList(0, trips.size() - 1)) {
					Coord originCoord = trip.getOriginActivity().getCoord();
					int zoneIndex = travelZones.getZone(originCoord.getX(), originCoord.getY());
					counts.set(zoneIndex, counts.get(zoneIndex) + 1);
				}
			}
		}

		return counts;
	}

	public List<Integer> getAbsoluteDifferences(Population population, List<Integer> reference) {
		List<Integer> differences = new LinkedList<>();
		List<Integer> counts = getCounts(population);

		for (int i = 0; i < travelZones.getNumberofZones(); i++) {
			differences.add(counts.get(i) - reference.get(i));
		}

		return differences;
	}

	public List<Double> getRelativeDifferences(Population population, List<Integer> reference) {
		List<Integer> absolute = getAbsoluteDifferences(population, reference);
		List<Double> relative = new LinkedList<>();

		for (int i = 0; i < travelZones.getNumberofZones(); i++) {
			relative.add((double) absolute.get(i) / (double) reference.get(i));
		}

		return relative;
	}

	public boolean validate(Population population, List<Integer> reference) {
		return getAbsoluteDifferences(population, reference).stream().filter(c -> c != 0).count() == 0;
	}
}
