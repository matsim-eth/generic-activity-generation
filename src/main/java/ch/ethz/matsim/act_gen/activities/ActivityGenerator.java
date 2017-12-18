package ch.ethz.matsim.act_gen.activities;

import org.matsim.api.core.v01.population.Person;

public interface ActivityGenerator {
    public void generate(Person person, int zone);
}
