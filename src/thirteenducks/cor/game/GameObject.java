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

import java.io.Serializable;
import java.util.List;
import thirteenducks.cor.networks.client.behaviour.ClientBehaviour;
import thirteenducks.cor.game.ability.Ability;
import thirteenducks.cor.game.server.behaviour.ServerBehaviour;
import thirteenducks.cor.graphics.GOGraphicsData;
import thirteenducks.cor.graphics.Sprite;
import thirteenducks.cor.graphics.input.InteractableGameElement;

/**
 * Superklasse für "Spielobjekte". Das werden vor allem Einheiten und Gebäude sein.
 *
 */
public abstract class GameObject implements Serializable, Sprite, BehaviourProcessor, InteractableGameElement, Hideable {

    /**
     * Dieses Objekt lebt noch nicht.
     * z.B. ein Gebäude als Baustelle
     */
    public static final int LIFESTATUS_UNBORN = 0;
    /**
     * Dieses Objekt lebt. (Normalzustand)
     */
    public static final int LIFESTATUS_ALIVE = 1;
    /**
     * Dieses Objekt ist gestorben.
     */
    public static final int LIFESTATUS_DEAD = 2;

    /**
     * Dieses Objekt tut gerade nichts. (Normalzustand)
     */
    public static final int STATUS_IDLE = 0;
    /**
     * Dieses Objekt bewegt sich gerade.
     */
    public static final int STATUS_MOVING = 1;
    /**
     * Dieses Objekt arbeitet gerade.
     */
    public static final int STATUS_WORKING = 2;
    /**
     * Agressive Bewegung zum Ziel. Ziele in Reichweite werden während der Bewegung angegriffen,
     * das Objekt bliebt notfalls auch stehen.
     * Default-Bewegungsmodus bei Rechtsklick auf den Boden.
     */
    public static final int MOVE_AGGRESSIVE = 0;
    /**
     * Direkte Bewegung ("fliehen").
     * Das Objekt wird auf dem Weg nicht stehen bleiben um Feinde zu bekämpfen,
     * außer das Ziel wird durch eine vollständige Blockade unerreichbar.
     * Wird durch einen doppel-Rechtsklick (Boden) aktiviert.
     */
    public static final int MOVE_DIRECT = 1;
    /**
     * Agressive Bewegung zum Ziel / anschließend wird es angegriffen.
     * Auf dem Weg werden Feinde bekämpft, eventuell bleibt das Objekt auch stehen.
     * Das hat zur Folge, dass auch andere Objekte angegriffen werden, wenn man so eine ganze
     * Gruppe anklickt.
     * Default-Angriffsmodus beim Rechtsklick auf Feinde.
     */
    public static final int ATK_AGGRESSIVE = 2;
    /**
     * Das Objekt versucht nah genug an das Ziel heranzukommen, damit es angegriffen werden kann.
     * Das Objekt wird keine anderen Einheiten angreiffen, solange noch die Möglichkeit besteht,
     * das gesetzte Ziel zu erreichen.
     * Dieses "Focus-Fire" kann mit einem Doppel-Rechtsklick auf Feinde aktiviert werden.
     */
    public static final int ATK_FOCUS = 3;
    /**
     * Leichte Infantrie
     */
    public static final int ARMORTYPE_LIGHTINF = 0;
    /**
     * Schwere Infantrie
     */
    public static final int ARMORTYPE_HEAVYINF = 1;
    /**
     * Kavallerie
     */
    public static final int ARMORTYPE_KAV = 2;
    /**
     * Leichte Fahrzeuge / Kriegsmaschinen a la Katapult
     */
    public static final int ARMORTYPE_VEHICLE = 3;
    /**
     * Schwere Fahrzeuge / Panzer
     */
    public static final int ARMORTYPE_TANK = 4;
    /**
     * Lufteinheiten
     */
    public static final int ARMORTYPE_AIR = 5;
    /**
     * Gebäude
     */
    public static final int ARMORTYPE_BUILDING = 6;

