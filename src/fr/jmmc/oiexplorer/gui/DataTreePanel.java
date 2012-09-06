/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.gui;

import fr.jmmc.jmcs.gui.component.GenericJTree;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.oiexplorer.core.model.OIFitsCollection;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionEventListener;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oiexplorer.core.model.event.GenericEvent;
import fr.jmmc.oiexplorer.core.model.event.OIFitsCollectionEvent;
import fr.jmmc.oiexplorer.core.model.event.OIFitsCollectionEventType;
import fr.jmmc.oiexplorer.core.model.oi.OIDataFile;
import fr.jmmc.oiexplorer.core.model.oi.SubsetDefinition;
import fr.jmmc.oiexplorer.core.model.oi.TableUID;
import fr.jmmc.oiexplorer.core.model.oi.TargetUID;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OITable;
import java.util.List;
import java.util.Map;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This panel contains the data tree
 * 
 * // TODO: add a subset selector and also support subset changed event !
 * // TODO: support both multiple file and table selection(s)
 * 
 * @author mella
 */
public final class DataTreePanel extends javax.swing.JPanel implements TreeSelectionListener,
                                                                       OIFitsCollectionEventListener {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(DataTreePanel.class);

    /* members */
    /** OIFitsCollectionManager singleton */
    private OIFitsCollectionManager ocm = OIFitsCollectionManager.getInstance();
    /** subset identifier */
    private String subsetId = OIFitsCollectionManager.CURRENT;
    /** subset to edit */
    private SubsetDefinition subsetDefinition = null;
    /** Swing data tree */
    private GenericJTree<Object> dataTree;

    /** Creates new form DataTreePanel */
    public DataTreePanel() {
        ocm.getOiFitsCollectionEventNotifier().register(this);
        
        initComponents();
        postInit();
    }

    /**
     * This method is useful to set the models and specific features of initialized swing components :
     */
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
    }

    /**
     * Update the data tree
     * @param oiFitsCollection OIFitsCollection to process
     */
    private void updateOIFitsCollection(final OIFitsCollection oiFitsCollection) {
        // force clean up ...
        setSubsetId(subsetId);

        generateTree(oiFitsCollection);

        // Restore subset selection (CURRENT):
        final SubsetDefinition subset = getSubsetDefinition();

        // ALWAYS select a target
        // TODO: the selection should be in sync with subset modification (load, external updates)
        if (oiFitsCollection.isEmpty()) {
            processTargetSelection(null);
        } else {
            boolean found = false;

            if (subset.getTarget() != null) {
                final DefaultMutableTreeNode targetTreeNode = dataTree.findTreeNode(subset.getTarget());

                if (targetTreeNode != null) {
                    DefaultMutableTreeNode tableTreeNode = null;

                    if (!subset.getTables().isEmpty()) {
                        // TODO: support multi selection:
                        final TableUID tableUID = subset.getTables().get(0);
                        final String filePath = tableUID.getFile().getFile();
                        final Integer extNb = tableUID.getExtNb();

                        for (int i = 0, size = targetTreeNode.getChildCount(); i < size; i++) {
                            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) targetTreeNode.getChildAt(i);
                            final OITable oiTable = (OITable) node.getUserObject();

                            if (filePath.equals(oiTable.getOIFitsFile().getAbsoluteFilePath())) {
                                if (extNb != null && extNb.intValue() == oiTable.getExtNb()) {
                                    tableTreeNode = node;
                                    break;
                                }
                            }
                        }

                    }
                    found = true;

                    if (tableTreeNode != null) {
                        dataTree.selectPath(new TreePath(tableTreeNode.getPath()));
                    } else {
                        dataTree.selectPath(new TreePath(targetTreeNode.getPath()));
                    }
                }
            }
            if (!found) {
                // select first target :
                dataTree.selectFirstChildNode(dataTree.getRootNode());
            }
        }
    }

    /**
     * Generate the tree from the current edited list of targets
     * @param oiFitsCollection OIFitsCollection to process
     */
    private void generateTree(final OIFitsCollection oiFitsCollection) {

        final DefaultMutableTreeNode rootNode = dataTree.getRootNode();
        rootNode.setUserObject("Targets");
        rootNode.removeAllChildren();

        final Map<TargetUID, OIFitsFile> oiFitsPerTarget = oiFitsCollection.getOiFitsPerTarget();

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

        // update subset definition (copy):
        final SubsetDefinition subset = getSubsetDefinition();
        subset.setTarget(target);
        subset.getTables().clear(); // means all

        // fire subset changed event:
        ocm.updateSubsetDefinition(this, subset);
    }

    /**
     * Update the UI when a OITable is selected in the data tree
     * @param target selected target
     * @param oiTable selected table
     */
    private void processTableSelection(final TargetUID target, final OITable oiTable) {
        logger.debug("processTableSelection: {}", oiTable);

        // update subset definition (copy):
        final SubsetDefinition subset = getSubsetDefinition();
        subset.setTarget(target);
        final List<TableUID> tables = subset.getTables();
        tables.clear();

        final OIDataFile dataFile = ocm.getOIDataFile(oiTable.getOIFitsFile());
        if (dataFile != null) {
            tables.add(new TableUID(dataFile, oiTable.getExtName(), oiTable.getExtNb()));
        }

        // fire subset changed event:
        ocm.updateSubsetDefinition(this, subset);
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
                updateOIFitsCollection(((OIFitsCollectionEvent) event).getOIFitsCollection());
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

    /**
     * Return the subset definition given its subset name
     * @return subsetDefinition subset definition
     */
    private SubsetDefinition getSubsetDefinition() {
        if (this.subsetDefinition == null) {
            // get copy:
            this.subsetDefinition = ocm.getSubsetDefinition(this.subsetId);
        }
        return this.subsetDefinition;
    }

    /**
     * Define the subset identifier and reset subset
     * @param subsetId subset identifier
     */
    public void setSubsetId(final String subsetId) {
        this.subsetId = subsetId;
        // force reset:
        this.subsetDefinition = null;
    }
}
