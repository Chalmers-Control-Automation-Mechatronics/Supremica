package org.supremica.properties;

import java.awt.Color;
import java.io.File;

import net.sourceforge.waters.analysis.bdd.BDDPackage;
import net.sourceforge.waters.gui.logging.IDELogLevel;
import net.sourceforge.waters.gui.renderer.EdgeArrowPosition;
import net.sourceforge.waters.gui.renderer.LayoutMode;
import net.sourceforge.waters.gui.util.IconSet;
import net.sourceforge.waters.gui.util.LookAndFeelOption;
import net.sourceforge.waters.model.options.BooleanOption;
import net.sourceforge.waters.model.options.ColorOption;
import net.sourceforge.waters.model.options.DoubleOption;
import net.sourceforge.waters.model.options.EnumOption;
import net.sourceforge.waters.model.options.FileOption;
import net.sourceforge.waters.model.options.MemoryOption;
import net.sourceforge.waters.model.options.PositiveIntOption;
import net.sourceforge.waters.model.options.StringOption;

import org.supremica.automata.BDD.BDDPartitioningType;
import org.supremica.automata.algorithms.EquivalenceRelation;
import org.supremica.automata.algorithms.SynthesisAlgorithm;
import org.supremica.automata.algorithms.SynthesisType;
import org.supremica.automata.algorithms.VerificationAlgorithm;
import org.supremica.automata.algorithms.VerificationType;
import org.supremica.automata.algorithms.minimization.MinimizationHeuristic;
import org.supremica.automata.algorithms.minimization.MinimizationPreselectingHeuristic;
import org.supremica.automata.algorithms.minimization.MinimizationSelectingHeuristic;
import org.supremica.automata.algorithms.minimization.MinimizationStrategy;
import org.supremica.util.BDD.OrderingAlgorithm;

public class Config
{

  //#########################################################################
  //# Options

  // GENERAL
  public static final EnumOption<LookAndFeelOption> GENERAL_LOOK_AND_FEEL = new EnumOption<>
    ("javaLookAndFeel", "Java Look&Feel (requires restart)",
     "Java Look&Feel (requires restart)",
    null, LookAndFeelOption.values(), LookAndFeelOption.DEFAULT);
  public static final EnumOption<IconSet> GUI_EDITOR_ICONSET = new EnumOption<>
    ("iconSet", "Icon Set and Font Scaling (requires restart)",
     "Size of icons and fonts displayed in the IDE (requires restart)",
     null, IconSet.values(), IconSet.WATERS_16);
  public static final MemoryOption GENERAL_HEAP_SIZE = new MemoryOption
    ("javaHeapSize", "Maximum heap memory (requires restart)",
     "Java heap size to be specific when launching the IDE the next time",
     null, null);
  public static final BooleanOption GENERAL_STUDENT_VERSION = new BooleanOption
    ("generalStudentVersion",
     "Student Verison", "Student version", null, false, false);
  public static final PositiveIntOption GUI_IDE_WIDTH = new PositiveIntOption
    ("ideFrameWidth",
     "IDE Width", "Width at which IDE opens", null, 1024, false);
  public static final PositiveIntOption GUI_IDE_HEIGHT = new PositiveIntOption
    ("ideFrameHeight",
     "IDE Height", "Height at which IDE opens", null, 768, false);
  public static final PositiveIntOption GUI_IDE_XPOS = new PositiveIntOption
    ("ideFrameX",
     "IDE X Position", "X position at which IDE opens", null, 0, false);
  public static final PositiveIntOption GUI_IDE_YPOS = new PositiveIntOption
    ("ideFrameY",
     "IDE Y Position", "Y position at which IDE opens", null, 0, false);
  public static final BooleanOption GUI_IDE_MAXIMIZED = new BooleanOption
    ("ideFrameMaximized",
     "Maximized Window", "Whether or not the IDE opens as a maximised window",
     null, false, false);


