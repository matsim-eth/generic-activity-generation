package ch.ethz.matsim.act_gen.activities;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;

import java.util.List;

public interface ActivityChain {
    public List<Activity> getActivityChain();
}
