import java.util.Timer;
import java.util.TimerTask;

public class GameTimer {

    private static final long firstRemindTime = 1000;
    private static final long lastRemindTime = 5000;
    private static final long stopRoundTime = 15000;

    private String userName;

    public GameTimer(final EastEricaBot bot) {
        userName = bot.getGame().getCurrentUser();
        TimerTask firstRemainder = new TimerTask() {
            public void run() {
                if (bot.getGame().isActivePhase() && userName.equals(bot.getGame().getCurrentUser()))
                    bot.sendSimpleMessageLeaveMarkup("Осталось 15 секунд!", bot.getGame().getChatId());
                    bot.sendSimpleMessageLeaveMarkup("Осталось 15 секунд!", bot.getGame().getUsers().get(bot.getGame().getCurrentUser()));
            }
        };
        TimerTask lastRemainder = new TimerTask() {
            public void run() {
                if (bot.getGame().isActivePhase() && userName.equals(bot.getGame().getCurrentUser()))
                    bot.sendSimpleMessageLeaveMarkup("Осталось 5 секунд!", bot.getGame().getChatId());
                    bot.sendSimpleMessageLeaveMarkup("Осталось 5 секунд!", bot.getGame().getUsers().get(bot.getGame().getCurrentUser()));
            }
        };
        TimerTask stopRound = new TimerTask() {
            public void run() {
                if (bot.getGame().isActivePhase() && userName.equals(bot.getGame().getCurrentUser())) {
                    bot.getGame().restoreLastWord();
                    bot.finishTurn();
                }
                Thread.currentThread().stop();
            }
        };

        Timer timer = new Timer("Timer");
        timer.schedule(firstRemainder, firstRemindTime);
        timer.schedule(lastRemainder, lastRemindTime);
        timer.schedule(stopRound, stopRoundTime);
    }


}
