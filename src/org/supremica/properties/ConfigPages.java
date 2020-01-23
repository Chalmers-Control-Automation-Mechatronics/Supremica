package org.supremica.properties;

import static org.supremica.properties.Config.*;

import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.analysis.options.AggregatorOptionPage;
import net.sourceforge.waters.analysis.options.DefaultLeafOptionPage;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.SimpleLeafOptionPage;

public class ConfigPages
{

  public static final SimpleLeafOptionPage GENERAL = new DefaultLeafOptionPage("general", "General") {
    @Override
    public List<Option<?>> getOptions()
    {
      return createOptionList
        (GUI_IDE_WIDTH, GUI_IDE_HEIGHT, GUI_IDE_XPOS, GUI_IDE_YPOS,
         GUI_IDE_MAXIMIZED,
         GENERAL_LOOK_AND_FEEL, GENERAL_STATE_SEPARATOR,
         GENERAL_STATE_LABEL_SEPARATOR, GENERAL_STUDENT_VERSION,
         INCLUDE_EXPERIMENTAL_ALGORITHMS);
    }
  };

  public static final SimpleLeafOptionPage GENERAL_LOG = new DefaultLeafOptionPage("general.log", "Log") {
    @Override
    public List<Option<?>> getOptions()
    {
      return createOptionList
        (LOG_GUI_VERBOSITY, LOG_CONSOLE_VERBOSITY, LOG_FILE,
         LOG_FILE_VERBOSITY, GENERAL_REDIRECT_STDOUT,
         GENERAL_REDIRECT_STDERR);
    }
  };

  public static final SimpleLeafOptionPage GENERAL_FILE = new DefaultLeafOptionPage("general.file", "File") {
    @Override
    public List<Option<?>> getOptions()
    {
      return createOptionList(FILE_OPEN_PATH, FILE_SAVE_PATH);
    }
  };

  public static final SimpleLeafOptionPage GUI_COMPILER = new DefaultLeafOptionPage("gui.compiler", "Compiler") {
    @Override
    public List<Option<?>> getOptions()
    {
      return createOptionList
        (INCLUDE_INSTANTIATION, BACKGROUND_COMPILER,
         OPTIMIZING_COMPILER, NORMALIZING_COMPILER,
         AUTOMATON_VARIABLES_COMPILER, INCLUDE_RAS_SUPPORT);
    }
  };

  public static final SimpleLeafOptionPage GUI_EDITOR = new DefaultLeafOptionPage("gui.editor", "Editor") {
    @Override
    public List<Option<?>> getOptions()
    {
      return createOptionList
        (GUI_EDITOR_ICONSET, GUI_EDITOR_BACKGROUND_COLOR,
         GUI_EDITOR_BACKGROUND_COLOR, GUI_EDITOR_LAYOUT_MODE,
         GUI_EDITOR_DEFAULT_EMPTY_MODULE,
         GUI_EDITOR_SHOW_GRID, GUI_EDITOR_GRID_SIZE,
         GUI_EDITOR_NODES_SNAP_TO_GRID, GUI_EDITOR_NODE_RADIUS,
         GUI_EDITOR_STATE_NAMES_HIDDEN,
         GUI_EDITOR_CONTROL_POINTS_MOVE_WITH_NODE,
         GUI_EDITOR_SPRING_EMBEDDER_TIMEOUT, GUI_EDITOR_EDGEARROW_POSITION);
    }
  };

