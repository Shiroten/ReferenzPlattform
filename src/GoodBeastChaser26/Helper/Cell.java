package GoodBeastChaser26.Helper;


import ExCells26.Mini.ExCells26ReaperMini;
import GoodBeastChaser26.GoodBeastChaser26Master;
import GoodBeastChaser26.GoodBeastChaser26Mini;
import de.hsa.games.fatsquirrel.utilities.XY;

import java.util.Hashtable;

/**
 * Created by Shiroten on 15.06.2017.
 */
public class Cell {

    private XY quadrant;
    private long lastFeedback;
    private GoodBeastChaser26Mini miniSquirrel;
    private boolean isActive = false;
    private boolean usableCell = true;

    public boolean isUsableCell() {
        return usableCell;
    }

    public void setUsableCell(boolean usableCell) {
        this.usableCell = usableCell;
    }

    public boolean isActive() {
        return isActive;
    }

    public Cell getNextCell() {
        return nextCell;
    }

    public void setNextCell(Cell nextCell) {
        this.nextCell = nextCell;
    }

    private Cell nextCell;
    private Hashtable<XY, Cell> neighbours = new Hashtable<>();

    public Cell(XY position) {
        this.quadrant = position;
    }

    public XY getQuadrant() {
        return quadrant;
    }

    public void setQuadrant(XY quadrant) {
        this.quadrant = quadrant;
    }

    public long getLastFeedback() {
        return lastFeedback;
    }

    public void setLastFeedback(long lastFeedback) {
        this.lastFeedback = lastFeedback;
    }

    public GoodBeastChaser26Mini getMiniSquirrel() {
        return miniSquirrel;
    }

    public void setMiniSquirrel(GoodBeastChaser26Mini miniSquirrel) {
        this.miniSquirrel = miniSquirrel;
    }

    public void addNeighbour(Cell toAdd) {
        neighbours.put(toAdd.getQuadrant(), toAdd);
    }

    public Cell getNeighbour(XY position) {
        return neighbours.get(position);
    }

    public Cell getConnectingCell() {
        for (Cell c : neighbours.values()) {
            if (c.isActive()) {
                continue;
            }
            if (c.getNeighbour(nextCell.getQuadrant()) != null) {
                return c;
            }
        }
        return null;
    }

    public void setActive(Cell nextCell) {
        isActive = true;
        this.nextCell = nextCell;
    }

    public boolean isInside(XY target, BotCom botCom) {
        /*
        if (Math.abs((myCell.getQuadrant().x - target.x)) > 10) {
            return false;
        }
        if (Math.abs((myCell.getQuadrant().y - target.y)) > 10) {
            return false;
        }
        */
        //Original Version:

        if (Math.abs((this.getQuadrant().x - target.x)) > botCom.getCellsize() / 2) {
            return false;
        }
        if (Math.abs((this.getQuadrant().y - target.y)) > botCom.getCellsize() / 2) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Position: " + quadrant + " is active: " + isActive());
        sb.append("\n\tNextCell: ");
        if (nextCell == null){
            sb.append("none");
        }else{
            sb.append(nextCell.getQuadrant());
        }

        int index = 0;
        for (Cell c : neighbours.values()) {
            index++;
            sb.append("\n\t" + index + ". Neighbour: " + c.getQuadrant() + " is active: " + c.isActive());
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Cell)) {
            return false;
        }
        return ((Cell) o).getQuadrant().equals(quadrant);
    }
}
