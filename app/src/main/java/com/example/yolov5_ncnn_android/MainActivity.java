package com.example.yolov5_ncnn_android;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private static final int TAKE_PHOTO = 3;

    private Uri mUri;

    private TextView _500_;
    private TextView _100_;
    private TextView _50_;
    private TextView _10_;
    private TextView _5_;
    private TextView _1_;
    private TextView resultTextView;
    private ImageView resultImageView;

    private double threshold = 0.4,nms_threshold = 0.7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int permission_storage = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission_camera = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permission_storage != PackageManager.PERMISSION_GRANTED || permission_camera != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    REQUEST_PERMISSIONS
            );
        }
        //YOLOv5.init(getAssets());
        YOLOv5.initCustomLayer(getAssets());
        _500_ = findViewById(R.id._500_);
        _100_ = findViewById(R.id._100_);
        _50_ = findViewById(R.id._50_);
        _10_ = findViewById(R.id._10_);
        _5_ = findViewById(R.id._5_);
        _1_ = findViewById(R.id._1_);
        resultTextView = findViewById(R.id.total);
        resultImageView = findViewById(R.id.imageView);
        Button file = findViewById(R.id.file);
        Button camera = findViewById(R.id.camera);
        file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int permission_storage = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission_storage == PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, REQUEST_PICK_IMAGE);
                }else{
                    Toast.makeText(MainActivity.this, "No Permission!", Toast.LENGTH_SHORT).show();
                }

            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int permission_storage = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                int permission_camera = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
                if (permission_storage == PackageManager.PERMISSION_GRANTED && permission_camera == PackageManager.PERMISSION_GRANTED){
                    String path = getFilesDir() + File.separator + "images" + File.separator;
                    File file = new File(path, "test.jpg");

                    if (file.isFile() && file.exists()) {
                        file.delete();
                    }
                    if(!file.getParentFile().exists())
                        file.getParentFile().mkdirs();

                    mUri = FileProvider.getUriForFile(MainActivity.this, "com.example.yolov5_ncnn_android.images", file);

                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
                    startActivityForResult(intent, TAKE_PHOTO);
                }else{
                    Toast.makeText(MainActivity.this, "No Permission!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap image = null;
        Bitmap mutableBitmap = null;
        switch (requestCode) {
            case REQUEST_PICK_IMAGE:
                if(data==null){
                    return;
                }
                image = getPicture(data.getData());
                break;
            case TAKE_PHOTO:
                try {
                    image = getImage(mUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
        }
        if(image==null){
            return;
        }
        //Box[] result = YOLOv5.detect(image, threshold, nms_threshold);
        Box[] result = YOLOv5.detectCustomLayer(image, threshold, nms_threshold);
        mutableBitmap = image.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        final Paint boxPaint = new Paint();
        boxPaint.setAlpha(200);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(4 * image.getWidth() / 800);
        boxPaint.setTextSize(40 * image.getWidth() / 800);


        int total = 0;
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("500", 0);
        map.put("100", 0);
        map.put("50", 0);
        map.put("10", 0);
        map.put("5", 0);
        map.put("1", 0);

        for (Box box : result) {
            boxPaint.setColor(box.getColor());
            boxPaint.setStyle(Paint.Style.FILL);
            canvas.drawText(box.getLabel(), box.x0, box.y0, boxPaint);
            boxPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(box.getRect(), boxPaint);
            int i = Integer.valueOf(box.getLabel()).intValue();
            map.put(box.getLabel(), map.get(box.getLabel()) + 1);
            total = i + total;
        }
        resultImageView.setImageBitmap(mutableBitmap);
        Iterator<Map.Entry<String, Integer>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> entry = it.next();
            if (entry.getKey().equals("500")) {
                _500_.setText(entry.getKey() + ": " + String.valueOf(entry.getValue()));
            } else if (entry.getKey().equals("100")) {
                _100_.setText(entry.getKey() + ": " + String.valueOf(entry.getValue()));
            } else if (entry.getKey().equals("50")) {
                _50_.setText(entry.getKey() + ": " + String.valueOf(entry.getValue()));
            } else if (entry.getKey().equals("10")) {
                _10_.setText(entry.getKey() + ": " + String.valueOf(entry.getValue()));
            } else if (entry.getKey().equals("5")) {
                _5_.setText(entry.getKey() + ": " + String.valueOf(entry.getValue()));
            } else {
                _1_.setText(entry.getKey() + ": " + String.valueOf(entry.getValue()));
            }
        }
        resultTextView.setText("    I have " + String.valueOf(total));

    }

    public Bitmap getPicture(Uri selectedImage) {
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = this.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
        int rotate = readPictureDegree(picturePath);
        return rotateBitmapByDegree(bitmap,rotate);
    }

    public int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                    bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

    public Bitmap getImage(Uri uri) throws FileNotFoundException {
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = 1;
        InputStream input = getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        return bitmap;
    }

}