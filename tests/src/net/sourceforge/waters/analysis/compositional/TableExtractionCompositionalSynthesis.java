//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;


/**
 * This class can be used to automatically run experiments for different
 * properties with all possible combinations of heuristics.
 *
 * @author Sahar Mohajerani, Robi Malik
 */

public class TableExtractionCompositionalSynthesis extends AbstractAnalysisTest
{

  //#########################################################################
  //# Main
  public static void main(final String[] args)
  {
    if (args.length == 0) {
      usage();
    }
    int index = 0;
    int verbosity = VERBOSITY_NORMAL;
    final String arg0 = args[0];
    if (arg0.startsWith("-")) {
      if (arg0.equals("-brief")) {
        verbosity = VERBOSITY_BRIEF;
      } else if (arg0.equals("-normal")) {
        verbosity = VERBOSITY_NORMAL;
      } else {
        usage();
      }
      index++;
    }
    if (args.length == index) {
      usage();
    }
    final File overviewFile = new File(args[index++]);
    File chartFile = null;
    if (index < args.length) {
      chartFile = new File(args[index++]);
    }
    File compareFile = null;
    if (index < args.length) {
      compareFile = new File(args[index++]);
    }
    if (index < args.length) {
      usage();
    }
    final TableExtractionCompositionalSynthesis extractor =
      new TableExtractionCompositionalSynthesis(verbosity);
    extractor.extract(overviewFile, chartFile, compareFile);
  }

  private static void usage()
  {
    System.err.println("USAGE: java " +
                       ProxyTools.getShortClassName
                         (TableExtractionCompositionalSynthesis.class) +
                       " [-brief|-normal]" +
                       " <overview-file> [<chart-file>] [<compare-file>]");
  }


  //#########################################################################
  //# Constructor
  private TableExtractionCompositionalSynthesis(final int verbosity)
  {
    final Map<String, Result> automataMap = new TreeMap<String, Result>();
    mResults.add(automataMap);
    final Map<String, Result> stateMap = new TreeMap<String, Result>();
    mResults.add(stateMap);
    createModelInfo("agv", "agv", true, false, "2.6 \\cdot 10^7");
    createModelInfo("agvb", "agvb", false, false, "2.3 \\cdot 10^7");
    createModelInfo("aip0alps", "aip0alps", false, true, "3.0 \\cdot 10^8");
    createModelInfo("FenCaiWon09b", "fencaiwon09b", false, true, "8.9 \\cdot 10^7");
    createModelInfo("FenCaiWon09s", "fencaiwon09s", false, false, "2.9 \\cdot 10^8");
    createModelInfo("fms2003", "fms2003", false, true, "1.7 \\cdot 10^7");
    createModelInfo("pslBig", "psl\\_big", false, true, "3.9 \\cdot 10^7");
    createModelInfo("pslBigWithManyRestartTrans", "psl\\_restart", false, true, "3.9 \\cdot 10^7");
    createModelInfo("pslWithResetTransWithPartLeftPlants", "psl\\_partleft", true, true, "7.7 \\cdot 10^7");
    createModelInfo("tbed_hisc1", "tbed\\_hisc1", false, true, "2.9 \\cdot 10^{17}");
    createModelInfo("tbed_noderailb", "tbed\\_noderailb", false, true, "3.2 \\cdot 10^{12}");
    createModelInfo("tbed_uncont", "tbed\\_uncont", true, false, "3.6 \\cdot 10^{12}");
    createModelInfo("verriegel3b", "verriegel3b", false, true, "1.3 \\cdot 10^{9}");
    createModelInfo("verriegel4b", "verriegel4b", false, true, "6.2 \\cdot 10^{10}");
    createModelInfo("6linka", "6linka", false,true,"2.4 \\cdot 10^{14}");
    createModelInfo("6linki", "6linki", false,true,"2.7 \\cdot 10^{14}");
    createModelInfo("6linkp", "6linkp", false,true,"4.2 \\cdot 10^{14}");
    createModelInfo("6linkre", "6linkre", false,true,"6.2 \\cdot 10^{14}");
    mVerbosity = verbosity;
  }