  public static final SimpleLeafOptionPage ALGORITHMS_SYNTHESIS = new DefaultLeafOptionPage("algorithms.synthesis", "Synthesis") {
    @Override
    public List<Option<?>> getOptions()
    {
      return createOptionList
        (SYNTHESIS_SYNTHESIS_TYPE, SYNTHESIS_ALGORITHM_TYPE, SYNTHESIS_PURGE,
         SYNTHESIS_RENAME, SYNTHESIS_OPTIMIZE,
         SYNTHESIS_MAXIMALLY_PERMISSIVE,
         SYNTHESIS_MAXIMALLY_PERMISSIVE_INCREMENTAL,
         SYNTHESIS_REDUCE_SUPERVISORS, SYNTHESIS_LOCALIZE_SUPERVISORS,
         SYNTHESIS_PRINT_GUARD, SYNTHESIS_ADD_GUARDS, SYNTHESIS_SAVE_IN_FILE,
         SYNTHESIS_COMPLEMENT_HEURISTIC, SYNTHESIS_INDEPENDENT_HEURISTIC,
         SYNTHESIS_SAVE_IDD_IN_FILE, SYNTHESIS_REACHABILITY,
         SYNTHESIS_OPTIMIZATION, SYNTHESIS_SUP_AS_PLANT);
    }
  };

  public static final SimpleLeafOptionPage GUI_ANALYZER = new DefaultLeafOptionPage("gui.analyzer", "Analyzer") {
    @Override
    public List<Option<?>> getOptions()
    {
      return createOptionList
        (GUI_ANALYZER_INCLUDE_SEAMLESS_SYNTHESIS,
         GUI_ANALYZER_INCLUDE_DIAGNOSABILIY, GUI_ANALYZER_INCLUDE_HISC,
         GUI_ANALYZER_INCLUDE_SD, GUI_ANALYZER_INCLUDE_OP,
         GUI_ANALYZER_SEND_PROPERTIES_TO_ANALYZER,
         GUI_ANALYZER_AUTOMATON_VIEWER_USE_CONTROLLED_SURFACE,
         INCLUDE_BOUNDED_UNCON_TOOLS, INCLUDE_WATERS_ANALYZER);
    }
  };

  public static final SimpleLeafOptionPage GUI_SIMULATOR = new DefaultLeafOptionPage("gui.simulator", "Simulator") {
    @Override
    public List<Option<?>> getOptions()
    {
      return createOptionList(INCLUDE_ANIMATOR, SIMULATION_IS_EXTERNAL,
                              SIMULATION_CYCLE_TIME);
    }
  };

  public static final SimpleLeafOptionPage GUI_DOT = new DefaultLeafOptionPage("gui.dot", "Dot") {
    @Override
    public List<Option<?>> getOptions()
    {
      return createOptionList
        (DOT_USE, DOT_EXECUTE_COMMAND, DOT_MAX_NBR_OF_STATES,
         DOT_LEFT_TO_RIGHT, DOT_WITH_STATE_LABELS, DOT_WITH_EVENT_LABELS,
         DOT_WITH_CIRCLES, DOT_USE_STATE_COLORS, DOT_USE_ARC_COLORS,
         DOT_USE_MULTI_LABELS, DOT_AUTOMATIC_UPDATE);
    }
  };

  public static final SimpleLeafOptionPage ALGORITHMS_SYNCHRONIZATION = new DefaultLeafOptionPage("algorithms.synchronization", "Synchronization") {
    @Override
    public List<Option<?>> getOptions()
    {
      return createOptionList
        (SYNC_FORBID_UNCON_STATES, SYNC_EXPAND_FORBIDDEN_STATES,
         SYNC_INITIAL_HASHTABLE_SIZE, SYNC_EXPAND_HASHTABLE,
         SYNC_NBR_OF_EXECUTERS, SYNC_AUTOMATON_NAME_SEPARATOR,
         SYNC_UNOBS_EVENTS_SYNC);
    }
  };

  public static final SimpleLeafOptionPage ALGORITHMS_VERIFICATION = new DefaultLeafOptionPage("algorithms.verification", "Verification") {
    @Override
    public List<Option<?>> getOptions()
    {
      return createOptionList
        (VERIFY_VERIFICATION_TYPE, VERIFY_ALGORITHM_TYPE,
         VERIFY_EXCLUSION_STATE_LIMIT, VERIFY_REACHABILITY_STATE_LIMIT,
         VERIFY_ONE_EVENT_AT_A_TIME, VERIFY_SKIP_UNCONTROLLABILITY_CHECK,
         VERIFY_NBR_OF_ATTEMPTS, VERIFY_SHOW_BAD_TRACE);
    }
  };

