
/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.automata.algorithms;

import java.util.*;
import org.supremica.automata.State;

public class StateMatcherOptions
{
	public static class Accepting
	{
		private static Collection types = new LinkedList();
		public static final Accepting DontCare = new Accepting("Don't care");
		public static final Accepting Yes = new Accepting("Yes");
		public static final Accepting No = new Accepting("No");
		private String description = null;

		private Accepting(String description)
		{
			this.description = description;

			types.add(this);
		}

		public static Iterator iterator()
		{
			return types.iterator();
		}

		public String toString()
		{
			return description;
		}

		public static Accepting toType(String type)
		{
			if (type.equals(DontCare.toString()))
			{
				return DontCare;
			}

			if (type.equals(Yes.toString()))
			{
				return Yes;
			}

			if (type.equals(No.toString()))
			{
				return No;
			}

			return null;
		}

		public static Object[] toArray()
		{
			return types.toArray();
		}
	}

	public static class Forbidden
	{
		private static Collection types = new LinkedList();
		public static final Forbidden DontCare = new Forbidden("Don't care");
		public static final Forbidden Yes = new Forbidden("Yes");
		public static final Forbidden No = new Forbidden("No");
		private String description = null;

		private Forbidden(String description)
		{
			this.description = description;

			types.add(this);
		}

		public static Iterator iterator()
		{
			return types.iterator();
		}

		public String toString()
		{
			return description;
		}

		public static Forbidden toType(String type)
		{
			if (type.equals(DontCare.toString()))
			{
				return DontCare;
			}

			if (type.equals(Yes.toString()))
			{
				return Yes;
			}

			if (type.equals(No.toString()))
			{
				return No;
			}

			return null;
		}

		public static Object[] toArray()
		{
			return types.toArray();
		}
	}

	public static class Deadlock
	{
		private static Collection types = new LinkedList();
		public static final Deadlock DontCare = new Deadlock("Don't care");
		public static final Deadlock Yes = new Deadlock("Yes");
		public static final Deadlock No = new Deadlock("No");
		private String description = null;

		private Deadlock(String description)
		{
			this.description = description;

			types.add(this);
		}

		public static Iterator iterator()
		{
			return types.iterator();
		}

		public String toString()
		{
			return description;
		}

		public static Deadlock toType(String type)
		{
			if (type.equals(DontCare.toString()))
			{
				return DontCare;
			}

			if (type.equals(Yes.toString()))
			{
				return Yes;
			}

			if (type.equals(No.toString()))
			{
				return No;
			}

			return null;
		}

		public static Object[] toArray()
		{
			return types.toArray();
		}
	}

	private Accepting acceptingCondition = Accepting.DontCare;
	private Forbidden forbiddenCondition = Forbidden.DontCare;
	private Deadlock deadlockCondition = Deadlock.DontCare;

	public StateMatcherOptions() {}

	public boolean matches(State theState)
	{
		if (acceptingCondition != Accepting.DontCare)
		{
			if (acceptingCondition == Accepting.Yes)
			{
				if (!theState.isAccepting())
				{
					return false;
				}
			}
			else if (acceptingCondition == Accepting.No)
			{
				if (theState.isAccepting())
				{
					return false;
				}
			}
		}

		if (forbiddenCondition != Forbidden.DontCare)
		{
			if (forbiddenCondition == Forbidden.Yes)
			{
				if (!theState.isForbidden())
				{
					return false;
				}
			}
			else if (forbiddenCondition == Forbidden.No)
			{
				if (theState.isForbidden())
				{
					return false;
				}
			}
		}

		if (deadlockCondition != Deadlock.DontCare)
		{
			if (deadlockCondition == Deadlock.Yes)
			{
				if (!theState.isDeadlock())
				{
					return false;
				}
			}
			else if (deadlockCondition == Deadlock.No)
			{
				if (theState.isDeadlock())
				{
					return false;
				}
			}
		}

		return true;
	}

	public void setAcceptingCondition(Accepting value)
	{
		acceptingCondition = value;
	}

	public void setForbiddenCondition(Forbidden value)
	{
		forbiddenCondition = value;
	}

	public void setDeadlockCondition(Deadlock value)
	{
		deadlockCondition = value;
	}

	public Accepting getAcceptingCondition()
	{
		return acceptingCondition;
	}

	public Forbidden getForbiddenCondition()
	{
		return forbiddenCondition;
	}

	public Deadlock getDeadlockCondition()
	{
		return deadlockCondition;
	}
}
