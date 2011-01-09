/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package thirteenducks.cor.game.client;

/**
 *
 * @author Johannes
 */
public class ClientStatistics {
    ClientCore.InnerClient rgi;
    int[] rescollected = new int[6]; // Wieviel von jeder Ressource gesammelt worden ist
    public int[][] collectedstats; //Sammelt am Ende vom Spiel alle Statistiken

    public ClientStatistics(ClientCore.InnerClient inner) {
        rgi = inner;
    }

    //Am Anfang des Spiels mit Spielerzahl initialisieren
    public void createStatArrays(int playernumber) {
	collectedstats = new int[playernumber + 1][12];
    }

    //Am Ende des Spiels Statistiken vom Server empfangen
    public void collectStatistics(int type, int value, int player) {
	collectedstats[player][type]=value;
    }

    //Während dem Spiel Statistiken audzeichnen
    public void trackRes(int ResType, int ResAmount) {
	rescollected[ResType] += ResAmount;
    }
}
