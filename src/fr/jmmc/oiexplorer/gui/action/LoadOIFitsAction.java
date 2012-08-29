/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.gui.action;

import fr.jmmc.jmcs.data.preference.FileChooserPreferences;
import fr.jmmc.jmcs.gui.action.ActionRegistrar;
import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.jmcs.gui.component.FileChooser;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.jmcs.util.MimeType;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.nom.tam.fits.FitsException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load one (or more) files.
 * @author mella
 */
public class LoadOIFitsAction extends RegisteredAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class name. This name is used to register to the ActionRegistrar */
    public final static String className = LoadOIFitsAction.class.getName();
    /** Action name. This name is used to register to the ActionRegistrar */
    public final static String actionName = "loadOIFits";
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(className);
    /** OIFits MimeType */
    private final static MimeType mimeType = MimeType.OIFITS;

    /**
     * Public constructor that automatically register the action in RegisteredAction.
     */
    public LoadOIFitsAction() {
        super(className, actionName);
    }

    /**
     * Handle the action event
     * @param evt action event
     */
    @Override
    public void actionPerformed(final ActionEvent evt) {
        logger.debug("actionPerformed");

        File files[] = null;

        // If the action was automatically triggered from App launch
        if (evt.getSource() == ActionRegistrar.getInstance()) {
            File file = new File(evt.getActionCommand());

            if (!file.exists() || !file.isFile()) {
                MessagePane.showErrorMessage("Could not load the file : " + file.getAbsolutePath());
                files = null;
            }

            if (file != null) {
                // update current directory for oidata:
                FileChooserPreferences.setCurrentDirectoryForMimeType(mimeType, file.getParent());

                files = new File[]{file};
            }

        } else {
            files = FileChooser.showOpenFilesChooser("Load oifits file", null, mimeType);
        }

        // If a file was defined (No cancel in the dialog)
        if (files != null) {
            final OIFitsChecker checker = new OIFitsChecker();

            OIFitsCollectionManager.getInstance().setNotify(false);

            final long startTime = System.nanoTime();

            String fileLocation = null;
            Exception e = null;
            try {
                for (File file : files) {
                    fileLocation = file.getAbsolutePath();

                    StatusBar.show("loading file: " + fileLocation);

                    OIFitsCollectionManager.getInstance().loadOIFitsFile(fileLocation, checker);
                }
            } catch (MalformedURLException mue) {
                e = mue;
            } catch (IOException ioe) {
                e = ioe;
            } catch (FitsException fe) {
                e = fe;
            } finally {
                logger.info("LoadOIFitsAction: duration = {} ms.", 1e-6d * (System.nanoTime() - startTime));

                OIFitsCollectionManager.getInstance().setNotify(true);

                if (e != null) {
                    MessagePane.showErrorMessage("Could not load the file : " + fileLocation, e);
                    StatusBar.show("Could not load the file : " + fileLocation);
                }

                // display validation messages anyway:
                final String checkReport = checker.getCheckReport();
                logger.info("validation results:\n{}", checkReport);
                
                MessagePane.showMessage(checkReport);
            }
        }

    }
}
