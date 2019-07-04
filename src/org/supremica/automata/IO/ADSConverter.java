/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.supremica.automata.IO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;

import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.ExtendedAutomaton;


/**
 * ADSConverter class to convert ADS files to ExtendedAutomaton.
 *
 * @author Mohammad Reza Shoaei (shoaei@chalmers.se)
 * @version %I%, %G%
 * @since 1.0
 */

public class ADSConverter {
    private final Logger logger = LogManager.getLogger(ADSConverter.class);
    private final URI uri;

    public ADSConverter(final URI uri) throws MalformedURLException{
        this.uri = uri;
    }

    public ExtendedAutomaton getExtendedAutomaton() throws IOException{
        final URL mURL = uri.toURL();
        final InputStream stream = mURL.openStream();
        final Reader raw = new InputStreamReader(stream);
        final BufferedReader reader = new BufferedReader(raw);
        String name;
        final HashSet<String> markedStates = new HashSet<String>();
        final HashSet<String[]> transitions = new HashSet<String[]>();
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
                    } catch(final NumberFormatException e){
                        logger.error("There is problem in marker states part");
                        return null;
                    }
                case 3:
                    break;
                case 4:
                    final String[] split = s.split("\\s+");
                    transitions.add(split);
                    break;
            }
        }

        final ExtendedAutomaton ex = new ExtendedAutomaton(name, ComponentKind.PLANT);
        boolean isInitial, isMark;

        for(final String[] t:transitions){
            final String source = t[0];
            final String event = t[1];
            final String target = t[2];

            isInitial = (Integer.parseInt(source) == 0)?true:false;
            isMark = (markedStates.contains(source))?true:false;
            ex.addState(source, isMark, isInitial, false);

            isInitial = (Integer.parseInt(target) == 0)?true:false;
            isMark = (markedStates.contains(target))?true:false;

            ex.addState(target, isMark, isInitial, false);

            final boolean isObservable = (Integer.parseInt(event) == 1000)?false:true;
            String kind;
            if(isObservable)
                kind = (Integer.parseInt(event)%2 != 0)?EventKind.CONTROLLABLE.toString():EventKind.UNCONTROLLABLE.toString();
            else
                kind = EventKind.CONTROLLABLE.toString();

            ex.addEvent(event, kind, isObservable);
            ex.addTransition(source, target, event, null, null);
        }
        return ex;
    }
}
