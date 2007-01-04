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

import org.supremica.automata.algorithms.EquivalenceRelation;
import org.supremica.automata.algorithms.SynthesisAlgorithm;
import org.supremica.automata.algorithms.SynthesisType;
import org.supremica.automata.algorithms.VerificationAlgorithm;
import org.supremica.automata.algorithms.VerificationType;
import org.supremica.automata.algorithms.minimization.MinimizationHeuristic;
import org.supremica.automata.algorithms.minimization.MinimizationStrategy;
import org.supremica.util.BDD.Options;

/**
 * Configurable options. All of these are automatically added to a GUI for editing.
 * The current configuration is saved in the file SupremicaProperties.cfg which per default
 * is loaded on startup.
 */
public final class Config
{
    // Valid PropertyTypes:
    //   GENERAL
    //   GENERAL_LOG
    //   GENERAL_FILE
    //   GENERAL_COMM
    //   GENERAL_COMM_XMLRPC
    //   GENERAL_SOFTPLC
    //   GUI
    //   GUI_EDITOR
    //   GUI_ANALYZER
    //   GUI_SIMULATOR
    //   GUI_DOT
    //   ALGORITHMS
    //   ALGORITHMS_SYNC
    //   ALGORITHMS_VERIFICATION
    //   ALGORITHMS_SYNTHESIS
    //   ALGORITHMS_MINIMIZATION
    //   ALGORITHMS_BDD
    //   ALGORITHMS_HMI
    //   MISC

    // GENERAL_COMM_XMLRPC
    public static final BooleanProperty XML_RPC_ACTIVE = new BooleanProperty(PropertyType.GENERAL_COMM_XMLRPC, "xmlRpcActive", false, "XML-RPC Active");
    public static final IntegerProperty XML_RPC_PORT = new IntegerProperty(PropertyType.GENERAL_COMM_XMLRPC, "xmlRpcPort", 9112, "XML-RPC Port", false, 0);
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
    /**
     * Possible values for look and feel
     * System: All platforms
     * Metal: All platforms   (javax.swing.plaf.metal.MetalLookAndFeel)
     * Motif: All platforms   (com.sun.java.swing.plaf.motif.MotifLookAndFeel)
     * com.sun.java.swing.plaf.windows.WindowsLookAndFeel : Windows only
     * javax.swing.plaf.mac.MacLookAndFeel : Mac only
     */
    private static final String[] LOOKANDFEEL_LEGALVALUES= {"System", "Metal", "Motif", 
    "Windows", "Mac", "GTK+"};
    public static final StringProperty GENERAL_LOOKANDFEEL  = new StringProperty(PropertyType.GENERAL, "generalLookAndFeel", "System", "Look and feel (requires restart)", LOOKANDFEEL_LEGALVALUES);
    public static final StringProperty GENERAL_STATE_SEPARATOR  = new StringProperty(PropertyType.GENERAL, "generalStateSeparator", ".", "State separator character");
    public static final StringProperty GENERAL_STATELABEL_SEPARATOR  = new StringProperty(PropertyType.GENERAL, "generalStateLabelSeparator", ",", "State label separator character");
    public static final BooleanProperty GENERAL_USE_SECURITY = new BooleanProperty(PropertyType.GENERAL, "generalUseSecurity", false, "Use file security");
    public static final BooleanProperty GENERAL_STUDENT_VERSION = new BooleanProperty(PropertyType.GENERAL, "generalStudentVersion", false, "Student version (requires restart)");
    public static final BooleanProperty INCLUDE_EXPERIMENTAL_ALGORITHMS = new BooleanProperty(PropertyType.GENERAL, "includeExperimentalAlgorithms", false, "Include experimental algorithms");

