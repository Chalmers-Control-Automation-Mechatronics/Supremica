
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
package org.supremica.automata;

import java.util.*;
import org.supremica.log.*;
import org.supremica.util.Args;
import org.supremica.automata.IO.ProjectBuildFromXML;
import javax.swing.JOptionPane;
import java.io.File;
import java.net.URL;
import java.awt.Component;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.plain.base.DocumentElement;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
/**
 * An ordered set of Automaton-objects.
 * @see Automaton
 */
public class Automata
    extends DocumentElement
    implements AutomatonListener, Iterable<Automaton>, ProductDESProxy
{
    private static final String defaultName = "Untitled";
    private static Logger logger = LoggerFactory.createLogger(Automata.class);
    private ArrayList<Automaton> theAutomata;
    private HashMap<String,Automaton> nameMap;
    private String name = defaultName;
    private String comment = null;
    private AutomataListeners listeners = null;
    
    public Automata()
    {
        super(defaultName);
        theAutomata = new ArrayList<Automaton>();
        nameMap = new HashMap<String,Automaton>();
    }
    
    /**
     * Copy constructor that also makes a (deep) copy of all the
     * automata contained in oldAutomata. Calling this is equal to
     * calling Automata(oldAutomata, false)
     */
    public Automata(Automata oldAutomata)
    {
        this(oldAutomata, false);
    }
    
    /**
     * Construct an Automata object with a single automaton.
     */
    public Automata(Automaton theAutomaton)
    {
        this();
        
        addAutomaton(theAutomaton);
    }
    
    /**
     * Does not make a new copy of the contained automata unless shallowCopy is false
     */
    public Automata(Automata oldAutomata, boolean shallowCopy)
    {
        this();
        
        if (shallowCopy)
        {
            shallowAutomataCopy(oldAutomata);
        }
        else
        {
            deepAutomataCopy(oldAutomata);
        }
    }
    
    public Automata(URL url)
    throws Exception
    {
        super(defaultName);
        ProjectBuildFromXML builder = new ProjectBuildFromXML();
        Project theProject = builder.build(url);
        shallowAutomataCopy(theProject);
    }
    
    public Automata(File file)
    throws Exception
    {
        this(file.toURL());
    }
    
    public Automata clone()
    {
        //Automata clonedObject = (Automata)super.clone();
        
        return new Automata(this, false);
    }
    
    private void deepAutomataCopy(Automata oldAutomata)
    {
        for (Automaton automaton : oldAutomata)
        {
            addAutomaton(new Automaton(automaton));
        }
    }
    
    private void shallowAutomataCopy(Automata oldAutomata)
    {
        for (Automaton automaton : oldAutomata)
        {
            addAutomaton(automaton);
        }
    }
    
    /**
     * Adds the automaton aut to this Automata. If there already is an automaton with the
     * same name as aut, aut is NOT added.
     */
    public void addAutomaton(Automaton aut)
    {
        if (!containsAutomaton(aut.getName()))
        {
            theAutomata.add(aut);
            nameMap.put(aut.getName(), aut);
            aut.addListener(this);
            notifyListeners(AutomataListeners.MODE_AUTOMATON_ADDED, aut);
        }
    }
    
    /**
     * Adds all automata in 'automata' to this Automata. Automata with the same name as
     * already present automata are NOT added.
     */
    public void addAutomata(Automata automata)
    {
        for (Automaton automaton : automata)
        {
            addAutomaton(automaton);
        }
    }
    
    /**
     * Iterates over all automata in automata.
     * If an automaton with the same name is not in the cureent
     * automata then the automata is added. If there already is an automaton
     * then that automaton is replace with the new one.
     * @param automata
     */
    public void updateAutomata(Automata automata)
    {
        for (Automaton automaton : automata)
        {
            if (containsAutomaton(automaton.getName()))
            {
                removeAutomaton(automaton.getName());
            }
            
            addAutomaton(automaton);
        }
    }
    
    public void removeAutomaton(Automaton aut)
    {
        if (containsAutomaton(aut))
        {
            theAutomata.remove(aut);
            nameMap.remove(aut.getName());
            notifyListeners(AutomataListeners.MODE_AUTOMATON_REMOVED, aut);
        }
    }
    
    public void removeAutomata(Automata automata)
    {
        for (Automaton automaton : automata)
        {
            removeAutomaton(automaton.getName());
        }
    }
    
    public void removeAutomaton(String name)
    {
        Automaton currAutomaton = getAutomaton(name);
        
        if (currAutomaton != null)
        {
            removeAutomaton(currAutomaton);
        }
    }
    
    // Moves automaton one step up or down in the ArrayList
    public void moveAutomaton(Automaton aut, boolean directionIsUp)
    {
        int firstAutomatonIndex = theAutomata.indexOf(aut);
        int secondAutomatonIndex;
        
        if (directionIsUp)
        {
            secondAutomatonIndex = firstAutomatonIndex - 1;
        }
        else
        {
            secondAutomatonIndex = firstAutomatonIndex + 1;
        }
        
        Automaton firstAutomaton = aut;
        Automaton secondAutomaton = (Automaton) theAutomata.get(secondAutomatonIndex);
        
        theAutomata.set(firstAutomatonIndex, secondAutomaton);
        theAutomata.set(secondAutomatonIndex, firstAutomaton);
        notifyListeners();
    }
    
    // Moves automaton to arbitrary destination
    public void moveAutomaton(Automaton aut, int destinationIndex)
    {
        int originIndex = theAutomata.indexOf(aut);
        
        if (originIndex > destinationIndex)
        {
            for (int i = originIndex; i > destinationIndex; i--)
            {
                moveAutomaton(aut, true);
            }
        }
        else if (originIndex < destinationIndex)
        {
            for (int i = originIndex; i < destinationIndex; i++)
            {
                moveAutomaton(aut, false);
            }
        }
    }
    
    public void renameAutomaton(Automaton aut, String newName)
    {
        aut.setName(newName);
    }
    
    
    public Iterator<Automaton> iterator()
    {
        return theAutomata.iterator();
    }
    
    /**
     * Iterates backwards through the automata... necessary
     * in the automataMove_actionPerformed in ActionMan when
     * moving down
     *
     *@see org.supremica.gui.ActionMan
     */
    public Iterator<Automaton> backwardsIterator()
    {
        ArrayList<Automaton> backwardList = new ArrayList<Automaton>();
        Iterator<Automaton> forwardIterator = iterator();
        while (forwardIterator.hasNext())
        {
            backwardList.add(0, forwardIterator.next());
        }
        
        return backwardList.iterator();
    }
    
    public Iterator<Automaton> plantIterator()
    {
        return new AutomatonTypeIterator(AutomatonType.PLANT);
    }
    
    /**
     * Returns a new automata object with all plant
     * in this automata. Note that this reuses the references
     * to the plant automata.
     */
    public Automata getPlantAutomata()
    {
        Automata newAutomata = new Automata();
        
        for (Iterator theIt = plantIterator(); theIt.hasNext(); )
        {
            Automaton currAutomaton = (Automaton) theIt.next();
            
            newAutomata.addAutomaton(currAutomaton);
        }
        
        return newAutomata;
    }
    
    public Iterator<Automaton> specificationIterator()
    {
        return new AutomatonTypeIterator(AutomatonType.SPECIFICATION);
    }
    
    /**
     * Returns a new automata object with all specifications
     * in this automata. Note that this reuses the references
     * to the specification automata.
     */
    public Automata getSpecificationAutomata()
    {
        Automata newAutomata = new Automata();
        
        for (Iterator theIt = specificationIterator(); theIt.hasNext(); )
        {
            Automaton currAutomaton = (Automaton) theIt.next();
            
            newAutomata.addAutomaton(currAutomaton);
        }
        
        return newAutomata;
    }
    
    public Iterator<Automaton> supervisorIterator()
    {
        return new AutomatonTypeIterator(AutomatonType.SUPERVISOR);
    }
    
    /**
     * Returns a new automata object with all supervisors
     * in this automata. Note that this reuses the references
     * to the supervisor automata.
     */
    public Automata getSupervisorAutomata()
    {
        Automata newAutomata = new Automata();
        
        for (Iterator theIt = supervisorIterator(); theIt.hasNext(); )
        {
            Automaton currAutomaton = (Automaton) theIt.next();
            
            newAutomata.addAutomaton(currAutomaton);
        }
        
        return newAutomata;
    }
    
    /**
     * Returns a new automata object with all specification and supervisor
     * automata in this automata. Note that this reuses the references
     * to the plant automata.
     */
    public Automata getSpecificationAndSupervisorAutomata()
    {
        /*
        Automata newAutomata = new Automata();
         
        for (Iterator theIt = specificationIterator(); theIt.hasNext(); )
        {
            Automaton currAutomaton = (Automaton) theIt.next();
         
            newAutomata.addAutomaton(currAutomaton);
        }
         
        for (Iterator theIt = supervisorIterator(); theIt.hasNext(); )
        {
            Automaton currAutomaton = (Automaton) theIt.next();
         
            newAutomata.addAutomaton(currAutomaton);
        }
         
        return newAutomata;
         */
        
        Automata newAutomata = getSpecificationAutomata();
        newAutomata.addAutomata(getSupervisorAutomata());
        
        return newAutomata;
    }
    
    /**
     * Returns true if all automata are deterministic
     */
    public boolean isDeterministic()
    {
        boolean deterministic = true;
        
        for (Automaton automaton : this)
        {
            if (!automaton.isDeterministic())
            {
                //logger.warn("Automaton " + automaton + " is not deterministic");
                deterministic = false;
            }
        }
        
        return deterministic;
    }
    
    /**
     * Returns true if all automata have initial states
     */
    public boolean hasInitialState()
    {
        for (Automaton automaton : this)
        {
            if (!automaton.hasInitialState())
            {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Returns true if each automaton has at least one accepting state
     * Of course, this is no guarantee that the composition will have
     * an accepting state.
     */
    public boolean hasAcceptingState()
    {
        for (Automaton automaton : this)
        {
            if (!automaton.hasAcceptingState())
            {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Returns true if any of the automata has a forbidden state.
     */
    public boolean hasForbiddenState()
    {
        for (Automaton automaton : this)
        {
            if (automaton.nbrOfForbiddenStates() > 0)
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Returns name of first automaton found that has no accepting states. Returns null
     * if all automata have at least one accepting state.
     */
    
        /*
        public String hasAcceptingState()
        {
                        for (Iterator automataIterator = iterator(); automataIterator.hasNext(); )
                        {
                                        Automaton automaton = (Automaton) automataIterator.next();
         
                                        if(!automaton.hasAcceptingState())
                                        {
                                                        return automaton.getName();
                                        }
                        }
                        return null;
        }
         */
    
    /**
     * Returns true if all automata have all events prioritized
     */
    public boolean isAllEventsPrioritized()
    {
        for (Automaton automaton : this)
        {
            if (!automaton.isAllEventsPrioritized())
            {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * True if all automata are plants
     */
    public boolean isAllAutomataPlants()
    {
        for (Automaton automaton : this)
        {
            if (automaton.getType() != AutomatonType.PLANT)
            {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * True if all automata are supervisors
     */
    public boolean isAllAutomataSupervisors()
    {
        for (Automaton automaton : this)
        {
            if (automaton.getType() != AutomatonType.SUPERVISOR)
            {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * True if all automata are specifications
     */
    public boolean isAllAutomataSpecifications()
    {
        for (Automaton automaton : this)
        {
            if (automaton.getType() != AutomatonType.SPECIFICATION)
            {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * True if NONE of the automata are plants.
     */
    public boolean hasNoPlants()
    {
        for (Automaton automaton : this)
        {
            if (automaton.getType() == AutomatonType.PLANT)
            {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * True if no automaton is  either specification OR supervisor.
     * This is good to for early termination in algorithms :)
     */
    public boolean hasNoSpecificationsAndSupervisors()
    {
        for (Automaton automaton : this)
        {
            if ((automaton.getType() == AutomatonType.SPECIFICATION) || (automaton.getType() == AutomatonType.SUPERVISOR))
            {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Returns true if at least one automaton has the event as prioritized.
     * Returns false if the event is not included in any alphabet or
     * all automata has this event as as non-prioritized.
     */
    public boolean isPrioritizedInAtleastOneAutomaton(LabeledEvent theEvent)
    {
        for (Automaton automaton : this)
        {
            if (automaton.isEventPrioritized(theEvent.getLabel()))
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Returns true if the system is really several systems, i.e. can be divided into sets of
     * automata that have disjoint alphabets.
     */
    private boolean isSeveralSystems()
    {
        Automata autA = new Automata(this.getFirstAutomaton());
        Alphabet unionAlpha = autA.getUnionAlphabet();
        boolean change = true;
        
        while (change)
        {
            change = false;
            for (Automaton theAut : this)
            {
                if (autA.containsAutomaton(theAut))
                {
                    continue;
                }
                Alphabet alpha = theAut.getAlphabet();
                
                                /*
                                // Compare the alphabets!
                                Alphabet diff = Alphabet.minus(unionAlpha, alpha);
                                if (diff.size() == unionAlpha.size())
                                {
                                        // Disjoint (so far)
                                }
                                else if (diff.size() > 0)
                                {
                                        // Not disjoint, new events in unionAlpha!
                                        autA.addAutomaton(theAut);
                                        unionAlpha.union(theAut.getAlphabet());
                                        change = true;
                                }
                                else
                                {
                                        // Not disjoint, no change!
                                        autA.addAutomaton(theAut);
                                }
                                 */
                if (alpha.hasCommonEvents(unionAlpha))
                {
                    // Not disjoint!
                    autA.addAutomaton(theAut);
                    unionAlpha.union(theAut.getAlphabet());
                    change = true;
                }
            }
        }
        
        // What's the result?
        if (autA.size() < this.size())
        {
            if (autA.size() > 1)
                logger.warn("Some of the selected automata share no events with the other " +
                    "selected automata. For example, the automata " + autA +
                    " are disconnected from the rest.");
            else
                logger.warn("Some of the selected automata share no events with the other " +
                    "selected automata. For example, the automaton " +
                    autA.getFirstAutomaton() + " is disconnected from the rest.");
            return true;
        }
        
        return false;
    }
    
    /**
     * Returns true if the controllability is consistent through all the automata.
     */
    private boolean isEventEpsilonConsistent(Alphabet unionAlphabet)
    {
        // Iterate over the alphabet and examine all automata
        for (LabeledEvent currEvent : unionAlphabet)
        {
            // Examine each automata
            for (Automaton automaton : this)
            {
                Alphabet currAlpha = automaton.getAlphabet();
                
                if (currAlpha.contains(currEvent.getLabel()))
                {
                    if (currEvent.isObservable() != currAlpha.getEvent(currEvent.getLabel()).isObservable())
                    {
                        logger.error("The event " + currEvent + " is not epsilon consistent.");
                        
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * Returns true if the controllability is consistent through all the automata.
     */
    public boolean isEventControllabilityConsistent()
    {
        // Get the union alphabet (ignoring consistency here)
        Alphabet unionAlphabet = getUnionAlphabet();
        
        return isEventControllabilityConsistent(unionAlphabet);
    }
    /**
     * Returns true if the controllability is consistent through all the automata.
     */
    public boolean isEventControllabilityConsistent(Alphabet unionAlphabet)
    {
        // Iterate over the alphabet and examine all automata
        for (LabeledEvent currEvent : unionAlphabet)
        {
            
            // Examine each automata
            for (Automaton automaton : this)
            {
                Alphabet currAlpha = automaton.getAlphabet();
                if (currAlpha.contains(currEvent.getLabel()))
                {
                    if (currEvent.isControllable() != currAlpha.getEvent(currEvent.getLabel()).isControllable())
                    {
                        logger.error("The event " + currEvent + " is not controllability consistent.");
                        
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * Returns true if any automaton has a self loop
     */
    public boolean hasSelfLoop()
    {
        // Examine each automata
        for (Automaton automaton : this)
        {
            if (automaton.hasSelfLoop())
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Returns the number of Automaton:s in this Automata.
     */
    public int size()
    {
        return theAutomata.size();
    }
    
    /**
     * Returns the number of Automaton:s in this Automata.
     */
    public int nbrOfAutomata()
    {
        return size();
    }
    
    public boolean containsAutomaton(String name)
    {
        return nameMap.containsKey(name);
    }
    
    public boolean containsAutomaton(Automaton otherAutomaton)
    {
        Automaton thisAutomaton = nameMap.get(otherAutomaton.getName());
        
        if (thisAutomaton == null)
        {
            return false;
        }
        
        return thisAutomaton == otherAutomaton;
    }
    
    /**
     * When exporting to files some automata names may be illegal.
     * In Windows the following characters are illegal in file names: \/:*?"<>|
     */
    public void normalizeAutomataNames()
    {
        // Examine each automata
        for (Automaton currAutomaton : this)
        {
            String currAutomatonName = currAutomaton.getName();
            String newAutomatonName = currAutomatonName.replace("|", "_");
            newAutomatonName = newAutomatonName.replace("(", "_");
            newAutomatonName = newAutomatonName.replace(")", "_");
            renameAutomaton(currAutomaton, newAutomatonName);
        }
    }
    
    /**
     * Set the synchronization indices. The returned alphabet is the union alphabet
     * and contains the synchronization index of all the events in this automata.
     */
    public Alphabet setIndices()
    {
        // Get the union alphabet (ignoring consistency)
        Alphabet theAlphabet = getUnionAlphabet();
        
        // Adjust the indices of the alphabet
        theAlphabet.setIndices();
        
        // Adjust the indices of the automata
        int i = 0;
        // Examine each automata
        for (Automaton automaton : this)
        {
            automaton.setIndices(i++, theAlphabet);
        }
        
        return theAlphabet;
    }
    
    /**
     * Returns the union alphabet of all represented automata.
     */
    public Alphabet getUnionAlphabet()
    {
        // Add all alphabets to a new one...
        Alphabet unionAlphabet = new Alphabet();
        // Examine each automata
        for (Automaton automaton : this)
        {
            unionAlphabet.union(automaton.getAlphabet());
        }
        return unionAlphabet;
    }
    
    /**
     * Returns the union alphabet of all observable events in the represented automata.
     */
    public Alphabet getObservableUnionAlphabet()
    {
        // Add all alphabets to a new one...
        Alphabet unionAlphabet = new Alphabet();
        // Examine each automata
        for (Automaton automaton : this)
        {
            unionAlphabet.union(automaton.getObservableAlphabet());
        }
        return unionAlphabet;
    }
    
    /**
     * Returns the alphabet of events that are in the automata
     * alphabet but not in the automaton alphabet.
     */
    public Alphabet getInverseAlphabet(Automaton automaton)
    {
        Alphabet automataAlphabet = getUnionAlphabet();
        return automataAlphabet.minus(automaton.getAlphabet());
    }
    
    public Set<AutomatonProxy> getAutomata()
    {
        Iterator<Automaton> iterator = iterator();
        Set<AutomatonProxy> automata = new HashSet<AutomatonProxy>();
        while(iterator.hasNext())
        {
            automata.add(iterator.next());
        }
        return automata;
    }
    
    public Set<EventProxy> getEvents()
    {
        return getUnionAlphabet().getWatersEventsWithPropositions();
    }
    
    public Automaton getAutomaton(String name)
    {
        return nameMap.get(name);
    }
    
    public Automaton getAutomatonAt(int i)
    {
        return theAutomata.get(i);
    }
    
    public Automaton getFirstAutomaton()
    {
        return getAutomatonAt(0);
    }
    
    /**
     * Returns index of automaton.
     */
    public int getAutomatonIndex(Automaton theAutomaton)
    {
        for (int i = 0; i < theAutomata.size(); i++)
        {
            Automaton currAutomaton = getAutomatonAt(i);
            
            if (currAutomaton == theAutomaton)
            {
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * Compares two automata objects for equality. This test is supposed
     * to a quick test, therefor we do not check for language equality
     * in this automaton. Note, that the automata must be in the same order
     * for this method to return true.
     */
    public boolean equalAutomata(Automata other)
    {
        //System.err.println("equalAutomata: " + getName() + " " + other.getName());
        if (nbrOfAutomata() != other.nbrOfAutomata())
        {
            return false;
        }
        
        if (!getName().equals(other.getName()))
        {
            return false;
        }
        
        for (Iterator<Automaton> thisAutIt = iterator(), otherAutIt = other.iterator();
        thisAutIt.hasNext() || otherAutIt.hasNext(); )
        {
            //System.err.println("automata i");
            if (!thisAutIt.hasNext())
            {
                //System.err.println("automata i this");
                return false;
            }
            
            if (!otherAutIt.hasNext())
            {
                //System.err.println("automata i other");
                return false;
            }
            
            Automaton thisAutomaton = (Automaton) thisAutIt.next();
            Automaton otherAutomaton = (Automaton) otherAutIt.next();
            
            if (!thisAutomaton.equalAutomaton(otherAutomaton))
            {
                //System.err.println("unequal automaton");
                return false;
            }
        }
        
        //System.err.println("equal automaton");
        return true;
    }
    
    public void setName(String name)
    {
        if (name == null)
        {
            throw new NullPointerException("Null name not supported");
        }
        else
        {
            this.name = name;
        }
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setComment(String comment)
    {
        this.comment = comment;
    }
    
    public String getComment()
    {
        if (comment == null)
        {
            return "";
        }
        
        return comment;
    }
    
    public void clear()
    {
        setName(defaultName);
        setComment(null);
        
        while (size() != 0)
        {
            removeAutomaton(getFirstAutomaton());
        }
        
        theAutomata.clear();
        nameMap.clear();
    }
    
    public String getUniqueAutomatonName()
    {
        return getUniqueAutomatonName("Untitled");
    }
    
    public String getUniqueAutomatonName(String prefix)
    {
        if (prefix == null)
        {
            return getUniqueAutomatonName();
        }
        
        if (!containsAutomaton(prefix))
        {
            return prefix;
        }
        
        int index = 1;
        String newName;
        
        do
        {
            newName = prefix + "(" + index++ + ")";
        }
        while (containsAutomaton(newName));
        
        return newName;
    }
    
    public String getUniqueEventLabel()
    {
        return getUniqueEventLabel("e");
    }
    
    public String getUniqueEventLabel(String prefix)
    {
        if(prefix == null)	// clever recursion here :-)
        {
            return getUniqueEventLabel();
        }
        
        Alphabet alpha = getUnionAlphabet();
        StringBuffer buf = new StringBuffer(prefix);
        int num = 1; // number to append to prefix
        while(alpha.contains(buf.toString()))
        {
            buf.append(num++);
        }
        return buf.toString();
    }
    
    public void stateAdded(Automaton aut, State q)
    {    // Do nothing
    }
    
    public void stateRemoved(Automaton aut, State q)
    {    // Do nothing
    }
    
    public void arcAdded(Automaton aut, Arc a)
    {    // Do nothing
    }
    
    public void arcRemoved(Automaton aut, Arc a)
    {    // Do nothing
    }
    
    public void attributeChanged(Automaton aut)
    {    // Do nothing
    }
    
    public void automatonRenamed(Automaton aut, String oldName)
    {
        nameMap.remove(oldName);
        nameMap.put(aut.getName(), aut);
        notifyListeners(AutomataListeners.MODE_AUTOMATON_RENAMED, aut);
    }
    
    public void updated()
    {
        updated(null);
    }
    
    public void updated(Object obj)
    {
        notifyListeners();
    }
    
    public AutomataListeners getListeners()
    {
        if (listeners == null)
        {
            listeners = new AutomataListeners(this);
        }
        
        return listeners;
    }
    
    public void addListener(AutomataListener listener)
    {
        // Semantic Warning: Local "listeners" shadows a field of the same name in "org.supremica.automata.Automata".
        // AutomataListeners listeners = getListeners();
        // listeners.addListener(listener);
        
        getListeners().addListener(listener);
    }
    
    void notifyListeners()
    {
        if (listeners != null)
        {
            listeners.notifyListeners();
        }
    }
    
    void notifyListeners(int mode, Automaton a)
    {
        // logger.debug("Automata.notifyListeners Start");
        if (listeners != null)
        {
            // logger.debug("Automata.notifyListeners");
            listeners.notifyListeners(mode, a);
        }
    }
    
    public void beginTransaction()
    {
        if (listeners != null)
        {
            listeners.beginTransaction();
        }
    }
    
    public void endTransaction()
    {
        if (listeners != null)
        {
            listeners.endTransaction();
        }
    }
    
    class AutomatonTypeIterator
        implements Iterator<Automaton>
    {
        private Iterator autIt;
        private AutomatonType theType;
        private Automaton theAutomaton = null;
        
        public AutomatonTypeIterator(AutomatonType theType)
        {
            this.autIt = theAutomata.iterator();
            this.theType = theType;
            
            findNext();
        }
        
        public boolean hasNext()
        {
            return theAutomaton != null;
        }
        
        public Automaton next()
        {
            Automaton returnAutomaton = theAutomaton;
            
            findNext();
            
            return returnAutomaton;
        }
        
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
        
        private void findNext()
        {
            while (autIt.hasNext())
            {
                theAutomaton = (Automaton) autIt.next();
                
                if (theAutomaton.getType() == theType)
                {
                    return;
                }
            }
            
            theAutomaton = null;
        }
    }
    
    // Useful for debugging (among other things)
    public String toDebugString()
    {
        StringBuffer sbuf = new StringBuffer();
        
        for (Iterator it = iterator(); it.hasNext(); )
        {
            Automaton automaton = (Automaton) it.next();
            
            sbuf.append(automaton.toString());
            sbuf.append("\n");
        }
        
        return sbuf.toString();
    }
    
    public String toString()
    {
        StringBuffer sbuf = new StringBuffer("{");
        
        if (size() > 0)
        {
            // Examine each automata
            for (Automaton automaton : this)
            {
                sbuf.append(automaton.toString() + ", ");
            }
            
            sbuf.delete(sbuf.length() - 2, sbuf.length());
        }
        
        sbuf.append("}");
        return sbuf.toString();
    }
    
    // Useful for debugging (among other things) - writes Java code
    public String toCode()
    {
        StringBuffer sbuf = new StringBuffer();
        
        // Examine each automata
        for (Automaton automaton : this)
        {
            sbuf.append(automaton.toCode());
            sbuf.append("\n");
        }
        
        sbuf.append("Automata automata = new Automata();\n");
        
        // Examine each automata
        for (Automaton automaton : this)
        {
            sbuf.append("automata.addAutomaton(" + automaton.getName() + ");");
            sbuf.append("\n");
        }
        
        return sbuf.toString();
    }
    
    public String stateToString(int[] arrstate)
    {
        StringBuffer sbuf = new StringBuffer();
        int i = 0;
        
        // Examine each automata
        for (Automaton automaton : this)
        {
            State state = automaton.getStateWithIndex(arrstate[i]);
            
            sbuf.append(state.getName() + ".");
            
            ++i;
        }
        
        return sbuf.toString();
    }
    
    /**
     * Examines automata size.
     *
     * @param gui If gui != null, a JOptionPane shows the results and guides the user.
     * @param minSize Minimum size of the automata.
     */
    public boolean sanityCheck(Component gui, int minSize)
    {
        return sanityCheck(gui, minSize, false, false, false, false);
    }
    
    /**
     * Examines automata size and - optionally - some other stuff.
     *
     * @param gui If gui != null, a JOptionPane shows the results and guides the user.
     * @param minSize Minimum size of the automata.
     * @param mustHaveInitial Test requires automata to have initial states.
     * @param mustHaveValidType Test requires that the automata are not of undefined type.
     * @param mustBeControllabilityConsistent Test requires that an event has the same
     * controllability status in all automata.
     * @param examineStructure Test examines whether there are disjoint parts in the system.
     *
     * This method was originally in gui.ActionMan (to handle the gui-stuff conveniently).
     */
    public boolean sanityCheck(Component gui, int minSize,
        boolean mustHaveInitial, boolean mustHaveValidType,
        boolean mustBeControllabilityConsistent, boolean examineStructure)
    {
        // Is this automata empty? If so, just bail out.
        if (size() <= 0)
        {
            // This is InsaNe
            return false;
        }
        else if (size() > 500)
        {
            logger.warn("Skipping sanity...");
            return true;
        }
        
        // Examine if there are inadequate events...
        // Examine each automata
        for (Automaton automaton : this)
        {
            Alphabet inadequate = automaton.getInadequateEvents();
            if (inadequate.size() > 0)
            {
                if (inadequate.size() == 1)
                    logger.warn("In " + automaton + ", the event " + inadequate +
                        " is selflooped in all states.");
                else
                    logger.warn("In " + automaton + ", the events " + inadequate +
                        " are selflooped in all states.");
            }
        }
        
        // Get the union alphabet (ignoring consistency here)
        Alphabet unionAlphabet = getUnionAlphabet();
        
        // Warns if there are events with equal (lowercase) names.
        // Always do this check (irritating? well yes... but those are really bad names!)
        if (!AlphabetHelpers.isEventNamesSafe(unionAlphabet))
        {
            // Warning has been written in log window by isEventNamesSafe.
        }
        
        // Examines controllability consistency
        if (mustBeControllabilityConsistent)
        {
            if (!isEventControllabilityConsistent(unionAlphabet))
            {
                return false;
            }
            
            if (!isEventEpsilonConsistent(unionAlphabet))
            {
                return false;
            }
        }
        
        // Warns if the system has disjoint modules (the system can be divided into at least two sets
        // of modules whose union alphabets are disjoint)
        if (examineStructure)
        {
            if (isSeveralSystems())
            {
                // Warning has been written in the log window by isSeveralSystems().
            }
        }
        
        // Examines each automaton for an initial state
        if (mustHaveInitial)
        {
            // All automata must have initial states.
            // There is another method for this, Automata.hasInitialState(),
            // but it doesn't tell which automaton breaks the test...
            for (Automaton currAutomaton : this)
            {
                
                // Does this automaton have an initial state?
                if (!currAutomaton.hasInitialState())
                {
                    if (gui != null)
                    {
                        String message = "The automaton " + currAutomaton +
                            " does not have an initial state.\n" + "Please specify an initial state.";
                        Object[] options = { "Cancel" };
                        int cont = JOptionPane.showOptionDialog(gui, message, "Alert",
                            JOptionPane.OK_OPTION,
                            JOptionPane.WARNING_MESSAGE, null,
                            options, options[0]);
                    }
                    else
                    {
                        logger.error("The automaton " + currAutomaton + " has no initial state.");
                    }
                    
                    // This is iNsanE!
                    return false;
                }
            }
        }
        
        // Examines the type of each automaton
        if (mustHaveValidType && (size() > 1))
        {
            // All automata must have a defined type, i.e. must not be of type "UNDEFINED".
            for (Automaton currAutomaton : this)
            {
                // Is this Automaton's type AutomatonType.UNDEFINED?
                if (currAutomaton.getType() == AutomatonType.UNDEFINED)
                {
                    if (gui != null)
                    {
                        String message = "The automaton " + currAutomaton + " is of type 'Undefined'.\n" +
                            "Please specify a type.";
                        Object[] options = { "Cancel" };
                        int cont = JOptionPane.showOptionDialog(gui, message, "Alert",
                            JOptionPane.OK_OPTION,
                            JOptionPane.WARNING_MESSAGE, null,
                            options, options[0]);
                    }
                    else
                    {
                        logger.error("The automaton " + currAutomaton +
                            " is of type 'Undefined'. Please specify a type.");
                    }
                    
                    // This is iNsaNe!
                    return false;
                }
            }
        }
        
        // Make sure the automata has the right size!
        if ((minSize > 0) && (size() < minSize))
        {
            // Generate message
            String size;
            if (minSize == 1)
            {
                size = "one automaton";
            }
            else if (minSize == 2)
            {
                size = "two automata";
            }
            else
            {
                size = minSize + " automata";
            }
            String message = "At least " + size + " must be selected!";
            
            // Present result
            if (gui != null)
            {
                JOptionPane.showMessageDialog(gui, message, "Alert", JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                logger.error(message);
            }
            
            // This is inSaNe!
            return false;
        }
        
        // Perfectly sane!
        return true;
    }
    
    
    public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
    {
        final ProductDESProxyVisitor desvisitor = (ProductDESProxyVisitor) visitor;
        return desvisitor.visitProductDESProxy(this);
    }
    
    public boolean equalsByContents(final Proxy partner)
    {
        final Automata des = (Automata) partner;
        
        return	ProxyTools.isEqualSetByContents(getEvents(), des.getEvents()) &&
            ProxyTools.isEqualSetByContents(getAutomata(), des.getAutomata());
    }
    
    public boolean equalsWithGeometry(final Proxy partner)
    {
        return equalsByContents(partner);
    }
    
    public int hashCodeByContents()
    {
        int result = ProxyTools.getSetHashCodeByContents(getEvents());
        result *= 5;
        result += ProxyTools.getSetHashCodeByContents(getAutomata());
        return result;
    }
    
    public int hashCodeWithGeometry()
    {
        return hashCodeByContents();
    }
    
    public boolean refequals(final NamedProxy partner)
    {
        return getName().equals(partner.getName());
    }
    
    public int refHashCode()
    {
        return getName().hashCode();
    }
}
