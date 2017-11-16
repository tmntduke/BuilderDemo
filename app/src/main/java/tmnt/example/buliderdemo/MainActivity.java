package tmnt.example.buliderdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        User user = new UserBuilder()
                .age(10)
                .firstName("zhang")
                .lastName("yu")
                .nickName("tmnt")
                .build();

        Log.i(TAG, "onCreate: " + user.getFirstName() + "\n-----\n"
                + user.getLastName() + "\n------\n"
                + user.getNickName() + "\n------\n");
    }
}
