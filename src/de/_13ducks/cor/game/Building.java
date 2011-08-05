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
package de._13ducks.cor.game;

import de._13ducks.cor.game.client.Client;
import de._13ducks.cor.game.client.ClientCore.InnerClient;
import de._13ducks.cor.graphics.input.InteractableGameElement;
import de._13ducks.cor.networks.client.behaviour.ClientBehaviour;
import de._13ducks.cor.networks.server.behaviour.ServerBehaviour;
import java.util.ArrayList;
import java.util.List;
import de._13ducks.cor.game.client.ClientCore;
import de._13ducks.cor.game.server.Server;
import de._13ducks.cor.game.server.ServerCore;
import de._13ducks.cor.graphics.GraphicsContent;
import de._13ducks.cor.graphics.Renderer;
import de._13ducks.cor.graphics.input.SelectionMarker;
import de._13ducks.cor.networks.globalbehaviour.GlobalBehaviourProduceServer;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

/**
 * Superklasse für Gebäude allgemein
 * Gebäude sind GO's die sich nicht bewegen können.
 * Diese Implementierung (und Tochterklassen) unterstützen beliebige (quadratische (!)) Größen
 * Gebäude dieser Implementierung erscheinen zunächst als wehrlose Baustelle und müssen aufgebaut werden.
 * Einheiten (Unit) können Gebäude betreten, sofern das Gebäude über freie Slots verfügt.
 *
 * Gebäude haben während der Bauphase ein spezielles Lebensenergie-Verhalten:
 * Sie beginnen mit recht wenig, die Lebensenergie steigt während dem Bauvorgang linear an.
 * Wenn sie nicht angegriffen werden, steigt die Energie linear mit dem Baufortschritt.
 * Durch Angriffe wird der Baufortschritt jedoch nicht zurückgesetzt, das Gebäude ist dann mit weniger Energie "fertig".
 * Sollte der angesammelte Schaden die aktuelle Energie überschreiten, so gilt das Gebäude als eingerissen.
 *
 * @author tfg
 */
public class Building extends GameObject {

    /**
     * Gebäude akzeptiert alle Units
     */
    public static final int ACCEPTS_ALL = 1;
    /**
     * Gebäude akzeptiert nur Ernter
     */
    public static final int ACCEPTS_HARVESTERS_ONLY = 2;
    /**
     * Größe dieses Gebäudes in Feldern Richtung rechts oben.
     * Ausgehend von der Zuordnungsposition ganz links.
     */
    protected int z1 = 2;
    /**
     * Größe dieses Gebäudes in Feldern Richtung rechts unten.
     * Ausgehend von der Zuordnungsposition ganz links.
     */
    protected int z2 = 2;
    /**
     * Der derzeitige Baufortschritt, in 0.Prozent
     * Nur relevant, wenn lifeStatus noch auf unborn steht.
     */
    private double buildprogress;
    /**
     * Der derzeitige Eroberungsfortschritt, in 0.Prozent
     */
    private double captureprogress;
    /**
     * Die derzeitige Eroberungsrate
     */
    private double capturerate;
    /**
     * Zeitpunkt der letzten Änderung des Capturing, die vom Server gesendet wurde
     */
    private long lastcapturetime;
    /**
     * Liste mit Units, die sich derzeit in diesem Gebäude befinden.
     */
    private List<Unit> intraUnits;

    /**
     * Gibt die Anzahl freier Slots an (also wieviel Einheiten das Gebäude betreten können)
     */
    protected int maxIntra = 0;

