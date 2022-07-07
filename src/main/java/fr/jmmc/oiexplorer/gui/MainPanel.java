/**
 * *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 *****************************************************************************
 */
package fr.jmmc.oiexplorer.gui;

import com.jidesoft.swing.JideButton;
import com.jidesoft.swing.JideTabbedPane;
import com.jidesoft.swing.TabEditingValidator;
import fr.jmmc.jmcs.Bootstrapper;
import fr.jmmc.jmcs.data.MimeType;
import fr.jmmc.jmcs.gui.action.ActionRegistrar;
import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.jmcs.gui.component.Disposable;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.oiexplorer.core.export.DocumentExportable;
import fr.jmmc.oiexplorer.core.export.DocumentMode;
import fr.jmmc.oiexplorer.core.export.DocumentOptions;
import fr.jmmc.oiexplorer.core.export.DocumentSize;
import fr.jmmc.oiexplorer.core.export.Orientation;
import fr.jmmc.oiexplorer.core.gui.GlobalView;
import fr.jmmc.oiexplorer.core.gui.PlotChartPanel;
import fr.jmmc.oiexplorer.core.gui.PlotView;
import fr.jmmc.oiexplorer.core.gui.action.ExportDocumentAction;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEvent;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventListener;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventType;
import fr.jmmc.oiexplorer.core.model.oi.Identifiable;
import fr.jmmc.oiexplorer.core.model.oi.Plot;
import fr.jmmc.oiexplorer.core.model.oi.SubsetDefinition;
import fr.jmmc.oiexplorer.core.model.plot.PlotDefinition;
import fr.jmmc.oiexplorer.gui.action.LoadOIDataCollectionAction;
import fr.jmmc.oiexplorer.gui.action.LoadOIFitsAction;
import fr.jmmc.oiexplorer.gui.action.OIFitsExplorerExportAction;
import fr.jmmc.oiexplorer.gui.action.RemoveAction;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.UIResource;
import org.jfree.chart.ui.Drawable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main container of OIFits Explorer App
 *
 * @author mella
 */
public class MainPanel extends javax.swing.JPanel implements OIFitsCollectionManagerEventListener, DocumentExportable {

    /**
     * default serial UID for Serializable interface
     */
    private static final long serialVersionUID = 1;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(MainPanel.class);

    /** view name prefix for DEBUG views */
    public final static String DEBUG_VIEW_PREFIX = "DEBUG - ";

    /** dev mode flag (-Doixp.devMode=true) */
    public final static boolean DEV_MODE = "true".equalsIgnoreCase(System.getProperty("oixp.devMode", "false"));

    /* members */
    /** OIFitsCollectionManager singleton reference */
    private final static OIFitsCollectionManager ocm = OIFitsCollectionManager.getInstance();
    /** new plot tab action */
    private NewPlotTabAction newPlotTabAction = null;
    /** global view */
    private GlobalView globalView = null;
    /** prepared page content */
    private List<DocumentPage> pages = null;

    // debug support:
    private JEditorPane editorPane = null;

    /**
     * Creates new form MainPanel
     */
    public MainPanel() {
        // always bind at the beginning of the constructor (to maintain correct ordering):
        ocm.bindCollectionChangedEvent(this);
        ocm.bindPlotListChangedEvent(this);
        ocm.getActivePlotChangedEventNotifier().register(this);

        if (DEV_MODE) {
            ocm.getPlotChangedEventNotifier().register(this);
        }

        // Build GUI
        initComponents();

        // Finish init
        postInit();
    }

    /**
     * Free any ressource or reference to this instance : remove this instance from OIFitsCollectionManager event
     * notifiers dispose also child components
     */
    @Override
    public void dispose() {
        if (logger.isDebugEnabled()) {
            logger.debug("dispose: {}", ObjectUtils.getObjectInfo(this));
        }

        ocm.unbind(this);

        // forward dispose() to child components:
        if (dataTreePanel != null) {
            dataTreePanel.dispose();
        }

        for (int i = 0, tabCount = tabbedPaneTop.getTabCount(); i < tabCount; i++) {
            final Component com = tabbedPaneTop.getComponentAt(i);
            if (com instanceof PlotView) {
                final PlotView plotView = (PlotView) com;
                plotView.dispose();
            }
        }
    }