  // GENERAL_LOG
  public static final EnumOption<IDELogLevel> LOG_GUI_VERBOSITY = new EnumOption<>
  ("logLevelGUI", "Verbosity Level for GUI", "Verbosity level for graphical user interface",
    null, IDELogLevel.values(), IDELogLevel.INFO);
  public static final EnumOption<IDELogLevel> LOG_CONSOLE_VERBOSITY = new EnumOption<>
  ("logLevelConsole", "Verbosity Level for Console", "Verbosity level for console (stderr)",
    null, IDELogLevel.values(), IDELogLevel.NONE);
  public static final FileOption LOG_FILE = new FileOption
    ("logFileName", "Log File",
     "File to capture log output with log file verbosity", "-log",
     FileOption.Type.OUTPUT_FILE);
  public static final EnumOption<IDELogLevel> LOG_FILE_VERBOSITY = new EnumOption<>
  ("logLevelFile", "Verbosity Level for Log File", "Verbosity level for log file",
    null, IDELogLevel.values(), IDELogLevel.NONE);
  public static final BooleanOption GENERAL_REDIRECT_STDOUT = new BooleanOption
    ("generalRedirectStdout",
     "Capture stdout in GUI", "Capture stdout (System.out.println) in GUI", null, false);
  public static final BooleanOption GENERAL_REDIRECT_STDERR = new BooleanOption
    ("generalRedirectStderr",
     "Capture stderr in GUI", "Capture stderr (System.err.println) in GUI", null, false);


  // GENERAL_FILE
  public static final FileOption FILE_OPEN_PATH = new FileOption
    ("fileOpenPath", "Default File Open Path",
     "Default directory when opening modules and other input files",
     null, getHomeDirectory(), FileOption.Type.DIRECTORY);
  public static final FileOption FILE_SAVE_PATH = new FileOption
    ("fileSavePath", "Default File Save Path",
     "Default directory when creating log and other output files",
     null, getHomeDirectory(), FileOption.Type.DIRECTORY);


  //GUI_COMPILER
  //moved to net.sourceforge.waters.model.compiler.CompilerOptions


  //GUI_EDITOR
  public static final ColorOption GUI_EDITOR_BACKGROUND_COLOR = new ColorOption
    ("backgroundColor", "Automaton Background Colour",
     "Background colour of the graph editor when creating automata",
     null, Color.WHITE);
  public static final EnumOption<LayoutMode> GUI_EDITOR_LAYOUT_MODE = new EnumOption<>
    ("layoutMode", "Layout Mode",
      "General mode how automata are rendered in the graph editor",
     null, LayoutMode.values(), LayoutMode.Default);
  public static final BooleanOption GUI_EDITOR_DEFAULT_EMPTY_MODULE = new BooleanOption
    ("defaultEmptyModule", "Open with an Empty Module",
     "Open an empty module when launching Supremica without options", null, true);
  public static final BooleanOption GUI_EDITOR_SHOW_GRID = new BooleanOption
    ("showGrid",
     "Show Grid", "Show grid in graph editor", null, true);
  public static final PositiveIntOption GUI_EDITOR_GRID_SIZE = new PositiveIntOption
    ("gridSize",
     "Grid Size", "Grid size in graph editor", null, 16, 4, 64);
  public static final BooleanOption GUI_EDITOR_NODES_SNAP_TO_GRID = new BooleanOption
    ("nodesSnapToGrid",
     "Nodes Snap to Grid", "Nodes snap to grid", null, true);
  public static final PositiveIntOption GUI_EDITOR_NODE_RADIUS = new PositiveIntOption
    ("nodeRadius", "Node Size",
     "Radius of simple nodes in graph editor", null, 6, 4, 32);
  public static final BooleanOption GUI_EDITOR_STATE_NAMES_HIDDEN = new BooleanOption
    ("hideStateNames", "Suppress state names",
     "Do not display state names in graph editor", null, false);
  public static final BooleanOption GUI_EDITOR_CONTROL_POINTS_MOVE_WITH_NODE = new BooleanOption
    ("controlPointsMoveWithNode",
     "Control points move with node", "Control points move with node", null, true);
  public static final EnumOption<EdgeArrowPosition> GUI_EDITOR_EDGEARROW_POSITION = new EnumOption<>
    ("edgeArrowAtEnd", "Edge Arrow Position", "Edge arrow position",
     null, EdgeArrowPosition.values(), EdgeArrowPosition.End);
  public static final PositiveIntOption GUI_EDITOR_SPRING_EMBEDDER_TIMEOUT = new PositiveIntOption
    ("springEmbedderTimeout", "Maximum Layout Time",
     "Maximum time (in milliseconds) before stopping automatic graph layout", null, 10000);