    /**
     * Gibt an, welche Einheiten akzeptiert werden.
     */
    protected int accepts = Building.ACCEPTS_HARVESTERS_ONLY;
    /**
     * Speichert den genommenen Schaden während der Bauphase
     */
    private int damageWhileContruction;
    /**
     * Cacht alle Positionen dieses Gebäudes, weils dauernd benötigt wird.
     */
    protected Position[] positions;
    /**
     * Cacht alle Positionen, die für die Sichtbarkeitsberechnung relevant sind.
     * In der Regel sind das die 4 Ecken.
     * Wird zwischengespeichert, weils dauernd benötigt wird (bei jedem Frame)
     */
    private Position[] visPositions;
    /**
     * Wurde die Selektion schon eingetragen?
     * Muss in der Regel nur ein einiziges Mal geschehen
     */
    private boolean selectionSet = false;
    /**
     * Neutrales Gebäude?
     */
    private boolean neutral = false;
    /**
     * Welche CaptureRate hat ServerBehaviourCapture im letzten Tick übergeben?
     */
    private double lastcapturerate = 0;
    /**
     * Derzeit gehovered?
     */
    private boolean hovered = false;

    /**
     * Erzeugt ein neues Gebäude mit den gegebenen Parametern.
     * Default-Konstruktor, erzeugt im Spiel vollständig nutzbare Gebäude
     * @param newNetId die netID für dieses Gebäude
     * @param mainPos die Zuordnungsposition
     */
    public Building(int newNetId, Position mainPos, boolean neutral) {
        super(newNetId, mainPos);
        intraUnits = new ArrayList<Unit>();
        positions = new Position[z1 * z2];
        visPositions = new Position[4];
        this.neutral = neutral;
        setVisrange(7); // Default für Gebäude
        z1 = 12;
        z2 = 12;
        positions = new Position[z1 * z2];
    }

    /**
     * Erzeugt ein Platzhalter-Gebäude, das nicht direkt im Spiel verwendet werden kann, aber als Platzhalter für
     * Attribute und Fähigkeiten dient.
     */
    public Building(DescParamsBuilding params) {
        super(params);
        applyBuildingParams(params);
    }

    /**
     * Erzeugt ein neues Gebäude als eigenständige Kopie des Übergebenen.
     * Wichtige Parameter werden kopiert, Sachen die jedes Gebäude selber haben sollte nicht.
     * Wichtig: Die Position muss noch gesetzt werden, die ist Anfangs 0,0
     * @param newNetId Die netId des neuen Gebäudes
     * @param copyFrom Das Gebäude, dessen Parameter kopiert werden sollen
     */
    public Building(int newNetId, Building copyFrom) {
        super(newNetId, copyFrom);
        this.accepts = copyFrom.accepts;
        this.maxIntra = copyFrom.maxIntra;
        this.z1 = copyFrom.z1;
        this.z2 = copyFrom.z2;
        this.neutral = copyFrom.neutral;
        intraUnits = new ArrayList<Unit>();
        positions = new Position[z1 * z2];
        visPositions = new Position[4];
    }

    /**
     * Wendet die Parameterliste an (kopiert die Parameter rein)
     * @param par
     */
    private void applyBuildingParams(DescParamsBuilding par) {
        this.accepts = par.getAccepts();
        this.maxIntra = par.getMaxIntra();
        this.z1 = par.getZ1();
        this.z2 = par.getZ2();
        this.neutral = par.isNeutral();
    }

    /**
     * Liefert die Anzahl freier Slots für Einheiten.
     * @return die Anzahl freier Slots für Einheiten.
     */
    public int intraFree() {
        return maxIntra - intraUnits.size();
    }

    /**
     * Liefert die Anzahl an Einheiten, die sich derzeit in diesem Gebäude befinden.
     * @return die Anzahl an Einheiten, die sich derzeit in diesem Gebäude befinden.
     */
    public int currentIntra() {
        return intraUnits.size();
    }

