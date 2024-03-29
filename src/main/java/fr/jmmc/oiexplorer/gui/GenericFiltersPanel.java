/** *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ***************************************************************************** */
package fr.jmmc.oiexplorer.gui;

import fr.jmmc.jmcs.gui.component.GenericListModel;
import fr.jmmc.oiexplorer.core.gui.GenericFilterEditor;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEvent;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventListener;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventType;
import fr.jmmc.oiexplorer.core.model.oi.DataType;
import fr.jmmc.oiexplorer.core.model.oi.GenericFilter;
import fr.jmmc.oiexplorer.core.model.oi.Identifiable;
import fr.jmmc.oiexplorer.core.model.oi.SubsetDefinition;
import fr.jmmc.oiexplorer.core.model.plot.Range;
import fr.jmmc.oitools.OIFitsProcessor;
import fr.jmmc.oitools.processing.Selector;
import fr.jmmc.oitools.processing.SelectorResult;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.jmmc.jmcs.gui.util.ResourceImage;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.gui.util.SwingUtils.ComponentSizeVariant;
import fr.jmmc.oitools.processing.BaseSelectorResult;
import javax.swing.SwingUtilities;

public final class GenericFiltersPanel extends javax.swing.JPanel
        implements OIFitsCollectionManagerEventListener, ChangeListener, ActionListener {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(GenericFiltersPanel.class);

    /** OIFitsCollectionManager singleton reference */
    private final static OIFitsCollectionManager ocm = OIFitsCollectionManager.getInstance();

    private final static int IDX_DEL_BUTTON = 0;
    private final static int IDX_FILTER_EDITOR = 1;

    /* members */
    /** List of GenericFilterEditor for each GenericFilter in the current SubsetDefinition */
    private final transient List<GenericFilterEditor> genericFilterEditorList = new ArrayList<>(1);

    /** List of available filter column names */
    private final transient GenericListModel<String> nameComboBoxModel;

    /** when true, disables handler of Changes set on GenericFilterEditors. Used in updateGUI(). */
    private boolean updatingGUI = false;

    /** Creates new form GenericFiltersPanel */
    public GenericFiltersPanel() {
        initComponents();
        this.nameComboBoxModel = new GenericListModel<String>(new ArrayList<String>(25), true);
        postInit();
    }

    /**
     * This method is useful to set the models and specific features of initialized swing components :
     */
    private void postInit() {
        ocm.bindSubsetDefinitionChanged(this);

        jComboBoxColumnName.setModel(nameComboBoxModel);

        // use small variant:
        SwingUtils.adjustSize(this.jButtonAddGenericFilter, ComponentSizeVariant.small);
        SwingUtils.adjustSize(this.jToggleButtonShow, ComponentSizeVariant.small);

        // update button UI:
        SwingUtilities.updateComponentTreeUI(this);
    }

    /** Removes listeners references */
    @Override
    public void dispose() {
        genericFilterEditorList.forEach(GenericFilterEditor::dispose);
        ocm.unbind(this);
    }

    /** 
     * Updates SubsetDefinition from the GUI values to update the generic filters. 
     * Called when there is a change on GenericFilterEditors. */
    private void updateModel() {
        logger.debug("updateModel");

        final SubsetDefinition subsetCopy = ocm.getCurrentSubsetDefinition();
        subsetCopy.setHideFilteredData(!this.jToggleButtonShow.isSelected());

        final List<GenericFilter> filters = subsetCopy.getGenericFilters();
        filters.clear();

        // take every genericFilter value from the genericFilterEditors and put it in a SubsetDefinition copy
        for (final GenericFilterEditor genericFilterEditor : this.genericFilterEditorList) {
            final GenericFilter genericFilterCopy = Identifiable.clone(genericFilterEditor.getGenericFilter());
            filters.add(genericFilterCopy);
        }
        ocm.updateSubsetDefinition(this, subsetCopy);
    }

    /** 
     * Update GenericFilterEditors from the OIExplorer Model values. 
     * Called when a SUBSET_CHANGED event is received
     * @param isEventFromThisSource true if the event comes from this panel instance
     * @param subsetDefinition event's instance
     */
    private void updateGUI(final boolean isEventFromThisSource, final SubsetDefinition subsetDefinition) {
        logger.debug("updateGUI");

        final SelectorResult selectorResult = subsetDefinition.getSelectorResult();

        try {
            updatingGUI = true;

            if (!isEventFromThisSource) {
                jToggleButtonShow.setSelected(!subsetDefinition.isHideFilteredData());
                updateToggleButtonShowLabel();

                // clear and recreate column name choices:
                nameComboBoxModel.clear();
                nameComboBoxModel.addAll(Selector.SPECIAL_COLUMN_NAMES);

                // update column choices from SelectorResult:
                nameComboBoxModel.addAll(getDistinctNumericalColumnNames(selectorResult));

                if (jComboBoxColumnName.getSelectedIndex() == -1) {
                    jComboBoxColumnName.setSelectedIndex(0);
                }

                // clear and re-create GenericFilterEditors:
                jPanelGenericFilters.removeAll();
                genericFilterEditorList.forEach(GenericFilterEditor::dispose);
                genericFilterEditorList.clear();

                for (GenericFilter genericFilter : subsetDefinition.getGenericFilters()) {
                    addGenericFilterEditor(Identifiable.clone(genericFilter));
                }

                revalidate();
            }

            // Always update CLI args:
            final String cliArgs = (selectorResult != null)
                    ? OIFitsProcessor.generateCLIargs(selectorResult.getSelector()) : "(no matching data)";
            jTextAreaCLI.setText(cliArgs);

        } finally {
            updatingGUI = false;
        }
    }

    /** Adds a GenericFilterEditor to the Panel, along with a delete button */
    private void addGenericFilterEditor(final GenericFilter genericFilter) {
        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints gridBagConstraints = new GridBagConstraints(); // gridBagConstraints

        final JButton delButton = new JButton();
        delButton.setIcon(ResourceImage.LIST_DEL.icon());
        delButton.setToolTipText("Remove this filter");
        delButton.addActionListener(this);

        // use small variant:
        SwingUtils.adjustSize(delButton, ComponentSizeVariant.small);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.weightx = 0;
        gridBagConstraints.insets = new Insets(0, 2, 0, 4);
        panel.add(delButton, gridBagConstraints, IDX_DEL_BUTTON);

        final GenericFilterEditor newGenericFilterEditor = new GenericFilterEditor();
        newGenericFilterEditor.addChangeListener(this);
        newGenericFilterEditor.setGenericFilter(genericFilter);
        genericFilterEditorList.add(newGenericFilterEditor);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.9;
        gridBagConstraints.insets = SwingUtils.NO_MARGIN;

        panel.add(newGenericFilterEditor, gridBagConstraints, IDX_FILTER_EDITOR);

        jPanelGenericFilters.add(panel, 0);

        // update button UI:
        SwingUtilities.updateComponentTreeUI(panel);
    }

    /** Handler for the Add button, adds a new generic filter editor */
    private void handleAddGenericFilter() {
        if (!updatingGUI) {
            final GenericFilter newGenericFilter = new GenericFilter();
            newGenericFilter.setEnabled(true);

            String columnName = (String) jComboBoxColumnName.getSelectedItem();
            if (columnName == null) {
                columnName = Selector.FILTER_EFFWAVE;
            }
            newGenericFilter.setColumnName(columnName);

            final DataType dataType = Selector.isRangeFilter(columnName) ? DataType.NUMERIC : DataType.STRING;
            newGenericFilter.setDataType(dataType);

            switch (dataType) {
                case NUMERIC:
                    final fr.jmmc.oitools.model.range.Range oitoolsRange
                                                            = ocm.getOIFitsCollection().getColumnRange(columnName);
                    final Range range = new Range();
                    range.setMin(Double.isFinite(oitoolsRange.getMin()) ? oitoolsRange.getMin() : Double.NaN);
                    range.setMax(Double.isFinite(oitoolsRange.getMax()) ? oitoolsRange.getMax() : Double.NaN);
                    newGenericFilter.getAcceptedRanges().add(range);
                    break;
                case STRING:
                    final List<String> initValues = ocm.getOIFitsCollection().getDistinctValues(columnName);
                    if (initValues != null) {
                        newGenericFilter.getAcceptedValues().addAll(initValues);
                    }
                    break;
                default:
            }
            addGenericFilterEditor(newGenericFilter);

            revalidate();
            updateModel();
        }
    }

    /** Handler for the Del button. removes the generic filter editor associated to the button */
    private void handleDelGenericFilter(final JButton delButton) {
        if (!updatingGUI) {
            /* retrieve the panel containing the actioned button and the generic filter editor to delete */
            delButton.removeActionListener(this);
            final JPanel panel = (JPanel) delButton.getParent();
            final GenericFilterEditor genericFilterEditorToDel = (GenericFilterEditor) panel.getComponent(IDX_FILTER_EDITOR);
            genericFilterEditorList.remove(genericFilterEditorToDel);
            jPanelGenericFilters.remove(panel);

            revalidate();
            repaint();
            updateModel();
        }
    }

    /** Listener on actions on the del buttons.
     *
     * @param evt Event, the del button is the source
     */
    @Override
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() instanceof JButton) {
            handleDelGenericFilter((JButton) evt.getSource());
        }
    }

    /** Listener on changes on GenericFilterEditors
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
                updateGUI(event.getSources().contains(this), event.getSubsetDefinition());
                break;
            default:
        }
        logger.debug("onProcess done {}", event);
    }

    /**
     * Return the set of distinct columns available in tables of the given SelectorResult.
     *
     * @param selectorResult Selector result from plot's subset definition
     * @return a Set of Strings with every distinct column names
     */
    private static Set<String> getDistinctNumericalColumnNames(final SelectorResult selectorResult) {
        return BaseSelectorResult.getDataModel(selectorResult).getNumericalColumnNames();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanelToolbar = new javax.swing.JPanel();
        jButtonAddGenericFilter = new javax.swing.JButton();
        jComboBoxColumnName = new javax.swing.JComboBox<>();
        jToggleButtonShow = new javax.swing.JToggleButton();
        jScrollPaneFilters = new javax.swing.JScrollPane();
        jPanelGenericFilters = new javax.swing.JPanel();
        jScrollPaneCLI = new javax.swing.JScrollPane();
        jTextAreaCLI = new javax.swing.JTextArea();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Filters"));
        setLayout(new java.awt.GridBagLayout());

        jPanelToolbar.setLayout(new java.awt.GridBagLayout());

        jButtonAddGenericFilter.setIcon(fr.jmmc.jmcs.gui.util.ResourceImage.LIST_ADD.icon());
        jButtonAddGenericFilter.setToolTipText("add a new filter for the given column");
        jButtonAddGenericFilter.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButtonAddGenericFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddGenericFilterActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 4);
        jPanelToolbar.add(jButtonAddGenericFilter, gridBagConstraints);

        jComboBoxColumnName.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "COLUMN_NAME" }));
        jComboBoxColumnName.setToolTipText("Select the column name and click on [+] to add a new filter");
        jComboBoxColumnName.setPrototypeDisplayValue("XXXX");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.8;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 1, 2);
        jPanelToolbar.add(jComboBoxColumnName, gridBagConstraints);

        jToggleButtonShow.setText("Show");
        jToggleButtonShow.setToolTipText("Show or Hide filtered data");
        jToggleButtonShow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonShowActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 1, 1);
        jPanelToolbar.add(jToggleButtonShow, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jPanelToolbar, gridBagConstraints);

        jScrollPaneFilters.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jPanelGenericFilters.setLayout(new javax.swing.BoxLayout(jPanelGenericFilters, javax.swing.BoxLayout.Y_AXIS));
        jScrollPaneFilters.setViewportView(jPanelGenericFilters);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.8;
        add(jScrollPaneFilters, gridBagConstraints);

        jScrollPaneCLI.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextAreaCLI.setEditable(false);
        jTextAreaCLI.setColumns(20);
        jTextAreaCLI.setLineWrap(true);
        jTextAreaCLI.setRows(4);
        jTextAreaCLI.setTabSize(4);
        jTextAreaCLI.setToolTipText("OITools CLI arguments equivalent to current filters");
        jTextAreaCLI.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jScrollPaneCLI.setViewportView(jTextAreaCLI);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.1;
        add(jScrollPaneCLI, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonAddGenericFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddGenericFilterActionPerformed
        handleAddGenericFilter();
    }//GEN-LAST:event_jButtonAddGenericFilterActionPerformed

    private void jToggleButtonShowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonShowActionPerformed
        updateToggleButtonShowLabel();
        updateModel();
    }//GEN-LAST:event_jToggleButtonShowActionPerformed

    private void updateToggleButtonShowLabel() {
        this.jToggleButtonShow.setText(this.jToggleButtonShow.isSelected() ? "Hide" : "Show");
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddGenericFilter;
    private javax.swing.JComboBox<String> jComboBoxColumnName;
    private javax.swing.JPanel jPanelGenericFilters;
    private javax.swing.JPanel jPanelToolbar;
    private javax.swing.JScrollPane jScrollPaneCLI;
    private javax.swing.JScrollPane jScrollPaneFilters;
    private javax.swing.JTextArea jTextAreaCLI;
    private javax.swing.JToggleButton jToggleButtonShow;
    // End of variables declaration//GEN-END:variables

}
