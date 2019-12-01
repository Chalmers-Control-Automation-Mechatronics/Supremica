package org.supremica.automata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.StringTokenizer;

import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Zenuity 2019 Hackfest
 *
 * Algorithm that converts sufficient requirement into specification EFA.
 *
 * @author zhefei
 */

public class FTSpecEFABuilder
{

  private final Logger logger = LogManager.getLogger(FTSpecEFABuilder.class);

  private final ModuleSubject module;
  private final HashMap<String, HashSet<String>> spec2Events;

  private static final String SPEC_PREFIX = "Spec_";
  private static int specCounter = 1;

  public FTSpecEFABuilder(final File spec, final ModuleSubject module)
  {
    this.module = module;
    this.spec2Events = new HashMap<>();

    // fill out spec2Events by parsing the file
    FileReader fReader = null;
    BufferedReader bReader = null;
    try {
      fReader = new FileReader(spec);
      bReader = new BufferedReader(fReader);
      String formula = null;
      while((formula = bReader.readLine()) != null) {
        StringTokenizer st = new StringTokenizer(formula, "->");
        final String premise = st.nextToken();
        // 1. Assume that the conclusion is always system failure location
        //    others are possible but are not implemented.
        // 2. Only "&" is considered, since "|" can be split.
        st = new StringTokenizer(premise, "&");
        final String specName =
          FTSpecEFABuilder.SPEC_PREFIX + FTSpecEFABuilder.specCounter;
        spec2Events.put(specName, new HashSet<String>());
        FTSpecEFABuilder.specCounter++;
        while(st.hasMoreTokens()) {
          spec2Events.get(specName).add(st.nextToken().trim());
        }
      }
    }
    catch (final FileNotFoundException e) {}
    catch (final IOException e) {e.printStackTrace();}

    // debug
    for (final Map.Entry<String,HashSet<String>> entry
      : spec2Events.entrySet()) {
      final StringJoiner sj = new StringJoiner(",");
      for (final String e : entry.getValue()) {
        sj.add(e);
      }
      logger.debug(entry.getKey() + ": " + sj.toString());
    }
  }

  public void buildEFA() {
    // build specification EFA for each entry of spec2Events
    final ModuleSubject m = this.module;
    final ExtendedAutomata exAutomata = new ExtendedAutomata(m);
    final String[] specs =
      spec2Events.keySet().toArray(new String[spec2Events.size()]);
    Arrays.sort(specs);
    for (final String sp: specs) {
      logger.debug("Build spec EFA " + sp);
      final HashSet<String> events = spec2Events.get(sp);
      final ExtendedAutomaton efa =
        new ExtendedAutomaton(sp, ComponentKind.SPEC);
      // build EFA
      for(int i = 0; i <= events.size(); i++) {
        // add location
        if (i == 0) { // initial
          efa.addState(sp + i, false, true, false);
        }
        else if (i == events.size()) {
          efa.addState(sp + i, true, false, false);
        }
        else {
          efa.addState(sp + i, false, false, false);
        }
        // add transition, labeled with events
        if (i != 0) {
          for (final String event : events) {
            efa.addTransition(sp + (i - 1), sp + i, event, null, null);
          }
        }
      }
      // add blocked events ...
      final List<IdentifierProxy> eventIdentifiers = new ArrayList<>();
      for (final EventDeclProxy e : module.getEventDeclList()) {
        final String n = e.getName();
        if (e.getKind() == EventKind.CONTROLLABLE && !events.contains(n)) {
          eventIdentifiers.add(new SimpleIdentifierSubject(n));
        }
      }
      final LabelBlockSubject blockedEvents =
        new LabelBlockSubject(eventIdentifiers, null);
      efa.getGraph().setBlockedEvents(blockedEvents);

      // add efa to the module
      exAutomata.addAutomaton(efa);
    }
  }
}