  // GUI_ANALYZER
  public static final BooleanOption GUI_ANALYZER_INCLUDE_SEAMLESS_SYNTHESIS = new BooleanOption
    ("includeSeamlessSynthesis", "Include Seamless Synthesis",
     "Include symbolic synthesis option in editor's Analyze menu", null, true);
  public static final BooleanOption GUI_ANALYZER_INCLUDE_HISC = new BooleanOption
    ("includeHISC", "Include HISC Checks",
     "Add Hierachical Interface-Based Supervisory Coontrol (HISC) property checks " +
     "in editor's Verify menu", null, false);
  public static final BooleanOption GUI_ANALYZER_INCLUDE_SD = new BooleanOption
    ("includeSD", "Include Sampled-Data Checks",
     "Add sampled-data property checks in editor's Verify menu", null, false);
  public static final BooleanOption INCLUDE_WATERS_ANALYZER = new BooleanOption
    ("useWatersAnalyzer", "Use Waters Analyzer",
     "Analyzer tab uses new Waters analyzer instead of Supremica", null, false);


  // GUI_SIMULATOR
  public static final BooleanOption INCLUDE_ANIMATOR = new BooleanOption
    ("includeAnimator",
     "Include 2D Graphical Animator", "Include 2D Graphical Animator", null, false);
  public static final BooleanOption SIMULATION_IS_EXTERNAL = new BooleanOption
    ("simulationIsExternal",
     "External Simulation Process", "External simulation process", null, false);
  public static final PositiveIntOption SIMULATION_CYCLE_TIME = new PositiveIntOption
    ("simulationCycleTime",
     "Simulator Cycle Time", "Simulator cycle time in milliseconds", null, 100);


  // GUI_DOT
  public static final BooleanOption DOT_USE = new BooleanOption
    ("dotUse",
     "Use Dot", "Use dot", null, true);
  public static final StringOption DOT_EXECUTE_COMMAND = new StringOption
    ("dotExecuteCommand",
     "Dot Command", "Dot command", null, "dot");
  public static final PositiveIntOption DOT_MAX_NBR_OF_STATES = new PositiveIntOption
    ("dotMaxNbrOfStatesWithoutWarning",
     "Maximum Number of States", "Maximum number of states without warning", null, 100);
  public static final BooleanOption DOT_LEFT_TO_RIGHT = new BooleanOption
    ("dotLeftToRight",
     "Left to Right Layout", "Layout from left to right, otherwise from top to bottom", null, false);
  public static final BooleanOption DOT_WITH_STATE_LABELS = new BooleanOption
    ("dotWithStateLabels",
     "Draw State Names", "Draw state names", null, true);
  public static final BooleanOption DOT_WITH_EVENT_LABELS = new BooleanOption
    ("dotWithEventLabels",
     "Draw Event Labels", "Draw event labels", null, true);
  public static final BooleanOption DOT_WITH_CIRCLES = new BooleanOption
    ("dotWithCircles",
     "Circle State Names", "Draw circle around state names", null, false);
  public static final BooleanOption DOT_USE_STATE_COLORS = new BooleanOption
    ("dotUseStateColors",
     "Use Colors for States", "Use colors for states", null, true);
  public static final BooleanOption DOT_USE_ARC_COLORS = new BooleanOption
    ("dotUseArcColors",
     "Use Colors for Arcs", "Use colors for arcs", null, false);
  public static final BooleanOption DOT_USE_MULTI_LABELS = new BooleanOption
    ("dotUseMultiLabels",
     "Multiple Labels on Arc", "Draw multiple labels on one arc", null, true);
  public static final BooleanOption DOT_AUTOMATIC_UPDATE = new BooleanOption
    ("dotAutomaticUpdate",
     "Automatic Layout Update", "Do automatic update of the layout", null, true);

