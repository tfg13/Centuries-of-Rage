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
package de._13ducks.cor.game.client;

import de._13ducks.cor.game.Building;
import de._13ducks.cor.game.GameObject;
import de._13ducks.cor.game.ability.AbilityUpgrade.upgradeaffects;
import de._13ducks.cor.game.ability.AbilityUpgrade.upgradetype;
import java.io.*;
import java.util.*;
import java.security.*;
import jonelo.jacksum.*;
import jonelo.jacksum.algorithm.*;
import org.newdawn.slick.Input;
import de._13ducks.cor.game.BehaviourProcessor;
import de._13ducks.cor.game.DescParamsBuilding;
import de._13ducks.cor.game.DescParamsUnit;
import de._13ducks.cor.networks.client.behaviour.DeltaUpgradeParameter;
import de._13ducks.cor.game.NetPlayer;
import de._13ducks.cor.game.ability.Ability;
import de._13ducks.cor.game.ability.AbilityBuild;
import de._13ducks.cor.game.ability.AbilityRecruit;
import de._13ducks.cor.game.ability.AbilityUpgrade;
import de._13ducks.cor.graphics.BuildingAnimator;
import de._13ducks.cor.graphics.UnitAnimator;
import de._13ducks.cor.map.CoRMap;
import de._13ducks.cor.game.Position;
import de._13ducks.cor.game.Unit;
import de._13ducks.cor.game.Unit2x2;
import de._13ducks.cor.game.Unit3x3;
import de._13ducks.cor.game.ability.AbilityStop;
import de._13ducks.cor.game.server.movement.MovementMap;
import de._13ducks.cor.graphics.Sprite;
import de._13ducks.cor.graphics.input.InteractableGameElement;
import de._13ducks.cor.map.DescIO;
import de._13ducks.cor.map.GameDescParams;
import de._13ducks.cor.map.MapIO;

/**
 * Das MapModul auf der Client-Seite
 * Liest .map - Dateien und verifiziert, ob sie mit der Server-Version übereinstimmen
 * Verwaltet die 3 großen ArrayLists (Einheiten, Gebäude, Ressourcen)
 * Verwaltet die netID - HashMap
 *
 * @author tfg
 */
public class ClientMapModule {

    ClientCore.InnerClient rgi;
    boolean syntaxWarned = false; // Ob schon eine Warnung wegen Syntax-Fehlern angezeigt wurde.
    public List<Unit> unitList;
    public List<Building> buildingList;
    public List<Sprite> allList;
    HashMap<Integer, GameObject> netIDList;
    public CoRMap theMap;
    public byte[] descSettings;        // Die Settings. Bekommen wir vom Server geschickt.
    public byte[] abilitySettings;     // Die Settings. Bekommen wir vom Server geschickt.
    public byte[] mapData;             // Die Map, falls ein Senden vom Server nötig war.
    File mapFile;
    String mapFileName;
    public MovementMap moveMap;
    /**
     * Server-Kollision (nur für Debug)
     * 0 = Frei
     * 1 = Unerreichbar
     * 2 = Blockiert
     * 3 = Besetzt (drüberlaufen)
     */
    public int[][] serverCollision;
    public long[][] serverRes;

    public ClientMapModule(ClientCore.InnerClient in) {
        rgi = in;
        netIDList = new HashMap<Integer, GameObject>();
    }

    /**
     * Findet heraus, ob bereits eine Map geladen wurde
     * (testet auf map != null)
     * @return
     */
    public boolean mapLoaded() {
        return theMap != null;
    }

