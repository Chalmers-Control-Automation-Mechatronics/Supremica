/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.properties;


public final class Config
{
	// Boolean:
	//
	// BooleanProperty(PropertyType type, String key, boolean value, String comment)
	// BooleanProperty(PropertyType type, String key, boolean value, String comment, boolean immutable)
    //
    // Integer:
    //
    // IntegerProperty(PropertyType type, String key, int value, String comment)
	// IntegerProperty(PropertyType type, String key, int value, String comment, boolean immutable)
	// IntegerProperty(PropertyType type, String key, int value, String comment, boolean immutable, int min)
	// IntegerProperty(PropertyType type, String key, int value, String comment, boolean immutable, int min, int max)
	//
	// String:
	//
	// StringProperty(PropertyType type, String key, String value, String comment)
	// StringProperty(PropertyType type, String key, String value, String comment, boolean immutable)
	// StringProperty(PropertyType type, String key, String value, String comment, boolean immutable, String[] legalValues)
	// StringProperty(PropertyType type, String key, String value, String comment, boolean immutable, String[] legalValues, boolean ignoreCase)


	// Valid PropertyTypes:
	//   GENERAL
	//   GENERAL_LOG
	//   GENERAL_FILE
	//   GENERAL_COMM
	//   GENERAL_COMM_XMLRPC
	//   GENERAL_COMM_DOCDB
	//   GUI
	//   GUI_EDITOR
	//   GUI_ANALYZER
	//   GUI_DOT
	//   ALGORITHMS
	//   ALGORITHMS_SYNC
	//   ALGORITHMS_VERIFiCATION
	//   ALGORITHMS_SYNTHESIS
	//   ALGORITHMS_MINIMIZATION
	//   ALGORITHMS_BDD
	//   MISC

	// GENERAL_COMM_DOCDB
	public static final StringProperty DOC_DB_SERVER_NAME = new StringProperty(PropertyType.GENERAL_COMM_DOCDB, "docdbHost", "localhost", "Doc DB server name");
	public static final IntegerProperty DOC_DB_SERVER_PORT = new IntegerProperty(PropertyType.GENERAL_COMM_DOCDB, "docdbPort", 9111, "Doc DB server port");
	public static final StringProperty DOC_DB_SERVER_USER = new StringProperty(PropertyType.GENERAL_COMM_DOCDB, "docdbUser", "", "Doc DB server user");
	public static final StringProperty DOC_DB_SERVER_DOC = new StringProperty(PropertyType.GENERAL_COMM_DOCDB, "docdbDoc", "work", "Doc DB server doc");

	// GENERAL_COMM_XMLRPC
	public static final BooleanProperty XML_RPC_ACTIVE = new BooleanProperty(PropertyType.GENERAL_COMM_XMLRPC, "xmlRpcActive", false, "XML-RPC Active");
	public static final IntegerProperty XML_RPC_PORT = new IntegerProperty(PropertyType.GENERAL_COMM_XMLRPC, "xmlRpcPort", 9112, "XML-RPC Active", false, 0);
	public static final StringProperty XML_RPC_FILTER = new StringProperty(PropertyType.GENERAL_COMM_XMLRPC, "xmlRpcFilter", "127.0.0.1", "XML-RPC Filter");
	public static final BooleanProperty XML_RPC_DEBUG = new BooleanProperty(PropertyType.GENERAL_COMM_XMLRPC, "xmlRpcDebug", false, "XML-RPC Debug");