    /**
     * Ein dieses Objekt beschreibender String, bei allen Objekten dieses Typs gleich
     * z.B. "Bogenschütze"
     */
    private String descName;
    /**
     * Zeigt, ob das Objekt derzeit selektier ist. Wird vom Selektionssystem gesetzt.
     */
    private boolean isSelected = false;
    /**
     * Die PlayerID des Spielers, der dieses Objekt aktuell kontrolliert
     * Die PlayerID's beginnen mit 1, 0 bedeutet neutral
     */
    private int playerId = 0;
    /**
     * Die derzeitige Zuordnungsposition des Objekts.
     * Die Zuordnungsposition ist die Position ganz links.
     */
    private Position mainPosition;
    /**
     * Die Rüstungsklasse diese Objekts
     */
    protected int armorType;
    /**
     * Schaden gegen andere Rüstungsklassen in Prozent.
     * Default ist 100
     */
    private int[] damageFactors;
    /**
     * Aktuelle Lebensenergie dieses Objekts.
     * Normalerweise stirbt das Objekt bei <=0
     */
    protected int hitpoints;
    /**
     * Maximale Lebensenergie dieses Objekts.
     */
    protected int maxhitpoints;
    /**
     * Der Typ der folgenden descTypeId.
     * Derzeit werden vor allem B für Gebäude und U für Einheiten genutzt.
     */
    private char descTypeType;
    /**
     * Die ID-Nummer dieser Einheit.
     * Wird für Zuordnungen mehrerer Einheiten des gleichen Typs verwendet.
     */
    private int descTypeId;
    /**
     * Die Liste mit den Abilitys dieses Objekts.
     * Abilitys werden im Hud als anklickbare Knöpfe dargestellt.
     */
    private List<Ability> abilitys;
    /**
     * Die Server-Behaviour dieses Objekts.
     * Werden regelmäßig vom GameLogic-Thread aufgerufen.
     */
    private List<ServerBehaviour> sbehaviours;
    /**
     * Die Client-Behaviour dieses Objekts.
     * Werden regelmäßig vom GameLogic-Thread aufgerufen.
     */
    private List<ClientBehaviour> cbehaviours;
    /**
     * Default-Wegpunkt.
     * Derzeit verwendet für das erste Laufziel neu erzeugter Einheiten
     */
    private Position waypoint;
    /**
     * Der derzeitige Lebens-Zustand dieses Objekts.
     * Kann soetwas wie "am Leben" oder "schon gestorben" sein.
     */
    private int lifeStatus;
    /**
     * Bewegungs/Angriffszustand dieses Objekts.
     * z.B. Aggressive Bewegung, FocusFire
     */
    private int moveAtkMode;
    /**
     * Was die Einheit gerade tut.
     * z.B. "nichts" "gehen" "arbeiten"
     */
    private int status;
    /**
     * Die Sichtweite dieses Objekts im FoW.
     * Gemessen in eckigen Kreisen um das Zentrum, (sogut es ganzzahlig geht)
     */
    private int visrange;
    /**
     * Die dieses Objekt in der derzeit laufenden Spielpartie netzwerkweit eindeutig identifizierende Id.
     */
    public final int netID;
    /**
     * Diese Anzahl von Millisekunden müssen mindestens zwischen 2 Schlägen/Schüssen vergehen.
     */
    private int fireDelay;
    /**
     * Basis-Schaden dieses Objekts.
     */
    private int damage;
    /**
     * Aktuelles Angriffsziel dieses Objekts
     */
    private GameObject attackTarget;
    /**
     * Die Reichweite dieser Einheit.
     * Echte Entfernungsmessung zwischen(!) den Einheiten.
     * Nahkämpfer haben also etwa 0
     */
    protected double range;
    /**
     * Geschossgeschwindigkeit.
     * 0 ist für Nahkämpfer, also Instant-Hit.
     */
    private int bulletspeed = 15;
    /**
     * Geschosstextur. Nahkämpfer haben keine.
     */
    private String bullettexture;
    /**
     * Delay in Millisekunden zwischen dem Beginn eines Angriffs ("ausholen") und dem "zuschlagen". In dieser Zeit wird die Angriffsanimation abgespielt und die Einheit von der Grafikengine etwas vor bewegt. (nur Nahkampf)
     */
    private int atkdelay = 0;
    /**
     * Enthält alle Grafik- & Animationsdaten
     */
    private GOGraphicsData graphicsData;
    /**
     * Ein beschreibender String für GUI-Infos.
     * z.B. "Ein langsamer aber zäher Kämpfer"
     */
    private String descDescription;
    /**
     * Ein beschreibender String für GUI-Infos.
     * Sagt, gegen was dieses Objekt besonders stark ist.
     * Optional
     * z.B. "Light Infantry"
     */
    private String descPro;
    /**
     * Ein beschreibender String für GUI-Infos.
     * Sagt, gegen was dieses Objekt besonders schwach ist.
     * Optional
     * z.B. "Heavy Infantry"
     */
    private String descCon;
    /**
     * Heilrate dieses Objekts.
     * Mit dieser Rate heilt es andere in seiner Nähe, nicht sich selbst(!)
     */
    private int healRate = 0;

    /**
     * Erzeugt ein neues GameObject mit der angegebenen ID an der Stelle mainPos
     * @param newNetId Die netId dieses Objekts
     * @param mainPos Die Haupt-Zuordnungsposition
     */
    protected GameObject(int newNetId, Position mainPos) {
        netID = newNetId;
        mainPosition = mainPos;
    }
    /**
     * Erzeugt ein Platzhalter-GameObject, das nicht direkt im Spiel verwendet werden kann, aber als Platzhalter für
     * Attribute und Fähigkeiten dient.
     */
    protected GameObject() {
        netID = -1;
    }

    /**
     * @return the visrange
     */
    public int getVisrange() {
        return visrange;
    }

    /**
     * @param visrange the visrange to set
     */
    public void setVisrange(int visrange) {
        this.visrange = visrange;
    }

    public abstract Position freeDirectAroundMe();

    /**
     * Überprüft, ob die Angegebene Position zur Zeit ein direktes Nachbarfeld dieses Objekts ist.
     * Ecken zählen dazu, innen liegende Felder nicht.
     * @param pos die zu prüfende Position
     * @return true, wenn die Position ein direktes Nachbarfeld, sonst false
     */
    public abstract boolean isAroundMe(Position pos);

    /**
     * Returns the MainPosition
     * @return the MainPosition
     */
    public Position getMainPosition() {
        try {
            return mainPosition.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException("Error cloning Position!", ex);
        }
    }

    /**
     * @return the descTypeType
     */
    public char getDescTypeType() {
        return descTypeType;
    }

    /**
     * @return the descTypeId
     */
    public int getDescTypeId() {
        return descTypeId;
    }

    /**
     * @return the descName
     */
    public String getName() {
        return descName;
    }

    /**
     * @param playerId the playerId to set
     */
    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    /**
     * Liefert alle Positionen, auf denen ein GO derzeit steht.
     * @return
     */
    public abstract Position[] getPositions();

    /**
     * @param mainPosition the mainPosition to set
     */
    public void setMainPosition(Position mainPosition) {
        this.mainPosition = mainPosition;
    }
}
