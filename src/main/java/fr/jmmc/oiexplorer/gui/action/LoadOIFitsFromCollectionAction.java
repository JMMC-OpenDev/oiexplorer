/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.gui.action;

import fr.jmmc.jmcs.data.MimeType;
import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.jmcs.gui.component.FileChooser;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import static fr.jmmc.oiexplorer.gui.action.LoadOIDataCollectionAction.loadOIFitsCollectionFromFile;
import java.awt.event.ActionEvent;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load one (or more) files from given OIFitsCollection file.
 * @author mella
 */
public final class LoadOIFitsFromCollectionAction extends RegisteredAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class name. This name is used to register to the ActionRegistrar */
    public final static String className = LoadOIFitsFromCollectionAction.class.getName();
    /** Action name. This name is used to register to the ActionRegistrar */
    public final static String actionName = "loadOIFitsFromCollection";
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(className);
    /** OIFits MimeType */
    private final static MimeType mimeType = MimeType.OIFITS_EXPLORER_COLLECTION;

    /**
     * Public constructor that automatically register the action in RegisteredAction.
     */
    public LoadOIFitsFromCollectionAction() {
        super(className, actionName);
    }

    /**
     * Handle the action event
     * @param evt action event
     */
    @Override
    public void actionPerformed(final ActionEvent evt) {
        logger.debug("actionPerformed");

        final OIFitsCollectionManager ocm = OIFitsCollectionManager.getInstance();

        final File file = FileChooser.showOpenFileChooser("Select OIFits Explorer Collection", null, mimeType, null);

        // If a file was defined (No cancel in the dialog)
        if (file != null) {
            loadOIFitsCollectionFromFile(file, ocm, true);
        }
    }
}
