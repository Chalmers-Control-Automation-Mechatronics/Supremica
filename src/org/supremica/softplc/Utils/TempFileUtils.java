/**
 * This class handles the creation of temporary directories in the
 * system's default temporary-file directory.
 * @author Anders Röding
 */
package org.supremica.softplc.Utils;
import java.io.File;
import java.util.Random;
import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.lang.SecurityException;
public class TempFileUtils {

	public static void main(String[] args) {
		try {
			File tmpdir = createTempDir("ILC");
			System.out.println(tmpdir);
		}
		catch (IOException e) {
			System.err.println(e);
		}
	}

	/**
	 * Creates a new empty directory in the default temporary-file directory,
	 * using the given prefix to generate the name.
	 * @param prefix The prefix string to be used in generating the
	 *               directory's name; must be at least three characters long
	 * @exception java.lang.IllegalArgumentException If the prefix argument
	 *                                               contains fewer than three
	 *                                               characters
	 * @exception java.io.IOException If a file could not be created
	 * @exception java.lang.SecurityException If a security manager exists
	 *                  and its SecurityManager.checkWrite(java.lang.String)
	 *                  method does not allow a file to be created
	 */
	public static File createTempDir(String prefix)
		throws IOException, IllegalArgumentException, SecurityException {
		if (prefix.length() < 3)
			throw new IllegalArgumentException("Prefix string too short");
		Random rand = new Random();
		String systemTempDir = System.getProperty("java.io.tmpdir");
		SecurityManager security = System.getSecurityManager();
		if (security != null) {
			security.checkWrite(systemTempDir);
		}
		File tempDir;
		for (int i = 0; i<200; i++){
			tempDir = new File(systemTempDir, prefix + rand.nextInt(999999));
			if (tempDir.mkdir())
				return tempDir;
		}
		throw new IOException("Could not create directory in " +
							  systemTempDir);
	}










}
