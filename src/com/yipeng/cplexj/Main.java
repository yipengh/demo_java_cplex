package com.yipeng.cplexj;

import ilog.concert.*;
import ilog.cplex.*;

/**
 * Created by huangyip on 07/10/2015.
 */
public class Main {

    public static void main(String[] args) {
        Graph graph = new Graph();
        graph.read("res/dat/testcase1.dat");
        solveODS(graph);
    }

    public static void readTestGraph() {
        Graph graph = new Graph();
        graph.read("res/dat/testcase1.dat");

        Utils.console("G(" + graph.getNbVertices() + ", " + graph.getNbArcs() + ", " + graph.getNbRequests() + ", " +
                graph.getNbBlockages() + ")");

        Utils.console("Vertices:");
        Utils.console(graph.getVertices());
        Utils.console("Arcs:");
        Utils.console(graph.getArcs());
        Utils.console("Requests:");
        Utils.console(graph.getRequests());
        Utils.console("Blockages:");
        Utils.console(graph.getBlockages());
    }

    public static void solveODS(Graph graph) {
        int n = graph.getNbVertices();
        int m = graph.getNbArcs();
        try {
            IloCplex cplex = new IloCplex();

            // Decision variables
            IloNumVar[][][][] f = new IloNumVar[n][n][n][n];
            IloNumVar[][][][] y = new IloNumVar[n][n][n][n];
            IloNumVar[][] x = new IloNumVar[n][n];
            // Instantiate variables
            for (int o = 0; o < n; o++) {
                for (int d = 0; d < n; d++) {
                    if (o == d) continue;

                    for (int i = 0; i < n; i++) {
                        f[o][d][i] = cplex.numVarArray(n, -1, 1);
                        y[o][d][i] = cplex.numVarArray(n, 0, 1);
                        x[i] = cplex.numVarArray(n, 0, 1);
                    }
                }
            }

            // Objective
            IloLinearNumExpr expr = cplex.linearNumExpr();
            for (int i = 0; i < m; i++) {
                Arc arc = graph.getArcs().get(i);

                for (int o = 0; o < n; o++) {
                    for (int d = 0; d < n; d++) {
                        if (o == d) continue;

                        expr.addTerm(arc.getCost(), y[o][d][arc.getSource()][arc.getTarget()]);
                    }
                }
            }
            cplex.addMinimize(expr);

            cplex.end();
        } catch(IloException e) {
            System.err.println("Concert exception caught: " + e);
        }
    }

    public static void simpleLP() {
        try {
            IloCplex cplex = new IloCplex();

            // Decision variables
            IloNumVar[] x = cplex.numVarArray(2, 0.0, Double.MAX_VALUE);

            // Objective function expression
            IloLinearNumExpr expr = cplex.linearNumExpr();
            expr.addTerm(1, x[0]);
            expr.addTerm(0.64, x[1]);
            cplex.addMaximize(expr);

            // Constraints
            cplex.addLe(cplex.sum(cplex.prod(50, x[0]), cplex.prod(31, x[1])), 250);
            cplex.addGe(cplex.sum(cplex.prod(3, x[0]), cplex.prod(-2, x[1])), -4);

            // Solve
            cplex.solve();

            System.out.println("Status: " + cplex.getStatus());
            System.out.println("z = " + cplex.getObjValue());
        } catch (IloException e) {
            System.err.println("Concert exception caught: " + e);
        }
    }

}
