//# -*- indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.valid
//# CLASS:   SupremicaUnmarshaller
//###########################################################################
//# $Id: SupremicaUnmarshaller.java,v 1.2 2006-09-15 09:26:13 robi Exp $
//###########################################################################

package org.supremica.automata.IO;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import org.supremica.gui.StandardExtensionFileFilter;

import org.xml.sax.SAXException;


public class SupremicaUnmarshaller
    implements ProxyUnmarshaller<ModuleProxy>
{
    
    //#########################################################################
    //# Constructor
    public SupremicaUnmarshaller(final ModuleProxyFactory modfactory)
    throws JAXBException, SAXException
    {
        builder = new ProjectBuildFromXml();
        mImporter = new ProductDESImporter(modfactory);
    }


    //#########################################################################
    //# Interface net.sourceforge.waters.model.marshaller.ProxyUnmarshaller
    public ModuleProxy unmarshal(final URI uri)
    throws WatersUnmarshalException, IOException
    {
        URL url = uri.toURL();
        final ProductDESProxy des;
        try
        {
           des = builder.build(url);
        }
        catch (Exception ex)
        {
            throw new WatersUnmarshalException(ex);
        }
        return mImporter.importModule(des);
    }
    
    public Class<ModuleProxy> getDocumentClass()
    {
        return ModuleProxy.class;
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
    private final ProjectBuildFromXml builder;
    private final ProductDESImporter mImporter;    
}