  private void createModelInfo(final String fullName,
                               final String shortName,
                               final boolean nonblocking,
                               final boolean controllable,
                               final String size)
  {
    final ModelInfo info = new ModelInfo(fullName, shortName, controllable, nonblocking, size);
    mModelInfoMap.put(fullName, info);
  }


  //#########################################################################
  //# Table Extraction
  private void extract(final File overviewFile,
                       final File chartFile,
                       final File compareFile)
  {
    try {
      final ProductDESProxyFactory factory =
        ProductDESElementFactory.getInstance();
      final CompositionalAutomataSynthesizer automataSynthesizer =
        new CompositionalAutomataSynthesizer(factory);
      automataSynthesizer.setDetailedOutputEnabled(false);
      final List<Configuration> configurations = new LinkedList<>();
      final Configuration configWSOEUnsup =
        new Configuration(automataSynthesizer,
                          AutomataSynthesisAbstractionProcedureFactory.WSOE_UNSUP);
      configurations.add(configWSOEUnsup);
      final AbstractCompositionalSynthesizer stateRepresentationSynthesizer =
        new CompositionalStateRepresentationSynthesizer(factory);
      stateRepresentationSynthesizer.setFailingEventsEnabled(false);
      final Configuration configStateRepresent =
        new Configuration(stateRepresentationSynthesizer,
                          StateRepresentationSynthesisAbstractionProcedureFactory.WSOE_UNSUP);
      configurations.add(configStateRepresent);
      final AbstractCompositionalModelAnalyzer.PreselectingMethodFactory
      preselectingFactory = automataSynthesizer.getPreselectingMethodFactory();
      final CompositionalSelectionHeuristicFactory selectionFactory =
        automataSynthesizer.getSelectionHeuristicFactory();
      final AbstractCompositionalModelAnalyzer.PreselectingMethod
        comparePreselectingMethod = AbstractCompositionalModelAnalyzer.MustL;
      final SelectionHeuristicCreator
        compareSelectingMethod = CompositionalSelectionHeuristicFactory.MaxC;
      int compareIndex = 0;
      int methodCount = 0;
      for (final AbstractCompositionalModelAnalyzer.PreselectingMethod
           preselectingMethod : preselectingFactory.getEnumConstants()) {
        for (final SelectionHeuristicCreator
             selectingMethod: selectionFactory.getEnumConstants()) {
          methodCount++;
          int mapIndex = 0;
          for (final Configuration config: configurations) {
            readCsvFile(methodCount, preselectingMethod,
                        selectingMethod, mapIndex, config);
            if (preselectingMethod == comparePreselectingMethod &&
                selectingMethod == compareSelectingMethod) {
              compareIndex = methodCount;
            }
            mapIndex++;
          }
        }
      }
      printOverviewTable(overviewFile);
      for (final Map<String, Result> map: mResults) {
        map.clear();
      }
      int mapIndex = 0;
      for (final Configuration config: configurations) {
        readCsvFile(compareIndex, comparePreselectingMethod,
                    compareSelectingMethod, mapIndex, config);
        mapIndex++;
      }
      if (chartFile != null) {
        printChart(chartFile);
      }
      if (compareFile != null) {
        printStateComparisonTable(compareFile);
      }
    } catch (final Throwable exception) {
      System.err.println("FATAL ERROR");
      exception.printStackTrace(System.err);
    }
  }

  private void readCsvFile(final int methodCount,
                           final AbstractCompositionalModelAnalyzer.PreselectingMethod preselectingMethod,
                           final SelectionHeuristicCreator selectingMethod,
                           final int mapIndex, final Configuration config)
    throws IOException
  {
    final String outputprop = System.getProperty("waters.test.outputdir");
    final String preName = preselectingMethod.toString();
    final String selName = selectingMethod.toString();
    final String filename =
      methodCount + "_" + config + "_" + preName + "_" + selName +
      "_NR.csv";
    final File csvFile = new File(outputprop, filename);
    final String heuristics = preName + "/" + selName;
    readCsvFile(csvFile, mapIndex, heuristics);
  }

