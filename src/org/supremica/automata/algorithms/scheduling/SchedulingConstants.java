package org.supremica.automata.algorithms.scheduling;

public class SchedulingConstants
{
	public static final String MODIFIED_A_STAR = "Modified A*";
        public static final String MILP = "MILP";
	public static final String MILP_GLPK = "MILP_GLPK";
        public static final String MILP_CBC = "MILP_CBC";
        public static final String MILP_CPLEX = "MILP_CPLEX";
	public static final String VIS_GRAPH = "Visibility Graph";
	public static final String MULTITHREADED_A_STAR = "Multithreaded A*";

	public static final String ONE_PRODUCT_RELAXATION = "1-product relax";
	public static final String TWO_PRODUCT_RELAXATION = "2-product relax";
	public static final String VIS_GRAPH_TIME_RELAXATION = "visibility graph (time)";
	public static final String VIS_GRAPH_NODE_RELAXATION = "visibility graph (node)";
	public static final String BRUTE_FORCE_RELAXATION = "brute force";
	public static final String OPTIMAL = "optimal";
	public static final String SUBOPTIMAL = "suboptimal";
        
        public static final int MESSAGE_TYPE_INFO = 0;
        public static final int MESSAGE_TYPE_WARN = 1;
        public static final int MESSAGE_TYPE_ERROR = 2;
}