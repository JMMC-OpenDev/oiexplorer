/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer;

import com.jidesoft.swing.DefaultOverlayable;
import com.jidesoft.swing.Overlayable;
import fr.jmmc.jmcs.App;
import fr.jmmc.jmcs.Bootstrapper;
import fr.jmmc.jmcs.data.app.ApplicationDescription;
import fr.jmmc.jmcs.gui.component.ComponentResizeAdapter;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.jmcs.gui.task.TaskSwingWorkerExecutor;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.network.interop.SampCapability;
import fr.jmmc.jmcs.network.interop.SampMessageHandler;
import fr.jmmc.jmcs.gui.util.ResourceImage;
import fr.jmmc.jmcs.util.StringUtils;
import fr.jmmc.jmcs.util.concurrent.ParallelJobExecutor;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oiexplorer.gui.MainPanel;
import fr.jmmc.oiexplorer.gui.action.LoadOIDataCollectionAction;
import fr.jmmc.oiexplorer.gui.action.LoadOIFitsAction;
import fr.jmmc.oiexplorer.gui.action.NewAction;
import fr.jmmc.oiexplorer.gui.action.OIFitsExplorerExportPDFAction;
import fr.jmmc.oiexplorer.gui.action.SaveOIDataCollectionAction;
import fr.jmmc.oitools.model.OIFitsChecker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EtchedBorder;
import org.astrogrid.samp.Message;
import org.astrogrid.samp.client.SampException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the OIFitsExplorer application
 * @author mella, bourgesl
 */
public final class OIFitsExplorer extends App {

    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(OIFitsExplorer.class.getName());

    /* members */
    /** main Panel */
    private MainPanel mainPanel;
    /* Minimal size of main component */
    private static final Dimension INITIAL_DIMENSION = new java.awt.Dimension(1200, 700);

    private DefaultOverlayable overlayable = null;

    /**
     * Main entry point : use swing setup and then launch the application
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        // Start application with the command line arguments
        Bootstrapper.launchApp(new OIFitsExplorer(args));
    }

    /**
     * Return the OIFitsExplorer singleton
     * @return OIFitsExplorer singleton
     */
    public static OIFitsExplorer getInstance() {
        return (OIFitsExplorer) App.getInstance();
    }

    /**
     * Public constructor with command line arguments
     * @param args command line arguments
     */
    public OIFitsExplorer(final String[] args) {
        super(args);
    }

    /**
     * Initialize services before the GUI
     *
     * @throws IllegalStateException if the configuration files are not found or IO failure
     * @throws IllegalArgumentException if the load configuration failed
     */
    @Override
    protected void initServices() throws IllegalStateException, IllegalArgumentException {

        // Initialize tasks and the task executor :
        TaskSwingWorkerExecutor.start();

        // Initialize the parallel job executor:
        ParallelJobExecutor.getInstance();
    }

    /**
     * Initialize application objects
     *
     * @throws RuntimeException if the OifitsExplorerGui initialization failed
     */
    @Override
    protected void setupGui() throws RuntimeException {
        logger.debug("OifitsExplorerGui.setupGui() handler : enter");
        prepareFrame();
        logger.debug("OifitsExplorerGui.setupGui() handler : exit");
    }

    /**
     * Execute application body = make the application frame visible
     */
    @Override
    protected void execute() {

        SwingUtils.invokeLaterEDT(new Runnable() {
            /**
             * Show the application frame using EDT
             */
            @Override
            public void run() {
                logger.debug("OifitsExplorerGui.execute() handler called.");

                // reset OIFitsManager to fire an OIFits collection changed event to all registered listeners:
                OIFitsCollectionManager.getInstance().start();

                getFrame().setVisible(true);
            }
        });
    }

    /**
     * Hook to handle operations before closing application.
     *
     * @return should return true if the application can exit, false otherwise
     * to cancel exit.
     */
    @Override
    public boolean canBeTerminatedNow() {
        logger.debug("OifitsExplorerGui.finish() handler called.");

        // Ask the user if he wants to save modifications       
        //@TODO replace by code when save will be available.
        MessagePane.ConfirmSaveChanges result = MessagePane.ConfirmSaveChanges.Ignore;
        //MessagePane.ConfirmSaveChanges result = MessagePane.showConfirmSaveChangesBeforeClosing();

        // Handle user choice
        switch (result) {
            // If the user clicked the "Save" button, save and exit
            case Save:
                /*
                 if (this.saveAction != null) {
                 return this.saveAction.save();
                 }
                 */
                break;

            // If the user clicked the "Don't Save" button, exit
            case Ignore:
                break;

            // If the user clicked the "Cancel" button or pressed 'esc' key, don't exit
            case Cancel:
            default: // Any other case
                return false;
        }

        return true;
    }

    /**
     * Hook to handle operations when exiting application.
     * @see App#exit(int)
     */
    @Override
    public void cleanup() {
        // dispose GUI:
        if (this.mainPanel != null) {
            this.mainPanel.dispose();
        }
    }

