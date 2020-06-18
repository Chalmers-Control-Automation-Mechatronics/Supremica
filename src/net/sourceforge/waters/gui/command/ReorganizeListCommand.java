//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui.command;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.IdentityHashMap;
import java.util.ArrayList;

import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.EventListExpressionSubject;

public class ReorganizeListCommand
	implements Command
{
	private final EventListExpressionSubject mList;
	private final List<AbstractSubject> mIdentifiers;
	private final IdentityHashMap<AbstractSubject, Integer> mIndexs;
	private final int mNewPosition;
	private final String mDescription = "Move Event";

	public ReorganizeListCommand(final EventListExpressionSubject group,
                               final List<? extends AbstractSubject> identifiers,
                               final int newPosition)
	{
		mList = group;
		mIdentifiers = new ArrayList<AbstractSubject>(identifiers.size());
		mIdentifiers.addAll(identifiers);
		Collections.sort(mIdentifiers, new Comparator<AbstractSubject>()
		{
			public int compare(final AbstractSubject a1, final AbstractSubject a2)
			{
				return (mList.getEventIdentifierListModifiable().indexOf(a1) -
                mList.getEventIdentifierListModifiable().indexOf(a2));
			}

			public boolean equals(final Object o)
			{
				return o == this;
			}
		});
		mIndexs = new IdentityHashMap<AbstractSubject, Integer>();
		for (final AbstractSubject a : identifiers)
		{
			final int index = mList.getEventIdentifierList().indexOf(a);
			mIndexs.put(a, new Integer(index));
		}
		mNewPosition = newPosition;
	}

	public void execute()
	{
		final List<AbstractSubject> list = mList.getEventIdentifierListModifiable();
		list.removeAll(mIdentifiers);
		int i = 0;
		for (final AbstractSubject a : mIdentifiers)
		{
			int index = mNewPosition + i;
			if (index > list.size())
			{
				index = list.size();
			}
			list.add(index, a);
			i++;
		}
		// Remove label and add to new position in list
	}

    /**
     * Undoes the Command
     */
    public void undo()
    {
		final List<AbstractSubject> list =
						mList.getEventIdentifierListModifiable();
		// Remove label and add to new position in list
		list.removeAll(mIdentifiers);
		for (final AbstractSubject a : mIdentifiers)
		{
			list.add(mIndexs.get(a).intValue(), a);
		}
    }

    public void setUpdatesSelection(final boolean update)
    {
    }

	public boolean isSignificant()
	{
		return true;
	}

    public String getName()
    {
		return mDescription;
    }
}
