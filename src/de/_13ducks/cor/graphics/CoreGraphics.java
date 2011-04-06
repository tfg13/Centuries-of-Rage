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
package de._13ducks.cor.graphics;

import de._13ducks.cor.game.Bullet;
import de._13ducks.cor.game.client.ClientCore;
import de._13ducks.cor.game.Building;
import de._13ducks.cor.game.Unit;
import de._13ducks.cor.game.client.ClientCore.InnerClient;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import org.newdawn.slick.*;
import org.newdawn.slick.opengl.renderer.Renderer;
import de._13ducks.cor.game.Pauseable;
import de._13ducks.cor.graphics.input.CoRInput;
import de._13ducks.cor.mainmenu.MainMenu;
import de._13ducks.cor.map.AbstractMapElement;

/**
 *
 * @author tfg
 */
public class CoreGraphics extends AppGameContainer implements Pauseable {

    private ClientCore core;
    ClientCore.InnerClient rgi;
    public GraphicsContent content;
    Dimension displaySize;
    // RogGraphicsComponent content;
    private HashMap<String, GraphicsImage> imgMap; // Hier sind alle Bilder drin
    HashMap imgInput;
    int imgInputNumber = 0;
    boolean showFrameRate = false;
    public CoRInput inputM; // InputModul
    public boolean miniMapScrolling = false;
    public boolean dragSelectionBox = false;
    public int dSBX = 0;
    public int dSBY = 0;
    private HashMap<Integer, Unit> descAnimUnit;
    // Das folgene nicht permanent (hier im Code) ändern!
    // Wer das unbedingt Testen will kann im configfile die option
    // benchmark=true
    // einsetzen
    private boolean nolimits = false; // Keine Framerate-Begrenzung/Nur Benchmark
    long starttime;             // Wann die Grafikengine gestartet wurde
    private boolean pauseMod = false;   // Für den Pausemodus
    private long pauseTime;           // Zeitpunkt des Pausierenes
    private boolean fowtrigger = false; // Trigger für flackerfreies-Fow-Updaten
    boolean fullScreenMode;
    public boolean slickReady = false;
    int framerate;
    long lastFowCalc;
    boolean seenPause = false;
    Thread slickGraphics;
    final List<Bullet> newBullets;
    public boolean rightScrollingEnabled = false;
    public boolean rightScrolling = false;
    public long rightScrollStart;
    int rightX;
    int rightY;
    double rightDX;
    double rightDY;
    int rightInitX;
    int rightInitY;
    double rightScrollSpeed = 0.5;
    Robot robot;
    int imgLoadCounter = 0;
    private DisplayMode[] modi;
    private DisplayMode[] sorted;
    private DisplayMode[] fullfilter;
    private MainMenu mainmenu;

    private CoreGraphics(ClientCore.InnerClient inner, Dimension size, boolean fullScreen) throws SlickException {
        super(new GraphicsContent(), size.width, size.height, fullScreen);
        content = (GraphicsContent) super.game;
        rgi = inner; // Die Innere Klasse übernehmen
        displaySize = size;
        newBullets = Collections.synchronizedList(new ArrayList<Bullet>());
    }

