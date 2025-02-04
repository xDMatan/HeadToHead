package com.example.headtohead;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Random;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.headtohead.question.ClassicQuestion;
import com.example.headtohead.question.GameCollections;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class GameActivityClassic extends AppCompatActivity implements View.OnClickListener {
    private GameCollections collections;
    private EditText etAnswer;
    private ImageButton ibSend;
    private ClassicGameFBmodule fBmodule;
    private RecyclerView RvChat;
    private ClassicQuestion CurrentQuestion;
    private int player;
    private TextView tvQuestion, Current;
    private Handler handler;
    private CountDownTimer countDownTimer;
    private MediaPlayer mediaPlayer;
    private ArrayList<String> stringArrayList;
    private int WhichPLayerisPlaying = 0;
    private String WaitingString;
    private CustomDialog customDialog;
    private ArrayList<ClassicQuestion> classicCollection, Temp;
    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_classic);
        intent = getIntent();
        if (intent.getStringExtra("PlaySong").equals("true")) {
            String Song = intent.getStringExtra("SongName");
            int resourceId = getResources().getIdentifier(Song, "raw", getPackageName());
            mediaPlayer = MediaPlayer.create(this, resourceId);


            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
        collections = new GameCollections();
        classicCollection = collections.getClassisCollection();
        etAnswer = findViewById(R.id.etAnwser);
        ibSend = findViewById(R.id.ibSendAnswer);
        ibSend.setOnClickListener(this);
        tvQuestion = findViewById(R.id.TvQuestion);
        fBmodule = new ClassicGameFBmodule(this);
        fBmodule.ClassicSetPlayers(this);
        CurrentQuestion = new ClassicQuestion("", "", new HashMap<>(), false);
        RvChat = findViewById(R.id.rvChat);
        stringArrayList = new ArrayList<String>();
        Temp = collections.getClassisCollection();
        // Set up RecyclerView
        RvChat.setLayoutManager(new LinearLayoutManager(this));
        RvChat.setAdapter(new StringAdapter(stringArrayList));
        etAnswer.setFocusable(false);
        handler = new Handler();
        Current = findViewById(R.id.Tvcurrent);
        WaitingString = "Strating game in a few Seconds";
        customDialog = new CustomDialog(this, this);
        fBmodule.Chat(this);
        fBmodule.SetQuestion(this);
        fBmodule.ShowCustomDialog(this);
        fBmodule.WhoNeedsToPlay(this);
    }

    public void StratingPoint(int player) {
        this.player = player;

        if (this.player == 1) {
            tvQuestion.setText("Waiting for Players");
        }
        if (this.player == 2) {
            tvQuestion.setText("Waiting...");
            GetANewRandomQuestion();//מעלה שאלה חדשה
            DatabaseReference CurrentQuestion = FirebaseDatabase.getInstance().getReference("ClassicGameControl/CurrentQuestion");
            CurrentQuestion.setValue(Temp.get(0).getQuestion());

        }
    }

    public void SetQuestionForPlayersAndStartTheGame(String Question) {
        if (customDialog.getHandler() != null) {
            customDialog.getHandler().removeCallbacksAndMessages(null);
        }
        if(this.player==1&&customDialog.isShowing())
        {
            customDialog.dismiss();
        }
        stringArrayList.clear();
        RvChat.setAdapter(new StringAdapter(stringArrayList));
        tvQuestion.setText(WaitingString);
        WaitingString = "strating next round in a few seconds";
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < classicCollection.size(); i++) {
                    if (Question.equals(classicCollection.get(i).getQuestion()))
                        CurrentQuestion = classicCollection.get(i);
                }
                if (getPlayer() == 2) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Random rand = new Random();
                            int WhoplaysFirst = rand.nextInt(2) + 1;//יחליט מי ראשון
                            DatabaseReference CurrentPLayer = FirebaseDatabase.getInstance().getReference("ClassicGameControl/CurrentPlayer");
                            CurrentPLayer.setValue(WhoplaysFirst);
                        }
                    }, 1000);
                }
            }
        }, 2000);
    }

    public void GameTime() {
        tvQuestion.setText(CurrentQuestion.getQuestion());
        if (WhichPLayerisPlaying == this.player) {
            etAnswer.setFocusable(true);
            etAnswer.setEnabled(true);
            etAnswer.setFocusableInTouchMode(true);
            etAnswer.setInputType(InputType.TYPE_CLASS_TEXT);

            countDownTimer = new CountDownTimer(15000, 50) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long seconds = millisUntilFinished / 1000;
                    long milliseconds = millisUntilFinished % 1000;
                    Current.setText(String.format("Time remaining: %d.%03d seconds", seconds, milliseconds));

                }

                @Override
                public void onFinish() {
                    Current.setText("Time's up!");
                    DatabaseReference ShowCustomDialog = FirebaseDatabase.getInstance().getReference("ClassicGameControl/ShowCustomDialog");
                    ShowCustomDialog.setValue(true);

                }
            }.start();
        } else {
            if(countDownTimer!=null)
                countDownTimer.cancel();
            Current.setText("opponnent's turn...");
            etAnswer.setFocusable(false);
            etAnswer.setEnabled(false);
            etAnswer.setFocusableInTouchMode(false);
            etAnswer.setInputType(InputType.TYPE_NULL);

        }
    }


    @Override
    public void onClick(View v) {
        if (v == ibSend) {
            if (!etAnswer.getText().toString().equals("")) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ClassicGameControl/Chat");
                reference.setValue(etAnswer.getText().toString());//מעלה את השאלה לfb
            }
        }
    }

    public void GetANewRandomQuestion() {
        Boolean valid = false;
        while (!valid) {
            java.util.Collections.shuffle(Temp);
            if (!Temp.get(0).getWaseverused()) {
                Temp.get(0).setWaseverused(true);
                valid = true;
            }

        }


    }

    public void SetChat(String LastMassage) {
        String Result = "";
        String Name = "";
        if (this.player == this.WhichPLayerisPlaying)
            Name += "אתה";
        else
            Name += "יריב";
        if (CurrentQuestion.getAnswers().containsKey(LastMassage)) {
            if (CurrentQuestion.getAnswers().get(LastMassage)) {
                Result += "נכון";

            } else
                Result += "היה כבר בשימוש";
        } else
            Result += "לא נכון";
        stringArrayList.add(Name + " : " + LastMassage + " - " + Result);
        RvChat.setAdapter(new StringAdapter(stringArrayList));
        RvChat.scrollToPosition(stringArrayList.size() - 1);
        etAnswer.setText("");
        if (CurrentQuestion.getAnswers().containsKey(LastMassage)) {//אם התשובה הייתה נכונה תעביא את התור לשחקו הבא
            if (CurrentQuestion.getAnswers().get(LastMassage)) {
                CurrentQuestion.getAnswers().put(LastMassage, false);

                if (this.player == getWhichPLayerisPlaying()) {
                    etAnswer.setFocusable(false);
                    etAnswer.setEnabled(false);
                    etAnswer.setFocusableInTouchMode(false);
                    etAnswer.setInputType(InputType.TYPE_NULL);
                    countDownTimer.cancel();

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ClassicGameControl/CurrentPlayer");
                    if (getWhichPLayerisPlaying() == 1)
                        reference.setValue(2);
                    else
                        reference.setValue(1);
                }
            }
        }
    }


    public int getWhichPLayerisPlaying() {
        return WhichPLayerisPlaying;
    }

    public void setWhichPLayerisPlaying(int whichPLayerisPlaying) {
        WhichPLayerisPlaying = whichPLayerisPlaying;
    }

    public int getPlayer() {
        return player;
    }

    public ArrayList<ClassicQuestion> getTemp() {
        return Temp;
    }

    public void CustomDialog() {
        if (!isFinishing() && !isDestroyed()) {
            if (customDialog != null && !customDialog.isShowing()) {
                customDialog.show();
                customDialog.Change();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if(countDownTimer!=null)
            countDownTimer.cancel();

    }
}