package ch.ethz.matsim.act_gen.shoerl.testing;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBExpr;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBQuadExpr;
import gurobi.GRBVar;

public class RunGurobiExample {
	static public void main(String[] args) throws GRBException {
		GRBEnv environment = new GRBEnv();
		GRBModel model = new GRBModel(environment);

		GRBVar x = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x");
		GRBVar y = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "y");
		GRBVar z = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "z");

		GRBLinExpr expr = new GRBLinExpr();
		expr.addTerm(1.0, x);
		expr.addTerm(1.0, y);
		expr.addTerm(2.0, z);
		model.setObjective(expr, GRB.MAXIMIZE);

		expr = new GRBLinExpr();
		expr.addTerm(1.0, x);
		expr.addTerm(2.0, y);
		expr.addTerm(3.0, z);
		model.addConstr(expr, GRB.LESS_EQUAL, 4.0, "c0");

		expr = new GRBLinExpr();
		expr.addTerm(1.0, x);
		expr.addTerm(1.0, y);
		model.addConstr(expr, GRB.GREATER_EQUAL, 1.0, "c1");

		model.optimize();

		System.out.println(x.get(GRB.StringAttr.VarName) + " " + x.get(GRB.DoubleAttr.X));
		System.out.println(y.get(GRB.StringAttr.VarName) + " " + y.get(GRB.DoubleAttr.X));
		System.out.println(z.get(GRB.StringAttr.VarName) + " " + z.get(GRB.DoubleAttr.X));
		System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));

		model.dispose();
		environment.dispose();
	}
}
