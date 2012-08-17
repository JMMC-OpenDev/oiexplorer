/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.gui;

import fr.jmmc.jmcs.gui.component.GenericJTree;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.oiexplorer.OIFitsExplorerGui;
import fr.jmmc.oiexplorer.core.gui.OIFitsHtmlPanel;
import fr.jmmc.oiexplorer.core.gui.Vis2Panel;
import fr.jmmc.oiexplorer.core.model.OIFitsCollection;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oiexplorer.core.model.TargetUID;
import fr.jmmc.oiexplorer.core.model.event.OIFitsCollectionEvent;
import fr.jmmc.oiexplorer.core.model.event.OIFitsCollectionListener;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OITable;
import java.util.Map;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This panel contains the data tree
 * @author mella
 */
public class DataTreePanel extends javax.swing.JPanel implements OIFitsCollectionListener, TreeSelectionListener {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(DataTreePanel.class);

    /* members */
    /** oiFits collection in use */
    private OIFitsCollection oiFitsCollection;
    /** data tree */
    private GenericJTree<Object> dataTree;

    /** Creates new form DataTreePanel */
    public DataTreePanel() {
        initComponents();
        postInit();
    }

    private void postInit() {

        // dataTree contains TargetUID or OITable objects:
        dataTree = new GenericJTree<Object>(Object.class) {
            /** default serial UID for Serializable interface */
            private static final long serialVersionUID = 1;

            @Override
            protected String convertUserObjectToString(final Object userObject) {
                if (userObject instanceof TargetUID) {
                    return ((TargetUID) userObject).getTarget();
                }
                if (userObject instanceof OITable) {
                    return ((OITable) userObject).toString();
                }
                return toString(userObject);
            }
        };

        // tree selection listener :
        dataTree.addTreeSelectionListener(this);

        genericTreePanel.add(dataTree);

        OIFitsCollectionManager.getInstance().register(this);
    }

    public void onProcess(final OIFitsCollectionEvent event) {
        logger.debug("Received event to process {}", event);
        
        generateTree(event.getOIFitsCollection());

        if (event.getOIFitsCollection().isEmpty()) {
            processTargetSelection(null);
        } else {
            // select first target :
            dataTree.selectFirstChildNode(dataTree.getRootNode());
        }
    }

    /**
     * Generate the tree from the current edited list of targets
     * @param oifitsCollection OIFitsCollection to process
     */
    private void generateTree(final OIFitsCollection oifitsCollection) {

        this.oiFitsCollection = oifitsCollection;

        final DefaultMutableTreeNode rootNode = dataTree.getRootNode();
        rootNode.setUserObject("Targets");
        rootNode.removeAllChildren();

        final Map<TargetUID, OIFitsFile> oiFitsPerTarget = oifitsCollection.getOiFitsPerTarget();

        for (Map.Entry<TargetUID, OIFitsFile> entry : oiFitsPerTarget.entrySet()) {
            final TargetUID target = entry.getKey();
            final DefaultMutableTreeNode targetTreeNode = dataTree.addNode(rootNode, target);

            final OIFitsFile dataForTarget = entry.getValue();
            for (OITable table : dataForTarget.getOiTables()) {
                dataTree.addNode(targetTreeNode, table);
            }
        }
        // fire node structure changed :
        dataTree.fireNodeChanged(rootNode);
    }

    /**
     * Process the tree selection events
     * @param e tree selection event
     */
    @Override
    public void valueChanged(final TreeSelectionEvent e) {
        final DefaultMutableTreeNode currentNode = dataTree.getLastSelectedNode();

        if (currentNode != null) {
            // Use invokeLater to selection change issues with editors :
            SwingUtils.invokeLaterEDT(new Runnable() {
                /**
                 * Update tree selection
                 */
                @Override
                public void run() {
                    // Check if it is the root node :
                    final DefaultMutableTreeNode rootNode = dataTree.getRootNode();
                    if (currentNode == rootNode) {
                        dataTree.selectFirstChildNode(rootNode);
                        return;
                    }

                    /* retrieve the node that was selected */
                    final Object userObject = currentNode.getUserObject();

                    if (userObject instanceof TargetUID) {
                        final TargetUID target = (TargetUID) userObject;

                        processTargetSelection(target);
                    } else if (userObject instanceof OITable) {
                        final OITable oiTable = (OITable) userObject;

                        final DefaultMutableTreeNode parentNode = dataTree.getParentNode(currentNode);

                        if (parentNode != null && parentNode.getUserObject() instanceof TargetUID) {
                            final TargetUID parentTarget = (TargetUID) parentNode.getUserObject();

                            processTableSelection(parentTarget, oiTable);
                        }
                    }
                }
            });
        }
    }

    /**
     * Update the UI when a target is selected in the data tree
     * @param target selected target
     */
    private void processTargetSelection(final TargetUID target) {
        logger.debug("processTargetSelection: {}", target);

        // Get OIFitsFile structure for this target:
        final OIFitsFile dataForTarget = this.oiFitsCollection.getOiDataList(target);

        // Get main container
        final MainPanel mainPanel = OIFitsExplorerGui.getInstance().getMainPanel();
                
        // Update Html output:        
        final OIFitsHtmlPanel oiFitsHtmlPanel = mainPanel.getOIFitsHtmlPanel();

        // update Html representation:
        oiFitsHtmlPanel.updateOIFits(dataForTarget);

        // Update plots:
        final Vis2Panel vis2Panel = mainPanel.getVis2PlotPanel();

        vis2Panel.plot(target, dataForTarget);     
        
        // Update plot selector:
        final PlotSelectorPanel plotSelectorPanel = mainPanel.getPlotSelectorPanel();
        plotSelectorPanel.updateOIFits(target, dataForTarget);
        
    }

    /**
     * Update the UI when a OITable is selected in the data tree
     * @param target selected target
     * @param oiTable selected table
     */
    private void processTableSelection(final TargetUID target, final OITable oiTable) {
        logger.debug("processTableSelection: {}", oiTable);

        // Get main container
        final MainPanel mainPanel = OIFitsExplorerGui.getInstance().getMainPanel();
                
        // Update Html output:
        final OIFitsHtmlPanel oiFitsHtmlPanel = mainPanel.getOIFitsHtmlPanel();

        // update Html representation:
        oiFitsHtmlPanel.updateOIFits(oiTable);

        // Update plots:
        final Vis2Panel vis2Panel = mainPanel.getVis2PlotPanel();

        final OIFitsFile oiFitsFile = new OIFitsFile();
        oiFitsFile.addOiTable(oiTable);

        vis2Panel.plot(target, oiFitsFile);
        
        // Update plot selector:
        final PlotSelectorPanel plotSelectorPanel = mainPanel.getPlotSelectorPanel();
        plotSelectorPanel.updateOIFits(target, oiFitsFile);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane = new javax.swing.JScrollPane();
        genericTreePanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        genericTreePanel.setLayout(new java.awt.BorderLayout());
        jScrollPane.setViewportView(genericTreePanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel genericTreePanel;
    private javax.swing.JScrollPane jScrollPane;
    // End of variables declaration//GEN-END:variables
}
