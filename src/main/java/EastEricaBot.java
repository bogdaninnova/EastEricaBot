import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class EastEricaBot  extends TelegramLongPollingBot {

    private static final String botUsername = "EastEricaBot";
    private static final String botToken = "858313302:AAGyCP8vfiuPBetMhdY9WRymza6PlKuv7sA";
    private Game game;

    private static final String admin = "bogdaninnova";

    @Override
    public void onUpdateReceived(Update update) {

        String text = update.getMessage().getText();
        User user = update.getMessage().getFrom();
        long chatId = update.getMessage().getChatId();

        System.out.println(text);
        System.out.println(user);
        System.out.println(chatId);

        if (admin.equals(user.getUserName()) && text.equals("/startNewGame")) {
            sendSimpleMessage("Пишите мне в лс слова", chatId);
            game = new Game(chatId);
            return;
        }

        if (game == null) {
            sendSimpleMessage("Игра ещё не началась!", chatId);
            return;
        }

        if (!game.getUsers().containsKey(user.getUserName()))
            return;

        if (!game.isGameStarted() && user.getId() == chatId && text.equals("/mywords")) {

        }

        if (admin.equals(user.getUserName()) && text.equals("/startNewRound")) {
            game.resetWordsLeft();
            game.setGameStarted(true);
            sendSimpleMessage("Начало нового раунда", chatId);
            sendSimpleMessage("Сейчас ход игрока @" + game.getCurrentUser() + "!", chatId);
            sendSimpleMessage("Привет, сейчас твой ход!\nЖми /start чтобы начать.", game.getUsers().get(game.getCurrentUser()));
            return;
        }

        if (user.getUserName().equals(game.getCurrentUser()) && !game.isActivePhase() && text.equals("/start")) {
            sendSimpleMessage(game.getRandomWord(), user.getId());
            game.setActivePhase(true);
            game.removeWord();
            new GameTimer(this);
        }

        if (game.isActivePhase() && user.getId() == chatId && user.getUserName().equals(game.getCurrentUser()) && text.equals("next")) {

            if (game.getCurrentWord() != null)
                sendSimpleMessage("Отгаданное слово: " + game.getCurrentWord(), game.getChatId());

            if (game.isEmptyWordSet()) {
                finishTurn();
            } else {
                sendSimpleMessage(game.getRandomWord(), user.getId());
                game.removeWord();
            }
        }

        if (!game.isGameStarted()) {
            if (user.getId() == chatId)
                game.addWordToAll(text);
            //return;
        }
    }


    public void finishTurn() {
        game.setActivePhase(false);
        if (game.isWordSetEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Конец раунда, количество угаданных слов:");
            for (String player : game.getStatistics().keySet()) {
                sb.append("\nИгрок @");
                sb.append(player);
                sb.append(": ");
                sb.append(game.getStatistics().get(player));
            }
            sendSimpleMessage(sb.toString(), game.getChatId());
            sendSimpleMessage("Конец раунда!", game.getUsers().get(game.getCurrentUser()));
            game.nextPlayer();
        } else {
            sendSimpleMessage("Время вышло!", game.getUsers().get(game.getCurrentUser()));
            sendSimpleMessage("Время вышло!", game.getChatId());
            game.nextPlayer();
            sendSimpleMessage("Сейчас ход игрока @" + game.getCurrentUser() + "!", game.getChatId());
            sendSimpleMessage("Привет, сейчас твой ход!\nЖми /start чтобы начать.", game.getUsers().get(game.getCurrentUser()));
        }


    }


    public void sendSimpleMessage(String text, long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public Game getGame() {
        return game;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
