
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.automata.IO;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.AbstractMap.SimpleEntry;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automaton;
import org.supremica.automata.DefaultProjectFactory;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.Project;
import org.supremica.automata.ProjectFactory;
import org.supremica.automata.State;

/**
 * Import UMDES files, http://www.eecs.umich.edu/umdes/
 * From the UMDES documentation:
 *
 * Individual FSM
 * The default for each event (transition) is controllable and
 * observable (c and o). If an event is uncontrollable and unobservable,
 * you may specify so after the new state with `uc' and `uo'.
 * If the event is either uncontrollable but observable or unobservable
 * but controllable, you may simply state the variable (`uc' or `uo')
 * that describe the negative characteristic. Please note that you may
 * ignore `uc' and `uo' completely if you choose to create the unobservable
 * events file manually by writing this text file. If you choose to use the
 * routine write_uo, you have to state the event properties in the machine.fsm
 * files. Also, the program called "add_prop" adds the uc and uo
 * properties to all events in the FSM file based on the events.uo and
 * event.uc inputs.
 *
 * 4
 * {# States}
 *
 *
 * VC   1/0     4
 * {State}  {Marked/Unmarked} {# Transitions}
 * SC1  VSC
 * {Event} {New State}
 * CV VC
 * OV VO
 * SO1 VSO
 *
 *
 * VO 0 4
 * ... ...
 * ... ...
 * Optionally, additional events not appearing in transitions can
 * be added to a machine. To do this, after the last state and transition,
 * add a new line begining with the key work EVENTS After this line
 * additional events can be listed in the format for an event list.
 * i.e. To add uncontrollable and unobservable event 'a' to an FSM,
 * add the following at the end of the file
 *
 * EVENTS
 * a uc uo
 *
 *//*
 * The extended fsm-file format looks like the above, only that the
 * file can have multiple such fsm definitions (including optional EVENTS)
 * one after the other. A positive integer while reading additional events
 * signifes the start of a new automaton (MF Feb 2022)
 ***/
public class ProjectBuildFromFSM
{
    private final ProjectFactory theProjectFactory;
    private final Project currProject;
    protected String automatonName = "Imported from UMDES";
//	private InputProtocol inputProtocol = InputProtocol.UnknownProtocol;

    public ProjectBuildFromFSM()
    {
        this.theProjectFactory = new DefaultProjectFactory();
        this.currProject = theProjectFactory.getProject();
    }

    public ProjectBuildFromFSM(final ProjectFactory theProjectFactory)
    {
        this.theProjectFactory = theProjectFactory;
        this.currProject = theProjectFactory.getProject();        
    }

    public Project build(final URL url)
    throws Exception
    {
        final String protocol = url.getProtocol();

        if (protocol.equals("file"))
        {
            //inputProtocol = InputProtocol.FileProtocol;

            final String fileName = url.getFile();
            final File thisFile = new File(fileName);
            automatonName = thisFile.getName();

            final int lastdot = automatonName.lastIndexOf(".");
            if (lastdot > 0)
            {
                automatonName = automatonName.substring(0, lastdot);
            }
        }
        else if (protocol.equals("jar"))
        {
            //inputProtocol = InputProtocol.JarProtocol;
        }
        else
        {
            //inputProtocol = InputProtocol.UnknownProtocol;

            System.err.println("Unknown protocol: " + protocol);
            return null;
        }

        final InputStream stream = url.openStream();
        build(stream);
        stream.close(); // Was the stream never closed?
        return this.currProject;
    }

    private Project build(final InputStream is)
    throws Exception
    {
      final FSMbuildHelper builder = new FSMbuildHelper(is);
      int index = 1; 
      do
      {
        final Automaton automaton = builder.build();
        automaton.setName(automatonName + "_" + index);
        currProject.addAutomaton(automaton);
        index += 1;
      } while(builder.hasMore());
      
      return currProject;
    }
}

