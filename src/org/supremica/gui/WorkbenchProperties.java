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

package org.supremica.gui;

import java.util.*;
import java.io.*;
import org.supremica.automata.algorithms.SynthesisType;
import org.supremica.automata.algorithms.SynthesisAlgorithm;

public final class WorkbenchProperties
	extends Properties
{
	private static final String FILE_OPEN_PATH = "fileOpenPath";
	private static final String FILE_SAVE_PATH = "fileSavePath";
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
	private static final String INCLUDE_BOUNDED_UNCON_TOOLS = "includeBoundedUnconTools";
	private static final String VERBOSE_MODE = "verboseMode";
	// SynchronizationOptions
	private static final String SYNC_FORBID_UNCON_STATES = "syncForbidUncontrollableStates";
	private static final String SYNC_EXPAND_FORBIDDEN_STATES = "syncExpandUncontrollableStates";
	private static final String SYNC_INITIAL_HASHTABLE_SIZE = "syncInitialHashtableSize";
	private static final String SYNC_EXPAND_HASHTABLE = "syncExpandHashtable";
	private static final String SYNC_NBR_OF_EXECUTERS = "synchNbrOfExecuters";
	// VerificationOptions
	private static final String VERIFY_VERIFICATION_TYPE = "verifyVerificationType";
	private static final String VERIFY_ALGORITHM_TYPE = "verifyAlgorithmType";
	private static final String VERIFY_EXCLUSION_STATE_LIMIT = "verifyExclusionStateLimit";
	private static final String VERIFY_REACHABILITY_STATE_LIMIT = "verifyReachabilityStateLimit";
	private static final String VERIFY_ONE_EVENT_AT_A_TIME = "verifyOneEventAtATime";
	private static final String VERIFY_SKIP_UNCONTROLLABILITY_CHECK = "skipUncontrollabilityCheck";
	// SynthesizerOptions
	private static final String SYNTHESIS_SYNTHESIS_TYPE = "synthesisSynthesisType";
	private static final String SYNTHESIS_ALGORITHM_TYPE = "synthesisAlgorithmType";
	private static final String SYNTHESIS_PURGE = "synthesisPurge";
	private static final String SYNTHESIS_OPTIMIZE = "synthesisOptimize";
	private static final String SYNTHESIS_MAXIMALLY_PERMISSIVE = "synthesisMaximallyPermissive";

	private static final String GENERAL_USE_SECURITY = "GeneralUseSecurity";

	private static final WorkbenchProperties wp = new WorkbenchProperties();

	private WorkbenchProperties()
	{
		setProperty(FILE_OPEN_PATH, "../examples/");
		setProperty(FILE_SAVE_PATH, ".");
		setProperty(XML_RPC_ACTIVE, "true");
		setProperty(XML_RPC_PORT, "9112");
		setProperty(DOT_USE, "true");
		setProperty(DOT_EXECUTE_COMMAND, "dot");
		setProperty(DOT_MAX_NBR_OF_STATES, "40");
		setProperty(DOT_LEFT_TO_RIGHT, "false");
		setProperty(DOT_WITH_STATE_LABELS, "true");
		setProperty(DOT_WITH_CIRCLES, "false");
		setProperty(DOT_USE_COLORS, "true");
		setProperty(DOT_USE_MULTI_LABELS, "true");
		setProperty(DOT_AUTOMATIC_UPDATE, "true");
		setProperty(INCLUDE_EDITOR, "true");
		setProperty(INCLUDE_BOUNDED_UNCON_TOOLS, "false");
		setProperty(VERBOSE_MODE, "false");
		setProperty(SYNC_FORBID_UNCON_STATES, "true");
		setProperty(SYNC_EXPAND_FORBIDDEN_STATES, "true");
		setProperty(SYNC_INITIAL_HASHTABLE_SIZE, Integer.toString((1 << 14) - 1));
		setProperty(SYNC_EXPAND_HASHTABLE, "true");
		setProperty(SYNC_NBR_OF_EXECUTERS, "1");
		setProperty(VERIFY_VERIFICATION_TYPE, "0");
		setProperty(VERIFY_ALGORITHM_TYPE, "0");
		setProperty(VERIFY_EXCLUSION_STATE_LIMIT, "1000");
		setProperty(VERIFY_REACHABILITY_STATE_LIMIT, "1000");
		setProperty(VERIFY_ONE_EVENT_AT_A_TIME, "false");
		setProperty(VERIFY_SKIP_UNCONTROLLABILITY_CHECK, "false");
		setProperty(SYNTHESIS_SYNTHESIS_TYPE, SynthesisType.Controllable.toString());
		setProperty(SYNTHESIS_ALGORITHM_TYPE, SynthesisAlgorithm.Modular.toString());
		setProperty(SYNTHESIS_PURGE, "true");
		setProperty(SYNTHESIS_OPTIMIZE, "true");
		setProperty(SYNTHESIS_MAXIMALLY_PERMISSIVE, "true");
		setProperty(GENERAL_USE_SECURITY, "false");
	}

	public static void load(String fileName)
		throws IOException
	{
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

	public static boolean useDot()
	{
		return toBoolean(wp.getProperty(DOT_USE));
	}

	public static void setUseDot(boolean useDot)
	{
		wp.setProperty(DOT_USE, toString(useDot));
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
	public static int verifyVerificationType()
	{
		return toInt(wp.getProperty(VERIFY_VERIFICATION_TYPE));
	}

	public static void setVerifyVerificationType(int type)
	{
		wp.setProperty(VERIFY_VERIFICATION_TYPE, toString(type));
	}

	public static int verifyAlgorithmType()
	{
		return toInt(wp.getProperty(VERIFY_ALGORITHM_TYPE));
	}

	public static void setVerifyAlgorithmType(int type)
	{
		wp.setProperty(VERIFY_ALGORITHM_TYPE, toString(type));
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

	public static void setSynthesisOptimize(boolean maximallyPermissive)
	{
		wp.setProperty(SYNTHESIS_OPTIMIZE, toString(maximallyPermissive));
	}

	public static boolean synthesisMaximallyPermissive()
	{
		return toBoolean(wp.getProperty(SYNTHESIS_MAXIMALLY_PERMISSIVE));
	}

	public static void setSynthesisMaximallyPermissive(boolean maximallyPermissive)
	{
		wp.setProperty(SYNTHESIS_MAXIMALLY_PERMISSIVE, toString(maximallyPermissive));
	}

	public static boolean generalUseSecurity()
	{
		return toBoolean(wp.getProperty(GENERAL_USE_SECURITY));
	}

	public static void setUseSecurity(boolean useSecurity)
	{
		wp.setProperty(GENERAL_USE_SECURITY, toString(useSecurity));
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
}
