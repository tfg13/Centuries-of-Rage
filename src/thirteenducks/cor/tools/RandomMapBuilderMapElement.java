

package thirteenducks.cor.tools;

/**
 *
 */
public class RandomMapBuilderMapElement {

    private int tex;
    private boolean collision;

    public int getTex() {
        return tex;
    }

    public void setTex(int i) {
        tex = i;
    }

    public boolean isBlocked() {
        return collision;
    }

    public void setBlocked(boolean collision) {
        this.collision = collision;
    }

}