    /**
     * Disable event processing for GUI components that may interfere with shell actions (export...)
     */
    public void prepareShellAction() {
        // Hack to unbind DataTreePanel when exporting plots (to avoid changing initial subset selection):
        this.dataTreePanel.dispose();
        // read-only but useless event processing:
        this.granuleTreePanel.dispose();
        this.oifitsFileListPanel.dispose();
    }

    /**
     * Export the component as a document using the given action:
     * the component should check if there is something to export ?
     * @param action export action to perform the export action
     */
    @Override
    public void performAction(final ExportDocumentAction action) {
        logger.info("MainPanel.performAction");

        action.process(this);
    }

    /**
     * Return the default file name
     * @param fileExtension  document's file extension
     * @return default file name
     */
    @Override
    public String getDefaultFileName(final String fileExtension) {
        return "AllPlots." + fileExtension;
    }

    /** Array of drawables  */
    private static class DocumentPage {

        /* members */
        final Drawable[] drawables;

        DocumentPage(final Drawable[] drawables) {
            this.drawables = drawables;
        }

        Drawable[] getDrawables() {
            return drawables;
        }

    }

    /**
     * Prepare the page layout before doing the export:
     * Performs layout and modifies the given options
     * @param options document options used to prepare the document
     */
    @Override
    public void prepareExport(final DocumentOptions options) {

        // allocate pages:
        this.pages = new ArrayList<DocumentPage>();

        int numberOfPages = 0;

        final int tabCount = tabbedPaneTop.getTabCount();

        if (DocumentMode.MULTI_PAGE == options.getMode()) {

            for (int i = 0; i < tabCount; i++) {
                final Component com = tabbedPaneTop.getComponentAt(i);
                if (com instanceof PlotView) {
                    final PlotView view = (PlotView) com;

                    final PlotChartPanel plotChartPanel = view.getPlotPanel();

                    if (plotChartPanel.canExportPlotFile()) {
                        numberOfPages++;

                        // warning: use pageIndex = 1 (unused) but may change in future !
                        this.pages.add(new DocumentPage(plotChartPanel.preparePage(1)));
                    }
                } else if (com instanceof GlobalView) {
                    final GlobalView view = (GlobalView) com;
                    numberOfPages++;

                    // warning: use pageIndex = 1 (unused) but may change in future !
                    this.pages.add(new DocumentPage(view.preparePage(1)));
                }
            }
        } else if (DocumentMode.DEFAULT == options.getMode()) {
            for (int i = 0; i < tabCount; i++) {
                final Component com = tabbedPaneTop.getComponentAt(i);
                if (com instanceof PlotView) {
                    final PlotView view = (PlotView) com;

                    final PlotChartPanel plotChartPanel = view.getPlotPanel();

                    if (plotChartPanel.canExportPlotFile()) {
                        numberOfPages++;

                        // warning: use pageIndex = 1 (unused) but may change in future !
                        this.pages.add(new DocumentPage(plotChartPanel.preparePage(1)));
                    }
                }
            }
        } else if (DocumentMode.SINGLE_PAGE == options.getMode()) {
            numberOfPages = 1;

            final List<Drawable> chartList = new ArrayList<Drawable>(tabCount);

            for (int i = 0; i < tabCount; i++) {
                final Component com = tabbedPaneTop.getComponentAt(i);
                if (com instanceof PlotView) {
                    final PlotView view = (PlotView) com;

                    final PlotChartPanel plotChartPanel = view.getPlotPanel();

                    if (plotChartPanel.canExportPlotFile()) {
                        chartList.add(plotChartPanel.getChart());
                    }
                }
            }

            // put all charts in one page:
            this.pages.add(new DocumentPage(chartList.toArray(new Drawable[chartList.size()])));

        } else {
            logger.info("unsupported DocumentMode: {}", options.getMode());
        }

        options.setDocumentSize(DocumentSize.NORMAL)
                .setOrientation(Orientation.Landscape)
                .setNumberOfPages(numberOfPages);
    }

    /**
     * Return the page to export given its page index
     * @param pageIndex page index (1..n)
     * @return Drawable array to export on this page
     */
    @Override
    public Drawable[] preparePage(final int pageIndex) {
        final DocumentPage currentPage = pages.get(pageIndex - 1);
        return currentPage.getDrawables();
    }

    /**
     * Callback indicating the document is done to reset the component's state
     */
    @Override
    public void postExport() {
        // page cleanup:
        pages = null;
    }