  private void readCsvFile(final File fileName,
                           final int mapIndex,
                           final String heuristics)
    throws IOException
  {
    if (fileName.exists()) {
      final int nameColumn = 0;
      int timeColumn = 0;
      int totalTrColumn = 0;
      int totalStatesColumn = 0;
      int memColumn = 0;
      int resultColumn = 0;
      final Map<String,Result> map = mResults.get(mapIndex);
      final BufferedReader br = new BufferedReader(new FileReader(fileName));
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        final String sep = line.indexOf(',') >= 0 ? "," : ";";
        final String[] words = line.split(sep);
        if (words.length > 6) {
          if (words[nameColumn].equals("Model")) {
            resultColumn = findIndex(words, "Result");
            timeColumn = findIndex(words, "RunTime");
            totalTrColumn = findIndex(words, "TotTrans");
            totalStatesColumn = findIndex(words, "TotStates");
            memColumn = findIndex(words, "UnrenamedSupervisorMemoryEstimate");
            if (memColumn < 0) {
              memColumn = findIndex(words, "MemoryEstimate");
            }
          } else {
            final String result = words[resultColumn];
            if (result.equalsIgnoreCase("true")) {
              String name = words[nameColumn];
              final int dotpos = name.indexOf('.');
              if (dotpos >= 0) {
                name = name.substring(0, dotpos);
              }
              final double time = 0.001 * Long.parseLong(words[timeColumn]);
              final int totTrans = Integer.parseInt(words[totalTrColumn]);
              final int totStates = Integer.parseInt(words[totalStatesColumn]);
              final int supMem = Integer.parseInt(words[memColumn]);
              final Result oldResult = map.get(name);
              if (oldResult == null || oldResult.getMemory() > supMem) {
                final Result newResult = new Result(name, time, totStates,
                                                    totTrans, supMem, heuristics);
                map.put(name, newResult);
              }
            }
          }
        }
      }
      br.close();
    } else {
      System.err.println(fileName + " not found.");
    }
  }

  private void printOverviewTable(final File outputFile) throws IOException
  {
    final OutputStream stream = new FileOutputStream(outputFile);
    final PrintWriter writer = new PrintWriter(stream);
    for (final Map.Entry<String,ModelInfo> entry : mModelInfoMap.entrySet()) {
      final String name = entry.getKey();
      final ModelInfo info = entry.getValue();
      String text = info.getTableText(mVerbosity);
      writer.print(text);
      for (int mapIndex = 0; mapIndex < mResults.size(); mapIndex++) {
        final Map<String, Result> map = mResults.get(mapIndex);
        final Result result = map.get(name);
        if (result == null) {
          if (mVerbosity >= VERBOSITY_NORMAL) {
            writer.print(" & & &");
          } else {
            writer.print(" & &");
          }
        } else {
          writer.print(" & ");
          final double time = result.getTime();
          text = String.format((Locale) null, "%.2f\\,s", time);
          writer.print(text);
          writer.print(" & ");
          final int memory = result.getMemory();
          writer.print(memory);
          writer.print(" & ");
          if (mVerbosity >= VERBOSITY_NORMAL) {
            final String heuristics = result.getHeuristics();
            writer.print(heuristics);
          } else {
            final int trans = result.getTotalTransitions();
            writer.print(trans);
          }
        }
      }
      writer.println(" \\\\");
    }
    writer.close();
  }

  private void printStateComparisonTable(final File outputFile)
    throws IOException
  {
    final OutputStream stream = new FileOutputStream(outputFile);
    final PrintWriter writer = new PrintWriter(stream);
    for (final Map.Entry<String,ModelInfo> entry : mModelInfoMap.entrySet()) {
      final String name = entry.getKey();
      final ModelInfo info = entry.getValue();
      String text = info.getTableText(VERBOSITY_NORMAL);
      writer.print(text);
      for (int mapIndex = 0; mapIndex <mResults.size(); mapIndex++) {
        final Map<String, Result> map = mResults.get(mapIndex);
        final Result result = map.get(name);
        if (result == null) {
          writer.print(" & & & &");
        } else {
          final int totStates = result.getTotalStates();
          final int totTrans = result.getTotalTransitions();
          final double time = result.getTime();
          final int memory = result.getMemory();
          text = " & " + totStates + " & " + totTrans + " & " +
                 String.format((Locale) null, "%.2f\\,s", time) + " & " +
                 memory;
          writer.print(text);
        }
      }
      writer.println(" \\\\");
    }
    writer.close();
  }

  private void printChart(final File outputFile) throws FileNotFoundException
  {
    final OutputStream stream = new FileOutputStream(outputFile);
    final PrintWriter writer = new PrintWriter(stream);
    writer.println("\\psset{linewidth=.4pt}\\psset{unit=.1mm}\\newgray{lightgray}{0.9}");
    writer.println("\\begin{pspicture}(-75,-200)(766,330)");
    int maxTransition = 0;
    for (final Map.Entry<String,ModelInfo> entry : mModelInfoMap.entrySet()) {
      final String name = entry.getKey();
      for (int mapIndex = 0; mapIndex <mResults.size(); mapIndex++) {
        final Map<String, Result> map = mResults.get(mapIndex);
        final Result result = map.get(name);
        if (result != null) {
          final int totTrans = result.getTotalTransitions();
          if (totTrans > maxTransition) {
            maxTransition = totTrans;
          }
        }
      }
    }
    int groupStartX = 10;
    for (final Map.Entry<String,ModelInfo> entry : mModelInfoMap.entrySet()) {
      double barStartX = groupStartX + OFFSET_IN_GROUP;
      final String name = entry.getKey();
      final ModelInfo info = entry.getValue();
      for (int mapIndex = 0; mapIndex < mResults.size(); mapIndex++) {
        final Map<String, Result> map = mResults.get(mapIndex);
        final Result result = map.get(name);
        if (result == null) {
          final double x = groupStartX + OFFSET_IN_GROUP + BAR_WIDTH;
          final String pos = mapIndex == 0 ? "lb" : "rb";
          writer.format("\\rput[%s]{90}(%.2f,7){\\tiny Out of memory}", pos, x);
        } else {
          final int totTrans = result.getTotalTransitions();
          final double barHeight = (double)totTrans/ maxTransition * BAR_HEIGHT;
          final double x1 = barStartX;
          final double x2 = x1 + BAR_WIDTH;
          final double y1 = 0;
          final double y2 = barHeight;
          final String colour = BAR_COLOUR[mapIndex];
          writer.format("\\pspolygon[%s](%.2f,%.2f)(%.2f,%.2f)(%.2f,%.2f)(%.2f,%.2f)",
                        colour, x1, y1, x1, y2, x2, y2, x2, y1);
        }
        barStartX = barStartX + BAR_WIDTH;

      }
      final String text = info.getShortName();
      final int x = groupStartX + GROUP_WIDTH/2;
      writer.format("\\rput[r]{90}(%d,-7){\\scriptsize %s}\n",x,text);
      groupStartX = groupStartX + GROUP_WIDTH;

    }
    for (int tr=500000; tr<maxTransition; tr+=500000) {
      final double  y = (double)tr/ maxTransition * BAR_HEIGHT;
      writer.format("\\psline[linewidth=.4pt](-5,%.2f)(5,%.2f)",y, y);
      final double label = tr/1e6;
      writer.format("\\rput[r]{0}(-7,%.2f){\\scriptsize %.1f}", y, label);
    }
    writer.println("\\psline[linewidth=.1pt](-10,0)(766,0)");
    writer.format("\\psline[arrows=->,arrowsize=15,arrowlength=2,"
      + "arrowinset=0.3,linewidth=.1pt](0,0)(0,%d)",BAR_HEIGHT+30);
    writer.format("\\rput[tr]{0}(-7,%d){\\scriptsize $\\times 10^6$}",BAR_HEIGHT+30);
    writer.println("\\end{pspicture}");
    writer.close();
  }


  //#########################################################################
  //# Auxiliary Methods
  private int findIndex(final String[] array, final String label)
  {
    for (int i = 0; i < array.length; i++) {
      if (array[i].equals(label)) {
        return i;
      }
    }
    return -1;
  }


  //#########################################################################
  //# Inner Class Configuration
  private static class Configuration
  {
    //#######################################################################
    //# Constructor
    private Configuration(final AbstractCompositionalSynthesizer synthesizer,
                          final AbstractionProcedureCreator factory)
    {
      mSynthesizer = synthesizer;
      mFactory = factory;
    }

    //#######################################################################
    //# Overrides for java.lang.Object
    @Override
    public String toString()
    {
      String name = ProxyTools.getShortClassName(mSynthesizer);
      if (name.startsWith("Compositional")) {
        name = name.substring(13);
      }
      if (name.endsWith("Synthesizer")) {
        name = name.substring(0,name.length()-11);
      }
      name += "_" + mFactory.toString();
      return name;
    }

    //#######################################################################
    //# Data Members
    private final AbstractCompositionalSynthesizer mSynthesizer;
    private final AbstractionProcedureCreator mFactory;
  }


  //#########################################################################
  //# Inner Class ModelInfo
  private static class ModelInfo
  {
    //#######################################################################
    //# Constructor
    ModelInfo(final String fullName,
              final String shortName,
              final boolean controllable,
              final boolean nonblocking,
              final String size)
    {
      mFullName = fullName;
      mShortName = shortName;
      mControllable = controllable;
      mNonblocking = nonblocking;
      mSize = size;
    }

    //#######################################################################
    //# Simple Access
    private String getTableText(final int verbosity)
    {
      final StringBuilder builder = new StringBuilder(mShortName);
      if (verbosity >= VERBOSITY_NORMAL) {
        builder.append(" & ");
        builder.append(mControllable);
        builder.append(" & ");
        builder.append(mNonblocking);
      }
      builder.append(" & ");
      builder.append(mSize);
      return builder.toString();
    }

    private String getShortName()
    {
      return mShortName;
    }

    //#######################################################################
    //# Data Members
    @SuppressWarnings("unused")
    private final String mFullName;
    private final String mShortName;
    private final boolean mControllable;
    private final boolean mNonblocking;
    private final String mSize;
  }


  //#########################################################################
  //# Inner Class Result
  private static class Result
  {

    //#######################################################################
    //# Constructor
    private Result(final String name, final double time, final int totStates,
                   final int totTrans, final int memory,
                   final String heuristics)
    {
      mName = name;
      mTime = time;
      mTotStates = totStates;
      mTotTrans = totTrans;
      mMemory = memory;
      mHeuristics = heuristics;
    }

    public double getTime()
    {
      return mTime;
    }

    public int getTotalTransitions()
    {
      return mTotTrans;
    }

    public int getTotalStates()
    {
      return mTotStates;
    }

    public int getMemory()
    {
      return mMemory;
    }

    public String getHeuristics()
    {
      return mHeuristics;
    }

    //#######################################################################
    //# Data Members
    @SuppressWarnings("unused")
    private final String mName;
    private final double mTime;
    private final int mTotStates;
    private final int mTotTrans;
    private final int mMemory;
    private final String mHeuristics;
  }


  //#######################################################################
  //# Inner Class LowerCaseComparator
  private static class LowerCaseComparator implements Comparator<String>
  {
    @Override
    public int compare(final String s1, final String s2)
    {
      if (s1.length() == 0) {
        return s2.length() == 0 ? 0 : -1;
      } else if (s2.length() == 0) {
        return 1;
      }
      final boolean digit1 = Character.isDigit(s1.charAt(0));
      final boolean digit2 = Character.isDigit(s2.charAt(0));
      if (digit1 != digit2) {
        return digit1 ? 1 : -1;
      }
      return s1.compareToIgnoreCase(s2);
    }
  }


  //#######################################################################
  //# Data Members
  private final Map<String, ModelInfo> mModelInfoMap =
    new TreeMap<String, ModelInfo>(new LowerCaseComparator());
  private final List<Map<String,Result>> mResults =
    new ArrayList<Map<String,Result>>(2);
  private final int mVerbosity;

  private static final int GROUP_WIDTH = 42;
  private static final int BAR_WIDTH = 16;
  private static final double OFFSET_IN_GROUP = 0.5*GROUP_WIDTH - BAR_WIDTH;
  private static final int BAR_HEIGHT = 300;
  private static final String[] BAR_COLOUR = {"fillstyle=solid,fillcolor=lightgray",
                                              "fillstyle=solid,fillcolor=gray"};

  private static final int VERBOSITY_BRIEF = 0;
  private static final int VERBOSITY_NORMAL = 1;
}
