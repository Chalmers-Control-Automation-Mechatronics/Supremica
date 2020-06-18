//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.analysis.annotation;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.TransitionProxy;


/**
 * The projecting controllability check algorithm.
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
  public ComposeOrder(final ProductDESProxy model, final Set<AutomatonProxy> automata)
  {
    mModel = model;
    mAutomata = automata;
  }

  public void run()
  {
    int temperature = MAXTEMP;
    final Map<EventProxy, Set<AutomatonProxy>> localmap = new THashMap<EventProxy, Set<AutomatonProxy>>();
    final Map<AutomatonProxy, TObjectIntHashMap<EventProxy>> transmap = new THashMap<AutomatonProxy, TObjectIntHashMap<EventProxy>>();
    final Random rand = new Random(SEED);
    final List<Composition> compositions = new ArrayList<Composition>();
    final List<UpperComp> uppercomps = new ArrayList<UpperComp>();
    for (final AutomatonProxy a : mAutomata) {
      compositions.add(new BaseComp(a));
      final TObjectIntHashMap<EventProxy> etrans = new TObjectIntHashMap<EventProxy>();
      transmap.put(a, etrans);
      for (final EventProxy e : a.getEvents()) {
        Set<AutomatonProxy> auts = localmap.get(e);
        if (auts == null) {
          auts = new THashSet<AutomatonProxy>();
          localmap.put(e, auts);
        }
        auts.add(a);
      }
      for (final TransitionProxy t : a.getTransitions()) {
        int tnum = etrans.get(t.getEvent());
        tnum++;
        etrans.put(t.getEvent(), tnum);
      }
    }
    UpperComp uc = null;
    //TObjectIntHashMap<Composition> compint = new TObjectIntHashMap<Composition>();
    while (compositions.size() != 1) {
      final Composition comp1 = compositions.remove(rand.nextInt(compositions.size()));
      final Composition comp2 = compositions.remove(rand.nextInt(compositions.size()));
      uc = new UpperComp(comp1, comp2, localmap, transmap);
      uppercomps.add(uc);
      //compint.put(uc, uppercomps.size());
      compositions.add(uc);
    }
    while (temperature != 0) {
      //System.out.println("temperature: " + temperature);
      if (temperature % 1000 == 0) {System.out.println("temperature: " + temperature);}
      final int root = rand.nextInt(uppercomps.size());
      //System.out.println("root: " + root);
      final Composition branch1 = uppercomps.get(root).getComp1();
      final Composition branch2 = uppercomps.get(root).getComp2();
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
      final double newnonlocal = calcnum(keepbranch, swapbranch, localmap, transmap);
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
    } catch (final Throwable t) {
      t.printStackTrace();
    }
  }

  private String output(final Composition uc, final BufferedWriter writer) throws Throwable
  {
    if (uc.getComp1() == null) {
      return uc.getComp().get(0).getName();
    }
    final String name1 = output(uc.getComp1(), writer);
    final String name2 = output(uc.getComp2(), writer);
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

  private double calcnum(final List<AutomatonProxy> automata, final Map<EventProxy, Set<AutomatonProxy>> map,
                      final Map<AutomatonProxy, TObjectIntHashMap<EventProxy>> tmap)
  {
    final Set<EventProxy> events = new THashSet<EventProxy>();
    for (final AutomatonProxy a : automata) {
      events.addAll(a.getEvents());
    }
    double num = 0;
    double local = 0;
    for (final EventProxy e : events) {
      double num2 = 1;
      for (final AutomatonProxy a : automata) {
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
    return num / local;
  }

  private double calcnum(final Composition comp1, final Composition comp2, final Map<EventProxy, Set<AutomatonProxy>> map,
                      final Map<AutomatonProxy, TObjectIntHashMap<EventProxy>> tmap)
  {
    final List<AutomatonProxy> automata = comp1.getComp();
    automata.addAll(comp2.getComp());
    return calcnum(automata, map, tmap);
  }

  private double calcnum(final Composition comp, final Map<EventProxy, Set<AutomatonProxy>> map,
                      final Map<AutomatonProxy, TObjectIntHashMap<EventProxy>> tmap)
  {
    return calcnum(comp.getComp(), map, tmap);
  }

  private class BaseComp
    implements Composition
  {
    private final AutomatonProxy mAut;

    public BaseComp(final AutomatonProxy aut)
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
      final List<AutomatonProxy> list = new LinkedList<AutomatonProxy>();
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

    public UpperComp(final Composition comp1, final Composition comp2, final Map<EventProxy, Set<AutomatonProxy>> map,
                     final Map<AutomatonProxy, TObjectIntHashMap<EventProxy>> tmap)
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

    public void setComp1(final Composition comp1)
    {
      mComp1 = comp1;
    }

    public void setComp2(final Composition comp2)
    {
      mComp2 = comp2;
    }

    public List<AutomatonProxy> getComp()
    {
      final List<AutomatonProxy> list = getComp1().getComp();
      list.addAll(getComp2().getComp());
      return list;
    }

    public double numNonLocal()
    {
      return mNum;
    }

    public void setnumlocal(final double local)
    {
      mNum = local;
    }
  }
}
