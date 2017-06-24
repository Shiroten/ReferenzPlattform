package ExCells26.Helper;


import ExCells26.Helper.Exceptions.FullGridException;
import ExCells26.Helper.Exceptions.NoConnectingNeighbourException;
import ExCells26.Squirrel.ExCells26Master;
import ExCells26.Squirrel.Mini.MiniType;
import de.hsa.games.fatsquirrel.core.bot.BotController;
import de.hsa.games.fatsquirrel.utilities.XY;

import java.util.ArrayList;
import java.util.Hashtable;


public class BotCom {

    private ExCells26Master master;

    Hashtable<XY, Cell> getGrid4() {
        return grid4;
    }

    public ArrayList<Cell> getGrid() {
        return grid;
    }

    public Cell getNextRallyPoint(XY coordinate) {
        Cell temp = null;
        for (Cell cell : new ArrayList<>(grid)) {
            System.out.println(temp);
            System.out.println(cell);

            if (temp == null) {
                temp = cell;
            } else if (XYsupport.distanceInSteps(coordinate,temp.getQuadrant()) >
                    XYsupport.distanceInSteps(coordinate,cell.getQuadrant())) {
                temp = cell;
            }
        }
        return temp;
    }

    ArrayList<Cell> grid = new ArrayList<>();

    public Hashtable<XY, Cell> grid4 = new Hashtable<>();
    private MiniType nextMiniTypeToSpawn;
    private Cell cellForNextMini;
    private XY fieldLimit;

    private boolean fieldLimitFound;
    XY startPositionOfMaster;
    public XY positionOfExCellMaster;

    //Default: 21 (last working number)
    int cellDistanceX = 15;
    int cellDistanceY = 15;

    //Default: 11 (last working number)
    private int cellCenterOffsetX = 7;
    private int cellCenterOffsetY = 7;

    //Default: 21 (last working number)
    private int cellsize = 15;

    public int getCellDistanceX() {
        return cellDistanceX;
    }

    public int getCellDistanceY() {
        return cellDistanceY;
    }

    public BotController getMaster() {
        return master;
    }

    public void setMaster(ExCells26Master master) {
        this.master = master;
    }

    public void setNextMiniTypeToSpawn(MiniType nextMiniTypeToSpawn) {
        this.nextMiniTypeToSpawn = nextMiniTypeToSpawn;
    }

    public XY getFieldLimit() {
        return fieldLimit;
    }

    public void setFieldLimit(XY fieldLimit) {
        this.fieldLimit = fieldLimit;
    }

    public MiniType getNextMiniTypeToSpawn() {
        return nextMiniTypeToSpawn;
    }

    public int getCellsize() {
        return cellsize;
    }

    public void setCellsize(int cellsize) {
        this.cellsize = cellsize;
    }

    public boolean isFieldLimitFound() {
        return fieldLimitFound;
    }

    public void setFieldLimitFound() {
        this.fieldLimitFound = true;
    }

    public void setStartPositionOfMaster(XY startPositionOfMaster) {
        this.startPositionOfMaster = startPositionOfMaster;
    }

    public Cell getCellForNextMini() {
        return cellForNextMini;
    }

    public void setCellForNextMini(Cell cellForNextMini) {
        this.cellForNextMini = cellForNextMini;
    }




    XY cellAt(XY position) {
        //Range from 1<-11->21, 22<-32->42, 43<-53->63, 64<-74->84, 85<-95->105, ...
        //Modulo Operation with 21

        int xFactor = (position.x - 1) / cellDistanceX; //calculates 4 for 105 and 5 for 126
        int yFactor = (position.y - 1) / cellDistanceY;

        //returns the middle position of the cell by adding 11 after multiplying
        return new XY(cellDistanceX * xFactor + cellCenterOffsetX, cellDistanceY * yFactor + cellCenterOffsetY);
    }


    private boolean validCell(XY coordinate) {
        return !(coordinate.x < 0 || coordinate.x > fieldLimit.x) && !(coordinate.y < 0 || coordinate.y > fieldLimit.y);
    }

    public void addRallyPoint(XY coordinate) {
        if (grid.size() > 0) {
            Cell temp = new Cell(coordinate);
            grid.get(grid.size()-1).setNextCell(temp);
            grid.add(temp);
            temp.setNextCell(grid.get(0));
        } else {
            Cell temp = new Cell(coordinate);
            temp.setNextCell(temp);
            grid.add(temp);
        }
    }




}
