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

    }
}