  public static final SimpleLeafOptionPage ALGORITHMS_MINIMIZATION = new DefaultLeafOptionPage("algorithms.minimization", "Minimization") {
    @Override
    public List<Option<?>> getOptions()
    {
      return createOptionList
        (MINIMIZATION_EQUIVALENCE_RELATION,
         MINIMIZATION_ALSO_MINIMIZE_TRANSITIONS, MINIMIZATION_KEEP_ORIGINAL,
         MINIMIZATION_IGNORE_MARKING, MINIMIZATION_STRATEGY,
         MINIMIZATION_HEURISTIC, MINIMIZATION_PRESELECTING_HEURISTIC,
         MINIMIZATION_SELECTING_HEURISTIC, MINIMIZATION_SILENT_EVENT_NAME,
         MINIMIZATION_SILENT_CONTROLLABLE_EVENT_NAME,
         MINIMIZATION_SILENT_UNCONTROLLABLE_EVENT_NAME,
         MINIMIZATION_USE_TAU_EVENT_MAP);
    }
  };

  public static final SimpleLeafOptionPage ALGORITHMS_BDD = new DefaultLeafOptionPage("algorithms.bdd", "BDD") {
    @Override
    public List<Option<?>> getOptions()
    {
      return createOptionList
        (BDD2_BDD_LIBRARY, BDD2_INITIAL_NODE_TABLE_SIZE,
         BDD2_CACHE_SIZE, BDD2_MAX_INCREASE_NODES,
         BDD2_INCREASE_FACTOR, BDD2_CACHE_RATIO,
         BDD2_PARTITIONING, BDD_ORDER_ALGO, BDD_PARTITION_MAX,
         SYNTHESIS_PEAK_BDD, BDD_DEBUG_ON, BDD_PROFILE_ON);
    }
  };

  public static final SimpleLeafOptionPage ALGORITHMS_HMI = new DefaultLeafOptionPage("algorithms.hmi", "HMI") {
    @Override
    public List<Option<?>> getOptions()
    {
      return createOptionList(INCLUDE_USER_INTERFACE,
                              EXPAND_EXTENDED_AUTOMATA);
    }
  };

  public static final SimpleLeafOptionPage MISC = new DefaultLeafOptionPage("misc", "Misc") {
    @Override
    public List<Option<?>> getOptions()
    {
      return createOptionList(TUM_EXTERNAL_ON);
    }
  };

  public static final AggregatorOptionPage IDE_AGGREGATOR_OPTION_PAGE =
    new AggregatorOptionPage("IDE", ConfigPages.GENERAL, ConfigPages.GENERAL_FILE, ConfigPages.GENERAL_LOG, ConfigPages.MISC);

  public static final AggregatorOptionPage GUI_AGGREGATOR_OPTION_PAGE =
    new AggregatorOptionPage("GUI", ConfigPages.GUI_EDITOR, ConfigPages.GUI_SIMULATOR, ConfigPages.GUI_ANALYZER, ConfigPages.GUI_COMPILER);

  public static final AggregatorOptionPage ANALYZER_AGGREGATOR_OPTION_PAGE =
    new AggregatorOptionPage("Supremica Analyzer", ConfigPages.ALGORITHMS_SYNCHRONIZATION,
                             ConfigPages.ALGORITHMS_VERIFICATION, ConfigPages.ALGORITHMS_SYNTHESIS,
                             ConfigPages.ALGORITHMS_MINIMIZATION,
                             ConfigPages.ALGORITHMS_BDD, ConfigPages.ALGORITHMS_HMI, ConfigPages.GUI_DOT);

  public static List<Option<?>> createOptionList(final Option<?>...options) {
    final List<Option<?>> list = new LinkedList<>();
    for (final Option<?> option : options) {
      list.add(option);
    }
    return list;
  }

}
