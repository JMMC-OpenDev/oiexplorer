/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.jmmc.oiexplorer.gui.action;

import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.oiexplorer.OIFitsExplorer;
import fr.jmmc.oiexplorer.gui.OIFitsFileListPanel;
import fr.jmmc.oitools.model.OIFitsFile;
import java.awt.event.ActionEvent;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Remove Action. See the list of available actions as String constants. For now, there is only one to remove from
 * collection all OIFitsFiles that are concerned by the current SubsetDefinition.
 */
public class RemoveAction extends RegisteredAction {

    /**
     * Class name. This name is used to register to the ActionRegistrar
     */
    public final static String className = RemoveAction.class.getName();

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(className);

    /** Action name. This name is used to register to the ActionRegistrar */
    public final static String actionName = "remove";

    public RemoveAction() {
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
