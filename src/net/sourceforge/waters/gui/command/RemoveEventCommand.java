package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.EventListExpressionSubject;

public class RemoveEventCommand
    implements Command
{
    private final EventListExpressionSubject mList;
    private final AbstractSubject mIdentifier;
    private final int mPosition;
    private final String mDescription = "Remove Event";

    public RemoveEventCommand(final EventListExpressionSubject group,
        final AbstractSubject identifier)
    {
        mList = group;
        mIdentifier = identifier;
        mPosition = mList.getEventIdentifierListModifiable().indexOf(mIdentifier);
    }

    public void execute()
    {
        mList.getEventIdentifierListModifiable().remove(mIdentifier);
    }

    /**
     * Undoes the Command
     */
    public void undo()
    {
        int pos = mPosition;
        if (pos > mList.getEventIdentifierListModifiable().size())
        {
            pos = mList.getEventIdentifierListModifiable().size();
        }
        mList.getEventIdentifierListModifiable().add(pos, mIdentifier);
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
