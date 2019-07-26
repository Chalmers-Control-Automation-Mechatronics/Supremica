//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.properties
//# CLASS:   Config
//###########################################################################
//# $Id: c9f343494bf80e91ad9f70d162e2394bbd7c39e8 $
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

import java.awt.Color;

import net.sourceforge.waters.analysis.bdd.BDDPackage;
import net.sourceforge.waters.gui.logging.IDELogLevel;
import net.sourceforge.waters.gui.renderer.EdgeArrowPosition;
import net.sourceforge.waters.gui.renderer.LayoutMode;
import net.sourceforge.waters.gui.util.IconSet;
import net.sourceforge.waters.gui.util.LookAndFeelOption;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;

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
import org.supremica.util.BDD.Options;


/**
 * <P>Configurable Options.</P>
 *
 * <P>All static variables declared in this class are automatically
 * registered by their constructors for loading, saving, and editing.
 * They are initialised on startup of the IDE by reading a properties file
 * whose name is specified with the <CODE>-p</CODE> on the command line.
 * Without the option, the properties are initialised to their hard-coded
 * default values. When the properties dialog is opened, it allows the user
 * to change all registered properties, and they are automatically saved to
 * the properties file (if set) when they dialog is closed.</P>
 *
 * <P>The default for the properties file is <CODE>.supremica</CODE> in the
 * Supremica source directory, when launched from Eclipse. When launched
 * from the Waters startuo scripts, it is <CODE>.waters</CODE> in the user's
 * home directory for Linux or <CODE>waters.properties</CODE> in the user's
 * home directory for Windows.</P>
 *
 * @author Knut &Aring;kesson
 */
public final class Config
{
    // Valid PropertyTypes (see PropertyType.java):
    //   GENERAL
    //   GENERAL_LOG
    //   GENERAL_FILE
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

    // GUI_DOT
    public static final BooleanProperty DOT_USE = new BooleanProperty(PropertyType.GUI_DOT, "dotUse", true, "Use Dot");
    public static final ObjectProperty<String> DOT_EXECUTE_COMMAND = new ObjectProperty<String>(PropertyType.GUI_DOT, "dotExecuteCommand", "dot", "Dot command");
    public static final IntegerProperty DOT_MAX_NBR_OF_STATES =
      new IntegerProperty(PropertyType.GUI_DOT, "dotMaxNbrOfStatesWithoutWarning",
                          100, "Max number of states without warning", true, 0);
    public static final BooleanProperty DOT_LEFT_TO_RIGHT = new BooleanProperty(PropertyType.GUI_DOT, "dotLeftToRight", false, "Layout from left to right, otherwise from top to bottom");
    public static final BooleanProperty DOT_WITH_STATE_LABELS = new BooleanProperty(PropertyType.GUI_DOT, "dotWithStateLabels", true, "Draw state names");
    public static final BooleanProperty DOT_WITH_EVENT_LABELS = new BooleanProperty(PropertyType.GUI_DOT, "dotWithEventLabels", true, "Draw event labels");
    public static final BooleanProperty DOT_WITH_CIRCLES = new BooleanProperty(PropertyType.GUI_DOT, "dotWithCircles", false, "Draw circle around state names");
    public static final BooleanProperty DOT_USE_STATE_COLORS = new BooleanProperty(PropertyType.GUI_DOT, "dotUseStateColors", true, "Use colors for states");
    public static final BooleanProperty DOT_USE_ARC_COLORS = new BooleanProperty(PropertyType.GUI_DOT, "dotUseArcColors", false, "Use colors for arcs");
    public static final BooleanProperty DOT_USE_MULTI_LABELS = new BooleanProperty(PropertyType.GUI_DOT, "dotUseMultiLabels", true, "Draw multiple labels on one arc");
    public static final BooleanProperty DOT_AUTOMATIC_UPDATE = new BooleanProperty(PropertyType.GUI_DOT, "dotAutomaticUpdate", true, "Do automatic update of the layout");