  // SUPREMICA_GENERAL
  public static final StringOption GENERAL_STATE_SEPARATOR = new StringOption
    ("generalStateSeparator",
     "State Separator Character", "State separator character", null, ".");
  public static final StringOption GENERAL_STATE_LABEL_SEPARATOR = new StringOption
    ("generalStateLabelSeparator",
     "State Label Separator Character", "State label separator character", null, ".");
  public static final BooleanOption GUI_ANALYZER_INCLUDE_OP = new BooleanOption
    ("includeOP", "Include Observer Projection Algorithms",
     "Include Observer Projection (OP) in Supremica analyzer", null, false) {
    {
      VerificationType.OP.setConfigOption(this);
    }
  };
  public static final BooleanOption GUI_ANALYZER_SEND_PROPERTIES_TO_ANALYZER = new BooleanOption
    ("guiAnalyzerSendPropertiesToAnalyzer", "Send properties to analyzer",
     "Display property automata from the editor in Supremica analyzer", null, false);
  public static final BooleanOption INCLUDE_BOUNDED_UNCON_TOOLS = new BooleanOption
    ("includeBoundedUnconTools",
     "Include Unbounded Controllability Tools", "Include unbounded controllability tools", null, false);
  public static final BooleanOption INCLUDE_EXPERIMENTAL_ALGORITHMS = new BooleanOption
    ("includeExperimentalAlgorithms",
     "Include Experimental Algorithms", "Include experimental algorithms", null, false) {
    {
      VerificationType.DIAGNOSABILITY.setConfigOption(this);
    }
  };

  // ALGORITHMS_SYNCHRONIZATION
  public static final BooleanOption SYNC_FORBID_UNCON_STATES = new BooleanOption
    ("syncForbidUncontrollableStates",
     "Forbid Uncontrollable States when Synchronizing", "Forbid uncontrollable states when synchronizing", null, true);
  public static final BooleanOption SYNC_EXPAND_FORBIDDEN_STATES = new BooleanOption
    ("syncExpandUncontrollableStates",
     "Expand Forbidden States when Synchronizing", "Expand forbidden states when synchronizing", null, true);
  public static final PositiveIntOption SYNC_INITIAL_HASHTABLE_SIZE = new PositiveIntOption
    ("syncInitialHashtableSize",
     "Initial Hash Table Size", "Initial hash table size", null, (1 << 14) - 1, 1, Integer.MAX_VALUE);
  public static final BooleanOption SYNC_EXPAND_HASHTABLE = new BooleanOption
    ("syncExpandHashtable",
     "Expand Hashtable", "Expand hashtable", null, true);
  public static final PositiveIntOption SYNC_NBR_OF_EXECUTERS = new PositiveIntOption
    ("synchNbrOfExecuters",
     "Number of Synchronization Threads", "Number of synchronization threads", null, 1, 1, Integer.MAX_VALUE);
  public static final StringOption SYNC_AUTOMATON_NAME_SEPARATOR = new StringOption
    ("synchAutomatonNameSeparator", "Automata Name Separator", "Automata name separator", null, "||");
  public static final BooleanOption SYNC_UNOBS_EVENTS_SYNC = new BooleanOption
    ("syncUnobsEventsSync",
     "Unobservable (non-tau) Events Synchronize", "Unobservable (non-tau) events synchronize", null, false);