    public void addIntra(Unit unit, ServerCore.InnerServer inner) {
        /* // Reinjumpen lassen
        intraUnits.add(unit);
        unit.isIntra = true;
        inner.netmap.setCollision(unit.position, collision.free);
        inner.netmap.setUnitRef(unit.position, null, unit.playerId);
        inner.netmap.unitList.remove(unit); */
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addIntra(Unit unit, ClientCore.InnerClient inner) {
        // Reinjumpen lassen
   /*     intraUnits.add(unit);
        unit.isIntra = true;
        inner.mapModule.setCollision(unit.position, collision.free);
        inner.mapModule.setUnitRef(unit.position, null, unit.playerId);
        inner.mapModule.unitList.remove(unit);
        try {
        inner.rogGraphics.content.allListLock.lock();
        inner.mapModule.allList.remove(unit);
        } finally {
        inner.rogGraphics.content.allListLock.unlock();
        }
        
        inner.rogGraphics.triggerUpdateHud();
        ((AbilityIntraManager) this.getAbility(-2)).updateIntra();
        this.isWorking = true; */
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeUnit(ServerCore.InnerServer inner) {
        /*    // Die Erstbeste nehmen
        Unit jump = intraUnits.get(0);
        intraUnits.remove(0);
        jump.isIntra = false;
        // Wenns geht, dann an die alte Position
        Position newpos = jump.position;
        if (inner.netmap.isGroundColliding(jump.position)) {
        newpos = this.getBestEdge(newpos, inner).aroundMe(0, inner);
        }
        jump.position = newpos;
        inner.netmap.setCollision(newpos, collision.occupied);
        inner.netmap.setUnitRef(newpos, jump, jump.playerId);
        inner.netmap.unitList.add(jump);
        // Broadcasten
        inner.netctrl.broadcastDATA(inner.packetFactory((byte) 42, this.netID, jump.netID, jump.position.X, jump.position.Y)); */
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeAll(ServerCore.InnerServer inner) {
        /*  while (!intraUnits.isEmpty()) {
        this.removeUnit(inner);
        } */
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeUnit(Unit unit, Position pos, ClientCore.InnerClient inner) {
        /* // Unit löschen
        intraUnits.remove(unit);
        // Auf Map setzen
        unit.isIntra = false;
        unit.position = pos;
        inner.mapModule.setCollision(position, collision.blocked);
        inner.mapModule.setUnitRef(pos, unit, unit.playerId);
        inner.mapModule.unitList.add(unit);
        try {
        inner.rogGraphics.content.allListLock.lock();
        inner.mapModule.allList.add(unit);
        } finally {
        inner.rogGraphics.content.allListLock.unlock();
        }
        inner.rogGraphics.triggerUpdateHud();
        ((AbilityIntraManager) this.getAbility(-2)).updateIntra();
        if (intraUnits.isEmpty()) {
        this.isWorking = false;
        } */
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public GameObject getCopy(int newNetId) {
        return new Building(newNetId, this);
    }

    @Override
    public boolean selectable() {
        return true;
    }

    @Override
    public boolean isSelectableByPlayer(int playerId) {
        // Im Debugmode is es erlaubt selectAll=true zu setzen, dann darf man alle selektieren und steuern
        if (Client.getInnerClient().isInDebugMode() && "true".equals(Client.getInnerClient().configs.get("selectAll"))) {
            return true;
        }
        return playerId == this.getPlayerId();
    }

    @Override
    public boolean isMultiSelectable() {
        return false;
    }

    @Override
    public boolean renderInFullFog() {
        return false;
    }

    @Override
    public boolean renderInHalfFog() {
        //@TODO: Gebäude müssen sichtbar bleiben, wenn man sie einmal gesehen hat.
        return false;
    }

    @Override
    public boolean renderInNullFog() {
        return true;
    }

    @Override
    public int getColorId() {
        return getPlayerId();
    }

    @Override
    public void renderSprite(Graphics g, int x, int y, double scrollX, double scrollY, Color spriteColor) {
        Renderer.drawImage(getGraphicsData().defaultTexture, x + GraphicsContent.BASIC_FIELD_OFFSET_X - getGraphicsData().offsetX, (int) (y - 7.5 - getGraphicsData().offsetY));
    }

    @Override
    public boolean isAroundMe(Position pos) {
        int ux = pos.getX();
        int uy = pos.getY();
        // Position diese Gebäudes
        int x = this.getMainPosition().getX();
        int y = this.getMainPosition().getY();
        // Alle Felder um das Gebäude rum testen
        // Zuerst die Start-Ecke (links)
        x -= 2;
        if (x == ux && y == uy) {
            return true;
        }
        // Jetzt nach rechts oben gehen (+- z1) ((eines weiter gehen, damit ist gleich die obere ecke mit getestet))
        for (int i = 0; i <= z1; i++) {
            x++;
            y--;
            if (x == ux && y == uy) {
                return true;
            }
        }
        // Jetzt nach rechts unten gehen (++ z2) ((eines weiter gehen, damit ist gleich die obere ecke mit getestet))
        for (int i = 0; i <= z2; i++) {
            x++;
            y++;
            if (x == ux && y == uy) {
                return true;
            }
        }
        // Jetzt nach links unten gehen (-- z1) ((eines weiter gehen, damit ist gleich die obere ecke mit getestet))
        for (int i = 0; i <= z1; i++) {
            x--;
            y++;
            if (x == ux && y == uy) {
                return true;
            }
        }
        // Jetzt nach links oben gehen (-- z1)
        for (int i = 1; i <= z1; i++) {
            x--;
            y--;
            if (x == ux && y == uy) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the z1
     */
    public int getZ1() {
        return z1;
    }

    /**
     * @return the z2
     */
    public int getZ2() {
        return z2;
    }

    /**
     * Platziert das Gebäude als Baustelle
     */
    public void placeSite(Position mainPos) {
        setMainPosition(mainPos);
    }

    @Override
    public void dealDamageS(int damage) {
        super.dealDamageS(damage);
        if (getLifeStatus() == GameObject.LIFESTATUS_UNBORN) {
            this.damageWhileContruction += damage;
        }
    }

    /**
     * Der derzeitige Baufortschritt, in 0.Prozent
     * Nur relevant, wenn lifeStatus noch auf unborn steht.
     * @return the buildprogress
     */
    public double getBuildprogress() {
        return buildprogress;
    }

    /**
     * Der derzeitige Baufortschritt, in 0.Prozent
     * Nur relevant, wenn lifeStatus noch auf unborn steht.
     * @param buildprogress the buildprogress to set
     */
    public void setBuildprogress(double buildprogress) {
        this.buildprogress = buildprogress;
        if (buildprogress >= 1) {
            this.lifeStatus = GameObject.LIFESTATUS_ALIVE;
        }
    }

    /**
     * Der derzeitige Baufortschritt, in 0.Prozent
     * Nur relevant, wenn lifeStatus noch auf unborn steht.
     * @param buildprogress the buildprogress to set
     */
    public void setCaptureProgress(double capprogress) {
        this.captureprogress = capprogress;
        if (this.captureprogress < 0) {
            this.captureprogress = 0;
        }
    }

    /**
     * Speichert den genommenen Schaden während der Bauphase
     * @return the damageWhileContruction
     */
    public int getDamageWhileContruction() {
        return damageWhileContruction;
    }

    /**
     * Gibt die Anzahl freier Slots an (also wieviel Einheiten das Gebäude betreten können)
     * @return the maxIntra
     */
    public int getMaxIntra() {
        return maxIntra;
    }

    @Override
    public Position[] getVisisbilityPositions() {
        return visPositions;
    }

    @Override
    public Position[] getPositions() {
        return positions;
    }

    @Override
    public void setMainPosition(Position mainPosition) {
        // Wir überschreiben das, damit man positions setzen kann.
        super.setMainPosition(mainPosition);
        int counter = 0;
        for (int z1c = 0; z1c < z1; z1c++) {
            for (int z2c = 0; z2c < z2; z2c++) {
                positions[counter++] = new Position(mainPosition.getX() + z1c + z2c, mainPosition.getY() - z1c + z2c);
            }
        }
        try {
            visPositions[0] = mainPosition.clone();
            visPositions[1] = new Position(mainPosition.getX() + z1 - 1, mainPosition.getY() - z2 + 1);
            visPositions[2] = new Position(mainPosition.getX() + z1 - 1, mainPosition.getY() + z2 - 1);
            visPositions[3] = new Position(mainPosition.getX() + (z1 - 1) * 2, mainPosition.getY());
        } catch (CloneNotSupportedException ex) {
            // Passiert net.
        }
    }

    @Override
    public boolean selPosChanged() {
        return !selectionSet;
    }

    @Override
    public SelectionMarker getSelectionMarker() {
        selectionSet = true;
        return new SelectionMarker(this, null, positions);
    }

    @Override
    public FloatingPointPosition getCentralPosition() {
        double x = this.getMainPosition().getX() + ((getZ1() + getZ2() + 0.0) / 2) - 1;
        double y = this.getMainPosition().getY();
        FloatingPointPosition Pos = new FloatingPointPosition(x, y);
        return Pos;
    }

    public double getCaptureProgress() {
        return captureprogress;
    }

    public void changeCaptureProgress(int capturerate, int player) {
        captureprogress += capturerate;
        if (captureprogress > 100) {
            // fertig übernommen, playerid wechseln
            this.setPlayerId(player);
            captureprogress = 0;
            // Globalproducebehaviour des Spielers die neue Ressourcenrate mitteilen
            GlobalBehaviourProduceServer gloBhvProSrv = (GlobalBehaviourProduceServer) Server.getInnerServer().game.getPlayer(player).getProduceBehaviour();
            gloBhvProSrv.incrementProdrate(this.getHarvRate());
            // an Client senden
            Server.getInnerServer().netctrl.broadcastDATA(Client.getInnerClient().packetFactory((byte) 57, netID, Float.floatToIntBits(Float.NaN), (int) capturerate, player));
        } else if (captureprogress <= 0) {
            capturerate = 0;
            // capturerate kleiner 0 muss gesendet werden, damit client balken ausblendet
            Server.getInnerServer().netctrl.broadcastDATA(Client.getInnerClient().packetFactory((byte) 57, netID, Float.floatToIntBits(0.0f), (int) capturerate, player));
            lastcapturerate = capturerate;
        } else {
            if (lastcapturerate != capturerate) {
                // hat sich capturerate geändert? wenn ja, senden
                Server.getInnerServer().netctrl.broadcastDATA(Client.getInnerClient().packetFactory((byte) 57, netID, Float.floatToIntBits((float) captureprogress), (int) capturerate, player));
                lastcapturerate = capturerate;
            }
        }
        if (captureprogress < 0) {
            captureprogress = 0;
        }
    }

    /**
     * @return the neutral
     */
    public boolean isNeutral() {
        return neutral;
    }

    /**
     * @param neutral the neutral to set
     */
    public void setNeutral(boolean neutral) {
        this.neutral = neutral;
    }

    @Override
    public Position freeDirectAroundMe() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void renderMinimapMarker(Graphics g, int x, int y, Color spriteColor) {
        if (neutral) {
            g.setLineWidth(2);
            g.setColor(Color.gray);
            g.drawRect(x, y, 5, 5);
        } else {
            g.setColor(spriteColor);
            g.fillRect(x, y, 8, 8);
        }
    }

    @Override
    public void renderSkyEffect(Graphics g, int x, int y, double scrollX, double scrollY, Color spriteColor) {
        double progress = this.getCaptureProgress();
        if (progress > 0.01) {
            long timenow = System.currentTimeMillis();
            long timediff = timenow - lastcapturetime;

            final int xposition = 95; //95
            final int xlength = 60; //60

            double capprediction = capturerate * timediff / 1000.0;
            g.setColor(Color.black);
            g.fillRect(x + xposition - 3, y + 5, xlength + 6, 10);
            g.setColor(Color.red);
            g.fillRect(x + xposition, y + 8, (float) (xlength * Math.min(1 , progress / 100.0 + capprediction / 100.0)), 4);
        }

        g.setLineWidth(1);
        hovered = false;
    }

    @Override
    public void renderGroundEffect(Graphics g, int x, int y, double scrollX, double scrollY, Color spriteColor) {
        if (!neutral) {
            x += GraphicsContent.BASIC_FIELD_OFFSET_X;
            y += GraphicsContent.BASIC_FIELD_OFFSET_Y;
            // Linien ziehen
            g.setLineWidth(4);
            //g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
            g.setColor(isSelected() ? Color.white : spriteColor);
            g.drawLine(x, y, x + (getZ1() * 10), (int) (y - (getZ1() * 7.5)));
            g.drawLine(x, y, x + (getZ2() * 10), (int) (y + (getZ2() * 7.5)));
            g.drawLine((x + (getZ1() * 10)), (int) (y - (getZ1() * 7.5)), (x + (getZ1() * 10) + (getZ2() * 10)), (int) (y - (getZ1() * 7.5) + (getZ2() * 7.5)));
            g.drawLine((x + (getZ2() * 10)), (int) (y + (getZ2() * 7.5)), (x + (getZ1() * 10) + (getZ2() * 10)), (int) (y - (getZ1() * 7.5) + (getZ2() * 7.5)));
        }

        double progress = this.getCaptureProgress();

        if (progress > 0.01 || hovered) {
            g.setColor(spriteColor.multiply(new Color(1f, 1f, 1f, .5f)));
            g.setLineWidth(1);

            float axisX = GraphicsContent.FIELD_HALF_X * 60;
            float axisY = (float) (GraphicsContent.FIELD_HALF_Y * 60);

            float CenterX = (float) (this.getCentralPosition().getX() * GraphicsContent.FIELD_HALF_X + GraphicsContent.OFFSET_PRECISE_X + GraphicsContent.BASIC_FIELD_OFFSET_X);
            float CenterY = (float) (this.getCentralPosition().getY() * GraphicsContent.FIELD_HALF_Y + GraphicsContent.BASIC_FIELD_OFFSET_Y);

            g.drawOval(CenterX - axisX / 2 - (float) scrollX, CenterY - axisY / 2 - (float) scrollY, axisX, axisY);
        }

        g.setLineWidth(1);
    }

    @Override
    public boolean isAttackableBy(int playerID) {
        if (neutral) {
            return false; // Neutrale Gebäude sind natürlich nicht angreiffbar
        } else {
            // TODO: Auf globales Server-Objekt zugreiffen
            return this.getPlayerId() != playerID;
        }
    }

    @Override
    public void pause() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void unpause() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean gotClientBehaviours() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean gotServerBehaviours() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<ClientBehaviour> getClientBehaviours() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<ServerBehaviour> getServerBehaviours() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void command(int button, InteractableGameElement target, List<InteractableGameElement> repeaters, boolean doubleKlick, InnerClient rgi) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void command(int button, FloatingPointPosition target, List<InteractableGameElement> repeaters, boolean doubleKlick, InnerClient rgi) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void keyCommand(int key, char character) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @return the capturerate
     */
    public double getCaptureRate() {
        return capturerate;
    }

    /**
     * @param capturerate the capturerate to set
     */
    public void setCaptureRate(double capturerate) {
        this.capturerate = capturerate;
    }

    /**
     * @return the lastcapturetick
     */
    public long getLastCaptureTime() {
        return lastcapturetime;
    }

    /**
     * @param lastcapturetick the lastcapturetick to set
     */
    public void setLastCaptureTime(long lastcapturetime) {
        this.lastcapturetime = lastcapturetime;
    }
    
    @Override
    public void mouseHovered() {
        hovered = true;
    }
}
