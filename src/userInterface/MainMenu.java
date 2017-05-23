package rmsf.eduardodiogo.ludo3wifi.userInterface;


import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import rmsf.eduardodiogo.ludo3wifi.R;

public class MainMenu extends FragmentActivity {
    public final static String EXTRA_MESSAGE = "ROOMNAME";
    private FindGameFragment frag2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        if(savedInstanceState == null){
            doHosting(null);
        }
    }

    public void doHosting(View v){
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        InputHostInfoFragment frag = new InputHostInfoFragment();
        findViewById(R.id.main_menu).setOnKeyListener( new View.OnKeyListener()
        {
            public boolean onKey( View v, int keyCode, KeyEvent event )
            {
                if( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
                    finish();
                }
                return MainMenu.this.onKeyDown(keyCode, event);
            }
        } );
        FragmentTransaction transaction=  fm.beginTransaction();
        transaction.replace(R.id.inputInfoContainer, frag);
        transaction.addToBackStack(null);
        transaction.commit();

    }

    public void findGame( View v){
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        FindGameFragment frag = new FindGameFragment();
        findViewById(R.id.main_menu).setOnKeyListener( new View.OnKeyListener()
        {
            public boolean onKey( View v, int keyCode, KeyEvent event )
            {
                if( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
                    finish();
                }
                return MainMenu.this.onKeyDown(keyCode, event);
            }
        } );
        FragmentTransaction transaction=  fm.beginTransaction();
        transaction.replace(R.id.inputInfoContainer, frag);
        transaction.addToBackStack(null);
        transaction.commit();
        this.frag2 = frag;
    }
    public void StartRoom(View v){
        EditText UserNameInput = (EditText) this.findViewById(R.id.inptUsername);
        String username = UserNameInput.getText().toString();
        String roomname = username + " Room";
        Log.d("InH","Starting Host");
        Log.d("InH","Room name: " + roomname);
        Intent intent = new Intent( this, RoomActivity.class);
        intent.putExtra("USERNAME", username);
        intent.putExtra("STATUS", "OWNER");
        intent.putExtra("ROOMNAME", username + " Room");
        startActivity(intent);
    }

}
