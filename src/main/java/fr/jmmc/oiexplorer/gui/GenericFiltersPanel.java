/** *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ***************************************************************************** */
package fr.jmmc.oiexplorer.gui;

import fr.jmmc.oiexplorer.core.gui.GenericFilterEditor;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEvent;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventListener;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventType;
import fr.jmmc.oiexplorer.core.model.oi.DataType;
import fr.jmmc.oiexplorer.core.model.oi.GenericFilter;
import fr.jmmc.oiexplorer.core.model.oi.SubsetDefinition;
import fr.jmmc.oiexplorer.core.model.plot.Range;
import static fr.jmmc.oitools.OIFitsConstants.COLUMN_EFF_WAVE;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GenericFiltersPanel extends javax.swing.JPanel
        implements OIFitsCollectionManagerEventListener, ChangeListener, ActionListener {

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

        SubsetDefinition subsetDefinitionCopy = OCM.getCurrentSubsetDefinition();

        subsetDefinitionCopy.getGenericFilters().clear();

        // take every genericFilter value from the genericFilterEditors and put it in a SubsetDefinition copy
        for (GenericFilterEditor genericFilterEditor : this.genericFilterEditorList) {
            GenericFilter genericFilterCopy = (GenericFilter) genericFilterEditor.getGenericFilter().clone();
            subsetDefinitionCopy.getGenericFilters().add(genericFilterCopy);
        }

        OCM.updateSubsetDefinition(this, subsetDefinitionCopy);
    }

    /** Updates GenericFilterEditors from the OIExplorer Model values. Called when a SUBSET_CHANGED event is received */
    private void updateGUI() {
        logger.debug("updates GUI");

        try {
            updatingGUI = true;

            SubsetDefinition subsetDefinitionCopy = OCM.getCurrentSubsetDefinition();

            // we clear and re-create GenericFilterEditors
            jPanelGenericFilters.removeAll();
            genericFilterEditorList.forEach(GenericFilterEditor::dispose);
            genericFilterEditorList.clear();

            if (subsetDefinitionCopy != null) {

                boolean modified = false; // some generic filters values can be modified by GenericFilterEditor

                for (GenericFilter genericFilter : subsetDefinitionCopy.getGenericFilters()) {
                    final boolean oneModified = addGenericFilterEditor(genericFilter);
                    modified |= oneModified;
                }

                if (modified) { // if some generic filters have been modified, submit the changes to the model
                    updateModel();
                }
            }

            revalidate();
        }
        finally {
            updatingGUI = false;
        }
    }

    private boolean addGenericFilterEditor(final GenericFilter genericFilter) {

        JPanel newPanel = new JPanel();
        newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.X_AXIS));

        GenericFilterEditor newGenericFilterEditor = new GenericFilterEditor();
        newGenericFilterEditor.addChangeListener(this);
        final boolean modified = newGenericFilterEditor.setGenericFilter(genericFilter);
        genericFilterEditorList.add(newGenericFilterEditor);
        newPanel.add(newGenericFilterEditor);

        JButton delButton = new JButton("-");
        delButton.addActionListener(this);
        newPanel.add(delButton);

        jPanelGenericFilters.add(newPanel, 0);

        return modified;
    }

    private void handlerAddGenericFilter() {
        if (!updatingGUI) {

            GenericFilter newGenericFilter = new GenericFilter();
            newGenericFilter.setEnabled(true);
            newGenericFilter.setColumnName(COLUMN_EFF_WAVE);
            newGenericFilter.setDataType(DataType.NUMERIC);
            final Range range = new Range();
            range.setMin(Double.NaN);
            range.setMax(Double.NaN);
            newGenericFilter.getAcceptedRanges().add(range);

            addGenericFilterEditor(newGenericFilter);

            revalidate();

            updateModel();
        }
    }

    private void handlerDelGenericFilter(final JButton delButton) {
        if (!updatingGUI) {
            try {
                delButton.removeActionListener(this);
                JPanel panel = (JPanel) delButton.getParent();
                GenericFilterEditor genericFilterEditorToDel = (GenericFilterEditor) panel.getComponent(0);
                genericFilterEditorList.remove(genericFilterEditorToDel);
                jPanelGenericFilters.remove(panel);
                revalidate();
                repaint();
                updateModel();
            }
            catch (ClassCastException e) {
                logger.error("Cannot find GenericFilterEditor panel to remove.");
            }
        }
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() instanceof JButton) {
            handlerDelGenericFilter((JButton) evt.getSource());
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
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanelGenericFilters = new javax.swing.JPanel();
        jButtonAddGenericFilter = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Generic Filters"));
        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[] {0, 10, 0};
        layout.rowHeights = new int[] {0, 7, 0};
        setLayout(layout);

        jPanelGenericFilters.setLayout(new javax.swing.BoxLayout(jPanelGenericFilters, javax.swing.BoxLayout.Y_AXIS));
        jScrollPane1.setViewportView(jPanelGenericFilters);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.9;
        gridBagConstraints.weighty = 0.9;
        add(jScrollPane1, gridBagConstraints);

        jButtonAddGenericFilter.setText("+");
        jButtonAddGenericFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddGenericFilterActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jButtonAddGenericFilter, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonAddGenericFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddGenericFilterActionPerformed
        handlerAddGenericFilter();
    }//GEN-LAST:event_jButtonAddGenericFilterActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddGenericFilter;
    private javax.swing.JPanel jPanelGenericFilters;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

}