	// GUI_DOT
	public static final BooleanProperty DOT_USE = new BooleanProperty(PropertyType.GUI_DOT, "dotUse", true, "Use Dot");
	public static final StringProperty DOT_EXECUTE_COMMAND = new StringProperty(PropertyType.GUI_DOT, "dotExecuteCommand", "dot", "Dot command");
	public static final IntegerProperty DOT_MAX_NBR_OF_STATES = new IntegerProperty(PropertyType.GUI_DOT, "dotMaxNbrOfStatesWithoutWarning", 100, "Max number of states without warning", false, 0);
	public static final BooleanProperty DOT_LEFT_TO_RIGHT = new BooleanProperty(PropertyType.GUI_DOT, "dotLeftToRight", false, "Layout from left to right, otherwise from top to bottom");
	public static final BooleanProperty DOT_WITH_STATE_LABELS = new BooleanProperty(PropertyType.GUI_DOT, "dotWithStateLabels", true, "Draw state names");
	public static final BooleanProperty DOT_WITH_EVENT_LABELS = new BooleanProperty(PropertyType.GUI_DOT, "dotWithEventLabels", true, "Draw event labels");
	public static final BooleanProperty DOT_WITH_CIRCLES = new BooleanProperty(PropertyType.GUI_DOT, "dotWithCircles", false, "Draw circle around state names");
	public static final BooleanProperty DOT_USE_STATE_COLORS = new BooleanProperty(PropertyType.GUI_DOT, "dotUseStateColors", true, "Use colors for states");
	public static final BooleanProperty DOT_USE_ARC_COLORS = new BooleanProperty(PropertyType.GUI_DOT, "dotUseArcColors", false, "Use colors for arcs");
	public static final BooleanProperty DOT_USE_MULTI_LABELS = new BooleanProperty(PropertyType.GUI_DOT, "dotUseMultiLabels", true, "Draw multiple labels on one arc");
	public static final BooleanProperty DOT_AUTOMATIC_UPDATE = new BooleanProperty(PropertyType.GUI_DOT, "dotAutomaticUpdate", true, "Do automatic update of the layout");

	// GENERAL
	public static final StringProperty GENERAL_GENERAL_LOOKANDFEEL  = new StringProperty(PropertyType.GENERAL, "generalLookAndFeel", "System", "Look and feel");
	public static final StringProperty GENERAL_STATE_SEPARATOR  = new StringProperty(PropertyType.GENERAL, "generalStateSeparator", ".", "State separator character");
	public static final StringProperty GENERAL_STATELABEL_SEPARATOR  = new StringProperty(PropertyType.GENERAL, "generalStateLabelSeparator", ",", "State label separator character");
	public static final StringProperty GENERAL_SILENT_EVENT_NAME  = new StringProperty(PropertyType.GENERAL, "eneralSilentEventName", "tau", "Silent event name");
	public static final StringProperty GENERAL_SILENT_CONTROLLABLE_EVENT_NAME  = new StringProperty(PropertyType.GENERAL, "generalSilentControllableEventName", "tau_c", "Silent controllable event name");
	public static final StringProperty GENERAL_SILENT_UNCONTROLLABLE_EVENT_NAME  = new StringProperty(PropertyType.GENERAL, "generalSilentUnontrollableEventName", "tau_u", "Silent uncontrollable event name");
	public static final BooleanProperty GENERAL_USE_SECURITY = new BooleanProperty(PropertyType.GENERAL, "generalUseSecurity", false, "Use file security");
	public static final BooleanProperty GENERAL_STUDENT_VERSION = new BooleanProperty(PropertyType.GENERAL, "generalStudentVersion", false, "Student version");
	public static final BooleanProperty GENERAL_REDIRECT_STDOUT = new BooleanProperty(PropertyType.GENERAL, "generalRedirectStdout", true, "Redirect stdout");
	public static final BooleanProperty GENERAL_REDIRECT_STDERR = new BooleanProperty(PropertyType.GENERAL, "generalRedirectStderr", true, "Redirect stderr");