/*
 * Two reasons for this class:
 * 1. I need to push back a line to manage the extended fsm file format
 * 2. StringTokenizer is depreceated, and when it eventualky disappears, you
 *    dear future programmer, have an easier time fixing this using String.split
 *    with all of its quirks.
**/
class Tokenizer
{
  private final BufferedReader reader;
  private String currLine;
  private String currToken;
  private String pushedLine;
  private String pushedToken;
  private StringTokenizer tokenizer;
  private int currLineNumber = 0;
  
  public Tokenizer(final InputStream is)
  {
    this.reader = new BufferedReader(new InputStreamReader(is));
  }
  
  public String readLine()
  throws java.io.IOException
  {
    if (this.pushedLine != null)
    {
      this.currLine = this.pushedLine;
      this.pushedLine = null;
    }
    else
    {
      this.currLine = this.reader.readLine();
      this.currLineNumber += 1;
    }
    
    if (this.currLine != null)
      this.tokenizer = new StringTokenizer(this.currLine);
      
    return this.currLine;
  }
  
  public void pushLine()
  {
    this.pushedLine = this.currLine;
  }
  
  public void pushToken()
  {
    this.pushedToken = this.currToken;
  }
  
  public boolean hasMoreTokens()
  {
    return this.tokenizer.hasMoreTokens() || this.pushedToken != null;
  }
  
  public String nextToken()
  {
    if (this.pushedToken != null)
    {
      this.currToken = this.pushedToken;
      this.pushedToken = null;
    }
    else
      this.currToken = tokenizer.nextToken();
  
    return this.currToken;
  }
  // Debug only!
  public String getCurrLine()
  {
    return this.currLine;
  }
  // For error reporting
  public int getCurrLineNumber()
  {
    return this.currLineNumber;
  }

}

class FSMbuildHelper
{
    private final Tokenizer tokenizer;
		private Automaton currAutomaton;
		private Alphabet currAlphabet;
    private TransitionMap transitionMap;
		private enum ParserState
		{
			READ_NUMBER_OF_STATES,
			READ_STATES,
			READ_TRANSITIONS,
			READ_ADDITIONAL_EVENTS,
      READ_THIS_AUTOMATON, // MF added to allow multiple FSMs in a *.fsm file
		}
		private ParserState currParserState = ParserState.READ_NUMBER_OF_STATES;
		private int numberOfRemainingStates = 0;
		private int numberOfRemainingTransitions = 0;
    private boolean weHaveMore = false; // Records whether there is another fsm to read
		private State currState = null;
		private boolean initialState;

    public FSMbuildHelper(InputStream is)
    throws java.io.IOException
    {
        this.tokenizer = new Tokenizer(is); // new FSMReader(is);
        this.init();
		}

    private void init()
    {
      this.currAutomaton = new Automaton(""); // Cannt have empty name?
      this.currAlphabet = this.currAutomaton.getAlphabet();
      this.initialState = true; // The first one we read is the initial state
      this.transitionMap = new TransitionMap();
    }
    
		public Automaton build()
    throws Exception
		{
			String currLine = tokenizer.readLine();
			while (currLine != null)
			{ // System.err.println(tokenizer.getCurrLineNumber());

				while (tokenizer.hasMoreTokens())
				{
          // This is a bit of legacy, could be removed, but it is keept as Iäm lazy
					final String currToken = tokenizer.nextToken();

          // Could replace all of this by currParserState.method(tokenizer, currToken);
					if (currParserState == ParserState.READ_NUMBER_OF_STATES)
					{
            this.weHaveMore = false;
            readNumberStates(tokenizer, currToken);
					}
					else if (currParserState == ParserState.READ_STATES)
					{
            readStates(tokenizer, currToken);
					}
					else if (currParserState == ParserState.READ_TRANSITIONS)
					{
            readTransitions(tokenizer, currToken);
					}
					else if (currParserState == ParserState.READ_ADDITIONAL_EVENTS)
					{
              readAdditionalEvents(tokenizer, currToken);
					}
          else if (currParserState == ParserState.READ_THIS_AUTOMATON)
          { // In a multi-fsm file, we just finished reading this automaton
            // We return the just read automaton here, and set up for reading 
            // the next one. After this call, currParserState == READ_NUMBER_OF_STATES
              return prepareToReadNext(tokenizer);
          }
				}

				currLine = tokenizer.readLine();
			}
      
			// Add all transitions and events
      handleTransitions();

			return currAutomaton;
		}
    
