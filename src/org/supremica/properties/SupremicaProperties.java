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
 * Haradsgatan 26A
 * 431 42 Molndal
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

import java.util.*;
import java.io.*;
import org.supremica.automata.algorithms.SynthesisType;
import org.supremica.automata.algorithms.SynthesisAlgorithm;
import org.supremica.automata.algorithms.VerificationType;
import org.supremica.automata.algorithms.VerificationAlgorithm;

import org.supremica.util.BDD.Options;

/**
 * Properties for Supremica.
 *
 **/
public final class SupremicaProperties
	extends Properties
{
	private static final String XML_RPC_ACTIVE = "xmlRpcActive";
	private static final String XML_RPC_PORT = "xmlRpcPort";
	private static final String DOT_USE = "dotUse";
	private static final String DOT_EXECUTE_COMMAND = "dotExecuteCommand";
	private static final String DOT_MAX_NBR_OF_STATES = "dotMaxNbrOfStatesWithoutWarning";
	private static final String DOT_LEFT_TO_RIGHT = "dotLeftToRight";
	private static final String DOT_WITH_STATE_LABELS = "dotWithStateLabels";
	private static final String DOT_WITH_CIRCLES = "dotWithCircles";
	private static final String DOT_USE_COLORS = "dotUseColors";
	private static final String DOT_USE_MULTI_LABELS = "dotUseMultiLabels";
	private static final String DOT_AUTOMATIC_UPDATE = "dotAutomaticUpdate";
	private static final String INCLUDE_EDITOR = "includeEditor";
	private static final String INCLUDE_CELLEDITOR = "includeCellEditor";
	private static final String INCLUDE_BOUNDED_UNCON_TOOLS = "includeBoundedUnconTools";
	private static final String INCLUDE_EXPERIMENTAL_ALGORITHMS = "includeExperimentalAlgorithms";
	private static final String VERBOSE_MODE = "verboseMode";
	private static final String SUPERUSER_IDENTITY = "superuserIdentity";
	private static final String ALLOW_SUPERUSER_LOGIN = "allowSuperUserLogin";
	private static final String GENERAL_REDIRECT_STDOUT = "generalRedirectStdout";
	private static final String GENERAL_REDIRECT_STDERR = "generalRedirectStderr";
	private static final String GENERAL_LOOKANDFEEL = "generalLookAndFeel";
	//private static final String GENERAL_INCLUDE_ANIMATIONS = "generalIncludeAnimations";
	private static final String GENERAL_USE_RCP = "generalUseRcp";
	private static final String GENERAL_USE_ROBOTCOORDINATION = "generalUseRobotCoordination";
	private static final String GENERAL_USE_ROBOTCOORDINATION_ABB = "generalUseRobotCoordinationABB";

	// Logging options
	private static final String LOG_TO_CONSOLE = "logToConsole";
	private static final String LOG_TO_GUI = "logToGUI";

	// File Options
	private static final String FILE_OPEN_PATH = "fileOpenPath";
	private static final String FILE_SAVE_PATH = "fileSavePath";
	private static final String FILE_ALLOW_OPEN = "FileAllowOpen";
	private static final String FILE_ALLOW_SAVE = "FileAllowSave";
	private static final String FILE_ALLOW_IMPORT = "FileAllowImport";
	private static final String FILE_ALLOW_EXPORT = "FileAllowExport";
	private static final String FILE_ALLOW_QUIT = "FileAllowQuit";

	// Synchronization Options
	private static final String SYNC_FORBID_UNCON_STATES = "syncForbidUncontrollableStates";
	private static final String SYNC_EXPAND_FORBIDDEN_STATES = "syncExpandUncontrollableStates";
	private static final String SYNC_INITIAL_HASHTABLE_SIZE = "syncInitialHashtableSize";
	private static final String SYNC_EXPAND_HASHTABLE = "syncExpandHashtable";
	private static final String SYNC_NBR_OF_EXECUTERS = "synchNbrOfExecuters";

	// Verification Options
	private static final String VERIFY_VERIFICATION_TYPE = "verifyVerificationType";
	private static final String VERIFY_ALGORITHM_TYPE = "verifyAlgorithmType";
	private static final String VERIFY_EXCLUSION_STATE_LIMIT = "verifyExclusionStateLimit";
	private static final String VERIFY_REACHABILITY_STATE_LIMIT = "verifyReachabilityStateLimit";
	private static final String VERIFY_ONE_EVENT_AT_A_TIME = "verifyOneEventAtATime";
	private static final String VERIFY_SKIP_UNCONTROLLABILITY_CHECK = "skipUncontrollabilityCheck";
	private static final String VERIFY_NBR_OF_ATTEMPTS = "nbrOfAttempts";

	// Synthesizer Options
	private static final String SYNTHESIS_SYNTHESIS_TYPE = "synthesisSynthesisType";
	private static final String SYNTHESIS_ALGORITHM_TYPE = "synthesisAlgorithmType";
	private static final String SYNTHESIS_PURGE = "synthesisPurge";
	private static final String SYNTHESIS_OPTIMIZE = "synthesisOptimize";
	private static final String SYNTHESIS_MAXIMALLY_PERMISSIVE = "synthesisMaximallyPermissive";
	private static final String SYNTHESIS_REDUCE_SUPERVISORS = "synthesisReduceSupervisors";
	private static final String GENERAL_USE_SECURITY = "GeneralUseSecurity";

    // BDD Options
    private static final String BDD_SHOW_GROW = "bddShowGrowth";
    private static final String BDD_ALTER_PCG = "bddAlterPCG";
    private static final String BDD_TRACE_ON  = "bddTraceOn";
    private static final String BDD_DEBUG_ON  = "bddDebugOn";
    private static final String BDD_ALGORITHM = "bddAlgorithm";
    private static final String BDD_COUNT_ALGO= "bddCountAlgorithm";
    private static final String BDD_LOCAL_SATURATION = "bddLocalSaturation";
    private static final String BDD_UC_OPTIMISTIC = "bddUCOptimistic";
    private static final String BDD_NB_OPTIMISTIC = "bddNBOptimistic";
    private static final String BDD_LIB_PATH = "bddLibPath";

	// Simulation stuff
	private static final String SIMULATION_IS_EXTERNAL = "simulationIsExternal";
	private static final String SIMULATION_CYCLE_TIME = "simulationCycleTime";

	// Animator Options
	private static final String INCLUDE_ANIMATOR = "includeAnimator";

	// ShoeFactory Options
	private static final String INCLUDE_SHOE_FACTORY = "includeShoeFactory";

	// JGrafchart Options
	private static final String INCLUDE_JGRAFCHART = "includeJGrafchart";

	// SoftPLC Options
	private static final String INCLUDE_SOFTPLC = "includeSoftPLC";
	private static final String SOFTPLC_CYCLE_TIME = "softplcCycleTime";
	private static Vector softplcInterfaces = new Vector();

	// Special Menu Options
	private static final String SHOW_GENETIC_ALGORITHMS = "showGeneticAlgorithms";
	private static final String SHOW_ROBOTSTUDIO_LINK = "showRobotstudioLink";

	// Coordination ABB
	private static final String SHOW_COORDINATION_ABB = "showCoordinationABB";

	// ActiveXBridge
	private static final String USE_ACTIVEX_BRIDGE = "useActiveXBridge";


	private Set forbidExternalModification = new HashSet();

    // There is a reason why we do the initialization like this.
    // dont touch this code!!      /Arash
    private static SupremicaProperties wp = null;
    static {
	wp = new SupremicaProperties();
	updateBDDOptions(false);
    }

	private SupremicaProperties()
	{
		setProperty(FILE_OPEN_PATH, System.getProperty("user.home"), true);
		setProperty(FILE_SAVE_PATH, System.getProperty("user.home"), true);
		setProperty(FILE_ALLOW_OPEN, "true", true);
		setProperty(FILE_ALLOW_SAVE, "true", true);
		setProperty(FILE_ALLOW_IMPORT, "true", true);
		setProperty(FILE_ALLOW_EXPORT, "true", true);
		setProperty(FILE_ALLOW_QUIT, "true", true);
		setProperty(XML_RPC_ACTIVE, "false", true);
		setProperty(XML_RPC_PORT, "9112", true);
		setProperty(DOT_USE, "true", true);
		setProperty(DOT_EXECUTE_COMMAND, "dot", true);
		setProperty(DOT_MAX_NBR_OF_STATES, "40", true);
		setProperty(DOT_LEFT_TO_RIGHT, "false", true);
		setProperty(DOT_WITH_STATE_LABELS, "true", true);
		setProperty(DOT_WITH_CIRCLES, "false", true);
		setProperty(DOT_USE_COLORS, "true", true);
		setProperty(DOT_USE_MULTI_LABELS, "true", true);
		setProperty(DOT_AUTOMATIC_UPDATE, "true", true);
		setProperty(GENERAL_REDIRECT_STDOUT, "true", true);
		setProperty(GENERAL_REDIRECT_STDERR, "true", true);
		setProperty(GENERAL_LOOKANDFEEL, "System", true);
		//setProperty(GENERAL_INCLUDE_ANIMATIONS, "false", true);
		setProperty(GENERAL_USE_RCP, "false", true);
		setProperty(GENERAL_USE_RCP, "false", true);
		setProperty(GENERAL_USE_ROBOTCOORDINATION, "false", true);
		setProperty(GENERAL_USE_ROBOTCOORDINATION_ABB, "false", true);
		setProperty(INCLUDE_EDITOR, "false", true);
		setProperty(INCLUDE_SHOE_FACTORY, "false", true);
		setProperty(INCLUDE_SOFTPLC, "false", true);
		setProperty(INCLUDE_JGRAFCHART, "false", true);
		setProperty(INCLUDE_BOUNDED_UNCON_TOOLS, "false", true);
		setProperty(INCLUDE_EXPERIMENTAL_ALGORITHMS, "false", true);

		setProperty(INCLUDE_BOUNDED_UNCON_TOOLS, "false", true);
		setProperty(VERBOSE_MODE, "false", true);
		setProperty(SUPERUSER_IDENTITY, "ESS030", false);
		setProperty(LOG_TO_CONSOLE, "false", true);
		setProperty(LOG_TO_GUI, "false", true);
		setProperty(ALLOW_SUPERUSER_LOGIN, "true", false);
		setProperty(SYNC_FORBID_UNCON_STATES, "true", true);
		setProperty(SYNC_EXPAND_FORBIDDEN_STATES, "true", true);
		setProperty(SYNC_INITIAL_HASHTABLE_SIZE, Integer.toString((1 << 14) - 1), true);
		setProperty(SYNC_EXPAND_HASHTABLE, "true", true);
		setProperty(SYNC_NBR_OF_EXECUTERS, "1", true);
		setProperty(VERIFY_VERIFICATION_TYPE, VerificationType.Controllability.toString(), true);
		setProperty(VERIFY_ALGORITHM_TYPE, VerificationAlgorithm.Modular.toString(), true);
		setProperty(VERIFY_EXCLUSION_STATE_LIMIT, "1000", true);
		setProperty(VERIFY_REACHABILITY_STATE_LIMIT, "1000", true);
		setProperty(VERIFY_ONE_EVENT_AT_A_TIME, "false", true);
		setProperty(VERIFY_SKIP_UNCONTROLLABILITY_CHECK, "false", true);
		setProperty(VERIFY_NBR_OF_ATTEMPTS, "5", true);
		setProperty(SYNTHESIS_SYNTHESIS_TYPE, SynthesisType.Both.toString(), true);
		setProperty(SYNTHESIS_ALGORITHM_TYPE, SynthesisAlgorithm.Monolithic.toString(), true);
		setProperty(SYNTHESIS_PURGE, "false", true);
		setProperty(SYNTHESIS_OPTIMIZE, "false", true);
		setProperty(SYNTHESIS_MAXIMALLY_PERMISSIVE, "true", true);
		setProperty(SYNTHESIS_REDUCE_SUPERVISORS, "false", true);
		setProperty(GENERAL_USE_SECURITY, "false", false);
		setProperty(INCLUDE_ANIMATOR, "false", true);
		setProperty(SOFTPLC_CYCLE_TIME, "40", true);
		setProperty(SHOW_GENETIC_ALGORITHMS, "false", false);
		setProperty(SHOW_ROBOTSTUDIO_LINK, "false", false);
		setProperty(USE_ACTIVEX_BRIDGE, "false", false);
		setProperty(SHOW_COORDINATION_ABB, "false", false);

		softplcInterfaces.add(new org.supremica.gui.SoftplcInterface("org.supremica.softplc.Simulator.BTSim"));

		// BDD stuff
		setProperty(BDD_SHOW_GROW , toString(Options.show_grow), true);
		setProperty(BDD_ALTER_PCG , toString(Options.user_alters_PCG), true);
		setProperty(BDD_TRACE_ON  , toString(Options.trace_on), true);
		setProperty(BDD_DEBUG_ON  , toString(Options.debug_on), true);
		setProperty(BDD_UC_OPTIMISTIC, toString(Options.uc_optimistic), true);
		setProperty(BDD_NB_OPTIMISTIC, toString(Options.nb_optimistic), true);
		setProperty(BDD_LOCAL_SATURATION, toString(Options.local_saturation), true);
		setProperty(BDD_ALGORITHM , toString(Options.algo_family), true);
		setProperty(BDD_COUNT_ALGO, toString(Options.count_algo), true);

		// Simulation stuff
		setProperty(SIMULATION_IS_EXTERNAL, "false", false);
		setProperty(SIMULATION_CYCLE_TIME, "100", false);
	}

	public static final void setProperties(File aFile)
		throws Exception
	{

		FileInputStream fStream = new FileInputStream(aFile);
		BufferedInputStream bStream = new BufferedInputStream(fStream);
		setProperties(bStream);
	}

	public static final void setProperties(InputStream iStream)
		throws Exception
	{
		Properties newProperties = new Properties();
		newProperties.load(iStream);
		setProperties(newProperties);

		updateBDDOptions(false);
	}

	public static final void setProperties(Properties otherProperties)
	{
		for (Enumeration propEnum = otherProperties.propertyNames(); propEnum.hasMoreElements();)
		{
			String currKey = (String)propEnum.nextElement();
			if (wp.allowExternalModification(currKey))
			{
				wp.setProperty(currKey, otherProperties.getProperty(currKey));
			}
		}
	}

	public boolean allowExternalModification(String key)
	{
		return !forbidExternalModification.contains(key.toLowerCase());
	}

	public void setProperty(String key, String value, boolean allowExternalModification)
	{
		setProperty(key, value);
		if (!allowExternalModification)
		{
			forbidExternalModification.add(key.toLowerCase());
		}
	}

	public static void load(String fileName)
		throws IOException
	{

		// System.err.println("PropertiesLoad");
		FileInputStream fileStream = new FileInputStream(fileName);

		wp.load(fileStream);
	}

	public static String getFileOpenPath()
	{
		File theFile = new File(wp.getProperty(FILE_OPEN_PATH));

		return theFile.getAbsolutePath();
	}

	public static void setFileOpenPath(String path)
	{
		wp.setProperty(FILE_OPEN_PATH, path);
	}

	public static String getFileSavePath()
	{
		File theFile = new File(wp.getProperty(FILE_SAVE_PATH));

		return theFile.getAbsolutePath();
	}

	public static void setFileSavePath(String path)
	{
		wp.setProperty(FILE_SAVE_PATH, path);
	}

	public static boolean fileAllowOpen()
	{
		return toBoolean(wp.getProperty(FILE_ALLOW_OPEN));
	}

	public static void setFileAllowOpen(boolean allow)
	{
		wp.setProperty(FILE_ALLOW_OPEN, toString(allow));
	}

	public static boolean fileAllowSave()
	{
		return toBoolean(wp.getProperty(FILE_ALLOW_SAVE));
	}

	public static void setFileAllowSave(boolean allow)
	{
		wp.setProperty(FILE_ALLOW_SAVE, toString(allow));
	}

	public static boolean generalUseRCP()
	{
		return toBoolean(wp.getProperty(GENERAL_USE_RCP));
	}

	public static void setGeneralUseRCP(boolean allow)
	{
		wp.setProperty(GENERAL_USE_RCP, toString(allow));
	}

	public static boolean generalUseRobotCoordination()
	{
		return toBoolean(wp.getProperty(GENERAL_USE_ROBOTCOORDINATION));
	}

	public static void setGeneralUseRobotCoordination(boolean allow)
	{
		wp.setProperty(GENERAL_USE_ROBOTCOORDINATION, toString(allow));
	}

	public static boolean generalUseRobotCoordinationABB()
	{
		return toBoolean(wp.getProperty(GENERAL_USE_ROBOTCOORDINATION_ABB));
	}

	public static void setGeneralUseRobotCoordinationABB(boolean allow)
	{
		wp.setProperty(GENERAL_USE_ROBOTCOORDINATION_ABB, toString(allow));
	}

	public static boolean fileAllowImport()
	{
		return toBoolean(wp.getProperty(FILE_ALLOW_IMPORT));
	}

	public static void setFileAllowImport(boolean allow)
	{
		wp.setProperty(FILE_ALLOW_IMPORT, toString(allow));
	}

	public static boolean fileAllowExport()
	{
		return toBoolean(wp.getProperty(FILE_ALLOW_EXPORT));
	}

	public static void setFileAllowExport(boolean allow)
	{
		wp.setProperty(FILE_ALLOW_EXPORT, toString(allow));
	}

	public static boolean fileAllowQuit()
	{
		return toBoolean(wp.getProperty(FILE_ALLOW_QUIT));
	}

	public static void setFileAllowQuit(boolean allow)
	{
		wp.setProperty(FILE_ALLOW_QUIT, toString(allow));
	}

	public static boolean isXmlRpcActive()
	{
		return toBoolean(wp.getProperty(XML_RPC_ACTIVE));
	}

	public static void setXmlRpcActive(boolean active)
	{
		wp.setProperty(XML_RPC_ACTIVE, toString(active));
	}

	public static int getXmlRpcPort()
	{
		return toInt(wp.getProperty(XML_RPC_PORT));
	}

	public static void setXmlRpcPort(int port)
	{
		wp.setProperty(XML_RPC_PORT, toString(port));
	}

	public static boolean generalRedirectStdout()
	{
		return toBoolean(wp.getProperty(GENERAL_REDIRECT_STDOUT));
	}

	public static void setGeneralRedirectStdout(boolean allow)
	{
		wp.setProperty(GENERAL_REDIRECT_STDOUT, toString(allow));
	}

	public static boolean generalRedirectStderr()
	{
		return toBoolean(wp.getProperty(GENERAL_REDIRECT_STDERR));
	}

	public static void setGeneralRedirectStderr(boolean allow)
	{
		wp.setProperty(GENERAL_REDIRECT_STDERR, toString(allow));
	}

	public static boolean useDot()
	{
		return toBoolean(wp.getProperty(DOT_USE));
	}

	public static void setUseDot(boolean useDot)
	{
		wp.setProperty(DOT_USE, toString(useDot));
	}

	public static String getLookAndFeel()
	{
		return wp.getProperty(GENERAL_LOOKANDFEEL);
	}

	/**
	 * Possible values
	 * Metal : All platforms
	 * javax.swing.plaf.metal.MetalLookAndFeel : All platforms
	 * com.sun.java.swing.plaf.windows.WindowsLookAndFeel : Windows only
	 * com.sun.java.swing.plaf.motif.MotifLookAndFeel : All platforms
	 * javax.swing.plaf.mac.MacLookAndFeel : Mac only
	 * System : All platforms
	 */
	public static void setLookAndFeel(String command)
	{
		wp.setProperty(GENERAL_LOOKANDFEEL, command);
	}

	public static String getDotExecuteCommand()
	{
		return wp.getProperty(DOT_EXECUTE_COMMAND);
	}

	public static void setDotExecuteCommand(String command)
	{
		wp.setProperty(DOT_EXECUTE_COMMAND, command);
	}

	public static int getDotMaxNbrOfStatesWithoutWarning()
	{
		return toInt(wp.getProperty(DOT_MAX_NBR_OF_STATES));
	}

	public static void setDotMaxNbrOfStatesWithoutWarning(int maxNbrOfStates)
	{
		wp.setProperty(DOT_MAX_NBR_OF_STATES, toString(maxNbrOfStates));
	}

	public static boolean isDotLeftToRight()
	{
		return toBoolean(wp.getProperty(DOT_LEFT_TO_RIGHT));
	}

	public static void setDotLeftToRight(boolean leftToRight)
	{
		wp.setProperty(DOT_LEFT_TO_RIGHT, toString(leftToRight));
	}

	public static boolean isDotWithStateLabels()
	{
		return toBoolean(wp.getProperty(DOT_WITH_STATE_LABELS));
	}

	public static void setDotWithStateLabels(boolean withStateLabels)
	{
		wp.setProperty(DOT_WITH_STATE_LABELS, toString(withStateLabels));
	}

	public static boolean isDotWithCircles()
	{
		return toBoolean(wp.getProperty(DOT_WITH_CIRCLES));
	}

	public static void setDotWithCircles(boolean withCircles)
	{
		wp.setProperty(DOT_WITH_CIRCLES, toString(withCircles));
	}

	public static boolean isDotUseColors()
	{
		return toBoolean(wp.getProperty(DOT_USE_COLORS));
	}

	public static void setDotUseColors(boolean useColors)
	{
		wp.setProperty(DOT_USE_COLORS, toString(useColors));
	}

	public static boolean isDotUseMultipleLabels()
	{
		return toBoolean(wp.getProperty(DOT_USE_MULTI_LABELS));
	}

	public static void setDotUseMultipleLabels(boolean useMultiLabels)
	{
		wp.setProperty(DOT_USE_MULTI_LABELS, toString(useMultiLabels));
	}

	public static boolean isDotAutomaticUpdate()
	{
		return toBoolean(wp.getProperty(DOT_AUTOMATIC_UPDATE));
	}

	public static void setDotAutomaticUpdate(boolean automaticUpdate)
	{
		wp.setProperty(DOT_AUTOMATIC_UPDATE, toString(automaticUpdate));
	}

	public static boolean includeEditor()
	{
		return toBoolean(wp.getProperty(INCLUDE_EDITOR));
	}

	public static boolean includeExperimentalAlgorithms()
	{
		return toBoolean(wp.getProperty(INCLUDE_EXPERIMENTAL_ALGORITHMS));
	}

	public static boolean includeSoftPLC()
	{
		return toBoolean(wp.getProperty(INCLUDE_SOFTPLC));
	}

	public static boolean includeCellEditor()
	{
		return toBoolean(wp.getProperty(INCLUDE_CELLEDITOR));
	}

	public static boolean includeShoeFactory()
	{
		return toBoolean(wp.getProperty(INCLUDE_SHOE_FACTORY));
	}

	public static boolean includeJGrafchart()
	{
		return toBoolean(wp.getProperty(INCLUDE_JGRAFCHART));
	}

	public static boolean includeBoundedUnconTools()
	{
		return toBoolean(wp.getProperty(INCLUDE_BOUNDED_UNCON_TOOLS));
	}

	public static boolean verboseMode()
	{
		return toBoolean(wp.getProperty(VERBOSE_MODE));
	}

	public static void setVerboseMode(boolean mode)
	{
		wp.setProperty(VERBOSE_MODE, toString(mode));
	}

	public static String getSuperuserIdentity()
	{
		return wp.getProperty(SUPERUSER_IDENTITY);
	}

	public static boolean allowSuperUserLogin()
	{
		return toBoolean(wp.getProperty(ALLOW_SUPERUSER_LOGIN));
	}

	public static void setAllowSuperUserLogin(boolean mode)
	{
		wp.setProperty(ALLOW_SUPERUSER_LOGIN, toString(mode));
	}

	public static boolean logToConsole()
	{
		return toBoolean(wp.getProperty(LOG_TO_CONSOLE));
	}

	public static void setLogToConsole(boolean mode)
	{
		wp.setProperty(LOG_TO_CONSOLE, toString(mode));
	}

	public static boolean logToGUI()
	{
		return toBoolean(wp.getProperty(LOG_TO_GUI));
	}

	public static void setLogToGUI(boolean mode)
	{
		wp.setProperty(LOG_TO_GUI, toString(mode));
	}

	// Synchronization...
	public static boolean syncForbidUncontrollableStates()
	{
		return toBoolean(wp.getProperty(SYNC_FORBID_UNCON_STATES));
	}

	public static void setSyncForbidUncontrollableStates(boolean forbid)
	{
		wp.setProperty(SYNC_FORBID_UNCON_STATES, toString(forbid));
	}

	public static boolean syncExpandForbiddenStates()
	{
		return toBoolean(wp.getProperty(SYNC_EXPAND_FORBIDDEN_STATES));
	}

	public static void setSyncExpandForbiddenStates(boolean expand)
	{
		wp.setProperty(SYNC_EXPAND_FORBIDDEN_STATES, toString(expand));
	}

	public static int syncInitialHashtableSize()
	{
		return toInt(wp.getProperty(SYNC_INITIAL_HASHTABLE_SIZE));
	}

	public static void setSyncInitialHashtableSize(int size)
	{
		wp.setProperty(SYNC_INITIAL_HASHTABLE_SIZE, toString(size));
	}

	public static boolean syncExpandHashtable()
	{
		return toBoolean(wp.getProperty(SYNC_EXPAND_HASHTABLE));
	}

	public static void setSyncExpandHashtable(boolean expand)
	{
		wp.setProperty(SYNC_EXPAND_HASHTABLE, toString(expand));
	}

	public static int syncNbrOfExecuters()
	{
		return toInt(wp.getProperty(SYNC_NBR_OF_EXECUTERS));
	}

	public static void setSyncNbrOfExecuters(int nbrOfExecuters)
	{
		wp.setProperty(SYNC_NBR_OF_EXECUTERS, toString(nbrOfExecuters));
	}

	// Verification...
	public static VerificationType verifyVerificationType()
	{
		return VerificationType.toType(wp.getProperty(VERIFY_VERIFICATION_TYPE));
		//return toInt(wp.getProperty(VERIFY_VERIFICATION_TYPE));
	}

	public static void setVerifyVerificationType(VerificationType type)
	{
		wp.setProperty(VERIFY_VERIFICATION_TYPE, type.toString());
		// wp.setProperty(VERIFY_VERIFICATION_TYPE, toString(type));
	}

	public static VerificationAlgorithm verifyAlgorithmType()
	{
		return VerificationAlgorithm.toAlgorithm(wp.getProperty(VERIFY_ALGORITHM_TYPE));
		//return toInt(wp.getProperty(VERIFY_ALGORITHM_TYPE));
	}

	public static void setVerifyAlgorithmType(VerificationAlgorithm type)
	{
		wp.setProperty(VERIFY_ALGORITHM_TYPE, type.toString());
		// wp.setProperty(VERIFY_ALGORITHM_TYPE, toString(type));
	}

	public static int verifyExclusionStateLimit()
	{
		return toInt(wp.getProperty(VERIFY_EXCLUSION_STATE_LIMIT));
	}

	public static void setVerifyExclusionStateLimit(int limit)
	{
		wp.setProperty(VERIFY_EXCLUSION_STATE_LIMIT, toString(limit));
	}

	public static int verifyReachabilityStateLimit()
	{
		return toInt(wp.getProperty(VERIFY_REACHABILITY_STATE_LIMIT));
	}

	public static void setVerifyReachabilityStateLimit(int limit)
	{
		wp.setProperty(VERIFY_REACHABILITY_STATE_LIMIT, toString(limit));
	}

	public static boolean verifyOneEventAtATime()
	{
		return toBoolean(wp.getProperty(VERIFY_ONE_EVENT_AT_A_TIME));
	}

	public static void setVerifyOneEventAtATime(boolean bool)
	{
		wp.setProperty(VERIFY_ONE_EVENT_AT_A_TIME, toString(bool));
	}

	public static boolean verifySkipUncontrollabilityCheck()
	{
		return toBoolean(wp.getProperty(VERIFY_SKIP_UNCONTROLLABILITY_CHECK));
	}

	public static void setVerifySkipUncontrollabilityCheck(boolean bool)
	{
		wp.setProperty(VERIFY_SKIP_UNCONTROLLABILITY_CHECK, toString(bool));
	}

	public static int verifyNbrOfAttempts()
	{
		return toInt(wp.getProperty(VERIFY_NBR_OF_ATTEMPTS));
	}

	public static void setVerifyNbrOfAttempts(int nbr)
	{
		wp.setProperty(VERIFY_NBR_OF_ATTEMPTS, toString(nbr));
	}

	// Synthesis...
	public static SynthesisType synthesisSynthesisType()
	{
		return SynthesisType.toType(wp.getProperty(SYNTHESIS_SYNTHESIS_TYPE));
	}

	public static void setSynthesisSynthesisType(SynthesisType type)
	{
		wp.setProperty(SYNTHESIS_SYNTHESIS_TYPE, type.toString());
	}

	public static SynthesisAlgorithm synthesisAlgorithmType()
	{
		return SynthesisAlgorithm.toAlgorithm(wp.getProperty(SYNTHESIS_ALGORITHM_TYPE));
	}

	public static void setSynthesisAlgorithmType(SynthesisAlgorithm type)
	{
		wp.setProperty(SYNTHESIS_ALGORITHM_TYPE, type.toString());
	}

	public static boolean synthesisPurge()
	{
		return toBoolean(wp.getProperty(SYNTHESIS_PURGE));
	}

	public static void setSynthesisPurge(boolean purge)
	{
		wp.setProperty(SYNTHESIS_PURGE, toString(purge));
	}

	public static boolean synthesisOptimize()
	{
		return toBoolean(wp.getProperty(SYNTHESIS_OPTIMIZE));
	}

	public static void setSynthesisOptimize(boolean bool)
	{
		wp.setProperty(SYNTHESIS_OPTIMIZE, toString(bool));
	}

	public static boolean synthesisMaximallyPermissive()
	{
		return toBoolean(wp.getProperty(SYNTHESIS_MAXIMALLY_PERMISSIVE));
	}

	public static void setSynthesisMaximallyPermissive(boolean bool)
	{
		wp.setProperty(SYNTHESIS_MAXIMALLY_PERMISSIVE, toString(bool));
	}

	public static boolean synthesisReduceSupervisors()
	{
		return toBoolean(wp.getProperty(SYNTHESIS_REDUCE_SUPERVISORS));
	}

	public static void setSynthesisReduceSupervisors(boolean bool)
	{
		wp.setProperty(SYNTHESIS_REDUCE_SUPERVISORS, toString(bool));
	}

	public static boolean generalUseSecurity()
	{
		return toBoolean(wp.getProperty(GENERAL_USE_SECURITY));
	}

	public static void setUseSecurity(boolean useSecurity)
	{
		wp.setProperty(GENERAL_USE_SECURITY, toString(useSecurity));
	}

	public static boolean includeAnimator()
	{
		return toBoolean(wp.getProperty(INCLUDE_ANIMATOR));
	}

	public static void setIncludeAnimator(boolean includeAnimator)
	{
		wp.setProperty(INCLUDE_ANIMATOR, toString(includeAnimator));
	}

	public static int getSoftplcCycleTime()
	{
		return toInt(wp.getProperty(SOFTPLC_CYCLE_TIME));
	}

	public static void setSoftplcCycleTime(int cycleTime)
	{
		wp.setProperty(SOFTPLC_CYCLE_TIME, toString(cycleTime));
	}

	public static Vector getSoftplcInterfaces()
	{
		return softplcInterfaces;
	}

	public static void setSoftplcInterfaces(Vector interfaces)
	{
		softplcInterfaces = interfaces;
	}






	// Simulation
	public static boolean getSimulationIsExternal(){  return toBoolean(wp.getProperty(SIMULATION_IS_EXTERNAL));    }
	public static void setSimulationIsExternal(boolean a){  wp.setProperty(SIMULATION_IS_EXTERNAL, toString(a));    }

	public static int getSimulationCycleTime(){  return toInt(wp.getProperty(SIMULATION_CYCLE_TIME));    }
	public static void setSimulationCycleTime(int a){  wp.setProperty(SIMULATION_CYCLE_TIME, toString(a));    }


    // BDD
    public static String getBDDLibPath() { return wp.getProperty(BDD_LIB_PATH); }

    	public static int getBDDAlgorithm(){  return toInt(wp.getProperty(BDD_ALGORITHM));    }
    	public static void setBDDAlgorithm(int a){  wp.setProperty(BDD_ALGORITHM, toString(a));    }

    	public static int getBDDCountAlgorithm(){  return toInt(wp.getProperty(BDD_COUNT_ALGO));    }
    	public static void setBDDCountAlgorithm(int a){  wp.setProperty(BDD_COUNT_ALGO, toString(a));    }


	public static int getBDDShowGrow(){  return toInt(wp.getProperty(BDD_SHOW_GROW));    }
    public static void setBDDShowGrow(int a){  wp.setProperty(BDD_SHOW_GROW, toString(a));    }

	public static boolean getBDDAlterPCG(){  return toBoolean(wp.getProperty(BDD_ALTER_PCG));    }
    	public static void setBDDAlterPCG(boolean a){  wp.setProperty(BDD_ALTER_PCG, toString(a));    }

	public static boolean getBDDTraceOn(){  return toBoolean(wp.getProperty(BDD_TRACE_ON));    }
    	public static void setBDDTraceOn(boolean a){  wp.setProperty(BDD_TRACE_ON, toString(a));    }

	public static boolean getBDDDebugOn(){  return toBoolean(wp.getProperty(BDD_DEBUG_ON));    }
    	public static void setBDDDebugOn(boolean a){  wp.setProperty(BDD_DEBUG_ON, toString(a));    }

	public static boolean getBDDLocalSaturation(){  return toBoolean(wp.getProperty(BDD_LOCAL_SATURATION));    }
    	public static void setBDDLocalSaturation(boolean a){  wp.setProperty(BDD_LOCAL_SATURATION, toString(a));    }

	public static boolean isBDDUCOptimistic(){  return toBoolean(wp.getProperty(BDD_UC_OPTIMISTIC));    }
    	public static void setBDDUCOptimistic(boolean a){  wp.setProperty(BDD_UC_OPTIMISTIC, toString(a));    }

	public static boolean isBDDNBOptimistic(){  return toBoolean(wp.getProperty(BDD_NB_OPTIMISTIC));    }
    	public static void setBDDNBOptimistic(boolean a){  wp.setProperty(BDD_NB_OPTIMISTIC, toString(a));    }

    /*
     * The problem is that we got to copies of BDD Options.
     * This will make sure they are both updated
     */
    public static void updateBDDOptions(boolean from_Options) {

	if(from_Options) {
	    // Options -> Properties
	    setBDDAlgorithm(Options.algo_family);
	    setBDDShowGrow(Options.show_grow);
	    setBDDAlterPCG(Options.user_alters_PCG);
	    setBDDDebugOn(Options.debug_on);
	    setBDDUCOptimistic(Options.uc_optimistic);
	    setBDDNBOptimistic(Options.nb_optimistic);
	    setBDDLocalSaturation(Options.local_saturation);
	    setBDDTraceOn(Options.trace_on);
	    setBDDCountAlgorithm(Options.count_algo);
	} else {
	    // Properties -> Options
	    Options.algo_family      = getBDDAlgorithm();
	    Options.show_grow        = getBDDShowGrow();
	    Options.user_alters_PCG  = getBDDAlterPCG();
	    Options.uc_optimistic    = isBDDUCOptimistic();
	    Options.nb_optimistic    = isBDDNBOptimistic();
	    Options.debug_on         = getBDDDebugOn();
	    Options.trace_on         = getBDDTraceOn();
	    Options.local_saturation = getBDDLocalSaturation();
	    Options.count_algo       = getBDDCountAlgorithm();
	    if(getBDDLibPath() != null) Options.extraLibPath = getBDDLibPath() ;
	}
    }


	public static boolean showGeneticAlgorithms()
	{
		return toBoolean(wp.getProperty(SHOW_GENETIC_ALGORITHMS));
	}

	public static boolean showRobotstudioLink()
	{
		return toBoolean(wp.getProperty(SHOW_ROBOTSTUDIO_LINK));
	}

	public static boolean useActiveXBridge()
	{
		return toBoolean(wp.getProperty(USE_ACTIVEX_BRIDGE));
	}

	public static boolean showCoordinationABB()
	{
		return toBoolean(wp.getProperty(SHOW_COORDINATION_ABB));
	}

	private static String toString(boolean b)
	{
		if (b)
		{
			return Boolean.TRUE.toString();
		}
		else
		{
			return Boolean.FALSE.toString();
		}
	}

	private static String toString(int i)
	{
		return Integer.toString(i);
	}

	private static boolean toBoolean(String s)
	{
		return Boolean.valueOf(s) == Boolean.TRUE;
	}

	private static int toInt(String s)
	{
		return Integer.parseInt(s);
	}


	/**
	 * Looks for "-p propertyFile", and loads it if it exists.
	 */
	public static void loadProperties(String[] args)
	{
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-p") || args[i].equals("--properties"))
			{
				if (i + 1 < args.length)
				{
					String fileName = args[i + 1];
					File propFile = new File(fileName);
					try
					{
						setProperties(propFile);
					}
					catch (Exception e)
					{
						System.err.println("Error reading properties file: " + propFile.getAbsolutePath());
					}
				}
			}
		}
	}
}
