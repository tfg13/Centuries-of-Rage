package de._13ducks.cor.game.ability;

import org.newdawn.slick.Color;
import de._13ducks.cor.game.GameObject;
import de._13ducks.cor.game.Unit;
import de._13ducks.cor.game.client.ClientCore;

/**
 *
 */
public class AbilityStop extends Ability {
    
    private Unit caster;

    public AbilityStop(Unit caster2, ClientCore.InnerClient newinner) {
        super(-2);
        this.type = Ability.ABILITY_MOVE;
        this.cooldown = 0.0;
        frameColor = Color.green;
        caster = caster2;
        this.name = "Stop movement";
        rgi = newinner;
        this.symbols = new String[]{"img/game/stop.png"};
        this.useForAll = true;
    }

    @Override
    public void perform(GameObject caster) {
        this.caster.stopMovement(rgi);
    }

    @Override
    public void antiperform(GameObject caster) {
        perform(caster); // Wie Linksklick
    }

    @Override
    public boolean isAvailable() {
        return caster.moveStoppable();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AbilityStop) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

}
