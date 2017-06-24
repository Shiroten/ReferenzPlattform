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
    private int fleeing = 0;
    private boolean fullField = false;
    private int test = 10;

    public ExCells26Master(BotCom botCom) {
        this.botCom = botCom;
    }

    @Override
    public void nextStep(ControllerContext view) {
        toDoAtStartOfNextStep(view);
        if (firstCall) {
            initOfMaster(view);
        }
        if (!fullField) {
            try {
                botCom.expand();
            } catch (NoConnectingNeighbourException e) {
                fullField = true;
            }
        }
        if (currentCell.getQuadrant().minus(view.locate()).length() < 5.7) {
            spawningReaper(currentCell);
            changeCurrentCell();
        }
        if (test % 10 == 1){
            spawningReaper(currentCell);
            changeCurrentCell();
            test++;
        }

        moveToCurrentCell();
    }

    public void nextStep2(ControllerContext view) {
        Logger logger2 = Logger.getLogger(Launcher.class.getName());
        logger2.log(Level.FINE, "Starting NextStep");

        toDoAtStartOfNextStep(view);
        if (firstCall) {
            initOfMaster(view);
        }

        XY positionOfNextBadBeast;
        try {
            positionOfNextBadBeast = SquirrelHelper.findNextBadBeast(view);
        } catch (NoTargetException e) {
            positionOfNextBadBeast = new XY(999, 999);
        }

        if (fleeing > 0) {
            //Move away from BadBeast to safely execute the rest of the implementation
            XY toMove = null;
            try {
                toMove = SquirrelHelper.safeField(view);
            } catch (NoTargetException e) {
                Logger logger = Logger.getLogger(Launcher.class.getName());
                logger.log(Level.FINE, e.getMessage());
            }

            PathFinder pf = new PathFinder(botCom);
            try {
                view.move(pf.directionTo(view.locate(), toMove, view));
            } catch (FullFieldException e) {
                currentCell.setUsableCell(false);
                changeCurrentCell();
            } catch (FieldUnreachableException e) {
                changeCurrentCell();
            }
            fleeing--;
        }

        if (positionOfNextBadBeast.minus(view.locate()).length() < 2.9) {
            fleeing = 10;
            changeCurrentCell();
        } else {
            if (view.getEnergy() > 500) {
                spawnMoreMinis();
            }

            if (view.getRemainingSteps() < 100) {
                collectingReapers();
                return;
            }

            if (currentCell.isInside(view.locate(), botCom) && firstTimeInCell) {
                collectMiniOfCell();
                return;
            }

            if (currentCell.getQuadrant().equals(view.locate()) && !firstTimeInCell) {
                try {
                    botCom.expand();
                    if (view.getEnergy() >= 100) {
                        if (currentCell.getMiniSquirrel() == null) {
                            spawningReaper(currentCell);
                        }
                    } else {
                        //maybe something better
                        changeCurrentCell();
                    }
                } catch (NoConnectingNeighbourException e) {
                    Logger logger = Logger.getLogger(Launcher.class.getName());
                    logger.log(Level.FINE, e.getMessage());
                }
                changeCurrentCell();
            }
            moveToCurrentCell();
        }
    }

    private void spawnMoreMinis() {
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
            } else {
                //Todo: adding can't spawn
            }
        } catch (SpawnException | OutOfViewException e) {
            //Todo: add to Log
            e.printStackTrace();
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
            //Todo: add to Log
            //e.printStackTrace();
        } catch (FieldUnreachableException e) {
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

        }
        botCom.setNextMiniTypeToSpawn(MiniType.RECON);
        for (XY direction : XYsupport.directions()) {
            if (view.getEntityAt(view.locate().plus(direction)) == EntityType.NONE) {
                view.spawnMiniBot(direction, 100);
            }
        }
        firstCall = false;
    }

    public Cell getCurrentCell() {
        return currentCell;
    }

    public void setCurrentCell(Cell currentCell) {
        this.currentCell = currentCell;
    }


}
