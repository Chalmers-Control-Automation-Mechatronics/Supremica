//# -*- indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.valid
//# CLASS:   SupremicaUnmarshaller
//###########################################################################
//# $Id: SupremicaUnmarshaller.java,v 1.17 2007-06-23 10:16:00 robi Exp $
//###########################################################################

package org.supremica.automata.IO;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.StandardExtensionFileFilter;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.Project;
import org.supremica.automata.State;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

import org.xml.sax.SAXException;


public class SupremicaUnmarshaller
    implements ProxyUnmarshaller<Project>
{

    //#########################################################################
    //# Constructor
    public SupremicaUnmarshaller(final ModuleProxyFactory modfactory)
        throws JAXBException, SAXException
    {
        builder = new ProjectBuildFromXML();
        //mImporter = new ProductDESImporter(modfactory);
    }    
    
    //#########################################################################
    //# Interface net.sourceforge.waters.model.marshaller.ProxyUnmarshaller
    public Project unmarshal(final URI uri)
        throws WatersUnmarshalException, IOException
    {
        URL url = uri.toURL();
        final Project project;
        try
        {
            project = builder.build(url);
            project.setLocation(uri);
        }
        catch (Exception ex)
        {
            throw new WatersUnmarshalException(ex);
        }
            
        /*
        // Examine the result
        if (validate(project))                
            return mImporter.importModule(project);
        else
            // Would like to import it directly into the analyzer not to miss out
            // on the Supremica-specific parts...
         */
        return project;
    }       
    
    public Class<Project> getDocumentClass()
    {
        return Project.class;
    }
    
    public String getDefaultExtension()
    {
        return ".xml";
    }

    public String getDescription()
    {
        return "Supremica Project files [*.xml]";
    }
    
    public Collection<String> getSupportedExtensions()
    {
        final String ext = getDefaultExtension();
        return Collections.singletonList(ext);
    }

    public FileFilter getDefaultFileFilter()
    {
        final String ext = getDefaultExtension();
        final String description = getDescription();
        return StandardExtensionFileFilter.getFilter(ext, description);
    }

    public Collection<FileFilter> getSupportedFileFilters()
    {
        final FileFilter filter = getDefaultFileFilter();
        return Collections.singletonList(filter);
    }

    public DocumentManager getDocumentManager()
    {
        //return mImporter.getDocumentManager();
        return mDocumentManager;
    }
    
    public void setDocumentManager(DocumentManager manager)
    {
        //mImporter.setDocumentManager(manager);
        mDocumentManager = manager;
    }    


    //#########################################################################
    //# Static Class Methods
    /**
     * Examines if there are conversion problems in a Supremica project.
     */
    public static boolean validate(Project project)
    {
        if (project.hasAnimation())
        {
            logger.warn("Project contains an animation, this is not supported by the editor.");
            return false;
        }
        
        for (Automaton aut : project)
        {
            for (State state : aut)
            {
                // This now works?
                /*
                if (state.getCost() != State.UNDEF_COST)
                {
                    logger.warn("State cost information in the automata model is not supported by the editor.");
                    return false;
                }
                */
            }
        }
        return true;
    }

    
    //#########################################################################
    //# Data Members
    private final ProjectBuildFromXML builder;
    private DocumentManager mDocumentManager;

    private static final Logger logger =
        LoggerFactory.createLogger(SupremicaUnmarshaller.class);

}