    // GENERAL
    public static final ObjectProperty<LookAndFeelOption> GENERAL_LOOKANDFEEL =
      new ObjectProperty<>(PropertyType.GENERAL, "javaLookAndFeel",
                           LookAndFeelOption.DEFAULT, LookAndFeelOption.class,
                           "Java Look&Feel (requires restart)");
    public static final ObjectProperty<String> GENERAL_STATE_SEPARATOR  = new ObjectProperty<String>(PropertyType.GENERAL, "generalStateSeparator", ".", "State separator character");
    public static final ObjectProperty<String> GENERAL_STATELABEL_SEPARATOR  = new ObjectProperty<String>(PropertyType.GENERAL, "generalStateLabelSeparator", ",", "State label separator character");
    public static final BooleanProperty GENERAL_USE_SECURITY = new BooleanProperty(PropertyType.GENERAL, "generalUseSecurity", false, "Use file security");
    public static final BooleanProperty GENERAL_STUDENT_VERSION =
      new BooleanProperty(PropertyType.GENERAL, "generalStudentVersion",
                          false, "Student version", false);
    public static final BooleanProperty INCLUDE_EXPERIMENTAL_ALGORITHMS = new BooleanProperty(PropertyType.GENERAL, "includeExperimentalAlgorithms", false, "Include experimental algorithms (requires restart)");

    // GENERAL_LOG
    public static final ObjectProperty<IDELogLevel> LOG_GUI_VERBOSITY =
      new ObjectProperty<>(PropertyType.GENERAL_LOG, "logLevelGUI",
                           IDELogLevel.INFO,
                           IDELogLevel.getAllowedValuesForLogPanel(),
                           IDELogLevel.class,
                           "Verbosity level for graphical user interface",
                           true);
    public static final ObjectProperty<IDELogLevel> LOG_CONSOLE_VERBOSITY =
      new ObjectProperty<>(PropertyType.GENERAL_LOG, "logLevelConsole",
                           IDELogLevel.NONE, IDELogLevel.class,
                           "Verbosity level for console (stderr)");
    public static final ObjectProperty<String> LOG_FILE =
      new ObjectProperty<String>(PropertyType.GENERAL_LOG, "logFileName",
                                 "", "Log file");
    public static final ObjectProperty<IDELogLevel> LOG_FILE_VERBOSITY =
      new ObjectProperty<>(PropertyType.GENERAL_LOG, "logLevelFile",
                           IDELogLevel.NONE, IDELogLevel.class,
                           "Verbosity level for log file");
    public static final BooleanProperty GENERAL_REDIRECT_STDOUT =
      new BooleanProperty(PropertyType.GENERAL_LOG, "generalRedirectStdout",
                          false, "Capture stdout (System.out.println) in GUI ");
    public static final BooleanProperty GENERAL_REDIRECT_STDERR =
      new BooleanProperty(PropertyType.GENERAL_LOG, "generalRedirectStderr",
                          false, "Capture stderr (System.err.println) in GUI ");

    // GENERAL_FILE
    public static final ObjectProperty<String> FILE_OPEN_PATH = new ObjectProperty<String>(PropertyType.GENERAL_FILE, "fileOpenPath", System.getProperty("user.home"), "Default file open path");
    public static final ObjectProperty<String> FILE_SAVE_PATH = new ObjectProperty<String>(PropertyType.GENERAL_FILE, "fileSavePath", System.getProperty("user.home"), "Default file save path");
    public static final BooleanProperty FILE_ALLOW_OPEN = new BooleanProperty(PropertyType.GENERAL_FILE, "fileAllowOpen", true, "Allow user to open file");
    public static final BooleanProperty FILE_ALLOW_SAVE = new BooleanProperty(PropertyType.GENERAL_FILE, "fileAllowSave", true, "Allow user to save file");
    public static final BooleanProperty FILE_ALLOW_IMPORT = new BooleanProperty(PropertyType.GENERAL_FILE, "fileAllowImport", true, "Allow user to import file");
    public static final BooleanProperty FILE_ALLOW_EXPORT = new BooleanProperty(PropertyType.GENERAL_FILE, "fileAllowExport", true, "Allow user to export file");
    public static final BooleanProperty FILE_ALLOW_QUIT = new BooleanProperty(PropertyType.GENERAL_FILE, "fileAllowQuit", true, "Allow user to quit Supremica");

