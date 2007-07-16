//# -*- indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.valid
//# CLASS:   SupremicaMarshaller
//###########################################################################
//# $Id: SupremicaMarshaller.java,v 1.2 2007-07-16 11:34:32 flordal Exp $
//###########################################################################

package org.supremica.automata.IO;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import javax.swing.filechooser.FileFilter;

import net.sourceforge.waters.model.marshaller.DocumentManager;
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
    public void marshal(final Project project, final File filename)
        throws IOException
    {
        final AutomataToXML exporter = new AutomataToXML(project);
        exporter.serialize(filename.getAbsolutePath());
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

    public FileFilter getDefaultFileFilter()
    {
        final String ext = getDefaultExtension();
        final String description = getDescription();
        return StandardExtensionFileFilter.getFilter(ext, description);
    }
}
