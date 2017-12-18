package ch.ethz.matsim.act_gen.travelzones;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.facilities.ActivityFacility;

public class TravelZonesSimpleImpl implements TravelZones{
    private int nx;
    private int ny;
    private double max_x = Double.MIN_VALUE;
    private double max_y = Double.MIN_VALUE;
    private double min_x = Double.MAX_VALUE;
    private double min_y = Double.MAX_VALUE;

    public TravelZonesSimpleImpl(Scenario scenario, int nx, int ny) {
        this.nx = nx;
        this.ny = ny;

        for ( Id<ActivityFacility> facilityId : scenario.getActivityFacilities().getFacilities().keySet() ) {
            double x = scenario.getActivityFacilities().getFacilities().get(facilityId).getCoord().getX();
            double y = scenario.getActivityFacilities().getFacilities().get(facilityId).getCoord().getY();

            if ( x > this.max_x) {
                this.max_x = x;
            }
            if ( y > this.max_y) {
                this.max_y = y;
            }
            if ( x < this.min_x) {
                this.min_x = x;
            }
            if ( y > this.min_y) {
                this.min_y = y;
            }
        }
    }

    @Override
    public int getZone(double x, double y) {
        int i = (int)Math.floor( (x - min_x) / (max_x - min_x) * nx) - 1;
        int j = (int)Math.floor( (y - min_y) / (max_y - min_y) * ny) - 1;
        return j* nx + i;
    }

    @Override
    public int getNumberofZones() {
        return this.nx*this.ny;
    }
}