    // GUI
    public static IntegerProperty GUI_IDE_WIDTH =
      new IntegerProperty(PropertyType.GUI, "ideFrameWidth",
                          1024, "Width at which IDE opens", false, 0);
    public static IntegerProperty GUI_IDE_HEIGHT =
      new IntegerProperty(PropertyType.GUI, "ideFrameHeight",
                          768, "Height at which IDE opens", false, 0);
    public static IntegerProperty GUI_IDE_XPOS =
      new IntegerProperty(PropertyType.GUI, "ideFrameX",
                          0, "X position at which IDE opens", false, 0);
    public static IntegerProperty GUI_IDE_YPOS =
      new IntegerProperty(PropertyType.GUI, "ideFrameY",
                          0, "Y position at which IDE opens", false, 0);
    public static BooleanProperty GUI_IDE_MAXIMIZED =
      new BooleanProperty(PropertyType.GUI, "ideFrameMaximized", false,
                          "Whether or not the IDE opens as a maximised window",
                          false);

    public static final BooleanProperty INCLUDE_EXTERNALTOOLS = new BooleanProperty(PropertyType.GUI, "includeExternalTools", true, "Include external tools");
    public static final BooleanProperty INCLUDE_INSTANTION =
      new BooleanProperty(PropertyType.GUI, "includeInstantiation", true,
			  "Enable instantiation and other advanced features");
    public static final BooleanProperty BACKGROUND_COMPILER =
      new BooleanProperty(PropertyType.GUI, "backgroundCompiler", true,
              "Compile automatically while editing");
    public static final BooleanProperty OPTIMIZING_COMPILER =
      new BooleanProperty(PropertyType.GUI, "optimizingCompiler", false,
              "Remove redundant events, transitions, and components " +
              "when compiling");
    public static final BooleanProperty NORMALIZING_COMPILER =
      new BooleanProperty(PropertyType.GUI, "normalizingCompiler", false,
              "Use normalising EFSM compiler");
    public static final BooleanProperty AUTOMATON_VARIABLES_COMPILER =
      new BooleanProperty(PropertyType.GUI, "automatonVariablesCompiler", false,
              "Allow automaton names in EFSM guards");
    public static final BooleanProperty INCLUDE_RAS_SUPPORT =
      new BooleanProperty(PropertyType.GUI, "includeRASSupport", false,
              "Include RAS support");

    // GUI_EDITOR
    public static final ObjectProperty<IconSet> GUI_EDITOR_ICONSET =
      new ObjectProperty<>(PropertyType.GUI_EDITOR, "iconSet",
                           IconSet.WATERS_16, IconSet.class,
                           "Icon set and font scaling (requires restart)");
    public static final ColorProperty GUI_EDITOR_BACKGROUND_COLOR =
      new ColorProperty(PropertyType.GUI_EDITOR, "backgroundColor",
                        Color.WHITE, "Automaton background colour");
    public static final ObjectProperty<LayoutMode> GUI_EDITOR_LAYOUT_MODE =
      new ObjectProperty<>(PropertyType.GUI_EDITOR, "layoutMode",
                           LayoutMode.Default, LayoutMode.class,
                           "Layout mode");
    public static final BooleanProperty GUI_EDITOR_DEFAULT_EMPTY_MODULE =
      new BooleanProperty(PropertyType.GUI_EDITOR, "defaultEmptyModule",
                          true, "Open with an empty module");
    public static final BooleanProperty GUI_EDITOR_SHOW_GRID =
      new BooleanProperty(PropertyType.GUI_EDITOR, "showGrid",
                          true, "Show grid");
    public static final IntegerProperty GUI_EDITOR_GRID_SIZE =
      new IntegerProperty(PropertyType.GUI_EDITOR, "gridSize",
                          16, "Grid size", true, 4, 64, 4);
    public static final BooleanProperty GUI_EDITOR_NODES_SNAP_TO_GRID =
      new BooleanProperty(PropertyType.GUI_EDITOR, "nodesSnapToGrid",
                          true, "Nodes snap to grid");
    public static final IntegerProperty GUI_EDITOR_NODE_RADIUS =
      new IntegerProperty(PropertyType.GUI_EDITOR, "nodeRadius", 6,
                          "Node size", true, 4, 32, 1);
    public static final BooleanProperty GUI_EDITOR_STATE_NAMES_HIDDEN =
      new BooleanProperty(PropertyType.GUI_EDITOR, "hideStateNames",
                          false, "Suppress state names", false);
    public static final BooleanProperty GUI_EDITOR_CONTROL_POINTS_MOVE_WITH_NODE =
      new BooleanProperty(PropertyType.GUI_EDITOR, "controlPointsMoveWithNode",
                          true, "Control points move with node");
    public static final EnumProperty<EdgeArrowPosition> GUI_EDITOR_EDGEARROW_POSITION =
      new EnumProperty<>(PropertyType.GUI_EDITOR, "edgeArrowAtEnd",
                         EdgeArrowPosition.End,
                         EdgeArrowPosition.End,
                         EdgeArrowPosition.Middle,
                         EdgeArrowPosition.class,
                         "Edge arrow position");
    public static final IntegerProperty GUI_EDITOR_SPRING_EMBEDDER_TIMEOUT =
      new IntegerProperty(PropertyType.GUI_EDITOR, "springEmbedderTimeout",
                          10000, "Maximum layout time", true, 0);

