/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.gui;

import fr.jmmc.jmcs.gui.component.GenericListModel;
import fr.jmmc.oiexplorer.core.gui.PlotPanelEditor;
import fr.jmmc.oiexplorer.core.gui.Vis2Panel;
import fr.jmmc.oiexplorer.core.model.OIFitsCollection;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionEventListener;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oiexplorer.core.model.event.GenericEvent;
import fr.jmmc.oiexplorer.core.model.event.OIFitsCollectionEvent;
import fr.jmmc.oiexplorer.core.model.event.OIFitsCollectionEventType;
import fr.jmmc.oiexplorer.core.model.event.SubsetDefinitionEvent;
import fr.jmmc.oiexplorer.core.model.oi.SubsetDefinition;
import fr.jmmc.oitools.model.OIFitsFile;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main container of OIFits Explorer App
 * @author mella
 */
public final class MainPanel extends javax.swing.JPanel implements OIFitsCollectionEventListener {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(MainPanel.class);

    /** Creates new form MainPanel */
    public MainPanel() {
        OIFitsCollectionManager.getInstance().getOiFitsCollectionEventNotifier().register(this);
        OIFitsCollectionManager.getInstance().getSubsetDefinitionEventNotifier().register(this);

        initComponents();
        postInit();
    }

    /**
     * This method is useful to set the models and specific features of initialized swing components :
     */
    private void postInit() {
        this.jListOIFitsFiles.setCellRenderer(new OIFitsListRenderer());

        this.plotPanel.init();
    }

    /**
     * Refresh the list of OIfits files
     */
    private void updateOIFitsList(final OIFitsCollection oiFitsCollection) {
        final Object oldValue = this.jListOIFitsFiles.getSelectedValue();

        this.jListOIFitsFiles.setModel(new GenericListModel<OIFitsFile>(oiFitsCollection.getOIFitsFiles()));

        // restore previous selected item :
        this.jListOIFitsFiles.setSelectedValue(oldValue, true);
    }

    /**
     * Refresh the HTML view
     * @param subsetDefinition subset definition
     */
    private void updateHtmlView(final SubsetDefinition subsetDefinition) {
        this.oIFitsHtmlPanel.updateOIFits(subsetDefinition.getOIFitsSubset());
    }

    /**
     * Handle the given OIFits collection event
     * @param event OIFits collection event
     */
    @Override
    public void onProcess(final GenericEvent<OIFitsCollectionEventType> event) {
        logger.debug("Received event to process {}", event);

        switch (event.getType()) {
            case CHANGED:
                updateOIFitsList(((OIFitsCollectionEvent) event).getOIFitsCollection());
                break;
            case SUBSET_CHANGED:
                updateHtmlView(((SubsetDefinitionEvent) event).getSubsetDefinition());
                break;
            default:
        }
    }

    /** 
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mainSplitPane = new javax.swing.JSplitPane();
        jScrollPaneList = new javax.swing.JScrollPane();
        jListOIFitsFiles = new javax.swing.JList();
        rightSplitPane = new javax.swing.JSplitPane();
        dataTreePanel = new fr.jmmc.oiexplorer.gui.DataTreePanel();
        jTabbedPaneViews = new javax.swing.JTabbedPane();
        plotPanel = new fr.jmmc.oiexplorer.core.gui.PlotPanelEditor();
        oIFitsHtmlPanel = new fr.jmmc.oiexplorer.core.gui.OIFitsHtmlPanel();

        setLayout(new java.awt.GridBagLayout());

        mainSplitPane.setResizeWeight(0.1);

        jListOIFitsFiles.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPaneList.setViewportView(jListOIFitsFiles);

        mainSplitPane.setLeftComponent(jScrollPaneList);

        rightSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        rightSplitPane.setResizeWeight(0.2);
        rightSplitPane.setTopComponent(dataTreePanel);

        jTabbedPaneViews.addTab("plots", plotPanel);
        jTabbedPaneViews.addTab("data", oIFitsHtmlPanel);

        rightSplitPane.setBottomComponent(jTabbedPaneViews);
        jTabbedPaneViews.getAccessibleContext().setAccessibleName("plots");
        jTabbedPaneViews.getAccessibleContext().setAccessibleDescription("");

        mainSplitPane.setRightComponent(rightSplitPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(mainSplitPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private fr.jmmc.oiexplorer.gui.DataTreePanel dataTreePanel;
    private javax.swing.JList jListOIFitsFiles;
    private javax.swing.JScrollPane jScrollPaneList;
    private javax.swing.JTabbedPane jTabbedPaneViews;
    private javax.swing.JSplitPane mainSplitPane;
    private fr.jmmc.oiexplorer.core.gui.OIFitsHtmlPanel oIFitsHtmlPanel;
    private fr.jmmc.oiexplorer.core.gui.PlotPanelEditor plotPanel;
    private javax.swing.JSplitPane rightSplitPane;
    // End of variables declaration//GEN-END:variables

    public DataTreePanel getDataTreePanel() {
        return dataTreePanel;
    }

    public PlotPanelEditor getPlotPanel() {
        return plotPanel;
    }

    public Vis2Panel getVis2PlotPanel() {
        return plotPanel.getPlotPanel();
    }

    /**
     * This custom renderer defines the target icon (calibrator or science) and use the target Name
     * @author bourgesl
     */
    private static final class OIFitsListRenderer extends DefaultListCellRenderer {

        /** default serial UID for Serializable interface */
        private static final long serialVersionUID = 1;

        /**
         * Public constructor
         */
        private OIFitsListRenderer() {
            super();
        }

        /**
         * Return a component that has been configured to display the specified
         * value. That component's <code>paint</code> method is then called to
         * "render" the cell.  If it is necessary to compute the dimensions
         * of a list because the list cells do not have a fixed size, this method
         * is called to generate a component on which <code>getPreferredSize</code>
         * can be invoked.
         *
         * @param list The JList we're painting.
         * @param value The value returned by list.getModel().getElementAt(index).
         * @param index The cells index.
         * @param isSelected True if the specified cell was selected.
         * @param cellHasFocus True if the specified cell has the focus.
         * @return A component whose paint() method will render the specified value.
         *
         * @see JList
         * @see ListSelectionModel
         * @see ListModel
         */
        @Override
        public Component getListCellRendererComponent(
                final JList list,
                final Object value,
                final int index,
                final boolean isSelected,
                final boolean cellHasFocus) {

            final String val;
            if (value == null) {
                val = null;
            } else if (value instanceof OIFitsFile) {
                val = ((OIFitsFile) value).getName(); // or getAbsoluteFilePath()
            } else {
                val = value.toString();
            }

            super.getListCellRendererComponent(
                    list, val, index,
                    isSelected, cellHasFocus);

            return this;
        }
    }
}
