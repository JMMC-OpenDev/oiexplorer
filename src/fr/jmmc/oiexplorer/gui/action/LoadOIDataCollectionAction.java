/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.gui.action;

import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.jmcs.gui.component.FileChooser;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.jmcs.jaxb.JAXBFactory;
import fr.jmmc.jmcs.jaxb.JAXBUtils;
import fr.jmmc.jmcs.util.MimeType;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oiexplorer.core.model.oi.OIDataFile;
import fr.jmmc.oiexplorer.core.model.oi.OiDataCollection;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.nom.tam.fits.FitsException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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
    }

    /**
     * Handle the action event
     * @param evt action event
     */
    @Override
    public void actionPerformed(final ActionEvent evt) {
        logger.debug("actionPerformed");

        File file = null;


        file = FileChooser.showOpenFileChooser("Load oiexplorer data collection file", null, mimeType, null);

        // If a file was defined (No cancel in the dialog)
        if (file != null) {

            final OIFitsChecker checker = new OIFitsChecker();

            OIFitsCollectionManager.getInstance().setNotify(false);

            final long startTime = System.nanoTime();

            String fileLocation = file.getAbsolutePath();
            Exception e = null;
            try {
                JAXBFactory jbf = JAXBFactory.getInstance(OiDataCollection.class.getPackage().getName());
                OiDataCollection userCollection = (OiDataCollection) JAXBUtils.loadObject(file.toURI().toURL(), jbf);
                StatusBar.show("loading collection: " + fileLocation);

                OIFitsCollectionManager.getInstance().loadOIDataCollection(userCollection, checker);
            } catch (MalformedURLException mue) {
                e = mue;
            } catch (IOException ioe) {
                e = ioe;
            } catch (FitsException fe) {
                e = fe;
            } finally {
                logger.info("LoadOIFitsAction {} : duration = {} ms.", 1e-6d * (System.nanoTime() - startTime));

                OIFitsCollectionManager.getInstance().setNotify(true);

                final String checkReport = checker.getCheckReport();

                if (e != null) {
                    MessagePane.showErrorMessage("Error loading data from file : " + fileLocation, e);
                    StatusBar.show("Error loading data from  file : " + fileLocation);
                } else {
                    MessagePane.showMessage(checkReport);

                }
                // display validation messages anyway:
                logger.info("validation results:\n{}", checkReport);

            }
        }

    }
}
