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
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.StandardExtensionFileFilter;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxyFactory;

import org.supremica.automata.Project;

import org.xml.sax.SAXException;


public class SupremicaUnmarshaller implements ProxyUnmarshaller<Project>
{

  //#########################################################################
  //# Constructor
  public SupremicaUnmarshaller(final ModuleProxyFactory modfactory)
    throws JAXBException, SAXException
  {
    builder = new ProjectBuildFromXML();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.marshaller.ProxyUnmarshaller
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
    return ".xml";
  }

  public String getDescription()
  {
    return "Supremica Project files [*.xml]";
  }

  @Override
  public Collection<String> getSupportedExtensions()
  {
    final String ext = getDefaultExtension();
    return Collections.singletonList(ext);
  }

  public FileFilter getDefaultFileFilter()
  {
    final String ext = getDefaultExtension();
    final String description = getDescription();
    return StandardExtensionFileFilter.getFilter(description, ext);
  }

  @Override
  public Collection<FileFilter> getSupportedFileFilters()
  {
    final FileFilter filter = getDefaultFileFilter();
    return Collections.singletonList(filter);
  }

  @Override
  public DocumentManager getDocumentManager()
  {
    return mDocumentManager;
  }

  @Override
  public void setDocumentManager(final DocumentManager manager)
  {
    mDocumentManager = manager;
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
  private DocumentManager mDocumentManager;
}
