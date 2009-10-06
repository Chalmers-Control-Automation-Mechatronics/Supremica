//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.properties
//# CLASS:   Config
//###########################################################################
//# $Id$
//###########################################################################

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

import javax.swing.UIManager;
import javax.swing.text.html.Option;

import org.supremica.automata.algorithms.EquivalenceRelation;
import org.supremica.automata.algorithms.SynthesisAlgorithm;
import org.supremica.automata.algorithms.SynthesisType;
import org.supremica.automata.algorithms.VerificationAlgorithm;
import org.supremica.automata.algorithms.VerificationType;
import org.supremica.automata.algorithms.minimization.MinimizationHeuristic;
import org.supremica.automata.algorithms.minimization.MinimizationStrategy;
import org.supremica.automata.BDD.BDDPartitioningType;
import org.supremica.automata.BDD.BDDLibraryType;
import org.supremica.util.BDD.Options;

/**
 * Configurable options. All of these are automatically added to a GUI for editing.
 * The current configuration is saved in the file SupremicaProperties.cfg which per default
 * is loaded on startup.
 */
public final class Config
{
    // Valid PropertyTypes (see PropertyType.java):
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
    public static final ObjectProperty XML_RPC_FILTER = new ObjectProperty(PropertyType.GENERAL_COMM_XMLRPC, "xmlRpcFilter", "127.0.0.1", "XML-RPC Filter");
    public static final BooleanProperty XML_RPC_DEBUG = new BooleanProperty(PropertyType.GENERAL_COMM_XMLRPC, "xmlRpcDebug", false, "XML-RPC Debug");

    // GUI_DOT
    public static final BooleanProperty DOT_USE = new BooleanProperty(PropertyType.GUI_DOT, "dotUse", true, "Use Dot");
    public static final ObjectProperty DOT_EXECUTE_COMMAND = new ObjectProperty(PropertyType.GUI_DOT, "dotExecuteCommand", "dot", "Dot command");
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
     * GTK
     */
    private static enum LOOKANDFEEL_LEGALVALUES {System, Metal, Motif, Windows, Mac, GTK};
    //private static final Object[] LOOKANDFEEL_LEGALVALUES2 = UIManager.getInstalledLookAndFeels(); // Won't work
    public static final ObjectProperty GENERAL_LOOKANDFEEL  = new ObjectProperty(PropertyType.GENERAL, "generalLookAndFeel", LOOKANDFEEL_LEGALVALUES.System, "Look and feel (requires restart)", LOOKANDFEEL_LEGALVALUES.values());
    public static final ObjectProperty GENERAL_STATE_SEPARATOR  = new ObjectProperty(PropertyType.GENERAL, "generalStateSeparator", ".", "State separator character");
    public static final ObjectProperty GENERAL_STATELABEL_SEPARATOR  = new ObjectProperty(PropertyType.GENERAL, "generalStateLabelSeparator", ",", "State label separator character");
    public static final BooleanProperty GENERAL_USE_SECURITY = new BooleanProperty(PropertyType.GENERAL, "generalUseSecurity", false, "Use file security");
    public static final BooleanProperty GENERAL_STUDENT_VERSION = new BooleanProperty(PropertyType.GENERAL, "generalStudentVersion", false, "Student version (requires restart)");
    public static final BooleanProperty INCLUDE_EXPERIMENTAL_ALGORITHMS = new BooleanProperty(PropertyType.GENERAL, "includeExperimentalAlgorithms", false, "Include experimental algorithms (requires restart)");

    // GENERAL_LOG
    public static final BooleanProperty LOG_TO_CONSOLE =
      new BooleanProperty(PropertyType.GENERAL_LOG, "logToConsole",
			   true, "Log to Console");
    public static final BooleanProperty LOG_TO_GUI =
      new BooleanProperty(PropertyType.GENERAL_LOG, "logToGUI",
			   false, "Log to Graphical User Interface");
    public static final BooleanProperty GENERAL_REDIRECT_STDOUT =
      new BooleanProperty(PropertyType.GENERAL_LOG, "generalRedirectStdout",
			   true, "Redirect stdout");
    public static final BooleanProperty GENERAL_REDIRECT_STDERR =
      new BooleanProperty(PropertyType.GENERAL_LOG, "generalRedirectStderr",
			   false, "Redirect stderr");
    public static final BooleanProperty VERBOSE_MODE =
      new BooleanProperty(PropertyType.GENERAL_LOG, "verboseMode",
			  false, "Verbose mode");

