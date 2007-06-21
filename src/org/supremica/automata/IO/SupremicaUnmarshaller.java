//# -*- indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.valid
//# CLASS:   SupremicaUnmarshaller
//###########################################################################
//# $Id: SupremicaUnmarshaller.java,v 1.14 2007-06-21 11:21:50 flordal Exp $
//###########################################################################

package org.supremica.automata.IO;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.html.parser.DocumentParser;
import javax.xml.bind.JAXBException;
import net.sourceforge.waters.model.base.DocumentProxy;

import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.State;
import org.supremica.gui.StandardExtensionFileFilter;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

import org.xml.sax.SAXException;


public class SupremicaUnmarshaller
    implements ProxyUnmarshaller<DocumentProxy>
{
    protected static Logger logger = LoggerFactory.createLogger(SupremicaUnmarshaller.class);

    //#########################################################################
    //# Constructor
    public SupremicaUnmarshaller(final ModuleProxyFactory modfactory)
    throws JAXBException, SAXException
    {
        builder = new ProjectBuildFromXML();
        mImporter = new ProductDESImporter(modfactory);
    }    
    
    //#########################################################################
    //# Interface net.sourceforge.waters.model.marshaller.ProxyUnmarshaller
    public DocumentProxy unmarshal(final URI uri)
    throws WatersUnmarshalException, IOException
    {
        URL url = uri.toURL();
        final Automata automata;
        try
        {
            automata = builder.build(url);
        }
        catch (Exception ex)
        {
            throw new WatersUnmarshalException(ex);
        }
            
        // Examine the result
        if (validate(automata))                
            return mImporter.importModule(automata);
        else
            // Would like to import it directly into the analyzer not to miss out
            // on the Supremica-specific parts...
            return automata;
    }       
    
    public Class<DocumentProxy> getDocumentClass()
    {
        return DocumentProxy.class;
    }
    
    public String getDefaultExtension()
    {
        return ".xml";
    }
    
    public Collection<String> getSupportedExtensions()
    {
        return Collections.singletonList(getDefaultExtension());
    }
    
    public Collection<FileFilter> getSupportedFileFilters()
    {
        FileFilter filter = new StandardExtensionFileFilter(getDefaultExtension(), "Supremica Project files [*.xml]");
        return Collections.singletonList(filter);
    }
    
    public DocumentManager getDocumentManager()
    {
        return mImporter.getDocumentManager();
    }
    
    public void setDocumentManager(DocumentManager manager)
    {
        mImporter.setDocumentManager(manager);
    }    
    
    //#########################################################################
    //# Data Members
    private final ProjectBuildFromXML builder;
    private final ProductDESImporter mImporter;
    
    /**
     * Examines if there are conversion problems in an automata. 
     */
    public static boolean validate(Automata automata)
    {
        for (Automaton aut : automata)
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
}
