/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.gui.action;

import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.oiexplorer.OIFitsExplorer;
import fr.jmmc.oiexplorer.gui.OIFitsFileListPanel;
import java.awt.event.ActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Remove Action. See the list of available actions as String constants. For now, there is only one to remove from
 * collection all OIFitsFiles that are concerned by the current SubsetDefinition.
 */
public class RemoveOIFitsAction extends RegisteredAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;

    /**
     * Class name. This name is used to register to the ActionRegistrar
     */
    public final static String className = RemoveOIFitsAction.class.getName();

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(className);

    /** Action name. This name is used to register to the ActionRegistrar */
    public final static String actionName = "remove";

    public RemoveOIFitsAction() {
        super(className, actionName);
    }

    /**
     * Handle the action event
     *
     * @param evt action event
     */
    @Override
    public void actionPerformed(final ActionEvent evt) {
        OIFitsFileListPanel oiFitsFileListPanel = OIFitsExplorer.getInstance().getMainPanel().getOIFitsFileListPanel();
        oiFitsFileListPanel.removeSelectedOIFitsFiles();
    }

}
