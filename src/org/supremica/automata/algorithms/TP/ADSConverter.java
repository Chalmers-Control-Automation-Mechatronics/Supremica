/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.supremica.automata.algorithms.TP;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.xsd.base.EventKind;
import org.supremica.automata.*;
import org.supremica.gui.ide.IDE;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

/**
 *
 * @author shoaei
 */
public class ADSConverter {
    private final Logger logger = LoggerFactory.createLogger(IDE.class); 
    private final ExtendedAutomata exAutomata;
    private final ModuleSubjectFactory factory;
    
    public ADSConverter(ExtendedAutomata exAutomata) throws MalformedURLException{
        this.exAutomata = exAutomata;
        this.factory = ModuleSubjectFactory.getInstance();
    }
    
    public ExtendedAutomaton convert(final URI uri) throws IOException{
        URL mURL = uri.toURL();
        final InputStream stream = mURL.openStream();
        final Reader raw = new InputStreamReader(stream);
        final BufferedReader reader = new BufferedReader(raw);
        String name;
        HashSet<String> markedStates = new HashSet<String>();
        HashSet<String[]> transitions = new HashSet<String[]>();
        String s;
        s=reader.readLine();
        if(!s.contains("CTCT ADS")){
            logger.error("The file format is not CTCT ADS");
            return null;
        }
        reader.readLine();
        name = reader.readLine();
        
        int step = 0;
        while((s=reader.readLine())!=null){
            if(s.contains("#") || s.contains(":"))
                continue;
            
            if(s.isEmpty()){
                step++;
                continue;
            }
            
            switch(step){
                case 1:
                    break;
                case 2:
                    try{
                        markedStates.add(s);
                        break;
                    } catch(NumberFormatException e){
                        logger.error("There is problem in marker states part");
                        return null;
                    }
                case 3:
                    break;
                case 4:
                    String[] split = s.split("\\s+");
                    transitions.add(split);
                    break;
            }
        }
        
        ExtendedAutomaton ex = new ExtendedAutomaton(name, exAutomata, false);
        boolean isInitial, isMark;
        HashSet<String> alphabet = new HashSet<String>();
        
        boolean hasMarked = false;
        for(EventDeclProxy e:exAutomata.getModule().getEventDeclList())
            if(e.getKind() == EventKind.PROPOSITION && e.getName().equals(EventDeclProxy.DEFAULT_MARKING_NAME))
                hasMarked = true;
        if(!markedStates.isEmpty() && !hasMarked){
            exAutomata.getModule().getEventDeclListModifiable().add
                        (factory.createEventDeclProxy(factory.createSimpleIdentifierProxy
                        (EventDeclProxy.DEFAULT_MARKING_NAME), EventKind.PROPOSITION));            
        }
            
        for(EventDeclProxy e:exAutomata.getUnionAlphabet())
            alphabet.add(e.getName());
        
        for(String[] t:transitions){
            String source = t[0];
            String event = t[1];
            String target = t[2];
            
            if(Integer.parseInt(source) == 0)
                isInitial = true;
            else 
                isInitial = false;
            
            if(markedStates.contains(source))
                isMark = true;
            else
                isMark = false;
                        
            ex.addState(source, isMark, isInitial, false);
            
            if(Integer.parseInt(target) == 0)
                isInitial = true;
            else 
                isInitial = false;
            
            if(markedStates.contains(target))
                isMark = true;
            else
                isMark = false;
                        
            ex.addState(target, isMark, isInitial, false);

            String kind;
            if(Integer.parseInt(event)%2 != 0)
                kind = "controllable";
            else
                kind = "uncontrollable";
            
            if(!alphabet.contains(event)){
                alphabet.add(event);
                exAutomata.addEvent(event, kind);
            }
            
            ex.addTransition(source, target, event + ";", "", "");
        }
        return ex;
    }
}
