/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: TestEnv.java,v 1.3 2010-06-02 15:24:08 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2010/06/02 11:52:27  bourgesl
 * use logger instead of System.out
 *
 * Revision 1.1  2010/04/28 14:39:20  bourgesl
 * basic test cases for OIValidator Viewer/Validator and new OIFitsLoader
 *
 ******************************************************************************/
package fr.jmmc.oitools.test;

/**
 * This interface holds several constants
 * @author bourgesl
 */
public interface TestEnv {
  /** folder containing oidata test files. By default $home/oidata/ */
  public final static String TEST_DIR = System.getProperty("user.home") + "/oidata/";
  /** folder containing copied oidata files. By default $home/oidata/copy/ */
  public final static String COPY_DIR = TEST_DIR + "copy/";

  /* constants */

  /** Logger associated to test classes */
  public final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
          "fr.jmmc.oitools.test");
}