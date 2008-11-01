/* Potato Chip Graph editor
 * Subject.java - Subject/Observer inteface
 * Copyright (C) 2005 - Alastair Porter, Gian Perrone and Simon Ware
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * $Id$
 */

package net.sourceforge.waters.gui.observer;

/**
 * The Subject Interface for the Observer Design Pattern.
 *
 * It makes So many it so that many method of viewing and Modifying Concrete data
 * representation can Operate on the Same data representation at the Same Time and 
 * can be Updated Simultaneously
 *
 * @author Simon Ware
 */

public interface Subject
{
    /**
     * attachs an Observer to this Subject.
     */
    public void attach(Observer o);

    /**
     * removes an Observer from this Subject
     */
    public void detach(Observer o);

    /**
     * notifies all observers of this Subject of a modification
     */
    public void fireEditorChangedEvent(EditorChangedEvent e);
}
