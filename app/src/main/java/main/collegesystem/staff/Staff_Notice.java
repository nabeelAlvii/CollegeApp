package main.collegesystem.staff;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SendCallback;

import java.util.List;

import main.collegesystem.About;
import main.collegesystem.Login;
import main.collegesystem.Profile;
import main.collegesystem.R;

public class Staff_Notice extends AppCompatActivity implements View.OnClickListener {
    public static String nm, mail, utype, phon, brnch, addr;
    EditText title, contnet;
    Button upld;
    String titlev, contntv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_notice_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        upld = (Button) findViewById(R.id.uplbtn);
        upld.setOnClickListener(this);
    }

    public void unsetChannel() {
        List<String> las = ParseInstallation.getCurrentInstallation().getList("channels");
        if (las == null) {
            Log.i("Channels :", "Not Cleared !");
        } else {
            ParseInstallation.getCurrentInstallation().removeAll("channels", las);
            ParseInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    Log.i("Channels :", "Cleared !");
                }
            });
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.staff_menu, menu);//Menu Resource, Menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.staffprof:
                SharedPreferences pref = this.getSharedPreferences("Login_state", MODE_PRIVATE);
                String sessionToken = pref.getString("sessionToken", "");
                try {
                    ParseUser.become(sessionToken);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                ParseUser user = ParseUser.getCurrentUser();
                if (user != null) {
                    nm = user.getUsername();
                    mail = user.getEmail();
                    utype = user.get("Type").toString();
                    phon = user.get("PhoneNo").toString();
                    brnch = user.get("Branch").toString();
                    addr = user.get("Address").toString();
                    Intent i = new Intent(getApplicationContext(), Profile.class);
                    Bundle detail = new Bundle();
                    detail.putString("uname", nm);
                    detail.putString("mail", mail);
                    detail.putString("utype", utype);
                    detail.putString("phone", phon);
                    detail.putString("branch", brnch);
                    detail.putString("address", addr);

                    i.putExtras(detail);
                    startActivity(i);
                    Log.i("Current User :--", "user :" + nm);
                } else {
                    Log.i("Current User :--", "user null");
                    Toast.makeText(getApplicationContext(), "Profile isn't initialized", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.Logout:
                ParseUser currentUser = ParseUser.getCurrentUser();
                if (currentUser != null) {
                    // do stuff with the user
                    ParseUser.logOutInBackground(new LogOutCallback() {
                        @Override
                        public void done(ParseException e) {
                            SharedPreferences pref = getApplicationContext().getSharedPreferences("Login_state", MODE_PRIVATE);
                            SharedPreferences.Editor edi = pref.edit();
                            edi.putString("type", null);
                            edi.putBoolean("firstlogin", true);
                            edi.apply();
                            unsetChannel();
                            Intent i = new Intent(getApplicationContext(), Login.class);
                            startActivity(i);
                            finish();
                            Toast.makeText(getApplicationContext(), "Logout Selected", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                return true;
            case R.id.About:
                Intent t = new Intent(this, About.class);
                startActivity(t);
                Toast.makeText(getApplicationContext(), "About Selected", Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        title = (EditText) findViewById(R.id.titl);
        contnet = (EditText) findViewById(R.id.cont);
        titlev = title.getText().toString();
        contntv = contnet.getText().toString();
        final long weekInterval = 60 * 60 * 24 * 7; // 1 week

        final ParseObject notice = new ParseObject("Notice");
        notice.put("Title", titlev);
        notice.put("Content", contntv);
        notice.put("From", ParseUser.getCurrentUser().getUsername());
        notice.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Log.i("Notice Upload :", "Successfull");
                ParsePush push = new ParsePush();
                /*JSONObject notjob=new JSONObject();
                try {
                    notjob.put("alert",title);
                    notjob.put("uri","noticestud");
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                push.setData(notjob);*/
                push.setMessage(titlev);
                push.setChannel("Student");
                push.setExpirationTimeInterval(weekInterval);
                push.sendInBackground(new SendCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.i("Notice is Sent :", "sucssessful");
                        Toast.makeText(Staff_Notice.this, "Notice Sent To All Student", Toast.LENGTH_SHORT).show();
                    }
                });
                Toast.makeText(Staff_Notice.this, "Notice Uploaded", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void gotoNoticeList(View view) {
        Intent i = new Intent(this, Staff_Notice_List.class);
        startActivity(i);
    }
}
