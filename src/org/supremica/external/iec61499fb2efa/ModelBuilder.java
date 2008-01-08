/*
 * Copyright (C) 2007 Goran Cengic
 */

/*
 * @author Goran Cengic (cengic@chalmers.se)
 */

package org.supremica.external.iec61499fb2efa;

abstract class ModelBuilder
{

	abstract void loadSystem();

	abstract void analyzeSystem();

	abstract void buildModels();

	abstract void writeResult();

}