    // GENERAL_LOG
    public static final BooleanProperty LOG_TO_CONSOLE = new BooleanProperty(PropertyType.GENERAL_LOG, "logToConsole", false, "Log to Console");
    public static final BooleanProperty LOG_TO_GUI = new BooleanProperty(PropertyType.GENERAL_LOG, "logToGUI", false, "Log to Graphical User Interface");
    public static final BooleanProperty GENERAL_REDIRECT_STDOUT = new BooleanProperty(PropertyType.GENERAL_LOG, "generalRedirectStdout", true, "Redirect stdout");
    public static final BooleanProperty GENERAL_REDIRECT_STDERR = new BooleanProperty(PropertyType.GENERAL_LOG, "generalRedirectStderr", true, "Redirect stderr");
    public static final BooleanProperty VERBOSE_MODE = new BooleanProperty(PropertyType.GENERAL_LOG, "verboseMode", false, "Verbose mode");

    // GENERAL_FILE
    public static final StringProperty FILE_OPEN_PATH = new StringProperty(PropertyType.GENERAL_FILE, "fileOpenPath", LocalSystem.getHomeDirectory(), "Default file open path");
    public static final StringProperty FILE_SAVE_PATH = new StringProperty(PropertyType.GENERAL_FILE, "fileSavePath", LocalSystem.getHomeDirectory(), "Default file save path");
    public static final BooleanProperty FILE_ALLOW_OPEN = new BooleanProperty(PropertyType.GENERAL_FILE, "fileAllowOpen", true, "Allow user to open file");
    public static final BooleanProperty FILE_ALLOW_SAVE = new BooleanProperty(PropertyType.GENERAL_FILE, "fileAllowSave", true, "Allow user to save file");
    public static final BooleanProperty FILE_ALLOW_IMPORT = new BooleanProperty(PropertyType.GENERAL_FILE, "fileAllowImport", true, "Allow user to import file");
    public static final BooleanProperty FILE_ALLOW_EXPORT = new BooleanProperty(PropertyType.GENERAL_FILE, "fileAllowExport", true, "Allow user to export file");
    public static final BooleanProperty FILE_ALLOW_QUIT = new BooleanProperty(PropertyType.GENERAL_FILE, "fileAllowQuit", true, "Allow user to quit Supremica");

    // GENERAL_SOFTPLC
    public static final BooleanProperty INCLUDE_SOFTPLC = new BooleanProperty(PropertyType.GENERAL_SOFTPLC, "includeSoftPLC", false, "Include soft PLC");
    public static final IntegerProperty SOFTPLC_CYCLE_TIME = new IntegerProperty(PropertyType.GENERAL_SOFTPLC, "softplcCycleTime", 40, "SoftPLC Cycle time (ms)", false, 1);
    public static final StringProperty SOFTPLC_INTERFACES = new StringProperty(PropertyType.GENERAL_SOFTPLC, "softplcInterfaces", "org.supremica.softplc.Simulator.BTSim", "Default interface");

    // GUI
    public static final BooleanProperty INCLUDE_JGRAFCHART = new BooleanProperty(PropertyType.GUI, "includeJGrafchart", false, "Include JGrafchart");
    public static final BooleanProperty INCLUDE_SHOE_FACTORY = new BooleanProperty(PropertyType.GUI, "includeShoeFactory", false, "Include Shoe factory simulation");

    // GUI_EDITOR
    //public static final BooleanProperty GUI_EDITOR_USE_SPRING_EMBEDDER = new BooleanProperty(PropertyType.GUI_EDITOR, "useSpringEmbedder", true, "Use spring embedder for automatic graph layout");
    public static final IntegerProperty GUI_EDITOR_SPRING_EMBEDDER_TIMEOUT = new IntegerProperty(PropertyType.GUI_EDITOR, "springEmbedderTimeout", 10000, "Maximum layout time");

