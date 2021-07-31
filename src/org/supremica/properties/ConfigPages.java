package org.supremica.properties;

import static org.supremica.properties.Config.*;

import net.sourceforge.waters.model.options.AggregatorOptionPage;
import net.sourceforge.waters.model.options.SimpleLeafOptionPage;
import net.sourceforge.waters.model.options.WatersOptionPages;

public class ConfigPages
{

  public static final SimpleLeafOptionPage GENERAL =
    new SimpleLeafOptionPage("general", "General",
                             GUI_IDE_WIDTH, GUI_IDE_HEIGHT,
                             GUI_IDE_XPOS, GUI_IDE_YPOS,
                             GUI_IDE_MAXIMIZED,
                             GENERAL_LOOK_AND_FEEL,
                             GUI_EDITOR_ICONSET,
                             GENERAL_HEAP_SIZE,
                             GENERAL_STUDENT_VERSION);

  public static final SimpleLeafOptionPage GENERAL_LOG =
    new SimpleLeafOptionPage("general.log", "Log",
                             LOG_GUI_VERBOSITY, LOG_CONSOLE_VERBOSITY,
                             LOG_FILE, LOG_FILE_VERBOSITY,
                             GENERAL_REDIRECT_STDOUT,
                             GENERAL_REDIRECT_STDERR);

  public static final SimpleLeafOptionPage GENERAL_FILE =
    new SimpleLeafOptionPage("general.file", "File",
                             FILE_OPEN_PATH, FILE_SAVE_PATH);

  public static final SimpleLeafOptionPage GUI_EDITOR =
    new SimpleLeafOptionPage("gui.editor", "Editor",
                             GUI_EDITOR_BACKGROUND_COLOR,
                             GUI_EDITOR_BACKGROUND_COLOR,
                             GUI_EDITOR_LAYOUT_MODE,
                             GUI_EDITOR_DEFAULT_EMPTY_MODULE,
                             GUI_EDITOR_SHOW_GRID, GUI_EDITOR_GRID_SIZE,
                             GUI_EDITOR_NODES_SNAP_TO_GRID,
                             GUI_EDITOR_NODE_RADIUS,
                             GUI_EDITOR_STATE_NAMES_HIDDEN,
                             GUI_EDITOR_CONTROL_POINTS_MOVE_WITH_NODE,
                             GUI_EDITOR_SPRING_EMBEDDER_TIMEOUT,
                             GUI_EDITOR_EDGEARROW_POSITION);

  public static final SimpleLeafOptionPage ALGORITHMS_SYNTHESIS =
    new SimpleLeafOptionPage("algorithms.synthesis", "Synthesis",
                             SYNTHESIS_SYNTHESIS_TYPE,
                             SYNTHESIS_ALGORITHM_TYPE, SYNTHESIS_PURGE,
                             SYNTHESIS_RENAME, SYNTHESIS_OPTIMIZE,
                             SYNTHESIS_MAXIMALLY_PERMISSIVE,
                             SYNTHESIS_MAXIMALLY_PERMISSIVE_INCREMENTAL,
                             SYNTHESIS_REDUCE_SUPERVISORS,
                             SYNTHESIS_LOCALIZE_SUPERVISORS,
                             SYNTHESIS_PRINT_GUARD, SYNTHESIS_ADD_GUARDS,
                             SYNTHESIS_SAVE_IN_FILE,
                             SYNTHESIS_COMPLEMENT_HEURISTIC,
                             SYNTHESIS_INDEPENDENT_HEURISTIC,
                             SYNTHESIS_SAVE_IDD_IN_FILE,
                             SYNTHESIS_REACHABILITY,
                             SYNTHESIS_OPTIMIZATION,
                             SYNTHESIS_SUP_AS_PLANT);

  public static final SimpleLeafOptionPage GUI_ANALYZER =
    new SimpleLeafOptionPage("gui.analyzer", "Analyzer",
                             INCLUDE_WATERS_ANALYZER,
                             GUI_ANALYZER_INCLUDE_SEAMLESS_SYNTHESIS,
                             GUI_ANALYZER_INCLUDE_HISC,
                             GUI_ANALYZER_INCLUDE_SD);

  public static final SimpleLeafOptionPage GUI_SIMULATOR =
    new SimpleLeafOptionPage("gui.simulator", "Simulator",
                             INCLUDE_ANIMATOR, SIMULATION_IS_EXTERNAL,
                             SIMULATION_CYCLE_TIME);

  public static final SimpleLeafOptionPage GUI_DOT =
    new SimpleLeafOptionPage("gui.dot", "Dot",
                             DOT_USE, DOT_EXECUTE_COMMAND,
                             DOT_MAX_NBR_OF_STATES, DOT_LEFT_TO_RIGHT,
                             DOT_WITH_STATE_LABELS, DOT_WITH_EVENT_LABELS,
                             DOT_WITH_CIRCLES, DOT_USE_STATE_COLORS,
                             DOT_USE_ARC_COLORS, DOT_USE_MULTI_LABELS,
                             DOT_AUTOMATIC_UPDATE);

