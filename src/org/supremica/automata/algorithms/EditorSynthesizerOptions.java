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
package org.supremica.automata.algorithms;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.properties.Config;


public final class EditorSynthesizerOptions
{
  //#########################################################################
  //# Data Members
  private static Logger logger =
    LogManager.getLogger(SynthesizerOptions.class);

  private boolean dialogOK = false;
  private SynthesisType synthesisType;
  private SynthesisAlgorithm synthesisAlgorithm;
  private boolean purge;
  private boolean removeUnnecessarySupervisors;
  private boolean maximallyPermissive;
  private boolean maximallyPermissiveIncremental;
  private boolean reduceSupervisors;
  private boolean printGuard;
  private boolean addGuards;
  private boolean saveInFile;
  private boolean saveIDDInFile;
  private boolean genPLCCodeTUMBox;
  private boolean compHeuristic;
  private boolean indpHeuristic;
  private boolean reachability;
  private boolean peakBDD;
  private boolean rememberDisabledUncontrollableEvents;
  private boolean optimization;
  private long globalClockDomain = 0;

  public boolean oneEventAtATime = false;
  public boolean addOnePlantAtATime = false;

  //Guard options
  private String event;
  private ExpressionType expressionType;

  //Optimization options
  private String optVariable;
  private boolean typeOfVarOpt = true;

  //#########################################################################
  //# Constructors
  /**
   * The current options, based on earlier user preferences.
   */
  public EditorSynthesizerOptions()
  {
    this(Config.SYNTHESIS_SYNTHESIS_TYPE.get(),
         Config.SYNTHESIS_ALGORITHM_TYPE.get(),
         Config.SYNTHESIS_PURGE.get(),
         Config.SYNTHESIS_OPTIMIZE.get(),
         Config.SYNTHESIS_MAXIMALLY_PERMISSIVE.get(),
         Config.SYNTHESIS_MAXIMALLY_PERMISSIVE_INCREMENTAL.get(),
         Config.SYNTHESIS_REDUCE_SUPERVISORS.get(),
         Config.SYNTHESIS_PRINT_GUARD.get(),
         Config.SYNTHESIS_ADD_GUARDS.get(),
         Config.SYNTHESIS_SAVE_IN_FILE.get(),
         Config.SYNTHESIS_SAVE_IDD_IN_FILE.get(),
         Config.SYNTHESIS_REACHABILITY.get(),
         Config.SYNTHESIS_PEAKBDD.get(),
         Config.SYNTHESIS_OPTIMIZATION.get());
  }

  /**
   * This is not a good constructor so it is private, it is impossible to read
   * in the code. Use the "getDefault..." method in this class instead or when
   * they won't suit you, modify the necessary options one by one, starting
   * from default! Much more readable and also more practical when adding new
   * options.
   */
  private EditorSynthesizerOptions(final SynthesisType synthesisType,
                                   final SynthesisAlgorithm synthesisAlgorithm,
                                   final boolean purge,
                                   final boolean removeUnnecessarySupervisors,
                                   final boolean maximallyPermissive,
                                   final boolean maximallyPermissiveIncremental,
                                   final boolean reduceSupervisors,
                                   final boolean computePrintGuard,
                                   final boolean addGuards,
                                   final boolean saveInFile,
                                   final boolean saveIDDInFile,
                                   final boolean reachability,
                                   final boolean peakBDD,
                                   final boolean optimization)
  {
    this.synthesisType = synthesisType;
    this.synthesisAlgorithm = synthesisAlgorithm;
    this.purge = purge;
    this.removeUnnecessarySupervisors = removeUnnecessarySupervisors;
    this.maximallyPermissive = maximallyPermissive;
    this.maximallyPermissiveIncremental = maximallyPermissiveIncremental;
    this.reduceSupervisors = reduceSupervisors;
    this.printGuard = computePrintGuard;
    this.addGuards = addGuards;
    this.saveInFile = saveInFile;
    this.saveIDDInFile = saveIDDInFile;
    this.reachability = reachability;
    this.peakBDD = peakBDD;
    this.optimization = optimization;

    this.event = "";
    this.expressionType = ExpressionType.ADAPTIVE;

    this.optVariable = "";
  }

  /**
   * Stores the current set of options in SupremicaProperties.
   */
  public void saveOptions()
  {
    Config.SYNTHESIS_SYNTHESIS_TYPE.setValue(synthesisType);
    Config.SYNTHESIS_ALGORITHM_TYPE.setValue(synthesisAlgorithm);
    Config.SYNTHESIS_MAXIMALLY_PERMISSIVE.set(maximallyPermissive);
    Config.SYNTHESIS_MAXIMALLY_PERMISSIVE_INCREMENTAL
      .set(maximallyPermissiveIncremental);
    Config.SYNTHESIS_PRINT_GUARD.set(printGuard);
    Config.SYNTHESIS_ADD_GUARDS.set(addGuards);
    Config.SYNTHESIS_SAVE_IN_FILE.set(saveInFile);
    Config.SYNTHESIS_SAVE_IDD_IN_FILE.set(saveIDDInFile);
    Config.SYNTHESIS_COMPLEMENT_HEURISTIC.set(compHeuristic);
    Config.SYNTHESIS_INDEPENDENT_HEURISTIC.set(indpHeuristic);
  }

