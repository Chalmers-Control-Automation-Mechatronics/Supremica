//# -*- indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: org.supremica.automata.IO
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

import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxyFactory;

import org.supremica.automata.Project;

import org.xml.sax.SAXException;


public class SupremicaUnmarshaller implements ProxyUnmarshaller<Project>
{

  //#########################################################################
  //# Constructor
  public SupremicaUnmarshaller(final ModuleProxyFactory modfactory)
    throws SAXException
  {
    builder = new ProjectBuildFromXML();
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.model.marshaller.ProxyUnmarshaller<ModuleProxy>
  @Override
  public Project unmarshal(final URI uri)
    throws WatersUnmarshalException, IOException
  {
    final URL url = uri.toURL();
    final Project project;
    try {
      project = builder.build(url);
      project.setLocation(uri);
    } catch (final Exception ex) {
      throw new WatersUnmarshalException(ex);
    }
    return project;
  }

  @Override
  public Class<Project> getDocumentClass()
  {
    return Project.class;
  }

  @Override
  public String getDefaultExtension()
  {
    return SupremicaMarshaller.XML_FILE_FILTER.getExtension();
  }

  @Override
  public Collection<String> getSupportedExtensions()
  {
    final String ext = getDefaultExtension();
    return Collections.singletonList(ext);
  }

  @Override
  public Collection<FileFilter> getSupportedFileFilters()
  {
    return Collections.singletonList(SupremicaMarshaller.XML_FILE_FILTER);
  }


  //#########################################################################
  //# Static Class Methods
  /**
   * Checks for Supremica-to-Waters conversion problems.
   * This method examines the given Supremica project to determine whether
   * it uses any features that cannot be converted to Waters.
   * The present implementation checks for the presence of an animation
   * and for events marked as <I>not prioritised</I>.
   * @return <CODE>true</CODE> if the given project can be converted to
   *         Waters without loss of information, <CODE>false</CODE> otherwise.
   */
  public static boolean isWatersCompatible(final Project project)
  {
    return !project.hasAnimation() && !project.hasNonPrioritizedEvents();
  }


  //#########################################################################
  //# Data Members
  private final ProjectBuildFromXML builder;

}