    // GUI_ANALYZER
    public static final BooleanProperty INCLUDE_BOUNDED_UNCON_TOOLS = new BooleanProperty(PropertyType.GUI_ANALYZER, "includeBoundedUnconTools", false, "Include unbounded controllability tools");
    public static final BooleanProperty GUI_ANALYZER_AUTOMATONVIEWER_USE_CONTROLLED_SURFACE = new BooleanProperty(PropertyType.GUI_ANALYZER, "automatonViewerUseControlledSurface", false, "Use new controlled surface panel to display an automaton");

    // GUI_SIMULATOR
    public static final BooleanProperty INCLUDE_ANIMATOR = new BooleanProperty(PropertyType.GUI_SIMULATOR, "includeAnimator", false, "Include 2D Graphical Animator");
    public static final BooleanProperty SIMULATION_IS_EXTERNAL = new BooleanProperty(PropertyType.GUI_SIMULATOR, "simulationIsExternal", false, "External simulation process");
    public static final IntegerProperty SIMULATION_CYCLE_TIME = new IntegerProperty(PropertyType.GUI_SIMULATOR, "simulationCycleTime", 100, "Simulator Cycle time (ms)", false, 0);

    // ALGORITHMS_SYNCHRONIZATION
    public static final BooleanProperty SYNC_FORBID_UNCON_STATES = new BooleanProperty(PropertyType.ALGORITHMS_SYNCHRONIZATION, "syncForbidUncontrollableStates", true, "Forbid uncontrollable states when synchronizing");
    public static final BooleanProperty SYNC_EXPAND_FORBIDDEN_STATES = new BooleanProperty(PropertyType.ALGORITHMS_SYNCHRONIZATION, "syncExpandUncontrollableStates", true, "Expand forbidden states when synchronizing");
    public static final IntegerProperty SYNC_INITIAL_HASHTABLE_SIZE = new IntegerProperty(PropertyType.ALGORITHMS_SYNCHRONIZATION, "syncInitialHashtableSize", (1 << 14) - 1, "Initial hashtable size", false, 1);
    public static final BooleanProperty SYNC_EXPAND_HASHTABLE = new BooleanProperty(PropertyType.ALGORITHMS_SYNCHRONIZATION, "syncExpandHashtable", true, "Expand hashtable");
    public static final IntegerProperty SYNC_NBR_OF_EXECUTERS = new IntegerProperty(PropertyType.ALGORITHMS_SYNCHRONIZATION, "synchNbrOfExecuters", 1, "Number of synchronization threads", false, 1);
    public static final StringProperty SYNC_AUTOMATON_NAME_SEPARATOR = new StringProperty(PropertyType.ALGORITHMS_SYNCHRONIZATION, "synchAutomatonNameSeparator", "||", "Automata name separator");

    // ALGORITHMS_VERIFICATION
    public static final StringProperty VERIFY_VERIFICATION_TYPE = new StringProperty(PropertyType.ALGORITHMS_VERIFICATION, "verifyVerificationType", VerificationType.CONTROLLABILITY, "Default verificaton type", VerificationType.values());
    public static final StringProperty VERIFY_ALGORITHM_TYPE  = new StringProperty(PropertyType.ALGORITHMS_VERIFICATION, "verifyAlgorithmType", VerificationAlgorithm.MODULAR, "Default verificaton algorithm", VerificationAlgorithm.values());
    public static final IntegerProperty VERIFY_EXCLUSION_STATE_LIMIT = new IntegerProperty(PropertyType.ALGORITHMS_VERIFICATION, "verifyExclusionStateLimit", 1000, "Exclusion state limit", false, 1);
    public static final IntegerProperty VERIFY_REACHABILITY_STATE_LIMIT = new IntegerProperty(PropertyType.ALGORITHMS_VERIFICATION, "verifyReachabilityStateLimit", 1000, "Reachability state limit", false, 1);
    public static final BooleanProperty VERIFY_ONE_EVENT_AT_A_TIME = new BooleanProperty(PropertyType.ALGORITHMS_VERIFICATION, "verifyOneEventAtATime", false, "Verify one event at a time");
    public static final BooleanProperty VERIFY_SKIP_UNCONTROLLABILITY_CHECK = new BooleanProperty(PropertyType.ALGORITHMS_VERIFICATION, "skipUncontrollabilityCheck", false, "Skip uncontrollability check");
    public static final IntegerProperty VERIFY_NBR_OF_ATTEMPTS = new IntegerProperty(PropertyType.ALGORITHMS_VERIFICATION, "nbrOfAttempts", 5, "Number of attempts", false, 1);
    public static final BooleanProperty VERIFY_SHOW_BAD_TRACE = new BooleanProperty(PropertyType.ALGORITHMS_VERIFICATION, "showBadTrace", false, "Show trace to bad state");

