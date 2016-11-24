package org.telegram.ui.Components;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

/**
 * This class has the controls to set the color of the filter
 *
 * @author Hathibelagal
 */
public class FilterActivity extends Activity implements OnSeekBarChangeListener {

    public final static int REQUEST_CODE = -1010101;
    SharedMemory shared;
    Button cancel, apply;
    SeekBar alphaSeek;
    SeekBar redSeek;
    SeekBar greenSeek;
    SeekBar blueSeek;
    int alpha, red, green, blue;

    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        apply = (Button) findViewById(R.id.apply);
        cancel = (Button) findViewById(R.id.cancel);
        cancel.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        apply.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        initialize();
    }

    private void initialize() {

        stopServiceIfActive();

        shared = new SharedMemory(this);

        alphaSeek = (SeekBar) findViewById(R.id.alphaControl);
        redSeek = (SeekBar) findViewById(R.id.redControl);
        greenSeek = (SeekBar) findViewById(R.id.greenControl);
        blueSeek = (SeekBar) findViewById(R.id.blueControl);
        alphaSeek.setOnSeekBarChangeListener(this);
        redSeek.setOnSeekBarChangeListener(this);
        greenSeek.setOnSeekBarChangeListener(this);
        blueSeek.setOnSeekBarChangeListener(this);

        alpha = shared.getAlpha();
        red = shared.getRed();
        green = shared.getGreen();
        blue = shared.getBlue();

        alphaSeek.setProgress(alpha);
        redSeek.setProgress(red);
        greenSeek.setProgress(green);
        blueSeek.setProgress(blue);

        updateColor();

    }

    private void stopServiceIfActive() {
        if (MainService.STATE == MainService.ACTIVE) {
            Intent i = new Intent(this, MainService.class);
            stopService(i);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        if (seekBar == alphaSeek) {
            alpha = seekBar.getProgress();
        }
        if (seekBar == redSeek) {
            red = seekBar.getProgress();
        }
        if (seekBar == greenSeek) {
            green = seekBar.getProgress();
        }
        if (seekBar == blueSeek) {
            blue = seekBar.getProgress();
        }
        updateColor();
    }

    private void updateColor() {
        int color = SharedMemory.getColor(alpha, red, green, blue);
        ColorDrawable cd = new ColorDrawable(color);
        getWindow().setBackgroundDrawable(cd);
    }

    @Override
    public void onStartTrackingTouch(SeekBar sb) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar sb) {
    }

    public void cancelClick(View v) {
        finish();
    }

    public void applyClick(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkDrawOverlayPermission();

        } else {
            shared.setAlpha(alpha);
            shared.setRed(red);
            shared.setGreen(green);
            shared.setBlue(blue);

            Intent i = new Intent(this, MainService.class);
            startService(i);

            makeNotification();
            finish();
        }

    }

    private void makeNotification() {
        NotificationCompat.Builder nb = new NotificationCompat.Builder(this);
        nb.setSmallIcon(R.drawable.ic_launcher);
        nb.setContentTitle(LocaleController.getString("ChangeTheScreenLights", R.string.ChangeTheScreenLights));
        nb.setContentText(LocaleController.getString("Active", R.string.Active));
        nb.setAutoCancel(true);
        Intent resultIntent = new Intent(this, FilterActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(FilterActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        nb.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0x355, nb.build());
    }

    public void checkDrawOverlayPermission() {
        /** check if we already  have permission to draw over other apps */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                /** if not construct intent to request permission */
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                /** request permission via start activity for result */
                startActivityForResult(intent, REQUEST_CODE);
            } else {
                shared.setAlpha(alpha);
                shared.setRed(red);
                shared.setGreen(green);
                shared.setBlue(blue);

                Intent i = new Intent(this, MainService.class);
                startService(i);

                makeNotification();
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /** check if received result code
         is equal our requested code for draw permission  */
        if (requestCode == REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(FilterActivity.this)) {
                    shared.setAlpha(alpha);
                    shared.setRed(red);
                    shared.setGreen(green);
                    shared.setBlue(blue);

                    Intent i = new Intent(this, MainService.class);
                    startService(i);

                    makeNotification();
                    finish();
                }
            }
        }
    }
}
