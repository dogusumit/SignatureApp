package com.dogusumit.signature;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    SignaturePad imzaAlani;
    Button btn1, btn2, btn3;
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imzaAlani = findViewById(R.id.signature_pad);
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);

        isStoragePermissionGranted();

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    imzaAlani.clear();
                } catch (Exception e) {
                    toastla(getString(R.string.hata) + e.getMessage());
                }
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kaydet();
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gonder();
            }
        });

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            try {
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null)
                    actionBar.hide();
                findViewById(R.id.reklamAlani).setVisibility(View.GONE);
            } catch (Exception e) {
                toastla(e.getLocalizedMessage());
            }
        } else {
            try {
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null)
                    actionBar.show();
                findViewById(R.id.reklamAlani).setVisibility(View.VISIBLE);
                AdView mAdView = findViewById(R.id.adView);
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
            } catch (Exception e) {
                toastla(e.getLocalizedMessage());
            }
        }
    }

    void toastla(String s) {
        Toast.makeText(context, s, Toast.LENGTH_LONG).show();
    }

    File kaydet() {
        try {
            File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/" + getString(R.string.path));
            if (dir.exists() || dir.mkdirs()) {
                String dosya = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US).format(Calendar.getInstance().getTime()) + ".jpg";
                File file = new File(dir, dosya);
                if (!file.exists() || file.delete()) {
                    FileOutputStream out = new FileOutputStream(file);
                    imzaAlani.getSignatureBitmap().compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.flush();
                    out.close();

                    addImageToGallery(file.getPath(), context);

                    toastla(getString(R.string.kayit_ok) + dir.getPath());
                    return file;
                }
            }
            toastla(getString(R.string.file_error));
            return null;
        } catch (Exception e) {
            toastla(getString(R.string.hata) + e.getMessage());
            return null;
        }
    }

    void gonder() {
        try {
            File file = kaydet();
            if (file != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setType("image/jpeg");
                Uri contentUri;
                if (Build.VERSION.SDK_INT >= 24) {
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    contentUri = FileProvider.getUriForFile(context, "com.dogusumit.fileprovider", file);
                } else {
                    contentUri = Uri.fromFile(file);
                }
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));
            }
        } catch (Exception e) {
            toastla(getString(R.string.hata) + e.getMessage());
        }
    }

    public void addImageToGallery(final String filePath, final Context context) {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.MediaColumns.DATA, filePath);
            context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } catch (Exception e) {
            toastla(getString(R.string.hata) + e.getMessage());
        }
    }

    public void isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }


    private void uygulamayiOyla() {
        Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
            } catch (Exception ane) {
                toastla(getString(R.string.hata) + e.getMessage());
            }
        }
    }

    private void marketiAc() {
        try {
            Uri uri = Uri.parse("market://developer?id=dogusumit");
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/developer?id=dogusumit")));
            } catch (Exception ane) {
                toastla(getString(R.string.hata) + e.getMessage());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.oyla:
                uygulamayiOyla();
                return true;
            case R.id.market:
                marketiAc();
                return true;
            case R.id.cikis:
                System.exit(0);
                android.os.Process.killProcess(android.os.Process.myPid());
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}