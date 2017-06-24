package ExCells26.Squirrel;

import ExCells26.Helper.BotCom;
import ExCells26.Helper.Cell;
import ExCells26.Helper.Exceptions.*;
import ExCells26.Helper.PathFinder;
import ExCells26.Helper.XYsupport;
import ExCells26.Squirrel.Mini.ExCells26ReaperMini;
import ExCells26.Squirrel.Mini.MiniType;
import de.hsa.games.fatsquirrel.Launcher;
import de.hsa.games.fatsquirrel.core.actions.OutOfViewException;
import de.hsa.games.fatsquirrel.core.actions.SpawnException;
import de.hsa.games.fatsquirrel.core.bot.BotController;
import de.hsa.games.fatsquirrel.core.bot.ControllerContext;
import de.hsa.games.fatsquirrel.core.entities.EntityType;
import de.hsa.games.fatsquirrel.utilities.XY;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ExCells26Master implements BotController {

    private BotCom botCom;
    private Cell currentCell;
    private boolean firstCall = true;
    private ControllerContext view;
    private ExCells26ReaperMini miniOfCurrentCell;
    private boolean firstTimeInCell = true;
    private Logger logger = Logger.getLogger(Launcher.class.getName());

    public Cell getCurrentCell() {
        return currentCell;
    }

    public void setCurrentCell(Cell currentCell) {
        this.currentCell = currentCell;
    }

    public ExCells26Master(BotCom botCom) {
        this.botCom = botCom;
    }

    @Override
    public void nextStep(ControllerContext view) {
        toDoAtStartOfNextStep(view);
        if (firstCall) {
            initOfMaster(view);
            return;
        }

        if (view.getRemainingSteps() < 100) {
            collectingReapers();
        }

        if (checkForDanger() < 4.3) {
            moveToCurrentCell();
            return;
        }

        try {
            if (view.getEnergy() > 1000 && botCom.isFreeCell()) {
                spawningReaper(botCom.freeCell(), 200);
                return;
            } else if (view.getEnergy() > 1000) {
                botCom.getAllCells();
                botCom.expand();
                spawningReaper(botCom.freeCell(), 200);
                return;
            }
        } catch (FullGridException e) {
            logger.log(Level.WARNING, "FullGridException of nextStep");
        } catch (NoConnectingNeighbourException e) {
            logger.log(Level.WARNING, "NoConnectingNeighbourException of nextStep");
        }

        if (!(botCom.isFieldLimitFound() && view.getRemainingSteps() > 100
                && maximumDensityOfNodes() && isSaturated())) {
            if (currentCell.getQuadrant().minus(view.locate()).length() < 10) {
                if (firstTimeInCell && currentCell.getMiniSquirrel() != null) {
                    collectMiniOfCell();
                    return;
                }
                if (currentCell.getMiniSquirrel() == null && view.getEnergy() > 200) {
                    spawningReaper(currentCell, 200);
                    changeCurrentCell();
                    return;
                }
            }
            moveToCurrentCell();
            return;

        }
        getMoreMinis();

    }


    private void getMoreMinis() {
        if (!maximumDensityOfNodes()) {
            enhanceDensityOfGrid();
        }

        if (allActiveCellsAreUsed()) {
            if (botCom.isFreeCell()) {
                spawnMoreMinis();
            } else {
                try {
                    botCom.expand();
                } catch (NoConnectingNeighbourException e) {
                    logger.log(Level.WARNING, "NoConnectingNeighbourException of getMoreMinis");
                }
            }
        }

        if (currentCell.getQuadrant().minus(view.locate()).length() < 7.1) {
            collectMiniOfCell();
        }
    }

    private void enhanceDensityOfGrid() {
        //Todo: rework
        botCom.calculateCellSize();
    }

    private boolean isSaturated() {
        //Todo: rework
        return false;
    }

    private boolean maximumDensityOfNodes() {
        int treshold = 5;
        if (botCom.getCellDistanceX() < treshold && botCom.getCellDistanceY() < treshold) {
            return true;
        }
        return false;
    }

    private double checkForDanger() {
        try {
            XY positionOfNearestBadBeast = SquirrelHelper.findNextBadBeast(view);
            return positionOfNearestBadBeast.minus(view.locate()).length();
        } catch (NoTargetException e) {
            return Double.POSITIVE_INFINITY;
        }
    }

    private void spawnMoreMinis() {
        try {
            if (view.getEnergy() > 200)
                spawningReaper(botCom.freeCell(), 200);
        } catch (FullGridException e) {
            logger.log(Level.WARNING, "FullGridException of spawnMoreMinis");
            logger.log(Level.WARNING, e.getMessage());
        }
    }

    private boolean allActiveCellsAreUsed() {
        //Checks if all possible nodes for cells within parameters of cellDistance are used
        for (Cell cell : botCom.grid.values()) {
            if (cell.isUsableCell() && !cell.isActive()) {
                return false;
            }
        }
        return true;
    }

    private void collectMiniOfCell() {
        firstTimeInCell = false;
        if (currentCell.getMiniSquirrel() != null) {
            miniOfCurrentCell = currentCell.getMiniSquirrel();
            miniOfCurrentCell.setMyCell(null);
            miniOfCurrentCell.setGoToMaster();
            currentCell.setMiniSquirrel(null);
        }
    }

    private void changeCurrentCell() {
        firstTimeInCell = true;
        currentCell = currentCell.getNextCell();
        //System.out.println("\nGo to nextCell: " + currentCell);
    }

    private void toDoAtStartOfNextStep(ControllerContext view) {
        botCom.positionOfExCellMaster = view.locate();
        botCom.checkAttendance(view.getRemainingSteps());
        this.view = view;
    }

    private void spawningReaper(Cell cellForMini, int startEnergy) {
        try {
            botCom.setNextMiniTypeToSpawn(MiniType.REAPER);
            botCom.setCellForNextMini(cellForMini);
            XY spawnDirection = cellForMini.getNextCell().getQuadrant().minus(view.locate());
            spawnDirection = XYsupport.normalizedVector(spawnDirection).times(-1);
            if (view.getEntityAt(view.locate().plus(spawnDirection)) == EntityType.NONE) {
                view.spawnMiniBot(spawnDirection, startEnergy);
            }
        } catch (SpawnException | OutOfViewException e) {
            logger.log(Level.WARNING, "OutOfViewException of spawningReaper");
        }
    }

    private void moveToCurrentCell() {
        PathFinder pf = new PathFinder(botCom);
        XY betterMove = XY.ZERO_ZERO;
        try {
            betterMove = pf.directionTo(currentCell.getQuadrant(), view, false);
        } catch (FullFieldException e) {
            try {
                betterMove = SquirrelHelper.tryAgain(currentCell.getQuadrant(), view, pf);
            } catch (FullFieldException e1) {
                logger.log(Level.WARNING, "FullFieldException of moveToCurrentCell");
                logger.log(Level.WARNING, "Position: " + view.locate().toString());
                logger.log(Level.WARNING, "Destination: " + currentCell.getQuadrant());
                changeCurrentCell();
            }
        } catch (FieldUnreachableException e) {
            logger.log(Level.WARNING, "FieldUnreachableException of moveToCurrentCell");
            logger.log(Level.WARNING, "Position: " + view.locate().toString());
            logger.log(Level.WARNING, "Destination: " + currentCell.getQuadrant());
            changeCurrentCell();
        }
        view.move(betterMove);
    }

    private void collectingReapers() {
        XY middle = new XY(botCom.getFieldLimit().x / 2, botCom.getFieldLimit().y / 2);
        PathFinder pf = new PathFinder(botCom);
        XY toMove = XY.ZERO_ZERO;
        try {
            toMove = pf.directionTo(middle, view, false);
        } catch (FullFieldException e) {
            logger.log(Level.WARNING, "FullFieldException of collectingReapers");
        } catch (FieldUnreachableException e) {
            logger.log(Level.WARNING, "FieldUnreachableException of collectingReapers");
        }
        view.move(XYsupport.normalizedVector(toMove));
    }

    private void initOfMaster(ControllerContext view) {
        botCom.setStartPositionOfMaster(view.locate());
        //Todo: set right after implementation of reconMini
        //botCom.setFieldLimit(view.locate());
        botCom.setFieldLimit(new XY(80, 60));
        botCom.setMaster(this);
        botCom.init();
        botCom.calculateCellSize();
        botCom.getAllCells();

        try {
            for (int i = 0; i < 4; i++) {
                botCom.expand();
            }
        } catch (NoConnectingNeighbourException e) {
            logger.log(Level.WARNING, "NoConnectingNeighbourException of initOfMaster");
        }
        botCom.setNextMiniTypeToSpawn(MiniType.RECON);
        for (XY direction : XYsupport.directions()) {
            if (view.getEntityAt(view.locate().plus(direction)) == EntityType.NONE) {
                view.spawnMiniBot(direction, 100);
            }
        }
        firstCall = false;
    }


}