    // GUI_ANALYZER
    public static final ObjectProperty<ModelAnalyzerFactoryLoader> GUI_ANALYZER_USED_FACTORY =
      new ObjectProperty<>(PropertyType.GUI_ANALYZER, "guiAnalyzerUsedFactory",
                           ModelAnalyzerFactoryLoader.Monolithic,
                           ModelAnalyzerFactoryLoader.class,
                           "Model verifier factory used by Editor's Verify menu");
    public static final BooleanProperty GUI_ANALYZER_INCLUDE_SEAMLESS_SYNTHESIS =
      new BooleanProperty(PropertyType.GUI_ANALYZER, "includeSeamlessSynthesis",
                          true, "Include Seamless Synthesis");
    public static final BooleanProperty GUI_ANALYZER_INCLUDE_DIAGNOSABILIY =
      new BooleanProperty(PropertyType.GUI_ANALYZER, "includeDiagnosability", false,
                          "Include diagnosability check");
    public static final BooleanProperty GUI_ANALYZER_INCLUDE_HISC =
      new BooleanProperty(PropertyType.GUI_ANALYZER, "includeHISC", false,
                          "Include HISC property checks");
    public static final BooleanProperty GUI_ANALYZER_INCLUDE_SD =
      new BooleanProperty(PropertyType.GUI_ANALYZER, "includeSD", false,
                          "Include sampled-data property checks");
    public static final BooleanProperty GUI_ANALYZER_INCLUDE_OP =
      new BooleanProperty(PropertyType.GUI_ANALYZER, "includeOP", false,
                          "Include Observer Projection algorithms");

    public static final BooleanProperty GUI_ANALYZER_SEND_PROPERTIES_TO_ANALYZER =
      new BooleanProperty(PropertyType.GUI_ANALYZER,
                          "guiAnalyzerSendPropertiesToAnalyzer", false,
                          "Send properties to analyzer");
    public static final BooleanProperty GUI_ANALYZER_AUTOMATONVIEWER_USE_CONTROLLED_SURFACE =
      new BooleanProperty(PropertyType.GUI_ANALYZER,
                          "automatonViewerUseControlledSurface", false,
                          "Use new controlled surface panel to display an automaton");
    public static final BooleanProperty INCLUDE_BOUNDED_UNCON_TOOLS =
      new BooleanProperty(PropertyType.GUI_ANALYZER,
                          "includeBoundedUnconTools", false,
                          "Include unbounded controllability tools");
    public static final BooleanProperty INCLUDE_WATERS_ANALYZER =
      new BooleanProperty(PropertyType.GUI_ANALYZER,
                          "useWatersAnalyzer", false,
                          "Use Waters Analyzer");

    // GUI_SIMULATOR
    public static final BooleanProperty INCLUDE_WATERS_SIMULATOR =
      new BooleanProperty(PropertyType.GUI_SIMULATOR, "includeWatersSimulator",
                          true, "Include Waters Simulator");
    public static final BooleanProperty INCLUDE_ANIMATOR = new BooleanProperty(PropertyType.GUI_SIMULATOR, "includeAnimator", false, "Include 2D Graphical Animator");
    public static final BooleanProperty SIMULATION_IS_EXTERNAL = new BooleanProperty(PropertyType.GUI_SIMULATOR, "simulationIsExternal", false, "External simulation process");
    public static final IntegerProperty SIMULATION_CYCLE_TIME =
      new IntegerProperty(PropertyType.GUI_SIMULATOR, "simulationCycleTime",
                          100, "Simulator Cycle time (ms)", true, 0);


