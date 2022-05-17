/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.jmmc.oiexplorer.gui.action;

import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.jmcs.gui.component.MessagePane;
import java.awt.event.ActionEvent;
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

    /**
     * Action to to remove from collection all OIFitsFiles that are concerned by the current SubsetDefinition. This
     * Action is used in ApplicationData.
     */
    public final static String ACTION_CURRENT_SUBSET_DEF = "currentSubsetDefinition";

    /**
     * The action field is dynamic to permit several different behaviour. This can not be done by the "actionCommand" in
     * the event, because we need to access the behaviours from ActionRegistrar.
     */
    private final String action;

    public RemoveAction(final String action) {
        super(className, action);
        this.action = action;
    }

    /**
     * Handle the action event
     *
     * @param evt action event
     */
    @Override
    public void actionPerformed(final ActionEvent evt) {
        MessagePane.showMessage(action);
    }

}