    //------------------------------------------------------------------------------
    // Return java.util.AbstractMap.SimpleEntry as pair that holds the c/uc and o/uo attributes
    // There is no check that the same event is defined both as c and uc, or o and uo
    private SimpleEntry<Boolean, Boolean> handleEventAttributes(final Tokenizer tokenizer)
    throws java.io.IOException, Exception
    {
      boolean eventControllable = true; // default controllable
      boolean eventObservable = true;   // default observable
      
      while(tokenizer.hasMoreTokens())
      {
        final String optionalParameter = tokenizer.nextToken();
        
        if (optionalParameter.equalsIgnoreCase("uc"))
        {
          eventControllable = false;
        }
        else if (optionalParameter.equalsIgnoreCase("uo"))
        {
          eventObservable = false;
        }
        else if (optionalParameter.equalsIgnoreCase("c") || optionalParameter.equalsIgnoreCase("o"))
        {
          // All is fine, already set up, do nothing
        }
        else // Something's not right
        {
          throw new Exception("Unknown event attribute: " + optionalParameter);
        }
      }
      
      return new SimpleEntry<Boolean, Boolean>(eventControllable, eventObservable);
    }
    //------------------------------------------------------------------------------
    private void readNumberStates(final Tokenizer tokenizer, final String currToken)
    {
      try
      {
        numberOfRemainingStates = Integer.parseInt(currToken);
      }
      catch (NumberFormatException ex)
      {
        System.err.println("Expected the number of states. Read: " + currToken);
        throw ex;
      }

      if (numberOfRemainingStates < 1)
        System.err.println("The automaton must have at least one state (the initial state)");

      currParserState = ParserState.READ_STATES;
    }
    //------------------------------------------------------------------------
    private void readStates(final Tokenizer tokenizer, final String currToken)
    throws Exception
    {
      final String stateName = currToken;
      final String markedString = tokenizer.nextToken();
      final String nbrOfTransitionsString = tokenizer.nextToken();
      
      if (stateName == null)
      {
        System.err.println("Expected a state name");
      }

      if (markedString == null)
      {
        System.err.println("Expected the marking of the state, 0 (unmarked) or 1 (marked)");
      }

      if (nbrOfTransitionsString == null)
      {
        System.err.println("Expected the number of transitions");
      }

      int marked = -1;
      String errstring = "Expected the marking of the state, 0 (unmarked) or 1 (marked)";
      
      try
      {
        errstring = "Expected the marking of the state, 0 (unmarked) or 1 (marked)";
        marked = Integer.parseInt(markedString);

        if ((marked < 0) || (marked > 1))
        {
          errstring = "Expected the marking of the state, 0 (unmarked) or 1 (marked)";
          throw new NumberFormatException(errstring);
        }
        
        errstring = "Expected the number of transitions";
        numberOfRemainingTransitions = Integer.parseInt(nbrOfTransitionsString);

        if (numberOfRemainingTransitions < 0)
        {
          errstring = "The automaton must have a non negative number of transitions";
          throw new NumberFormatException(errstring);
        }
      }
      catch(final NumberFormatException excp)
      {
        System.err.println("line "+ tokenizer.getCurrLineNumber() + ": " + errstring);
        throw new Exception("line "+ tokenizer.getCurrLineNumber() + ": " + errstring);
      }
      
      // Create and add the state
      currState = currAutomaton.createUniqueState(stateName);

      if (initialState)
      {
        currState.setInitial(true);
        initialState = false;
      }
      
      final boolean ismarked = (marked == 1) ? true : false;
      
      currState.setAccepting(ismarked);
      currState.setForbidden(false);

      currAutomaton.addState(currState);

      numberOfRemainingStates--;

      if (numberOfRemainingTransitions > 0)
        currParserState = ParserState.READ_TRANSITIONS;
      else
      {
        if (numberOfRemainingStates > 0)
          currParserState = ParserState.READ_STATES;
        else
          currParserState = ParserState.READ_ADDITIONAL_EVENTS;
      }
    }
    //-----------------------------------------------------------------------------
    private void readTransitions(final Tokenizer tokenizer, final String currToken)
    throws java.io.IOException, Exception
    {
      final String currEvent = currToken;
      final String destStateName = tokenizer.nextToken();
      
      if (currEvent == null)
      {
        System.err.println("Expected an event");
      }

      if (destStateName == null)
      {
        System.err.println("Expected a destination state");
      }

      final SimpleEntry<Boolean, Boolean> attr = handleEventAttributes(tokenizer);
      final boolean currEventControllable = attr.getKey();
      final boolean currEventObservable = attr.getValue();
      
      final LabeledEvent currLabeledEvent = new LabeledEvent(currEvent);

      currLabeledEvent.setControllable(currEventControllable);
      currLabeledEvent.setObservable(currEventObservable);
      currLabeledEvent.setPrioritized(true);
      transitionMap.addArc(currState.getName(), destStateName, currLabeledEvent);

      numberOfRemainingTransitions--;

      if (numberOfRemainingTransitions > 0)
      {
        currParserState = ParserState.READ_TRANSITIONS;
      }
      else
      {
        if (numberOfRemainingStates > 0)
        {
          currParserState = ParserState.READ_STATES;
        }
        else
        {
          currParserState = ParserState.READ_ADDITIONAL_EVENTS;
        }
      }
    }
    //-------------------------------------------------------------------------
    /* Here, all the states and transitions have been read, and what follows is
     * either a set of extra events, preceeded by the token "EVENTS", or a positive
     * integer that signals the start of a new automaton. Note that if there is an 
     * event named "events" (highly unlikely, but still) this event will be confused
     * with the token "EVENTS" and discarded.
    **/
    private void readAdditionalEvents(final Tokenizer tokenizer, final String currToken)
    throws java.io.IOException, Exception
    {
      if (currToken.equalsIgnoreCase("EVENTS"))
      {    
        // Next follows one or more events, remain in the same parser state and keep reading
        currParserState = ParserState.READ_ADDITIONAL_EVENTS;
        return; // If there is an additional event named "events", we will miss it!
      }
      else if (0 <= isPosInteger(currToken)) // if a number then it is start of next automaton
      {
        // Need to push back the current line for next call of build
        tokenizer.pushLine();
        currParserState = ParserState.READ_THIS_AUTOMATON; // Just finished reading this one
      }
      else // this is an event supposedly not already defined on a transition
      {
        final String currEvent = currToken;

        if (currAlphabet.contains(currEvent)) // If already defined, throw exception
        {
          throw new Exception("Alphabet alredy contains: " + currEvent);
        }

        final SimpleEntry<Boolean, Boolean> attr = handleEventAttributes(tokenizer);
        final boolean currEventControllable = attr.getKey();
        final boolean currEventObservable = attr.getValue();

        final LabeledEvent currLabeledEvent = new LabeledEvent(currEvent);
        currLabeledEvent.setControllable(currEventControllable);
        currLabeledEvent.setObservable(currEventObservable);
        currLabeledEvent.setPrioritized(true);
        currAlphabet.addEvent(currLabeledEvent);
      }
    }
    //-------------------------------------------------------------
    /* Finalize reading the current automaton and set up to read the 
     * next, this is for extended fsm-file format. Returns teh just 
     * now finished automaton.
    **/
    private Automaton prepareToReadNext(final Tokenizer tokenizer)
    throws java.io.IOException, Exception
    {
        // Finalize the current automaton, and cache it
        handleTransitions();
        Automaton cache = this.currAutomaton;  
        
        this.tokenizer.pushLine(); // Push back the current line to be read next time
        this.weHaveMore = true;    // Signal that there's more to read

        // Re-nitialize
        this.init();
        this.currParserState = ParserState.READ_NUMBER_OF_STATES;
        
        return cache; 
    }
    //-----------------------------------------------
    /* Returns a positive integer if this string
     * represents a positive integer, else a negative
     * integer is returned. Only straight sequence of 
     * digits is handled, nothing else.     
    **/
    private int isPosInteger(final String str)
    {
      int val = 0;
      for (int i = 0; i < str.length(); i++)
      {
        final char ch = str.charAt(i);
        if (ch < '0' || '9' < ch) return -1;
          
        val = 10 * val + (ch - '0');
      }
      return val;
    }
    
