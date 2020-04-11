import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class EastEricaBot extends TelegramLongPollingBot {

    private static final String botUsername = "EastEricaBot";
    private static final String botToken = "858313302:AAGyCP8vfiuPBetMhdY9WRymza6PlKuv7sA";

    private static HashMap<Long, Game> games = new HashMap<>();

    public static UsersList usersList = new UsersList();

    @Override
    public void onUpdateReceived(Update update) {

        String text = update.getMessage().getText();
        User user = update.getMessage().getFrom();
        long chatId = update.getMessage().getChatId();

        //System.out.println(update.getMessage().getReplyToMessage().getText());
        System.out.println(text);
        System.out.println(user);
        System.out.println(chatId);

        if (update.getMessage().getReplyToMessage() != null &&
                !text.toLowerCase().equals("список") &&
                !text.toLowerCase().equals("удалить") &&
                !text.toLowerCase().equals("отмена"))
             return;

        if (text.equals("/start")) {
            if (chatId == user.getId()) {
                usersList.addUser(user.getUserName().toLowerCase(), user.getId());
                sendSimpleMessage("User @" + user.getUserName() + " with id = " + user.getId() + " is registered!", 119970632);
            }
            return;
        }

        if (usersList.getUserId(user.getUserName()) == 0)
            return;
        long gameChatId = getUsersGame(user.getUserName());

        if (text.length() > 15) {
            if ((gameChatId == 0) && text.substring(0, 15).equals("/startNewGame @")) {
                ArrayList<String> list = new ArrayList<>(Arrays.asList(text.replace(" ", "").substring(text.indexOf("@")).split("@")));
                int errors = 0;

                for (String userName : list)
                    if (usersList.getUserId(userName) == 0) {
                        sendSimpleMessage("Пользователь @" + userName + " не зарегестрирован!", chatId);
                        errors++;
                    }
                if (errors > 0 || list.size() == 0)
                    return;

                games.put(chatId, new Game(chatId, list, user.getUserName()));

                sendSimpleMessage("Пишите мне персонажей в лс", chatId);
                for (String userName : games.get(chatId).getPlayers())
                    sendSimpleMessage("Привет! Загадывай персонажей (отдельными сообщениями)", usersList.getUserId(userName));
                return;
            }
        }

        if (gameChatId == 0) {
            sendSimpleMessage("Игра ещё не началась!", chatId);
            return;
        }
        Game game = games.get(gameChatId);

        if (game.getAdmin().equals(user.getUserName()) && text.equals("/stopGame")) {
            games.remove(gameChatId);
            sendSimpleMessage("Игра закончена!", gameChatId);
            return;
        }

        if (text.toLowerCase().equals("/statistic") && games.containsKey(chatId)) {
            if (games.get(chatId).isGameStarted()) {
                if (!games.get(chatId).isActivePhase())
                    sendStatistic(games.get(chatId));
            } else
                sendSimpleMessage("Раунд ещё не начался!", chatId);
            return;
        }

        if (game.getAdmin().equals(user.getUserName()) && text.equals("/startNewRound")) {
            game.resetWordsLeft();
            game.setGameStarted(true);
            sendSimpleMessage("Начало нового раунда", gameChatId);
            sendSimpleMessage("Ход игрока @" + game.getCurrentUser() + "! Ждём готовности.", gameChatId);
            sendSimpleMessage(
                    "Привет, сейчас твой ход!\nЖми 'Начать' когда будешь готов",
                    "Начать",
                    usersList.getUserId(game.getCurrentUser())
            );
            return;
        }

        if (user.getId() == chatId && user.getUserName().equals(game.getCurrentUser()) && !game.isActivePhase() && text.equals("Начать")) {
            sendSimpleMessage(game.getRandomWord(), "Следующий Персонаж", chatId);
            sendSimpleMessage("Начали! Ход игрока @" + game.getCurrentUser(), gameChatId);
            game.setActivePhase(true);
            game.removeWord();
            new GameTimer(this, game);
        }

        if (game.isActivePhase() && user.getId() == chatId && user.getUserName().equals(game.getCurrentUser()) && text.equals("Следующий Персонаж")) {

            if (game.getCurrentWord() != null)
                sendSimpleMessage("Отгаданный персонаж: " + game.getCurrentWord(), gameChatId);

            if (game.isEmptyWordSet()) {
                finishTurn(game);
            } else {
                sendSimpleMessage(game.getRandomWord(), "Следующий Персонаж", chatId);
                game.removeWord();
            }
        }

        if (!game.isActivePhase() && games.containsKey(chatId) && text.toLowerCase().equals("/защитать") && game.getCurrentWord() != null) {
            sendSimpleMessage("Отгаданный персонаж: " + game.getCurrentWord(), chatId);
            games.get(chatId).addPointToPrevious();
            if (game.isWordSetEmpty()) {
                sendSimpleMessage("Конец раунда!", usersList.getUserId(game.getCurrentUser()));
                sendSimpleMessage("Конец раунда!", game.getChatId());
                sendStatistic(game);
            }
            return;
        }

        if (!game.isActivePhase() && games.containsKey(chatId) && text.toLowerCase().equals("отмена")) {

            System.out.println(1);

            if (!update.getMessage().getReplyToMessage().getFrom().getUserName().equals("EastEricaBot"))
                return;

            System.out.println(2);

            if (update.getMessage().getReplyToMessage().getText().length() < 21)
                return;

            System.out.println(3);

            String word = update.getMessage().getReplyToMessage().getText().substring(21);

            System.out.println(word);
            if (games.get(chatId).cancelWord(word))
                sendSimpleMessage("Отгаданный Персонаж '" + word + "' не защитывается!", chatId);

            return;
        }

        if (!game.isGameStarted() && user.getId() == chatId) {
            if (text.toLowerCase().equals("удалить"))
                game.revokeWord(user.getUserName(), update.getMessage().getReplyToMessage().getText());

            if (text.toLowerCase().equals("список") || text.toLowerCase().equals("удалить")) {
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


    private static long getUsersGame(String userName) {
        for (long gameChatId : games.keySet())
            if (games.get(gameChatId).getPlayers().contains(userName))
                return gameChatId;
        return 0;
    }

    public void finishTurn(Game game) {
        game.setActivePhase(false);
        if (game.isWordSetEmpty()) {
            sendSimpleMessage("Конец раунда!", usersList.getUserId(game.getCurrentUser()));
            sendSimpleMessage("Конец раунда!", game.getChatId());
            sendStatistic(game);
            game.nextPlayer();
        } else {
            sendSimpleMessage("Время вышло!", usersList.getUserId(game.getCurrentUser()));
            sendSimpleMessage("Время вышло!", game.getChatId());
            game.nextPlayer();
            sendSimpleMessage("Сейчас ход игрока @" + game.getCurrentUser() + "!", game.getChatId());
            sendSimpleMessage("Привет, сейчас твой ход!\nЖми 'Начать' когда будешь готов", "Начать", usersList.getUserId(game.getCurrentUser()));
        }
    }

    private void sendStatistic(Game game) {
        if (game.getStatistics().isEmpty()) {
            sendSimpleMessage("Пока ещё никто ничего не отгадал!", game.getChatId());
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Количество угаданных персонажей:");
        for (String player : game.getStatistics().keySet()) {
            sb.append("\nИгрок @");
            sb.append(player);
            sb.append(": ");
            sb.append(game.getStatistics().get(player));
        }
        sendSimpleMessage(sb.toString(), game.getChatId());
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

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
