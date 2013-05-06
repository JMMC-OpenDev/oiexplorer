/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.gui;

import fr.jmmc.jmcs.gui.FeedbackReport;
import fr.jmmc.jmcs.gui.component.GenericJTree;
import fr.jmmc.jmcs.gui.component.GenericListModel;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.oiexplorer.core.model.OIFitsCollection;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEvent;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventListener;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventType;
import static fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventType.ACTIVE_PLOT_CHANGED;
import static fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventType.COLLECTION_CHANGED;
import static fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventType.SUBSET_CHANGED;
import fr.jmmc.oiexplorer.core.model.oi.Plot;
import fr.jmmc.oiexplorer.core.model.oi.SubsetDefinition;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OITable;
import java.awt.Component;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.TreeSelectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This panel contains the oifits file load into the collection manager
 * and hightligh the files used by active plot.
 * @author mella
 */
public class OifitsFileListPanel extends javax.swing.JPanel implements OIFitsCollectionManagerEventListener {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(OifitsFileListPanel.class);
    /* members */
    /** OIFitsCollectionManager singleton */
    private final OIFitsCollectionManager ocm = OIFitsCollectionManager.getInstance();
    /** subset identifier */
    private String subsetId = null;

    /** Creates new form OifitsFileListPanel */
    public OifitsFileListPanel() {
        // always bind at the beginning of the constructor (to maintain correct ordering):
        ocm.bindCollectionChangedEvent(this);
        ocm.getPlotChangedEventNotifier().register(this);
        // DOES not work : ocm.bindSubsetDefinitionListChangedEvent(this);
        // TODO fix and replace PlotChangedEvent
        ocm.getActivePlotChangedEventNotifier().register(this);

        initComponents();
        postInit();
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
     * Free any ressource or reference to this instance :
     * remove this instance from OIFitsCollectionManager event notifiers
     */
    @Override
    public void dispose() {
        if (logger.isDebugEnabled()) {
            logger.debug("dispose: {}", ObjectUtils.getObjectInfo(this));
        }

        ocm.unbind(this);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        oifitsFileList = new javax.swing.JList();

        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setViewportView(oifitsFileList);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList oifitsFileList;
    // End of variables declaration//GEN-END:variables

    private void postInit() {
        // Add renderer to get short oifits filenames
        oifitsFileList.setCellRenderer(new OIFitsListRenderer());

        oifitsFileList.setEnabled(false);
    }

    /**
     * Update the file list
     * @param oiFitsCollection OIFitsCollection to process
     */
    protected void updateOIFitsList(final OIFitsCollection oiFitsCollection) {
        // always init default content
        final GenericListModel lm = new GenericListModel<OIFitsFile>(oiFitsCollection.getOIFitsFiles());
        oifitsFileList.setModel(lm);
        
        //ignore if subset has not been set (by ACTIVE_PLOT_CHANGED)
        if (this.subsetId == null) {
            return;
        }

        final SubsetDefinition subset = getSubsetDefinition();
        // ignore for non valid oifitsSubset associated
        if (subset==null || subset.getOIFitsSubset() == null) {
            return;
        }


        // select element present in both lists
        final List<OITable> oiFitsOfSubset = subset.getOIFitsSubset().getOITableList();                
        oifitsFileList.clearSelection();
        ListSelectionModel sm = oifitsFileList.getSelectionModel();

        int found = -1;
        for (int i = 0; i < lm.getSize(); i++) {
            for (OITable table : oiFitsOfSubset) {
                if (table.getOIFitsFile() == lm.getElementAt(i)) {
                    sm.addSelectionInterval(i, i);
                    if (found < 0) {
                        found = i;
                    }
                    break;
                }
            }
        }

        // display first item on top of the list
        SwingUtils.invokeAndWaitEDT(new Runnable() {
            @Override
            public void run() {
                oifitsFileList.ensureIndexIsVisible(lm.getSize()-1);
            }
        });
        oifitsFileList.ensureIndexIsVisible(found);
    }

    /**
     * Update the file list
     * @param activePlot plot used to initialize file list.
     */
    protected void updateOIFitsList(SubsetDefinition subset) {
        if (subset != null) {
            setSubsetId(subset.getId());
            updateOIFitsList(ocm.getOIFitsCollection());
        }
    }

    /**
     * Update the file list
     * @param activePlot plot used to initialize file list.
     */
    protected void updateOIFitsList(Plot activePlot) {
        if (activePlot != null) {
            updateOIFitsList(activePlot.getSubsetDefinition());
        }
    }

    /**
     * Define the subset identifier
     * @param subsetId subset identifier
     */
    public void setSubsetId(final String subsetId) {
        this.subsetId = subsetId;
    }

    /**
     * Return a new copy of the SubsetDefinition given its identifier (to update it)
     * @return copy of the SubsetDefinition or null if not found
     */
    private SubsetDefinition getSubsetDefinition() {
        return ocm.getSubsetDefinition(this.subsetId);
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

    /**
     * Handle the given OIFits collection event
     * @param event OIFits collection event
     */
    @Override
    public void onProcess(final OIFitsCollectionManagerEvent event) {
        logger.debug("onProcess {}", event);

        switch (event.getType()) {
            case COLLECTION_CHANGED:
                updateOIFitsList(event.getOIFitsCollection());
                break;
            /* TODO make it work
             * case SUBSET_CHANGED:
                updateOIFitsList(event.getSubsetDefinition());
                break;
             */
            case PLOT_CHANGED:
                updateOIFitsList(event.getPlot());
                break;
            case ACTIVE_PLOT_CHANGED:
                updateOIFitsList(event.getActivePlot());
                break;
            default:
        }
        logger.debug("onProcess {} - done", event);
    }
}
