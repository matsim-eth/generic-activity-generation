package ch.ethz.matsim.act_gen.shoerl.testing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import ch.ethz.matsim.act_gen.shoerl.DuplicationProblemSolver;
import gurobi.GRBException;

public class RunToyExample {	
	static public void main(String[] args) throws GRBException {
		int numberOfZones = 3;
		
		List<List<Integer>> traces = new LinkedList<>();
		traces.add(Arrays.asList(0, 1, 2));
		traces.add(Arrays.asList(0, 1, 0, 1));
		traces.add(Arrays.asList(0, 2, 1, 2, 0));
		traces.add(Arrays.asList(0, 0, 0));
		traces.add(Arrays.asList(2, 1, 2, 1));
		traces.add(Arrays.asList(1, 1, 2, 1));
		traces.add(Arrays.asList(1, 0, 1, 2, 1));
		
		double[][] transitions = new double[traces.size()][numberOfZones];
		double[] counts = new double[numberOfZones];
		
		for (int i = 0; i < traces.size(); i++) {
			List<Integer> trace = traces.get(i);
			
			for (int zone : trace.subList(0, trace.size() - 1)) {
				transitions[i][zone]++;
				counts[zone]++;
			}
			
			System.out.println("Transitions " + i + " : " + Arrays.toString(transitions[i]));
		}
		
		System.out.println("Initial counts: " + Arrays.toString(counts));
		
		double[] reference = new double[numberOfZones];
		for (int z = 0; z < numberOfZones; z++) {
			reference[z] = counts[z];
		}
		
		reference[0] += 2;
		reference[1] += 3;
		reference[2] += 1;
		
		System.out.println("Reference counts: " + Arrays.toString(reference));
		
		DuplicationProblemSolver solver = new DuplicationProblemSolver();
		double[] weights = solver.solve(reference, transitions, true, 120.0, 4);
		
		double[] newCounts = new double[numberOfZones];
		
		for (int i = 0; i < traces.size(); i++) {
			List<Integer> trace = traces.get(i);
			
			for (int d = 0; d < weights[i]; d++) { 
				for (int zone : trace.subList(0, trace.size() - 1)) {
					newCounts[zone]++;
				}
			}
		}
		
		System.out.println("Original counts: " + Arrays.toString(counts));
		System.out.println("New counts: " + Arrays.toString(newCounts));
		System.out.println("(Reference: " + Arrays.toString(reference) + ")");
	}
}
