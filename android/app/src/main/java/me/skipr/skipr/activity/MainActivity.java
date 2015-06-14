package me.skipr.skipr.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import me.skipr.skipr.R;
import me.skipr.skipr.fragment.MainFragment;


public class MainActivity extends BaseActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            String roomId = getIntent().getExtras().getString("room_id", "");
            final String roomName = getIntent().getExtras().getString("room_name", "");
            String token = getIntent().getExtras().getString("token", "");
            Boolean isPlayer = getIntent().getExtras().getBoolean("isPlayer", false);

            MainFragment mainFragment = MainFragment.newInstance(roomId, roomName, token, isPlayer);
            getFragmentManager().beginTransaction()
                    .add(R.id.container, mainFragment)
                    .commit();

            findViewById(R.id.share_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JSONObject objJson = new JSONObject();
                    try {
                        objJson.put("room_name", roomName);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Branch.getInstance(getApplicationContext()).getContentUrl("email", objJson, new Branch.BranchLinkCreateListener() {
                        @Override
                        public void onLinkCreate(String s, BranchError branchError) {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_SUBJECT, "Join my room in Skipr!");
                            intent.putExtra(Intent.EXTRA_TEXT, "Join me in creating a shared playlist!" + s);
                            intent.setType("text/plain");
                            startActivity(Intent.createChooser(intent, "Choose Email Client"));
                        }

                    });

                }

            });
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        

        return super.onOptionsItemSelected(item);
    }
}
