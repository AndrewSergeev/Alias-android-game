package il.co.freebie.alias;

/**
 * Created by one 1 on 01-Oct-18.
 */

public class ConstantsHolder {

    public static final String CURRENT_GAME_DETAILS = "game_details";
    public static final String IF_NEW_GAME = "if_new_game";
    public static final String TEAMS = "teams_players";
    public static final String WORDS_AMOUNT = "words_amount";
    public static final String TIME_LIMIT = "time_limit";
    public static final String IF_LAST_WORD_FOR_EVERYONE = "last_word_for_all";
    public static final String WORDS_LIST = "words_list";
    public static final String CONST_WORDS_LIST = "const_words_list";
    public static final String CURRENT_TEAM = "curr_team";
    public static final String UNREAD_WORD = "unread_word";
    public static final String GUESSED_WORDS_LIST = "guessed_words_list";
    public static final String PASSED_WORDS_LIST = "passed_words_list";
    public static final String ROUND_TEAMS_TURN = "turn_of";
    public static final String SCORED_POINTS = "scored_points";
    public static final String START_GAME = "start_game";
    public static final String ON_RESUME = "onresume";
    public static final int MAX_TEAMS_NUMBER = 4;

    public static String translateIfNeed(String str) {
        String strToReturn = str;
        if(str.equals("מילוןקל"))
        {
            strToReturn = "EasydictionaryHE";
        }
        else if(str.equals("מילוןקשה"))
        {
            strToReturn = "HarddictionaryHE";
        }

        return strToReturn;
    }
}
