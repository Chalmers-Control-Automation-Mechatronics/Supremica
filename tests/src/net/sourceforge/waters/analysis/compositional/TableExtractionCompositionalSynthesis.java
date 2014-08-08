//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   SelfRunningExperimentCompositionalSynthesis
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.io.BufferedReader;
import java.io.File;
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
    final TableExtractionCompositionalSynthesis extractor =
      new TableExtractionCompositionalSynthesis();
    final File heuristicFile = new File(args[0]);
    final File compareFile = new File(args[1]);
    extractor.extract(heuristicFile, compareFile);
  }


  //#########################################################################
  //# Constructor
  private TableExtractionCompositionalSynthesis()
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
    createModelInfo("tbed_uncont", "tbed\\_noderailb", true, false, "3.6 \\cdot 10^{12}");
    createModelInfo("verriegel3b", "verriegel3b", false, true, "1.3 \\cdot 10^{9}");
    createModelInfo("verriegel4b", "verriegel4b", false, true, "6.2 \\cdot 10^{10}");
    createModelInfo("6linka", "6linka", false,true,"2.4 \\cdot 10^{14}");
    createModelInfo("6linki", "6linki", false,true,"2.7 \\cdot 10^{14}");
    createModelInfo("6linkp", "6linkp", false,true,"4.2 \\cdot 10^{14}");
    createModelInfo("6linkre", "6linkre", false,true,"6.2 \\cdot 10^{14}");
  }

  private void createModelInfo(final String fullName, final String shortName,
            final boolean nonblocking, final boolean controllable,  final String size)
  {
    final ModelInfo info = new ModelInfo(fullName, shortName, controllable, nonblocking, size);
    mModelInfoMap.put(fullName, info);
  }


  //#########################################################################
  //# Table Extraction
  private void extract(final File heuristicFile, final File compareFile)
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
        compareSelectingMethod = CompositionalSelectionHeuristicFactory.MaxL;
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
      printHeuristicTable(heuristicFile);
      for (final Map<String, Result> map: mResults) {
        map.clear();
      }
      int mapIndex = 0;
      for (final Configuration config: configurations) {
        readCsvFile(compareIndex, comparePreselectingMethod,
                    compareSelectingMethod, mapIndex, config);
        mapIndex++;
      }
      printStateComparisonTable(compareFile);
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

  private void readCsvFile(final File fileName, final int mapIndex, final String heuristics) throws IOException
  {
    if (fileName.exists()) {
      final int nameColumn = 0;
      int timeColumn = 0;
      int totalTrColumn = 0;
      int totalStatesColumn = 0;
      int memColumn = 0;
      int resultColumn = 0;
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
              final Map<String,Result> map = mResults.get(mapIndex);
              final Result oldResult = map.get(name);
              if (oldResult == null || oldResult.getMemory() > supMem) {
                final Result newResult =new Result(name, time, totStates,
                                                   totTrans, supMem, heuristics);
                map.put(name, newResult);
              }
            }
          }
        }
      }
      br.close();
    }
  }

  private void printHeuristicTable(final File outputFile) throws IOException
  {
    final OutputStream stream = new FileOutputStream(outputFile);
    final PrintWriter writer = new PrintWriter(stream);
    for (final Map.Entry<String,ModelInfo> entry : mModelInfoMap.entrySet()) {
      final String name = entry.getKey();
      final ModelInfo info = entry.getValue();
      String text = info.getTableText();
      System.out.print(text);
      writer.print(text);
      for (int mapIndex = 0; mapIndex <mResults.size(); mapIndex++) {
        final Map<String, Result> map = mResults.get(mapIndex);
        final Result result = map.get(name);
        if (result == null) {
          writer.print(" & & &");
          System.out.print(" & & &" );
        } else {
          final double time = result.getTime();
          final int memory = result.getMemory();
          final String heuristics = result.getHeuristics();
          text = " & " + String.format((Locale) null, "%.2f\\,s", time)
            + " & " + memory  + " & " + heuristics;
          System.out.print(text);
          writer.print(text);
        }
      }
      writer.println(" \\\\");
      System.out.println(" \\\\");
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
      String text = info.getTableText();
      System.out.print(text);
      writer.print(text);
      for (int mapIndex = 0; mapIndex <mResults.size(); mapIndex++) {
        final Map<String, Result> map = mResults.get(mapIndex);
        final Result result = map.get(name);
        if (result == null) {
          writer.print(" & & & &");
          System.out.print(" & & & &" );
        } else {
          final int totStates = result.getTotalStates();
          final int totTrans = result.getTotalTransition();
          final double time = result.getTime();
          final int memory = result.getMemory();
          text = " & " + totStates + " & " + totTrans + " & " +
                 String.format((Locale) null, "%.2f\\,s", time) + " & " +
                 memory;
          System.out.print(text);
          writer.print(text);
        }
      }
      writer.println(" \\\\");
      System.out.println(" \\\\");
    }

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
    ModelInfo(final String fullName, final String shortName,
              final boolean controllable,
              final boolean nonblocking, final String size)
    {
      mFullName = fullName;
      mShortName = shortName;
      mControllable = controllable;
      mNonblocking = nonblocking;
      mSize = size;
    }

    //#######################################################################
    //# Simple Access
    private String getTableText()
    {
      return mShortName + " & " + mControllable + " & " + mNonblocking +
             " & " + mSize;
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

    public int getTotalTransition()
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
      return s1.compareToIgnoreCase(s2);
    }
  }


  //#######################################################################
  //# Data Members
  private final Map<String, ModelInfo> mModelInfoMap =
    new TreeMap<String, ModelInfo>(new LowerCaseComparator());
  private final List<Map<String,Result>> mResults =
    new ArrayList<Map<String,Result>>(2);

}
