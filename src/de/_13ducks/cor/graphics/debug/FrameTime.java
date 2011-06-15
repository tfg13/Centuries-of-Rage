/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de._13ducks.cor.graphics.debug;

/**
 *
 * @author tfg
 */
public class FrameTime {
    
    private long start;
    
    private long end;

    /**
     * @return the startMillis
     */
    public long getStartMillis() {
        return start;
    }

    /**
     * @param startMillis the startMillis to set
     */
    public void setStartMillis(long startMillis) {
        this.start = startMillis;
    }

    /**
     * @return the endMillis
     */
    public long getEndMillis() {
        return end;
    }

    /**
     * @param endMillis the endMillis to set
     */
    public void setEndMillis(long endMillis) {
        this.end = endMillis;
    }
    
}
