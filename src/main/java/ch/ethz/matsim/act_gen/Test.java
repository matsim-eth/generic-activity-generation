package ch.ethz.matsim.act_gen;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.io.File;


public class Test {
    private static final Logger log = Logger.getLogger(Test.class);

    public static void main(String[] args){
//        Config config = ConfigUtils.loadConfig(args[0]);
//        Scenario scenario = ScenarioUtils.loadScenario(config);
//        Population population = scenario.getPopulation();
//
//        for (Person person : population.getPersons().values()) {
//            log.info("Person " + person.getId().toString());
//            Plan plan = person.getSelectedPlan();
//            log.info("Activities : ");
//            for (PlanElement planElement : plan.getPlanElements()) {
//                if ( planElement instanceof Activity) {
//                    log.info(((Activity) planElement).toString());
//                }
//            }
//            log.info("---");
//        }

        File osmShapefile = new File("/home/ctchervenkov/Documents/projects/sccer/BFS_shapes_2015/");


    }
}
