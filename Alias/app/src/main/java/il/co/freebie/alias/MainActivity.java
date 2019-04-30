package il.co.freebie.alias;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import static il.co.freebie.alias.ConstantsHolder.*;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private Button resumeGameBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(CURRENT_GAME_DETAILS, MODE_PRIVATE);

        resumeGameBtn = findViewById(R.id.resume_game_btn);
        Button newGameBtn = findViewById(R.id.new_game_btn);
        Button rulesBtn = findViewById(R.id.rules_btn);
        ImageView logoIv = findViewById(R.id.logo_iv);
        ImageView shineIv = findViewById(R.id.shine_iv);

        Animation alphaAndRotateAnim = AnimationUtils.loadAnimation(this, R.anim.logo_anim);
        Animation alphaShineAnim = AnimationUtils.loadAnimation(this, R.anim.shine_anim);
        alphaAndRotateAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }
            @Override
            public void onAnimationRepeat(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                MediaPlayer logoMp = MediaPlayer.create(MainActivity.this, R.raw.bit);
                logoMp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        mediaPlayer.release();
                    }
                });
                logoMp.start();
            }
        });
        logoIv.startAnimation(alphaAndRotateAnim);
        shineIv.startAnimation(alphaShineAnim);

        newGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean ifNewGame = sharedPreferences.getBoolean(IF_NEW_GAME, true);
                if(!ifNewGame)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(R.string.sure_fore_new_game).setMessage(R.string.current_will_be_deleted)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.clear();
                                    editor.commit();
                                    Intent intent = new Intent(MainActivity.this, NewGameActivity.class);
                                    startActivity(intent);
                                }
                            }).setNegativeButton(R.string.no,null).show();
                }
                else
                {
                    Intent intent = new Intent(MainActivity.this, NewGameActivity.class);
                    startActivity(intent);
                }
            }
        });
        resumeGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ScoreManagerActivity.class);
                intent.putExtra(ON_RESUME, true);
                startActivity(intent);
            }
        });
        rulesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RulesActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        boolean ifNewGame = sharedPreferences.getBoolean(IF_NEW_GAME, true);
        if(!ifNewGame)
        {
            resumeGameBtn.setEnabled(true);
            resumeGameBtn.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onBackPressed() {
        //this way nothing happens
        //exit to home android
    }
}
