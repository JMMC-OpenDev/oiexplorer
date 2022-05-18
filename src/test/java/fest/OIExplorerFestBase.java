/** *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/

package fest;

import fest.common.JmcsFestSwingJUnitTestCase;
import fr.jmmc.jmcs.Bootstrapper;
import fr.jmmc.jmcs.data.preference.CommonPreferences;
import fr.jmmc.jmcs.data.preference.PreferencesException;
import fr.jmmc.jmcs.data.preference.SessionSettingsPreferences;
import fr.jmmc.oiexplorer.Preferences;
import java.io.File;
import org.fest.swing.annotation.GUITest;
import org.fest.swing.exception.ComponentLookupException;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OIExplorerFestBase extends JmcsFestSwingJUnitTestCase {

    public static int MY_DELAY = NULL_DELAY;  // choose NULL_DELAY for speed and MEDIUM_DELAY for debug

    public static final String MENU_FILE = "File";
    public static final String MENU_NEW = "New OIFits Collection";
    public static final String MENU_ADD = "Add OIFits file";
    public static final String MENU_REMOVE = "Remove selected OIFits file(s)";

    public static File RESOURCE_TEST_FOLDER = new File(getProjectFolderPath() + "src/test/resources/");
    public static String GAMMA2_VELORUM_FILENAME
            = "PRODUCT_Gamma2_Velorum_2.02-2.09micron_2007-03-31T01_39_00.5372.fits";
    public static String GAM_VIC_FILENAME = "2008-Contest_Binary.fits";

    @BeforeClass
    /**
     * FIRST method, starts the app.
     */
    public static void m0_init() {

        // invoke Bootstrapper method to initialize logback now:
        Bootstrapper.getState();

        // reset Preferences:
        Preferences.getInstance().resetToDefaultPreferences();
        SessionSettingsPreferences.getInstance().resetToDefaultPreferences();

        try {
            CommonPreferences.getInstance().setPreference(CommonPreferences.SHOW_STARTUP_SPLASHSCREEN, false);
            CommonPreferences.getInstance().setPreference(CommonPreferences.FEEDBACK_REPORT_USER_EMAIL, "FAKE_EMAIL");
        } catch (PreferencesException pe) {
            logger.error("setPreference failed", pe);
        }

        // define robot delays :
        defineRobotDelayBetweenEvents(MY_DELAY);


        // disable tooltips :
        enableTooltips(false);

        // Start application:
        JmcsFestSwingJUnitTestCase.startApplication(fr.jmmc.oiexplorer.OIFitsExplorer.class);
    }

    @Test
    @GUITest
    /**
     * LAST method, exits the app.
     */
    public void m999_exit() {
        window.close();
        confirmDialogDontSave();
    }

    public void newCollection() {
        window.menuItemWithPath(MENU_FILE, MENU_NEW).click();
    }

    public void addOIFitsFile(File directory, String filename) {
        window.menuItemWithPath(MENU_FILE, MENU_ADD).click();
        window.fileChooser().setCurrentDirectory(directory);
        window.fileChooser().selectFile(new File(filename));
        window.fileChooser().approve();
        window.optionPane().okButton().click();
    }

    public void remove() {
        window.menuItemWithPath(MENU_FILE, MENU_REMOVE).click();
        try {
            window.optionPane().yesButton().click();
        } catch (ComponentLookupException e) {
            window.optionPane().okButton().click();
        }
    }
}
