

package org.supremica.util.BDD.encoding;

import org.supremica.util.BDD.*;


/**
 * Factory class, returns a state encoding object according to the chosen algorithm
 * ( see "BDD 2" tab in the prefrence dialog)
 *
 */

public class EncodingFactory {


	public static Encoding getEncoder() {
		switch(Options.encoding_algorithm) {
			case Options.ENCODING_DEFAULT:	return new DefaultEncoding();
			case Options.ENCODING_RANDOM:	return new RandomEncoding();
			case Options.ENCODING_DFS_I:	return new DFSEncoding(true);
			case Options.ENCODING_DFS_M:	return new DFSEncoding(false);
			case Options.ENCODING_BFS_I:	return new BFSEncoding(true);
			case Options.ENCODING_BFS_M:	return new BFSEncoding(false);
			default:
				System.err.println("Unknown encoding algorithm requested!");
		}


		// shouldn't come here
		return null;
	}
}
