package il.co.freebie.alias;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import static il.co.freebie.alias.ConstantsHolder.*;


/**
 * Created by one 1 on 15-Sep-18.
 */

public class NewGameActivity extends AppCompatActivity implements View.OnClickListener {

    private int minWords = 10;
    private int maxWords = 200;
    private int minTime = 30;
    private int maxTime = 990;
    private String[] teamNames;
    private int[] teamColors;
    private TeamAdapter teamAdapter;
    private ArrayList<Team> teamsOnTheScreen = new ArrayList<>();
    private List<String> teamNamesNotOnScreen = new ArrayList<>();
    private Button moreTimeBtn;
    private Button lessTimeBtn;
    private Button moreWordsBtn;
    private Button lessWordsBtn;
    private TextView timeLimitTv;
    private TextView wordsAmountTv;
    private CheckBox lastWordCb;
    private Button selectDictionaryBtn;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getResources().getString(R.string.play_new_game));
        setContentView(R.layout.activity_new_game);

        Random random = new Random();
        RecyclerView recyclerView = findViewById(R.id.command_recycler);
        Button addTeamBtn = findViewById(R.id.add_team_btn);
        selectDictionaryBtn = findViewById(R.id.select_dictionary_btn);
        timeLimitTv = findViewById(R.id.seconds_for_round_tv);
        wordsAmountTv = findViewById(R.id.words_amount_tv);
        moreTimeBtn = findViewById(R.id.more_time_btn);
        lessTimeBtn = findViewById(R.id.less_time_btn);
        moreWordsBtn = findViewById(R.id.more_words_btn);
        lessWordsBtn = findViewById(R.id.less_words_btn);
        lastWordCb = findViewById(R.id.last_word_all_cb);

        teamNames = getResources().getStringArray(R.array.teams_array);
        teamColors = getResources().getIntArray(R.array.color_array);

        moreTimeBtn.setOnClickListener(this);
        lessTimeBtn.setOnClickListener(this);
        moreWordsBtn.setOnClickListener(this);
        lessWordsBtn.setOnClickListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final int first = random.nextInt(teamNames.length - 1);//////////////////////////
        int second = random.nextInt(teamNames.length - 1);//////////////////////////
        while ((second == first))
        {
            second = random.nextInt(teamNames.length - 1);/////////////////
        }

        teamsOnTheScreen.add(new Team(teamNames[first], teamColors[first]));
        teamsOnTheScreen.add(new Team(teamNames[second], teamColors[second]));
        teamAdapter = new TeamAdapter(teamsOnTheScreen);
        teamAdapter.setListener(new TeamAdapter.ITeamListener() {
            @Override
            public void onTeamClicked(View view) {
                String text = getResources().getString(R.string.swipe_to_delete);
                Toast.makeText(NewGameActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });

        //for swiping team item in list of items
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                teamsOnTheScreen.remove(viewHolder.getAdapterPosition());
                teamAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        ////
        recyclerView.setAdapter(teamAdapter);

        selectDictionaryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog dlg;
                final AlertDialog.Builder builder = new AlertDialog.Builder(NewGameActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                final View dialogView  = inflater.inflate(R.layout.dictionaries_layout,null);
                builder.setView(dialogView);

                final ArrayList<String> dictionariesNamesList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.categories)));
                final ArrayAdapter<String> dictionariesAdapter =
                        new ArrayAdapter<String>(NewGameActivity.this,R.layout.custom_text_view,dictionariesNamesList);

                Button enDialogBtn = dialogView.findViewById(R.id.en_btn);
                Button heDialogBtn = dialogView.findViewById(R.id.he_btn);
                enDialogBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dictionariesNamesList.clear();
                        dictionariesNamesList.addAll(Arrays.asList(getResources().getStringArray(R.array.categoriesEN)));
                        dictionariesAdapter.notifyDataSetChanged();
                    }
                });
                heDialogBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dictionariesNamesList.clear();
                        dictionariesNamesList.addAll(Arrays.asList(getResources().getStringArray(R.array.categoriesHE)));
                        dictionariesAdapter.notifyDataSetChanged();
                    }
                });
                ListView dictionariesListView = dialogView.findViewById(R.id.dictionaries_list);
                dictionariesListView.setAdapter(dictionariesAdapter);
                dlg = builder.create();
                dictionariesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        selectDictionaryBtn.setText(dictionariesNamesList.get(i));
                        dlg.dismiss();
                    }
                });
                dlg.show();
            }
        });

        addTeamBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(teamsOnTheScreen.size() < MAX_TEAMS_NUMBER)//THINK IF NEED MORE
                {
                    teamNamesNotOnScreen.clear();
                    for(String teamName : teamNames){
                        boolean ifOnScreen = false;
                        for(Team team : teamsOnTheScreen)
                        {
                            if(teamName.equals(team.getTeamName()))
                            {
                                ifOnScreen = true;
                                break;
                            }
                        }

                        if(!ifOnScreen)
                        {
                            teamNamesNotOnScreen.add(teamName);
                        }
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(NewGameActivity.this);
                    builder.setItems(teamNamesNotOnScreen.toArray(new String[0]), new MyItemsDialogListener()).show();
                }
                else
                {
                    String text = getResources().getString(R.string.teams_number_limit);
                    Toast.makeText(NewGameActivity.this, text, Toast.LENGTH_LONG).show();
                }
            }
        });
        sharedPreferences = getSharedPreferences(CURRENT_GAME_DETAILS, MODE_PRIVATE);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void saveData() {
        int secondsLimit = Integer.parseInt(timeLimitTv.getText().toString());
        int wordsAmount = Integer.parseInt(wordsAmountTv.getText().toString());
        boolean ifWithLastWord = lastWordCb.isChecked();
        String selectedDictionary = selectDictionaryBtn.getText().toString();

        String dictionaryName = selectedDictionary.replaceAll(" ", "");
        dictionaryName = ConstantsHolder.translateIfNeed(dictionaryName);
        int resourseId = this.getResources().getIdentifier(dictionaryName, "array", this.getPackageName());
        String[] words = getResources().getStringArray(resourseId);
        ArrayList<String> wordsList = new ArrayList<String>(Arrays.asList(words));

        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String JsonTeamList = gson.toJson(teamsOnTheScreen);
        editor.putString(TEAMS, JsonTeamList);
        String JsonWordsList = gson.toJson(wordsList);
        editor.putString(WORDS_LIST, JsonWordsList);
        editor.putString(CONST_WORDS_LIST, JsonWordsList);
        editor.putInt(TIME_LIMIT, secondsLimit);
        editor.putInt(WORDS_AMOUNT, wordsAmount);
        editor.putBoolean(IF_LAST_WORD_FOR_EVERYONE, ifWithLastWord);
        editor.putBoolean(IF_NEW_GAME, false);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_game_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_start)
        {
            if(selectDictionaryBtn.getText().equals(getResources().getString(R.string.select_dictionary)))
            {
                Toast.makeText(this, getResources().getString(R.string.dictionary_not_selected), Toast.LENGTH_SHORT).show();
            }
            else if(teamsOnTheScreen.size() < 2)
            {
                Toast.makeText(this, getResources().getString(R.string.min_two_teams), Toast.LENGTH_SHORT).show();
            }
            else
            {
                saveData();
                Intent intent = new Intent(this, ScoreManagerActivity.class);
                startActivity(intent);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void makeButtonEnabled(Button btn)
    {
        if(!btn.isEnabled())
        {
            btn.setEnabled(true);
            btn.setAlpha(1f);
            btn.setClickable(true);
        }
    }

    private void makeButtonDisabled(Button btn)
    {
        btn.setEnabled(false);
        btn.setAlpha(.5f);
        btn.setClickable(false);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        int seconds;
        int words;
        switch (viewId){
            case R.id.more_time_btn:
                seconds = Integer.parseInt(timeLimitTv.getText().toString());
                if(seconds < maxTime)
                {
                    makeButtonEnabled(lessTimeBtn);
                    seconds += 30;
                    timeLimitTv.setText(seconds + "");
                }
                if(seconds == maxTime)
                {
                    makeButtonDisabled(moreTimeBtn);
                }
                break;
            case R.id.less_time_btn:
                seconds = Integer.parseInt(timeLimitTv.getText().toString());
                if(seconds > minTime)
                {
                    makeButtonEnabled(moreTimeBtn);
                    seconds -= 30;
                    timeLimitTv.setText(seconds + "");
                }
                if(seconds == minTime)
                {
                    makeButtonDisabled(lessTimeBtn);
                }
                break;
            case R.id.more_words_btn:
                words = Integer.parseInt(wordsAmountTv.getText().toString());
                if (words < maxWords)
                {
                    makeButtonEnabled(lessWordsBtn);
                    words += 10;
                    wordsAmountTv.setText(words + "");
                }
                if(words == maxWords)
                {
                    makeButtonDisabled(moreWordsBtn);
                }
                break;
            case R.id.less_words_btn:
                words = Integer.parseInt(wordsAmountTv.getText().toString());
                if(words > minWords)
                {
                    makeButtonEnabled(moreWordsBtn);
                    words -= 10;
                    wordsAmountTv.setText(words + "");
                }
                if(words == minWords)
                {
                    makeButtonDisabled(lessWordsBtn);
                }
                break;
        }
    }

    private class MyItemsDialogListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            String teamNameToAdd = teamNamesNotOnScreen.get(i);
            for(int j = 0; j < teamNames.length; j++)
            {
                if(teamNames[j].equals(teamNameToAdd))
                {
                    teamsOnTheScreen.add(new Team(teamNames[j], teamColors[j]));
                    teamAdapter.notifyItemInserted(teamsOnTheScreen.size() - 1);
                    break;
                }
            }
        }
    }
}
