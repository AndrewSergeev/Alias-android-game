package il.co.freebie.alias;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Random;
import static il.co.freebie.alias.ConstantsHolder.*;


/**
 * Created by one 1 on 22-Sep-18.
 */

public class GameBoardActivity extends AppCompatActivity {

    private CountDownTimer timer;
    private MediaPlayer ticksMp;
    private MediaPlayer alarmMp;

    private MPCompleteListener mpListener;
    private PointBtnClickListener pointBtnClickListener;
    private SharedPreferences sharedPreferences;
    private TextView timerTv;
    private ProgressBar timerProgressBar;
    private TextView mainCardTv;
    private ImageView pointUpIv;
    private ImageView pointDownIv;
    private TextView positivePointsTv;
    private TextView negativePointsTv;
    private ImageView lastReversedCardIv;

    private String currTeamName;
    private int secondsPassed;
    private int secondsLimit;
    private ArrayList<String> words;
    private ArrayList<String> guessedWords;
    private ArrayList<String> passedWords;
    private String unreadLastWord;
    private Random random;
    private int screenHeight;

    private FrameLayout.LayoutParams mainCardConstParams;
    private int cardConstLeftMargin;
    private int cardConstTopMargin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_board);

        TextView guessedTv = findViewById(R.id.guessed_tv);
        TextView passTv = findViewById(R.id.pass_tv);
        timerTv = findViewById(R.id.timer_service_tv);
        timerProgressBar = findViewById(R.id.time_progress_bar);
        mainCardTv = findViewById(R.id.main_card_tv);
        pointUpIv = findViewById(R.id.point_plus_iv);
        pointDownIv = findViewById(R.id.minus_point_iv);
        positivePointsTv = findViewById(R.id.positive_points_tv);
        negativePointsTv = findViewById(R.id.negative_points_tv);
        lastReversedCardIv = findViewById(R.id.last_reversed_card_iv);

        mpListener = new MPCompleteListener();
        ticksMp = MediaPlayer.create(this, R.raw.timerthreesec);
        alarmMp = MediaPlayer.create(this, R.raw.alarm);

        guessedWords = new ArrayList<>();
        passedWords = new ArrayList<>();
        random = new Random();

        mpListener = new MPCompleteListener();
        ticksMp.setOnCompletionListener(mpListener);
        alarmMp.setOnCompletionListener(mpListener);

        getScreenDimensions();
        mainCardConstParams = (FrameLayout.LayoutParams) mainCardTv.getLayoutParams();
        cardConstLeftMargin = mainCardConstParams.leftMargin;
        cardConstTopMargin = mainCardConstParams.topMargin;
        sharedPreferences = getSharedPreferences(CURRENT_GAME_DETAILS, MODE_PRIVATE);
        loadData();
        setNewCard();

        Animation alphaAnim = AnimationUtils.loadAnimation(this, R.anim.game_board_alpha_anim);
        guessedTv.startAnimation(alphaAnim);
        passTv.startAnimation(alphaAnim);

        currTeamName = getIntent().getStringExtra(CURRENT_TEAM);
        getSupportActionBar().setTitle(currTeamName);
        timerTv.setText(secondsLimit + "");
        timer = new CountDownTimer(secondsLimit * 1000,1000) {

            @Override
            public void onTick(long l) {
                timerTv.setText(l / 1000 + "");
                secondsPassed = secondsLimit - (int)l/1000;
                if(secondsLimit - secondsPassed == 3)
                {
                    ticksMp.start();
                }

                int progress = secondsPassed * 100 / secondsLimit;
                timerProgressBar.setProgress(progress);
            }

            @Override
            public void onFinish() {
                timerTv.setText(0 + "");
                timerProgressBar.setProgress(100);
                unreadLastWord = mainCardTv.getText().toString();
                words.remove(unreadLastWord);
                alarmMp.start();

                final ObjectAnimator invisibleAnim = ObjectAnimator.ofFloat(mainCardTv,"scaleY",1f,0f).setDuration(1000);
                final ObjectAnimator visibleAnim = ObjectAnimator.ofFloat(lastReversedCardIv,"scaleY",0f,1f).setDuration(1000);
                invisibleAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        visibleAnim.start();
                    }
                });
                visibleAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        saveDataAndGoToNextActivity();
                    }
                });
                invisibleAnim.start();
            }
        };

        pointBtnClickListener = new PointBtnClickListener();
        pointUpIv.setOnClickListener(pointBtnClickListener);
        pointDownIv.setOnClickListener(pointBtnClickListener);

        mainCardTv.setOnTouchListener(new View.OnTouchListener() {
            FrameLayout.LayoutParams layoutParams;
            int firstTouchX = 0, firstTouchY = 0;
            int firstY = 0;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int currX = (int)motionEvent.getRawX();
                final int currY = (int)motionEvent.getRawY();

                switch (motionEvent.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                        firstTouchX = currX - layoutParams.leftMargin;
                        firstTouchY = currY - layoutParams.topMargin;
                        firstY = currY;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                        layoutParams.leftMargin = currX - firstTouchX;
                        layoutParams.topMargin = currY - firstTouchY;
                        break;
                    case MotionEvent.ACTION_UP:
                        layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                        int YToMove = currY < firstY ? -screenHeight : screenHeight;
                        if(YToMove > 0)
                        {
                            increasePoints(negativePointsTv);
                            passedWords.add(mainCardTv.getText().toString());
                        }
                        else
                        {
                            increasePoints(positivePointsTv);
                            guessedWords.add(mainCardTv.getText().toString());
                        }
                        animateMainCard(YToMove);
                        break;
                }

                view.requestLayout();
                return true;
            }
        });
        timer.start();
    }

    @Override
    public void onBackPressed() {
        showAlertDlg();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {//on android.R.id.home clicked
        showAlertDlg();
        return true;
    }

    private void showAlertDlg(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.exit_tomenu_quest).setMessage(R.string.progress_will_be_deleted)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        timer.cancel();
                        Intent intent = new Intent(GameBoardActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }).setNegativeButton(R.string.no,null).setIcon(R.drawable.mylogo).show();
    }

    private void saveDataAndGoToNextActivity() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String JsonWordsList = gson.toJson(words);
        editor.putString(WORDS_LIST, JsonWordsList);
        editor.commit();

        Intent intent = new Intent(GameBoardActivity.this, RoundResultsActivity.class);
        intent.putExtra(CURRENT_TEAM, currTeamName);
        intent.putStringArrayListExtra(GUESSED_WORDS_LIST, guessedWords);
        intent.putStringArrayListExtra(PASSED_WORDS_LIST, passedWords);
        intent.putExtra(UNREAD_WORD, unreadLastWord);
        startActivity(intent);
    }

    private void loadData() {
        words = getListFromSP(WORDS_LIST);
        secondsLimit = sharedPreferences.getInt(TIME_LIMIT, 0);
    }

    private ArrayList<String> getListFromSP(String constStr)
    {
        Gson gson = new Gson();
        String JsonWordsList = sharedPreferences.getString(constStr, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();

        return gson.fromJson(JsonWordsList, type);
    }

    private void increasePoints(TextView tv)
    {
        if(tv.getId() == R.id.positive_points_tv)
        {
            MediaPlayer pointUpMp = MediaPlayer.create(this, R.raw.pointup);
            pointUpMp.setOnCompletionListener(mpListener);
            pointUpMp.start();
        }
        else if(tv.getId() == R.id.negative_points_tv)
        {
            MediaPlayer pointDownMp = MediaPlayer.create(this, R.raw.pointdown);
            pointDownMp.setOnCompletionListener(mpListener);
            pointDownMp.start();
        }

        String pointsStr = tv.getText().toString();
        if(pointsStr.equals(""))
        {
            tv.setText("1");
        }
        else
        {
            int points = Integer.parseInt(pointsStr);
            points++;
            tv.setText(points + "");
        }
    }

    private void setNewCard()
    {
        if(words.size() == 1)
        {
            words = getListFromSP(CONST_WORDS_LIST);
        }

        int wordIdx = random.nextInt(words.size());
        mainCardTv.setText(words.get(wordIdx));
        words.remove(wordIdx);

    }

    private void animateMainCard(int YToMove) {
        mainCardTv.animate().translationY(YToMove).setDuration(400).withEndAction(new Runnable() {
            @Override
            public void run() {
                setNewCard();
                mainCardTv.animate().translationY(0).setDuration(1).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mainCardConstParams.topMargin = cardConstTopMargin;
                        mainCardConstParams.leftMargin = cardConstLeftMargin;
                        mainCardTv.setLayoutParams(mainCardConstParams);
                    }
                });
            }
        }).start();
    }

    private void getScreenDimensions() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenHeight = size.y;
    }

    private class PointBtnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if(view.getId() == R.id.point_plus_iv)
            {
                increasePoints(positivePointsTv);
                guessedWords.add(mainCardTv.getText().toString());
                animateMainCard(-screenHeight);
            }
            else if(view.getId() == R.id.minus_point_iv)
            {
                increasePoints(negativePointsTv);
                passedWords.add(mainCardTv.getText().toString());
                animateMainCard(screenHeight);
            }
        }
    }

    private class MPCompleteListener implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.release();
        }
    }
}
