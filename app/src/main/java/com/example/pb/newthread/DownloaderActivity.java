package com.example.pb.newthread;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;

public class DownloaderActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Void> {

    private enum State {
        STATE_UNKNOWN, STATE_IDLE, STATE_DOWNLOADING, STATE_DOWNLOADED;
    }

    private ProgressBar progressBar;
    private TextView statusView;
    private Button commandButton;

    private Downloader downloader;
    private static final int IMAGE_LOADER_ID = 21;

    private State currentState = State.STATE_UNKNOWN;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            progressBar.setProgress(msg.arg1);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.downloader_activity);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        statusView = (TextView) findViewById(R.id.status_view);
        commandButton = (Button) findViewById(R.id.command_button);

        downloader = (Downloader) getSupportLoaderManager().getLoader(IMAGE_LOADER_ID);
        if (downloader != null) {
            downloader.setHandler(handler);
            setState(State.STATE_DOWNLOADING);
            getSupportLoaderManager().initLoader(IMAGE_LOADER_ID, null, this);
        } else {
            setState(State.STATE_IDLE);
        }
    }

    private void setState(State state) {
        if (currentState == state) {
            return;
        }

        currentState = state;
        switch (currentState) {
            case STATE_IDLE:
                statusView.setText(R.string.status_idle);
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                commandButton.setText(R.string.download);
                commandButton.setEnabled(true);
                commandButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setState(State.STATE_DOWNLOADING);
                        getSupportLoaderManager().initLoader(IMAGE_LOADER_ID, null, DownloaderActivity.this);
                    }
                });
                break;
            case STATE_DOWNLOADING:
                statusView.setText(R.string.status_downloading);
                progressBar.setVisibility(ProgressBar.VISIBLE);
                commandButton.setText(R.string.downloading);
                commandButton.setEnabled(false);
                commandButton.setOnClickListener(null);
                break;
            case STATE_DOWNLOADED:
                statusView.setText(R.string.status_downloaded);
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                commandButton.setEnabled(true);
                commandButton.setText(R.string.open);
                commandButton.setEnabled(true);
                commandButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent openingIntent = new Intent();
                        openingIntent.setAction(Intent.ACTION_VIEW);
                        File image = new File(getFilesDir().getPath() + "/android_image.jpg");
                        openingIntent.setDataAndType(Uri.fromFile(image), "image/*");
                        startActivity(openingIntent);
                    }
                });
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (downloader != null) {
            downloader.setHandler(null);
        }
        handler = null;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        downloader = new Downloader(this);
        downloader.setHandler(handler);
        return downloader;
    }

    @Override
    public void onLoadFinished(Loader<Void> loader, Void data) {
        setState(State.STATE_DOWNLOADED);
    }

    @Override
    public void onLoaderReset(Loader<Void> loader) {

    }
}
