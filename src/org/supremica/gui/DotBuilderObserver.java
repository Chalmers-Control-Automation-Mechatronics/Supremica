package org.supremica.gui;

import java.io.InputStream;
import att.grappa.Graph;

public interface DotBuilderObserver
{
	public void setGraph(Graph theGraph);
	public void setInputStream(InputStream inputStream);
}