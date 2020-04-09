
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Game {

    private HashMap<String, Integer> users = new HashMap<>();
    private ArrayList<String> wordsLeft;
    private ArrayList<String> playersOrder = new ArrayList<>();
    private int currentPlayer = 0;
    private boolean isGameStarted = false;
    private Random rand = new Random();

    private int currentWordNumber = 0;
    private String currentWord = null;
    private long chatId;

    private HashMap<String, ArrayList<String>> wordsAll = new HashMap<>();
    private HashMap<String, Integer> statistics;


    private boolean isActivePhase = false;

    public Game(long chatId) {
        setChatId(chatId);
        users.put("bogdaninnova", 119970632);
        users.put("mandarinaobshaetsia", 283147469);
        users.put("cooll_babka", 183926638);
        users.put("levgerina", 322035787);
        //users.put("Jormungandre", 283463865);

        playersOrder.add("bogdaninnova");
        playersOrder.add("mandarinaobshaetsia");
        playersOrder.add("cooll_babka");
        playersOrder.add("levgerina");
        //playersOrder.add("Jormungandre");
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
        return playersOrder.get(currentPlayer);
    }

    public void nextPlayer() {
        if (++currentPlayer == playersOrder.size())
            currentPlayer = 0;
    }

    public boolean isGameStarted() {
        return isGameStarted;
    }

    public void setGameStarted(boolean isGameStarted) {
        this.isGameStarted = isGameStarted;
    }

    public HashMap<String, Integer> getUsers() {
        return users;
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
}
