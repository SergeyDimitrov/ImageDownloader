package com.example.pb.newthread;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.AsyncTaskLoader;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Downloader extends AsyncTaskLoader {
    public static final String MSG_UPDATE_PROGRESS = "MSG_UPDATE_PROGRESS";
    private Handler handler;
    private final Context context;

    public Downloader(Context context) {
        super(context);
        this.context = context.getApplicationContext();
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public Void loadInBackground() {
        URL url;
        HttpURLConnection connection = null;

        try {
            url = new URL(context.getResources().getString(R.string.image_url));
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
        } catch (Exception e) {

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
                updateProgress(bytesWritten * 100 / fileLength);
            }
            out.flush();
            in.close();
            out.close();
            connection.disconnect();
        } catch (Exception e) {
            // ...
        }


        return null;
    }

    private void updateProgress(int progress) {
        if (handler == null) {
            return;
        }
        Message msg = handler.obtainMessage();
        msg.obj = MSG_UPDATE_PROGRESS;
        msg.arg1 = progress;
        handler.sendMessage(msg);
    }
}