  // ALGORITHMS_VERIFICATION
  public static final EnumOption<VerificationType> VERIFY_VERIFICATION_TYPE = new EnumOption<>
  ("verifyVerificationType", "Default Verification Type", "Default verification type",
    null, VerificationType.values(), VerificationType.CONTROLLABILITY);
  public static final EnumOption<VerificationAlgorithm> VERIFY_ALGORITHM_TYPE = new EnumOption<>
  ("verifyAlgorithmType", "Default Verification Algorithm", "Default verification algorithm",
    null, VerificationAlgorithm.values(), VerificationAlgorithm.MODULAR);
  public static final PositiveIntOption VERIFY_EXCLUSION_STATE_LIMIT = new PositiveIntOption
    ("verifyExclusionStateLimit",
     "Exclusion State Limit", "Exclusion state limit", null, 1000, 1, Integer.MAX_VALUE);
  public static final PositiveIntOption VERIFY_REACHABILITY_STATE_LIMIT = new PositiveIntOption
    ("verifyReachabilityStateLimit",
     "Reachability State Limit", "Reachability state limit", null, 1000, 1, Integer.MAX_VALUE);
  public static final BooleanOption VERIFY_ONE_EVENT_AT_A_TIME = new BooleanOption
    ("verifyOneEventAtATime",
     "Verify One Event at a Time", "Verify one event at a time", null, false);
  public static final BooleanOption VERIFY_SKIP_UNCONTROLLABILITY_CHECK = new BooleanOption
    ("skipUncontrollabilityCheck",
     "Skip Uncontrollability Check", "Skip uncontrollability check", null, false);
  public static final PositiveIntOption VERIFY_NBR_OF_ATTEMPTS = new PositiveIntOption
    ("nbrOfAttempts",
     "Number of Attempts", "Number of attempts", null, 5, 1, Integer.MAX_VALUE);
  public static final BooleanOption VERIFY_SHOW_BAD_TRACE = new BooleanOption
    ("showBadTrace", "Show trace to bad state",
     "Show counterexample as info in log", null, false);


  // ALGORITHMS_SYNTHESIS
  public static final EnumOption<SynthesisType> SYNTHESIS_SYNTHESIS_TYPE = new EnumOption<>
  ("synthesisSynthesisType", "Default Synthesis Type", "Default synthesis type",
    null, SynthesisType.values(), SynthesisType.NONBLOCKING_CONTROLLABLE);
  public static final EnumOption<SynthesisAlgorithm> SYNTHESIS_ALGORITHM_TYPE = new EnumOption<>
  ("synthesisAlgorithmType", "Default Synthesis Algorithm", "Default synthesis algorithm",
    null, SynthesisAlgorithm.values(), SynthesisAlgorithm.MONOLITHIC);
  public static final BooleanOption SYNTHESIS_PURGE = new BooleanOption
    ("synthesisPurge",
     "Remove Forbidden States After Synthesis", "Remove forbidden states after synthesis", null, true);
  public static final BooleanOption SYNTHESIS_RENAME = new BooleanOption
    ("synthesisRename",
     "Rename States to Generic Names (q0, q1, ...)", "Rename states to generic names (q0, q1, ...)", null, false);
  public static final BooleanOption SYNTHESIS_OPTIMIZE = new BooleanOption
    ("synthesisOptimize",
     "Try to Remove Supervisors that are not Necessary", "Try to remove supervisors that are not necessary", null, false);
  public static final BooleanOption SYNTHESIS_MAXIMALLY_PERMISSIVE = new BooleanOption
    ("synthesisMaximallyPermissive",
     "Synthesize a Maximally Permissive Supervisor", "Synthesize a maximally permissive supervisor", null, true);
  public static final BooleanOption SYNTHESIS_MAXIMALLY_PERMISSIVE_INCREMENTAL = new BooleanOption
    ("synthesisMaximallyPermissiveIncremental",
     "Use Incremental Algorithm when Synthesizing", "Use incremental algorithm when synthesizing", null, true);
  public static final BooleanOption SYNTHESIS_REDUCE_SUPERVISORS = new BooleanOption
    ("synthesisReduceSupervisors",
     "Try to Minimize Supervisors", "Try to minimize supervisors", null, false);
  public static final BooleanOption SYNTHESIS_LOCALIZE_SUPERVISORS = new BooleanOption
    ("synthesisLocalizeSupervisors",
     "Try to Localize Supervisors", "Try to localize supervisors", null, false);
  public static final BooleanOption SYNTHESIS_PRINT_GUARD = new BooleanOption
    ("generateGuard",
     "Generate Guards For the Controllable Events", "Generate guards for the controllable events", null, false);
  public static final BooleanOption SYNTHESIS_ADD_GUARDS = new BooleanOption
    ("addGuards",
     "Add the Guards to the Model", "Add the guards to the model", null, false);
  public static final BooleanOption SYNTHESIS_SAVE_IN_FILE = new BooleanOption
    ("saveInFile",
     "Save the Guard-Event Pairs in a File", "Save the guard-event pairs in a file", null, false);
  public static final BooleanOption SYNTHESIS_COMPLEMENT_HEURISTIC = new BooleanOption
    ("complementHeuristic",
     "Apply the Complement Heuristic", "Apply the complement heuristic", null, true);
  public static final BooleanOption SYNTHESIS_INDEPENDENT_HEURISTIC = new BooleanOption
    ("independentHeuristic",
     "Apply the Independent Heuristic", "Apply the independent heuristic", null, true);
  public static final BooleanOption SYNTHESIS_SAVE_IDD_IN_FILE = new BooleanOption
    ("saveIDDInFile",
     "Save the Guard-Event Pairs as an IDD in a File", "Save the guard-event pairs as an IDD in a file", null, false);
  public static final BooleanOption SYNTHESIS_REACHABILITY = new BooleanOption
    ("reachability",
     "Remove the Unreachable States", "Remove the unreachable states", null, false);
  public static final BooleanOption SYNTHESIS_OPTIMIZATION = new BooleanOption
    ("optimization",
     "Compute the Global Optimal Time", "Compute the global optimal time", null, false);
  public static final BooleanOption SYNTHESIS_SUP_AS_PLANT = new BooleanOption
    ("synthesisSupsAsPlants",
     "Consider Supervisors as Plants", "Consider supervisors as plants", null, false);


