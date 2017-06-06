package com.ludo3wifi.userInterface;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.ludo3wifi.userInterface.gameui.GameUI;

public class GameLauncher extends AndroidApplication {
    private GameUI mGameUI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        mGameUI = new GameUI(this, getIntent().getExtras().getInt("playerID"),
                getIntent().getExtras().getInt("nPlayers"));
        initialize(mGameUI, config);

    }


    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Quit Game?")
                .setMessage("Are you sure you want to quit this game?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        mGameUI.end();
                        finish();
                        startActivity(new Intent(getBaseContext(), MainMenu.class));
                    }
                }).create().show();

    }

    @Override
    public void onPause(){
        mGameUI.onPause();
        super.onPause();
    }

    @Override
    public void onResume(){
        super.onResume();
        mGameUI.onResume();
    }
}