    // GENERAL_FILE
    public static final ObjectProperty FILE_OPEN_PATH = new ObjectProperty(PropertyType.GENERAL_FILE, "fileOpenPath", LocalSystem.getHomeDirectory(), "Default file open path");
    public static final ObjectProperty FILE_SAVE_PATH = new ObjectProperty(PropertyType.GENERAL_FILE, "fileSavePath", LocalSystem.getHomeDirectory(), "Default file save path");
    public static final BooleanProperty FILE_ALLOW_OPEN = new BooleanProperty(PropertyType.GENERAL_FILE, "fileAllowOpen", true, "Allow user to open file");
    public static final BooleanProperty FILE_ALLOW_SAVE = new BooleanProperty(PropertyType.GENERAL_FILE, "fileAllowSave", true, "Allow user to save file");
    public static final BooleanProperty FILE_ALLOW_IMPORT = new BooleanProperty(PropertyType.GENERAL_FILE, "fileAllowImport", true, "Allow user to import file");
    public static final BooleanProperty FILE_ALLOW_EXPORT = new BooleanProperty(PropertyType.GENERAL_FILE, "fileAllowExport", true, "Allow user to export file");
    public static final BooleanProperty FILE_ALLOW_QUIT = new BooleanProperty(PropertyType.GENERAL_FILE, "fileAllowQuit", true, "Allow user to quit Supremica");

    // GENERAL_SOFTPLC
    public static final BooleanProperty INCLUDE_SOFTPLC = new BooleanProperty(PropertyType.GENERAL_SOFTPLC, "includeSoftPLC", false, "Include soft PLC");
    public static final IntegerProperty SOFTPLC_CYCLE_TIME = new IntegerProperty(PropertyType.GENERAL_SOFTPLC, "softplcCycleTime", 40, "SoftPLC Cycle time (ms)", false, 1);
    public static final ObjectProperty SOFTPLC_INTERFACES = new ObjectProperty(PropertyType.GENERAL_SOFTPLC, "softplcInterfaces", "org.supremica.softplc.Simulator.BTSim", "Default interface");

    // GUI
    public static final BooleanProperty INCLUDE_EXTERNALTOOLS = new BooleanProperty(PropertyType.GUI, "includeExternalTools", true, "Include external tools");
    public static final BooleanProperty INCLUDE_JGRAFCHART = new BooleanProperty(PropertyType.GUI, "includeJGrafchart", false, "Include JGrafchart");
    public static final BooleanProperty INCLUDE_SOCEDITOR = new BooleanProperty(PropertyType.GUI, "includeSOCEditor", true, "Include SOC editor");
    public static final BooleanProperty INCLUDE_SHOE_FACTORY = new BooleanProperty(PropertyType.GUI, "includeShoeFactory", false, "Include Shoe factory simulation");
    public static final BooleanProperty INCLUDE_INSTANTION =
      new BooleanProperty(PropertyType.GUI, "includeInstantiation", true,
			  "Enable instantiation and other advanced features");
    public static final BooleanProperty OPTIMIZING_COMPILER =
      new BooleanProperty(PropertyType.GUI, "optimizingCompiler", true,
			  "Remove redundant events, transitions, and components " +
              "when compiling");
    public static final BooleanProperty USE_EVENT_ALPHABET =
      new BooleanProperty(PropertyType.GUI, "useEventAlphabet", true,
			  "Use per-event alphabet when compiling EFA");

