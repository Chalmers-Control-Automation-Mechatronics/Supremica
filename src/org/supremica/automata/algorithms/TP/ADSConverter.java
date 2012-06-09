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
import net.sourceforge.waters.model.compiler.instance.EventKindException;
import net.sourceforge.waters.plain.module.IdentifierElement;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.xsd.base.EventKind;
import org.supremica.automata.*;

/**
 *
 * @author shoaei
 */
public class ADSConverter {
    private final URL mURL;
    private final ExtendedAutomata exAutomata;
    
    public ADSConverter(final URI uri, ExtendedAutomata exAutomata) throws MalformedURLException{
        mURL = uri.toURL();
        this.exAutomata = exAutomata;
    }
    
    public ExtendedAutomaton conver() throws IOException{
        ExtendedAutomaton ex = null;
        final InputStream stream = mURL.openStream();
        final Reader raw = new InputStreamReader(stream);
        final BufferedReader reader = new BufferedReader(raw);
        String name;
        int stateSize = 0;
        HashSet<String> markedStates = new HashSet<String>();
        HashSet<String[]> transitions = new HashSet<String[]>();
        String s;
        s=reader.readLine();
        System.err.println(s.contains("CTCT ADS"));
        if(!s.contains("CTCT ADS")){
            return ex;
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
                    try{
                        stateSize = Integer.parseInt(s);
                        break;
                    } catch(NumberFormatException e){
                        System.err.println("Step 1 error");
                        return ex;
                    }
                    
                case 2:
                    try{
                        markedStates.add(s);
                        break;
                    } catch(NumberFormatException e){
                        System.err.println("Step 2 error");
                        return ex;
                    }
                case 3:
                    
                case 4:
                    String[] split = s.split("\\s+");
                    transitions.add(split);
                    break;
            }
        }
        

        ex = new ExtendedAutomaton(name, exAutomata, false);
        boolean isInitial, isMark;
        HashSet<String> alphabet = new HashSet<String>();
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
            
            if(alphabet.add(event))
                exAutomata.addEvent(event, kind);
            
            ex.addTransition(source, target, event + ";", "", "");
        }

//        if(ex.getNodes().size() != stateSize){
//            System.err.println("State size is not matched.");
//            return null;
//        }
        
        return ex;
    }
}
