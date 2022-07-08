/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.gui;

import fr.jmmc.jmal.ALX;
import fr.jmmc.jmcs.gui.component.GenericJTree;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.jmcs.util.StringUtils;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEvent;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventListener;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventType;
import static fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventType.ACTIVE_PLOT_CHANGED;
import static fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventType.COLLECTION_CHANGED;
import fr.jmmc.oiexplorer.core.model.oi.OIDataFile;
import fr.jmmc.oiexplorer.core.model.oi.Plot;
import fr.jmmc.oiexplorer.core.model.oi.SubsetDefinition;
import fr.jmmc.oiexplorer.core.model.oi.SubsetFilter;
import fr.jmmc.oiexplorer.core.model.oi.TableUID;
import fr.jmmc.oitools.model.Granule;
import fr.jmmc.oitools.model.InstrumentMode;
import fr.jmmc.oitools.model.InstrumentModeManager;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsCollection;
import fr.jmmc.oitools.model.OITable;
import fr.jmmc.oitools.model.Target;
import fr.jmmc.oitools.model.TargetManager;
import fr.jmmc.oitools.util.GranuleComparator;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This panel contains the data tree
 * 
 * // TODO: support multiple table selections
 * 
 * @author mella
 */
public final class DataTreePanel extends javax.swing.JPanel implements TreeSelectionListener, OIFitsCollectionManagerEventListener {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(DataTreePanel.class);
    /** singleton instance */
    public static final GranuleComparator CMP_TARGET_INSMODE = new GranuleComparator(
            Arrays.asList(
                    Granule.GranuleField.TARGET,
                    Granule.GranuleField.INS_MODE
            )
    );

    /* members */
    /** OIFitsCollectionManager singleton reference */
    private final static OIFitsCollectionManager ocm = OIFitsCollectionManager.getInstance();
    /** subset identifier */
    private String subsetId = OIFitsCollectionManager.CURRENT_SUBSET_DEFINITION;
    /** Swing data tree */
    private GenericJTree<Object> dataTree;

    /** Creates new form DataTreePanel */
    public DataTreePanel() {
        // always bind at the beginning of the constructor (to maintain correct ordering):
        ocm.bindCollectionChangedEvent(this);
        ocm.getActivePlotChangedEventNotifier().register(this);

        initComponents();
        postInit();
    }

    /**
     * Free any resource or reference to this instance :
     * remove this instance from OIFitsCollectionManager event notifiers
     */
    @Override
    public void dispose() {
        if (logger.isDebugEnabled()) {
            logger.debug("dispose: {}", ObjectUtils.getObjectInfo(this));
        }

        ocm.unbind(this);
    }

    /**
     * This method is useful to set the models and specific features of initialized swing components :
     */
    private void postInit() {

        // dataTree contains TargetUID, InsModeUID or OITable objects:
        dataTree = createTree();

        // Define root node once:
        final DefaultMutableTreeNode rootNode = dataTree.getRootNode();
        rootNode.setUserObject("Targets");

        ToolTipManager.sharedInstance().registerComponent(dataTree);

        dataTree.setCellRenderer(new TooltipTreeCellRenderer());

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

        final SubsetDefinition subsetRef = getSubsetDefinitionRef();

        // ALWAYS select a target
        if (oiFitsCollection.isEmpty()) {
            processSelection(null, null, null);
        } else {
            // Restore subset selection:
            TreePath[] newSelection = null;

            if (subsetRef != null) {
                newSelection = computeSelectionFromSubsetFilter(subsetRef.getFilter(), oiFitsCollection);
            }

            if ((newSelection == null) || (newSelection.length == 0)) {
                // select first target :
                dataTree.selectFirstChildNode(dataTree.getRootNode());
            } else {
                dataTree.selectPaths(newSelection);
            }
        }
    }

    /**
     * Update the data tree
     * @param activePlot plot used to initialize tree element.
     */
    private void updateOIFitsCollection(Plot activePlot) {
        if (activePlot != null) {
            SubsetDefinition subset = activePlot.getSubsetDefinition();
            setSubsetId(subset.getId());
            updateOIFitsCollection(ocm.getOIFitsCollection());
        }
    }