    // ALGORITHMS_SYNTHESIS
    public static final StringProperty SYNTHESIS_SYNTHESIS_TYPE = new StringProperty(PropertyType.ALGORITHMS_SYNTHESIS, "synthesisSynthesisType", SynthesisType.NONBLOCKINGCONTROLLABLE, "Default synthesis type", SynthesisType.values());
    public static final StringProperty SYNTHESIS_ALGORITHM_TYPE  = new StringProperty(PropertyType.ALGORITHMS_SYNTHESIS, "synthesisAlgorithmType", SynthesisAlgorithm.MONOLITHIC, "Default synthesis algorithm", SynthesisAlgorithm.values());
    public static final BooleanProperty SYNTHESIS_PURGE = new BooleanProperty(PropertyType.ALGORITHMS_SYNTHESIS, "synthesisPurge", false, "Remove forbidden states after synthesis");
    public static final BooleanProperty SYNTHESIS_OPTIMIZE = new BooleanProperty(PropertyType.ALGORITHMS_SYNTHESIS, "synthesisOptimize", false, "Try to remove supervisors that are not necessary");
    public static final BooleanProperty SYNTHESIS_MAXIMALLY_PERMISSIVE = new BooleanProperty(PropertyType.ALGORITHMS_SYNTHESIS, "synthesisMaximallyPermissive", true, "Synthesize a maximally permissive supervisor");
    public static final BooleanProperty SYNTHESIS_MAXIMALLY_PERMISSIVE_INCREMENTAL = new BooleanProperty(PropertyType.ALGORITHMS_SYNTHESIS, "synthesisMaximallyPermissiveIncremental", true, "Use incremental algorithm when synthesizing");
    public static final BooleanProperty SYNTHESIS_REDUCE_SUPERVISORS = new BooleanProperty(PropertyType.ALGORITHMS_SYNTHESIS, "synthesisReduceSupervisors", false, "Try to minimize supervisors");

    // ALGORITHMS_MINIMIZATION
    public static final StringProperty MINIMIZATION_EQUIVALENCE_RELATION = new StringProperty(PropertyType.ALGORITHMS_MINIMIZATION, "minimizationEquivalenceRelation", EquivalenceRelation.LANGUAGEEQUIVALENCE.toString(), "Default equivalence relation", EquivalenceRelation.values());
    public static final BooleanProperty MINIMIZATION_ALSO_MINIMIZE_TRANSITIONS = new BooleanProperty(PropertyType.ALGORITHMS_MINIMIZATION, "minimizationAlsoMinimizeTransitions", true, "Minimize the number of transitions");
    public static final BooleanProperty MINIMIZATION_KEEP_ORIGINAL = new BooleanProperty(PropertyType.ALGORITHMS_MINIMIZATION, "minimizationKeepOriginal", true, "Keep original");
    public static final BooleanProperty MINIMIZATION_IGNORE_MARKING = new BooleanProperty(PropertyType.ALGORITHMS_MINIMIZATION, "minimizationIgnoreMarking", false, "Ignore marking");
    public static final StringProperty MINIMIZATION_STRATEGY = new StringProperty(PropertyType.ALGORITHMS_MINIMIZATION, "minimizationStrategy", MinimizationStrategy.FewestTransitionsFirst.toString(), "Minimization strategy", MinimizationStrategy.values());
    public static final StringProperty MINIMIZATION_HEURISTIC = new StringProperty(PropertyType.ALGORITHMS_MINIMIZATION, "minimizationHeuristic", MinimizationHeuristic.MostLocal.toString(), "Minimization heuristics", MinimizationHeuristic.values());
    public static final StringProperty MINIMIZATION_SILENT_EVENT_NAME = new StringProperty(PropertyType.ALGORITHMS_MINIMIZATION, "generalSilentEventName", "tau", "Silent event name");
    public static final StringProperty MINIMIZATION_SILENT_CONTROLLABLE_EVENT_NAME = new StringProperty(PropertyType.ALGORITHMS_MINIMIZATION, "generalSilentControllableEventName", "tau_c", "Silent controllable event name");
    public static final StringProperty MINIMIZATION_SILENT_UNCONTROLLABLE_EVENT_NAME = new StringProperty(PropertyType.ALGORITHMS_MINIMIZATION, "generalSilentUnontrollableEventName", "tau_u", "Silent uncontrollable event name");