  // ALGORITHMS_MINIMIZATION
  public static final EnumOption<EquivalenceRelation> MINIMIZATION_EQUIVALENCE_RELATION = new EnumOption<>
  ("minimizationEquivalenceRelation", "Default Equivalence Relation", "Default equivalence relation",
    null, EquivalenceRelation.values(), EquivalenceRelation.LANGUAGEEQUIVALENCE);
  public static final BooleanOption MINIMIZATION_ALSO_MINIMIZE_TRANSITIONS = new BooleanOption
    ("minimizationAlsoMinimizeTransitions",
     "Minimize the Number of Transitions", "Minimize the number of transitions", null, true);
  public static final BooleanOption MINIMIZATION_KEEP_ORIGINAL = new BooleanOption
    ("minimizationKeepOriginal",
     "Keep Original", "Keep original", null, true);
  public static final BooleanOption MINIMIZATION_IGNORE_MARKING = new BooleanOption
    ("minimizationIgnoreMarking",
     "Ignore Marking", "Ignore marking", null, false);
  public static final EnumOption<MinimizationStrategy> MINIMIZATION_STRATEGY = new EnumOption<>
  ("minimizationStrategy", "Minimization Strategy", "Minimization strategy",
    null, MinimizationStrategy.values(), MinimizationStrategy.FewestTransitionsFirst);
  public static final EnumOption<MinimizationHeuristic> MINIMIZATION_HEURISTIC = new EnumOption<>
  ("minimizationHeuristic", "Minimization Heuristics", "Minimization heuristics",
    null, MinimizationHeuristic.values(), MinimizationHeuristic.MostLocal);
  public static final EnumOption<MinimizationPreselectingHeuristic> MINIMIZATION_PRESELECTING_HEURISTIC = new EnumOption<>
  ("minimizationPreselecting", "Minimization Preselecting Heuristics", "Minimization Preselecting Heuristics",
    null, MinimizationPreselectingHeuristic.values(), MinimizationPreselectingHeuristic.AtLeastOneLocalEvent);
  public static final EnumOption<MinimizationSelectingHeuristic> MINIMIZATION_SELECTING_HEURISTIC = new EnumOption<>
  ("minimizationSelectingHeuristic", "Minimization Selecting Heuristics", "Minimization Selecting Heuristics",
    null, MinimizationSelectingHeuristic.values(), MinimizationSelectingHeuristic.MinimumActualStates);
  public static final StringOption MINIMIZATION_SILENT_EVENT_NAME = new StringOption
    ("generalSilentEventName", "Silent Event Name", "Silent event name", null, "tau");
  public static final StringOption MINIMIZATION_SILENT_CONTROLLABLE_EVENT_NAME = new StringOption
    ("generalSilentControllableEventName", "Silent Controllable Event Name", "Silent controllable event name", null, "tau_c");
  public static final StringOption MINIMIZATION_SILENT_UNCONTROLLABLE_EVENT_NAME = new StringOption
    ("generalSilentUnontrollableEventName", "Silent Uncontrollable Event Name", "Silent uncontrollable event name", null, "tau_u");
  public static final BooleanOption MINIMIZATION_USE_TAU_EVENT_MAP = new BooleanOption
    ("minimizationUseTaueventMap",
     "Use Tau Event Map", "Use Tau Event Map", null, true);


