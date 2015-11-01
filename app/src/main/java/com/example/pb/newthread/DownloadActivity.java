package com.example.pb.newthread;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

// Errors?
// Design

public class DownloadActivity extends Activity {
    private static final String STATE_KEY = "state_key";
    private static final int STATE_IDLE = 1;
    private static final int STATE_DOWNLOADING = 2;
    private static final int STATE_DONWLOADED = 3;

    private TextView statusView;
    private Button commandButton;
    private ProgressBar progressBar;
    private MyAsyncTask task;
    private int currState;

    public Object onRetainNonConfigurationInstance() {
        if (task != null) task.setContext(null);
        return task;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusView = (TextView) findViewById(R.id.status_view);
        commandButton = (Button) findViewById(R.id.start_button);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        if (savedInstanceState == null || savedInstanceState.getInt(STATE_KEY) == STATE_IDLE) {
            currState = STATE_IDLE;
        } else if (savedInstanceState.getInt(STATE_KEY) == STATE_DOWNLOADING) {
            currState = STATE_DOWNLOADING;
        } else {
            currState = STATE_DONWLOADED;
        }

        switch (currState) {
            case STATE_IDLE:
                makeFirstState();
                break;
            case STATE_DOWNLOADING:
                task = (MyAsyncTask) getLastNonConfigurationInstance();
                task.setContext(this);
                makeSecondState();
                break;
            case STATE_DONWLOADED:
                makeThirdState();
                break;
        }
    }

    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(STATE_KEY, currState);
    }

    public void makeFirstState() {
        currState = STATE_IDLE;
        statusView.setText(R.string.status_idle);
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        commandButton.setText(R.string.download);
        commandButton.setEnabled(true);
        commandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task = new MyAsyncTask(DownloadActivity.this);
                task.execute();
            }
        });
    }

    public void makeSecondState() {
        currState = STATE_DOWNLOADING;
        statusView.setText(R.string.status_downloading);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        progressBar.setMax(100);
        commandButton.setText(R.string.downloading);
        commandButton.setEnabled(false);
    }

    public void makeThirdState() {
        currState = STATE_DONWLOADED;
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
    }

    private static class MyAsyncTask extends AsyncTask<Void, Integer, Integer> {
        private static final int CONNECTION_ERROR = 1;
        private static final int SERVER_DOWN_ERROR = 2;
        private Context context;

        public MyAsyncTask(Context context) {
            this.context = context;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            ((DownloadActivity)context).makeSecondState();
        }

        protected Integer doInBackground(Void... params) {

            URL url;
            HttpURLConnection connection;

            try {
                url = new URL(context.getResources().getString(R.string.image_url));
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
            } catch (Exception e) {
                return CONNECTION_ERROR;
            }

            try {
                InputStream in = new BufferedInputStream(connection.getInputStream(), 1024);
                OutputStream out = context.openFileOutput("android_image.jpg", Context.MODE_WORLD_READABLE);

                int fileLength = connection.getContentLength();
                int bytesWritten = 0;
                int bytesRead;
                byte[] buffer = new byte[1024];

                while ((bytesRead = in.read(buffer)) > 0) {
                    bytesWritten += bytesRead;
                    out.write(buffer, 0, bytesRead);
                    publishProgress(bytesWritten * 100 / fileLength);
                }
                out.flush();
                in.close();
                out.close();
                connection.disconnect();

                if (fileLength == -1 || fileLength != bytesWritten) {
                    return SERVER_DOWN_ERROR;
                }

            } catch (Exception e) {
                // ...
            }


            return null;
        }

        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            ((DownloadActivity)context).progressBar.setProgress(values[0]);
        }

        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            if (result == null) ((DownloadActivity)context).makeThirdState();
            else {
                int errMsg = -1;

                switch (result) {
                    case CONNECTION_ERROR:
                        errMsg = R.string.connection_error;
                        break;
                    case SERVER_DOWN_ERROR:
                        errMsg = R.string.server_error;
                        break;
                }
                Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show();
                ((DownloadActivity)context).makeFirstState();
            }
        }

        public void setContext(Context context) {
            this.context = context;
        }
    }


}
