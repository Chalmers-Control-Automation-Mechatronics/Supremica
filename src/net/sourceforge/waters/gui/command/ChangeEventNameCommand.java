/*package net.sourceforge.waters.gui.command;

public class ChangeEventNameCommand
	implements Command
{
	public ChangeEventNameCommand(final GraphSubject graph,
								  final EventTableModel model,
								  final identifierSubject old,
								  final identifierSubject neo,
								  final int column,
								  final int row)
	{
		mGraph = graph
		mOld = old;
		mNew = neo;
		mModel = model;
		mCommands = new CompoundCommand();
		mColumn = column;
		mRow = row;
		if (old != null)
		{
			boolean alreadyhas = false;
			mCommands.addCommand(new RemoveFromTableCommand(mModel,
															mOld,
															mRow,
															mColumn));
			for (int i = 0; i < mModel.getRowCount(); i++)
			{
				if (mRow != i && mModel.getEvent(row).equals(mNew))
				{
					alreadyhas = true;
					break;
				}
			}
			if (!alreadyhas)
			{
				mCommands.addCommand(new AddToTableCommand(mModel,
														   mNew, mRow,
														   mColumn));
			}
			ListIterator<AbstractSubject> li = mGraph.getBlockedEvents().getEventListModifiable().listIterator();
			while(li.hasNext())
			{
				AbstractSubject a = li.next();
				if (a.equals(old))
				{
					mCommands.addCommand(new addEventCommand(
				}
			}
			for (EdgeSubject e : mGraph.getEdgesModifiable())
			{
				li = e.getLabelBlock().getEventListModifiable().listIterator();
				while(li.hasNext())
				{
					AbstractSubject a = li.next();
					if (a.equals(old))
					{
						li.set(ident.clone());
					}
				}
			}
		}
	}
}*/
