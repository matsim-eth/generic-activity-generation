package ch.ethz.matsim.act_gen.activities;

import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;

import java.util.ArrayList;
import java.util.List;

public class ActivityChainImplFromPlan implements ActivityChain {
    private List<Activity> activityList;

    public ActivityChainImplFromPlan(Plan plan) {
        activityList = new ArrayList<>();
        for (PlanElement planElement : plan.getPlanElements()) {
            if (planElement instanceof Activity) {
                activityList.add((Activity) planElement);
            }
        }
    }


    @Override
    public List<Activity> getActivityChain() {
        return activityList;
    }

}