    // ALGORITHMS_SYNCHRONIZATION
    public static final BooleanProperty SYNC_FORBID_UNCON_STATES = new BooleanProperty(PropertyType.ALGORITHMS_SYNCHRONIZATION, "syncForbidUncontrollableStates", true, "Forbid uncontrollable states when synchronizing");
    public static final BooleanProperty SYNC_EXPAND_FORBIDDEN_STATES = new BooleanProperty(PropertyType.ALGORITHMS_SYNCHRONIZATION, "syncExpandUncontrollableStates", true, "Expand forbidden states when synchronizing");
    public static final IntegerProperty SYNC_INITIAL_HASHTABLE_SIZE =
      new IntegerProperty(PropertyType.ALGORITHMS_SYNCHRONIZATION,
                          "syncInitialHashtableSize", (1 << 14) - 1,
                          "Initial hash table size", true, 1);
    public static final BooleanProperty SYNC_EXPAND_HASHTABLE = new BooleanProperty(PropertyType.ALGORITHMS_SYNCHRONIZATION, "syncExpandHashtable", true, "Expand hashtable");
    public static final IntegerProperty SYNC_NBR_OF_EXECUTERS =
      new IntegerProperty(PropertyType.ALGORITHMS_SYNCHRONIZATION,
                          "synchNbrOfExecuters", 1,
                          "Number of synchronization threads", true, 1);
    public static final ObjectProperty<String> SYNC_AUTOMATON_NAME_SEPARATOR = new ObjectProperty<String>(PropertyType.ALGORITHMS_SYNCHRONIZATION, "synchAutomatonNameSeparator", "||", "Automata name separator");
	public static final BooleanProperty SYNC_UNOBS_EVENTS_SYNC = new BooleanProperty(PropertyType.ALGORITHMS_SYNCHRONIZATION, "syncUnobsEventsSync", false, "Unobservable (non-tau) events synchronize");

    // ALGORITHMS_VERIFICATION
    public static final ObjectProperty<VerificationType> VERIFY_VERIFICATION_TYPE =
      new ObjectProperty<>(PropertyType.ALGORITHMS_VERIFICATION,
                           "verifyVerificationType",
                           VerificationType.CONTROLLABILITY,
                           VerificationType.class,
                           "Default verificaton type");
    public static final ObjectProperty<VerificationAlgorithm> VERIFY_ALGORITHM_TYPE =
      new ObjectProperty<>(PropertyType.ALGORITHMS_VERIFICATION,
                           "verifyAlgorithmType",
                           VerificationAlgorithm.MODULAR,
                           VerificationAlgorithm.class,
                           "Default verificaton algorithm");
    public static final IntegerProperty VERIFY_EXCLUSION_STATE_LIMIT =
      new IntegerProperty(PropertyType.ALGORITHMS_VERIFICATION,
                          "verifyExclusionStateLimit", 1000,
                          "Exclusion state limit", true, 1);
    public static final IntegerProperty VERIFY_REACHABILITY_STATE_LIMIT =
      new IntegerProperty(PropertyType.ALGORITHMS_VERIFICATION,
                          "verifyReachabilityStateLimit", 1000,
                          "Reachability state limit", true, 1);
    public static final BooleanProperty VERIFY_ONE_EVENT_AT_A_TIME = new BooleanProperty(PropertyType.ALGORITHMS_VERIFICATION, "verifyOneEventAtATime", false, "Verify one event at a time");
    public static final BooleanProperty VERIFY_SKIP_UNCONTROLLABILITY_CHECK = new BooleanProperty(PropertyType.ALGORITHMS_VERIFICATION, "skipUncontrollabilityCheck", false, "Skip uncontrollability check");
    public static final IntegerProperty VERIFY_NBR_OF_ATTEMPTS =
      new IntegerProperty(PropertyType.ALGORITHMS_VERIFICATION, "nbrOfAttempts",
                          5, "Number of attempts", true, 1);
    public static final BooleanProperty VERIFY_SHOW_BAD_TRACE = new BooleanProperty(PropertyType.ALGORITHMS_VERIFICATION, "showBadTrace", false, "Show trace to bad state");

