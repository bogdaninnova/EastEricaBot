import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

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

//        //System.out.println(update.getMessage().getReplyToMessage().getText());
//        System.out.println(text);
//        System.out.println(user);
//        System.out.println(chatId);

        if (admin.equals(user.getUserName()) && text.equals("/startNewGame")) {
            sendSimpleMessage("Пишите мне персонажей в лс", chatId);
            game = new Game(chatId);
            for (String userName : game.getUsers().keySet())
                sendSimpleMessage("Привет! Загадывай персонажей (отдельными сообщениями)", game.getUsers().get(userName));
            return;
        }

        if (game == null) {
            sendSimpleMessage("Игра ещё не началась!", chatId);
            return;
        }

        if (!game.getUsers().containsKey(user.getUserName()))
            return;

        if (admin.equals(user.getUserName()) && text.equals("/startNewRound")) {
            game.resetWordsLeft();
            game.setGameStarted(true);
            sendSimpleMessage("Начало нового раунда", chatId);
            sendSimpleMessage("Ход игрока @" + game.getCurrentUser() + "! Ждём готовности.", chatId);
            sendSimpleMessage("Привет, сейчас твой ход!\nЖми 'Начать' когда будешь готов", "Начать", game.getUsers().get(game.getCurrentUser()));
            return;
        }

        if (user.getId() == chatId && user.getUserName().equals(game.getCurrentUser()) && !game.isActivePhase() && text.equals("Начать")) {
            sendSimpleMessage(game.getRandomWord(), "Следующий Персонаж", user.getId());
            sendSimpleMessage("Начали! Ход игрока @" + game.getCurrentUser(), game.getChatId());
            game.setActivePhase(true);
            game.removeWord();
            new GameTimer(this);
        }

        if (game.isActivePhase() && user.getId() == chatId && user.getUserName().equals(game.getCurrentUser()) && text.equals("Следующий Персонаж")) {

            if (game.getCurrentWord() != null)
                sendSimpleMessage("Отгаданный персонаж: " + game.getCurrentWord(), game.getChatId());

            if (game.isEmptyWordSet()) {
                finishTurn();
            } else {
                sendSimpleMessage(game.getRandomWord(), "Следующий Персонаж", user.getId());
                game.removeWord();
            }
        }

        if (!game.isGameStarted() && user.getId() == chatId) {
            if (update.getMessage().getReplyToMessage() != null && update.getMessage().getText().toLowerCase().equals("удалить"))
                game.revokeWord(user.getUserName(), update.getMessage().getReplyToMessage().getText());

            if (update.getMessage().getText().toLowerCase().equals("список") || update.getMessage().getText().toLowerCase().equals("удалить")) {
                ArrayList<String> list = game.getMyWordsAll(user.getUserName());
                StringBuilder sb = new StringBuilder();
                sb.append(list.get(0));
                for (int i = 1; i < list.size(); i++) {
                    sb.append(", ");
                    sb.append(list.get(i));
                }
                sendSimpleMessage("У вас в списке " + list.size() + " персонажей! \n" + sb.toString(), chatId);
                return;
            }
            game.addWordToAll(user.getUserName(), text);
            //return;
        }
    }


    public void finishTurn() {
        game.setActivePhase(false);
        if (game.isWordSetEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Конец раунда, количество угаданных персонажей:");
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
            sendSimpleMessage("Привет, сейчас твой ход!\nЖми 'Начать' когда будешь готов", "Начать", game.getUsers().get(game.getCurrentUser()));
        }


    }


    public void sendSimpleMessage(String text, long chatId) {
        try {
            execute(new SendMessage().setChatId(chatId).setText(text).setReplyMarkup(new ReplyKeyboardRemove()));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendSimpleMessageLeaveMarkup(String text, long chatId) {
        try {
            execute(new SendMessage().setChatId(chatId).setText(text));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendSimpleMessage(String text, String keyboardButton, long chatId) {


        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        replyKeyboardMarkup.setSelective(false);
        replyKeyboardMarkup.setResizeKeyboard(false);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();

        keyboardRow.add(keyboardButton);
        keyboard.add(keyboardRow);
        replyKeyboardMarkup.setKeyboard(keyboard);

        SendMessage message = new SendMessage().setChatId(chatId).setText(text).setReplyMarkup(replyKeyboardMarkup);
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
