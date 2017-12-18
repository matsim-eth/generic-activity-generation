package ch.ethz.matsim.act_gen.trips;

public interface DemandDistributor {
    public void distribute(int[] inducedTripsPerZone);
}
