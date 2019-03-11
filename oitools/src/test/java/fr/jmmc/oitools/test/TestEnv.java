/* 
 * Copyright (C) 2018 CNRS - JMMC project ( http://www.jmmc.fr )
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
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
  public final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(TestEnv.class.getName());
}