    public CoreGraphics(HashMap<String, String> cfgvalues, ClientCore core) throws SlickException, LWJGLException {
        super(new GraphicsContent());
        content = (GraphicsContent) super.game;
        this.core = core;
        newBullets = Collections.synchronizedList(new ArrayList<Bullet>());
        // Bildgröße konfigurieren
        modi = Display.getAvailableDisplayModes();
        sorted = sortDisplayModes(filterList(sortDisplayModes(modi).toArray(new DisplayMode[1]))).toArray(new DisplayMode[1]);
        fullfilter = filterFullscreen(sorted);
        DisplayMode myMode = findInitialDisplayMode(cfgvalues);
        super.setDisplayMode(myMode.getWidth(), myMode.getHeight(), myMode.isFullscreenCapable());
        content.realPixX = myMode.getWidth();
        content.realPixY = myMode.getHeight();
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    CoreGraphics.super.start();
                } catch (SlickException ex) {
                    ex.printStackTrace();
                }
            }
        });
        t.setName("Graphics");
        t.start();
    }

    private DisplayMode[] filterFullscreen(DisplayMode[] list) {
        ArrayList<DisplayMode> bla = new ArrayList<DisplayMode>(list.length);
        bla.addAll(Arrays.asList(list));
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

    /**
     * Läd alle für die Darstellung des Splashscreens notwenigen Komponenten
     */
    public void loadSplash() {
        try {
            // Ladegrafik lesen
            content.duckslogo = new Image("img/game/13ducks.png");

        } catch (org.newdawn.slick.SlickException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Läd alle Bilder, die laut der preload-Datei vor dem Hauptmenu geladen werden müssen
     * Läd außerdem den Font-Manager
     */
    public void loadPreMain() {
        BufferedReader reader = null;
        try {
            imgMap = new HashMap();
            reader = new BufferedReader(new FileReader(new File("img/preload")));
            String read = null;
            while ((read = reader.readLine()) != null) {
                if (!read.startsWith("#")) {
                    // Dieses Bild einlesen
                    File imgFile = new File(read);
                    if (imgFile.canRead() && read.endsWith(".png")) {
                        try {
                            Image img = new org.newdawn.slick.Image(imgFile.getPath());
                            GraphicsImage tempImage = new GraphicsImage(img);
                            tempImage.setImageName(imgFile.getPath());
                            // Auf jeden Fall für das Grafikmodul behalten
                            imgMap.put(imgFile.getPath(), tempImage); // Dazu machen
                            if (tempImage.getImageName().contains("/")) {
                                tempImage = new GraphicsImage(img);
                                tempImage.setImageName(imgFile.getPath().replace('/', '\\'));
                                imgMap.put(tempImage.getImageName(), tempImage);
                            } else {
                                tempImage = new GraphicsImage(img);
                                tempImage.setImageName(imgFile.getPath().replace('\\', '/'));
                                imgMap.put(tempImage.getImageName(), tempImage);
                            }
                        } catch (SlickException sl) {
                            sl.printStackTrace();
                        }
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        FontManager.initFonts(content.realPixX, content.realPixY);
    }

    /**
     * Initialisiert das Hauptmenu
     */
    void initMainMenu() {
        mainmenu = new MainMenu(content.realPixX, content.realPixY, core, this.imgMap);
        getMainmenu().init(this);
    }

    /**
     * Das Hauptmenu ist fertig, wir sind mit einem Server verbunden, jetzt durchstarten und das spiel normal weiter laden.
     */
    public void initGame() {
    }

    /**
     * Initialisiert das Inputsystem für das Hauptmenue
     */
    void initInput() {
        input.addMouseListener(new MouseListener() {

            public void mouseWheelMoved(int i) {
            }

            public void mouseClicked(int i, int i1, int i2, int i3) {
                getMainmenu().mouseClicked(i, i1, i2, i3);
            }

            public void mousePressed(int i, int i1, int i2) {
            }

            public void mouseReleased(int i, int i1, int i2) {
            }

            public void mouseMoved(int i, int i1, int i2, int i3) {
                getMainmenu().mouseMoved(i2, i3);
            }

            public void mouseDragged(int i, int i1, int i2, int i3) {
            }

            public void setInput(Input input) {
            }

            public boolean isAcceptingInput() {
                return true;
            }

            public void inputEnded() {
            }

            public void inputStarted() {
            }
        });

        input.addKeyListener(new KeyListener() {

            public void keyPressed(int i, char c) {
                getMainmenu().keyPressed(i, c);
            }

            public void keyReleased(int i, char c) {
            }

            public void setInput(Input input) {
            }

            public boolean isAcceptingInput() {
                return true;
            }

            public void inputEnded() {
            }

            public void inputStarted() {
            }
        });
    }

    public void initModule() {
        this.setLoadStatus(4);
        // Init-Code
        // MUSS AUFGERUFEN WERDEN, SONST FUNKTIONIERT DAS MODUL NICHT!!!!
        // MUSS NACH DEM INIT DES MAPMODULS AUFGERUFEN WERDEN, SONST FUNKTIONIEREN DIE ANIMATIONEN NICHT!
        rgi.logger("[Graphics] Loading graphics...");
        // Bilder reinladen
        inputM = new CoRInput(rgi, this.getInput());
        rgi.logger("[Graphics] Loading images...");
        // Bilder aus dem /img ordner laden
        // Haupt-Zuordnungsdatei öffnen (types.list)
        File imgtypes = new File("img/types.list");
        imgInput = new HashMap();
        File f3 = null;
        try {
            FileReader imgtypereader = new FileReader(imgtypes);
            BufferedReader itr = new BufferedReader(imgtypereader);
            String inputv;
            while ((inputv = itr.readLine()) != null) {
                // Zeile für Zeile lesen
                // Verifizieren, dass Datei da ist:
                inputv = "img/" + inputv;
                File f = new File(inputv);
                if (f.isDirectory()) {
                    // Ok, ist da hinzufügen
                    imgInput.put(imgInputNumber, inputv);
                    imgInputNumber++;
                } else {
                    // Datei nicht da, ignorieren, könnte ein Kommentar sein
                }
            }
            imgtypereader.close();

            final ArrayList<String> blackList = new ArrayList<String>();
            // Blacklist einlesen
            itr = new BufferedReader(new FileReader(new File("img/blacklist")));
            inputv = null;
            while ((inputv = itr.readLine()) != null) {
                if (!inputv.startsWith("#")) {
                    blackList.add(inputv);
                }
            }

            // Anzahl der Bilder berechnen:
            int total = 0;
            ArrayList<File[]> folders = new ArrayList<File[]>();
            for (int i = 0; i < imgInputNumber; i++) {
                String temp = imgInput.get(i).toString();
                File folder = new File(temp);
                if (folder.isDirectory()) {
                    File[] images = folder.listFiles(new FileFilter() {

                        @Override
                        public boolean accept(File pathname) {
                            if (pathname.getName().endsWith(".png")) {
                                return !blackList.contains(pathname.getPath());
                            }
                            return false;
                        }
                    });
                    folders.add(images);
                    total += images.length;
                }
            }

            initImageLoadStatus(total);


            // Tatsächlich einlesen
            int counter = 0;
            int gcCounter = 0;


            for (File[] list : folders) {
                for (File image : list) {
                    if (image.exists() && image.isFile()) { // Datei vorhanden?
                        imgLoadCounter++;
                        counter++;
                        gcCounter++;
                        setImageLoadStatus(counter);
                        // Ok, hinzufügen
                        // Als Image reinladen, dann wird es direkt in den Ram geladen
                        Image img = new org.newdawn.slick.Image(image.getPath());
                        GraphicsImage tempImage = new GraphicsImage(img);
                        tempImage.setImageName(image.getPath());
                        // Auf jeden Fall für das Grafikmodul behalten
                        imgMap.put(image.getPath(), tempImage); // Dazu machen
                        if (tempImage.getImageName().contains("/")) {
                            tempImage = new GraphicsImage(img);
                            tempImage.setImageName(image.getPath().replace('/', '\\'));
                            imgMap.put(tempImage.getImageName(), tempImage);
                        } else {
                            tempImage = new GraphicsImage(img);
                            tempImage.setImageName(image.getPath().replace('\\', '/'));
                            imgMap.put(tempImage.getImageName(), tempImage);
                        }
                        rgi.logger("[Graphics][LoadImage]: " + tempImage.getImageName());
                        // Eventuell GC abhandeln
                        if (gcCounter >= 50) {
                            System.gc();
                            gcCounter = 0;
                        }
                    }
                }

            }

        } catch (FileNotFoundException ex) {
            // Datei nicht gefunden!
            rgi.logger("[Graphics][ERROR]File not found! Reason:");
            rgi.logger(ex);
        } catch (IOException ex) {
            // Sollte nicht auftreten...
            rgi.logger("[Graphics][ERROR]I/O ERROR, Reason:");
            rgi.logger(ex);
            rgi.logger("infile:");
            rgi.logger(f3.toString());
        } catch (SlickException ex) {
            // Sollte nicht auftreten...
            rgi.logger("[Graphics][ERROR]I/O ERROR, Reason:");
            rgi.logger(ex);
            rgi.logger("infile:");
            rgi.logger(f3.toString());
        }
        // Grafiken laden abgeschlossen
        if ("true".equals(rgi.configs.get("showframerate")) || rgi.isInDebugMode()) {
            // Framerate an
            this.setShowFPS(true);
        } else {
            this.setShowFPS(false);
        }
        if ("true".equals(rgi.configs.get("rightKlickScrolling"))) {
            this.rightScrollingEnabled = true;
        }
        content.setImageMap(imgMap);
        importColorableMarkers();
        try {
            content.colModeImage = new GraphicsImage(new Image("img/notinlist/editor/colmode.png"));
        } catch (SlickException ex) {
            rgi.logger(ex);
        }
        content.appendCoreInner(rgi);


        importHuds();
        rgi.logger("[Graphics]: Importing bullets");
        importBullets();
        content.setFogofwar(true);
        // Will der User etwa den total Verrückten Benchmark laufen lassen?
        if ("true".equals(rgi.configs.get("rightKlickScrolling"))) {
            // Vermerken
            rgi.logger("[Graphics][Init]: Launching benchmark...");
            // Jetzt setzen
            this.setShowFPS(true);
            nolimits = true;
        }
        this.setLoadStatus(5);
        //readAnimations();
        rgi.logger("[Graphics]: RogGraphics is ready to rock! (init completed)");
        this.triggerStatusWaiting();

    }

    public void setLoadStatus(int status) {
        content.loadStatus = status;
        content.loadWait = false;
        if (status > 2 && Thread.currentThread().equals(slickGraphics)) {
            content.paintComponent(this.getGraphics());
            Renderer.get().flush();
            Display.update();
        }
    }

    /**
     * Zählt nach jedem geladenen Bild hoch.
     * Ermöglicht ein flüssiges Animieren des Ladebalkens
     * @param imageNr
     */
    private void setImageLoadStatus(int imageNr) {
        content.imgLoadCount = imageNr;
        content.paintComponent(this.getGraphics());
        Renderer.get().flush();
        Display.update();
    }

    private void initImageLoadStatus(int total) {
        content.imgLoadTotal = total;
    }

    public void triggerLaunchError(int type) {
        content.lEtype = type;
        content.launchError = true;
        Input inputr = this.getInput();
        inputr.removeAllListeners();
        inputr.addMouseListener(new MouseListener() {

            @Override
            public void mouseWheelMoved(int change) {
            }

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                int xmin = (content.realPixX / 5);      // Startposition des Balkens
                int ymin = (int) ((content.realPixY / 10 * 2) + (content.realPixY / 2 * 0.9));
                int xmax = (content.realPixX / 5) + (content.realPixX / 5 * 3);      // Länge des Balkens
                int ymax = (content.realPixY / 10 * 2) + (content.realPixY / 2);
                if (x > xmin && x < xmax && y > ymin && y < ymax) {
                    // Ende
                    System.exit(4);
                }
            }

            @Override
            public void mousePressed(int button, int x, int y) {
            }

            @Override
            public void mouseReleased(int button, int x, int y) {
            }

            @Override
            public void mouseMoved(int oldx, int oldy, int newx, int newy) {
            }

            @Override
            public void setInput(Input input) {
            }

            @Override
            public boolean isAcceptingInput() {
                return true;
            }

            @Override
            public void inputEnded() {
            }

            @Override
            public void mouseDragged(int i, int i1, int i2, int i3) {
            }

            @Override
            public void inputStarted() {
            }
        });

        inputr.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(int key, char c) {
                if (key == Input.KEY_ENTER || key == Input.KEY_NUMPADENTER) {
                    System.exit(4);
                }
            }

            @Override
            public void keyReleased(int key, char c) {
            }

            @Override
            public void setInput(Input input) {
            }

            @Override
            public boolean isAcceptingInput() {
                return true;
            }

            @Override
            public void inputEnded() {
            }

            @Override
            public void inputStarted() {
            }
        });
        inputr.resume();
    }

    public void triggerStatusWaiting() {
        // Wir warten auf andere
        content.loadWait = true;
        content.repaint();
    }

    // Fügt gescheduelete Bullets jetzt ein
    private void manageBullets() {
        synchronized (newBullets) {
            content.allList.addAll(newBullets);
            newBullets.clear();
        }
    }

    /**
     * Liest alle Geschoss-Bildchen ein
     */
    private void importBullets() {
        File bulletfolder = new File("img/bullets");
        File[] bulletFiles = bulletfolder.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                File testFile = new File(dir.getPath() + "/" + name);
                return testFile.isFile() && (testFile.getName().endsWith(".png") || testFile.getName().endsWith(".PNG"));
            }
        });
        for (File bulletFile : bulletFiles) {
            try {
                // Einlesen:
                Image bullet = new Image(bulletFile.getPath());
                // Größe checken:
                if (bullet.getHeight() != 160 || bullet.getWidth() != 160) {
                    // Ungültig
                    System.out.println("Invalid bullet (wrong size): " + bulletFile);
                    rgi.logger("[Graphics][Error]: Invalid bullet (wrong size): " + bulletFile);
                    continue;
                }
                // 16 Einzelbilder rausschneiden
                for (int i = 0; i < 16; i++) {
                    Image frame = new Image(40, 40);
                    Graphics g2 = frame.getGraphics();
                    // Wir brauchen die aktuelle Reihe/Spalte
                    int b = i;
                    int s = 0;
                    while (b > 3) {
                        s++;
                        b -= 4;
                    }
                    g2.drawImage(bullet, 0, 0, 39, 39, b * 40, s * 40, (b * 40) + 40, (s * 40) + 40);
                    String key = bulletFile.getPath() + i;
                    imgMap.put(key.replaceAll("\\\\", "/"), new GraphicsImage(frame));
                    imgMap.put(key.replaceAll("/", "\\\\"), new GraphicsImage(frame));

                }
            } catch (SlickException ex) {
                System.out.println("Error reading bullet: " + bulletFile);
                rgi.logger("[Graphics][Error]: Error reading bullet: " + bulletFile);
            }
        }
    }

    private void importColorableMarkers() {
        // Liest die Selektionsmarkierungen ein
        try {
            GraphicsImage ti1 = new GraphicsImage(new Image("img/game/ground.png"));
            ti1.setImageName("img/game/ground.png");
            content.coloredImgMap.put("img/game/ground.png", ti1);
            GraphicsImage ti2 = new GraphicsImage(new Image("img/game/sel_s1.png"));
            ti2.setImageName("img/game/sel_s1.png");
            content.coloredImgMap.put("img/game/sel_s1.png", ti2);
            GraphicsImage ti3 = new GraphicsImage(new Image("img/game/building_defaulttarget.png"));
            ti3.setImageName("img/game/building_defaulttarget.png");
            content.coloredImgMap.put("img/game/building_defaulttarget.png", ti3);
            GraphicsImage ti4 = new GraphicsImage(new Image("img/game/sel_s2.png"));
            ti4.setImageName("img/game/sel_s2.png");
            content.coloredImgMap.put("img/game/sel_s2.png", ti4);
            GraphicsImage ti5 = new GraphicsImage(new Image("img/game/sel_s3.png"));
            ti5.setImageName("img/game/sel_s3.png");
            content.coloredImgMap.put("img/game/sel_s3.png", ti5);
        } catch (SlickException ex) {
            rgi.logger("[Graphics][Critical]: Error importing selection markers.");
            System.out.println("[Graphics][Critical]: Error importing selection markers.");
        }
    }

    private void importHuds() {
//        try {
//            // Liest alle Huds aus img/hud/ ein und schickt sie an RogGraphicsComponent
//            // Epochennummer ist Bildname
//            Image ep1 = new Image("img/hud/e1.png");
//            Image ep2 = new Image("img/hud/e2.png");
//            Image ep3 = new Image("img/hud/e3.png");
//            // Bilder geladen in Array packen und ab
//            content.huds = new Image[10];
//            content.huds[1] = ep1;
//            content.huds[2] = ep2;
//            content.huds[3] = ep3;
//        } catch (SlickException ex) {
//            System.out.println("ERROR: Can't load Huds!");
//            rgi.logger("[Graphics][Init][ERROR]: Can't load Huds!");
//            rgi.logger(ex);
//        }
    }

    /**
     * Erzwingt ein Frame-Rendern.
     * Kehrt erst zurück, nachdem der Frame auf den Bildschrim gezeichnet wurde.
     * Benötigt z.B. um während dem Animationen-Laden die Grafik flüssig arbeiten zu lassen.
     */
    public void forceFrame() {
        content.paintComponent(this.getGraphics());
        Renderer.get().flush();
        Display.update();
    }

    private void readAnimations() {
        // Animationsdaten einlesen
        rgi.logger("[Graphics][LoadAnim]: Start reading animations...");
        File f = new File("img/anim");
        if (f.exists() && f.isDirectory()) {
            // Ok, der Ordner ist schonmal da...
            // Hier trennt es sich nach Units U und Buildings B
            // TODO: Implement animations for buildings
            File u = new File("img/anim/U");
            if (u.exists() && u.isDirectory()) {
                // Gut, der Ordner ist da, jetzt die Inhalte (Ordner) einlesen
                File[] units = u.listFiles();
                for (File unit : units) {
                    if (!unit.isDirectory() || unit.getName().equals(".svn")) { // Gehört nicht dazu
                        continue;
                    }
                    System.gc();
                    // Ist es eine Nummer?
                    int desc;
                    try {
                        desc = Integer.parseInt(unit.getName());
                        // Ist das eine gültige DESC?
                        if (rgi.mapModule.isValidUnitDesc(desc)) {
                            // Ja, einlesen und eintragen
                            UnitAnimator amanager = new UnitAnimator();
                            // Inhalt genauer bestimmen:
                            ArrayList<File> contents = new ArrayList<File>(Arrays.asList(unit.listFiles()));
                            // Texturen für Idle?
                            File idleFolder = new File(unit.getPath() + "/idle");
                            if (contents.contains(idleFolder)) {
                                Image[][] tlist = new Image[10][];
                                // Richtungs-Unterordner?
                                // N
                                File nFold = new File(idleFolder.getPath() + "/N");
                                if (nFold.isDirectory()) {
                                    // Ja! - Einlesen
                                    ArrayList<Image> imglist = new ArrayList<Image>();
                                    int count = 0;
                                    while (true) {
                                        try {
                                            imglist.add(new Image(nFold.getPath() + "/" + count + ".png"));
                                            count++;
                                            forceFrame();
                                        } catch (Exception ex) {
                                            // Bild gibts nicht, dann wars das, abbrechen
                                            break;
                                        }
                                    }
                                    tlist[1] = imglist.toArray(new Image[imglist.size()]);
                                }
                                // NO
                                File noFold = new File(idleFolder.getPath() + "/NO");
                                if (noFold.isDirectory()) {
                                    // Ja! - Einlesen
                                    ArrayList<Image> imglist = new ArrayList<Image>();
                                    int count = 0;
                                    while (true) {
                                        try {
                                            imglist.add(new Image(noFold.getPath() + "/" + count + ".png"));
                                            count++;
                                            forceFrame();
                                        } catch (Exception ex) {
                                            // Bild gibts nicht, dann wars das, abbrechen
                                            break;
                                        }
                                    }
                                    tlist[2] = imglist.toArray(new Image[imglist.size()]);
                                }
                                // O
                                File oFold = new File(idleFolder.getPath() + "/O");
                                if (oFold.isDirectory()) {
                                    // Ja! - Einlesen
                                    ArrayList<Image> imglist = new ArrayList<Image>();
                                    int count = 0;
                                    while (true) {
                                        try {
                                            imglist.add(new Image(oFold.getPath() + "/" + count + ".png"));
                                            count++;
                                            forceFrame();
                                        } catch (Exception ex) {
                                            // Bild gibts nicht, dann wars das, abbrechen
                                            break;
                                        }
                                    }
                                    tlist[3] = imglist.toArray(new Image[imglist.size()]);
                                }
                                // SO
                                File soFold = new File(idleFolder.getPath() + "/SO");
                                if (soFold.isDirectory()) {
                                    // Ja! - Einlesen
                                    ArrayList<Image> imglist = new ArrayList<Image>();
                                    int count = 0;
                                    while (true) {
                                        try {
                                            imglist.add(new Image(soFold.getPath() + "/" + count + ".png"));
                                            count++;
                                            forceFrame();
                                        } catch (Exception ex) {
                                            // Bild gibts nicht, dann wars das, abbrechen
                                            break;
                                        }
                                    }
                                    tlist[4] = imglist.toArray(new Image[imglist.size()]);
                                }
                                // S
                                File sFold = new File(idleFolder.getPath() + "/S");
                                if (sFold.isDirectory()) {
                                    // Ja! - Einlesen
                                    ArrayList<Image> imglist = new ArrayList<Image>();
                                    int count = 0;
                                    while (true) {
                                        try {
                                            imglist.add(new Image(sFold.getPath() + "/" + count + ".png"));
                                            count++;
                                            forceFrame();
                                        } catch (Exception ex) {
                                            // Bild gibts nicht, dann wars das, abbrechen
                                            break;
                                        }
                                    }
                                    tlist[5] = imglist.toArray(new Image[imglist.size()]);
                                }
                                // Standard einlesen
                                ArrayList<Image> imglist = new ArrayList<Image>();
                                int count = 0;
                                while (true) {
                                    try {
                                        imglist.add(new Image(idleFolder.getPath() + "/" + count + ".png"));
                                        count++;
                                        forceFrame();
                                    } catch (Exception ex) {
                                        // Bild gibts nicht, dann wars das, abbrechen
                                        break;
                                    }
                                }
                                tlist[0] = imglist.toArray(new Image[imglist.size()]);
                                // Restliches Array füllen
                                if (fillAnimArray(tlist)) {
                                    int animframerate = readFrameRate(new File(idleFolder.getPath() + "/anim.properties"));
                                    amanager.addIdle(animframerate, tlist);
                                    rgi.logger("[Graphics][LoadAnim]: U-DESC:" + desc + "-IDLE-animation with " + tlist[0].length + " frames loaded.");
                                }
                            }
                            // Texturen für Bewegung?
                            File movingFolder = new File(unit.getPath() + "/moving");
                            if (contents.contains(movingFolder)) {
                                Image[][] tlist = new Image[10][];
                                // Richtungs-Unterordner?
                                // N
                                File nFold = new File(movingFolder.getPath() + "/N");
                                if (nFold.isDirectory()) {
                                    // Ja! - Einlesen
                                    ArrayList<Image> imglist = new ArrayList<Image>();
                                    int count = 0;
                                    while (true) {
                                        try {
                                            imglist.add(new Image(nFold.getPath() + "/" + count + ".png"));
                                            count++;
                                            forceFrame();
                                        } catch (Exception ex) {
                                            // Bild gibts nicht, dann wars das, abbrechen
                                            break;
                                        }
                                    }
                                    tlist[1] = imglist.toArray(new Image[imglist.size()]);
                                }
                                // NO
                                File noFold = new File(movingFolder.getPath() + "/NO");
                                if (noFold.isDirectory()) {
                                    // Ja! - Einlesen
                                    ArrayList<Image> imglist = new ArrayList<Image>();
                                    int count = 0;
                                    while (true) {
                                        try {
                                            imglist.add(new Image(noFold.getPath() + "/" + count + ".png"));
                                            count++;
                                            forceFrame();
                                        } catch (Exception ex) {
                                            // Bild gibts nicht, dann wars das, abbrechen
                                            break;
                                        }
                                    }
                                    tlist[2] = imglist.toArray(new Image[imglist.size()]);
                                }
                                // O
                                File oFold = new File(movingFolder.getPath() + "/O");
                                if (oFold.isDirectory()) {
                                    // Ja! - Einlesen
                                    ArrayList<Image> imglist = new ArrayList<Image>();
                                    int count = 0;
                                    while (true) {
                                        try {
                                            imglist.add(new Image(oFold.getPath() + "/" + count + ".png"));
                                            count++;
                                            forceFrame();
                                        } catch (Exception ex) {
                                            // Bild gibts nicht, dann wars das, abbrechen
                                            break;
                                        }
                                    }
                                    tlist[3] = imglist.toArray(new Image[imglist.size()]);
                                }
                                // SO
                                File soFold = new File(movingFolder.getPath() + "/SO");
                                if (soFold.isDirectory()) {
                                    // Ja! - Einlesen
                                    ArrayList<Image> imglist = new ArrayList<Image>();
                                    int count = 0;
                                    while (true) {
                                        try {
                                            imglist.add(new Image(soFold.getPath() + "/" + count + ".png"));
                                            count++;
                                            forceFrame();
                                        } catch (Exception ex) {
                                            // Bild gibts nicht, dann wars das, abbrechen
                                            break;
                                        }
                                    }
                                    tlist[4] = imglist.toArray(new Image[imglist.size()]);
                                }
                                // S
                                File sFold = new File(movingFolder.getPath() + "/S");
                                if (sFold.isDirectory()) {
                                    // Ja! - Einlesen
                                    ArrayList<Image> imglist = new ArrayList<Image>();
                                    int count = 0;
                                    while (true) {
                                        try {
                                            imglist.add(new Image(sFold.getPath() + "/" + count + ".png"));
                                            count++;
                                            forceFrame();
                                        } catch (Exception ex) {
                                            // Bild gibts nicht, dann wars das, abbrechen
                                            break;
                                        }
                                    }
                                    tlist[5] = imglist.toArray(new Image[imglist.size()]);
                                }
                                // Standard einlesen
                                ArrayList<Image> imglist = new ArrayList<Image>();
                                int count = 0;
                                while (true) {
                                    try {
                                        imglist.add(new Image(movingFolder.getPath() + "/" + count + ".png"));
                                        count++;
                                        forceFrame();
                                    } catch (Exception ex) {
                                        // Bild gibts nicht, dann wars das, abbrechen
                                        break;
                                    }
                                }
                                tlist[0] = imglist.toArray(new Image[imglist.size()]);
                                if (fillAnimArray(tlist)) {
                                    int animframerate = readFrameRate(new File(movingFolder.getPath() + "/anim.properties"));
                                    amanager.addMoving(animframerate, tlist);
                                    rgi.logger("[Graphics][LoadAnim]: U-DESC:" + desc + "-MOVING-animation with " + tlist[0].length + " frames loaded.");
                                }
                            }
                            // Texturen für Bewegung?
                            File harvFolder = new File(unit.getPath() + "/harvesting");
                            if (contents.contains(harvFolder)) {
                                Image[][] tlist = new Image[10][];
                                // Richtungs-Unterordner?
                                // N
                                File nFold = new File(harvFolder.getPath() + "/N");
                                if (nFold.isDirectory()) {
                                    // Ja! - Einlesen
                                    ArrayList<Image> imglist = new ArrayList<Image>();
                                    int count = 0;
                                    while (true) {
                                        try {
                                            imglist.add(new Image(nFold.getPath() + "/" + count + ".png"));
                                            count++;
                                            forceFrame();
                                        } catch (Exception ex) {
                                            // Bild gibts nicht, dann wars das, abbrechen
                                            break;
                                        }
                                    }
                                    tlist[1] = imglist.toArray(new Image[imglist.size()]);
                                }
                                // NO
                                File noFold = new File(harvFolder.getPath() + "/NO");
                                if (noFold.isDirectory()) {
                                    // Ja! - Einlesen
                                    ArrayList<Image> imglist = new ArrayList<Image>();
                                    int count = 0;
                                    while (true) {
                                        try {
                                            imglist.add(new Image(noFold.getPath() + "/" + count + ".png"));
                                            count++;
                                            forceFrame();
                                        } catch (Exception ex) {
                                            // Bild gibts nicht, dann wars das, abbrechen
                                            break;
                                        }
                                    }
                                    tlist[2] = imglist.toArray(new Image[imglist.size()]);
                                }
                                // O
                                File oFold = new File(harvFolder.getPath() + "/O");
                                if (oFold.isDirectory()) {
                                    // Ja! - Einlesen
                                    ArrayList<Image> imglist = new ArrayList<Image>();
                                    int count = 0;
                                    while (true) {
                                        try {
                                            imglist.add(new Image(oFold.getPath() + "/" + count + ".png"));
                                            count++;
                                            forceFrame();
                                        } catch (Exception ex) {
                                            // Bild gibts nicht, dann wars das, abbrechen
                                            break;
                                        }
                                    }
                                    tlist[3] = imglist.toArray(new Image[imglist.size()]);
                                }
                                // SO
                                File soFold = new File(harvFolder.getPath() + "/SO");
                                if (soFold.isDirectory()) {
                                    // Ja! - Einlesen
                                    ArrayList<Image> imglist = new ArrayList<Image>();
                                    int count = 0;
                                    while (true) {
                                        try {
                                            imglist.add(new Image(soFold.getPath() + "/" + count + ".png"));
                                            count++;
                                            forceFrame();
                                        } catch (Exception ex) {
                                            // Bild gibts nicht, dann wars das, abbrechen
                                            break;
                                        }
                                    }
                                    tlist[4] = imglist.toArray(new Image[imglist.size()]);
                                }
                                // S
                                File sFold = new File(harvFolder.getPath() + "/S");
                                if (sFold.isDirectory()) {
                                    // Ja! - Einlesen
                                    ArrayList<Image> imglist = new ArrayList<Image>();
                                    int count = 0;
                                    while (true) {
                                        try {
                                            imglist.add(new Image(sFold.getPath() + "/" + count + ".png"));
                                            count++;
                                            forceFrame();
                                        } catch (Exception ex) {
                                            // Bild gibts nicht, dann wars das, abbrechen
                                            break;
                                        }
                                    }
                                    tlist[5] = imglist.toArray(new Image[imglist.size()]);
                                }
                                // Standard einlesen
                                ArrayList<Image> imglist = new ArrayList<Image>();
                                int count = 0;
                                while (true) {
                                    try {
                                        imglist.add(new Image(harvFolder.getPath() + "/" + count + ".png"));
                                        count++;
                                        forceFrame();
                                    } catch (Exception ex) {
                                        // Bild gibts nicht, dann wars das, abbrechen
                                        break;
                                    }
                                }
                                tlist[0] = imglist.toArray(new Image[imglist.size()]);
                                if (fillAnimArray(tlist)) {
                                    int animframerate = readFrameRate(new File(harvFolder.getPath() + "/anim.properties"));
                                    amanager.addHarvesting(animframerate, tlist);
                                    rgi.logger("[Graphics][LoadAnim]: U-DESC:" + desc + "-HARVESTING-animation with " + tlist[0].length + " frames loaded.");
                                }
                            }
                            // Texturen für Angriff?
                            File attackingFolder = new File(unit.getPath() + "/attacking");
                            if (contents.contains(attackingFolder)) {
                                Image[][] tlist = new Image[10][];
                                // Richtungs-Unterordner?
                                // N
                                File nFold = new File(attackingFolder.getPath() + "/N");
                                if (nFold.isDirectory()) {
                                    // Ja! - Einlesen
                                    ArrayList<Image> imglist = new ArrayList<Image>();
                                    int count = 0;
                                    while (true) {
                                        try {
                                            imglist.add(new Image(nFold.getPath() + "/" + count + ".png"));
                                            count++;
                                            forceFrame();
                                        } catch (Exception ex) {
                                            // Bild gibts nicht, dann wars das, abbrechen
                                            break;
                                        }
                                    }
                                    tlist[1] = imglist.toArray(new Image[imglist.size()]);
                                }
                                // NO
                                File noFold = new File(attackingFolder.getPath() + "/NO");
                                if (noFold.isDirectory()) {
                                    // Ja! - Einlesen
                                    ArrayList<Image> imglist = new ArrayList<Image>();
                                    int count = 0;
                                    while (true) {
                                        try {
                                            imglist.add(new Image(noFold.getPath() + "/" + count + ".png"));
                                            count++;
                                            forceFrame();
                                        } catch (Exception ex) {
                                            // Bild gibts nicht, dann wars das, abbrechen
                                            break;
                                        }
                                    }
                                    tlist[2] = imglist.toArray(new Image[imglist.size()]);
                                }
                                // O
                                File oFold = new File(attackingFolder.getPath() + "/O");
                                if (oFold.isDirectory()) {
                                    // Ja! - Einlesen
                                    ArrayList<Image> imglist = new ArrayList<Image>();
                                    int count = 0;
                                    while (true) {
                                        try {
                                            imglist.add(new Image(oFold.getPath() + "/" + count + ".png"));
                                            count++;
                                            forceFrame();
                                        } catch (Exception ex) {
                                            // Bild gibts nicht, dann wars das, abbrechen
                                            break;
                                        }
                                    }
                                    tlist[3] = imglist.toArray(new Image[imglist.size()]);
                                }
                                // SO
                                File soFold = new File(attackingFolder.getPath() + "/SO");
                                if (soFold.isDirectory()) {
                                    // Ja! - Einlesen
                                    ArrayList<Image> imglist = new ArrayList<Image>();
                                    int count = 0;
                                    while (true) {
                                        try {
                                            imglist.add(new Image(soFold.getPath() + "/" + count + ".png"));
                                            count++;
                                            forceFrame();
                                        } catch (Exception ex) {
                                            // Bild gibts nicht, dann wars das, abbrechen
                                            break;
                                        }
                                    }
                                    tlist[4] = imglist.toArray(new Image[imglist.size()]);
                                }
                                // S
                                File sFold = new File(attackingFolder.getPath() + "/S");
                                if (sFold.isDirectory()) {
                                    // Ja! - Einlesen
                                    ArrayList<Image> imglist = new ArrayList<Image>();
                                    int count = 0;
                                    while (true) {
                                        try {
                                            imglist.add(new Image(sFold.getPath() + "/" + count + ".png"));
                                            count++;
                                            forceFrame();
                                        } catch (Exception ex) {
                                            // Bild gibts nicht, dann wars das, abbrechen
                                            break;
                                        }
                                    }
                                    tlist[5] = imglist.toArray(new Image[imglist.size()]);
                                }
                                // Standard einlesen
                                ArrayList<Image> imglist = new ArrayList<Image>();
                                int count = 0;
                                while (true) {
                                    try {
                                        imglist.add(new Image(attackingFolder.getPath() + "/" + count + ".png"));
                                        count++;
                                        forceFrame();
                                    } catch (Exception ex) {
                                        // Bild gibts nicht, dann wars das, abbrechen
                                        break;
                                    }
                                }
                                tlist[0] = imglist.toArray(new Image[imglist.size()]);
                                if (fillAnimArray(tlist)) {
                                    int animframerate = readFrameRate(new File(attackingFolder.getPath() + "/anim.properties"));
                                    amanager.addAttacking(animframerate, tlist);
                                    rgi.logger("[Graphics][LoadAnim]: U-DESC:" + desc + "-ATTACKING-animation with " + tlist[0].length + " frames loaded.");
                                }
                            }
                            // Texturen fürs Sterben?
                            File dieingFolder = new File(unit.getPath() + "/dieing");
                            if (contents.contains(dieingFolder)) {
                                // Ja! - Einlesen
                                ArrayList<Image> imglist = new ArrayList<Image>();
                                int count = 0;
                                while (true) {
                                    try {
                                        imglist.add(new Image(dieingFolder.getPath() + "/" + count + ".png"));
                                        count++;
                                        forceFrame();
                                    } catch (Exception ex) {
                                        // Bild gibts nicht, dann wars das, abbrechen
                                        break;
                                    }
                                }
                                if (imglist.size() > 0) {
                                    int animframerate = readFrameRate(new File(dieingFolder.getPath() + "/anim.properties"));
                                    amanager.addDieing(animframerate, imglist.toArray(new Image[imglist.size()]));
                                    rgi.logger("[Graphics][LoadAnim]: U-DESC:" + desc + "-DIEING-animation with " + count + " frames loaded.");
                                }
                            }
                            // Fertig mit einlesen - speichern
                            rgi.mapModule.insertUnitAnimator(desc, amanager);
                        }
                        // Nein
                        continue;
                    } catch (NumberFormatException ex) {
                        // Irgendwie keine Zahl
                        continue;
                    }
                }
            }

            // Buildings
            File b = new File("img/anim/B");
            if (b.exists() && b.isDirectory()) {
                // Gut, der Ordner ist da, jetzt die Inhalte (Ordner) einlesen
                File[] buildings = b.listFiles();
                for (File building : buildings) {
                    if (!building.isDirectory() || building.getName().equals(".svn")) { // Gehört nicht dazu
                        continue;
                    }
                    // Ist es eine Nummer?
                    int desc;
                    try {
                        desc = Integer.parseInt(building.getName());
                        // Ist das eine gültige DESC?
                        if (rgi.mapModule.isValidBuildingDesc(desc)) {
                            // Ja, einlesen und eintragen
                            BuildingAnimator amanager = new BuildingAnimator();
                            // Inhalt genauer bestimmen:
                            ArrayList<File> contents = new ArrayList<File>(Arrays.asList(building.listFiles()));
                            // Texturen für Idle?
                            File idleFolder = new File(building.getPath() + "/idle");
                            if (contents.contains(idleFolder)) {
                                // Ja! - Einlesen
                                ArrayList<Image> imglist = new ArrayList<Image>();
                                int count = 0;
                                while (true) {
                                    try {
                                        imglist.add(new Image(idleFolder.getPath() + "/" + count + ".png"));
                                        count++;
                                        forceFrame();
                                    } catch (Exception ex) {
                                        // Bild gibts nicht, dann wars das, abbrechen
                                        break;
                                    }
                                }
                                if (imglist.size() > 0) {
                                    int animframerate = readFrameRate(new File(idleFolder.getPath() + "/anim.properties"));
                                    amanager.addIdle(animframerate, imglist.toArray(new Image[imglist.size()]));
                                    rgi.logger("[Graphics][LoadAnim]: B-DESC:" + desc + "-IDLE-animation mit " + count + " frames loaded.");
                                }
                            }
                            // Texturen für Working?
                            File workingFolder = new File(building.getPath() + "/working");
                            if (contents.contains(workingFolder)) {
                                // Ja! - Einlesen
                                ArrayList<Image> imglist = new ArrayList<Image>();
                                int count = 0;
                                while (true) {
                                    try {
                                        imglist.add(new Image(workingFolder.getPath() + "/" + count + ".png"));
                                        count++;
                                        forceFrame();
                                    } catch (Exception ex) {
                                        // Bild gibts nicht, dann wars das, abbrechen
                                        break;
                                    }
                                }
                                if (imglist.size() > 0) {
                                    int animframerate = readFrameRate(new File(workingFolder.getPath() + "/anim.properties"));
                                    amanager.addWorking(animframerate, imglist.toArray(new Image[imglist.size()]));
                                    rgi.logger("[Graphics][LoadAnim]: B-DESC:" + desc + "-WORKING-animation with " + count + " frames loaded.");
                                }
                            }
                            // Texturen fürs sterben?
                            File dieingFolder = new File(building.getPath() + "/dieing");
                            if (contents.contains(dieingFolder)) {
                                // Ja! - Einlesen
                                ArrayList<Image> imglist = new ArrayList<Image>();
                                int count = 0;
                                while (true) {
                                    try {
                                        imglist.add(new Image(dieingFolder.getPath() + "/" + count + ".png"));
                                        count++;
                                        forceFrame();
                                    } catch (Exception ex) {
                                        // Bild gibts nicht, dann wars das, abbrechen
                                        break;
                                    }
                                }
                                if (imglist.size() > 0) {
                                    int animframerate = readFrameRate(new File(dieingFolder.getPath() + "/anim.properties"));
                                    amanager.addDieing(animframerate, imglist.toArray(new Image[imglist.size()]));
                                    rgi.logger("[Graphics][LoadAnim]: B-DESC:" + desc + "-DIEING-animation with " + count + " frames loaded.");
                                }
                            }
                            // Fertig mit einlesen - speichern
                            rgi.mapModule.insertBuildingAnimator(desc, amanager);
                        }
                        // Nein
                        continue;
                    } catch (NumberFormatException ex) {
                        // Irgendwie keine Zahl
                        continue;
                    }
                }
            }
        }
        rgi.logger("[Graphics][LoadAnim]: Finished reading animations.");
    }

    private boolean fillAnimArray(Image[][] arr) {
        // Ist überhaupt irgendetwas da?
        boolean ok = false;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != null) {
                ok = true;
                break;
            }
        }
        if (!ok) {
            // Abbrechen, es ist gar nix da
            return false;
        }
        // Füllt das AnimationArray so weit wie möglich auf
        // Muss 0 Erstellt werden?
        if (arr[0] == null || arr[0].length == 0) {
            // 0 Erstellen, versuche dafür 3,4,5,2,1
            if (arr[3] != null && arr[3][0] != null) {
                arr[0] = arr[3];
            } else if (arr[4] != null && arr[4][0] != null) {
                arr[0] = arr[4];
            } else if (arr[5] != null && arr[5][0] != null) {
                arr[0] = arr[5];
            } else if (arr[2] != null && arr[2][0] != null) {
                arr[0] = arr[2];
            } else if (arr[1] != null && arr[1][0] != null) {
                arr[0] = arr[1];
            }
        }
        // 0 Ist jetzt da, jetzt noch die vorhandenen Spiegeln
        // 2 Da?
        if (arr[2] != null && arr[2].length > 0 && arr[2][0] != null) {
            // Spiegeln nach 8
            Image[] narr = new Image[arr[2].length];
            for (int i = 0; i < arr[2].length; i++) {
                Image img = arr[2][i];
                Image newimg = img.getFlippedCopy(true, false);
                /*int lx = img.getWidth();
                for (int x = 0; x < img.getWidth(); x++) {
                for (int y = 0; y < img.getHeight(); y++) {
                // Einfach Pixel rumkopieren
                newimg.setRGB(lx - x - 1, y, img.getRGB(x, y));
                }
                } */
                narr[i] = newimg;
            }
            arr[8] = narr;
        }
        // 3 Da?
        if (arr[3] != null && arr[3].length > 0 && arr[3][0] != null) {
            // Spiegeln nach 7
            Image[] narr = new Image[arr[3].length];
            for (int i = 0; i < arr[3].length; i++) {
                Image img = arr[3][i];
                Image newimg = img.getFlippedCopy(true, false);
                /*int lx = img.getWidth();
                for (int x = 0; x < img.getWidth(); x++) {
                for (int y = 0; y < img.getHeight(); y++) {
                // Einfach Pixel rumkopieren
                newimg.setRGB(lx - x - 1, y, img.getRGB(x, y));
                }
                } */
                narr[i] = newimg;
            }
            arr[7] = narr;
        }
        // 4 Da?
        if (arr[4] != null && arr[4].length > 0 && arr[4][0] != null) {
            // Spiegeln nach 6
            Image[] narr = new Image[arr[4].length];
            for (int i = 0; i < arr[4].length; i++) {
                Image img = arr[4][i];
                Image newimg = img.getFlippedCopy(true, false);
                /*int lx = img.getWidth();
                for (int x = 0; x < img.getWidth(); x++) {
                for (int y = 0; y < img.getHeight(); y++) {
                // Einfach Pixel rumkopieren
                newimg.setRGB(lx - x - 1, y, img.getRGB(x, y));
                }
                } */
                narr[i] = newimg;
            }
            arr[6] = narr;
        }
        // Fertig.
        return true;
    }

    private int readFrameRate(File properties) {
        try {
            // Liest die Framerate aus einem gegebenen Properties-File ein.
            FileReader reader = new FileReader(properties);
            BufferedReader breader = new BufferedReader(reader);
            String zeile = null;
            while ((zeile = breader.readLine()) != null) {
                if (zeile.contains("framerate")) {
                    return Integer.parseInt(zeile.substring(zeile.indexOf("=") + 1));
                }
            }
            // Nicht gefunden, defaulting
            return 25;
        } catch (IOException ex) {
            return 25;
        } catch (NumberFormatException ex) {
            return 25;
        }
    }

    protected void initSubs() {
        // Läd die Submodule
        // Inputmodul
        rgi.logger("[Graphics]: Init Sub: RogInput...");
        inputM.initAsSub(this, content.visMap.length, content.visMap[0].length);
    }

    public void activateMap(AbstractMapElement[][] newVisMap) {
        content.setVisMap(newVisMap, rgi.mapModule.getMapSizeX(), rgi.mapModule.getMapSizeY());
        content.setPosition(0, 0);
        rgi.rogGraphics.initSubs();
    }

    /**
     * Schält den Fog of War ab, deckt also die komplette Karte auf
     */
    public void disableFoW() {
        // Nicht nochmal
        if (!content.fowDisabled) {
            content.fowDisabled = true;
            content.renderFogOfWar = false;
            // FoW-Array komplett aufdecken
            content.freeFogOfWar();
        }
    }

    public void defeated() {
        // Dieser Spieler hat verloren, das soll eingeblendet werden
        content.gameDone = 3;
        // Jetzt kann der Spieler ja ruhig alles sehen
        disableFoW();
        content.endTime = System.currentTimeMillis();
    }

    public void win() {
        // Dieser Spieler hat gewonnen, das soll eingeblendet werden
        content.gameDone = 1;
        // Jetzt kann der Spieler ja ruhig alles sehen
        disableFoW();
        content.endTime = System.currentTimeMillis();
    }

    public void done() {
        // Verloren und Ende
        disableFoW();
        content.gameDone = 2;
    }

