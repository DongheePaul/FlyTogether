package com.dong.FaceDetection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;
import com.nhancv.kurentoandroid.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FaceDetectionActivity extends AppCompatActivity {
    //카메라에서 찍은 사진을 담을 비트맵 객체
    Bitmap bitmap;
    Button bt_save;
    private int typeFace = 0;
    private int typeFlash = 0;
    private boolean flashmode = false;
    private static final String TAG = "FaceDetection";
    Bitmap mutableBitmap;
    SparseArray<Face> faces;
    ImageView imageView;
    FaceDetector detector;
    String mCurrentPhotoPath;
    private static final int MASK[] = {
            R.id.no_filter,
            R.id.hair,
            R.id.op,
            R.id.snap,
            R.id.glasses2,
            R.id.glasses3,
            R.id.glasses4,
            R.id.glasses5,
            R.id.mask,
            R.id.mask2,
            R.id.mask3,
            R.id.dog,
            R.id.cat2
    };
    private static final int MASKtype[] = {
            R.drawable.no_filter,
            R.drawable.hair,
            R.drawable.op,
            R.drawable.snap,
            R.drawable.glasses2,
            R.drawable.glasses3,
            R.drawable.glasses4,
            R.drawable.glasses5,
            R.drawable.mask,
            R.drawable.mask2,
            R.drawable.mask3,
            R.drawable.dog,
            R.drawable.cat2
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detection);
        bt_save = (Button)findViewById(R.id.bt_Save);
        bt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveImage();
            }
        });

        ImageButton no_filter = (ImageButton) findViewById(R.id.no_filter);
        no_filter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 0;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
            }
        });

        ImageButton hair = (ImageButton) findViewById(R.id.hair);
        hair.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 1;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                SetMaskOnFace();
            }
        });


        ImageButton op = (ImageButton) findViewById(R.id.op);
        op.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 2;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                SetMaskOnFace();
            }
        });

        ImageButton snap = (ImageButton) findViewById(R.id.snap);
        snap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 3;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                SetMaskOnFace();
            }
        });

        ImageButton glasses2 = (ImageButton) findViewById(R.id.glasses2);
        glasses2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 4;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                SetMaskOnFace();
            }
        });

        ImageButton glasses3 = (ImageButton) findViewById(R.id.glasses3);
        glasses3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 5;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                SetMaskOnFace();
            }
        });

        ImageButton glasses4 = (ImageButton) findViewById(R.id.glasses4);
        glasses4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 6;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                SetMaskOnFace();
            }
        });

        ImageButton glasses5 = (ImageButton) findViewById(R.id.glasses5);
        glasses5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 7;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                SetMaskOnFace();
            }
        });

        ImageButton mask = (ImageButton) findViewById(R.id.mask);
        mask.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 8;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                SetMaskOnFace();
            }
        });

        ImageButton mask2 = (ImageButton) findViewById(R.id.mask2);
        mask2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 9;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                SetMaskOnFace();
            }
        });

        ImageButton mask3 = (ImageButton) findViewById(R.id.mask3);
        mask3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 10;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                SetMaskOnFace();
            }
        });

        ImageButton dog = (ImageButton) findViewById(R.id.dog);
        dog.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 11;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                SetMaskOnFace();
            }
        });

        ImageButton cat2 = (ImageButton) findViewById(R.id.cat2);
        cat2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 12;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                SetMaskOnFace();
            }
        });

        //얼굴을 인식하는 디텍터 객체
        detector = new FaceDetector.Builder(this)
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();

        //카메라 어플에서 찍은 사진 파일의 경로를 인텐트로 받는다.
        Intent intent = getIntent();
        imageView = findViewById(R.id.imageView);
        mCurrentPhotoPath = intent.getStringExtra("mCurrentPhotoPath");

        imageView.post(new Runnable() {
            @Override
            public void run() {
                if (mCurrentPhotoPath != null) {
                    //카메라에서 찍은 사진을 비트맵으로 변환 후  이미지뷰에 셋한다.
                    bitmap = getBitmapFromPathForImageView(mCurrentPhotoPath, imageView);
                    imageView.setImageBitmap(bitmap);
                }
            }
        });

    }

    public void SaveImage(){
        Log.e("saveimage", "실행");
        //to get the image from the ImageView (say iv)
        BitmapDrawable draw = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = draw.getBitmap();

        FileOutputStream outStream = null;
        String state = Environment.getExternalStorageState();
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = null;
        if (state.contains(Environment.MEDIA_MOUNTED)) {
            dir = new File(sdCard.getAbsolutePath() + "/FlyTogether");
        }
        else {
            dir = new File(sdCard.getAbsolutePath() + "/FlyTogether");
        }
        if(!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = String.format("%d.jpg", System.currentTimeMillis());
        File outFile = new File(dir, fileName);
        try {
            outStream = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(outFile));
        sendBroadcast(intent);
        finish();
    }
    public void SetMaskOnFace(){
        // run image related code after the view was laid out
        // to have all dimensions calculated
        imageView.post(new Runnable() {
            @Override
            public void run() {
                if (mCurrentPhotoPath != null) {
                    //얼굴 인식을 위해 프레임 객체를 생성.
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    faces = detector.detect(frame);
                    Log.d(TAG, "Faces detected: " + String.valueOf(faces.size()));

                    Drawable drawable = getResources().getDrawable(MASKtype[typeFace]);
                    //Drawable drawable = getResources().getDrawable(R.drawable.snap);
                    Bitmap maskbitmap = ((BitmapDrawable)drawable).getBitmap();

                    mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    Canvas canvas = new Canvas(mutableBitmap);
                    for (int i = 0; i < faces.size(); ++i) {
                        Face face = faces.valueAt(i);
                        for (Landmark landmark : face.getLandmarks()) {
                            int cx = (int) (landmark.getPosition().x);
                            int cy = (int) (landmark.getPosition().y);
                            //canvas.drawCircle(cx, cy, 10, paint);
                        }
                        Path path = new Path();
                        path.moveTo(face.getPosition().x, face.getPosition().y);
                        path.lineTo(face.getPosition().x + face.getWidth(), face.getPosition().y);
                        path.lineTo(face.getPosition().x + face.getWidth(), face.getPosition().y + face.getHeight());
                        path.lineTo(face.getPosition().x, face.getPosition().y + face.getHeight());
                        path.close();
                        int width = (int)face.getWidth();
                        int height = (int)face.getHeight();
                        Bitmap maskbitmapresize = resize(maskbitmap, width, height);

                        //canvas.drawPath(path, redPaint);
                        canvas.drawBitmap(maskbitmapresize, face.getPosition().x, face.getPosition().y, null);
                    }
                    imageView.setImageBitmap(mutableBitmap);
                }
            }
        });
    }

    private Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float) maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float) maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }
    private Bitmap getBitmapFromPathForImageView(String mCurrentPhotoPath, ImageView imageView) {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        Bitmap rotatedBitmap = bitmap;

        // rotate bitmap if needed
        try {
            ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(bitmap, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(bitmap, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(bitmap, 270);
                    break;
            }
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return rotatedBitmap;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}
