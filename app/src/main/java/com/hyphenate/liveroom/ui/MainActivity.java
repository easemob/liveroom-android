package com.hyphenate.liveroom.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.liveroom.R;

public class MainActivity extends AppCompatActivity implements EMConnectionListener {

    private TextView titleView;

    private Fragment[] fragments = new Fragment[3];
    private int currentIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titleView = findViewById(R.id.txt_title);

        selectItem(0);

        registerIMConnectListener();

    }

    private void registerIMConnectListener(){
	    EMClient.getInstance().addConnectionListener(this);
    }

    private void exitLogin(){
	    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
	    startActivity(intent);
	    finish();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_room:
                selectItem(0);
                break;
            case R.id.btn_create:
                selectItem(1);
                break;
            case R.id.btn_settings:
                selectItem(2);
                break;
        }
    }

    private void selectItem(int p) {
        if (currentIndex == p) {
            return;
        }

        if (p == 0) {
            titleView.setText(R.string.title_room_list);
        } else if (p == 1) {
            titleView.setText(R.string.title_create_room);
        } else if (p == 2) {
            titleView.setText(R.string.title_settings);
        }

        if (fragments[p] == null) {
            Fragment fragment;
            if (p == 0) {
                fragment = new ChatRoomFragment();
            } else if (p == 1) {
                fragment = new CreateFragment();
            } else if (p == 2) {
                fragment = new SettingsFragment();
            } else {
                throw new IllegalArgumentException("Invalid index");
            }
            fragments[p] = fragment;
            getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
        }

        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        if (currentIndex != -1) {
            trans.hide(fragments[currentIndex]);
        }
        trans.show(fragments[p]).commit();

        currentIndex = p;
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EMClient.getInstance().removeConnectionListener(this);
	}

	@Override
	public void onConnected() {

	}

	@Override
	public void onDisconnected(final int error) {
    	runOnUiThread(new Runnable() {
		    @Override
		    public void run() {
			    if (error == EMError.USER_REMOVED) {
				    Toast.makeText(getApplicationContext(), R.string.em_user_remove, Toast.LENGTH_LONG).show();
				    exitLogin();
			    } else if (error == EMError.USER_KICKED_BY_OTHER_DEVICE) {
				    Toast.makeText(getApplicationContext(), R.string.connect_conflict, Toast.LENGTH_LONG).show();
				    exitLogin();
			    } else if (error == EMError.SERVER_SERVICE_RESTRICTED) {
				    Toast.makeText(getApplicationContext(), R.string.user_forbidden, Toast.LENGTH_LONG).show();
				    exitLogin();
			    }
		    }
	    });
	}
}