    public boolean hasMore()
    {
      return this.weHaveMore;
    }
    
    private void handleTransitions() throws Exception
    {
      for (Iterator<LabeledEvent> labelIt = this.transitionMap.labelIterator(); labelIt.hasNext(); )
			{
				final LabeledEvent currEvent = labelIt.next();
				final List<?> currList = this.transitionMap.getTransitions(currEvent);

				// This triggers if the same event is both on a transition and under the "EVENTS" tag
				if (this.currAlphabet.contains(currEvent))
				{
					throw new Exception(currEvent.getLabel() + " is already defined!");
				}

				this.currAlphabet.addEvent(currEvent);

				// Add the transition
				for (Iterator<?> transIt = currList.iterator(); transIt.hasNext(); )
				{
					final TransitionMap.Transition currTransition = (TransitionMap.Transition) transIt.next();
					final String sourceStateName = currTransition.getSourceStateName();
					final String destStateName = currTransition.getDestStateName();
					final State currSourceState = currAutomaton.getStateWithName(sourceStateName);
					final State currDestState = currAutomaton.getStateWithName(destStateName);
          if (currSourceState == null || currDestState == null)
            throw new Exception("Error with state name: " + destStateName);
            
					// Create and add the arc
					final Arc currArc = new Arc(currSourceState, currDestState, currEvent);
					currAutomaton.addArc(currArc);
				}
			}
    }
  }
  