    // ALGORITHMS_BDD
    // Most of the IntegerProperty:s here should be StringProperty:s with appropriate legal values...
    public static final IntegerProperty BDD_ALGORITHM = new IntegerProperty(PropertyType.ALGORITHMS_BDD, "bddAlgorithm", Options.algo_family, "Algorithm");
    public static final IntegerProperty BDD_SHOW_GROW = new IntegerProperty(PropertyType.ALGORITHMS_BDD, "bddShowGrowth", Options.show_grow, "Show growth");
    public static final BooleanProperty BDD_SIZE_WATCH = new BooleanProperty(PropertyType.ALGORITHMS_BDD, "bddSizeWatch", Options.size_watch, "Size watch");
    public static final BooleanProperty BDD_ALTER_PCG = new BooleanProperty(PropertyType.ALGORITHMS_BDD, "bddAlterPCG", Options.user_alters_PCG, "Alter PCG");
    public static final BooleanProperty BDD_DEBUG_ON = new BooleanProperty(PropertyType.ALGORITHMS_BDD, "bddDebugOn", Options.debug_on, "Debug on");
    public static final BooleanProperty BDD_UC_OPTIMISTIC = new BooleanProperty(PropertyType.ALGORITHMS_BDD, "bddUCOptimistic", Options.uc_optimistic, "uc optimistic");
    public static final BooleanProperty BDD_NB_OPTIMISTIC = new BooleanProperty(PropertyType.ALGORITHMS_BDD, "bddNBOptimistic", Options.nb_optimistic, "nb optimistic");
    public static final BooleanProperty BDD_LOCAL_SATURATION = new BooleanProperty(PropertyType.ALGORITHMS_BDD, "bddLocalSaturation", Options.local_saturation, "Local saturation");
    public static final BooleanProperty BDD_TRACE_ON = new BooleanProperty(PropertyType.ALGORITHMS_BDD, "bddTraceOn", Options.trace_on, "Trace on");
    public static final BooleanProperty BDD_PROFILE_ON = new BooleanProperty(PropertyType.ALGORITHMS_BDD, "bddProfileOn", Options.profile_on, "Profiling");
    public static final IntegerProperty BDD_COUNT_ALGO = new IntegerProperty(PropertyType.ALGORITHMS_BDD, "bddCountAlgorithm", Options.count_algo, "Count algorithm");
    public static final IntegerProperty BDD_LI_ALGO = new IntegerProperty(PropertyType.ALGORITHMS_BDD, "bddLanguageInclusionAlgorithm", Options.inclsuion_algorithm, "Inclusion algorithm");
    public static final IntegerProperty BDD_ORDER_ALGO = new IntegerProperty(PropertyType.ALGORITHMS_BDD, "bddAutomataOrderingAlgorithm", Options.ordering_algorithm, "Automata ordering algorithm");
    public static final IntegerProperty BDD_ORDERING_FORCE_COST = new IntegerProperty(PropertyType.ALGORITHMS_BDD, "bddOrderingForceCost", Options.ordering_force_cost, "Ordering force cost");
    public static final IntegerProperty BDD_AS_HEURISTIC = new IntegerProperty(PropertyType.ALGORITHMS_BDD, "bddAutomataSelectionHeuristics", Options.as_heuristics, "Automata selection heuristics");
    public static final IntegerProperty BDD_FRONTIER_TYPE = new IntegerProperty(PropertyType.ALGORITHMS_BDD, "bddFrontierType", Options.frontier_strategy, "Frontier strategy");
    public static final IntegerProperty BDD_H1 = new IntegerProperty(PropertyType.ALGORITHMS_BDD, "bddH1", Options.es_heuristics, "ES Heuristics");
    public static final IntegerProperty BDD_H2 = new IntegerProperty(PropertyType.ALGORITHMS_BDD, "bddH2", Options.ndas_heuristics, "NDAS Heuristics");
    public static final IntegerProperty BDD_DSSI_HEURISTIC = new IntegerProperty(PropertyType.ALGORITHMS_BDD, "bddDelayedStarSelection", Options.dssi_heuristics, "DSSI heuristics");
    // This really is an IntegerProperty, the others aren't
    public static final IntegerProperty BDD_PARTITION_MAX = new IntegerProperty(PropertyType.ALGORITHMS_BDD, "bddMaxPartitionSize", Options.max_partition_size, "Max Partition Size");
    public static final IntegerProperty BDD_ENCODING_ALGO = new IntegerProperty(PropertyType.ALGORITHMS_BDD, "bddStateEncodingAlgorithm", Options.encoding_algorithm, "Encoding algorithm");
    public static final StringProperty BDD_LIB_PATH  = new StringProperty(PropertyType.ALGORITHMS_BDD, "bddLibPath", Options.extraLibPath, "Extra Library path");
    public static final IntegerProperty BDD_SUP_REACHABILITY = new IntegerProperty(PropertyType.ALGORITHMS_BDD, "bddSupReachability", Options.sup_reachability_type, "Supervisor Reachability Type");
    public static final IntegerProperty BDD_DISJ_OPTIMIZER_ALGO = new IntegerProperty(PropertyType.ALGORITHMS_BDD, "bddDisjOptimizerAlgo", Options.disj_optimizer_algo, "Disjunctive optimizer algorithm");
    public static final IntegerProperty BDD_TRANSITION_OPTIMIZER_ALGO = new IntegerProperty(PropertyType.ALGORITHMS_BDD, "bddTransitionOptimizerAlgo", Options.transition_optimizer_algo, "Transition optimizer algorithm");
    public static final BooleanProperty BDD_INTERLEAVED_VARIABLES = new BooleanProperty(PropertyType.ALGORITHMS_BDD, "bddInterleavedVariables", Options.interleaved_variables, "Interleaved or seperated variable orders");
    public static final BooleanProperty BDD_LEVEL_GRAPHS = new BooleanProperty(PropertyType.ALGORITHMS_BDD, "bddLevelGraphs", Options.show_level_graph, "Shows the fillness of the workset sent to H1");

    // ALGORITHMS_HMI
    public static final BooleanProperty INCLUDE_USERINTERFACE = new BooleanProperty(PropertyType.ALGORITHMS_HMI, "includeUserInterface", false, "Include SwiXML analyzer tools");

    private static Config instance = null;

    /**
     * This class should only be instantiated to guarantee that it is loaded.
     */
    private Config()
    {
    }

    public static Config getInstance()
    {
        if (instance == null)
            instance = new Config();
        return instance;
    }
}

