import java.util.Timer;
import java.util.TimerTask;

public class GameTimer {

    private static final long firstRemindTime = 5000;
    private static final long lastRemindTime = 10000;
    private static final long stopRoundTime = 15000;

    private String userName;

    public GameTimer(final EastEricaBot bot, final Game game) {
        userName = game.getCurrentUser();
        TimerTask firstRemainder = new TimerTask() {
            public void run() {
                if (game.isActivePhase() && userName.equals(game.getCurrentUser())) {
                    bot.sendSimpleMessageLeaveMarkup("15 seconds left!", game.getChatId());
                    bot.sendSimpleMessageLeaveMarkup("15 seconds left!", EastEricaBot.usersList.getUserId(game.getCurrentUser()));
                }
            }
        };
        TimerTask lastRemainder = new TimerTask() {
            public void run() {
                if (game.isActivePhase() && userName.equals(game.getCurrentUser())) {
                    bot.sendSimpleMessageLeaveMarkup("5 seconds left!", game.getChatId());
                    bot.sendSimpleMessageLeaveMarkup("5 seconds left!", EastEricaBot.usersList.getUserId(game.getCurrentUser()));
                }
            }
        };
        TimerTask stopRound = new TimerTask() {
            public void run() {
                if (game.isActivePhase() && userName.equals(game.getCurrentUser())) {
                    game.restoreLastWord();
                    bot.finishTurn(game);
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