  // ALGORITHMS_BDD
  public static final EnumOption<BDDPackage> BDD2_BDD_LIBRARY = new EnumOption<>
  ("libraryName", "BDD Library", "BDD Library",
    null, BDDPackage.values(), BDDPackage.JAVA);
  public static final PositiveIntOption BDD2_INITIAL_NODE_TABLE_SIZE = new PositiveIntOption
    ("initialNodeTableSize",
     "Initial Node Table Size", "Initial node table size", null, 1000000);
  public static final PositiveIntOption BDD2_CACHE_SIZE = new PositiveIntOption
    ("cacheSize",
     "Operation Cache Size", "Operation cache size", null, 100000);
  public static final PositiveIntOption BDD2_MAX_INCREASE_NODES = new PositiveIntOption
    ("maxIncreaseNodes",
     "Maximum Increase", "Maximum number of nodes by which to increase node table after garbage collection", null, 2500000);
  public static final DoubleOption BDD2_INCREASE_FACTOR = new DoubleOption
    ("increaseFactor",
     "Increase Factor", "Factor by which to increase node table after garbage collection", null,
     2.0, 0.0, Double.POSITIVE_INFINITY);
  public static final DoubleOption BDD2_CACHE_RATIO = new DoubleOption
    ("cacheRatio",
     "Cache Ratio for the Operator Caches", "Cache ratio for the operator caches (#tablenodes/#cachenodes)", null,
     10.0, 0.0, Double.POSITIVE_INFINITY);
  public static final EnumOption<BDDPartitioningType> BDD2_PARTITIONING = new EnumOption<>
  ("partitioning", "BDD Transition Partitioning", "BDD transition partitioning",
    null, BDDPartitioningType.values(), BDDPartitioningType.MONOLITHIC);
  public static final EnumOption<OrderingAlgorithm> BDD_ORDER_ALGO = new EnumOption<>
  ("bddAutomataOrderingAlgorithm", "Automata ordering algorithm", "Automata ordering algorithm",
    null, OrderingAlgorithm.values(), OrderingAlgorithm.AO_HEURISTIC_FORCE);
  public static final PositiveIntOption BDD_PARTITION_MAX = new PositiveIntOption
    ("bddMaxPartitionSize",
     "Max Partition Size", "Max partition size", null, 10000);
  public static final BooleanOption SYNTHESIS_PEAK_BDD = new BooleanOption
    ("peakBDD",
     "Compute and print peak BDD", "Compute and print peak BDD", null, false);
  public static final BooleanOption BDD_DEBUG_ON = new BooleanOption
    ("bddDebugOn",
     "Debug On", "Debug on", null, false);
  public static final BooleanOption BDD_PROFILE_ON = new BooleanOption
    ("bddProfileOn",
     "Profiling", "profiling", null, false);


  // MISC
  public static final BooleanOption TUM_EXTERNAL_ON = new BooleanOption
    ("tumExternalOn",
     "Activate TUM Options", "Activate TUM options", null, false);
  public static final BooleanOption INCLUDE_RAS_SUPPORT = new BooleanOption
    ("includeRASSupport",
     "Include RAS Support", "Include RAS support", null, false);
  public static final BooleanOption INCLUDE_FT_SUPPORT = new BooleanOption
    ("includeFTSupport", "Include FT Support",
     "Support for the XML fault tree format from SystemWaver", null, false);


  //#########################################################################
  //# Dummy Constructor to prevent instantiation
  private Config()
  {
  }


  //#########################################################################
  //# Auxiliary Methods
  private static File getHomeDirectory()
  {
    final String home = System.getProperty("user.home");
    if (home != null) {
      return new File(home);
    } else {
      return new File(".");
    }
  }

}