    public void initModule() {
        rgi.rogGraphics.setLoadStatus(1);
        // Einstellungsdateien anfordern.
        // Abilitys anfordern:
        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 9, 3, 0, 0, 0));
    }

    /**
     * Aufrufen, nachdem abilitySettings reinkopiert wurde
     */
    public void gotAbilities() {
        rgi.rogGraphics.setLoadStatus(2);
        // DESC anfordern
        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 9, 2, 0, 0, 0));
    }

    /**
     * Aufrufen, nachdem descSettings reinkopiert wurde
     */
    public void gotDesc() {
        GameDescParams dparams = DescIO.readDesc(descSettings, abilitySettings, 616, rgi, null);
        rgi.rogGraphics.setLoadStatus(3);
        // Restlichen Init laufen lassen.
        rgi.logger("[MapModule]: Loading Abilities...");

        // Übernehmen:
        rgi.game.getOwnPlayer().clientDescAbilities = dparams.getAbilities();
        rgi.game.getOwnPlayer().descBuilding = dparams.getBuildings();
        rgi.game.getOwnPlayer().descUnit = dparams.getUnits();
        
        // Jetzt weiter machen:
        rgi.rogGraphics.content.initState = 1;
        //rgi.rogGraphics.initModule();
    }

    public void loadMap(String mapName, int targetHash) {
        mapFileName = mapName;
        boolean sendIt = false;
        // Läd die angegebene Map-Datei
        rgi.logger("[MapModul] Loading Map...");
        boolean fileAvailable = false;
        mapFile = new File(mapName);
        if (!mapFile.exists()) {
            // Datei nicht gefunden - Fehler loggen
            System.out.println("[MapModul]: Map not found! Map: <" + mapName + ">" + " will ask Server to send it.");
            rgi.logger("[MapModul]: Map not found! Map: <" + mapName + ">" + " will ask Server to send it.");
            sendIt = true;
        } else {
            try {
                // Checksumme gleich berechnen:
                AbstractChecksum checksum = JacksumAPI.getChecksumInstance("adler32");
                try {
                    checksum.readFile(mapName);
                } catch (IOException ex) {
                    System.out.println("FixMe: Checksum-Error??");
                }
                int mapHash = (int) checksum.getValue();
                if (mapHash != targetHash) {
                    System.out.println("[MapModul]: Map mismatch" + " will ask Server to send its version of this map.");
                    rgi.logger("[MapModul][ERROR]: Map mismatch" + " will ask Server to send its version of this map.");
                    sendIt = true;
                }
            } catch (NoSuchAlgorithmException ex) {
                System.out.println("CriticalFixMe: Can't calc checksum, Adler32 not available!");
            }
        }
        // Müss mer se noch holen?
        if (sendIt) {
            rgi.rogGraphics.setLoadStatus(7);
            //Maprequest senden
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 9, 1, 0, 0, 0));
        } else {
            gotMap();
        }

    }

    public void receivedMap() {
        try {
            // Erhaltene Map speichern
            FileOutputStream outp = new java.io.FileOutputStream(mapFile);
            outp.write(mapData);
            outp.flush();
            outp.close();
        } catch (IOException ex) {
        }
        gotMap();
    }

    public void gotMap() {
        rgi.rogGraphics.setLoadStatus(8);

        theMap = MapIO.readMap(mapFile.getPath(), MapIO.MODE_CLIENT, rgi, null);

        if (rgi.isInDebugMode()) {
            serverCollision = new int[theMap.getMapSizeX()][theMap.getMapSizeY()];
            serverRes = new long[theMap.getMapSizeX()][theMap.getMapSizeY()];
        }

        unitList = Collections.synchronizedList((ArrayList<Unit>) theMap.getMapPoperty("UNIT_LIST"));
        buildingList = Collections.synchronizedList((ArrayList<Building>) theMap.getMapPoperty("BUILDING_LIST"));
        refreshUnits();
        refreshBuildings();
        rgi.logger("[MapModul] Map \"" + mapFileName + "\" loaded.");
        createIDList();
        rgi.rogGraphics.activateMap(theMap.getVisMap());
        createAllLists();
        // DEBUG:
        moveMap = MovementMap.createMovementMap(theMap, buildingList);
        // Fertig, mitteilen
        rgi.rogGraphics.triggerStatusWaiting();
        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 1, 0, 0, 0, 0));
    }

    private void createAllLists() {
        // Erstellt eine Liste, in der Einheiten, Gebäude und Ressourcen enthalten sind.
        // Für Grafik only
        if (allList == null) {
            allList = new ArrayList<Sprite>();
        } else {
            allList.clear();
        }
        // Input
        List<InteractableGameElement> igelist = new ArrayList<InteractableGameElement>();
        // Game
        List<BehaviourProcessor> procList = new ArrayList<BehaviourProcessor>();
        for (Unit unit : unitList) {
            igelist.add(unit);
            allList.add(unit);
            procList.add(unit);
        }
        for (Building building : buildingList) {
            allList.add(building);
            igelist.add(building);
            procList.add(building);
        }
        rgi.rogGraphics.inputM.setIGEs(igelist);
        rgi.game.registerAllList(procList);
    }

    /**
     * Load server collision map
     */
    private void loadCollisionMap() {
        for (int x = 0; x < theMap.getMapSizeX(); x++) {
            for (int y = 0; y < theMap.getMapSizeY(); y++) {
                if (x % 2 != y % 2) {
                    continue;
                }
            }
        }
    }

    private void createIDList() {
        // Verlässt sich darauf, dass der MapEditor/RandomMapGenerator korrekte netIDs vergeben hat...
        netIDList.clear();
        for (Unit unit : unitList) {
            netIDList.put(unit.netID, unit);
        }
        for (Building building : buildingList) {
            netIDList.put(building.netID, building);
        }
    }

    private void refreshUnits() {
        // Im Speicherformat werden einige Dinge weggelassen, jetzt wird der Rest aufgefüllt, um wieder vollständige RogUnits zu haben
        // Ausserdem werden alle (gespeicherten) Parameter überschrieben, das macht Änderungen einfacher (man ändert nur die desctypes und muss nicht immer neue maps machen)
        HashMap<Integer, Unit> descUnit = rgi.game.getOwnPlayer().descUnit;
        for (Unit unit : unitList) {
            // Alle Parameter kopieren
            unit.copyPropertiesFrom(descUnit.get(unit.getDescTypeId()));
            unit.initClientMovementManager(rgi);
            unit.addAbility(new AbilityStop(unit, rgi));
        }
    }

    private void refreshBuildings() {
        // Im Speicherformat werden einige Dinge weggelassen, jetzt wird der Rest aufgefüllt, um wieder vollständige RogBuildings zu haben
        // Ausserdem werden alle (gespeicherten) Parameter überschrieben, das macht Änderungen einfacher (man ändert nur die desctypes und muss nicht immer neue maps machen)
        HashMap<Integer, Building> descBuilding = rgi.game.getOwnPlayer().descBuilding;
        for (Building building : buildingList) {
            building.copyPropertiesFrom(descBuilding.get(building.getDescTypeId()));
        }
    }

    private int saveStrtoInt(String transform, String ganzeZeile, int line) {
        // Übersetzt den String in ein int und fängt exceptions ab.
        int i;
        try {
            i = Integer.parseInt(transform);
            return i;
        } catch (java.lang.NumberFormatException ex) {
            // Ging nicht, Syntax-Fehler ins Logfile schreiben
            rgi.logger("[RogMapModule][ERROR]: Syntax-Error in line " + line + ": \"" + transform + "\" is not a valid number! - Defaulting to 1");
            if (!syntaxWarned) {
                // Anzeigen
                //rgi.rogGraphics.displayWarning("Einer oder mehrere Syntax-Fehler in \"game/descType\" \n eventuell sind Einheiten oder Gebäude falsch! \n Mehr Informationen im Logfile.");
                syntaxWarned = true;
            }
            return 1;
        }
    }

    private double saveStrtoDouble(String transform, String ganzeZeile, int line) {
        // Übersetzt den String in ein int und fängt exceptions ab.
        double i;
        try {
            i = Double.parseDouble(transform);
            return i;
        } catch (java.lang.NumberFormatException ex) {
            // Ging nicht, Syntax-Fehler ins Logfile schreiben
            rgi.logger("[RogMapModule][ERROR]: Syntax-Error in line " + line + ": \"" + transform + "\" is not a valid number! - Defaulting to 1");
            if (!syntaxWarned) {
                // Anzeigen
                //rgi.rogGraphics.displayWarning("Einer oder mehrere Syntax-Fehler in \"game/descType\" \n eventuell sind Einheiten oder Gebäude falsch! \n Mehr Informationen im Logfile.");
                syntaxWarned = true;
            }
            return 1;
        }
    }

    public boolean isValidUnitDesc(int desc) {
        if (rgi.game.getOwnPlayer().descUnit.get(desc) != null) {
            return true;
        }
        return false;
    }

    public boolean isValidBuildingDesc(int desc) {
        if (rgi.game.getOwnPlayer().descBuilding.get(desc) != null) {
            return true;
        }
        return false;
    }

    public void insertUnitAnimator(int desc, UnitAnimator rgua) {
        // Speichert einen vorkonfigurierten Animator in die DESC-Datenbank ein
        System.out.println("AddMe: Insert Unit Animator");
        // rgi.game.getOwnPlayer().descUnit.get(desc).anim = rgua;
    }

    public void insertBuildingAnimator(int desc, BuildingAnimator rgba) {
        // Speichert eien vorkonfigurierten Animator in die DESC-Datenbank ein
        System.out.println("AddMe: Insert Building Animator");
        // rgi.game.getOwnPlayer().descBuilding.get(desc).anim = rgba;
    }

    public List<Unit> getUnitList() {
        // Liefert die Einheitenliste zurück.
        return unitList;
    }

    public List<Building> getBuildingList() {
        // Gibt die Gebäudeliste zurück
        return buildingList;
    }

    public Unit getDescUnit(int descId, int newNetID, int playerId) {
        try {
            Unit unit = (Unit) rgi.game.getPlayer(playerId).descUnit.get(descId).getCopy(newNetID);
            ArrayList<Ability> list = new ArrayList<Ability>();
            List<Ability> unitsAbList = unit.getAbilitys();
            for (Ability ab : unitsAbList) {
                list.add(rgi.game.getPlayer(playerId).clientDescAbilities.get(ab.myId));
            }
            unit.setAbilitys(list);
            return unit;
        } catch (Exception ex) {
            System.out.println("FixMe: Cloning-Error Unit");
            return null;
        }
    }

    public Building getDescBuilding(int descId, int newNetID, int playerId) {
        try {
            Building building = (Building) rgi.game.getPlayer(playerId).descBuilding.get(descId).getCopy(newNetID);
            ArrayList<Ability> list = new ArrayList<Ability>();
            List<Ability> buildingsAbList = building.getAbilitys();
            for (Ability ab : buildingsAbList) {
                list.add(rgi.game.getPlayer(playerId).clientDescAbilities.get(ab.myId));
            }
            building.setAbilitys(list);
            return building;
        } catch (Exception ex) {
            System.out.println("FixMe: Cloning-Error Building");
            return null;
        }
    }

    public int getMapSizeX() {
        return theMap.getMapSizeX();
    }

    public int getMapSizeY() {
        return theMap.getMapSizeY();
    }

    /**
     * Führt die Globalen Paramter des DeltaUpgradeParamter aus.
     * Dies funktioniert nur, wenn global=true ist.
     * Dann werden aber alle andern Parameter ignoriert.
     * Daher funktionieren diese Upgrades nur als multidelta/epoche
     * @param para
     */
    public void performGlobalUpgrade(DeltaUpgradeParameter para) {
        NetPlayer me = rgi.game.getOwnPlayer();
        me.harvspeeds[1] += para.harvrate1up;
        me.harvspeeds[2] += para.harvrate2up;
        me.harvspeeds[3] += para.harvrate3up;
        me.harvspeeds[4] += para.harvrate4up;
        me.harvspeeds[5] += para.harvrate5up;
        me.maxlimit += para.limitup;
        me.res1 += para.res1up;
        me.res2 += para.res2up;
        me.res3 += para.res3up;
        me.res4 += para.res4up;
        me.res5 += para.res5up;
    }

    /**
     * Sucht eine Einheit anhand ihrer netID.
     *
     * @param netID - Die netID der Einheit.
     * @return RogUnit, falls sie gefunden wurde, sonst null.
     */
    public Unit getUnitviaID(int netID) {
        try {
            return (Unit) netIDList.get(netID);
        } catch (Exception ex) {
            // Gibts net, falscher Typ etc...
            return null;
        }
    }

    /**
     * Sucht ein Gebäude anhand seiner netID.
     *
     * @param netID - Die netID des Gebäudes.
     * @return RogBiulding, falls gefunden, sonst null.
     */
    public Building getBuildingviaID(int netID) {
        try {
            return (Building) netIDList.get(netID);
        } catch (Exception ex) {
            // Gibts net, falscher Typ etc...
            return null;
        }
    }

    /**
     * Sucht ein GameObject anhand seiner netID.
     *
     * @param netID - Die netID des GameObjects.
     * @return RogGameObjekt, falls sie gefunden wurde, sonst null.
     */
    public GameObject getGameObjectviaID(int netID) {
        try {
            return (GameObject) netIDList.get(netID);
        } catch (Exception ex) {
            // Gibts net, falscher Typ etc...
            return null;
        }
    }

    /**
     * Fügt ein Gebäude zum Spiel hinzu.
     * Darf nur vom NetController aufgerufen werden!
     * @param b
     */
    public void addBuilding(Building b) {
        try {
            rgi.rogGraphics.content.allListLock.lock();
            this.allList.add(b);
        } finally {
            rgi.rogGraphics.content.allListLock.unlock();
        }
        this.buildingList.add(b);
        rgi.game.addGO(b);
        rgi.rogGraphics.inputM.addGO(b);

        if (netIDList.containsKey(b.netID)) {
            throw new java.lang.UnknownError("Critical ID mismatch, overwriting netID-Entry");
        }
        this.netIDList.put(b.netID, b);

        // In Abhängigkeitsliste einfügen
        if (b.getLifeStatus() == GameObject.LIFESTATUS_ALIVE && b.getPlayerId() == rgi.game.getOwnPlayer().playerId) {
            if (!rgi.game.getOwnPlayer().bList.contains(b.getDescTypeId())) {
                rgi.game.getOwnPlayer().bList.add(b.getDescTypeId());
            }
        }

        // Selektionsschatten einfügen
            rgi.rogGraphics.builingsChanged();
    }

    /**
     * Sucht ein Gebäude anhand seiner Position
     * Bitte nur selten benutzen, da langsam.
     * @param position Die Position des Gebäudes (das Feld ganz links)
     * @return RogBuilding, wenn gefunden, sonst null
     */
    public Building findBuilingViaPosition(Position position) {
        for (int i = 0; i < buildingList.size(); i++) {
            Building b = buildingList.get(i);
            if (b.getMainPosition().equals(position)) {
                return b;
            }
        }
        return null;
    }

    /**
     * Fügt eine Einheit zum Spiel hinzu
     * Darf nur vom NetController aufgerufen werden!
     * @param b
     */
    public void addUnit(Unit u) {
        u.initClientMovementManager(rgi);
        u.addAbility(new AbilityStop(u, rgi));
        rgi.game.addGO(u);
        rgi.rogGraphics.inputM.addGO(u);
        if (netIDList.containsKey(u.netID)) {
            throw new java.lang.UnknownError("Critical ID mismatch, overwriting netID-Entry");
        }
        this.netIDList.put(u.netID, u);
        try {
            rgi.rogGraphics.content.allListLock.lock();
            this.allList.add(u);
        } finally {
            rgi.rogGraphics.content.allListLock.unlock();
        }
        this.unitList.add(u);

        // In Abhängigkeitsliste einfügen
        if (u.getPlayerId() == rgi.game.getOwnPlayer().playerId) {
            if (!rgi.game.getOwnPlayer().uList.contains(u.getDescTypeId())) {
                rgi.game.getOwnPlayer().uList.add(u.getDescTypeId());
            }
        }
    }

    /**
     * Spielt eine Todesanimation ab und entfernt die Einheit aus dem Spiel.
     * @param modunit
     */
    public void unitKilled(Unit unit) {
        if (unit != null) {

            rgi.rogGraphics.notifyUnitDieing(unit);
            this.unitList.remove(unit);
            this.netIDList.remove(unit.netID);
            rgi.game.removeGO(unit);
            rgi.rogGraphics.inputM.removeGO(unit);
            rgi.rogGraphics.inputM.removeFromSelection(unit);
        }
    }

    /**
     * Simple Hilfsmethode, die Damage dealt.
     * Für Bulletsystem.
     * Einheit wird NICHT entfernt, wenn sie stirbt!!!
     * Das kann nur der Server.
     * @param victim
     * @param damage
     * @deprecated
     */
    public void dealDamage(GameObject victim, int damage) {
        victim.dealDamageS(damage);
    }

    /**
     * Löscht die übergebenen Einheiten/Gebäude (killt sie)
     * Vorsicht, es dürfen nur ENTWEDER eine oder mehrere Einheiten ODER ein Gebäude enthalten sein
     * Mischungen oder mehrerer Gebäude sind nicht zulässig!
     * @param selected
     */
    public void deleteSelected(List<InteractableGameElement> selected) {
        // Löscht alle derzeit selektierten Einheiten / das derzeit selektierte Gebäude
        if (!selected.isEmpty()) {
            for (int i = 0; i < selected.size(); i++) {
                selected.get(i).keyCommand(Input.KEY_DELETE, '\0');
            }
        }
    }

    /**
     * Spielt eine Zerstöranimation ab und entfernt das Gebäude aus dem Spiel.
     * @param building
     */
    public void buildingKilled(Building building) {
        if (building != null) {
            building.killS();
            // Für eigene Gebäude den Sichtbereich auf erkundet setzen
            if (building.getPlayerId() == rgi.game.getOwnPlayer().playerId) {
                rgi.rogGraphics.content.cutDieingBuildingSight(building);
            }
            // Jetzt löschen
            this.buildingList.remove(building);
            this.netIDList.remove(building.netID);
            rgi.game.removeGO(building);
            rgi.rogGraphics.inputM.removeGO(building);
            rgi.rogGraphics.notifyBuildingDieing(building);
            rgi.rogGraphics.inputM.removeFromSelection(building);
            // Effekte entfernen
        }
    }

    /**
     * Verwaltet komplexere toDESC-Upgrades
     *
     * @param mode old=2, new=3, all=4
     * @param caster Der dieses Upgrade ausführt
     * @param fromDesc Von welcher Desc...
     * @param toDesc ... auf welche upgraden?
     */
    public void manageComplexUpgrades(int mode, GameObject caster, int fromDesc, int toDesc, int playerId) {
        boolean units = false;
        // Änderungen suchen:
        if (fromDesc < 0) {
            // Negative Werte sind Einheiten
            fromDesc *= -1;
            units = true;
        }

        // Wer denn alles?
        if (mode == 2) {
            // Vorhandene
            if (units) {
                for (Unit unit : unitList) {
                    if (unit.getDescTypeId() == fromDesc && unit.getPlayerId() == playerId) {
                        unit.performUpgrade(rgi, toDesc);
                    }
                }
            } else {
                for (Building building : buildingList) {
                    if (building.getDescTypeId() == fromDesc && building.getPlayerId() == playerId) {
                        building.performUpgrade(rgi, toDesc);
                    }
                }
            }
        } else if (mode == 3) {
            // Zukünftige
            if (units) {
                Unit updesc = rgi.game.getPlayer(playerId).descUnit.get(toDesc);
                rgi.game.getPlayer(playerId).descUnit.put(fromDesc, updesc);
            } else {
                rgi.game.getPlayer(playerId).descBuilding.put(fromDesc, rgi.game.getPlayer(playerId).descBuilding.get(toDesc));
            }
        } else if (mode == 4) {
            // Alle
            if (units) {
                rgi.game.getPlayer(playerId).descUnit.put(fromDesc, rgi.game.getPlayer(playerId).descUnit.get(toDesc));
            } else {
                rgi.game.getPlayer(playerId).descBuilding.put(fromDesc, rgi.game.getPlayer(playerId).descBuilding.get(toDesc));
            }
            if (units) {
                for (Unit unit : unitList) {
                    if (unit.getDescTypeId() == fromDesc && unit.getPlayerId() == playerId) {
                        unit.performUpgrade(rgi, toDesc);
                    }
                }
            } else {
                for (Building building : buildingList) {
                    if (building.getDescTypeId() == fromDesc && building.getPlayerId() == playerId) {
                        building.performUpgrade(rgi, toDesc);
                    }

                }
            }
        }
    }

    /**
     * Berechnet, ob 2 GOs sich derzeit direkt berühren.
     * @param obj1 Das erste Objekt
     * @param obj2 Das zweite Objekt
     * @return true, wenn sie direkt benachbart sind (ecken zählen)
     */
    public boolean directNeighbor(GameObject obj1, GameObject obj2) {
        final Position[] pos1 = obj1.getPositions();
        final Position[] pos2 = obj2.getPositions();

        // Diese Methode ist nicht sehr schön, aber es sollte funktionieren:
        for (Position from : pos1) {
            for (Position to : pos2) {
                int dx = from.getX() - to.getX();
                int dy = from.getY() - to.getY();
                if ((dx >= -1 && dx <= 1 && dy >= -1 && dy <= 1) || (dx >= -2 && dx <= 2 && dy == 0) || (dx == 0 && dy >= -2 && dy <= 2)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Entsperrt eine Ability
     * @param ability Die Ability, die entsperrt werden soll
     */
    public void unlockAbility(int ability) {
        // Für alle unlocken
        NetPlayer player = rgi.game.getOwnPlayer();
        player.clientDescAbilities.get(ability).invisibleLocked = false;
        for (Unit unit : unitList) {
            Ability abi = unit.getAbility(ability);
            if (abi != null) {
                abi.invisibleLocked = false;
            }
        }
        for (Building building : buildingList) {
            Ability abi = building.getAbility(ability);
            if (abi != null) {
                abi.invisibleLocked = false;
            }
        }
    }

    /**
     * Sperrt eine Ability
     * @param ability Die Ability, die gesperrt werden soll
     */
    public void lockAbility(int ability) {
        // Für alle unlocken
        NetPlayer player = rgi.game.getOwnPlayer();
        player.clientDescAbilities.get(ability).invisibleLocked = true;
        for (Unit unit : unitList) {
            Ability abi = unit.getAbility(ability);
            if (abi != null) {
                abi.invisibleLocked = true;
            }
        }
        for (Building building : buildingList) {
            Ability abi = building.getAbility(ability);
            if (abi != null) {
                abi.invisibleLocked = true;
            }
        }
    }
}
