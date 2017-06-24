package ExCells26;


import ExCells26.Helper.BotCom;
import ExCells26.Squirrel.ExCells26Master;
import ExCells26.Squirrel.Mini.ExCells26FeralMini;
import ExCells26.Squirrel.Mini.ExCells26ReaperMini;
import ExCells26.Squirrel.Mini.ExCells26ReconMini;
import de.hsa.games.fatsquirrel.core.bot.BotController;
import de.hsa.games.fatsquirrel.core.bot.BotControllerFactory;

public class BotControllerFactoryImpl implements BotControllerFactory {

    private BotCom botcom;

    public BotControllerFactoryImpl() {
        botcom = new BotCom();
        //Initialisierung von BotCom und allen wichtigen Klassen
    }

    public BotController createMasterBotController() {
        ExCells26Master master = new ExCells26Master(botcom);
        botcom.setMaster(master);
        return master;
    }

    public BotController createMiniBotController() {
        //Todo: Alternative, ein MiniSquirrel das unterschiedliche Kis in abhängigkeit einer Einstellung verwendet

        BotController mini;
        switch (botcom.getNextMiniTypeToSpawn()) {
            case RECON:
                mini = new ExCells26ReconMini(botcom);
                break;
            case REAPER:
                mini = new ExCells26ReaperMini(botcom);
                break;
            case FERAL:
                mini = new ExCells26FeralMini(botcom);
                break;
            default:
                mini = new ExCells26ReaperMini(botcom);
        }
        return mini;
    }
}