    // ALGORITHMS_SYNTHESIS
    public static final ObjectProperty<SynthesisType> SYNTHESIS_SYNTHESIS_TYPE =
      new ObjectProperty<>(PropertyType.ALGORITHMS_SYNTHESIS,
                           "synthesisSynthesisType",
                           SynthesisType.NONBLOCKING_CONTROLLABLE,
                           SynthesisType.class,
                           "Default synthesis type");
    public static final ObjectProperty<SynthesisAlgorithm> SYNTHESIS_ALGORITHM_TYPE =
      new ObjectProperty<>(PropertyType.ALGORITHMS_SYNTHESIS,
                           "synthesisAlgorithmType",
                           SynthesisAlgorithm.MONOLITHIC,
                           SynthesisAlgorithm.class,
                           "Default synthesis algorithm");
    public static final BooleanProperty SYNTHESIS_PURGE = new BooleanProperty(PropertyType.ALGORITHMS_SYNTHESIS, "synthesisPurge", true, "Remove forbidden states after synthesis"); // MF changed to true here
	public static final BooleanProperty SYNTHESIS_RENAME = new BooleanProperty(PropertyType.ALGORITHMS_SYNTHESIS, "synthesisRename", false, "Rename states to generic names (q0, q1, ...)");	// MF
    public static final BooleanProperty SYNTHESIS_OPTIMIZE = new BooleanProperty(PropertyType.ALGORITHMS_SYNTHESIS, "synthesisOptimize", false, "Try to remove supervisors that are not necessary");
    public static final BooleanProperty SYNTHESIS_MAXIMALLY_PERMISSIVE = new BooleanProperty(PropertyType.ALGORITHMS_SYNTHESIS, "synthesisMaximallyPermissive", true, "Synthesize a maximally permissive supervisor");
    public static final BooleanProperty SYNTHESIS_MAXIMALLY_PERMISSIVE_INCREMENTAL = new BooleanProperty(PropertyType.ALGORITHMS_SYNTHESIS, "synthesisMaximallyPermissiveIncremental", true, "Use incremental algorithm when synthesizing");
    public static final BooleanProperty SYNTHESIS_REDUCE_SUPERVISORS = new BooleanProperty(PropertyType.ALGORITHMS_SYNTHESIS, "synthesisReduceSupervisors", false, "Try to minimize supervisors");
    public static final BooleanProperty SYNTHESIS_LOCALIZE_SUPERVISORS = new BooleanProperty(PropertyType.ALGORITHMS_SYNTHESIS, "synthesisLocalizeSupervisors", false, "Try to localize supervisors");
    public static final BooleanProperty SYNTHESIS_PRINT_GUARD = new BooleanProperty(PropertyType.ALGORITHMS_SYNTHESIS, "generateGuard", false, "Generate guards for the controllable events");
    public static final BooleanProperty SYNTHESIS_ADD_GUARDS = new BooleanProperty(PropertyType.ALGORITHMS_SYNTHESIS, "addGuards", false, "Add the guards to the model");
    public static final BooleanProperty SYNTHESIS_SAVE_IN_FILE = new BooleanProperty(PropertyType.ALGORITHMS_SYNTHESIS, "saveInFile", false, "Save the guard-event pairs in a file");
    public static final BooleanProperty SYNTHESIS_COMPLEMENT_HEURISTIC = new BooleanProperty(PropertyType.ALGORITHMS_SYNTHESIS, "complementHeuristic", true, "Apply the complement heuristic");
    public static final BooleanProperty SYNTHESIS_INDEPENDENT_HEURISTIC = new BooleanProperty(PropertyType.ALGORITHMS_SYNTHESIS, "independentHeuristic", true, "Apply the independent heuristic");
    public static final BooleanProperty SYNTHESIS_SAVE_IDD_IN_FILE = new BooleanProperty(PropertyType.ALGORITHMS_SYNTHESIS, "saveIDDInFile", false, "Save the guard-event pairs as an IDD in a file");
    public static final BooleanProperty SYNTHESIS_REACHABILITY = new BooleanProperty(PropertyType.ALGORITHMS_SYNTHESIS, "reachability", false, "Remove the unreachable states");
    public static final BooleanProperty SYNTHESIS_OPTIMIZATION = new BooleanProperty(PropertyType.ALGORITHMS_SYNTHESIS, "optimization", false, "Compute the global optimal time");
	public static final BooleanProperty SYNTHESIS_SUP_AS_PLANT = new BooleanProperty(PropertyType.ALGORITHMS_SYNTHESIS, "synthesisSupsAsPlants", false, "Consider supervisors as plants");

