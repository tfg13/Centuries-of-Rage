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
package thirteenducks.cor.game;

import java.util.ArrayList;
import java.util.List;
import thirteenducks.cor.game.client.ClientCore;
import thirteenducks.cor.game.server.ServerCore;
import thirteenducks.cor.graphics.input.SelectionMarker;

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
public abstract class Building extends GameObject {

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
    private int z1 = 2;
    /**
     * Größe dieses Gebäudes in Feldern Richtung rechts unten.
     * Ausgehend von der Zuordnungsposition ganz links.
     */
    private int z2 = 2;
    /**
     * Der derzeitige Baufortschritt, in 0.Prozent
     * Nur relevant, wenn lifeStatus noch auf unborn steht.
     */
    private double buildprogress;
    /**
     * Liste mit Units, die sich derzeit in diesem Gebäude befinden.
     */
    private List<Unit> intraUnits;
    /**
     * Zeigt an, welche Ressource dieses Gebäude produziert, solange es Arbeiter beherbergt.
     * @deprecated
     */
    private int harvests = 0;
    /**
     * Gibt die Anzahl freier Slots an (also wieviel Einheiten das Gebäude betreten können)
     */
    protected int maxIntra = 0;
    /**
     * Gibt die  Ernterate pro interner Einheit an
     */
    private double harvRate = 0.0;
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
    private Position[] positions;
    /**
     * Wurde die Selektion schon eingetragen?
     * Muss in der Regel nur ein einiziges Mal geschehen
     */
    private boolean selectionSet = false;

    /**
     * Erzeugt ein neues Gebäude mit den gegebenen Parametern.
     * Default-Konstruktor, erzeugt im Spiel vollständig nutzbare Gebäude
     * @param newNetId die netID für dieses Gebäude
     * @param mainPos die Zuordnungsposition
     */
    protected Building(int newNetId, Position mainPos) {
        super(newNetId, mainPos);
        intraUnits = new ArrayList<Unit>();
        positions = new Position[z1 * z2];
        setVisrange(7); // Default für Gebäude
    }

    /**
     * Erzeugt ein Platzhalter-Gebäude, das nicht direkt im Spiel verwendet werden kann, aber als Platzhalter für
     * Attribute und Fähigkeiten dient.
     */
    protected Building(DescParamsBuilding params) {
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
    protected Building(int newNetId, Building copyFrom) {
        super(newNetId, copyFrom);
        this.accepts = copyFrom.accepts;
        this.maxIntra = copyFrom.maxIntra;
        this.z1 = copyFrom.z1;
        this.z2 = copyFrom.z2;
        intraUnits = new ArrayList<Unit>();
        positions = new Position[z1 * z2];
    }

    /**
     * Wendet die Parameterliste an (kopiert die Parameter rein)
     * @param par
     */
    private void applyBuildingParams(DescParamsBuilding par) {
        this.accepts = par.getAccepts();
        this.harvRate = par.getHarvRate();
        this.harvests = par.getHarvests();
        this.maxIntra = par.getMaxIntra();
        this.z1 = par.getZ1();
        this.z2 = par.getZ2();
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
    public void dealDamage(int damage) {
        super.dealDamage(damage);
        if (getLifeStatus() == GameObject.LIFESTATUS_UNBORN) {
            this.damageWhileContruction += damage;
        }
    }

    /**
     * @return the harvests
     */
    public int getHarvests() {
        return harvests;
    }

    /**
     * @return the harvRate
     */
    public double getHarvRate() {
        return harvRate;
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
        return positions;
    }

    @Override
    public void setMainPosition(Position mainPosition) {
        // Wir überschreiben das, damit man positions setzen kann.
        super.setMainPosition(mainPosition);
        int counter = 0;
        for (int z1c = 0; z1c < z1; z1c++) {
            for (int z2c = 0; z2c < z2; z2c++) {
                positions[counter++] = new Position((int) mainPosition.getX() + z1c + z2c,(int) mainPosition.getY() - z1c + z2c);
            }
        }
    }


    @Override
    public boolean selPosChanged() {
        return !selectionSet;
    }

    @Override
    public SelectionMarker getSelectionMarker() {
        return new SelectionMarker(this, null, positions);
    }
}
