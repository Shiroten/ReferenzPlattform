package GoodBeastChaser26;

import GoodBeastChaser26.Helper.BotCom;
import de.hsa.games.fatsquirrel.core.bot.BotController;
import de.hsa.games.fatsquirrel.core.bot.BotControllerFactory;

public class BotControllerFactoryImpl implements BotControllerFactory {

    private BotCom botcom;

    public BotControllerFactoryImpl() {
        botcom = new BotCom();
        //Initialisierung von BotCom und allen wichtigen Klassen
    }

    public BotController createMasterBotController() {
        GoodBeastChaser26Master master = new GoodBeastChaser26Master(botcom);
        botcom.setMaster(master);
        return master;
    }

    public BotController createMiniBotController() {
        //Todo: Alternative, ein MiniSquirrel das unterschiedliche Kis in abhängigkeit einer Einstellung verwendet

        BotController mini;
        switch (botcom.getNextMiniTypeToSpawn()) {
            case REAPER:
                mini = new GoodBeastChaser26Mini(botcom);
                break;
            default:
                mini = new GoodBeastChaser26Mini(botcom);
        }
        return mini;
    }
}
