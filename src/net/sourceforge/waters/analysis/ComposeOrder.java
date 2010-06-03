//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ProjectingControllabilityChecker
//###########################################################################
//# $Id: ProjectingControllabilityChecker.java 4468 2008-11-01 21:54:58Z robi $
//###########################################################################

package net.sourceforge.waters.analysis;

import gnu.trove.THashMap;
import gnu.trove.THashSet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import net.sourceforge.waters.analysis.modular.BiSimulator;
import net.sourceforge.waters.analysis.modular.ConfRevBiSimulator;
import net.sourceforge.waters.analysis.modular.BlockedEvents;
import net.sourceforge.waters.analysis.modular.NonDeterministicComposer;
import net.sourceforge.waters.cpp.analysis.NativeConflictChecker;
import net.sourceforge.waters.model.analysis.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;

import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import gnu.trove.TObjectIntHashMap;
import java.util.Collections;
import net.sourceforge.waters.analysis.compnb.IncomingEquivalent;
import gnu.trove.TObjectDoubleHashMap;
import java.security.AccessControlException;
import net.sourceforge.waters.analysis.modular.TransBiSimulator;
import net.sourceforge.waters.analysis.annotation.SilentOutGoing;
import java.io.FileReader;
import java.io.BufferedReader;
import net.sourceforge.waters.analysis.annotation.MakeBisimiliar;
import net.sourceforge.waters.analysis.annotation.BiSimulatorRedundant;
import net.sourceforge.waters.analysis.annotation.RemoveEvents;
import net.sourceforge.waters.analysis.modular.Composer;
import net.sourceforge.waters.analysis.annotation.OptimisticBiSimulatorRedundant;
import org.omg.SendingContext.RunTime;
import java.lang.Runtime;
import java.util.Random;


/**
 * The projectiong controllability check algorithm.
 *
 * @author Simon Ware
 */

public class ComposeOrder
{
  private static final long SEED = 564621;
  private static final int MAXTEMP = 100000;
  
  private final ProductDESProxy mModel;
  private final Set<AutomatonProxy> mAutomata;
  
  //#########################################################################
  //# Constructors
  public ComposeOrder(final ProductDESProxy model, Set<AutomatonProxy> automata)
  {
    mModel = model;
    mAutomata = automata;
  }
  
  public void run()
  {
    int temperature = MAXTEMP;
    Map<EventProxy, Set<AutomatonProxy>> localmap = new THashMap<EventProxy, Set<AutomatonProxy>>();
    Map<AutomatonProxy, TObjectIntHashMap<EventProxy>> transmap = new THashMap<AutomatonProxy, TObjectIntHashMap<EventProxy>>();
    Random rand = new Random(SEED);
    List<Composition> compositions = new ArrayList<Composition>();
    List<UpperComp> uppercomps = new ArrayList<UpperComp>();
    for (AutomatonProxy a : mAutomata) {
      compositions.add(new BaseComp(a));
      TObjectIntHashMap etrans = new TObjectIntHashMap<EventProxy>();
      transmap.put(a, etrans);
      for (EventProxy e : a.getEvents()) {
        Set<AutomatonProxy> auts = localmap.get(e);
        if (auts == null) {
          auts = new THashSet<AutomatonProxy>();
          localmap.put(e, auts);
        }
        auts.add(a);
      }
      for (TransitionProxy t : a.getTransitions()) {
        int tnum = etrans.get(t.getEvent());
        tnum++;
        etrans.put(t.getEvent(), tnum);
      }
    }
    UpperComp uc = null;
    int aggregate = 0;
    //TObjectIntHashMap<Composition> compint = new TObjectIntHashMap<Composition>();
    while (compositions.size() != 1) {
      Composition comp1 = compositions.remove(rand.nextInt(compositions.size()));
      Composition comp2 = compositions.remove(rand.nextInt(compositions.size()));
      uc = new UpperComp(comp1, comp2, localmap, transmap);
      uppercomps.add(uc);
      //compint.put(uc, uppercomps.size());
      compositions.add(uc);
      aggregate += uc.numNonLocal();
    }
    while (temperature != 0) {
      //System.out.println("temperature: " + temperature);
      if (temperature % 1000 == 0) {System.out.println("temperature: " + temperature);}
      int root = rand.nextInt(uppercomps.size());
      //System.out.println("root: " + root);
      Composition branch1 = uppercomps.get(root).getComp1();
      Composition branch2 = uppercomps.get(root).getComp2();
      Composition swapbranch = null;
      UpperComp lowerbranch = null;
      int first = 0;
      int second = 0;
      if (branch1 instanceof UpperComp && branch2 instanceof UpperComp) {
        if (rand.nextBoolean()) {
          swapbranch = branch1;
          lowerbranch = (UpperComp)branch2;
          first = 1;
        } else {
          swapbranch = branch2;
          lowerbranch = (UpperComp)branch1;
          first = 2;
        }
      } else if (branch1 instanceof UpperComp) {
        lowerbranch = (UpperComp)branch1;
        swapbranch = branch2;
        first = 2;
      } else if (branch2 instanceof UpperComp) {
        lowerbranch = (UpperComp)branch2;
        swapbranch = branch1;
        first = 1;
      } else {
        continue;
      }
      //System.out.println("acceptable");
      Composition keepbranch = null;
      Composition swapbranch2 = null;
      if (rand.nextBoolean()) {
        keepbranch = lowerbranch.getComp1();
        swapbranch2 = lowerbranch.getComp2();
        second = 2;
      } else {
        keepbranch = lowerbranch.getComp2();
        swapbranch2 = lowerbranch.getComp1();
        second = 1;
      }
      double newnonlocal = calcnum(keepbranch, swapbranch, localmap, transmap);
      boolean doit = true;
      if (newnonlocal > lowerbranch.numNonLocal()) {
        if (rand.nextInt(MAXTEMP) > temperature) {
          doit = false;
        }
      }
      if (doit) {
        if (first == 1) {
          uppercomps.get(root).setComp1(swapbranch2);
        } else {
          uppercomps.get(root).setComp2(swapbranch2);
        }
        if (second == 1) {
          lowerbranch.setComp1(swapbranch);
        } else {
          lowerbranch.setComp2(swapbranch);
        }
        lowerbranch.setnumlocal(newnonlocal);
      }
      temperature--;
    }
    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter("/home/darius/supremicastuff/" + mModel.getName()));
      output(uc, writer);
      writer.flush();
      writer.close();
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
  
