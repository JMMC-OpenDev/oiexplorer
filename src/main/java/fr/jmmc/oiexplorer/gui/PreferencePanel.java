/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.gui;

import fr.jmmc.jmcs.data.preference.PreferencesException;
import fr.jmmc.oiexplorer.Preferences;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Observable;
import java.util.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Preferences GUI
 */
public final class PreferencePanel extends javax.swing.JPanel implements Observer {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(PreferencePanel.class.getName());

    /* members */
    /** preference singleton */
    private final Preferences myPreferences = Preferences.getInstance();

    /**
     * Creates a new PreferencePanel
     */
    public PreferencePanel() {
        initComponents();

        postInit();
    }

    /**
     * This method is useful to set the models and specific features of initialized swing components :
     * Update the combo boxes with their models
     */
    private void postInit() {

        // Set the Aspro Preferences:
        this.chartPreferencesView.setPreferences(myPreferences);

        // register this instance as a Preference Observer :
        this.myPreferences.addObserver(this);

        // update GUI
        update(null, null);

        this.jFieldTargetSep.addPropertyChangeListener("value", new PropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                final double sepNew = ((Number) jFieldTargetSep.getValue()).doubleValue();

                if (sepNew <= 0.0) {
                    // invalid value :
                    jFieldTargetSep.setValue(myPreferences.getPreferenceAsDouble(Preferences.TARGET_MATCHER_SEPARATION));
                }
                try {
                    // will fire triggerObserversNotification so update() will be called
                    myPreferences.setPreference(Preferences.TARGET_MATCHER_SEPARATION, Double.valueOf(((Number) jFieldTargetSep.getValue()).doubleValue()));
                } catch (PreferencesException pe) {
                    logger.error("property failure : ", pe);
                }
            }
        });
    }

    /**
     * Overriden method to give object identifier
     * @return string identifier
     */
    @Override
    public String toString() {
        return "PreferencesView@" + Integer.toHexString(hashCode());
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
        jPanelLayout = new javax.swing.JPanel();
        chartPreferencesView = new fr.jmmc.oiexplorer.core.gui.ChartPreferencesView();
        jPanelPrefs = new javax.swing.JPanel();
        jLabelTargetSep = new javax.swing.JLabel();
        jFieldTargetSep = new javax.swing.JFormattedTextField();
        jPanelCommonPreferencesView = new fr.jmmc.jmcs.gui.component.CommonPreferencesView();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        jPanelLayout.setLayout(new javax.swing.BoxLayout(jPanelLayout, javax.swing.BoxLayout.PAGE_AXIS));
        jPanelLayout.add(chartPreferencesView);

        jPanelPrefs.setBorder(javax.swing.BorderFactory.createTitledBorder("Matcher"));
        jPanelPrefs.setLayout(new java.awt.GridBagLayout());

        jLabelTargetSep.setText("Max target separation (as)");
        jLabelTargetSep.setToolTipText("All related values below this threshold will be flagged out (V2, T3...)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 6);
        jPanelPrefs.add(jLabelTargetSep, gridBagConstraints);

        jFieldTargetSep.setColumns(5);
        jFieldTargetSep.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.0#"))));
        jFieldTargetSep.setName("jFieldMinElev"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
        jPanelPrefs.add(jFieldTargetSep, gridBagConstraints);

        jPanelLayout.add(jPanelPrefs);
        jPanelLayout.add(jPanelCommonPreferencesView);

        jScrollPane.setViewportView(jPanelLayout);

        add(jScrollPane);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private fr.jmmc.oiexplorer.core.gui.ChartPreferencesView chartPreferencesView;
    private javax.swing.JFormattedTextField jFieldTargetSep;
    private javax.swing.JLabel jLabelTargetSep;
    private fr.jmmc.jmcs.gui.component.CommonPreferencesView jPanelCommonPreferencesView;
    private javax.swing.JPanel jPanelLayout;
    private javax.swing.JPanel jPanelPrefs;
    private javax.swing.JScrollPane jScrollPane;
    // End of variables declaration//GEN-END:variables

    /**
     * Listen to preferences changes
     * @param o Preferences
     * @param arg unused
     */
    @Override
    public void update(final Observable o, final Object arg) {
        logger.debug("Preferences updated on : {}", this);

        // read prefs to set states of GUI elements
        this.jFieldTargetSep.setValue(this.myPreferences.getPreferenceAsDouble(Preferences.TARGET_MATCHER_SEPARATION));
    }

}
