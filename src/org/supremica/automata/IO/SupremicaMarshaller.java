//# -*- indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.valid
//# CLASS:   SupremicaMarshaller
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.automata.IO;

import java.io.File;
import java.io.IOException;

import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.StandardExtensionFileFilter;

import org.supremica.automata.Project;


public class SupremicaMarshaller
    implements ProxyMarshaller<Project>
{

    //#########################################################################
    //# Constructor
    public SupremicaMarshaller()
    {
    }


    //#########################################################################
    //# Interface net.sourceforge.waters.model.marshaller.ProxyMarshaller
    @Override
    public void marshal(final Project project, final File filename)
        throws IOException
    {
        final AutomataToXML exporter = new AutomataToXML(project);
        exporter.serialize(filename.getAbsolutePath());
    }

    @Override
    public Class<Project> getDocumentClass()
    {
        return Project.class;
    }

    @Override
    public String getDefaultExtension()
    {
        return XML_FILE_FILTER.getExtension();
    }

    @Override
    public StandardExtensionFileFilter getDefaultFileFilter()
    {
        return XML_FILE_FILTER;
    }


    //#########################################################################
    //# Class Constants
    public static StandardExtensionFileFilter XML_FILE_FILTER =
      new StandardExtensionFileFilter("Supremica Project files (*.xml)", ".xml");

}