  private String output(Composition uc, BufferedWriter writer) throws Throwable
  {
    if (uc.getComp1() == null) {
      return uc.getComp().get(0).getName();
    }
    String name1 = output(uc.getComp1(), writer);
    String name2 = output(uc.getComp2(), writer);
    writer.write(name1);
    writer.newLine();
    writer.write(name2);
    writer.newLine();
    writer.newLine();
    if (name1.compareTo(name2) < 0) {
      return name1 + "||" + name2;
    } else {
      return name2 + "||" + name1;
    }
  }

  private interface Composition
  {
    public Composition getComp1();
    
    public Composition getComp2();
    
    public List<AutomatonProxy> getComp();
    
    public double numNonLocal();
  }
  
  private double calcnum(List<AutomatonProxy> automata, Map<EventProxy, Set<AutomatonProxy>> map,
                      Map<AutomatonProxy, TObjectIntHashMap<EventProxy>> tmap)
  {
    Set<EventProxy> events = new THashSet<EventProxy>();
    for (AutomatonProxy a : automata) {
      events.addAll(a.getEvents());
    }
    double num = 0;
    double local = 0;
    for (EventProxy e : events) {
      double num2 = 1;
      for (AutomatonProxy a : automata) {
        if (!a.getEvents().contains(e)) {
          num2 *= (double)tmap.get(a).get(e);
        } else {
          num2 *= ((double)a.getStates().size() / (double)2);
        }
      }
      if (!automata.containsAll(map.get(e))) {
        num += num2;
      } else {
        local += num2;
      }
    }
    double total = num + local;
    //double ratio = local / total;
    return num / local;
  }
  
  private double calcnum(Composition comp1, Composition comp2, Map<EventProxy, Set<AutomatonProxy>> map,
                      Map<AutomatonProxy, TObjectIntHashMap<EventProxy>> tmap)
  {
    List<AutomatonProxy> automata = comp1.getComp();
    automata.addAll(comp2.getComp());
    return calcnum(automata, map, tmap);
  }
  
  private double calcnum(Composition comp, Map<EventProxy, Set<AutomatonProxy>> map,
                      Map<AutomatonProxy, TObjectIntHashMap<EventProxy>> tmap)
  {
    return calcnum(comp.getComp(), map, tmap);
  }
  
  private class BaseComp
    implements Composition
  {
    private AutomatonProxy mAut;
    
    public BaseComp(AutomatonProxy aut)
    {
      mAut = aut;
    }
    
    public Composition getComp1()
    {
      return null;
    }
    
    public Composition getComp2()
    {
      return null;
    }
    
    public List<AutomatonProxy> getComp()
    {
      List<AutomatonProxy> list = new LinkedList<AutomatonProxy>();
      list.add(mAut);
      return list;
    }
    
    public double numNonLocal()
    {
      return 0;
    }
  }
  
  private class UpperComp
    implements Composition
  {
    private Composition mComp1;
    private Composition mComp2;
    private double mNum = 0;
    
    public UpperComp(Composition comp1, Composition comp2, Map<EventProxy, Set<AutomatonProxy>> map,
                     Map<AutomatonProxy, TObjectIntHashMap<EventProxy>> tmap)
    {
      mComp1 = comp1;
      mComp2 = comp2;
      mNum = calcnum(this, map, tmap);
    }
    
    public Composition getComp1()
    {
      return mComp1;
    }
    
    public Composition getComp2()
    {
      return mComp2;
    }
    
    public void setComp1(Composition comp1)
    {
      mComp1 = comp1;
    }
    
    public void setComp2(Composition comp2)
    {
      mComp2 = comp2;
    }
    
    public List<AutomatonProxy> getComp()
    {
      List<AutomatonProxy> list = getComp1().getComp();
      list.addAll(getComp2().getComp());
      return list;
    }
    
    public double numNonLocal()
    {
      return mNum;
    }
    
    public void setnumlocal(double local)
    {
      mNum = local;
    }
  }
}
