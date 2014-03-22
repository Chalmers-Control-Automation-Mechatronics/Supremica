//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFATransitionLabelEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.simple;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.sourceforge.waters.analysis.efa.base.AbstractEFATransitionLabelEncoding;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;

/**
 * An implementation of {@link AbstractEFATransitionLabelEncoding}.
 * <p/>
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFATransitionLabelEncoding
 extends AbstractEFATransitionLabelEncoding<Integer>
{

  public SimpleEFATransitionLabelEncoding(final SimpleEFAEventEncoding reference, final int size)
  {
    super(size);
    mEventEncoding = reference;
    mEventList = new TIntHashSet();
    mConstraintEncoder = new SimpleInfoEncoder<>();
    createTransitionLabelId(EventEncoding.TAU, ConstraintList.TRUE);
  }

  public SimpleEFATransitionLabelEncoding(final SimpleEFAEventEncoding reference)
  {
    this(reference, DEFAULT_SIZE);
  }

  public final int createTransitionLabelId(final SimpleEFAEventDecl event, final ConstraintList con)
  {
    final int eventId = mEventEncoding.getEventId(event);
    if (eventId < 0) {
      return -1;
    }
    final int conId = mConstraintEncoder.encode(con);
    final int labelId = calculateLabel(eventId, conId);
    int id = super.getTransitionLabelId(labelId);
    if (id >= 0) {
      return id;
    } else {
      id = super.createTransitionLabelId(labelId);
      mEventList.add(eventId);
      return id;
    }
  }

  public final int createTransitionLabelId(final int eventId, final ConstraintList con)
  {
    if (eventId < 0) {
      return -1;
    }
    final int conId = mConstraintEncoder.encode(con);
    final int label = calculateLabel(eventId, conId);
    int id = super.getTransitionLabelId(label);
    if (id >= 0) {
      return id;
    } else {
      id = super.createTransitionLabelId(label);
      mEventList.add(eventId);
      return id;
    }
  }

  public SimpleEFAEventEncoding getEventEncoding()
  {
    return mEventEncoding;
  }

  public int getTransitionLabelId(final SimpleEFAEventDecl event, final ConstraintList constraint)
  {
    final int eventId = mEventEncoding.getEventId(event);
    final int conId = mConstraintEncoder.getInfoId(constraint);
    return super.getTransitionLabelId(calculateLabel(eventId, conId));
  }

  public int getTransitionLabelId(final int eventId, final ConstraintList constraint)
  {
    final int conId = mConstraintEncoder.getInfoId(constraint);
    return super.getTransitionLabelId(calculateLabel(eventId, conId));
  }

  public int getTransitionLabelId(final int eventId, final int conId)
  {
    return super.getTransitionLabelId(calculateLabel(eventId, conId));
  }

  public SimpleEFAEventDecl getEventDecl(final int label)
  {
    return mEventEncoding.getEventDecl(getEventId(label));
  }

  public SimpleEFAEventDecl getEventDeclByLabelId(final int labelId)
  {
    return mEventEncoding.getEventDecl(getEventIdByLabelId(labelId));
  }

  public int getEventIdByLabelId(final int labelId)
  {
    return getEventId(getTransitionLabel(labelId));
  }

  public ConstraintList getConstraint(final int label)
  {
    return mConstraintEncoder.decode(getConstraintId(label));
  }

  public ConstraintList getConstraintByLabelId(final Integer labelId)
  {
    return mConstraintEncoder.decode(getConstraintId(getTransitionLabel(labelId)));
  }

  public List<Integer> getTransitionLabels()
  {
    return super.getTransitionLabelsExceptTau();
  }

  public List<Integer> getTransitionLabelIdsByEventId(final int eventId)
  {
    if (!mEventList.contains(eventId)) {
      return null;
    }
    final ArrayList<Integer> list = new ArrayList<>();
    for (final int lb : super.getTransitionLabelsIncludingTau()) {
      if (getEventId(lb) == eventId) {
        list.add(getTransitionLabelId(lb));
      }
    }
    return Collections.unmodifiableList(list);
  }

  public TIntArrayList getEventList()
  {
    return new TIntArrayList(mEventList);
  }

  public static int getEventId(final int label)
  {
    return (short) label;
  }

  public static int getConstraintId(final int label)
  {
    return label >> 16;
  }

  private static int calculateLabel(final int event, final int con)
  {
    return (con << 16) | event;
  }

  @Override
  public String toString()
  {
    if (super.isEmpty()) {
      return "[]";
    }
    final StringBuilder events = new StringBuilder();
    final String sep = ", ";
    events.append('[');
    for (final int label : super.getTransitionLabelsExceptTau()) {
      final String out = Integer.toString(getTransitionLabelId(label)) + " > "
       + mEventEncoding.getEventDecl(getEventId(label)) + ':'
       + mConstraintEncoder.decode(getConstraintId(label)) + sep;
      events.append(out);
    }
    events.delete(events.length() - sep.length(), events.length());
    events.append(']');
    return events.toString();
  }

  public static final int TRUE_INDEX = 0;
  private final SimpleInfoEncoder<ConstraintList> mConstraintEncoder;
  private final SimpleEFAEventEncoding mEventEncoding;
  private final TIntHashSet mEventList;

}
