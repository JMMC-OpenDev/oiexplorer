/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.gui;

import com.jidesoft.swing.JideTabbedPane;
import fr.jmmc.jmcs.gui.component.GenericListModel;
import fr.jmmc.oiexplorer.core.gui.PlotPanelEditor;
import fr.jmmc.oiexplorer.core.gui.Vis2Panel;
import fr.jmmc.oiexplorer.core.gui.action.ExportPDFAction;
import fr.jmmc.oiexplorer.core.model.OIFitsCollection;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionEventListener;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oiexplorer.core.model.event.GenericEvent;
import fr.jmmc.oiexplorer.core.model.event.OIFitsCollectionEvent;
import fr.jmmc.oiexplorer.core.model.event.OIFitsCollectionEventType;
import fr.jmmc.oiexplorer.core.model.event.SubsetDefinitionEvent;
import fr.jmmc.oiexplorer.core.model.oi.Plot;
import fr.jmmc.oiexplorer.core.model.oi.SubsetDefinition;
import fr.jmmc.oiexplorer.gui.action.OIFitsExplorerExportPDFAction;
import fr.jmmc.oitools.model.OIFitsFile;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
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
    /** panel counter  to display title without given name */
    private static int panelCounter = 0;
    /** OIFitsCollectionManager singleton reference */
    private final OIFitsCollectionManager ocm = OIFitsCollectionManager.getInstance();

    /** Creates new form MainPanel */
    public MainPanel() {
        ocm.getOiFitsCollectionEventNotifier().register(this);
        ocm.getSubsetDefinitionEventNotifier().register(this);

        // Build GUI
        initComponents();
        
        // Finish init
        postInit();
    }

    /**
     * This method is useful to set the models and specific features of initialized swing components :
     */
    private void postInit() {
        // Add renderer to get short oifits filenames
        this.jListOIFitsFiles.setCellRenderer(new OIFitsListRenderer());

        // Link removeCurrentView to the tabpane close button
        this.tabbedPane.setCloseAction(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                removeCurrentView();
            }
        });
        /*
         this.tabbedPane.setTabShape(JideTabbedPane.SHAPE_ROUNDED_VSNET);
         this.tabbedPane.setTabResizeMode(JideTabbedPane.RESIZE_MODE_NONE);
         this.tabbedPane.setColorTheme(JideTabbedPane.COLOR_THEME_VSNET);
         this.tabbedPane.setTabEditingAllowed(true);
         // TODO : setTabEditingValidator(...)
         this.tabbedPane.setTabLeadingComponent(_plusButton);
         */

        // Build toolBar
        toolBar.add(OIFitsExplorerExportPDFAction.getInstance());

    }

    /**
     * Synchronize tab and Views of OIFitsCollectionManager
     * @see #onProcess(fr.jmmc.oiexplorer.core.model.event.GenericEvent) 
     */
    private void updateTabContent() {
        // TODO handle case where tab where already present
        logger.warn("prepareTabContent() for collection manager plots : {}", ocm.getUserCollection().getPlots());
        for (Plot plot : ocm.getUserCollection().getPlots()) {            
            final String plotId = plot.getName();                        
            PlotView p = new PlotView(plotId);
            addPanel(p, plotId);
        }
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
     * Handle the given OIFits collection event
     * @param event OIFits collection event
     */
    @Override
    public void onProcess(final GenericEvent<OIFitsCollectionEventType> event) {
        logger.debug("Received event to process {}", event);

        switch (event.getType()) {
            case CHANGED:
                // Update info for oifits file list
                updateOIFitsList(((OIFitsCollectionEvent) event).getOIFitsCollection());
                // Update tabpane content
                updateTabContent();
                break;
            case SUBSET_CHANGED:
                // now should be done in PlotView
                // updateHtmlView(((SubsetDefinitionEvent) event).getSubsetDefinition());
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
        dataSplitPane = new javax.swing.JSplitPane();
        dataTreePanel = new fr.jmmc.oiexplorer.gui.DataTreePanel();
        dataSplitTopPanel = new javax.swing.JPanel();
        toolBar = new javax.swing.JToolBar();
        jScrollPaneList = new javax.swing.JScrollPane();
        jListOIFitsFiles = new javax.swing.JList();
        tabbedPane = new com.jidesoft.swing.JideTabbedPane();

        setLayout(new java.awt.GridBagLayout());

        mainSplitPane.setResizeWeight(0.2);

        dataSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        dataSplitPane.setResizeWeight(0.3);
        dataSplitPane.setRightComponent(dataTreePanel);

        dataSplitTopPanel.setLayout(new java.awt.GridBagLayout());

        toolBar.setRollover(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        dataSplitTopPanel.add(toolBar, gridBagConstraints);

        jListOIFitsFiles.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPaneList.setViewportView(jListOIFitsFiles);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        dataSplitTopPanel.add(jScrollPaneList, gridBagConstraints);

        dataSplitPane.setLeftComponent(dataSplitTopPanel);

        mainSplitPane.setLeftComponent(dataSplitPane);

        tabbedPane.setBoldActiveTab(true);
        tabbedPane.setShowCloseButton(true);
        tabbedPane.setShowCloseButtonOnSelectedTab(true);
        tabbedPane.setShowCloseButtonOnTab(true);
        mainSplitPane.setRightComponent(tabbedPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(mainSplitPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane dataSplitPane;
    private javax.swing.JPanel dataSplitTopPanel;
    private fr.jmmc.oiexplorer.gui.DataTreePanel dataTreePanel;
    private javax.swing.JList jListOIFitsFiles;
    private javax.swing.JScrollPane jScrollPaneList;
    private javax.swing.JSplitPane mainSplitPane;
    private com.jidesoft.swing.JideTabbedPane tabbedPane;
    private javax.swing.JToolBar toolBar;
    // End of variables declaration//GEN-END:variables

    public DataTreePanel getDataTreePanel() {
        return dataTreePanel;
    }

    /**
     * Return the current plot panel
     * @return main panel
     */
    public PlotView getCurrentPanel() {
        return (PlotView) tabbedPane.getSelectedComponent();
    }

    public void addPanel(final JPanel panel, final String panelName) {

        final String name;
        if ((panelName != null) && (panelName.length() > 0)) {
            name = panelName;
        } else {
            name = panel.getClass().getSimpleName() + " " + panelCounter;
            panelCounter++;
        }

        // To correctly match deeper background color of inner tab panes
        panel.setOpaque(false);

        tabbedPane.add(name, panel);

        logger.debug("Added '{}' panel to PreferenceView tabbed pane.", name);
    }

    public void removeCurrentView() {
        logger.warn("removeCurrentView() TODO");
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