  public static final SimpleLeafOptionPage SUPREMICA_GENERAL =
    new SimpleLeafOptionPage("supremica.general", "General",
                             GENERAL_STATE_SEPARATOR,
                             GENERAL_STATE_LABEL_SEPARATOR,
                             GUI_ANALYZER_SEND_PROPERTIES_TO_ANALYZER,
                             GUI_ANALYZER_INCLUDE_OP,
                             INCLUDE_BOUNDED_UNCON_TOOLS,
                             INCLUDE_EXPERIMENTAL_ALGORITHMS);

  public static final SimpleLeafOptionPage ALGORITHMS_SYNCHRONIZATION =
    new SimpleLeafOptionPage("algorithms.synchronization", "Synchronization",
                             SYNC_FORBID_UNCON_STATES,
                             SYNC_EXPAND_FORBIDDEN_STATES,
                             SYNC_INITIAL_HASHTABLE_SIZE,
                             SYNC_EXPAND_HASHTABLE,
                             SYNC_NBR_OF_EXECUTERS,
                             SYNC_AUTOMATON_NAME_SEPARATOR,
                             SYNC_UNOBS_EVENTS_SYNC);

  public static final SimpleLeafOptionPage ALGORITHMS_VERIFICATION =
    new SimpleLeafOptionPage("algorithms.verification", "Verification",
                             VERIFY_VERIFICATION_TYPE, VERIFY_ALGORITHM_TYPE,
                             VERIFY_EXCLUSION_STATE_LIMIT,
                             VERIFY_REACHABILITY_STATE_LIMIT,
                             VERIFY_ONE_EVENT_AT_A_TIME,
                             VERIFY_SKIP_UNCONTROLLABILITY_CHECK,
                             VERIFY_NBR_OF_ATTEMPTS, VERIFY_SHOW_BAD_TRACE);

  public static final SimpleLeafOptionPage ALGORITHMS_MINIMIZATION =
    new SimpleLeafOptionPage("algorithms.minimization", "Minimization",
                             MINIMIZATION_EQUIVALENCE_RELATION,
                             MINIMIZATION_ALSO_MINIMIZE_TRANSITIONS,
                             MINIMIZATION_KEEP_ORIGINAL,
                             MINIMIZATION_IGNORE_MARKING,
                             MINIMIZATION_STRATEGY,
                             MINIMIZATION_HEURISTIC,
                             MINIMIZATION_PRESELECTING_HEURISTIC,
                             MINIMIZATION_SELECTING_HEURISTIC,
                             MINIMIZATION_SILENT_EVENT_NAME,
                             MINIMIZATION_SILENT_CONTROLLABLE_EVENT_NAME,
                             MINIMIZATION_SILENT_UNCONTROLLABLE_EVENT_NAME,
                             MINIMIZATION_USE_TAU_EVENT_MAP);

  public static final SimpleLeafOptionPage ALGORITHMS_BDD =
    new SimpleLeafOptionPage("algorithms.bdd", "BDD",
                             BDD2_BDD_LIBRARY, BDD2_INITIAL_NODE_TABLE_SIZE,
                             BDD2_CACHE_SIZE, BDD2_MAX_INCREASE_NODES,
                             BDD2_INCREASE_FACTOR, BDD2_CACHE_RATIO,
                             BDD2_PARTITIONING, BDD_ORDER_ALGO,
                             BDD_PARTITION_MAX, SYNTHESIS_PEAK_BDD,
                             BDD_DEBUG_ON, BDD_PROFILE_ON);

  public static final SimpleLeafOptionPage MISC =
    new SimpleLeafOptionPage("misc", "Misc",
                             TUM_EXTERNAL_ON, INCLUDE_RAS_SUPPORT,
                             INCLUDE_FT_SUPPORT);


  public static final AggregatorOptionPage IDE_AGGREGATOR_OPTION_PAGE =
    new AggregatorOptionPage("IDE", ConfigPages.GENERAL, ConfigPages.GENERAL_FILE, ConfigPages.GENERAL_LOG, ConfigPages.MISC);

  public static final AggregatorOptionPage GUI_AGGREGATOR_OPTION_PAGE =
    new AggregatorOptionPage("GUI", ConfigPages.GUI_EDITOR, ConfigPages.GUI_SIMULATOR,
                             ConfigPages.GUI_ANALYZER, WatersOptionPages.COMPILER);

  public static final AggregatorOptionPage ANALYZER_AGGREGATOR_OPTION_PAGE =
    new AggregatorOptionPage("Supremica Analyzer",
                             ConfigPages.SUPREMICA_GENERAL,
                             ConfigPages.ALGORITHMS_SYNCHRONIZATION,
                             ConfigPages.ALGORITHMS_VERIFICATION,
                             ConfigPages.ALGORITHMS_SYNTHESIS,
                             ConfigPages.ALGORITHMS_MINIMIZATION,
                             ConfigPages.ALGORITHMS_BDD, ConfigPages.GUI_DOT);

  public static final AggregatorOptionPage ROOT =
    new AggregatorOptionPage("Supremica",
                             IDE_AGGREGATOR_OPTION_PAGE,
                             GUI_AGGREGATOR_OPTION_PAGE,
                             WatersOptionPages.ANALYSIS,
                             ANALYZER_AGGREGATOR_OPTION_PAGE);

}