    /**
     * Prepare the frame widgets and define its minimum size
     * @param frame
     */
    private void prepareFrame() {
        logger.debug("prepareFrame : enter");

        final JFrame frame = new JFrame();

        // initialize the actions :
        registerActions();

        frame.setTitle(ApplicationDescription.getInstance().getProgramName());

        // handle frame icon
        final Image jmmcFavImage = ResourceImage.JMMC_FAVICON.icon().getImage();
        frame.setIconImage(jmmcFavImage);

        // get screen size to adjust minimum window size :
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        if (logger.isInfoEnabled()) {
            logger.info("screen size = {} x {}", screenSize.getWidth(), screenSize.getHeight());
        }

        // hack for screens smaller than 1152x864 screens :
        final int appWidth = 950;
        final int appHeightMin = 700;
        final int appHeightPref = (screenSize.getHeight() >= 864) ? 800 : appHeightMin;

        final Dimension dim = new Dimension(appWidth, appHeightMin);
        frame.setMinimumSize(dim);
        frame.setPreferredSize(new Dimension(appWidth, appHeightPref));
        frame.addComponentListener(new ComponentResizeAdapter(dim));

        // init the main panel :
        final Container container = frame.getContentPane();
        createContent(container);

        // Handle status bar
        container.add(StatusBar.getInstance(), BorderLayout.SOUTH);

        StatusBar.show("application started.");
        App.setFrame(frame);

        App.getFrame().setPreferredSize(INITIAL_DIMENSION);
        App.getFrame().pack();

        logger.debug("prepareFrame : exit");
    }

    /**
     * Create the main content i.e. the setting panel
     */
    private void createContent(final Container container) {
        // adds the main panel in scrollPane
        this.mainPanel = new MainPanel();
        this.mainPanel.setName("mainPanel"); // Fest

//        container.add(this.mainPanel, BorderLayout.CENTER);
        overlayable = new DefaultOverlayable(mainPanel);

        container.add(overlayable, BorderLayout.CENTER);
    }

    /**
     * Create the main actions present in the menu bar
     */
    private void registerActions() {
        // File menu :
        new NewAction();
        new LoadOIFitsAction();
        new LoadOIDataCollectionAction();
        new SaveOIDataCollectionAction();
        // export PDF :
        new OIFitsExplorerExportPDFAction();

        // Edit menu :
        // Interop menu :
    }

    /**
     * Create SAMP Message handlers
     */
    @Override
    protected void declareInteroperability() {

        // Add handler to load one new oifits
        new SampMessageHandler(SampCapability.LOAD_FITS_TABLE) {
            @Override
            protected void processMessage(final String senderId, final Message message) throws SampException {
                final String url = (String) message.getParam("url");

                if (!StringUtils.isEmpty(url)) {
                    // bring this application to front and load data
                    SwingUtils.invokeLaterEDT(new Runnable() {
                        @Override
                        public void run() {
                            App.showFrameToFront();

                            final OIFitsChecker checker = new OIFitsChecker();

                            try {
                                final long startTime = System.nanoTime();

                                OIFitsCollectionManager.getInstance().loadOIFitsFile(url, checker);

                                logger.info("LoadSampOIFitsAction: duration = {} ms.", 1e-6d * (System.nanoTime() - startTime));

                            } catch (IOException ioe) {
                                MessagePane.showErrorMessage(ioe.getMessage(), ioe.getCause());
                                StatusBar.show(ioe.getMessage());
                            } finally {
                                // display validation messages anyway:
                                final String checkReport = checker.getCheckReport();
                                logger.info("validation results:\n{}", checkReport);

                                MessagePane.showMessage(checkReport);
                            }
                        }
                    });
                }
            }
        };
    }

    /**
     * Return the main panel
     * @return main panel
     */
    public MainPanel getMainPanel() {
        return mainPanel;
    }

    /**
     * Remove the given panel from the main panel's overlay
     * @param panel panel to remove from the main panel's overlay
     */
    public void removeOverlay(final JPanel panel) {
        // force repaint the parent panel (ie main panel):
        panel.setVisible(false);
        overlayable.removeOverlayComponent(panel);
//        overlayable.repaint();
    }

    /**
     * Add the given panel to the main panel's overlay
     * @param panel panel to add to the main panel's overlay
     */
    public void addOverlay(final JPanel panel) {
        overlayable.addOverlayComponent(panel, Overlayable.SOUTH_WEST);
    }

    /**
     * Create a generic progress panel (typically shown in overlay)
     * @param message message to display
     * @param progressBar progress bar to use
     * @param cancelListener optional cancel action listener
     * @return new panel
     */
    public static JPanel createProgressPanel(final String message, final JProgressBar progressBar, final ActionListener cancelListener) {
        final JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        containerPanel.setBackground(new Color(0, 0, 0, 0)); // transparent

        final JPanel progressPanel = new JPanel();
        progressPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

        progressPanel.add(new JLabel(message));

        progressBar.setStringPainted(true);
        progressPanel.add(progressBar);

        if (cancelListener != null) {
            final JButton cancelBtn = new JButton("Cancel");
            cancelBtn.addActionListener(cancelListener);
            progressPanel.add(cancelBtn);
        }

        containerPanel.add(progressPanel);

        return containerPanel;
    }

}
