/*
 *  Copyright 2008, 2009, 2010, 2011:
 *   Tobias Fleig (tfg[AT]online[DOT]de),
 *   Michael Haas (mekhar[AT]gmx[DOT]de),
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
 * RogMainMenuAbout.java
 *
 * Created on 27.06.2010, 18:38:35
 */

package thirteenducks.cor.mainmenu;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JScrollBar;

/**
 *
 * @author tfg
 */
public class RogMainMenuAbout extends javax.swing.JPanel {

    boolean interrupt = false;

    /** Creates new form RogMainMenuAbout */
    public RogMainMenuAbout() {
        initComponents();
        jScrollPane1.getVerticalScrollBar().addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }
            @Override
            public void mousePressed(MouseEvent e) {
                interrupt();
            }
            @Override
            public void mouseReleased(MouseEvent e) {
            }
            @Override
            public void mouseEntered(MouseEvent e) {
            }
            @Override
            public void mouseExited(MouseEvent e) {
            }

        });
    }

    public void play() {
        final JScrollBar scroll = jScrollPane1.getVerticalScrollBar();
        scroll.setValue(0);
        scroll.setEnabled(false);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    interrupt = false;
                    scroll.setEnabled(true);
                    double progress = 0.0;
                    while (true) {
                        int nextVal = (int) (progress * scroll.getMaximum());
                        if (nextVal >= scroll.getMaximum()) {
                            interrupt();
                        }
                        scroll.setValue(nextVal);
                        progress += 0.00015;
                        Thread.sleep(10);
                        if (interrupt) {
                            break;
                        }
                    }
                } catch (InterruptedException ex) {
                }
            }

        };
        Thread t = new Thread(r);
        t.start();
    }

    public void interrupt() {
        interrupt = true;
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
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        jLabel46 = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        jLabel49 = new javax.swing.JLabel();
        jLabel50 = new javax.swing.JLabel();
        jLabel51 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        jLabel53 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        jLabel55 = new javax.swing.JLabel();
        jLabel56 = new javax.swing.JLabel();
        jLabel57 = new javax.swing.JLabel();
        jLabel58 = new javax.swing.JLabel();
        jLabel59 = new javax.swing.JLabel();
        jLabel60 = new javax.swing.JLabel();
        jLabel61 = new javax.swing.JLabel();
        jLabel62 = new javax.swing.JLabel();
        jLabel63 = new javax.swing.JLabel();
        jLabel64 = new javax.swing.JLabel();
        jLabel65 = new javax.swing.JLabel();
        jLabel66 = new javax.swing.JLabel();
        jLabel67 = new javax.swing.JLabel();
        jLabel68 = new javax.swing.JLabel();
        jLabel69 = new javax.swing.JLabel();
        jLabel70 = new javax.swing.JLabel();
        jLabel71 = new javax.swing.JLabel();
        jLabel72 = new javax.swing.JLabel();
        jLabel73 = new javax.swing.JLabel();
        jLabel74 = new javax.swing.JLabel();
        jLabel75 = new javax.swing.JLabel();
        jLabel76 = new javax.swing.JLabel();
        jLabel77 = new javax.swing.JLabel();
        jLabel78 = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(583, 249));
        setLayout(null);

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jPanel1.setPreferredSize(new java.awt.Dimension(520, 1780));
        jPanel1.setRequestFocusEnabled(false);

        jLabel1.setFont(new java.awt.Font("Dialog", 2, 12));
        jLabel1.setText("13 Ducks Entertainment proudly presents:");

        jLabel2.setFont(new java.awt.Font("Dialog", 1, 36));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Centuries of Rage");

        jLabel3.setText("project management: ");

        jLabel4.setText("Tobias Fleig");

        jLabel5.setText("Timo von Wysocki");

        jLabel6.setText("programming:");

        jLabel7.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel7.setText("graphics, networking, mapeditor: ");

        jLabel9.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel9.setText("gameengine:");

        jLabel10.setText("Michael Haas");

        jLabel11.setText("Tobias Fleig");

        jLabel12.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel12.setText("lobby, mainmenu:");

        jLabel13.setText("Michael Haas");

        jLabel14.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel14.setText("mapbuilder, pathfinder:");

        jLabel15.setText("Johannes Kattinger");

        jLabel16.setText("design:");

        jLabel17.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel17.setText("head of design:");

        jLabel18.setText("Timo von Wysocki");

        jLabel19.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel19.setText("buildings, 3d-modelling, concept artist:");

        jLabel21.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel21.setText("units:");

        jLabel22.setText("Johannes Kattinger");

        jLabel23.setText("misc:");

        jLabel24.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel24.setText("balancing:");

        jLabel25.setText("Johannes Kattinger");

        jLabel26.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel26.setText("techtree: ");

        jLabel27.setText("Johannes Kattinger");

        jLabel28.setText("Timo von Wysocki");

        jLabel29.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel29.setText("soundtrack:");

        jLabel30.setText("Wolfram Schindler");

        jLabel32.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel32.setText("food logistics:");

        jLabel33.setText("Felix Schindler");

        jLabel34.setText("Michael Haas");

        jLabel36.setText("Timo von Wysocki");

        jLabel35.setText("Timo's grandma");

        jLabel37.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel37.setText("testing:");

        jLabel38.setText("Tobias Fleig");

        jLabel39.setText("Michael Haas");

        jLabel40.setText("Johannes Kattinger");

        jLabel41.setText("Timo von Wysocki");

        jLabel42.setText("Felix Schindler");

        jLabel43.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel43.setText("we DO NOT thank:");

        jLabel44.setText("Patrick Epting");

        jLabel45.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel45.setText("companys offering internet via sattelite");

        jLabel46.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel46.setText("creators of terrible techno-remixes found on youtube");

        jLabel47.setText("Jonas Haller");

        jLabel48.setText("special thanks to:");

        jLabel49.setText("our physics teacher: Rainer Neff");

        jLabel50.setText("CoR - TSG Elite");

        jLabel51.setText("Wescht");

        jLabel52.setText("the inventor of chocolate-raisins");

        jLabel53.setText("Timo's grandma");

        jLabel54.setText("Dell - for producing awesome PCs");

        jLabel55.setText("Google - for GoogleCode and SketchUp");

        jLabel56.setText("Sun Microsystems - for Java technology and NetBeans");

        jLabel57.setText("Blender Foundation");

        jLabel58.setText("The GIMP");

        jLabel59.setText("everyone making free & open-source software - ");

        jLabel60.setFont(new java.awt.Font("Dialog", 1, 18));
        jLabel60.setText("you guys rock!");

        jLabel61.setFont(new java.awt.Font("Dialog", 1, 18));
        jLabel61.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel61.setText("13 Duck Entertainment's: Centuries of Rage");

        jLabel62.setText("Licensing & Copyright:");

        jLabel63.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel63.setText("images:");

        jLabel64.setText("Creative Commons Attribution-Share Alike 3.0 Germany");

        jLabel65.setFont(new java.awt.Font("Dialog", 2, 12));
        jLabel65.setText("http://creativecommons.org/licenses/by-sa/3.0/de/");

        jLabel66.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel66.setText("everything else:");

        jLabel67.setText("GNU General Public License 3 (or later)");

        jLabel68.setFont(new java.awt.Font("Dialog", 2, 12));
        jLabel68.setText("http://www.gnu.org/licenses/gpl.html");

        jLabel69.setText("Copyright 2008, 2009, 2010:");

        jLabel70.setText("Tobias Fleig");

        jLabel71.setText("tobifleig@gmail.com");

        jLabel72.setText("Michael Haas");

        jLabel73.setText("mekhar@gmx.de");

        jLabel74.setText("Johannes Kattinger");

        jLabel75.setText("johanneskattinger@gmx.de");

        jLabel76.setText("Timo von Wysocki");

        jLabel77.setText("77Timo@gmail.com");

        jLabel78.setFont(new java.awt.Font("Dialog", 2, 12));
        jLabel78.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel78.setText("All rights reserved");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel1))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 511, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel3))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(438, 438, 438)
                .addComponent(jLabel4))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(400, 400, 400)
                .addComponent(jLabel5))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel6))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel7))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(438, 438, 438)
                .addComponent(jLabel8))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel9))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(429, 429, 429)
                .addComponent(jLabel10))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(438, 438, 438)
                .addComponent(jLabel11))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel12))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(429, 429, 429)
                .addComponent(jLabel13))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel14))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(386, 386, 386)
                .addComponent(jLabel15))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel16))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel17))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(399, 399, 399)
                .addComponent(jLabel18))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel19))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel21))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(386, 386, 386)
                .addComponent(jLabel22))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel23))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel24))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(386, 386, 386)
                .addComponent(jLabel25))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel26))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(386, 386, 386)
                .addComponent(jLabel27))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(399, 399, 399)
                .addComponent(jLabel28))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel29))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(394, 394, 394)
                .addComponent(jLabel30))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel32))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(427, 427, 427)
                .addComponent(jLabel33))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(435, 435, 435)
                .addComponent(jLabel34))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(405, 405, 405)
                .addComponent(jLabel36))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(417, 417, 417)
                .addComponent(jLabel35))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel37))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(444, 444, 444)
                .addComponent(jLabel38))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(435, 435, 435)
                .addComponent(jLabel39))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(392, 392, 392)
                .addComponent(jLabel40))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(405, 405, 405)
                .addComponent(jLabel41))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(427, 427, 427)
                .addComponent(jLabel42))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel43))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(431, 431, 431)
                .addComponent(jLabel44))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(158, 158, 158)
                .addComponent(jLabel45, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(jLabel46, javax.swing.GroupLayout.PREFERRED_SIZE, 520, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(444, 444, 444)
                .addComponent(jLabel47))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel48))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(301, 301, 301)
                .addComponent(jLabel49))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(427, 427, 427)
                .addComponent(jLabel50))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(476, 476, 476)
                .addComponent(jLabel51))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(298, 298, 298)
                .addComponent(jLabel52))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(417, 417, 417)
                .addComponent(jLabel53))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(292, 292, 292)
                .addComponent(jLabel54))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(254, 254, 254)
                .addComponent(jLabel55))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(148, 148, 148)
                .addComponent(jLabel56))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(389, 389, 389)
                .addComponent(jLabel57))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(464, 464, 464)
                .addComponent(jLabel58))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(jLabel59)
                .addGap(39, 39, 39)
                .addComponent(jLabel60))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel61, javax.swing.GroupLayout.PREFERRED_SIZE, 517, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel62))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel63)
                .addGap(79, 79, 79)
                .addComponent(jLabel64))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(219, 219, 219)
                .addComponent(jLabel65))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel66)
                .addGap(145, 145, 145)
                .addComponent(jLabel67))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(301, 301, 301)
                .addComponent(jLabel68))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel69))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel70)
                .addGap(301, 301, 301)
                .addComponent(jLabel71))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel72)
                .addGap(316, 316, 316)
                .addComponent(jLabel73))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel74)
                .addGap(202, 202, 202)
                .addComponent(jLabel75))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel76)
                .addGap(273, 273, 273)
                .addComponent(jLabel77))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel78, javax.swing.GroupLayout.PREFERRED_SIZE, 517, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel1)
                .addGap(6, 6, 6)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addGap(4, 4, 4)
                .addComponent(jLabel4)
                .addGap(7, 7, 7)
                .addComponent(jLabel5)
                .addGap(13, 13, 13)
                .addComponent(jLabel6)
                .addGap(4, 4, 4)
                .addComponent(jLabel7)
                .addGap(6, 6, 6)
                .addComponent(jLabel8)
                .addGap(21, 21, 21)
                .addComponent(jLabel9)
                .addGap(6, 6, 6)
                .addComponent(jLabel10)
                .addGap(4, 4, 4)
                .addComponent(jLabel11)
                .addGap(4, 4, 4)
                .addComponent(jLabel12)
                .addGap(6, 6, 6)
                .addComponent(jLabel13)
                .addGap(4, 4, 4)
                .addComponent(jLabel14)
                .addGap(6, 6, 6)
                .addComponent(jLabel15)
                .addGap(16, 16, 16)
                .addComponent(jLabel16)
                .addGap(4, 4, 4)
                .addComponent(jLabel17)
                .addGap(6, 6, 6)
                .addComponent(jLabel18)
                .addGap(4, 4, 4)
                .addComponent(jLabel19)
                .addGap(27, 27, 27)
                .addComponent(jLabel21)
                .addGap(6, 6, 6)
                .addComponent(jLabel22)
                .addGap(16, 16, 16)
                .addComponent(jLabel23)
                .addGap(4, 4, 4)
                .addComponent(jLabel24)
                .addGap(6, 6, 6)
                .addComponent(jLabel25)
                .addGap(4, 4, 4)
                .addComponent(jLabel26)
                .addGap(6, 6, 6)
                .addComponent(jLabel27)
                .addGap(4, 4, 4)
                .addComponent(jLabel28)
                .addGap(4, 4, 4)
                .addComponent(jLabel29)
                .addGap(6, 6, 6)
                .addComponent(jLabel30)
                .addGap(4, 4, 4)
                .addComponent(jLabel32)
                .addGap(6, 6, 6)
                .addComponent(jLabel33)
                .addGap(4, 4, 4)
                .addComponent(jLabel34)
                .addGap(4, 4, 4)
                .addComponent(jLabel36)
                .addGap(4, 4, 4)
                .addComponent(jLabel35)
                .addGap(4, 4, 4)
                .addComponent(jLabel37)
                .addGap(6, 6, 6)
                .addComponent(jLabel38)
                .addGap(4, 4, 4)
                .addComponent(jLabel39)
                .addGap(4, 4, 4)
                .addComponent(jLabel40)
                .addGap(4, 4, 4)
                .addComponent(jLabel41)
                .addGap(4, 4, 4)
                .addComponent(jLabel42)
                .addGap(16, 16, 16)
                .addComponent(jLabel43)
                .addGap(6, 6, 6)
                .addComponent(jLabel44)
                .addGap(4, 4, 4)
                .addComponent(jLabel45)
                .addGap(4, 4, 4)
                .addComponent(jLabel46)
                .addGap(4, 4, 4)
                .addComponent(jLabel47)
                .addGap(16, 16, 16)
                .addComponent(jLabel48)
                .addGap(4, 4, 4)
                .addComponent(jLabel49)
                .addGap(4, 4, 4)
                .addComponent(jLabel50)
                .addGap(4, 4, 4)
                .addComponent(jLabel51)
                .addGap(4, 4, 4)
                .addComponent(jLabel52)
                .addGap(4, 4, 4)
                .addComponent(jLabel53)
                .addGap(16, 16, 16)
                .addComponent(jLabel54)
                .addGap(4, 4, 4)
                .addComponent(jLabel55)
                .addGap(4, 4, 4)
                .addComponent(jLabel56)
                .addGap(4, 4, 4)
                .addComponent(jLabel57)
                .addGap(4, 4, 4)
                .addComponent(jLabel58)
                .addGap(16, 16, 16)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jLabel59))
                    .addComponent(jLabel60))
                .addGap(36, 36, 36)
                .addComponent(jLabel61)
                .addGap(12, 12, 12)
                .addComponent(jLabel62)
                .addGap(4, 4, 4)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel63)
                    .addComponent(jLabel64))
                .addGap(4, 4, 4)
                .addComponent(jLabel65)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel66)
                    .addComponent(jLabel67))
                .addGap(4, 4, 4)
                .addComponent(jLabel68)
                .addGap(18, 18, 18)
                .addComponent(jLabel69)
                .addGap(4, 4, 4)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel70)
                    .addComponent(jLabel71))
                .addGap(4, 4, 4)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel72)
                    .addComponent(jLabel73))
                .addGap(4, 4, 4)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel74)
                    .addComponent(jLabel75))
                .addGap(4, 4, 4)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel76)
                    .addComponent(jLabel77))
                .addGap(10, 10, 10)
                .addComponent(jLabel78))
        );

        jScrollPane1.setViewportView(jPanel1);

        add(jScrollPane1);
        jScrollPane1.setBounds(12, 12, 559, 225);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel69;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel70;
    private javax.swing.JLabel jLabel71;
    private javax.swing.JLabel jLabel72;
    private javax.swing.JLabel jLabel73;
    private javax.swing.JLabel jLabel74;
    private javax.swing.JLabel jLabel75;
    private javax.swing.JLabel jLabel76;
    private javax.swing.JLabel jLabel77;
    private javax.swing.JLabel jLabel78;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

}