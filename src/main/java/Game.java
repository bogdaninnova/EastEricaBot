
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Game {

    private ArrayList<String> wordsLeft;
    private ArrayList<String> players;
    private int currentPlayer = 0;
    private boolean isGameStarted = false;
    private Random rand = new Random();

    private int currentWordNumber = 0;
    private String currentWord = null;
    private long chatId;

    private HashMap<String, ArrayList<String>> wordsAll = new HashMap<>();
    private HashMap<String, Integer> statistics;


    private boolean isActivePhase = false;

    public Game(long chatId, ArrayList<String> players) {
        setChatId(chatId);
        this.players = players;
    }

    public String getRandomWord() {
        currentWordNumber = rand.nextInt(wordsLeft.size());
        setCurrentWord(wordsLeft.get(currentWordNumber));
        return getCurrentWord();
    }

    public void removeWord() {
        wordsLeft.remove(currentWordNumber);

        if (!statistics.containsKey(getCurrentUser()))
            statistics.put(getCurrentUser(), 1);
        else
            statistics.put(getCurrentUser(), statistics.get(getCurrentUser()) + 1);
    }

    public boolean isEmptyWordSet() {
        return wordsLeft.isEmpty();
    }

    public void restoreLastWord() {
        wordsLeft.add(currentWord);
        currentWord = null;
        statistics.put(getCurrentUser(), statistics.get(getCurrentUser()) - 1);
    }

    public void resetWordsLeft() {
        statistics = new HashMap<>();
        wordsLeft = new ArrayList<>();
        for (String userName : wordsAll.keySet())
        wordsLeft.addAll(wordsAll.get(userName));
    }

    public String getCurrentUser() {
        return players.get(currentPlayer);
    }

    public void nextPlayer() {
        if (++currentPlayer == players.size())
            currentPlayer = 0;
    }

    public boolean isGameStarted() {
        return isGameStarted;
    }

    public void setGameStarted(boolean isGameStarted) {
        this.isGameStarted = isGameStarted;
    }

    public void addWordToAll(String userName, String word) {
        if (wordsAll.containsKey(userName))
            wordsAll.get(userName).add(word);
        else {
            ArrayList<String> list = new ArrayList<>();
            list.add(word);
            wordsAll.put(userName, list);
        }
    }

    public void revokeWord(String userName, String word) {
        if (wordsAll.containsKey(userName))
            wordsAll.get(userName).remove(word);
    }

    public ArrayList<String> getMyWordsAll(String userName) {
        return wordsAll.get(userName);
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public boolean isActivePhase() {
        return isActivePhase;
    }

    public boolean isWordSetEmpty() {
        return wordsLeft.isEmpty();
    }

    public void setActivePhase(boolean activePhase) {
        isActivePhase = activePhase;
    }

    public String getCurrentWord() {
        return currentWord;
    }

    public void setCurrentWord(String currentWord) {
        this.currentWord = currentWord;
    }

    public HashMap<String, Integer> getStatistics() {
        return statistics;
    }

    public ArrayList<String> getPlayers() {
        return players;
    }
}
