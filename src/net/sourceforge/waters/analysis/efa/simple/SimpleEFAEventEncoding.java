//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: 
//# PACKAGE: net.sourceforge.waters.analysis.efa.simple
//# CLASS:   SimpleEFAEventEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.simple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.module.IdentifierProxy;

/**
 *
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFAEventEncoding extends SimpleInfoEncoder<SimpleEFAEventDecl>
{

  public SimpleEFAEventEncoding(final int size)
  {
    super(size);
    createEventId(SimpleEFAHelper.getSubjectTAUDecl());
  }

  public SimpleEFAEventEncoding()
  {
    this(DEFAULT_SIZE);
  }

  public SimpleEFAEventEncoding(final SimpleEFAEventEncoding encoding)
  {
    super(encoding);
  }

  public int getEventId(final SimpleEFAEventDecl event)
  {
    return super.getInfoId(event);
  }

  public SimpleEFAEventDecl getEventDecl(final int eventId)
  {
    return super.decode(eventId);
  }

  public SimpleEFAEventDecl getEventDecl(final IdentifierProxy ident)
  {
    final List<SimpleEFAEventDecl> list = getEventDeclListExceptTau();
    for (final SimpleEFAEventDecl event : list) {
      if (event.toString().equals(ident.toString())) {
        return event;
      }
    }
    return null;
  }

  public final int createEventId(final SimpleEFAEventDecl event)
  {
    return super.encode(event);
  }

  public List<SimpleEFAEventDecl> getEventDeclListExceptTau()
  {
    final List<SimpleEFAEventDecl> list = new ArrayList<>(super.getInformation());
    list.subList(EventEncoding.NONTAU, list.size());
    return Collections.unmodifiableList(list);
  }

  public void removeEventDecl(final SimpleEFAEventDecl event)
  {
    super.replaceInfo(getEventId(event), null);
  }

}