    /**
     * Generate the tree from the current edited list of targets
     * @param oiFitsCollection OIFitsCollection to process
     */
    private void generateTree(final OIFitsCollection oiFitsCollection) {

        final DefaultMutableTreeNode rootNode = dataTree.getRootNode();
        rootNode.removeAllChildren();

        // Sort granule by criteria (target / insMode / night):
        final GranuleComparator comparator = CMP_TARGET_INSMODE;

        final List<Granule> granules = oiFitsCollection.getSortedGranules(comparator);
        logger.debug("granules sorted: {}", granules);

        final Map<Granule, Set<OIData>> oiDataPerGranule = oiFitsCollection.getOiDataPerGranule();

        // Add nodes and their data tables:
        final List<Granule.GranuleField> fields = comparator.getSortDirectives();
        final int fieldsLen = fields.size();

        final DefaultMutableTreeNode[] pathNodes = new DefaultMutableTreeNode[fieldsLen + 1];
        int level;
        Granule.GranuleField field;
        Object value, other;

        pathNodes[0] = rootNode;

        for (Granule granule : granules) {

            // loop on fields:
            for (level = 1; level <= fieldsLen; level++) {
                field = fields.get(level - 1);
                value = granule.getField(field);

                if (value == null) {
                    logger.warn("null field value for granule: {}", granule);
                    value = "UNDEFINED";
                }

                DefaultMutableTreeNode prevNode = pathNodes[level];
                if (prevNode != null) {
                    // compare ?
                    other = prevNode.getUserObject();

                    // note: equals uses custom implementation in Target / InstrumentMode / NightId (all members are equals)
                    // equals method must be called on other to support proxy object (value.equals(other) may be different)
                    if (other == null || other.equals(value)) {
                        continue;
                    } else {
                        // different:
                        for (int i = level + 1; i <= fieldsLen; i++) {
                            // clear previous nodes:
                            pathNodes[i] = null;
                        }
                    }
                }

                pathNodes[level] = dataTree.addNode(pathNodes[level - 1], value);
            }

            final DefaultMutableTreeNode parent = pathNodes[level - 1];

            // Leaf:
            final Set<OIData> oiDatas = oiDataPerGranule.get(granule);
            if (oiDatas != null) {
                // for now per OIData:
                for (OITable table : oiDatas) {
                    // Avoid Table duplicates :
                    if (GenericJTree.findTreeNode(parent, table) == null) {
                        dataTree.addNode(parent, table);
                    }
                }
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

        final TreePath[] selection = dataTree.getSelectionPaths();

        if (selection != null) {
            // Use invokeLater to selection change issues with editors :
            SwingUtils.invokeLaterEDT(new Runnable() {
                @Override
                public void run() {
                    // Check if the root node is selected and is the only one.
                    final DefaultMutableTreeNode rootNode = dataTree.getRootNode();
                    if (selection.length == 1 && selection[0].getLastPathComponent() == rootNode) {
                        dataTree.selectFirstChildNode(rootNode); // always select a node other than root
                        return;
                    }

                    updateSubsetFilterFromTreeSelection(selection);

                    // we compute a new selection, because it can prune the old one from unwanted paths
                    final TreePath[] newSelection = computeSelectionFromSubsetFilter(
                            getSubsetDefinitionRef().getFilter(), ocm.getOIFitsCollection());

                    if (!Arrays.equals(selection, newSelection)) {
                        dataTree.selectPaths(newSelection); // ask to change the selection if it has changed
                    }
                }
            });
        }
    }

    /**
     * Updates the current subsetFilter from selected TreePath array. 
     * Currently only allows one target.
     *
     * @param selection the list of selected paths
     */
    private void updateSubsetFilterFromTreeSelection(final TreePath[] selection) {

        Target target = null;
        InstrumentMode insMode = null;
        boolean allInstruments = false;
        final List<OITable> listOITable = new ArrayList<>();

        // go through all selected paths. two examples of typical paths : [root,target], [root,target,insMode,table]
        for (TreePath selectedPath : selection) {
            // go through all nodes of the path: root, target, instrument mode, table
            // as soon as one of them is not consistent (i.e a different target), we skip the path
            for (Object node : selectedPath.getPath()) {

                if (node.equals(dataTree.getRootNode())) {
                    continue; // ignore root node, process to the next node (a target)
                }

                final Object userObject = ((DefaultMutableTreeNode) node).getUserObject(); // associated user object

                if (userObject instanceof Target) {
                    final Target thisTarget = (Target) userObject;
                    if (target == null) {
                        target = thisTarget; // first target encountered: register it
                    } else if (target != thisTarget) {
                        break; // a target is already registered, and the two targets are different: skip this path !!
                    }
                } else if (userObject instanceof InstrumentMode) {
                    if (!allInstruments) { // if not all instruments are already enabled
                        final InstrumentMode thisInsMode = (InstrumentMode) userObject;
                        if (insMode == null) {
                            insMode = thisInsMode; // first instrument encountered: register it
                        } else if (insMode != thisInsMode) {
                            insMode = null; // an instrument is already registered, and the two are different:
                            allInstruments = true; // set to null to enable all instruments
                        }
                    }
                } else if (userObject instanceof OITable) {
                    // if we reach this table node, it means we already passed through its
                    // parent insMode and its grandparent target
                    listOITable.add((OITable) userObject); // add the table to the list
                } else {
                    logger.error("Encountered unsupported node in the selected path : {}", userObject);
                }
            }
        }

        processSelection(target, insMode, listOITable);

        if (logger.isDebugEnabled()) {
            logger.debug("new subsetFilter: {}", getSubsetDefinitionRef().getFilter().toShortString());
        }
    }

    /**
     * Computes the minimal list of TreePath from a given subsetFilter. Does not select intermediary nodes, i.e if an
     * instrument     * node is selected, the parent target node will not be selected.
     *
     * @param filter the subsetFilter used to select some of the paths
     * @param oiFitsCollection the collection used to for targetManager and instrument manager
     * @return the computed list of paths
     */
    private TreePath[] computeSelectionFromSubsetFilter(
            final SubsetFilter filter, final OIFitsCollection oiFitsCollection) {

        final ArrayList<TreePath> selection = new ArrayList<>();

        if (filter.getTargetUID() == null) {
            // if target null, select first target as a default
            DefaultMutableTreeNode firstTargetNode = (DefaultMutableTreeNode) dataTree.getRootNode().getFirstChild();
            selection.add(new TreePath(firstTargetNode.getPath()));
        } else {
            final Target target = oiFitsCollection.getTargetManager().getGlobalByUID(filter.getTargetUID());
            final DefaultMutableTreeNode targetTreeNode = dataTree.findTreeNode(target);

            if (targetTreeNode != null) {
                DefaultMutableTreeNode insModeTreeNode = null;
                List<DefaultMutableTreeNode> listTableTreeNode = null;

                if (filter.getInsModeUID() != null) {
                    final InstrumentMode insMode = oiFitsCollection.getInstrumentModeManager().getGlobalByUID(filter.getInsModeUID());
                    insModeTreeNode = GenericJTree.findTreeNode(targetTreeNode, insMode);
                }

                if (!filter.getTables().isEmpty()) {

                    // for every instrument
                    for (int i = 0, sizeI = targetTreeNode.getChildCount(); i < sizeI; i++) {
                        DefaultMutableTreeNode insNode = (DefaultMutableTreeNode) targetTreeNode.getChildAt(i);

                        // skip instrument if it is not the one specified (null means all instruments)
                        if (insModeTreeNode != null && !insModeTreeNode.equals(insNode)) {
                            continue;
                        }

                        // for every table of the filter
                        for (TableUID tableUID : filter.getTables()) {

                            final String filePath = tableUID.getFile().getFile();
                            final Integer extNb = tableUID.getExtNb();

                            for (int j = 0, sizeJ = insNode.getChildCount(); j < sizeJ; j++) {
                                final DefaultMutableTreeNode node = (DefaultMutableTreeNode) insNode.getChildAt(j);
                                final OITable oiTable = (OITable) node.getUserObject();

                                if (filePath.equals(oiTable.getOIFitsFile().getAbsoluteFilePath())) {
                                    if (extNb != null && extNb.intValue() == oiTable.getExtNb()) {
                                        if (listTableTreeNode == null) {
                                            listTableTreeNode = new ArrayList<>();
                                        }
                                        listTableTreeNode.add(node);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                if (listTableTreeNode != null) {
                    for (DefaultMutableTreeNode node : listTableTreeNode) {
                        selection.add(new TreePath(node.getPath()));
                    }
                } else if (insModeTreeNode != null) {
                    selection.add(new TreePath(insModeTreeNode.getPath()));
                } else {
                    selection.add(new TreePath(targetTreeNode.getPath()));
                }
            }
        }

        final TreePath[] arraySelection = selection.toArray(new TreePath[0]);

        if (logger.isDebugEnabled()) {
            logger.debug("new selection : {}", selectionToString(arraySelection));
        }
        return arraySelection;
    }

    /**
     * Update the SubsetDefinition depending on the data tree selection
     * @param target selected target UID
     * @param insMode selected InstrumentMode UID
     * @param listOITable selected tables
     */
    private void processSelection(final Target target, final InstrumentMode insMode, final List<OITable> listOITable) {
        logger.debug("processSelection: {}", target, insMode, listOITable);

        // update subset definition (copy):
        final SubsetDefinition subsetCopy = getSubsetDefinition();
        if (subsetCopy != null) {
            final SubsetFilter filter = subsetCopy.getFilter();

            filter.setTargetUID(target == null ? null : target.getTarget());
            filter.setInsModeUID(insMode == null ? null : insMode.getInsName());

            final List<TableUID> tables = filter.getTables();
            tables.clear();
            if (listOITable != null) {
                for (OITable oiTable : listOITable) {
                    final OIDataFile dataFile = ocm.getOIDataFile(oiTable.getOIFitsFile());
                    if (dataFile != null) {
                        tables.add(new TableUID(dataFile, oiTable.getExtName(), oiTable.getExtNb()));
                    }
                }
            }

            // fire subset changed event:
            ocm.updateSubsetDefinition(this, subsetCopy);
        }
    }

    /**
     * Computes a pretty string from a selection. Used for logging for example.
     *
     * @param selection the selection to export to a string
     * @return the string computed from the selection
     */
    private String selectionToString(final TreePath[] selection) {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < selection.length; i++) {
            sb.append(i == 0 ? "[" : ",[");
            final Object[] nodes = selection[i].getPath();

            for (int j = 0; j < nodes.length; j++) {
                final DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes[j];
                if (j > 0) {
                    sb.append(",");
                }
                if (node.equals(dataTree.getRootNode())) {
                    sb.append("Root");
                } else {
                    final Object userObject = node.getUserObject();
                    if (userObject == null) {
                        sb.append("null");
                    } else if (userObject instanceof Target) {
                        sb.append(((Target) userObject).getTarget());
                    } else if (userObject instanceof InstrumentMode) {
                        sb.append(((InstrumentMode) userObject).getInsName());
                    } else if (userObject instanceof OITable) {
                        final OITable oiTable = (OITable) userObject;
                        final OIDataFile dataFile = ocm.getOIDataFile(oiTable.getOIFitsFile());
                        if (dataFile == null) {
                            sb.append(((OITable) userObject).getExtName());
                        } else {
                            (new TableUID(dataFile, oiTable.getExtName(), oiTable.getExtNb())).appendShortString(sb);
                        }
                    } else {
                        sb.append(userObject.getClass().getSimpleName());
                    }
                }
            }
            sb.append("]");
        }
        return sb.toString();
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
        jPanelGenericFilters = new javax.swing.JPanel();

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

        jPanelGenericFilters.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jPanelGenericFilters, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel genericTreePanel;
    private javax.swing.JPanel jPanelGenericFilters;
    private javax.swing.JScrollPane jScrollPane;
    // End of variables declaration//GEN-END:variables

    /**
     * Return a new copy of the SubsetDefinition given its identifier (to update it)
     * @return copy of the SubsetDefinition or null if not found
     */
    private SubsetDefinition getSubsetDefinition() {
        return ocm.getSubsetDefinition(this.subsetId);
    }

    /**
     * Return a the SubsetDefinition reference given its identifier (to read it)
     * @return SubsetDefinition reference or null if not found
     */
    private SubsetDefinition getSubsetDefinitionRef() {
        return ocm.getSubsetDefinitionRef(this.subsetId);
    }

    /**
     * Define the subset identifier and reset subset
     * @param subsetId subset identifier
     */
    public void setSubsetId(final String subsetId) {
        this.subsetId = subsetId;
    }

    /*
     * OIFitsCollectionManagerEventListener implementation 
     */
    /**
     * Return the optional subject id i.e. related object id that this listener accepts
     * @param type event type
     * @return subject id (null means accept any event) or DISCARDED_SUBJECT_ID to discard event
     */
    @Override
    public String getSubjectId(final OIFitsCollectionManagerEventType type) {
        // accept all
        return null;
    }

    /**
     * Handle the given OIFits collection event
     * @param event OIFits collection event
     */
    @Override
    public void onProcess(final OIFitsCollectionManagerEvent event) {
        logger.debug("onProcess {}", event);

        switch (event.getType()) {
            case COLLECTION_CHANGED:
                updateOIFitsCollection(event.getOIFitsCollection());
                break;
            case ACTIVE_PLOT_CHANGED:
                updateOIFitsCollection(event.getActivePlot());
                break;
            default:
        }
        logger.debug("onProcess {} - done", event);
    }

    private static InstrumentModeManager getInstrumentModeManager() {
        return ocm.getOIFitsCollection().getInstrumentModeManager();
    }

    private static TargetManager getTargetManager() {
        return ocm.getOIFitsCollection().getTargetManager();
    }

    // TODO: share code with GranuleTreePanel ?
    private GenericJTree<Object> createTree() {
        return new GenericJTree<Object>(null, TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION) {
            /** default serial UID for Serializable interface */
            private static final long serialVersionUID = 1;

            /** temporary buffer */
            private final StringBuilder tmpBuf = new StringBuilder(64);

            @Override
            protected String convertUserObjectToString(final Object userObject) {
                if (userObject instanceof Target) {
                    return ((Target) userObject).getTarget(); // global UID
                }
                if (userObject instanceof InstrumentMode) {
                    return ((InstrumentMode) userObject).getInsName(); // global UID
                }
                if (userObject instanceof OITable) {
                    return getDisplayLabel((OITable) userObject, tmpBuf);
                }
                return toString(userObject);
            }

            /**
             * Return the label displayed in the data tree
             * @param table OITable to display
             * @param sb temporary buffer
             * @return label
             */
            private String getDisplayLabel(final OITable table, final StringBuilder sb) {
                if (table instanceof OIData) {
                    final OIData oiData = (OIData) table;
                    sb.setLength(0);
                    sb.append(table.getExtName());
                    sb.append('#');
                    sb.append(table.getExtNb());
                    final String dateObs = oiData.getDateObs();
                    if (!StringUtils.isEmpty(dateObs)) {
                        sb.append(' ').append(dateObs);
                    }
                    sb.append(' ').append(oiData.getInsName());
                    return sb.toString();
                }
                return (table != null) ? table.toString() : "UNDEFINED";
            }
        };
    }

    private final static class TooltipTreeCellRenderer extends DefaultTreeCellRenderer {

        private static final long serialVersionUID = 1L;

        /** temporary buffer */
        private final StringBuilder tmpBuf = new StringBuilder(64);

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean sel, boolean expanded, boolean leaf, int row,
                                                      boolean hasFocus) {

            if (value != null) {
                final Object userObject;
                if (value instanceof DefaultMutableTreeNode) {
                    userObject = ((DefaultMutableTreeNode) value).getUserObject();
                    setToolTipText(getTreeTooltipText(userObject, tmpBuf));
                }
            }
            return super.getTreeCellRendererComponent(tree, value, sel,
                    expanded, leaf, row, hasFocus);
        }

        private String getTreeTooltipText(final Object value, final StringBuilder sb) {
            sb.setLength(0);

            if (value instanceof Target) {
                final Target target = (Target) value;
                sb.append("<b>name:</b> ").append(target.getTarget());

                final List<String> aliases = getTargetManager().getSortedUniqueAliases(target);
                if (aliases != null) {
                    sb.append("<hr>");
                    sb.append("<b>Aliases:</b><br>");
                    for (int j = 0, end = aliases.size(); j < end; j++) {
                        if (j != 0) {
                            sb.append("<br>");
                        }
                        sb.append("- ").append(aliases.get(j));
                    }
                    sb.append("<hr>");
                } else {
                    sb.append("<br>");
                }
                sb.append("<b>Coords:</b> ");
                ALX.toHMS(sb, target.getRaEp0());
                sb.append(' ');
                ALX.toDMS(sb, target.getDecEp0());

                // TODO: check units
                if (!Double.isNaN(target.getPmRa()) && !Double.isNaN(target.getPmDec())) {
                    // convert deg/year in mas/year :
                    sb.append("<br><b>Proper motion</b> (mas/yr): ").append(target.getPmRa() * ALX.DEG_IN_MILLI_ARCSEC)
                            .append(' ').append(target.getPmDec() * ALX.DEG_IN_MILLI_ARCSEC);
                }
                if (!Double.isNaN(target.getParallax()) && !Double.isNaN(target.getParaErr())) {
                    sb.append("<br><b>Parallax</b> (mas): ").append(target.getParallax() * ALX.DEG_IN_MILLI_ARCSEC)
                            .append(" [").append(target.getParaErr() * ALX.DEG_IN_MILLI_ARCSEC).append(']');
                }
                if (target.getSpecTyp() != null && !target.getSpecTyp().isEmpty()) {
                    sb.append("<br><b>Spectral types</b>: ").append(target.getSpecTyp());
                }
            } else if (value instanceof InstrumentMode) {
                final InstrumentMode insMode = (InstrumentMode) value;
                sb.append("<b>name:</b> ").append(insMode.getInsName());

                final List<String> aliases = getInstrumentModeManager().getSortedUniqueAliases(insMode);
                if (aliases != null) {
                    sb.append("<hr>");
                    sb.append("<b>Aliases:</b><br>");
                    for (int j = 0, end = aliases.size(); j < end; j++) {
                        if (j != 0) {
                            sb.append("<br>");
                        }
                        sb.append("- ").append(aliases.get(j));
                    }
                    sb.append("<hr>");
                } else {
                    sb.append("<br>");
                }
                sb.append("<b>Nb channels:</b> ").append(insMode.getNbChannels());
                sb.append("<br><b>Lambda min:</b> ").append(insMode.getLambdaMin());
                sb.append("<br><b>Lambda max:</b> ").append(insMode.getLambdaMax());
                sb.append("<br><b>Resolution:</b> ").append(insMode.getResPower());
            } else if (value instanceof OIData) {
                final OIData oiData = (OIData) value;
                sb.append("<b>Table:</b> ").append(oiData.getExtName()).append('#').append(oiData.getExtNb());
                sb.append("<br><b>OIFits:</b> ").append(oiData.getOIFitsFile().getFileName());
                sb.append("<br><b>DATE-OBS:</b> ").append(oiData.getDateObs());
                sb.append("<br><b>ARRNAME:</b> ").append(oiData.getArrName());
                sb.append("<br><b>INSNAME:</b> ").append(oiData.getInsName());
                sb.append("<br><b>NB_MEASUREMENTS:</b> ").append(oiData.getNbMeasurements());

                sb.append("<br><b>Baselines:</b> ");
                for (short[] staIndex : oiData.getDistinctStaIndex()) {
                    sb.append(oiData.getStaNames(staIndex)).append(' '); // cached
                }
                sb.append("<br><b>Configurations:</b> ");
                for (short[] staConf : oiData.getDistinctStaConf()) {
                    sb.append(oiData.getStaNames(staConf)).append(' '); // cached
                }
            }
            if (sb.length() == 0) {
                return null;
            } else {
                sb.insert(0, "<html>");
                sb.append("</html>");
            }
            return sb.toString();
        }
    }
}
