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
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load one (or more) files from xml file.
 * @author mella
 */
public class SaveOIDataCollectionAction extends RegisteredAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class name. This name is used to register to the ActionRegistrar */
    public final static String className = SaveOIDataCollectionAction.class.getName();
    /** Action name. This name is used to register to the ActionRegistrar */
    public final static String actionName = "saveOIDataCollection";
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(className);
    /** TODO create new MimeType */
    private final static MimeType mimeType = null;

    /**
     * Public constructor that automatically register the action in RegisteredAction.
     */
    public SaveOIDataCollectionAction() {
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

        file = FileChooser.showSaveFileChooser("Save oiexplorer data collection file", null, mimeType, null);

        // If a file was defined (No cancel in the dialog)
        if (file != null) {
            final String fileLocation = file.getAbsolutePath();

            Exception e = null;
            try {
                final long startTime = System.nanoTime();

                // TODO: move such JAXB code into OIFitsCollectionManager methods !!
                final OiDataCollection userCollection = OIFitsCollectionManager.getInstance().getUserCollection();

                final JAXBFactory jbf = JAXBFactory.getInstance(OiDataCollection.class.getPackage().getName());

                JAXBUtils.saveObject(file, userCollection, jbf);

                logger.info("SaveOIDataCollectionAction in {} : duration = {} ms.", fileLocation, 1e-6d * (System.nanoTime() - startTime));
            } catch (IOException ex) {
                e = ex;
            } catch (IllegalStateException ex) {
                e = ex;
            } finally {
                if (e != null) {
                    MessagePane.showErrorMessage("Could not save the file : " + fileLocation, e);
                    StatusBar.show("Could not save the file : " + fileLocation);
                }
            }
        }
    }
}