  /**
   * Returns the default options for synthesis.
   */
  // In which case is this method used? The defaults are specified
  // in Config.
  // Julien Provost, 2018-09-21: This functions seems to never be used
  public static EditorSynthesizerOptions getDefaultSynthesizerOptions()
  {
    return new EditorSynthesizerOptions(SynthesisType.CONTROLLABLE,
                                        SynthesisAlgorithm.MONOLITHIC_WATERS,
                                        true,
                                        true,
                                        true,
                                        true,
                                        true,
                                        true,
                                        false,
                                        false,
                                        false,
                                        false,
                                        true,
                                        false);
  }

  //#########################################################################
  //# Sanity check
  public boolean isValid()
  {
    final String errorMessage = validOptions();
    if (errorMessage != null) {
      logger.error(errorMessage);
      return false;
    }

    return true;
  }

  public String validOptions()
  {
    if (synthesisType == null) {
      return "Unknown synthesis type.";
    }
    return null;
  }

  //#########################################################################
  //# Getters and setters
  public void setDialogOK(final boolean bool)
  {
    dialogOK = bool;
  }

  public boolean getDialogOK()
  {
    return dialogOK;
  }

  public void setSynthesisType(final SynthesisType type)
  {
    synthesisType = type;
  }

  public SynthesisType getSynthesisType()
  {
    return synthesisType;
  }

  public void setSynthesisAlgorithm(final SynthesisAlgorithm algorithm)
  {
    synthesisAlgorithm = algorithm;
  }

  public SynthesisAlgorithm getSynthesisAlgorithm()
  {
    return synthesisAlgorithm;
  }

  public void setPurge(final boolean bool)
  {
    purge = bool;
  }

  public boolean doPurge()
  {
    return purge;
  }

  public void setRememberDisabledUncontrollableEvents(final boolean remember)
  {
    rememberDisabledUncontrollableEvents = remember;
  }

  public boolean doRememberDisabledUncontrollableEvents()
  {
    return rememberDisabledUncontrollableEvents;
  }

  public void setRemoveUnecessarySupervisors(final boolean bool)
  {
    removeUnnecessarySupervisors = bool;
  }

  public boolean getRemoveUnecessarySupervisors()
  {
    return removeUnnecessarySupervisors;
  }

  public void setMaximallyPermissive(final boolean bool)
  {
    maximallyPermissive = bool;
  }

  public boolean getMaximallyPermissive()
  {
    return maximallyPermissive;
  }

  public void setMaximallyPermissiveIncremental(final boolean bool)
  {
    maximallyPermissiveIncremental = bool;
  }

  public boolean getMaximallyPermissiveIncremental()
  {
    return maximallyPermissiveIncremental;
  }

  public void setReduceSupervisors(final boolean bool)
  {
    reduceSupervisors = bool;
  }

  public boolean getReduceSupervisors()
  {
    return reduceSupervisors;
  }

  public boolean getPrintGuard()
  {
    return printGuard;
  }

  public void setPrintGuard(final boolean bool)
  {
    printGuard = bool;
  }

  public boolean getAddGuards()
  {
    return addGuards;
  }

  public void setAddGuards(final boolean bool)
  {
    addGuards = bool;
  }

  public boolean getSaveInFile()
  {
    return saveInFile;
  }

  public void setSaveInFile(final boolean bool)
  {
    saveInFile = bool;
  }

  public void setSaveIDDInFile(final boolean bool)
  {
    saveIDDInFile = bool;
  }

  public boolean getSaveIDDInFile()
  {
    return saveIDDInFile;
  }

  public void setGenPLCCodeTUMBox(final boolean bool)
  {
    genPLCCodeTUMBox = bool;
  }

  public boolean getGenPLCCodeTUMBox()
  {
    return genPLCCodeTUMBox;
  }

  public void setCompHeuristic(final boolean bool)
  {
    compHeuristic = bool;
  }

  public boolean getCompHeuristic()
  {
    return compHeuristic;
  }

  public void setIndpHeuristic(final boolean bool)
  {
    indpHeuristic = bool;
  }

  public boolean getIndpHeuristic()
  {
    return indpHeuristic;
  }

  public boolean getReachability()
  {
    return reachability;
  }

  public void setReachability(final boolean bool)
  {
    reachability = bool;
  }

  public boolean getPeakBDD()
  {
    return peakBDD;
  }

  public void setPeakBDD(final boolean bool)
  {
    peakBDD = bool;
  }

  public boolean getOptimization()
  {
    return optimization;
  }

  public void setOptimization(final boolean bool)
  {
    optimization = bool;
  }

  public void setGlobalClockDomain(final long domain)
  {
    globalClockDomain = domain;
  }

  public long getGlobalClockDomain()
  {
    return globalClockDomain;
  }

  //Guard options

  public ExpressionType getExpressionType()
  {
    return expressionType;
  }

  public void setExpressionType(final ExpressionType exType)
  {
    expressionType = exType;
  }

  public String getEvent()
  {
    return event;
  }

  public void setEvent(final String set)
  {
    event = set;
  }

  public String getOptVaribale()
  {
    return optVariable;
  }

  public void setOptVariable(final String var)
  {
    optVariable = var;
  }

  public boolean getTypeOfVarOpt()
  {
    return typeOfVarOpt;
  }

  public void setTypeOfVarOpt(final boolean minMax)
  {
    typeOfVarOpt = minMax;
  }

  //#########################################################################
  //# Enum
  public enum ExpressionType {
    FORBIDDEN,
    ALLOWED,
    ADAPTIVE
  }
}
