package il.co.freebie.alias;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static il.co.freebie.alias.ConstantsHolder.*;


/**
 * Created by one 1 on 21-Sep-18.
 */

public class ScoreManagerActivity extends AppCompatActivity {

    private int wordsAmount;
    private int turnOfNextTeam;
    private int playedTeamScore;
    private ArrayList<Team> teamsList;
    private SimpleAdapter adapter;
    private SharedPreferences sharedPreferences;

    private TextView nextPlayerTeamTv;
    private TextView getNextTeamColorTv;
    private ListView teamsListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_manager);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setElevation(0);

        nextPlayerTeamTv = findViewById(R.id.next_player_team_tv);
        getNextTeamColorTv = findViewById(R.id.team_color_for_score_tv);
        teamsListView = findViewById(R.id.teams_score_list);

        sharedPreferences = getSharedPreferences(CURRENT_GAME_DETAILS, MODE_PRIVATE);
        loadData();
        boolean resumed = getIntent().getBooleanExtra(ON_RESUME, false);
        if(!resumed)
        {
            manageScore();
            getIntent().putExtra(ON_RESUME, false);///////////////////
        }

        Team nextPlayerTeam = teamsList.get(turnOfNextTeam);
        nextPlayerTeamTv.setText(nextPlayerTeam.getTeamName());
        getNextTeamColorTv.getBackground().setColorFilter(nextPlayerTeam.getTeamColor(), PorterDuff.Mode.SRC_ATOP);
        fillListView();

        int[] colors = new int[]{
                ContextCompat.getColor(this,R.color.seaStar),
                ContextCompat.getColor(this,R.color.darkGoldStar),
                ContextCompat.getColor(this,R.color.goldStar)
        };

        ValueAnimator valueAnim = ValueAnimator.ofObject(new ArgbEvaluator(),colors[0],colors[1],colors[2]);
        valueAnim.setDuration(4000);/////////////////////////////////////////////////////

        valueAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ImageView image = findViewById(R.id.star_iv);
                int color = (int)valueAnimator.getAnimatedValue();
                image.setColorFilter(color);
            }
        });
        valueAnim.start();
    }

    private void fillListView() {
        //map for each list item
        List<Map<String,Object>> teamsData = new ArrayList<>();
        String playedRes = getResources().getString(R.string.played_in_round);
        String teamConstStr = "team";//key for team name
        String playedConstStr = "played";//key for string that will include "played in round" or "" - second key
        String scoreConsStr = "score";//key for team score in the round

        int teamIdx = 0;
        for(Team team : teamsList)
        {
            HashMap<String, Object> map = new HashMap<>();
            //checks if the team played in the round already
            String ifPlayed = teamIdx < turnOfNextTeam ? playedRes : "";//second value in the map
            map.put(teamConstStr, team.getTeamName());
            map.put(playedConstStr, ifPlayed);
            map.put(scoreConsStr, team.getTotalScore());
            teamsData.add(map);
            teamIdx++;
        }

        String[] from = {teamConstStr, playedConstStr, scoreConsStr};
        int[] to = {R.id.team_name_score_tv, R.id.played_tv, R.id.score_tv};
        adapter = new SimpleAdapter(this, teamsData, R.layout.score_team_layout, from, to);
        teamsListView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        saveData();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void loadData() {
        Gson gson = new Gson();
        String JsonTeamsList = sharedPreferences.getString(TEAMS, null);
        Type type = new TypeToken<ArrayList<Team>>() {}.getType();
        teamsList = gson.fromJson(JsonTeamsList, type);
        playedTeamScore = getIntent().getIntExtra(SCORED_POINTS, 0);
        wordsAmount = sharedPreferences.getInt(WORDS_AMOUNT, 0);////////
        turnOfNextTeam = sharedPreferences.getInt(ROUND_TEAMS_TURN, 0);
    }

    private void manageScore(){
        boolean startGame = sharedPreferences.getBoolean(START_GAME, true);
        //if this is not starting point of the game(not new game)
        if(!startGame)
        {
            boolean finished = false;
            //idx of the team that played right now
            int playedTeamIdx = turnOfNextTeam == 0 ? (teamsList.size() - 1) : turnOfNextTeam - 1;

            if(playedTeamScore != 0)
            {
                int totalTeamScore = teamsList.get(playedTeamIdx).getTotalScore();
                totalTeamScore += playedTeamScore;
                teamsList.get(playedTeamIdx).setTotalScore(totalTeamScore);
            }

            if(playedTeamIdx == teamsList.size() - 1)
            {
                finished = checkIfGotToFinish();
            }

            if(finished)
            {
                findWinners();
            }
        }
    }

    private boolean checkIfGotToFinish(){
        boolean finished = false;

        for (Team team : teamsList)
        {
            if(team.getTotalScore() >= wordsAmount)
            {
                finished = true;
                break;
            }
        }

        return finished;
    }

     @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        saveData();
        if(item.getItemId() == R.id.action_go)
        {
            Intent intent = new Intent(this, GameBoardActivity.class);
            intent.putExtra(CURRENT_TEAM, teamsList.get(turnOfNextTeam).getTeamName());
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void findWinners() {
        ArrayList<Team> winnersList = new ArrayList<>();

        for (Team team : teamsList)
        {
            if (team.getTotalScore() >= wordsAmount)
            {
                winnersList.add(team);//moamadim le menatskhim
            }
        }

        if(winnersList.size() > 1)
        {
            winnersList.sort(new Comparator<Team>() {
                @Override
                public int compare(Team t1, Team t2) {
                    int returningValue = 0;

                    if(t1.getTotalScore() > t2.getTotalScore())
                    {
                        returningValue = -1;
                    }
                    else if(t1.getTotalScore() < t2.getTotalScore())
                    {
                        returningValue = 1;
                    }

                    return returningValue;
                }
            });
        }

        Team bestResulter = winnersList.get(0);
        if(winnersList.size() == 1)
        {
            showWinnerAndFinishGame(bestResulter);

        }
        else if(bestResulter.getTotalScore() > winnersList.get(1).getTotalScore())
        {
            showWinnerAndFinishGame(bestResulter);
        }
        else
        {
            ArrayList<Team> bestResultersList = new ArrayList<>();
            for (Team team : winnersList)
            {
                if(team.getTotalScore() == bestResulter.getTotalScore())
                {
                    bestResultersList.add(team);
                }
                else
                {
                    break;
                }
            }

            teamsList = bestResultersList;
            saveData();
        }
    }

    private void showWinnerAndFinishGame(Team bestResulter)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String msg = bestResulter.getTeamName() + " " + getResources().getString(R.string.won);
        builder.setTitle(R.string.congratulations).setMessage(msg).setPositiveButton(R.string.finish, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.commit();
                Intent intent = new Intent(ScoreManagerActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }).setCancelable(false).setIcon(R.drawable.ic_looks_one_black_24dp).show();

        final MediaPlayer applaudsMp = MediaPlayer.create(this, R.raw.winner);
        applaudsMp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                applaudsMp.release();
            }
        });
        applaudsMp.start();
    }

    private void saveData(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String JsonTeamList = gson.toJson(teamsList);
        editor.putString(TEAMS, JsonTeamList);
        editor.putInt(ROUND_TEAMS_TURN, turnOfNextTeam);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.score_manager_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
}
