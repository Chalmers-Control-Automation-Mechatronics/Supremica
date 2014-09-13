//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: 
//# PACKAGE: net.sourceforge.waters.analysis.efa.simple
//# CLASS:   SimpleEFAEventEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.simple;

import gnu.trove.map.hash.TIntByteHashMap;
import gnu.trove.set.hash.THashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.xsd.base.EventKind;

/**
 *
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFAEventEncoding extends SimpleInfoEncoder<SimpleEFAEventDecl>
{

  public SimpleEFAEventEncoding(final int size)
  {
    super(size);
    mEventStatus = new TIntByteHashMap(size, 0.6f, -1, Byte.MIN_VALUE);
    mPropositions = new THashSet<>();
    createEventId(SimpleEFAHelper.getSubjectTAUDecl());
  }

  public SimpleEFAEventEncoding()
  {
    this(DEFAULT_SIZE);
  }

  public SimpleEFAEventEncoding(final SimpleEFAEventEncoding encoding)
  {
    super(encoding);
    mEventStatus = encoding.getEventsStatus();
  }

  public int getEventId(final SimpleEFAEventDecl event)
  {
    return getInfoId(event);
  }

  public TIntByteHashMap getEventsStatus()
  {
    return new TIntByteHashMap(mEventStatus);
  }

  public byte getEventStatus(final SimpleEFAEventDecl event)
  {
    return mEventStatus.get(getInfoId(event));
  }

  public byte getEventStatus(final int eventId)
  {
    return mEventStatus.get(eventId);
  }

  public Set<EventDeclProxy> getPropositions()
  {
    return Collections.unmodifiableSet(mPropositions);
  }

  public SimpleEFAEventDecl getEventDecl(final int eventId)
  {
    return decode(eventId);
  }

  public SimpleEFAEventDecl getEventDecl(final String name)
  {
    final List<SimpleEFAEventDecl> list = getEventDeclListExceptTau();
    for (final SimpleEFAEventDecl event : list) {
      if (event.toString().equals(name)) {
        return event;
      }
    }
    return null;
  }

  public int getEventId(final IdentifierProxy ident)
  {
    final List<SimpleEFAEventDecl> list = getEventDeclListExceptTau();
    for (final SimpleEFAEventDecl event : list) {
      if (event.toString().equals(ident.toString())) {
        return getEventId(event);
      }
    }
    return -1;
  }

  public final int createEventId(final SimpleEFAEventDecl event)
  {
    int id = encode(event);
    mEventStatus.put(id, getEventDeclStatus(event));
    if (event.getKind() == EventKind.PROPOSITION) {
      mPropositions.add(event.getEventDecl());
    }
    return id;
  }

  public List<SimpleEFAEventDecl> getEventDeclListExceptTau()
  {
    List<SimpleEFAEventDecl> list = new ArrayList<>(super.size());
    for (int i = NONTAU; i < super.size(); i++) {
      list.add(decode(i));
    }
    return Collections.unmodifiableList(list);
  }

  public List<SimpleEFAEventDecl> getEventDeclListIncludingTau()
  {
    return getInformation();
  }

  public void removeEventDecl(final SimpleEFAEventDecl event)
  {
    replaceInfo(getEventId(event), null);
  }

  public void setEventStatus(int eventId, byte status)
  {
    byte index = mEventStatus.get(eventId);
    if (index > Byte.MIN_VALUE) {
      mEventStatus.put(eventId, status);
    }
  }

  public void setEventStatus(SimpleEFAEventDecl event, byte status)
  {
    setEventStatus(getEventId(event), status);
  }

  public void resetEventStatus(SimpleEFAEventDecl event)
  {
    setEventStatus(getEventId(event), getEventDeclStatus(event));
  }

  public void resetAllEventsStatus()
  {
    for (int i = NONTAU; i < getInformation().size(); i++) {
      resetEventStatus(getEventDecl(i));
    }
  }

  public boolean isControllable(final int eventId)
  {
    return isControllable(getEventStatus(eventId));
  }

  public boolean isLocal(final int eventId)
  {
    return isLocal(getEventStatus(eventId));
  }

  public boolean isObservable(final int eventId)
  {
    return isObservable(getEventStatus(eventId));
  }

  @Override
  public int size()
  {
    return super.size();
  }

  public static byte getEventDeclStatus(final SimpleEFAEventDecl event)
  {
    byte status = STATUS_NONE;
    status |= event.getKind() == EventKind.CONTROLLABLE ? STATUS_CONTROLLABLE : STATUS_NONE;
    status |= event.isLocal() ? STATUS_LOCAL : STATUS_NONE;
    status |= event.isObservable() ? STATUS_OBSERVABLE : STATUS_NONE;
    status |= event.isBlocked() ? STATUS_BLOCKED : STATUS_NONE;
    status |= event.isProposition() ? STATUS_PROPOSITION : STATUS_NONE;
    return status;
  }

  public static boolean isControllable(final byte status)
  {
    return (status & STATUS_CONTROLLABLE) > 0;
  }

  public static boolean isLocal(final byte status)
  {
    return (status & STATUS_LOCAL) > 0;
  }

  public static boolean isObservable(final byte status)
  {
    return (status & STATUS_OBSERVABLE) > 0;
  }

  public static final int TAU = 0;
  public static final int NONTAU = TAU + 1;
  public static final byte STATUS_NONE = 0x00;
  public static final byte STATUS_CONTROLLABLE = 0x01;
  public static final byte STATUS_LOCAL = 0x02;
  public static final byte STATUS_OBSERVABLE = 0x04;
  public static final byte STATUS_BLOCKED = 0x08;
  public static final byte STATUS_PROPOSITION = 0x10;
  public static final byte STATUS_OTHER = 0x20;

  private TIntByteHashMap mEventStatus;
  private THashSet<EventDeclProxy> mPropositions;
}
