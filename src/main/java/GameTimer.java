import java.util.Timer;
import java.util.TimerTask;

public class GameTimer {

    private static final long firstRemindTime = 45000;
    private static final long lastRemindTime = 55000;
    private static final long stopRoundTime = 60000;

    private String userName;

    public GameTimer(final EastEricaBot bot) {
        userName = bot.getGame().getCurrentUser();
        TimerTask firstRemainder = new TimerTask() {
            public void run() {
                if (bot.getGame().isActivePhase() && userName.equals(bot.getGame().getCurrentUser()))
                    bot.sendSimpleMessageLeaveMarkup("15 seconds left!", bot.getGame().getChatId());
                    bot.sendSimpleMessageLeaveMarkup("15 seconds left!", bot.getGame().getUsers().get(bot.getGame().getCurrentUser()));
            }
        };
        TimerTask lastRemainder = new TimerTask() {
            public void run() {
                if (bot.getGame().isActivePhase() && userName.equals(bot.getGame().getCurrentUser()))
                    bot.sendSimpleMessageLeaveMarkup("5 seconds left!", bot.getGame().getChatId());
                    bot.sendSimpleMessageLeaveMarkup("5 seconds left!", bot.getGame().getUsers().get(bot.getGame().getCurrentUser()));
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
