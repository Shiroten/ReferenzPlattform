package ExCells26.Squirrel;

import ExCells26.Helper.BotCom;
import ExCells26.Helper.Cell;
import ExCells26.Helper.Exceptions.FieldUnreachableException;
import ExCells26.Helper.Exceptions.FullFieldException;
import ExCells26.Helper.Exceptions.NoConnectingNeighbourException;
import ExCells26.Helper.Exceptions.NoTargetException;
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
        }

        if (view.getEnergy() < 100) {
            collectingReapers();
        }

        if (checkForDanger() < 4.3) {
            moveToCurrentCell();
            return;
        }

        if (botCom.isFieldLimitFound() && view.getRemainingSteps() > 100
                && maximumDensityOfNodes() && isSaturated()) {
            if (currentCell.getQuadrant().minus(view.locate()).length() > 7.1) {
                collectMiniOfCell();
                return;
            }
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
                    logger.log(Level.WARNING, "NoConnectingNeighbourException");
                    logger.log(Level.WARNING, e.getMessage());
                }
            }
        }

        if (currentCell.getQuadrant().minus(view.locate()).length() > 7.1) {
            collectMiniOfCell();

        }
    }

    private void enhanceDensityOfGrid() {
        //Todo: rework
    }

    private boolean isSaturated() {
        //Todo: rework
        return false;
    }

    private boolean maximumDensityOfNodes() {
        //Todo: rework
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
        //Todo: rework

        /*
        Cell start = currentCell;
        Cell toCheck = start;
        while (true) {
            toCheck = toCheck.getNextCell();
            if (toCheck.getMiniSquirrel() == null) {
                break;
            } else {
                toCheck = toCheck.getNextCell();
            }

            if (toCheck.equals(start)) {
                try {
                    botCom.expand();
                } catch (NoConnectingNeighbourException e) {
                    return;
                }
            }
        }
        spawningReaper(toCheck);
        changeCurrentCell();
        */
    }

    private boolean allActiveCellsAreUsed() {
        //Todo: rework
        return false;
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
        //Todo: remove after debugging
        //System.out.println("\nGo to nextCell: " + currentCell);
    }

    private void toDoAtStartOfNextStep(ControllerContext view) {
        botCom.positionOfExCellMaster = view.locate();
        botCom.checkAttendance(view.getRemainingSteps());
        this.view = view;
    }

    private void spawningReaper(Cell cellForMini) {
        try {
            botCom.setNextMiniTypeToSpawn(MiniType.REAPER);
            botCom.setCellForNextMini(cellForMini);
            XY spawnDirection = cellForMini.getNextCell().getQuadrant().minus(view.locate());
            spawnDirection = XYsupport.normalizedVector(spawnDirection).times(-1);
            if (view.getEntityAt(view.locate().plus(spawnDirection)) == EntityType.NONE) {
                view.spawnMiniBot(spawnDirection, 100);
                System.out.println("Spawning Mini");
            }
        } catch (SpawnException | OutOfViewException e) {
            logger.log(Level.WARNING, "OutOfViewException");
            logger.log(Level.WARNING, e.getMessage());
        }
    }

    private void moveToCurrentCell() {
        PathFinder pf = new PathFinder(botCom);
        XY betterMove = XY.ZERO_ZERO;
        try {
            betterMove = pf.directionTo(view.locate(), currentCell.getQuadrant(), view);
        } catch (FullFieldException e) {
            Logger logger = Logger.getLogger(Launcher.class.getName());
            logger.log(Level.WARNING, "FullFieldException");
            logger.log(Level.WARNING, e.getMessage());
            changeCurrentCell();
        } catch (FieldUnreachableException e) {
            Logger logger = Logger.getLogger(Launcher.class.getName());
            logger.log(Level.WARNING, "FieldUnreachableException");
            logger.log(Level.WARNING, e.getMessage());
            changeCurrentCell();
        }
        view.move(betterMove);
    }

    private void collectingReapers() {
        XY middle = new XY(botCom.getFieldLimit().x / 2, botCom.getFieldLimit().y / 2);
        PathFinder pf = new PathFinder(botCom);
        XY toMove = XY.ZERO_ZERO;
        try {
            toMove = pf.directionTo(view.locate(), middle, view);
        } catch (FullFieldException e) {
            logger.log(Level.WARNING, "FullFieldException");
            logger.log(Level.WARNING, e.getMessage());
        } catch (FieldUnreachableException e) {
            logger.log(Level.WARNING, "FieldUnreachableException");
            logger.log(Level.WARNING, e.getMessage());
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
            logger.log(Level.WARNING, "NoConnectingNeighbourException");
            logger.log(Level.WARNING, e.getMessage());
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
