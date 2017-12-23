package ch.ethz.matsim.act_gen.shoerl;

import java.util.Arrays;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import gurobi.GRBQuadExpr;
import gurobi.GRBVar;

public class DuplicationProblemSolver {
	public double[] solve(double[] reference, double[][] transitions, boolean verbose, double timeout, int solvers) throws GRBException {
		GRBEnv environment = new GRBEnv();
		GRBModel model = new GRBModel(environment);

		GRBQuadExpr objective = new GRBQuadExpr();

		GRBVar[] variables = new GRBVar[transitions.length];
		for (int u = 0; u < transitions.length; u++) {
			variables[u] = model.addVar(1.0, Double.POSITIVE_INFINITY, 1.0, GRB.INTEGER, "w" + u);
		}

		double J0 = 0.0;

		for (int z = 0; z < reference.length; z++) {
			J0 += Math.pow(reference[z], 2.0);
		}

		objective.addConstant(J0);

		if (verbose) {
			System.out.println("J0: " + J0);
			System.out.println("p: ");
		}

		for (int u = 0; u < transitions.length; u++) {
			double pu = 0.0;

			for (int z = 0; z < reference.length; z++) {
				pu += reference[z] * transitions[u][z];
			}

			pu *= -2.0;
			objective.addTerm(pu, variables[u]);

			if (verbose) {
				System.out.print(pu + " ");
			}
		}

		if (verbose) {
			System.out.println("");
			System.out.println("Q: ");
		}

		for (int u = 0; u < transitions.length; u++) {
			for (int v = 0; v < transitions.length; v++) {
				double quv = 0.0;

				for (int z = 0; z < reference.length; z++) {
					quv += transitions[u][z] * transitions[v][z];
				}

				if (verbose) {
					System.out.print(quv + " ");
				}

				objective.addTerm(quv, variables[u], variables[v]);
			}

			if (verbose) {
				System.out.println("");
			}
		}

		model.setObjective(objective, GRB.MINIMIZE);
		model.write("duplication.mps");
		
		model.set(GRB.IntParam.ConcurrentMIP, solvers);
		model.set(GRB.DoubleParam.TimeLimit, timeout);
				
		model.optimize();

		double weights[] = new double[transitions.length];

		for (int u = 0; u < transitions.length; u++) {
			weights[u] = variables[u].get(GRB.DoubleAttr.X);
		}

		if (verbose) {
			System.out.println("weights: " + Arrays.toString(weights));
		}

		return weights;
	}
}
