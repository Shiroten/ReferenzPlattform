package ExCells26.Squirrel.Mini;


import ExCells26.Helper.*;
import ExCells26.Helper.Exceptions.FieldUnreachableException;
import ExCells26.Helper.Exceptions.FullFieldException;
import ExCells26.Helper.Exceptions.NoTargetException;
import de.hsa.games.fatsquirrel.core.bot.BotController;
import de.hsa.games.fatsquirrel.core.bot.ControllerContext;
import de.hsa.games.fatsquirrel.utilities.XY;

/**
 * Created by Shiroten on 15.06.2017.
 */
public class ExCells26FeralMini extends ExCells26ReaperMini implements BotController {

    public ExCells26FeralMini(BotCom botCom) {
        super(botCom);
    }

    @Override
    public void nextStep(ControllerContext view) {
        if (view.getRemainingSteps() < 200) {
            goToMaster = true;
        }

        if (view.getEnergy() > 5000) {
            setGoToMaster();
        }

        if (goToMaster) {
            executeGoToMaster(view);
            return;
        }

        XY toMove;
        try {
            toMove = calculateTarget(view);

        } catch (NoTargetException e) {
            toMove = XY.ZERO_ZERO;

            /*
            try {
                toMove = botCom.freeCell().getQuadrant();
            } catch (FullGridException e1) {
                toMove = botCom.positionOfExCellMaster.minus(view.locate()).times(-1);
            }
            */
        }

        PathFinder pf = new PathFinder(botCom);
        try {
            view.move(pf.directionTo(view.locate(), toMove, view));
        } catch (FullFieldException | FieldUnreachableException e) {
            //Worst Case no good possible Move
            view.move(XY.ZERO_ZERO);
        }

    }
}