	private static final String INCLUDE_EDITOR = "includeEditor";
	private static final String INCLUDE_CELLEDITOR = "includeCellEditor";
	private static final String INCLUDE_BOUNDED_UNCON_TOOLS = "includeBoundedUnconTools";
	private static final String INCLUDE_EXPERIMENTAL_ALGORITHMS = "includeExperimentalAlgorithms";
	private static final String VERBOSE_MODE = "verboseMode";
	private static final String SUPERUSER_IDENTITY = "superuserIdentity";
	private static final String ALLOW_SUPERUSER_LOGIN = "allowSuperUserLogin";


	// GENERAL_LOG
	public static final BooleanProperty LOG_TO_CONSOLE = new BooleanProperty(PropertyType.GENERAL_LOG, "logToConsole", false, "Log to Console");
	public static final BooleanProperty LOG_TO_GUI = new BooleanProperty(PropertyType.GENERAL_LOG, "logToGUI", false, "Log to Graphical User Interface");

	// GENERAL_FILE
	public static final StringProperty FILE_OPEN_PATH = new StringProperty(PropertyType.GENERAL_FILE, "fileOpenPath", LocalSystem.getHomeDirectory(), "Default file open path");
	public static final StringProperty FILE_SAVE_PATH = new StringProperty(PropertyType.GENERAL_FILE, "fileSavePath", LocalSystem.getHomeDirectory(), "Default file save path");
	public static final BooleanProperty FILE_ALLOW_OPEN = new BooleanProperty(PropertyType.GENERAL_FILE, "fileAllowOpen", true, "Allow user to open file");
	public static final BooleanProperty FILE_ALLOW_SAVE = new BooleanProperty(PropertyType.GENERAL_FILE, "fileAllowSave", true, "Allow user to save file");
	public static final BooleanProperty FILE_ALLOW_IMPORT = new BooleanProperty(PropertyType.GENERAL_FILE, "fileAllowImport", true, "Allow user to import file");
	public static final BooleanProperty FILE_ALLOW_EXPORT = new BooleanProperty(PropertyType.GENERAL_FILE, "fileAllowExport", true, "Allow user to export file");
	public static final BooleanProperty FILE_ALLOW_QUIT = new BooleanProperty(PropertyType.GENERAL_FILE, "fileAllowQuit", true, "Allow user to quit Supremica");


/*


	// Synchronization Options
	private static final String SYNC_FORBID_UNCON_STATES = "syncForbidUncontrollableStates";
	private static final String SYNC_EXPAND_FORBIDDEN_STATES = "syncExpandUncontrollableStates";
	private static final String SYNC_INITIAL_HASHTABLE_SIZE = "syncInitialHashtableSize";
	private static final String SYNC_EXPAND_HASHTABLE = "syncExpandHashtable";
	private static final String SYNC_NBR_OF_EXECUTERS = "synchNbrOfExecuters";
	private static final String SYNC_AUTOMATON_NAME_SEPARATOR = "synchAutomatonNameSeparator";

	// Verification Options
	private static final String VERIFY_VERIFICATION_TYPE = "verifyVerificationType";
	private static final String VERIFY_ALGORITHM_TYPE = "verifyAlgorithmType";
	private static final String VERIFY_EXCLUSION_STATE_LIMIT = "verifyExclusionStateLimit";
	private static final String VERIFY_REACHABILITY_STATE_LIMIT = "verifyReachabilityStateLimit";
	private static final String VERIFY_ONE_EVENT_AT_A_TIME = "verifyOneEventAtATime";
	private static final String VERIFY_SKIP_UNCONTROLLABILITY_CHECK = "skipUncontrollabilityCheck";
	private static final String VERIFY_NBR_OF_ATTEMPTS = "nbrOfAttempts";
	private static final String VERIFY_SHOW_BAD_TRACE = "showBadTrace";

	// Synthesizer Options
	private static final String SYNTHESIS_SYNTHESIS_TYPE = "synthesisSynthesisType";
	private static final String SYNTHESIS_ALGORITHM_TYPE = "synthesisAlgorithmType";
	private static final String SYNTHESIS_PURGE = "synthesisPurge";
	private static final String SYNTHESIS_OPTIMIZE = "synthesisOptimize";
	private static final String SYNTHESIS_MAXIMALLY_PERMISSIVE = "synthesisMaximallyPermissive";
	private static final String SYNTHESIS_MAXIMALLY_PERMISSIVE_INCREMENTAL = "synthesisMaximallyPermissiveIncremental";
	private static final String SYNTHESIS_REDUCE_SUPERVISORS = "synthesisReduceSupervisors";

	// Minimization Options
	private static final String MINIMIZATION_EQUIVALENCE_RELATION = "minimizationEquivalenceRelation";
	private static final String MINIMIZATION_ALSO_MINIMIZE_TRANSITIONS = "minimizationAlsoMinimizeTransitions";
	private static final String MINIMIZATION_KEEP_ORIGINAL = "minimizationKeepOriginal";
	private static final String MINIMIZATION_IGNORE_MARKING = "minimizationIgnoreMarking";
	private static final String MINIMIZATION_STRATEGY = "minimizationStrategy";
	private static final String MINIMIZATION_HEURISTIC = "minimizationHeuristic";

	// BDD Options. note that these mirror the stuff in org.supremica.util.BDD.Options
	private static final String BDD_SHOW_GROW = "bddShowGrowth";
	private static final String BDD_SIZE_WATCH = "bddSizeWatch";
	private static final String BDD_ALTER_PCG = "bddAlterPCG";
	private static final String BDD_TRACE_ON = "bddTraceOn";
	private static final String BDD_DEBUG_ON = "bddDebugOn";
	private static final String BDD_PROFILE_ON = "bddProfileOn";
	private static final String BDD_ALGORITHM = "bddAlgorithm";
	private static final String BDD_COUNT_ALGO = "bddCountAlgorithm";
	private static final String BDD_LOCAL_SATURATION = "bddLocalSaturation";
	private static final String BDD_UC_OPTIMISTIC = "bddUCOptimistic";
	private static final String BDD_NB_OPTIMISTIC = "bddNBOptimistic";
	private static final String BDD_LIB_PATH = "bddLibPath";
	private static final String BDD_LI_ALGO = "bddLanguageInclusionAlgorithm";    // inclsuion_algorithm
	private static final String BDD_ORDER_ALGO = "bddAutomataOrderingAlgorithm";    // ordering_algorithm
	private static final String BDD_ORDERING_FORCE_COST = "bddOrderingForceCost";    // ordering_force_cost
	private static final String BDD_AS_HEURISTIC = "bddAutomataSelectionHeuristics";    // as_heuristics
	private static final String BDD_FRONTIER_TYPE = "bddFrontierType";    // frontier_strategy
	private static final String BDD_H1 = "bddH1";    // es_heuristics
	private static final String BDD_H2 = "bddH2";    // ndas_heuristics
	private static final String BDD_DSSI_HEURISTIC = "bddDelayedStarSelection";    // dssi_heuristics
	private static final String BDD_PARTITION_MAX = "bddMaxPartitionSize";    // max_partition_size
	private static final String BDD_ENCODING_ALGO = "bddStateEncodingAlgorithm";    // encoding_algorithm
	private static final String BDD_SUP_REACHABILITY = "bddSupReachability";    // sup_reachability_type
	private static final String BDD_DISJ_OPTIMIZER_ALGO = "bddDisjOptimizerAlgo"; // disj_optimizer_algo
	private static final String BDD_TRANSITION_OPTIMIZER_ALGO = "bddTransitionOptimizerAlgo"; // transition_optimizer_algo
	private static final String BDD_INTERLEAVED_VARIABLES = "bddInterleavedVariables"; // interleaved_variables
	private static final String BDD_LEVEL_GRAPHS = "bddLevelGraphs"; // show_level_graph
*/

	static Config instance = new Config();

	private Config()
	{ // This class should only be instantiated to guarantee that it is loaded
	}

}