    // GUI_EDITOR
    //public static final BooleanProperty GUI_EDITOR_USE_SPRING_EMBEDDER = new BooleanProperty(PropertyType.GUI_EDITOR, "useSpringEmbedder", true, "Use spring embedder for automatic graph layout");
    public static final BooleanProperty GUI_EDITOR_DEFAULT_EMPTY_MODULE = new BooleanProperty(PropertyType.GUI_EDITOR, "defaultEmptyModule", true, "Open with an empty module");
    public static final IntegerProperty GUI_EDITOR_SPRING_EMBEDDER_TIMEOUT = new IntegerProperty(PropertyType.GUI_EDITOR, "springEmbedderTimeout", 10000, "Maximum layout time", false, 0);
    public static final BooleanProperty GUI_EDITOR_SHOW_GRID = new BooleanProperty(PropertyType.GUI_EDITOR, "showGrid", true, "Show grid");
    public static final IntegerProperty GUI_EDITOR_GRID_SIZE = new IntegerProperty(PropertyType.GUI_EDITOR, "gridSize", 16, "Grid size", false, 4, 64, 4);
    public static final BooleanProperty GUI_EDITOR_NODES_SNAP_TO_GRID = new BooleanProperty(PropertyType.GUI_EDITOR, "nodesSnapToGrid", true, "Nodes snap to grid");
    public static final IntegerProperty GUI_EDITOR_NODE_RADIUS =
        new IntegerProperty(PropertyType.GUI_EDITOR, "nodeRadius", 6,
                            "Node size", false, 4, 32, 1);
    public static final BooleanProperty GUI_EDITOR_CONTROL_POINTS_MOVE_WITH_NODE = new BooleanProperty(PropertyType.GUI_EDITOR, "controlPointsMoveWithNode", true, "Control points move with node");
    public static final BooleanProperty GUI_EDITOR_EDGEARROW_AT_END = new BooleanProperty(PropertyType.GUI_EDITOR, "edgeArrowAtEnd", true, "Draw edge arrows at the end");
    public static enum LAYOUT_MODE_LEGALVALUES { Default, ChalmersIDES }
    public static final ObjectProperty GUI_EDITOR_LAYOUT_MODE  = new ObjectProperty(PropertyType.GUI_EDITOR, "layoutMode", LAYOUT_MODE_LEGALVALUES.Default, "Layout mode", LAYOUT_MODE_LEGALVALUES.values());

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
    public static final ObjectProperty SYNC_AUTOMATON_NAME_SEPARATOR = new ObjectProperty(PropertyType.ALGORITHMS_SYNCHRONIZATION, "synchAutomatonNameSeparator", "||", "Automata name separator");

    // ALGORITHMS_VERIFICATION
    public static final ObjectProperty VERIFY_VERIFICATION_TYPE = new ObjectProperty(PropertyType.ALGORITHMS_VERIFICATION, "verifyVerificationType", VerificationType.CONTROLLABILITY, "Default verificaton type", VerificationType.values());
    public static final ObjectProperty VERIFY_ALGORITHM_TYPE  = new ObjectProperty(PropertyType.ALGORITHMS_VERIFICATION, "verifyAlgorithmType", VerificationAlgorithm.MODULAR, "Default verificaton algorithm", VerificationAlgorithm.values());
    public static final IntegerProperty VERIFY_EXCLUSION_STATE_LIMIT = new IntegerProperty(PropertyType.ALGORITHMS_VERIFICATION, "verifyExclusionStateLimit", 1000, "Exclusion state limit", false, 1);
    public static final IntegerProperty VERIFY_REACHABILITY_STATE_LIMIT = new IntegerProperty(PropertyType.ALGORITHMS_VERIFICATION, "verifyReachabilityStateLimit", 1000, "Reachability state limit", false, 1);
    public static final BooleanProperty VERIFY_ONE_EVENT_AT_A_TIME = new BooleanProperty(PropertyType.ALGORITHMS_VERIFICATION, "verifyOneEventAtATime", false, "Verify one event at a time");
    public static final BooleanProperty VERIFY_SKIP_UNCONTROLLABILITY_CHECK = new BooleanProperty(PropertyType.ALGORITHMS_VERIFICATION, "skipUncontrollabilityCheck", false, "Skip uncontrollability check");
    public static final IntegerProperty VERIFY_NBR_OF_ATTEMPTS = new IntegerProperty(PropertyType.ALGORITHMS_VERIFICATION, "nbrOfAttempts", 5, "Number of attempts", false, 1);
    public static final BooleanProperty VERIFY_SHOW_BAD_TRACE = new BooleanProperty(PropertyType.ALGORITHMS_VERIFICATION, "showBadTrace", false, "Show trace to bad state");

