/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata;

import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.subject.module.EventDeclSubject;

/**
 *
 * @author Sajed, Alexey, Zhennan
 */
public class LocationEvent {
    
    NodeProxy location;
    String event;

    public LocationEvent(NodeProxy location, String event)
    {
        this.location = location;
        this.event = event;
    }

    public NodeProxy getLocation()
    {
        return location;
    }

    public String getEvent()
    {
        return event;
    }

    @Override
    public int hashCode()
    {
        return (location.hashCode()+event.hashCode());
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof LocationEvent)
        {
            LocationEvent le = (LocationEvent)obj;
            return (le.getLocation().equals(location) && le.getEvent().equals(event));
        }
        else
        {
            return false;
        }
    }

}