    // ALGORITHMS_MINIMIZATION
    public static final ObjectProperty<EquivalenceRelation> MINIMIZATION_EQUIVALENCE_RELATION =
      new ObjectProperty<>(PropertyType.ALGORITHMS_MINIMIZATION,
                           "minimizationEquivalenceRelation",
                           EquivalenceRelation.LANGUAGEEQUIVALENCE,
                           EquivalenceRelation.class,
                           "Default equivalence relation");
    public static final BooleanProperty MINIMIZATION_ALSO_MINIMIZE_TRANSITIONS = new BooleanProperty(PropertyType.ALGORITHMS_MINIMIZATION, "minimizationAlsoMinimizeTransitions", true, "Minimize the number of transitions");
    public static final BooleanProperty MINIMIZATION_KEEP_ORIGINAL = new BooleanProperty(PropertyType.ALGORITHMS_MINIMIZATION, "minimizationKeepOriginal", true, "Keep original");
    public static final BooleanProperty MINIMIZATION_IGNORE_MARKING = new BooleanProperty(PropertyType.ALGORITHMS_MINIMIZATION, "minimizationIgnoreMarking", false, "Ignore marking");
    public static final ObjectProperty<MinimizationStrategy> MINIMIZATION_STRATEGY =
      new ObjectProperty<>(PropertyType.ALGORITHMS_MINIMIZATION,
                           "minimizationStrategy",
                           MinimizationStrategy.FewestTransitionsFirst,
                           MinimizationStrategy.class,
                           "Minimization strategy");
    public static final ObjectProperty<MinimizationHeuristic> MINIMIZATION_HEURISTIC =
      new ObjectProperty<>(PropertyType.ALGORITHMS_MINIMIZATION,
                           "minimizationHeuristic",
                           MinimizationHeuristic.MostLocal,
                           MinimizationHeuristic.class,
                           "Minimization heuristics");
    public static final ObjectProperty<MinimizationPreselectingHeuristic> MINIMIZATION_PRESELECTINGHEURISTIC =
      new ObjectProperty<>(PropertyType.ALGORITHMS_MINIMIZATION,
                           "minimizationPreselecting",
                           MinimizationPreselectingHeuristic.AtLeastOneLocalEvent,
                           MinimizationPreselectingHeuristic.class,
                           "Minimization Preselecting Heuristics");
    public static final ObjectProperty<MinimizationSelectingHeuristic> MINIMIZATION_SELECTINGHEURISTIC =
      new ObjectProperty<>(PropertyType.ALGORITHMS_MINIMIZATION,
                           "minimizationSelectingHeuristic",
                           MinimizationSelectingHeuristic.MinimumActualStates,
                           MinimizationSelectingHeuristic.class,
                           "Minimization Selecting Heuristics");
    public static final ObjectProperty<String> MINIMIZATION_SILENT_EVENT_NAME = new ObjectProperty<String>(PropertyType.ALGORITHMS_MINIMIZATION, "generalSilentEventName", "tau", "Silent event name");
    public static final ObjectProperty<String> MINIMIZATION_SILENT_CONTROLLABLE_EVENT_NAME = new ObjectProperty<String>(PropertyType.ALGORITHMS_MINIMIZATION, "generalSilentControllableEventName", "tau_c", "Silent controllable event name");
    public static final ObjectProperty<String> MINIMIZATION_SILENT_UNCONTROLLABLE_EVENT_NAME = new ObjectProperty<String>(PropertyType.ALGORITHMS_MINIMIZATION, "generalSilentUnontrollableEventName", "tau_u", "Silent uncontrollable event name");
    // MF: This one below is purely experimental, remove in production code
    public static final BooleanProperty MINIMIZATION_USE_TAUEVENT_MAP = new BooleanProperty(PropertyType.ALGORITHMS_MINIMIZATION, "minimizationUseTaueventMap", true, "Use TauEvent map");

