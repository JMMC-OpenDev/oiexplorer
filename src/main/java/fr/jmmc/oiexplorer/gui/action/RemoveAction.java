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
        if (action == null) {
            LOGGER.error("action is null.");
            return;
        }

        switch (action) {
            case ACTION_CURRENT_SUBSET_DEF:
                actionCurrentSubsetDefinition();
                break;
            default:
                LOGGER.error("unknown action.");
                break;
        }
    }

    private void actionCurrentSubsetDefinition() {
        OIFitsFileListPanel oiFitsFileListPanel = OIFitsExplorer.getInstance().getMainPanel().getOIFitsFileListPanel();

        List<OIFitsFile> selecteds = oiFitsFileListPanel.getSelectedOIFitsFiles();

        if (selecteds.isEmpty()) {
            MessagePane.showMessage("There is no file to delete.");
        } else {

            StringBuilder message = new StringBuilder(selecteds.size() * 200);

            message.append("Do you confirm to remove the following OIFits file(s):\n");
            for (OIFitsFile file : selecteds) {
                message.append("\n").append(file.getFileName()).append("\n(");
                message.append(file.getAcceptedTargetIds().length).append(" target(s), ");
                message.append(file.getNbOiVis()).append(" OI_VIS, ");
                message.append(file.getNbOiVis2()).append(" OI_VIS2, ");
                message.append(file.getNbOiT3()).append(" OI_T3)\n");
            }

            final boolean confirm = MessagePane.showConfirmMessage(message.toString());

            if (confirm) {
                oiFitsFileListPanel.removeSelectedOIFitsFiles();
            }
        }
    }

}