    // ALGORITHMS_SYNTHESIS
    public static final ObjectProperty SYNTHESIS_SYNTHESIS_TYPE = new ObjectProperty(PropertyType.ALGORITHMS_SYNTHESIS, "synthesisSynthesisType", SynthesisType.NONBLOCKINGCONTROLLABLE, "Default synthesis type", SynthesisType.values());
    public static final ObjectProperty SYNTHESIS_ALGORITHM_TYPE  = new ObjectProperty(PropertyType.ALGORITHMS_SYNTHESIS, "synthesisAlgorithmType", SynthesisAlgorithm.MONOLITHIC, "Default synthesis algorithm", SynthesisAlgorithm.values());
    public static final BooleanProperty SYNTHESIS_PURGE = new BooleanProperty(PropertyType.ALGORITHMS_SYNTHESIS, "synthesisPurge", false, "Remove forbidden states after synthesis");
    public static final BooleanProperty SYNTHESIS_OPTIMIZE = new BooleanProperty(PropertyType.ALGORITHMS_SYNTHESIS, "synthesisOptimize", false, "Try to remove supervisors that are not necessary");
    public static final BooleanProperty SYNTHESIS_MAXIMALLY_PERMISSIVE = new BooleanProperty(PropertyType.ALGORITHMS_SYNTHESIS, "synthesisMaximallyPermissive", true, "Synthesize a maximally permissive supervisor");
    public static final BooleanProperty SYNTHESIS_MAXIMALLY_PERMISSIVE_INCREMENTAL = new BooleanProperty(PropertyType.ALGORITHMS_SYNTHESIS, "synthesisMaximallyPermissiveIncremental", true, "Use incremental algorithm when synthesizing");
    public static final BooleanProperty SYNTHESIS_REDUCE_SUPERVISORS = new BooleanProperty(PropertyType.ALGORITHMS_SYNTHESIS, "synthesisReduceSupervisors", false, "Try to minimize supervisors");

    // ALGORITHMS_MINIMIZATION
    public static final ObjectProperty MINIMIZATION_EQUIVALENCE_RELATION = new ObjectProperty(PropertyType.ALGORITHMS_MINIMIZATION, "minimizationEquivalenceRelation", EquivalenceRelation.LANGUAGEEQUIVALENCE, "Default equivalence relation", EquivalenceRelation.values());
    public static final BooleanProperty MINIMIZATION_ALSO_MINIMIZE_TRANSITIONS = new BooleanProperty(PropertyType.ALGORITHMS_MINIMIZATION, "minimizationAlsoMinimizeTransitions", true, "Minimize the number of transitions");
    public static final BooleanProperty MINIMIZATION_KEEP_ORIGINAL = new BooleanProperty(PropertyType.ALGORITHMS_MINIMIZATION, "minimizationKeepOriginal", true, "Keep original");
    public static final BooleanProperty MINIMIZATION_IGNORE_MARKING = new BooleanProperty(PropertyType.ALGORITHMS_MINIMIZATION, "minimizationIgnoreMarking", false, "Ignore marking");
    public static final ObjectProperty MINIMIZATION_STRATEGY = new ObjectProperty(PropertyType.ALGORITHMS_MINIMIZATION, "minimizationStrategy", MinimizationStrategy.FewestTransitionsFirst, "Minimization strategy", MinimizationStrategy.values());
    public static final ObjectProperty MINIMIZATION_HEURISTIC = new ObjectProperty(PropertyType.ALGORITHMS_MINIMIZATION, "minimizationHeuristic", MinimizationHeuristic.MostLocal, "Minimization heuristics", MinimizationHeuristic.values());
    public static final ObjectProperty MINIMIZATION_SILENT_EVENT_NAME = new ObjectProperty(PropertyType.ALGORITHMS_MINIMIZATION, "generalSilentEventName", "tau", "Silent event name");
    public static final ObjectProperty MINIMIZATION_SILENT_CONTROLLABLE_EVENT_NAME = new ObjectProperty(PropertyType.ALGORITHMS_MINIMIZATION, "generalSilentControllableEventName", "tau_c", "Silent controllable event name");
    public static final ObjectProperty MINIMIZATION_SILENT_UNCONTROLLABLE_EVENT_NAME = new ObjectProperty(PropertyType.ALGORITHMS_MINIMIZATION, "generalSilentUnontrollableEventName", "tau_u", "Silent uncontrollable event name");

    // ALGORITHMS_BDD2
    // New BDD implementation using JavaBDD library
    public static final ObjectProperty BDD2_BDDLIBRARY = new ObjectProperty(PropertyType.ALGORITHMS_BDD2, "libraryName", BDDLibraryType.JAVA, "Binary Decision Diagram Library", BDDLibraryType.values());
    public static final IntegerProperty BDD2_INITIALNODETABLESIZE = new IntegerProperty(PropertyType.ALGORITHMS_BDD2, "initialNodeTableSize", 1000000, "Initial node table size");
    public static final IntegerProperty BDD2_CACHESIZE = new IntegerProperty(PropertyType.ALGORITHMS_BDD2, "cacheSize", 100000, "Operation cache size");
    public static final IntegerProperty BDD2_MAXINCREASENODES = new IntegerProperty(PropertyType.ALGORITHMS_BDD2, "maxIncreaseNodes", 2500000, "Set maximum number of nodes by which to increase node table after a garbage collection.");
    public static final DoubleProperty BDD2_INCREASEFACTOR = new DoubleProperty(PropertyType.ALGORITHMS_BDD2, "increaseFactor", 2.0, "Set factor by which to increase node table after a garbage collection.", false, 0.0);
    public static final DoubleProperty BDD2_CACHERATIO = new DoubleProperty(PropertyType.ALGORITHMS_BDD2, "cacheRatio", 10.0, "Sets the cache ratio for the operator caches (#tablenodes/#cachenodes)", false, 0.0);
    public static final ObjectProperty BDD2_PARTITIONING = new ObjectProperty(PropertyType.ALGORITHMS_BDD2, "partitioning", BDDPartitioningType.MONOLITHIC, "BDD transition partitioning", BDDPartitioningType.values());

