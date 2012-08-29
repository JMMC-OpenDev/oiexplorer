/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer;

import fr.jmmc.jmcs.App;
import fr.jmmc.jmcs.gui.component.ComponentResizeAdapter;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.jmcs.gui.task.TaskSwingWorkerExecutor;
import fr.jmmc.jmcs.gui.util.SwingSettings;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.resource.image.ResourceImage;
import fr.jmmc.jmcs.util.concurrent.ParallelJobExecutor;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oiexplorer.gui.MainPanel;
import fr.jmmc.oiexplorer.gui.action.LoadOIDataCollectionAction;
import fr.jmmc.oiexplorer.gui.action.LoadOIFitsAction;
import fr.jmmc.oiexplorer.gui.action.NewAction;
import fr.jmmc.oiexplorer.gui.action.OIFitsExplorerExportPDFAction;
import fr.jmmc.oiexplorer.gui.action.SaveOIDataCollectionAction;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
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

    /**
     * Main entry point : use swing setup and then start the application
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        // init swing application for science
        SwingSettings.setup();

        final long start = System.nanoTime();
        try {
            // Start application with the command line arguments
            new OIFitsExplorer(args);
        } finally {
            if (logger.isInfoEnabled()) {
                logger.info("startup : duration = {} ms.", 1e-6d * (System.nanoTime() - start));
            }
        }
    }

    /**
     * Return the OIFitsExplorer singleton
     * @return OIFitsExplorer singleton
     */
    public static OIFitsExplorer getInstance() {
        return (OIFitsExplorer) App.getSharedInstance();
    }

    /**
     * Public constructor with command line arguments
     * @param args command line arguments
     */
    public OIFitsExplorer(final String[] args) {
        super(args);
    }

    /**
     * Initialize application objects
     * @param args ignored arguments
     *
     * @throws RuntimeException if the OifitsExplorerGui initialisation failed
     */
    @Override
    protected void init(final String[] args) throws RuntimeException {
        logger.debug("OifitsExplorerGui.init() handler : enter");

        this.initServices();

        // Using invokeAndWait to be in sync with this thread :
        // note: invokeAndWaitEDT throws an IllegalStateException if any exception occurs
        SwingUtils.invokeAndWaitEDT(new Runnable() {
            /**
             * Initializes the swing components with their actions in EDT
             */
            @Override
            public void run() {
                prepareFrame(getFrame());
            }
        });

        logger.debug("OifitsExplorerGui.init() handler : exit");
    }

    /**
     * Initialize services before the GUI
     *
     * @throws IllegalStateException if the configuration files are not found or IO failure
     * @throws IllegalArgumentException if the load configuration failed
     */
    private void initServices() throws IllegalStateException, IllegalArgumentException {

        // Initialize OIFitsManager:
        OIFitsCollectionManager.getInstance();

        // Initialize tasks and the task executor :
        TaskSwingWorkerExecutor.start();

        // Initialize the parallel job executor:
        ParallelJobExecutor.getInstance();
    }

    /**
     * Execute application body = make the application frame visible
     */
    @Override
    protected void execute() {
        logger.debug("OifitsExplorerGui.execute() handler called.");

        SwingUtils.invokeLaterEDT(new Runnable() {
            /**
             * Show the application frame using EDT
             */
            @Override
            public void run() {
                logger.debug("OifitsExplorerGui.ready : handler called.");

                // reset OIFitsManager to fire an OIFits collection changed event to all registered listeners:
                OIFitsCollectionManager.getInstance().reset();

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
    protected boolean finish() {
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

        // stop the task executor :
        TaskSwingWorkerExecutor.stop();

        // stop the parallel job executor:
        ParallelJobExecutor.shutdown();

        super.cleanup();
    }

    /**
     * Prepare the frame widgets and define its minimum size
     * @param frame
     */
    private void prepareFrame(final JFrame frame) {
        logger.debug("prepareFrame : enter");

        frame.setTitle(App.getSharedApplicationDataModel().getProgramName());

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

        // handle closing by mouse :
        frame.addWindowListener(new CloseFrameAdapter());

        // previous adapter manages the windowClosing(event) :
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // init the main panel :
        createContent();

        // initialize the actions :
        registerActions();

        // Handle status bar
        getFramePanel().add(new StatusBar(), BorderLayout.SOUTH);

        StatusBar.show("application started.");

        logger.debug("prepareFrame : exit");
    }

    /**
     * Create the main content i.e. the setting panel
     */
    private void createContent() {
        // adds the main panel in scrollPane
        this.mainPanel = new MainPanel();

        getFramePanel().add(this.mainPanel, BorderLayout.CENTER);
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
        // TODO Add handler to load oifits
    }

    /**
     * Return the main panel
     * @return main panel
     */
    public MainPanel getMainPanel() {
        return this.mainPanel;
    }

    /**
     * Window adapter to handle windowClosing event.
     */
    private static final class CloseFrameAdapter extends WindowAdapter {

        @Override
        public void windowClosing(final WindowEvent e) {
            // callback on exit :
            App.quitAction().actionPerformed(null);
        }
    }
}