/*
 *  Copyright 2008, 2009, 2010, 2011:
 *   Tobias Fleig (tfg[AT]online[DOT]de),
 *   Michael Hase (mekhar[AT]gmx[DOT]de),
 *   Johannes Kattinger (johanneskattinger[AT]gmx[DOT]de)
 *
 *  - All rights reserved -
 *
 *
 *  This file is part of Centuries of Rage.
 *
 *  Centuries of Rage is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Centuries of Rage is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Centuries of Rage.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/*
 * RogMainMenuJoinServerControl.java
 *
 * Created on 04.01.2010, 20:05:13
 */
package thirteenducks.cor.mainmenu;

import java.util.Arrays;
import thirteenducks.cor.game.client.ClientCore;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import javax.swing.ComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListDataListener;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.newdawn.slick.*;

/**
 *
 * @author Michael
 */
public class RogMainMenuJoinServerControl extends javax.swing.JPanel {

    RogMainMenu mainmenu;   // Hauptmenü-Referenz
    DisplayMode[] modi;
    DisplayMode[] sorted;
    DisplayMode[] fullfilter;
    HashMap cfg;
    File thecfgfile;        // die config-file

    /** Creates new form RogMainMenuJoinServerControl */
    public RogMainMenuJoinServerControl() {
        initComponents();
        try {
            modi = Display.getAvailableDisplayModes();
            sorted = sortDisplayModes(filterList(sortDisplayModes(modi).toArray(new DisplayMode[1]))).toArray(new DisplayMode[1]);
            fullfilter = filterFullscreen(sorted);
            jComboBox1.setModel(new ComboBoxModel() {

                DisplayMode sel = null;
                DisplayMode[] data = fullfilter;

                @Override
                public void setSelectedItem(Object anItem) {
                    sel = (DisplayMode) anItem;
                }

                @Override
                public Object getSelectedItem() {
                    return sel;
                }

                @Override
                public int getSize() {
                    return data.length;
                }

                @Override
                public Object getElementAt(int index) {
                    return (data[index]);
                }

                @Override
                public void addListDataListener(ListDataListener l) {
                }

                @Override
                public void removeListDataListener(ListDataListener l) {
                }
            });
            jComboBox1.setRenderer(new ListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    if (value != null) {
                        String val = ((DisplayMode) value).toString();
                        return new JLabel(val.substring(0, val.lastIndexOf(" x")));
                    } else {
                        return new JLabel();
                    }
                }
            });
            jComboBox2.setModel(new ComboBoxModel() {

                DisplayMode sel = null;
                DisplayMode[] data = sorted;

                @Override
                public void setSelectedItem(Object anItem) {
                    sel = (DisplayMode) anItem;
                }

                @Override
                public Object getSelectedItem() {
                    return sel;
                }

                @Override
                public int getSize() {
                    return data.length;
                }

                @Override
                public Object getElementAt(int index) {
                    return (data[index]);
                }

                @Override
                public void addListDataListener(ListDataListener l) {
                }

                @Override
                public void removeListDataListener(ListDataListener l) {
                }
            });
            jComboBox2.setRenderer(new ListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    if (value != null) {
                        String val = ((DisplayMode) value).toString();
                        return new JLabel(val.substring(0, val.lastIndexOf(" x")));
                    } else {
                        return new JLabel();
                    }
                }
            });

            jComboBox1.setSelectedIndex(0);
            jComboBox2.setSelectedIndex(0);

        } catch (LWJGLException ex) {
            ex.printStackTrace();
        } catch (java.lang.UnsatisfiedLinkError ur) {
            JOptionPane.showMessageDialog(new JFrame(), "Can't find native librarys. See README for more information", "no native libs", JOptionPane.ERROR_MESSAGE);
            ur.printStackTrace();
        }

        readCfg();
    }

    /* Setzt die Referenz auf das Hauptmenü. Im Konstruktor kann das dank dem Netbeans-Gui-Editor nicht gemacht werden. */
    public void setMainMenuReference(RogMainMenu menu) {
        mainmenu = menu;
    }

    private void readCfg() {
        cfg = new HashMap();
        try {
            File cfgFile = new File("client_cfg.txt");
            thecfgfile = cfgFile;
            FileReader cfgReader = new FileReader(cfgFile);
            BufferedReader reader = new BufferedReader(cfgReader);
            String zeile = null;
            int i = 0; // Anzahl der Durchläufe zählen
            while ((zeile = reader.readLine()) != null) {
                if (i == 0) {
                    // Die erste Zeile überspringen
                    //   continue;
                }
                // Liest Zeile fuer Zeile, jetzt auswerten und in Variablen
                // schreiben
                int indexgleich = zeile.indexOf('='); // Istgleich suchen
                if (indexgleich == -1) {
                } else {
                    String v1 = zeile.substring(0, indexgleich); // Vor dem =
                    // rauschneiden
                    String v2 = zeile.substring(indexgleich + 1); // Nach dem
                    // =
                    // rausschneiden
                    System.out.println(v1 + " = " + v2);
                    cfg.put(v1, v2);

                }
            }
            reader.close();
        } catch (FileNotFoundException e1) {
            // cfg-Datei nicht gefunden -  egal, wird automatisch neu angelegt
            System.out.println("client_cfg.txt not found, creating new one...");
            try {
                thecfgfile.createNewFile();
            } catch (IOException ex) {
                System.out.println("[Core-Error] Failed to create client_cfg.txt .");
            }
        } catch (IOException e2) {
            // Inakzeptabel
            e2.printStackTrace();
            System.out.println("[Core-ERROR] Critical I/O ERROR!");
            System.exit(1);
        }


        // Entsprechende Werte selektieren:
        jRadioButton1.setSelected("true".equals(cfg.get("fullscreen")));
        jRadioButton2.setSelected("false".equals(cfg.get("fullscreen")));
        if (cfg.containsKey("framerate")) {
            try {
                jSpinner1.setValue(Integer.parseInt(cfg.get("framerate").toString()));
            } catch (java.lang.NumberFormatException ex) {
                jSpinner1.setValue(35);
            }
        }
        jRadioButton3.setSelected("false".equals(cfg.get("benchmark")));
        jRadioButton4.setSelected("true".equals(cfg.get("benchmark")));
        jCheckBox1.setSelected("true".equals(cfg.get("showframerate")));
        if (cfg.containsKey("playername")) {
            playernamebox.setText(cfg.get("playername").toString());
        }
        jCheckBox2.setSelected("true".equals(cfg.get("antialising")));
        jCheckBox3.setSelected("true".equals(cfg.get("rightKlickScrolling")));
        jCheckBox5.setSelected("true".equals(cfg.get("safegraphics")));

        // Den Displaymode selektieren - falls möglich
        if (cfg.containsKey("DisplayResolutionX") && cfg.containsKey("DisplayResolutionY")) {
            int tx = Integer.parseInt(cfg.get("DisplayResolutionX").toString());
            int ty = Integer.parseInt(cfg.get("DisplayResolutionY").toString());
            if (jRadioButton1.isSelected()) {
                // Vollbild
                for (int i = 0; i < fullfilter.length; i++) {
                    DisplayMode bbb = fullfilter[i];
                    if (bbb.getWidth() == tx && bbb.getHeight() == ty) {
                        // Gefunden
                        jComboBox1.setSelectedItem(bbb);
                        break;
                    }
                }
            } else {
                // Fenster
                for (int i = 0; i < sorted.length; i++) {
                    DisplayMode bbb = sorted[i];
                    if (bbb.getWidth() == tx && bbb.getHeight() == ty) {
                        // Gefunden
                        jComboBox2.setSelectedItem(bbb);
                        break;
                    }
                }
            }
        }
    }

    private void saveCfg() {
        // Noch einige Einstellungen Einlesen:
        cfg.put("DisplayResolutionX", ((DisplayMode) (jRadioButton1.isSelected() ? jComboBox1.getSelectedItem() : jComboBox2.getSelectedItem())).getWidth());
        cfg.put("DisplayResolutionY", ((DisplayMode) (jRadioButton1.isSelected() ? jComboBox1.getSelectedItem() : jComboBox2.getSelectedItem())).getHeight());
        cfg.put("fullscreen", jRadioButton1.isSelected());
        cfg.put("framerate", jSpinner1.getValue().toString());
        cfg.put("benchmark", jRadioButton4.isSelected());
        cfg.put("showframerate", jCheckBox1.isSelected());
        cfg.put("playername", playernamebox.getText());
        cfg.put("antialising", jCheckBox2.isSelected());
        cfg.put("rightKlickScrolling", jCheckBox3.isSelected());
        cfg.put("safegraphics", jCheckBox5.isSelected());

        // Jetzt rausschreiben:
        Set keys = cfg.keySet();
        try {
            FileWriter logcreator = new FileWriter("client_cfg.txt");

            for (Object o : keys) {
                // Jedes schreiben
                logcreator.append(o.toString() + "=" + cfg.get(o).toString() + '\n');
            }
            logcreator.close();
        } catch (IOException ex) {
            System.out.println("CRITICAL: ERROR WRITING CONFIGFILE!");
        }
    }

    private DisplayMode[] filterFullscreen(DisplayMode[] list) {
        ArrayList<DisplayMode> bla = new ArrayList<DisplayMode>(list.length);
        for (DisplayMode mode : list) {
            bla.add(mode);
        }
        for (int i = 0; i < bla.size(); i++) {
            if (!bla.get(i).isFullscreenCapable()) {
                bla.remove(i--);
            }
        }
        return bla.toArray(new DisplayMode[1]);
    }

    private DisplayMode[] filterList(DisplayMode[] list) {
        ArrayList<DisplayMode> bla = new ArrayList<DisplayMode>(list.length);
        bla.addAll(Arrays.asList(list));
        for (int i = 0; i < (bla.size() - 1); i++) {
            if (bla.get(i).getWidth() == bla.get(i + 1).getWidth() && bla.get(i).getHeight() == bla.get(i + 1).getHeight()) {
                bla.remove(i--);
            }
        }
        // Möglicherweise ist die Liste ziemlich leer - ein paar Standardauflösungen sollten angeboten werden (wenn auch nur für windowed)
        boolean add = true;
        int sx = 1024;
        int sy = 768;
        for (int r = 0; r < 8; r++) {
            for (DisplayMode mode : bla) {
                if (mode.getWidth() == sx && mode.getHeight() == sy) {
                    // Ist da, nicht adden
                    add = false;
                }
            }
            if (add) {
                bla.add(new DisplayMode(sx, sy));
            }
            switch (r) {
                case 0:
                    sx = 1280;
                    sy = 1024;
                    break;
                case 1:
                    sx = 1680;
                    sy = 1050;
                    break;
                case 2:
                    sx = 800;
                    sy = 600;
                    break;
                case 3:
                    sx = 1024;
                    sy = 600;
                    break;
                case 4:
                    sx = 1280;
                    sy = 800;
                    break;
                case 5:
                    sx = 1024;
                    sy = 640;
                    break;
                case 6:
                    sx = 940;
                    sy = 520;
                    break;
            }
            add = true;
        }
        return bla.toArray(new DisplayMode[1]);
    }

    private ArrayList<DisplayMode> sortDisplayModes(DisplayMode[] blub) {
        ArrayList<DisplayMode> bla = new ArrayList<DisplayMode>(blub.length);
        bla.addAll(Arrays.asList(blub));
        Collections.sort(bla, new Comparator<DisplayMode>() {

            @Override
            public int compare(DisplayMode o1, DisplayMode o2) {
                // Sortieren (in dieser Reihenfolge (groß zuerst)) nach X Y depth frequenz
                if (o1.getWidth() < o2.getWidth()) {
                    return 1;
                } else if (o1.getWidth() > o2.getWidth()) {
                    return -1;
                } else {
                    // X gleich, Y testen
                    if (o1.getHeight() < o2.getHeight()) {
                        return 1;
                    } else if (o1.getHeight() > o2.getHeight()) {
                        return -1;
                    } else {
                        // Y gleich, depth testen
                        if (o1.getBitsPerPixel() < o2.getBitsPerPixel()) {
                            return 1;
                        } else if (o1.getBitsPerPixel() > o2.getBitsPerPixel()) {
                            return -1;
                        } else {
                            // depth gleich, frequenz testen
                            if (o1.getFrequency() < o2.getFrequency()) {
                                return 1;
                            } else if (o1.getFrequency() > o2.getFrequency()) {
                                return -1;
                            } else {
                                // alles gleich
                                return 0;
                            }
                        }
                    }
                }
            }
        });
        return bla;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel4 = new javax.swing.JLabel();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        playernamebox = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jComboBox1 = new javax.swing.JComboBox();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jComboBox2 = new javax.swing.JComboBox();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jSpinner1 = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jSeparator5 = new javax.swing.JSeparator();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        jCheckBox4 = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        jCheckBox5 = new javax.swing.JCheckBox();

        jLabel4.setText("jLabel4");

        setPreferredSize(new java.awt.Dimension(584, 249));
        setLayout(null);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12));
        jLabel1.setText("Join Server");
        add(jLabel1);
        jLabel1.setBounds(12, 10, 76, 15);

        jTextField1.setText("127.0.0.1");
        jTextField1.setToolTipText("Enter the IP (or hostname within LAN's) of the server here. If you are the server 127.0.0.1 or localhost works fine.");
        add(jTextField1);
        jTextField1.setBounds(90, 40, 118, 19);

        jButton1.setText("Connect");
        jButton1.setToolTipText("Press this button to connect");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        add(jButton1);
        jButton1.setBounds(220, 40, 110, 48);

        jLabel2.setText("IP:");
        add(jLabel2);
        jLabel2.setBounds(20, 40, 18, 15);

        playernamebox.setText("unknownP");
        playernamebox.setToolTipText("Enter your name here");
        add(playernamebox);
        playernamebox.setBounds(90, 70, 118, 19);

        jLabel3.setText("Name:");
        add(jLabel3);
        jLabel3.setBounds(20, 70, 45, 15);
        add(jSeparator1);
        jSeparator1.setBounds(0, 100, 340, 10);

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        add(jSeparator2);
        jSeparator2.setBounds(340, 0, 20, 100);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.setToolTipText("Select fullscreen resolution");
        add(jComboBox1);
        jComboBox1.setBounds(131, 110, 200, 24);

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setSelected(true);
        jRadioButton1.setText("Fullscreen");
        jRadioButton1.setToolTipText("Play CoR in fullscreen mode");
        jRadioButton1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jRadioButton1StateChanged(evt);
            }
        });
        add(jRadioButton1);
        jRadioButton1.setBounds(10, 110, 98, 23);

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText("Window");
        jRadioButton2.setToolTipText("Play CoR in windowed mode");
        jRadioButton2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jRadioButton2StateChanged(evt);
            }
        });
        add(jRadioButton2);
        jRadioButton2.setBounds(10, 140, 82, 23);

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox2.setToolTipText("Select window resolution");
        jComboBox2.setEnabled(false);
        add(jComboBox2);
        jComboBox2.setBounds(131, 140, 200, 24);
        add(jSeparator3);
        jSeparator3.setBounds(0, 170, 340, 10);

        jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);
        add(jSeparator4);
        jSeparator4.setBounds(340, 100, 40, 70);

        buttonGroup2.add(jRadioButton3);
        jRadioButton3.setSelected(true);
        jRadioButton3.setText("limit framerate:");
        jRadioButton3.setToolTipText("Limit framerate (highly recommended)");
        jRadioButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton3ActionPerformed(evt);
            }
        });
        add(jRadioButton3);
        jRadioButton3.setBounds(10, 180, 135, 23);

        buttonGroup2.add(jRadioButton4);
        jRadioButton4.setText("no limit");
        jRadioButton4.setToolTipText("Don't limit framerate (recommended for benchmarks only)");
        add(jRadioButton4);
        jRadioButton4.setBounds(10, 210, 90, 23);

        jSpinner1.setToolTipText("frames per second, 35-50 recommended");
        jSpinner1.setPreferredSize(new java.awt.Dimension(28, 22));
        jSpinner1.setValue(35);
        add(jSpinner1);
        jSpinner1.setBounds(230, 180, 50, 22);

        jLabel5.setText("fps");
        add(jLabel5);
        jLabel5.setBounds(290, 180, 22, 22);

        jCheckBox1.setText("display framerate");
        jCheckBox1.setToolTipText("Show fps");
        add(jCheckBox1);
        jCheckBox1.setBounds(170, 220, 160, 20);

        jSeparator5.setOrientation(javax.swing.SwingConstants.VERTICAL);
        add(jSeparator5);
        jSeparator5.setBounds(340, 170, 70, 90);

        jCheckBox2.setText("antialising");
        jCheckBox2.setToolTipText("enable antialising (not recommended)");
        add(jCheckBox2);
        jCheckBox2.setBounds(350, 70, 100, 23);

        jCheckBox3.setText("hold right mouse to scroll");
        jCheckBox3.setToolTipText("enables right-klick-scrolling, not recommended, may not work correctly");
        add(jCheckBox3);
        jCheckBox3.setBounds(350, 90, 210, 23);

        jLabel6.setFont(new java.awt.Font("Dialog", 2, 12));
        jLabel6.setText("not recommended, buggy!");
        add(jLabel6);
        jLabel6.setBounds(380, 110, 170, 15);

        jCheckBox4.setText("Launch as AI (dev only)");
        add(jCheckBox4);
        jCheckBox4.setBounds(350, 130, 240, 23);

        jLabel7.setFont(new java.awt.Font("Dialog", 2, 12)); // NOI18N
        jLabel7.setText("may fix graphics problems");
        add(jLabel7);
        jLabel7.setBounds(380, 30, 200, 15);

        jCheckBox5.setText("safe graphics mode");
        jCheckBox5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox5ActionPerformed(evt);
            }
        });
        add(jCheckBox5);
        jCheckBox5.setBounds(350, 10, 220, 23);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        try {
            final boolean debug = true;

            final InetAddress adress;
            try {
                adress = InetAddress.getByName(this.jTextField1.getText());
            } catch (UnknownHostException ex) {
                JOptionPane.showMessageDialog(mainmenu, "destination unreachable or invalid IP", "CoR:Warning", JOptionPane.WARNING_MESSAGE);
                jTextField1.setForeground(java.awt.Color.red);
                return;
            }
            final int port = 39264;// default port
            
            final boolean ai = this.jCheckBox4.isSelected();
            Thread r = new Thread() {

                @Override
                public void run() {
                    try {
                        saveCfg();

                        // Client als KI oder normal starten?
                        // (das letzte argument gibt an obs ne ki ist)
                        if(ai)
                        {
                            ClientCore cc = new ClientCore(debug, adress, port, playernamebox.getText(), (DisplayMode) (jRadioButton1.isSelected() ? jComboBox1.getSelectedItem() : jComboBox2.getSelectedItem()), jRadioButton1.isSelected(), cfg, true);
                        }
                        else
                        {
                            ClientCore cc = new ClientCore(debug, adress, port, playernamebox.getText(), (DisplayMode) (jRadioButton1.isSelected() ? jComboBox1.getSelectedItem() : jComboBox2.getSelectedItem()), jRadioButton1.isSelected(), cfg, false);
                        }
                        
                        
                    } catch (RuntimeException ru) {
                        // Der Netcontroller schmeißt das, wenn er nicht connecten kann.
                        ru.printStackTrace();
                        JOptionPane.showMessageDialog(mainmenu, ru.getMessage(), "CoR:Error", JOptionPane.ERROR_MESSAGE);
                        // Kommando zurück
                        RogMainMenuJoinServerControl.this.setVisible(true);
                        mainmenu.setVisible(true);
                    } catch (SlickException ex) {
                        System.out.println("CRITICAL ERROR:");
                        ex.printStackTrace();
                    }
                }
            };
            this.setVisible(false);
            r.setName("Client_MAIN");
            r.start();
            mainmenu.setVisible(false);
        } catch (NoClassDefFoundError er) {
            JOptionPane.showMessageDialog(mainmenu, "Cannot find library: lib/slick.jar", "CoR:NoLibs", JOptionPane.ERROR_MESSAGE);
            System.exit(2);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jRadioButton1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jRadioButton1StateChanged
        jComboBox1.setEnabled(jRadioButton1.isSelected());
    }//GEN-LAST:event_jRadioButton1StateChanged

    private void jRadioButton2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jRadioButton2StateChanged
        jComboBox2.setEnabled(jRadioButton2.isSelected());
    }//GEN-LAST:event_jRadioButton2StateChanged

    private void jRadioButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton3ActionPerformed

    private void jCheckBox5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBox5ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField playernamebox;
    // End of variables declaration//GEN-END:variables
}