    // ALGORITHMS_BDD
    // Most of these are ugly integers in BDD.Options... but they have String representations here.
    public static final ObjectProperty BDD_ALGORITHM = new ObjectProperty(PropertyType.ALGORITHMS_BDD, "bddAlgorithm", Options.REACH_ALGO_NAMES[Options.algo_family], "Algorithm", Options.REACH_ALGO_NAMES);
    public static final ObjectProperty BDD_SHOW_GROW = new ObjectProperty(PropertyType.ALGORITHMS_BDD, "bddShowGrowth", Options.SHOW_GROW_NAMES[Options.show_grow], "Show growth", Options.SHOW_GROW_NAMES);
    public static final ObjectProperty BDD_COUNT_ALGO = new ObjectProperty(PropertyType.ALGORITHMS_BDD, "bddCountAlgorithm", Options.COUNT_ALGO_NAMES[Options.count_algo], "Count algorithm", Options.COUNT_ALGO_NAMES);
    public static final ObjectProperty BDD_LI_ALGO = new ObjectProperty(PropertyType.ALGORITHMS_BDD, "bddLanguageInclusionAlgorithm", Options.INCLUSION_ALGORITHM_NAMES[Options.inclusion_algorithm], "Inclusion algorithm", Options.INCLUSION_ALGORITHM_NAMES);
    public static final ObjectProperty BDD_ORDER_ALGO = new ObjectProperty(PropertyType.ALGORITHMS_BDD, "bddAutomataOrderingAlgorithm", Options.ORDERING_ALGORITHM_NAMES[Options.ordering_algorithm], "Automata ordering algorithm", Options.ORDERING_ALGORITHM_NAMES);
    public static final ObjectProperty BDD_ORDERING_FORCE_COST = new ObjectProperty(PropertyType.ALGORITHMS_BDD, "bddOrderingForceCost", Options.FORCE_TYPE_NAMES[Options.ordering_force_cost], "Ordering force cost", Options.FORCE_TYPE_NAMES);
    public static final ObjectProperty BDD_AS_HEURISTIC = new ObjectProperty(PropertyType.ALGORITHMS_BDD, "bddAutomataSelectionHeuristics", Options.AS_HEURISTIC_NAMES[Options.as_heuristics], "Automata selection heuristics", Options.AS_HEURISTIC_NAMES);
    public static final ObjectProperty BDD_FRONTIER_TYPE = new ObjectProperty(PropertyType.ALGORITHMS_BDD, "bddFrontierType", Options.FRONTIER_STRATEGY_NAMES[Options.frontier_strategy], "Frontier strategy", Options.FRONTIER_STRATEGY_NAMES);
    public static final ObjectProperty BDD_H1 = new ObjectProperty(PropertyType.ALGORITHMS_BDD, "bddH1", Options.ES_HEURISTIC_NAMES[Options.es_heuristics], "ES Heuristics", Options.ES_HEURISTIC_NAMES);
    public static final ObjectProperty BDD_H2 = new ObjectProperty(PropertyType.ALGORITHMS_BDD, "bddH2", Options.NDAS_HEURISTIC_NAMES[Options.ndas_heuristics], "NDAS Heuristics", Options.NDAS_HEURISTIC_NAMES);
    public static final ObjectProperty BDD_DSSI_HEURISTIC = new ObjectProperty(PropertyType.ALGORITHMS_BDD, "bddDelayedStarSelection", Options.DSSI_HEURISTIC_NAMES[Options.dssi_heuristics], "DSSI heuristics", Options.DSSI_HEURISTIC_NAMES);
    public static final ObjectProperty BDD_ENCODING_ALGO = new ObjectProperty(PropertyType.ALGORITHMS_BDD, "bddStateEncodingAlgorithm", Options.ENCODING_NAMES[Options.encoding_algorithm], "Encoding algorithm", Options.ENCODING_NAMES);
    public static final ObjectProperty BDD_SUP_REACHABILITY = new ObjectProperty(PropertyType.ALGORITHMS_BDD, "bddSupReachability", Options.SUP_REACHABILITY_NAMES[Options.sup_reachability_type], "Supervisor Reachability Type", Options.SUP_REACHABILITY_NAMES);
    public static final ObjectProperty BDD_DISJ_OPTIMIZER_ALGO = new ObjectProperty(PropertyType.ALGORITHMS_BDD, "bddDisjOptimizerAlgo", Options.DISJ_OPTIMIZER_NAMES[Options.disj_optimizer_algo], "Disjunctive optimizer algorithm", Options.DISJ_OPTIMIZER_NAMES);
    public static final ObjectProperty BDD_TRANSITION_OPTIMIZER_ALGO = new ObjectProperty(PropertyType.ALGORITHMS_BDD, "bddTransitionOptimizerAlgo", Options.TRANSITION_OPTIMIZER_NAMES[Options.transition_optimizer_algo], "Transition optimizer algorithm", Options.TRANSITION_OPTIMIZER_NAMES);
    // This really is an IntegerProperty!!
    public static final IntegerProperty BDD_PARTITION_MAX = new IntegerProperty(PropertyType.ALGORITHMS_BDD, "bddMaxPartitionSize", Options.max_partition_size, "Max Partition Size");
    public static final BooleanProperty BDD_SIZE_WATCH = new BooleanProperty(PropertyType.ALGORITHMS_BDD, "bddSizeWatch", Options.size_watch, "Size watch");
    public static final BooleanProperty BDD_ALTER_PCG = new BooleanProperty(PropertyType.ALGORITHMS_BDD, "bddAlterPCG", Options.user_alters_PCG, "Alter PCG");
    public static final BooleanProperty BDD_DEBUG_ON = new BooleanProperty(PropertyType.ALGORITHMS_BDD, "bddDebugOn", Options.debug_on, "Debug on");
    public static final BooleanProperty BDD_UC_OPTIMISTIC = new BooleanProperty(PropertyType.ALGORITHMS_BDD, "bddUCOptimistic", Options.uc_optimistic, "uc optimistic");
    public static final BooleanProperty BDD_NB_OPTIMISTIC = new BooleanProperty(PropertyType.ALGORITHMS_BDD, "bddNBOptimistic", Options.nb_optimistic, "nb optimistic");
    public static final BooleanProperty BDD_LOCAL_SATURATION = new BooleanProperty(PropertyType.ALGORITHMS_BDD, "bddLocalSaturation", Options.local_saturation, "Local saturation");
    public static final BooleanProperty BDD_TRACE_ON = new BooleanProperty(PropertyType.ALGORITHMS_BDD, "bddTraceOn", Options.trace_on, "Trace on");
    public static final BooleanProperty BDD_PROFILE_ON = new BooleanProperty(PropertyType.ALGORITHMS_BDD, "bddProfileOn", Options.profile_on, "Profiling");
    public static final BooleanProperty BDD_INTERLEAVED_VARIABLES = new BooleanProperty(PropertyType.ALGORITHMS_BDD, "bddInterleavedVariables", Options.interleaved_variables, "Interleaved or seperated variable orders");
    public static final BooleanProperty BDD_LEVEL_GRAPHS = new BooleanProperty(PropertyType.ALGORITHMS_BDD, "bddLevelGraphs", Options.show_level_graph, "Shows the fillness of the workset sent to H1");
    public static final ObjectProperty BDD_LIB_PATH  = new ObjectProperty(PropertyType.ALGORITHMS_BDD, "bddLibPath", Options.extraLibPath, "Extra Library path");
    public static final BooleanProperty BDD_SYNTHESIS_EXTRACT_AUTOMATON = new BooleanProperty(PropertyType.ALGORITHMS_BDD, "bddSynthesisExtractAutomaton", false, "Build automaton after BDD synthesis");

    // ALGORITHMS_HMI
    public static final BooleanProperty INCLUDE_USERINTERFACE = new BooleanProperty(PropertyType.ALGORITHMS_HMI, "includeUserInterface", false, "Include SwiXML analyzer tools");
    public static final BooleanProperty EXPAND_EXTENDED_AUTOMATA = new BooleanProperty(PropertyType.ALGORITHMS_HMI, "expandEFA", true, "Expand extended automata into DFA");

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

