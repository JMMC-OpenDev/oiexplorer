/** *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ***************************************************************************** */
package fr.jmmc.oiexplorer.gui;

import fr.jmmc.oiexplorer.core.gui.GenericFilterEditor;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEvent;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventListener;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventType;
import fr.jmmc.oiexplorer.core.model.oi.GenericFilter;
import fr.jmmc.oiexplorer.core.model.oi.SubsetDefinition;
import fr.jmmc.oitools.model.DataModel;
import fr.jmmc.oitools.processing.SelectorResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericFiltersPanel extends javax.swing.JPanel
        implements OIFitsCollectionManagerEventListener, ChangeListener {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(GenericFiltersPanel.class);

    /** OIFitsCollectionManager singleton reference */
    private final static OIFitsCollectionManager OCM = OIFitsCollectionManager.getInstance();

    /** List of GenericFilterEditor for each GenericFilter in the current SubsetDefinition */
    private final List<GenericFilterEditor> genericFilterEditorList;

    /** when true, disables handler of Changes set on GenericFilterEditors. Used in updateGUI(). */
    private boolean updatingGUI = false;

    /** Creates new form GenericFiltersPanel */
    public GenericFiltersPanel() {
        logger.debug("creates GenericFiltersPanel");
        initComponents();
        genericFilterEditorList = new ArrayList<>(1);
        OCM.getSubsetDefinitionChangedEventNotifier().register(this);
    }

    /** Removes listeners references */
    @Override
    public void dispose() {
        genericFilterEditorList.forEach(GenericFilterEditor::dispose);
        OCM.unbind(this);
    }

    /** Updates OIExplorer Model from the GUI values. Here it updates the generic filters. Called when there is a change
     * on GenericFilterEditors. */
    private void updateModel() {
        logger.debug("updates Model");

        final SubsetDefinition subsetDefinitionCopy = OCM.getCurrentSubsetDefinition();
        subsetDefinitionCopy.getGenericFilters().clear();

        // take every genericFilter value from the genericFilterEditors and put it in a SubsetDefinition copy
        for (final GenericFilterEditor genericFilterEditor : this.genericFilterEditorList) {
            final GenericFilter genericFilterCopy = (GenericFilter) genericFilterEditor.getGenericFilter().clone();
            subsetDefinitionCopy.getGenericFilters().add(genericFilterCopy);
        }

        OCM.updateSubsetDefinition(this, subsetDefinitionCopy);
    }

    /** Updates GenericFilterEditors from the OIExplorer Model values. Called when a SUBSET_CHANGED event is received */
    private void updateGUI() {
        logger.debug("updates GUI");

        try {
            updatingGUI = true;

            // we clear and re-create GenericFilterEditors
            this.removeAll();
            genericFilterEditorList.forEach(GenericFilterEditor::dispose);
            genericFilterEditorList.clear();

            final SubsetDefinition subsetDefinitionCopy = OCM.getCurrentSubsetDefinition();

            if (subsetDefinitionCopy != null) {
                // TODO begin: temporary static value, to remove later
                boolean modified = false;
                if (subsetDefinitionCopy.getGenericFilters().isEmpty()) {
                    modified = true;
                    subsetDefinitionCopy.getGenericFilters().add(new GenericFilter());
                }
                // TODO end

                for (GenericFilter genericFilter : subsetDefinitionCopy.getGenericFilters()) {

                    final GenericFilterEditor genericFilterEditor = new GenericFilterEditor();
                    genericFilterEditor.setGenericFilter(subsetDefinitionCopy.getSelectorResult(), genericFilter);
                    genericFilterEditor.addChangeListener(this);

                    genericFilterEditorList.add(genericFilterEditor);
                    this.add(genericFilterEditor);
                }

                // TODO begin: remove when the genericFilter does not receive temporary static value anymore
                if (modified) {
                    OCM.updateSubsetDefinition(this, subsetDefinitionCopy);
                }
                // TODO end
            }

            revalidate();
        } finally {
            updatingGUI = false;
        }
    }

    /** Listener on changes on GenericFilterEditors.
     *
     * @param ce Event
     */
    @Override
    public void stateChanged(ChangeEvent ce) {
        if (!updatingGUI) {
            updateModel();
        }
    }

    /*
     * OIFitsCollectionManagerEventListener implementation
     */
    /**
     * Return the optional subject id i.e. related object id that this listener accepts
     *
     * @param type event type
     * @return subject id (null means accept any event) or DISCARDED_SUBJECT_ID to discard event
     */
    @Override
    public String getSubjectId(final OIFitsCollectionManagerEventType type) {
        // accept all
        return null;
    }

    @Override
    public void onProcess(OIFitsCollectionManagerEvent event) {
        logger.debug("onProcess begin {}", event);

        switch (event.getType()) {
            case SUBSET_CHANGED:
                updateGUI();
                break;
            default:
        }
        logger.debug("onProcess done {}", event);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