    // ALGORITHMS_BDD
    // New BDD implementation using JavaBDD library
    // -- the following where formerly under algorithms.bdd2
    public static final ObjectProperty<BDDPackage> BDD2_BDDLIBRARY =
      new ObjectProperty<>(PropertyType.ALGORITHMS_BDD, "libraryName",
                           BDDPackage.JAVA, BDDPackage.class, "BDD Library");
    public static final IntegerProperty BDD2_INITIALNODETABLESIZE =
      new IntegerProperty(PropertyType.ALGORITHMS_BDD, "initialNodeTableSize", 1000000, "Initial node table size");
    public static final IntegerProperty BDD2_CACHESIZE =
      new IntegerProperty(PropertyType.ALGORITHMS_BDD, "cacheSize", 100000, "Operation cache size");
    public static final IntegerProperty BDD2_MAXINCREASENODES =
      new IntegerProperty(PropertyType.ALGORITHMS_BDD, "maxIncreaseNodes", 2500000, "Maximum number of nodes by which to increase node table after garbage collection");
    public static final DoubleProperty BDD2_INCREASEFACTOR =
      new DoubleProperty(PropertyType.ALGORITHMS_BDD, "increaseFactor", 2.0,
                         "Factor by which to increase node table after garbage collection",
                         true, 0.0);
    public static final DoubleProperty BDD2_CACHERATIO =
      new DoubleProperty(PropertyType.ALGORITHMS_BDD, "cacheRatio", 10.0,
                         "Cache ratio for the operator caches (#tablenodes/#cachenodes)",
                         true, 0.0);
    public static final ObjectProperty<BDDPartitioningType> BDD2_PARTITIONING =
      new ObjectProperty<>(PropertyType.ALGORITHMS_BDD, "partitioning",
                           BDDPartitioningType.MONOLITHIC,
                           BDDPartitioningType.class,
                           "BDD transition partitioning");

    // -- the following where formerly under algorithms.bdd
    // Most of these are ugly integers in BDD.Options... but they have String representations here.
    public static final ObjectProperty<String> BDD_ORDER_ALGO =
      new ObjectProperty<>(PropertyType.ALGORITHMS_BDD,
                           "bddAutomataOrderingAlgorithm",
                           Options.ORDERING_ALGORITHM_NAMES[Options.ordering_algorithm],
                           Options.ORDERING_ALGORITHM_NAMES,
                           String.class, "Automata ordering algorithm", true);
    public static final IntegerProperty BDD_PARTITION_MAX =
      new IntegerProperty(PropertyType.ALGORITHMS_BDD, "bddMaxPartitionSize", 10000, "Max partition size");
    public static final BooleanProperty SYNTHESIS_PEAKBDD =
      new BooleanProperty(PropertyType.ALGORITHMS_BDD, "peakBDD", false, "Compute and print peak BDD");
    public static final BooleanProperty BDD_DEBUG_ON =
      new BooleanProperty(PropertyType.ALGORITHMS_BDD, "bddDebugOn", Options.debug_on, "Debug on");
    public static final BooleanProperty BDD_PROFILE_ON =
      new BooleanProperty(PropertyType.ALGORITHMS_BDD, "bddProfileOn", Options.profile_on, "Profiling");

    // ALGORITHMS_HMI
    public static final BooleanProperty INCLUDE_USERINTERFACE = new BooleanProperty(PropertyType.ALGORITHMS_HMI, "includeUserInterface", false, "Include SwiXML analyzer tools");
    public static final BooleanProperty EXPAND_EXTENDED_AUTOMATA = new BooleanProperty(PropertyType.ALGORITHMS_HMI, "expandEFA", true, "Expand extended automata into DFA");

    // MISC
    public static final BooleanProperty TUM_EXTERNAL_ON =
      new BooleanProperty(PropertyType.MISC, "tumExternalOn", false, "Activate TUM options");

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
