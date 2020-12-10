package com.samsungpoc.ssshare;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();
    private static String screenShotDisplayName;
    private static String AUTHORITY = "com.samsungpoc.ssshare" + ".fileprovider";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // Declare read write permission here.
        }
    }

    public void takeScreenShot(View view) {

        Toast.makeText(this, "Please click Share Via button to take and share screen shot.", Toast.LENGTH_SHORT);
    }

    private Bitmap createBitmap() {
        Date now = new Date();

        Log.d(TAG, "now: " + android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now));
        screenShotDisplayName = "SS_" + now + ".png";

        View v1 = getWindow().getDecorView().getRootView();
        v1.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);

        return bitmap;
    }

    public void shareScreenShot(View view) {
        Bitmap bitmap = createBitmap();

        if(bitmap == null) {
            Log.e(TAG, "screen shot bitmap not created.");
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Downloads.DISPLAY_NAME, screenShotDisplayName);
        contentValues.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        contentValues.put(MediaStore.Downloads.MIME_TYPE, "image/*");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            Uri screenUri = this.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);

            try(OutputStream fileOutputStream = this.getContentResolver().openOutputStream(screenUri)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, "File does not exist");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to create screen image");
            }
            launchSimpleShare(screenUri);
        } else {

            File screenDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File screenFile = new File(screenDir, screenShotDisplayName);

            try(FileOutputStream fileOutputStream = new FileOutputStream(screenFile)){
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Uri fileUri = FileProvider.getUriForFile(this, AUTHORITY, screenFile);
        }

    }

    private void launchSimpleShare(Uri fileUri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.setType("image/*");

        Intent chooser = Intent.createChooser(shareIntent, "Share Via");
        chooser.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        this.startActivity(chooser);
    }
}