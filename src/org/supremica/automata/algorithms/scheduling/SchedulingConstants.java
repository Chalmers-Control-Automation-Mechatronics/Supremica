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
        
        /** A big enough value used by the MILP-solver (should be greater than any time-variable). */
        public static final int BIG_M_VALUE = 1000;
    
        /**
         *  The safety buffer between unbooking and booking, used in MILP. To use the
         *  automatic deduction of epsilon from the optmal time values in
         *  {@link buildScheduleAutomaton}, it should be a power of 10. For correct
         *  functioning, this variable should be strictly smaller than 10^(-x), where
         *  x is the total number of (individual) plalnt states.
         */
        public static final double EPSILON = 0.001;
}