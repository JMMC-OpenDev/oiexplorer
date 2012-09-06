/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.gui;

import com.jidesoft.swing.JideTabbedPane;
import fr.jmmc.jmcs.gui.util.WindowUtils;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.UIResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides one preference view containing tabbed panes.
 */
public class MainWindow extends JFrame implements ActionListener {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(MainWindow.class.getName());
    /** "Restore to Default Settings" button */
    private JButton _plusButton = null;
    /** "Save Modifications" button */
    private JButton _minusButton = null;
    private LinkedHashMap<String, JPanel> _panels;
    private JideTabbedPane _tabbedPane;
    private static int panelCounter = 0;

    /**
     * Constructor.
     * @param title window title
     */
    public MainWindow(final String title) {

        super("MainWindow");

        if (title != null) {
            setTitle(title);
        }

        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        _plusButton = new JButton("+");
        contentPane.add(_plusButton);

        // Build the tabbed pane
        _tabbedPane = new JideTabbedPane();
        _tabbedPane.setBoldActiveTab(true);
        _tabbedPane.setShowCloseButton(true);
        _tabbedPane.setUseDefaultShowCloseButtonOnTab(true);
        _tabbedPane.setTabShape(JideTabbedPane.SHAPE_ROUNDED_VSNET);
        _tabbedPane.setTabResizeMode(JideTabbedPane.RESIZE_MODE_NONE);
        _tabbedPane.setColorTheme(JideTabbedPane.COLOR_THEME_VSNET);
        _tabbedPane.setTabEditingAllowed(true);
        // TODO : setTabEditingValidator(...)
        _tabbedPane.setTabLeadingComponent(_plusButton);
        _tabbedPane.setCloseAction(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                System.out.println("Close button clicked");
            }
        });
        contentPane.add(_tabbedPane);

        // Add each preferences pane
        _panels = new LinkedHashMap<String, JPanel>();

        // only hide on close as this view is reused by the application:
        setDefaultCloseOperation(HIDE_ON_CLOSE);
    }

    public void init() {
        _plusButton.addActionListener(this);
//        _minusButton.addActionListener(this);

        WindowUtils.centerOnMainScreen(this);
        WindowUtils.setClosingKeyboardShortcuts(this);
    }

    public void restore(LinkedHashMap<String, JPanel> panels) {

        _panels.clear();
        _tabbedPane.removeAll();
        for (Map.Entry<String, JPanel> entry : panels.entrySet()) {
            final String panelName = entry.getKey();
            final JPanel panel = entry.getValue();
            addPanel(panel, panelName);
        }
    }

    /**
     * Free any resource or reference to this instance :
     * remove this instance form Preference Observers
     */
    @Override
    public void dispose() {
        _logger.debug("dispose: {}", this);

        // @TODO add deleteObserver(this) to dispose() to dereference each subview properly

        super.dispose();
    }

    /**
     * actionPerformed  -  Listener
     *
     * @param evt ActionEvent
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        _logger.trace("PreferencesView.actionPerformed");

        // If the "Restore to default settings" button has been pressed
        if (evt.getSource().equals(_plusButton)) {
            addView();
        }
    }

    /**
     * Externalize such code using introspection ?? or use Wizard ?
     */
    public void addView() {
        MainPanel tmp = new MainPanel();
        addPanel(tmp, null);
    }

    public void addPanel(final JPanel panel, final String panelName) {

        String name = "new panel" + panelCounter;
        if ((panelName != null) && (panelName.length() > 0)) {
            name = panelName;
        }

        // To correctly match deeper background color of inner tab panes
        panel.setOpaque(false);

        _tabbedPane.add(name, panel);
        _panels.put(name, panel);
        panelCounter++;

        _logger.debug("Added '{}' panel to PreferenceView tabbed pane.", name);
    }

    public void display() {
        pack();
        setVisible(true);
    }

    /**
     * Return the main panel
     * @return main panel
     */
    public MainPanel getCurrentPanel() {
        return (MainPanel) _tabbedPane.getSelectedComponent();
    }

    public static void main(String[] args) {

        LinkedHashMap<String, JPanel> panels = new LinkedHashMap<String, JPanel>();
        for (int i = 0; i < 3; i++) {
            JPanel tmp = new JPanel();
            tmp.add(new JButton("button " + i));
            panels.put("panel " + i, tmp);
        }

        final MainWindow mainWindow = new MainWindow(null);
        mainWindow.init();
        mainWindow.restore(panels);
        mainWindow.display();
    }
}
