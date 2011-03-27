package thirteenducks.cor.game.ability;

import org.newdawn.slick.Color;
import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.game.client.ClientCore;

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
    }

    @Override
    public void perform(GameObject caster) {
        this.caster.stopMovement();
    }

    @Override
    public void antiperform(GameObject caster) {
        perform(caster); // Wie Linksklick
    }

    @Override
    public boolean isAvailable() {
        return caster.moveStoppable();
    }



}
