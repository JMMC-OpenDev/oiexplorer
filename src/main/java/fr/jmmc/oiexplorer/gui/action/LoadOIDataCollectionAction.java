/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.gui.action;

import fr.jmmc.jmcs.data.MimeType;
import fr.jmmc.jmcs.data.preference.SessionSettingsPreferences;
import fr.jmmc.jmcs.gui.action.ActionRegistrar;
import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.jmcs.gui.component.FileChooser;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.jmcs.util.jaxb.XmlBindException;
import fr.jmmc.oiexplorer.OIFitsExplorer;
import fr.jmmc.oiexplorer.Preferences;
import fr.jmmc.oiexplorer.core.gui.OIFitsCheckerPanel;
import fr.jmmc.oiexplorer.core.model.LoadOIFitsListener;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oitools.model.OIFitsChecker;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load one (or more) files from xml file.
 * @author mella
 */
public final class LoadOIDataCollectionAction extends RegisteredAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class name. This name is used to register to the ActionRegistrar */
    public final static String className = LoadOIDataCollectionAction.class.getName();
    /** Action name. This name is used to register to the ActionRegistrar */
    public final static String actionName = "loadOIDataCollection";
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(className);
    /** OIFitsExplorer MimeType */
    private final static MimeType mimeType = MimeType.OIFITS_EXPLORER_COLLECTION;

    /**
     * Public constructor that automatically register the action in RegisteredAction.
     */
    public LoadOIDataCollectionAction() {
        super(className, actionName);
        flagAsOpenAction();
    }

    /**
     * Handle the action event
     * @param evt action event
     */
    @Override
    public void actionPerformed(final ActionEvent evt) {
        logger.debug("actionPerformed");
        
        if (!OIFitsExplorer.getInstance().checkAndConfirmSaveChanges("loading an OIFits Collection")) {
            return;
        }

        final OIFitsCollectionManager ocm = OIFitsCollectionManager.getInstance();

        File file;

        // If the action was automatically triggered from App launch
        if (evt.getSource() == ActionRegistrar.getInstance()) {
            file = new File(evt.getActionCommand());

            if (!file.exists() || !file.isFile()) {
                MessagePane.showErrorMessage("Could not load the file : " + file.getAbsolutePath());
                file = null;
            }

            if (file != null) {
                // update current directory for oidata:
                SessionSettingsPreferences.setCurrentDirectoryForMimeType(mimeType, file.getParent());
            }

        } else {

            final File oiFitsCollectionFile = ocm.getOiFitsCollectionFile();

            final String defaultFileName;

            if (oiFitsCollectionFile != null) {
                defaultFileName = oiFitsCollectionFile.getName();
            } else {
                defaultFileName = null;
            }

            file = FileChooser.showOpenFileChooser("Load an OIFits Collection", null, mimeType, defaultFileName);
        }

        // If a file was defined (No cancel in the dialog)
        if (file != null) {
            loadOIFitsCollectionFromFile(file, ocm, false);
        }
    }

    public static void loadOIFitsCollectionFromFile(File file, final OIFitsCollectionManager ocm, final boolean appendOIFitsFilesOnly) throws XmlBindException {
        final String fileLocation = file.getAbsolutePath();
        if (appendOIFitsFilesOnly) {
            StatusBar.show("loading OIFits files from OIFits Collection: " + fileLocation);
        } else {
            StatusBar.show("loading OIFits Collection: " + fileLocation);
        }

        // Create progress panel:
        final JProgressBar progressBar = new JProgressBar();
        final JPanel progressPanel = LoadOIFitsAction.createLoadOIFitsProgressPanel(progressBar);

        StatusBar.addCustomPanel(progressPanel);

        Exception e = null;
        try {
            final OIFitsChecker checker = OIFitsChecker.newInstance();

            ocm.loadOIFitsCollection(file, checker,
                    new LoadOIFitsListener() {

                @Override
                public void propertyChange(final PropertyChangeEvent pce) {
                    if ("progress".equals(pce.getPropertyName())) {
                        progressBar.setValue((Integer) pce.getNewValue());
                    }
                }

                @Override
                public void done(final boolean cancelled) {
                    StatusBar.removeCustomPanel(progressPanel);

                    if (!cancelled) {
                        OIFitsCheckerPanel.displayReport(checker, Preferences.getInstance());
                    }

                    // Fire the Ready event to any listener:
                    ocm.fireReady(this, null);
                }
            }, appendOIFitsFilesOnly);

        } catch (IllegalStateException ise) {
            e = ise;
        } catch (IOException ioe) {
            e = ioe;
        } finally {
            if (e != null) {
                StatusBar.removeCustomPanel(progressPanel);

                StatusBar.show("Could not load OIFits Collection: " + fileLocation);
                MessagePane.showErrorMessage("Could not load OIFits Collection: " + fileLocation, e);
            }
        }
    }
}
