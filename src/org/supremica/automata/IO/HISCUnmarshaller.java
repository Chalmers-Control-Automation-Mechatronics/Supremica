//# -*- indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.valid
//# CLASS:   SupremicaUnmarshaller
//###########################################################################
//# $Id$
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
import net.sourceforge.waters.model.marshaller.StandardExtensionFileFilter;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;

import org.xml.sax.SAXException;


public class HISCUnmarshaller
    implements ProxyUnmarshaller<ModuleProxy>
{
    //#########################################################################
    //# Constructor
    public HISCUnmarshaller(final ModuleProxyFactory modfactory)
    throws JAXBException, SAXException
    {
        builder = new ProjectBuildFromHISC();
        mImporter = new ProductDESImporter(modfactory);
    }


    //#########################################################################
    //# Interface net.sourceforge.waters.model.marshaller.ProxyUnmarshaller
    public ModuleProxy unmarshal(final URI uri)
    throws WatersUnmarshalException, IOException
    {
        final URL url = uri.toURL();
        final ProductDESProxy des;
        try
        {
           des = builder.build(url);
           return mImporter.importModule(des);
        }
        catch (final Exception ex)
        {
            throw new WatersUnmarshalException(ex);
        }
    }

    public Class<ModuleProxy> getDocumentClass()
    {
        return ModuleProxy.class;
    }

    public String getDefaultExtension()
    {
        return ".prj";
    }

    public Collection<String> getSupportedExtensions()
    {
        return Collections.singletonList(getDefaultExtension());
    }

    public Collection<FileFilter> getSupportedFileFilters()
    {
        final FileFilter filter = new StandardExtensionFileFilter("HISC Project files [*.prj]", getDefaultExtension());
        return Collections.singletonList(filter);
    }

    public DocumentManager getDocumentManager()
    {
        return mImporter.getDocumentManager();
    }

    public void setDocumentManager(final DocumentManager manager)
    {
        mImporter.setDocumentManager(manager);
    }

    //#########################################################################
    //# Data Members
    private final ProjectBuildFromHISC builder;
    private final ProductDESImporter mImporter;
}
