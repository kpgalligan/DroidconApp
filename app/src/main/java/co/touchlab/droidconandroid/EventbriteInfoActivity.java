package co.touchlab.droidconandroid;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import co.touchlab.droidconandroid.presenter.EventbriteInfoHost;
import co.touchlab.droidconandroid.presenter.EventbriteInfoPresenter;

/**
 * Created by kgalligan on 8/30/16.
 */
public class EventbriteInfoActivity extends AppCompatActivity
{

    private EditText emailField;
    private EventbriteInfoPresenter presenter;

    public static void callMe(Context context)
    {
        Intent intent = new Intent(context, EventbriteInfoActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventbrite_detail);
        emailField = (EditText) findViewById(R.id.email);
        presenter = new EventbriteInfoPresenter(new DamnKotlin());

        findViewById(R.id.save).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                presenter.updateEmail();
                Toast.makeText(EventbriteInfoActivity.this, "Email updated", Toast.LENGTH_LONG).show();
            }
        });
    }

    class DamnKotlin implements EventbriteInfoHost
    {
        @Override
        public void setEmail(String email)
        {
            emailField.setText(email);
        }

        @Override
        public String getEmail()
        {
            return emailField.getText().toString();
        }

        @Override
        public Context getContext()
        {
            return EventbriteInfoActivity.this;
        }
    }
}