//    public boolean clickedInSel(final int button, final int x, final int y, int clickCount) {
//        // Überprüft, ob ein Aufruf in die Einheiten-Selektions-Zone fiel
//        if (x > (content.hudX + content.hudSizeX * 0.15) && x < (content.hudX + content.hudSizeX * 0.85)) {
//            if (y > (content.realPixY * 3 / 7 + content.realPixY * 2 / 7 * 0.2) && y < (content.realPixY * 5 / 7)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public boolean clickedInOpt(int x, int y) {
//        // Überprüft, ob ein Aufruf in die Fähigkeiten-Zone fiel
//        // Auf Geschwindigkeit optimiert
//        if (x > (content.hudX + content.hudSizeX * 0.15) && x < (content.hudX + content.hudSizeX * 0.85)) {
//            if (y > (content.realPixY * 0.742714) && y < (content.realPixY * 0.9712857)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public boolean clickedInOpt(final int button, final int x, final int y, final int clickCount) {
//        // Überprüft, ob ein Aufruf in die Fähigkeiten-Zone fiel
//        // Auf Geschwindigkeit optimiert
//        if (x > (content.hudX + content.hudSizeX * 0.15) && x < (content.hudX + content.hudSizeX * 0.85)) {
//            if (y > (content.realPixY * 0.1248285) && y < (content.realPixY * 0.9712857)) {
//                return true;
//            }
//        }
//        return false;
//    }

    /*
     * Fügt ein Bullet zur Grafikengine hinzu.
     * Mehr muss nicht getan werden, Animation und Schaden werden automatisch berechnet, solange die Grafik läuft.
     * Nur scheduling, wird erst eingefügt, wenn gerade kein Frame gerendert wird.
     */
    public void addBullet(Bullet b) {
        synchronized (newBullets) {
            try {
                content.allListLock.lock();
                content.allList.add(b);
            } finally {
                content.allListLock.unlock();
            }
        }
    }

    public void addBulletB(Bullet b) {
        synchronized (newBullets) {
            try {
                content.allListLock.lock();
                content.allList.add(b);
            } finally {
                content.allListLock.unlock();
            }
        }
    }

    /**
     * Trifft letzte Vorbereitungen.
     * Benötigt ein ansonsten praktisch zu 100% geladenes Spiel
     */
    public void finalPrepare() {
        // Grafikausgabe einrichten
        content.epoche = 1;

        rgi.logger("[Graphics]: Calcing selection markers...");
        Color[] playercolors = new Color[rgi.game.playerList.size()];
        playercolors[0] = Color.white;
        for (int i = 1; i < playercolors.length; i++) {
            playercolors[i] = rgi.game.playerList.get(i).color;
        }
        content.calcColoredMaps(playercolors);
        // Nötige Variablen syncronisieren
        content.allList = rgi.mapModule.allList;
        content.buildingsChanged();
    }

    public void startRendering() {
        // Startet das Rendern, muss nach loadMap aufgerufen werden

        rgi.logger("[Graphics]: Starting renderer...");
        // Kantenglättung
        if ("true".equals(rgi.configs.get("antialising"))) {
            content.enableAntialising();
            rgi.logger("[Graphics]: Antialising active.");
        }

        // Thread für Mainloop starten
        content.startRender();
        content.modi = 3;
        try {
            // Maus mittig auf den Bildschirm setzen (funktioniert nur im Vollbild, oder wenn das Fenster noch nicht bewegt wurde..)
            robot = new Robot();
            robot.mouseMove(this.getScreenWidth() / 2, this.getScreenHeight() / 2);
        } catch (AWTException ex) {
        }


        starttime = new Date().getTime();
        seenPause = false;
    }

    public void jumpTo(int scrollX, int scrollY) {
        // Lässt die Ansicht zur Position springen, alles was größere Koordinaten hat ist drin, die angegebene Position ist links oben
        // Achtung: Garantiert nicht, dass die Position links oben auch eingestellt wird, die Map wird nicht über den Rand hinaus gescrollt....
        if ((scrollX + content.viewX) > content.sizeX) {
            scrollX = content.sizeX - content.viewX;
        }
        if ((scrollY + content.viewY) > content.sizeY) {
            scrollY = content.sizeY - content.viewY;
        }
        if (scrollX < 0) {
            scrollX = 0;
        }
        if (scrollY < 0) {
            scrollY = 0;
        }
        // Nur in 2er-Schritten scollen:
        if (scrollX % 2 == 1) {
            // Beim Rechtsklickscrollen ist die Behandlung etwas unterschiedlich, weil ints doof gerundet werden und die Ansicht dann immer nach links oben scrollen würde
            if (this.rightScrolling) {
                if (scrollX > content.positionX) {
                    scrollX++;
                } else {
                    scrollX--;
                }
                // Nicht über den Rand raus
                if (scrollX < 0) {
                    scrollX = 0;
                } else if ((scrollX + content.viewX) > content.sizeX) {
                    scrollX = content.sizeX - content.viewX;
                }
            } else {
                scrollX--;
                if (scrollX < 0) {
                    scrollX = 0;
                }
            }
        }
        if (scrollY % 2 == 1) {
            if (this.rightScrolling) {
                if (scrollY > content.positionY) {
                    scrollY++;
                } else {
                    scrollY--;
                }
                // Nicht über den Rand raus
                if (scrollY < 0) {
                    scrollY = 0;
                } else if ((scrollY + content.viewY) > content.sizeY) {
                    scrollY = content.sizeY - content.viewY;
                }
            } else {
                scrollY--;
                if (scrollY < 0) {
                    scrollY = 0;
                }
            }

        }
        content.setPosition(scrollX, scrollY);
    }

    public Dimension getPosition() {
        // Liefert die Koordinaten des OBEREN, LINKEN Feldes zurück, alles was Korrdinaten größer als das hat, ist sichtbar...
        return new Dimension(content.positionX, content.positionY);
    }

    public boolean isInSight(int vX, int vY) {
        // Prüft, ob der Benutzer das angegebene Feld gerade sehen kann
        if (vX >= content.positionX && vX < content.positionX + content.viewX) {
            if (vY >= content.positionY && vY < content.positionY + content.viewY) {
                return true;
            }
        }
        return false;
    }

    public void displayError(String s) {
        // Zeigt eine Fehlermeldung grafisch an, zum Wegklicken mit OK
        JOptionPane.showMessageDialog(new JFrame().getComponent(0),
                s,
                "Critical Error!",
                JOptionPane.ERROR_MESSAGE);
    }

    public void displayWarning(String s) {
        // Zeigt eine Warnmeldung grafisch an, zum Wegklicken mit OK
        JOptionPane.showMessageDialog(new JFrame().getComponent(0),
                s,
                "Warning!",
                JOptionPane.WARNING_MESSAGE);
    }

    public void startRightScrolling() {
        rightScrollStart = System.currentTimeMillis();
        this.setMouseGrabbed(true);
        rightX = input.getAbsoluteMouseX();
        rightY = input.getAbsoluteMouseY();
        this.rightScrolling = true;
    }

    public void stopRightScrolling() {
        this.rightScrolling = false;
        this.setMouseGrabbed(false);
    }

    private void manageRightScrolling() {
        rightDX += (input.getAbsoluteMouseX() - rightX) * rightScrollSpeed;
        rightDY += (input.getAbsoluteMouseY() - rightY) * rightScrollSpeed;
        rightX = input.getAbsoluteMouseX();
        rightY = input.getAbsoluteMouseY();
        if ((int) (rightDX) != 0 || (int) (rightDY) != 0) {
            this.jumpTo(((int) rightDX) + content.positionX, ((int) rightDY) + content.positionY);
            rightDX = 0;
            rightDY = 0;
            // Maus mittig setzen
            //robot.mouseMove(rightInitX, rightInitY);
        }
    }

    public void builingsChanged() {
        content.buildingsChanged = true;
    }

    /*
     * Setzt das Dauerhaft anzeigen der Energiebalken, wie man es aus Warcraft kennt
     * @param boolean b Balken anzeigen (true) oder nicht (false)
     */
    public void setAlwaysShowEnergyBars(boolean b) {
        content.alwaysshowenergybars = b;
    }

    public void notifyUnitDieing(final Unit unit) {
        // Muss aufgerufen werden, damit eine Einheit korrekt entfernt, aber davor noch eine Todesanimation abgespielt wird.
//        if (unit.anim != null && unit.anim.isDieingAnimated()) {
//            // Unit klinisch töten, alle Behaviour abstellen und sie unselectierbar machen
//            unit.alive = false;
//            /*    for (RogUnitBehaviour behaviour : unit.behaviours) {
//            behaviour.active = false;
//            } */
//            unit.movingtarget = null;
//            unit.isSelected = false;
//            unit.setPlayerId(0);
//
//            rgi.mapModule.setCollision(unit.position, collision.free);
//            rgi.mapModule.setUnitRef(unit.position, null, unit.playerId);
//            new Timer().schedule(new TimerTask() {
//
//                @Override
//                public void run() {
//                    try {
//                        content.allListLock.lock();
//                        content.allList.remove(unit);
//                    } finally {
//                        content.allListLock.unlock();
//                    }
//                }
//            }, unit.anim.getDieingDuration());
//        } else {
        try {
            content.allListLock.lock();
            content.allList.remove(unit);
        } finally {
            content.allListLock.unlock();
        }
        // }
    }

    public void notifyBuildingDieing(final Building building) {
//        // Muss aufgerufen werden, damit ein Gebäude korrekt entfernt, aber davor noch eine Todesanimation abgespielt wird.
//        if (building.anim != null && building.anim.isDieingAnimated()) {
//            // Gebäude klinisch töten, alle Behaviour abstellen und sie unselectierbar machen
//            building.alive = false;
//            /* for (RogUnitBehaviour behaviour : building.behaviours) {
//            behaviour.active = false;
//            } */
//            building.ready = false;
//            building.isSelected = false;
//            building.setPlayerId(0);
//            this.builingsChanged();
//            new Timer().schedule(new TimerTask() {
//
//                @Override
//                public void run() {
//                    try {
//                        content.allListLock.lock();
//                        content.allList.remove(building);
//                    } finally {
//                        content.allListLock.unlock();
//                    }
//                    // Kollsion entfernen
//                    for (int z1 = 0; z1 < building.z1; z1++) {
//                        for (int z2 = 0; z2 < building.z2; z2++) {
//                            rgi.mapModule.setCollision(building.position.X + z1 + z2, building.position.Y - z1 + z2, collision.free);
//                        }
//                    }
//                }
//            }, building.anim.getDieingDuration());
//        } else {
        try {
            content.allListLock.lock();
            content.allList.remove(building);
        } finally {
            content.allListLock.unlock();
        }
        this.builingsChanged();
        // }

    }

    public void triggerRefreshFow() {
        fowtrigger = true;
    }

    /**
     * Aufrufen, wenn sich die Epoche geändert hat.
     */
    public void epocheChanged() {
        content.epocheChanged = true;
        builingsChanged();
        // Feuer neu auf den Gebäuden verteilen
        //content.fireMan.epocheChanged(content.epoche, content.buildingList);
        // Allen mitteilen
        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 45, -2, content.epoche, 0, 0));
    }

    /*
     * Setzt das Dauerhaft anzeigen der Energiebalken, wie man es aus Warcraft kennt
     * @return boolean Balken anzeigen (true) oder nicht (false)
     */
    public boolean getAlwaysShowEnergyBars() {
        return content.alwaysshowenergybars;
    }

    public void renderMainMenu(GameContainer c, Graphics g) {
        getMainmenu().render(g);
    }

    public void renderAndCalc(GameContainer c, Graphics g) {

        // Mainloop - Frames limitieren, dazu Zeit nehmen
        Date startTime = new Date();
        if (pauseMod) {
            // Ok, habs gsehen
            content.paintComponent(g);
            seenPause = true;
        }
        if (!content.fowDisabled && (fowtrigger || startTime.getTime() - lastFowCalc > 250)) {
            content.calcFogOfWar();
            lastFowCalc = startTime.getTime();
            fowtrigger = false;
        }
        if (!seenPause) {
            content.paintComponent(g);
        }
//        if (miniMapScrolling) {
//            // Wie klick auf MiniMap behandeln
//            content.klickedOnMiniMap(content.mouseX, content.mouseY);
//        }
        // Eventuell neue Bullets adden:
        manageBullets();
        if (!this.rightScrolling) {
            // Maus-Scrollen abhandeln
            if (content.mouseX < 3) {
                content.scrollLeft();
            } else if (content.mouseX > content.realPixX - 4) {
                content.scrollRight();
            }
            if (content.mouseY < 3) {
                content.scrollUp();
            } else if (content.mouseY > content.realPixY - 4) {
                content.scrollDown();
            }
            // Rechtsklick-Scrollen
        } else {
            manageRightScrolling();
        }
        // Tastatur-Scrollen abhandeln
        if (inputM.scroll[0]) {
            content.scrollUp();
        }
        if (inputM.scroll[1]) {
            content.scrollLeft();
        }
        if (inputM.scroll[2]) {
            content.scrollDown();
        }
        if (inputM.scroll[3]) {
            content.scrollRight();
        }
        // Hatten wir das Pause gsehen?
        if (seenPause) {
            if (!pauseMod) {
                seenPause = false;
            }
        }
    }

    @Override
    public void pause() {
        pauseMod = true;
        content.pauseMode = true;
        //rgf.setIgnoreRepaint(false);
        for (int i = 0; i < content.allList.size(); i++) {
            Sprite r = content.allList.get(i);
            if (r.getClass().equals(Bullet.class)) {
                ((Bullet) r).pause();
            }
        }
    }

    @Override
    public void unpause() {
        pauseMod = false;
        content.pauseMode = false;
        for (int i = 0; i < content.allList.size(); i++) {
            Sprite r = content.allList.get(i);
            if (r.getClass().equals(Bullet.class)) {
                ((Bullet) r).unpause();
            }
        }
    }

    public long getPauseTime() {
        return pauseTime;
    }

    public void showstatistics() {
        //statisticsMod = true;
        content.gameDone = 0;
        System.out.println("AddMe: Start statistic!");
    }

    /**
     * Sucht einen Displaymode heraus, mit dem das Hauptmenu startet.
     * 2 Möglichkeiten:
     * Wenn in config angegebene und verfügbar, wird dieser verwendet.
     * Ansonsten wird die bestmögliche Vollbildauflösung genommen.
     * @param cfgvalues die Settings, hier wird die alte Einstellung rausglesen
     * @return der DisplayMode, mit dem gestartet werden soll.
     */
    private DisplayMode findInitialDisplayMode(HashMap<String, String> cfgvalues) {
        if (cfgvalues.containsKey("fullscreen")) {
            boolean fullscreen = "true".equals(cfgvalues.get("fullscreen"));
            if (cfgvalues.containsKey("DisplayResolutionX") && cfgvalues.containsKey("DisplayResolutionY")) {
                int tx = Integer.parseInt(cfgvalues.get("DisplayResolutionX").toString());
                int ty = Integer.parseInt(cfgvalues.get("DisplayResolutionY").toString());
                if (fullscreen) {
                    // Vollbild
                    for (int i = 0; i < fullfilter.length; i++) {
                        DisplayMode bbb = fullfilter[i];
                        if (bbb.getWidth() == tx && bbb.getHeight() == ty) {
                            // Gefunden
                            return bbb;
                        }
                    }
                } else {
                    // Fenster
                    for (int i = 0; i < sorted.length; i++) {
                        DisplayMode bbb = sorted[i];
                        if (bbb.getWidth() == tx && bbb.getHeight() == ty) {
                            // Gefunden
                            return bbb;
                        }
                    }
                }
            }
        }

        // Wenn wir hier hinkommen konnte keine alte Einstellung geladen werden. Dann die erste Fullscreen nehmen (falls vorhanden):
        if (fullfilter.length > 0) {
            return fullfilter[0];
        } else {
            return new DisplayMode(800, 600);
        }
    }

    public void setInner(InnerClient rgi) {
        this.rgi = rgi;
    }

    /**
     * Getter für Mainmeu
     * @return - das Hauptmenü
     */
    public MainMenu getMainmenu() {
        return mainmenu;
    }
}