class TransitionMap
{
    private final HashMap<LabeledEvent, List<Transition>> theMap =
        new HashMap<LabeledEvent, List<Transition>>();

    public TransitionMap()
    {}

    public void addArc(final String sourceState, final String destState, final LabeledEvent event)
    {
        final Transition newTransition = new Transition(sourceState, destState);
        final List<Transition> transitions;

        if (theMap.containsKey(event))
        {
            transitions = theMap.get(event);
        }
        else
        {
            transitions = new LinkedList<Transition>();

            theMap.put(event, transitions);
        }

        transitions.add(newTransition);
    }

    public Iterator<LabeledEvent> labelIterator()
    {
        final Set<LabeledEvent> currSet = theMap.keySet();
        return currSet.iterator();
    }

    public List<?> getTransitions(final LabeledEvent event)
    {
        if (theMap.containsKey(event))
        {
            return theMap.get(event);
        }
        else
        {
            return null;
        }
    }


/*
                public boolean containsEvent(String label)
                {
                                LabeledEvent tmpEvent = new LabeledEvent(label);
                                return theMap.containsKey(tmpEvent);
                }
 */
    class Transition
    {
        private final String sourceState;
        private final String destState;

        public Transition(final String sourceState, final String destState)
        {
            this.sourceState = sourceState;
            this.destState = destState;
        }

        public String getSourceStateName()
        {
            return sourceState;
        }

        public String getDestStateName()
        {
            return destState;
        }
    }
}