    /**
     * This method is useful to set the models and specific features of initialized swing components :
     */
    private void postInit() {
        registerActions();

        this.globalView = new GlobalView();

        if (this.tabbedPaneTop instanceof JideTabbedPane) {
            final JideTabbedPane jideTabbedPane = (JideTabbedPane) this.tabbedPaneTop;

            // Link removeCurrentView to the tabpane close button
            jideTabbedPane.setCloseAction(new AbstractAction() {
                /**
                 * default serial UID for Serializable interface
                 */
                private static final long serialVersionUID = 1;

                @Override
                public void actionPerformed(ActionEvent e) {
                    removeCurrentView();
                }
            });

            jideTabbedPane.setBoldActiveTab(true);
            jideTabbedPane.setShowCloseButton(true);
            jideTabbedPane.setShowCloseButtonOnSelectedTab(true);
            jideTabbedPane.setShowCloseButtonOnTab(true);

            jideTabbedPane.setTabShape(JideTabbedPane.SHAPE_ROUNDED_VSNET);
            jideTabbedPane.setTabResizeMode(JideTabbedPane.RESIZE_MODE_NONE);
            jideTabbedPane.setColorTheme(JideTabbedPane.COLOR_THEME_VSNET);
            jideTabbedPane.setTabEditingAllowed(true);

            jideTabbedPane.setTabEditingValidator(new TabEditingValidator() {
                @Override
                public boolean shouldStartEdit(int tabIndex, MouseEvent event) {
                    return true;
                }

                @Override
                public boolean isValid(int tabIndex, String tabText) {
                    return true;
                }

                @Override
                public boolean alertIfInvalid(int tabIndex, String tabText) {
                    setActivePlotName(tabText);
                    return true;
                }
            });

            final JideButtonUIResource plusButton = new JideButtonUIResource(newPlotTabAction);
            jideTabbedPane.setTabLeadingComponent(plusButton);

            jideTabbedPane.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    updateActivePlot();
                }
            });
        }
    }

    /**
     * Create the main actions and/or present in the toolbar
     */
    private void registerActions() {
        newPlotTabAction = new NewPlotTabAction(NewPlotTabAction.class.getName(), "newPlotTabAction");
        newPlotTabAction.putValue(Action.NAME, " + ");
        newPlotTabAction.putValue(Action.SHORT_DESCRIPTION, "add a new plot view ...");

        // Build toolbar
        toolBar.add(ActionRegistrar.getInstance().get(LoadOIFitsAction.className, LoadOIFitsAction.actionName)).setHideActionText(true);
        toolBar.add(ActionRegistrar.getInstance().get(RemoveAction.className, RemoveAction.actionName)).setHideActionText(true);
        toolBar.add(ActionRegistrar.getInstance().get(LoadOIDataCollectionAction.className, LoadOIDataCollectionAction.actionName)).setHideActionText(true);
        toolBar.add(OIFitsExplorerExportAction.getInstance(MimeType.PDF)).setHideActionText(true);
    }

    /**
     * Synchronize tab and Views of OIFitsCollectionManager
     * @see #onProcess(fr.jmmc.oiexplorer.core.model.event.GenericEvent)
     *
     * @param plotList plot list
     */
    private void updateTabContent(final List<Plot> plotList) {
        logger.debug("updateTabContent - plots : {}", plotList);

        // remove dead plot views:
        for (int i = 0, tabCount = tabbedPaneTop.getTabCount(); i < tabCount; i++) {
            final Component com = tabbedPaneTop.getComponentAt(i);
            if (com instanceof PlotView) {
                final PlotView plotView = (PlotView) com;

                if (!Identifiable.hasIdentifiable(plotView.getPlotId(), plotList)) {
                    if (removeView(i)) {
                        // restart loop (global view may be added/removed):
                        i = 0;
                        tabCount = tabbedPaneTop.getTabCount();
                    }
                }
            }
        }

        // add missing plot views:
        for (Plot plot : plotList) {
            final String plotId = plot.getId();

            // check where tab is already present:
            if (findPlotView(tabbedPaneTop, plotId) == -1) {
                final PlotView p = new PlotView(plotId);
                addView(p, plotId);
            }
        }
    }

    private String getSelectedPlotId() {
        PlotView currentPlotView = getCurrentPlotView();
        if (currentPlotView == null) {
            // TODO disable dataTreePanel ?
            return null;
        }
        return currentPlotView.getPlotId();
    }

    private void setSelectedPlotId(final String plotId) {
        logger.debug("setSelectedPlotId: {}", plotId);

        final int idx = findPlotView(tabbedPaneTop, plotId);
        if (idx != -1) {
            this.tabbedPaneTop.setSelectedIndex(idx);
        }
    }

    private void updateActivePlot() {
        String selectedPlotId = getSelectedPlotId();
        if (selectedPlotId != null) {
            ocm.fireActivePlotChanged(this, selectedPlotId, null);
        }
    }

    /*
     * Called by jideTab to change plot name.
     * @param name futur name of active plot
     */
    private void setActivePlotName(String name) {
        PlotView currentPlotView = getCurrentPlotView();
        if (currentPlotView != null) {
            final String plotId = currentPlotView.getPlotId();
            ocm.getPlotRef(plotId).setName(name);
            ocm.firePlotChanged(this, plotId, null);
        }
    }

    /**
     * Return the current plot view
     *
     * @return the current plot view or null if not a PlotView instance
     */
    public PlotView getCurrentPlotView() {
        final Component com = getCurrentView();
        return (com instanceof PlotView) ? (PlotView) com : null;
    }

    /**
     * Return the current view
     *
     * @return any component or null
     */
    private Component getCurrentView() {
        return tabbedPaneTop.getSelectedComponent();
    }

    /**
     * Return the current view as a DocumentExportable
     *
     * @return a DocumentExportable instance or null
     */
    public DocumentExportable getCurrentExportableView() {
        final Component com = getCurrentView();
        if (com instanceof PlotView) {
            return ((PlotView) com).getPlotPanel();
        }
        if (com instanceof GlobalView) {
            return (DocumentExportable) com;
        }
        return null;
    }

    /**
     * Add the given panel or one new if null given.
     *
     * @param panel Panel to add (PlotView instance)
     * @param tabName name of panel to be added
     */
    public void addView(final JComponent panel, final String tabName) {
        JComponent panelToAdd = panel;

        if (panelToAdd == null) {
            // note: as a plot is added, then updateTabContent() is called by event notifier:
            createNewPlot();
            return;
        }

        final String name;
        if ((tabName != null) && (tabName.length() > 0)) {
            name = tabName;
        } else {
            name = panelToAdd.getClass().getSimpleName();
        }

        // To correctly match deeper background color of inner tab panes
        panelToAdd.setOpaque(false);

        tabbedPaneTop.add(name, panelToAdd);

        if (panelToAdd instanceof PlotView) {
            final PlotView plotView = (PlotView) panelToAdd;

            final PlotChartPanel plotChartPanel = plotView.getPlotPanel();
            // update global view:
            this.globalView.addChart(plotChartPanel.getChart(), plotChartPanel.getCrosshairOverlay());
        }

        logger.debug("Added '{}' panel to PreferenceView tabbed pane.", name);

        updateOverviewTab();
    }

    public boolean hasView(final String tabName) {
        return (findView(tabbedPaneTop, tabName) != -1);
    }

    public void setSelectedView(final String tabName) {
        logger.debug("setSelectedView: {}", tabName);

        final int idx = findView(tabbedPaneTop, tabName);
        if (idx != -1) {
            this.tabbedPaneTop.setSelectedIndex(idx);
        }
    }

    /**
     * Create a new plot (plotDef,subset copied from current). The created objects are added to the manager
     */
    private void createNewPlot() {
        String id;

        // find subset id:
        for (int count = 1;; count++) {
            id = "SUBSET_" + count;
            if (!ocm.hasSubsetDefinition(id)) {
                break;
            }
        }

        final SubsetDefinition subset;

        // Keep same SubsetDefinition:
        if (false) {
            // dead code left as a reminder how to create properly a new subset (clone):
            subset = new SubsetDefinition();
            subset.setId(id);
            subset.setName(id);
            subset.copyValues(ocm.getCurrentSubsetDefinitionRef());

            if (!ocm.addSubsetDefinition(subset)) {
                throw new IllegalStateException("unable to addSubsetDefinition : " + subset);
            }
        } else {
            subset = ocm.getCurrentSubsetDefinitionRef();
        }

        // find plotDef id:
        for (int count = 1;; count++) {
            id = "PLOT_DEF_" + count;
            if (!ocm.hasPlotDefinition(id)) {
                break;
            }
        }

        final PlotDefinition plotDef = new PlotDefinition();
        plotDef.setId(id);
        plotDef.setName(id);
        plotDef.copyValues(ocm.getCurrentPlotDefinitionRef());

        if (!ocm.addPlotDefinition(plotDef)) {
            throw new IllegalStateException("unable to addPlotDefinition : " + plotDef);
        }

        // find plot id:
        for (int count = 1;; count++) {
            id = "VIEW_" + count;
            if (!ocm.hasPlot(id)) {
                break;
            }
        }

        // Create new Plot with subset and plotdefinition
        final Plot plot = new Plot();
        plot.setId(id);
        plot.setName(id);
        plot.setPlotDefinition(plotDef);
        plot.setSubsetDefinition(subset);

        // fire PlotListChanged ie will call updateTabContent():
        if (!ocm.addPlot(plot)) {
            throw new IllegalStateException("unable to addPlot : " + plot);
        }

        // change selected plot:
        ocm.fireActivePlotChanged(null, id, null);
    }

    /**
     * Remove the current view
     */
    private void removeCurrentView() {
        final int index = tabbedPaneTop.getSelectedIndex();
        if (index != -1) {
            logger.debug("removeCurrentView(): {}", index);

            // list will be refresh by fired event inside removePlot
            final String plotId = getSelectedPlotId();
            if (plotId != null) {
                ocm.removePlot(plotId);
            } else {
                removeView(index);
            }
        }
    }

    /**
     * Remove other views
     */
    public void removeOtherViews() {
        // remove views except plot views:
        for (int i = 0, tabCount = tabbedPaneTop.getTabCount(); i < tabCount; i++) {
            final Component com = tabbedPaneTop.getComponentAt(i);
            if (!(com instanceof PlotView)) {
                if (removeView(i)) {
                    // restart loop (global view may be added/removed):
                    i = 0;
                    tabCount = tabbedPaneTop.getTabCount();
                }
            }
        }
    }

    /**
     * Remove view at the given index
     * @param index view index
     * @return true if removed; false otherwise
     */
    private boolean removeView(final int index) {
        final String name = tabbedPaneTop.getTitleAt(index);
        if (name.startsWith(DEBUG_VIEW_PREFIX)) {
            return false;
        }

        // Note: views should be freed by GC soon
        // EventNotifier can remove them automatically from its listeners (weak reference)
        // BUT not immediately so such phantom views can still process useless events !
        // CONCLUSION: it is better to do it explicitely even if EventNotifier could do it but asynchronously:
        final Component com = tabbedPaneTop.getComponentAt(index);
        if (com instanceof PlotView) {
            final PlotView plotView = (PlotView) com;

            final PlotChartPanel plotChartPanel = plotView.getPlotPanel();
            // update global view:
            this.globalView.removeChart(plotChartPanel.getChart());

            // free resources (unregister event notifiers):
            plotView.dispose();
        } else if (com instanceof Disposable) {
            final Disposable d = (Disposable) com;

            // free resources (unregister event notifiers):
            d.dispose();
        }
        tabbedPaneTop.removeTabAt(index);

        updateOverviewTab();
        return true;
    }

    private void updateOverviewTab() {
        if (globalView.getChartCount() <= 1) {
            this.tabbedPaneTop.remove(globalView);
        } else {
            this.tabbedPaneTop.add(globalView, "Overview", 0);
        }
    }

    /**
     * Find the plot view given its plot identifier
     * @param tabbedPane tabbed pane
     * @param plotId plot identifier
     * @return view index or -1 if not found
     */
    private static int findPlotView(final JTabbedPane tabbedPane, final String plotId) {
        Component com;
        for (int i = 0, tabCount = tabbedPane.getTabCount(); i < tabCount; i++) {
            com = tabbedPane.getComponentAt(i);
            if (com instanceof PlotView) {
                final PlotView plotView = (PlotView) com;
                if (plotId.equals(plotView.getPlotId())) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Find the view given its name
     * @param tabbedPane tabbed pane
     * @param tabName name of the tabbed pane
     * @return view index or -1 if not found
     */
    private static int findView(final JTabbedPane tabbedPane, final String tabName) {
        for (int i = 0, tabCount = tabbedPane.getTabCount(); i < tabCount; i++) {
            final String name = tabbedPane.getTitleAt(i);
            if (tabName.equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Return the OIFits File List Panel.
     *
     * @return this.oifitsFileListPanel
     */
    public OIFitsFileListPanel getOIFitsFileListPanel() {
        return this.oifitsFileListPanel;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mainSplitPane = new javax.swing.JSplitPane();
        dataSplitPane = new javax.swing.JSplitPane();
        dataSplitTopPanel = new javax.swing.JPanel();
        toolBar = new javax.swing.JToolBar();
        jTabbedPaneBrowser = new javax.swing.JTabbedPane();
        granuleTreePanel = new fr.jmmc.oiexplorer.gui.GranuleTreePanel();
        oifitsFileListPanel = new fr.jmmc.oiexplorer.gui.OIFitsFileListPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        dataTreePanel = new fr.jmmc.oiexplorer.gui.DataTreePanel();
        genericFiltersPanel1 = new fr.jmmc.oiexplorer.gui.GenericFiltersPanel();
        tabbedPaneTop = createTabbedPane();

        setLayout(new java.awt.GridBagLayout());

        dataSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        dataSplitPane.setResizeWeight(0.3);
        dataSplitPane.setMinimumSize(new java.awt.Dimension(150, 58));

        dataSplitTopPanel.setLayout(new java.awt.GridBagLayout());

        toolBar.setRollover(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        dataSplitTopPanel.add(toolBar, gridBagConstraints);

        jTabbedPaneBrowser.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        jTabbedPaneBrowser.addTab("Granules", granuleTreePanel);
        jTabbedPaneBrowser.addTab("Files", oifitsFileListPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        dataSplitTopPanel.add(jTabbedPaneBrowser, gridBagConstraints);

        dataSplitPane.setLeftComponent(dataSplitTopPanel);

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(0.66);
        jSplitPane1.setTopComponent(dataTreePanel);
        jSplitPane1.setBottomComponent(genericFiltersPanel1);

        dataSplitPane.setRightComponent(jSplitPane1);

        mainSplitPane.setLeftComponent(dataSplitPane);
        mainSplitPane.setRightComponent(tabbedPaneTop);

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
    private fr.jmmc.oiexplorer.gui.GenericFiltersPanel genericFiltersPanel1;
    private fr.jmmc.oiexplorer.gui.GranuleTreePanel granuleTreePanel;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPaneBrowser;
    private javax.swing.JSplitPane mainSplitPane;
    private fr.jmmc.oiexplorer.gui.OIFitsFileListPanel oifitsFileListPanel;
    private javax.swing.JTabbedPane tabbedPaneTop;
    private javax.swing.JToolBar toolBar;
    // End of variables declaration//GEN-END:variables

    /**
     * This action open prepare plot objects and open one new tab.
     */
    private final class NewPlotTabAction extends RegisteredAction {

        /**
         * default serial UID for Serializable interface
         */
        private static final long serialVersionUID = 1;

        NewPlotTabAction(final String className, final String actionName) {
            super(className, actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            addView(null, null);
        }
    }

    private final static class JideButtonUIResource extends JideButton implements UIResource {

        /**
         * default serial UID for Serializable interface
         */
        private static final long serialVersionUID = 1;

        public JideButtonUIResource(String text) {
            super(text);
        }

        public JideButtonUIResource(Action action) {
            super(action);
        }
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
                // TODO init first tab if empty ?
                if (DEV_MODE) {
                    if (editorPane == null) {
                        addView(createDebugPanel(), DEBUG_VIEW_PREFIX + "Model");
                    }
                }
                break;
            case PLOT_LIST_CHANGED:
                // Update tabpane content
                updateTabContent(event.getPlotList());
                break;
            case PLOT_CHANGED:
                // nothing to do (debug model below)
                break;
            case ACTIVE_PLOT_CHANGED:
                setSelectedPlotId(event.getActivePlot().getId());
                break;
            default:
        }

        if (editorPane != null) {
            editorPane.setText(ocm.dumpOIFitsCollection() + ocm.getCurrentSubsetDefinitionRef());
        }

        logger.debug("onProcess {} - done", event);
    }

    static JTabbedPane createTabbedPane() {
        if (Bootstrapper.isHeadless()) {
            return new JTabbedPane();
        }
        return new JideTabbedPane();
    }

    private JComponent createDebugPanel() {
        if (editorPane == null) {
            editorPane = new JTextPane();
            editorPane.setEditable(false);
            editorPane.setFont(new Font("Monospaced", Font.PLAIN, SwingUtils.adjustUISize(12)));
        }

        final JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(editorPane);
        scrollPane.setBorder(null);
        return scrollPane;
    }
}
