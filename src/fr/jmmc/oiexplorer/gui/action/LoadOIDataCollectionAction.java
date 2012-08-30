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
import fr.jmmc.jmcs.jaxb.JAXBFactory;
import fr.jmmc.jmcs.jaxb.JAXBUtils;
import fr.jmmc.jmcs.util.MimeType;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oiexplorer.core.model.oi.OiDataCollection;
import fr.jmmc.oitools.model.OIFitsChecker;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load one (or more) files from xml file.
 * @author mella
 */
public class LoadOIDataCollectionAction extends RegisteredAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class name. This name is used to register to the ActionRegistrar */
    public final static String className = LoadOIDataCollectionAction.class.getName();
    /** Action name. This name is used to register to the ActionRegistrar */
    public final static String actionName = "loadOIDataCollection";
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(className);
    /** TODO create new MimeType */
    private final static MimeType mimeType = null;

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
                FileChooserPreferences.setCurrentDirectoryForMimeType(mimeType, file.getParent());
            }

        } else {
            file = FileChooser.showOpenFileChooser("Load oiexplorer data collection file", null, mimeType, null);
        }

        // If a file was defined (No cancel in the dialog)
        if (file != null) {

            final OIFitsChecker checker = new OIFitsChecker();

            final String fileLocation = file.getAbsolutePath();

            StatusBar.show("loading collection: " + fileLocation);

            try {
                final long startTime = System.nanoTime();

                // TODO: move such JAXB code into OIFitsCollectionManager methods !!
                final JAXBFactory jbf = JAXBFactory.getInstance(OiDataCollection.class.getPackage().getName());

                final OiDataCollection userCollection = (OiDataCollection) JAXBUtils.loadObject(file.toURI().toURL(), jbf);

                OIFitsCollectionManager.getInstance().loadOIDataCollection(userCollection, checker);

                logger.info("LoadOIDataCollectionAction: duration = {} ms.", 1e-6d * (System.nanoTime() - startTime));

            } catch (IllegalStateException ise) {
                MessagePane.showErrorMessage("Could not load collection: " + fileLocation, ise);
                StatusBar.show("Could not load collection: " + fileLocation);
            } catch (IOException ioe) {
                MessagePane.showErrorMessage(ioe.getMessage(), ioe.getCause());
                StatusBar.show(ioe.getMessage());
            } finally {
                // display validation messages anyway:
                final String checkReport = checker.getCheckReport();
                logger.info("validation results:\n{}", checkReport);

                MessagePane.showMessage(checkReport);
            }
        }
    }
}
