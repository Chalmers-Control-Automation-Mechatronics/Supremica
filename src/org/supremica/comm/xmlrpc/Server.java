
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
package org.supremica.comm.xmlrpc;

import java.util.*;
import java.io.*;
import org.supremica.gui.*;
import org.supremica.automata.*;
import org.supremica.automata.IO.*;
import org.supremica.properties.*;

import org.apache.xmlrpc.*;
import org.supremica.gui.ide.IDE;

public class Server
{    
    // singleton stuff:
    private static Server instance_ = null;
    
    public static void shutdown()
    {
        if (instance_ != null)
        {
            instance_.theServer.shutdown();
            
            instance_ = null;
        }
    }
    
    public static String escape(String was)
    {
        int len = was.length();
        StringBuffer sb = new StringBuffer(len);
        
        for (int i = 0; i < len; i++)
        {
            char c = was.charAt(i);
            
            switch (c)
            {
                
                case '<' :
                    sb.append("&lt;");
                    break;
                    
                case '>' :
                    sb.append("&gt;");
                    break;
                    
                case '&' :
                    sb.append("&amp;");
                    break;
                    
                case '\"' :
                    sb.append("&quot;");
                    break;
                    
                case '/' :
                case '\n' :
                case '\r' :
                    sb.append("&#");
                    sb.append((int) (c));
                    sb.append(';');
                    break;
                    
                default :
                    sb.append(c);;
            }
        }
        
        return sb.toString();
    }
    
    // -----------------------------------------------------
    private VisualProjectContainer container = null;
    private IDE ide = null;
    private WebServer theServer;
    
    // -----------------------------------------------------
    public Server(Object projectContainer, int port)
    throws Exception
    {
        instance_ = this;    // this statement would be illegal in C++  :)

        if (projectContainer instanceof VisualProjectContainer)
            container = (VisualProjectContainer) projectContainer;
        else if (projectContainer instanceof IDE)
            ide = (IDE) projectContainer;
        else
            throw new Exception("Bad input to constructor.");

        String filter = Config.XML_RPC_FILTER.getAsString();
        
        theServer = new WebServer(port);
        
        if (filter.length() > 0)
        {
            theServer.setParanoid(true);
            theServer.acceptClient(filter);
        }
        
        XmlRpc.setDebug(Config.XML_RPC_DEBUG.isTrue());
        theServer.addHandler("$default", this);
        theServer.start();
    }
    
    /**
     * Gets the active project from the GUI.
     */
    private Project getActiveProject()
    {
        if (container != null)
            return container.getActiveProject();
        else
            return ide.getActiveProject();
    }
    
    // ---------------------------------------------------
    public Vector<String> getAutomataIdentities()
    {
        Vector<String> theIdentities = new Vector<String>();
        Iterator<?> autIt = getActiveProject().iterator();
        
        while (autIt.hasNext())
        {
            Object o = autIt.next();
            String currName = o.toString();
            
            theIdentities.add(currName);
        }
        
        return theIdentities;
    }
    
    public int deleteAutomaton(String name)
    throws XmlRpcException
    {
        Automata as = getActiveProject();
        int oldsize = as.size();
        
        as.removeAutomaton(name);
        
        if (oldsize == as.size())
        {
            throw new XmlRpcException(0, name + " does not exist.");
        }
        
        return 0;    // ignored
    }
    
    public int renameAutomaton(String name, String newname)
    throws XmlRpcException
    {
        Automata as = getActiveProject();
        
        try
        {
            Automaton currAutomaton = as.getAutomaton(name);
            
            as.renameAutomaton(currAutomaton, newname);
        }
        catch (Exception e)
        {
            throw new XmlRpcException(0, name + " does not exist.");
        }
        
        return 0;
    }
    
