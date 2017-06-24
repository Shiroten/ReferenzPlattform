package ExCells26.Helper;


import ExCells26.Squirrel.Mini.ExCells26ReaperMini;
import de.hsa.games.fatsquirrel.core.bot.ControllerContext;
import de.hsa.games.fatsquirrel.core.entities.EntityType;
import de.hsa.games.fatsquirrel.utilities.XY;

import java.util.Hashtable;

/**
 * Created by Shiroten on 15.06.2017.
 */
public class Cell {

    private XY quadrant;
    private long lastFeedback;
    private ExCells26ReaperMini miniSquirrel;
    private boolean isActive = false;
    private boolean usableCell = true;
    private Cell nextCell;
    private Hashtable<XY, Cell> neighbours = new Hashtable<>();
    private boolean verified;

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

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

    public ExCells26ReaperMini getMiniSquirrel() {
        return miniSquirrel;
    }

    public void setMiniSquirrel(ExCells26ReaperMini miniSquirrel) {
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

        return Math.abs((this.getQuadrant().x - target.x)) <= botCom.getCellsize() / 2 && Math.abs((this.getQuadrant().y - target.y)) <= botCom.getCellsize() / 2;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Position: ").append(quadrant).append(" is active: ").append(isActive());
        sb.append("\n\tNextCell: ");
        if (nextCell == null){
            sb.append("none");
        }else{
            sb.append(nextCell.getQuadrant());
        }

        int index = 0;
        for (Cell c : neighbours.values()) {
            index++;
            sb.append("\n\t").append(index).append(". Neighbour: ").append(c.getQuadrant()).append(" is active: ").append(c.isActive());
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Cell && ((Cell) o).getQuadrant().equals(quadrant);
    }

    public void calculateDensity(ControllerContext controllerContext, BotCom botCom){
        XY upperLeftCorner = quadrant.plus(XY.LEFT_UP.times(botCom.getCellsize() / 2));
        XY lowerRightCorner = quadrant.plus(XY.RIGHT_DOWN.times(botCom.getCellsize()/2));
        double cellArea = botCom.getCellsize()*2;
        double wallCount = 0;
        for(int i = controllerContext.getViewUpperLeft().x; i < controllerContext.getViewLowerRight().x; i++){
            for(int j = controllerContext.getViewUpperLeft().y; j < controllerContext.getViewLowerRight().y; j++){
                if(XYsupport.isInRange(new XY(i, j),upperLeftCorner , lowerRightCorner)
                        && controllerContext.getEntityAt(new XY(i, j)) == EntityType.WALL){
                    wallCount++;
                }
            }
        }

        if(wallCount / cellArea > 0.3)
            setUsableCell(false);
    }
}
