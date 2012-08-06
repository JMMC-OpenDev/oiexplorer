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
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
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

    /** Class name. This name is used to register to the ActionRegistrar */
    public final static String className = LoadOIFitsAction.class.getName();
    /** Action name. This name is used to register to the ActionRegistrar */
    public final static String actionName = "loadOIFits";
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(className);
    /** AsproX MimeType */
    private final static MimeType mimeType = MimeType.OIFITS;

    /**
     * Public constructor that automatically register the action in RegisteredAction.
     */
    public LoadOIFitsAction() {
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

        File file = null;

        // If the action was automatically triggered from App launch
        if (evt.getSource() == ActionRegistrar.getInstance()) {
            file = new File(evt.getActionCommand());

            if (!file.exists() || !file.isFile()) {
                MessagePane.showErrorMessage("Could not load the file : " + file.getAbsolutePath());
                file = null;
            }

            if (file != null) {
                // update current directory for Observation settings:
                FileChooserPreferences.setCurrentDirectoryForMimeType(mimeType, file.getParent());
            }

        } else {

            file = FileChooser.showOpenFileChooser("Load observation settings", null, mimeType, null);

        }

        // If a file was defined (No cancel in the dialog)
        if (file != null) {
            StatusBar.show("file loaded : " + file.getName());

            // The file must be one oidata file (next line automatically unzip gz files)
            OIFitsChecker checker = new OIFitsChecker();
            try {
                OIFitsFile oifitsFile  = OIFitsLoader.loadOIFits(checker, file.getAbsolutePath());
                MessagePane.showMessage(checker.getCheckReport());
            } catch (MalformedURLException ex) {
                MessagePane.showErrorMessage("Could not load the file : " + file.getAbsolutePath(), ex);
            } catch (IOException ex) {
                MessagePane.showErrorMessage("Could not load the file : " + file.getAbsolutePath(), ex);
                java.util.logging.Logger.getLogger(LoadOIFitsAction.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FitsException ex) {
                MessagePane.showErrorMessage("Could not load the file : " + file.getAbsolutePath(), ex);
            }            
        }
    }
}