    // ----------------------------------------------
    public String getAutomaton(String name)
    throws XmlRpcException
    {
        Automaton currAutomaton = null;
        
        try
        {
            currAutomaton = getActiveProject().getAutomaton(name);
        }
        catch (Exception e)
        {
            throw new XmlRpcException(0, name + " does not exist.");
        }
        
        Automata theAutomata = new Automata();
        
        theAutomata.addAutomaton(currAutomaton);
        
        AutomataToXML exporter = new AutomataToXML(theAutomata);
        StringWriter response = new StringWriter();
        PrintWriter pw = new PrintWriter(response);
        
        exporter.serialize(pw);
        
        String escaped_xml = escape(response.toString());
        
        // System.out.println("Escaped document: " + escaped_xml);
        return escaped_xml;
    }
    
    public String getAutomata(Vector<?> automataIdentities)
    throws XmlRpcException
    {
        
        // Construct an automata object
        Automata theAutomata = new Automata();
        
        for (int i = 0; i < automataIdentities.size(); i++)
        {
            String currName = (String) automataIdentities.get(i);
            Automaton currAutomaton;
            
            try
            {
                currAutomaton = getActiveProject().getAutomaton(currName);
            }
            catch (Exception e)
            {
                throw new XmlRpcException(0, currName + " does not exist.");
            }
            
            theAutomata.addAutomaton(currAutomaton);
        }
        
        AutomataToXML exporter = new AutomataToXML(theAutomata);
        StringWriter response = new StringWriter();
        PrintWriter pw = new PrintWriter(response);
        
        exporter.serialize(pw);
        
        return escape(response.toString());
    }
    
    public int addAutomaton(String name, String automatonXmlEncoding)
    throws XmlRpcException
    {
        
        // System.out.println("name = " + name);
        // System.out.println("automatonXmlEncoding = " + automatonXmlEncoding);
        StringReader reader = new StringReader(automatonXmlEncoding);
        Project project = null;
        
        try
        {
            ProjectBuildFromXML builder = new ProjectBuildFromXML();
            
            project = builder.build(reader);
        }
        catch (Exception e)
        {
            throw new XmlRpcException(0, "Error while parsing automatonXmlEncoding: " + e.getMessage());
        }
        
        Iterator<?> autIt = project.iterator();
        
        if (!autIt.hasNext())
        {
            throw new XmlRpcException(0, "no documents found in RPC message");
        }
        
        Automaton currAutomaton = (Automaton) autIt.next();
        
        currAutomaton.setName(name);
        getActiveProject().addAutomaton(currAutomaton);
        
        return 0;    // ignore this
    }
    
    public int addAutomata(String automataXmlEncoding)
    throws XmlRpcException
    {
        StringReader reader = new StringReader(automataXmlEncoding);
        Project project;
        
        try
        {
            ProjectBuildFromXML builder = new ProjectBuildFromXML();
            
            project = builder.build(reader);
        }
        catch (Exception e)
        {
            throw new XmlRpcException(0, "Error while parsing automataXmlEncoding.");
        }
        
        Iterator<?> autIt = project.iterator();
        
        while (autIt.hasNext())
        {
            Automaton currAutomaton = (Automaton) autIt.next();
            
            try
            {
                getActiveProject().addAutomaton(currAutomaton);
            }
            catch (Exception e)
            {
                throw new XmlRpcException(0, currAutomaton.getName() + " does already exist.");
            }
        }
        
        return 0;    // ignore this
    }
    
    public int removeAutomata(Vector<?> automataIdentities)
    throws XmlRpcException
    {
        for (int i = 0; i < automataIdentities.size(); i++)
        {
            String currName = (String) automataIdentities.get(i);
            
            try
            {                
                // container.remove(currName);
                getActiveProject().removeAutomaton(currName);
            }
            catch (Exception e)
            {
                throw new XmlRpcException(0, currName + " does not exist.");
            }
        }
        
        return 0;    // ignore this
    }
    
        /*
        public void synchronizeAutomata(Vector automataIdentitites, String newautomatonIdentitity)
                                        throws XmlRpcException {}
         
        public void minimizeAutomaton(String automatonIdentity, String newIdentity)
                                        throws XmlRpcException {}
         */
}
