package com.dong.Diary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.dong.Main2Activity;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Emotion;
import com.microsoft.projectoxford.face.contract.Face;
import com.nhancv.kurentoandroid.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.utils.URIBuilder;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.util.EntityUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class WriteDiaryActivity extends AppCompatActivity {
    //api로 보낼 비트맵
    Bitmap bm;
    private ImageView imageView; // variable to hold the image view in our activity_main.xml
    private TextView resultText; // variable to hold the text view in our activity_main.xml
    Button BT_GetEmotion, BT_Save;
    String mCurrentPhotoPath, st_Emotion, st_id, st_name;
    String st_title, st_content;
    EditText et_title, et_content;
    //자동로그인용 sharedpreference
    SharedPreferences autoLogin;
    SharedPreferences.Editor editor;

    private static final int RESULT_LOAD_IMAGE  = 100;
    private static final int REQUEST_CAMERA_CODE = 300;
    private static final int REQUEST_PERMISSION_CODE = 200;

    // Replace `<API endpoint>` with the Azure region associated with
// your subscription key. For example,
// apiEndpoint = "https://westcentralus.api.cognitive.microsoft.com/face/v1.0"
    private final String apiEndpoint = "https://koreacentral.api.cognitive.microsoft.com/face/v1.0";

    // Replace `<Subscription Key>` with your subscription key.
// For example, subscriptionKey = "0123456789abcdef0123456789ABCDEF"
    private final String subscriptionKey = "";

    private final FaceServiceClient faceServiceClient =
            new FaceServiceRestClient(apiEndpoint, subscriptionKey);

    List<Double> EmotionList = new ArrayList<Double>();

    Emotion emotion;
    Double anger;
    Double contempt;
    Double disgust;
    Double fear;
    Double hapiness;
    Double neutral;
    Double sadness;
    Double surprise;

    String StrEmotionResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_diary);

        et_content = (EditText)findViewById(R.id.et_write_content);
        et_title = (EditText)findViewById(R.id.title_et_for_write);

        //자동로그인용 sharedpreference 파일.
        autoLogin = this.getSharedPreferences("autoLogin",Activity.MODE_PRIVATE);
        st_id = autoLogin.getString("id", "");
        st_name = autoLogin.getString("name", "");
        //자동로그인용 id, 비번이 저장되어 있는지 확인.
        if(!st_name.equals("")){
            Log.e("현재 저장된 자동로그인 아이디 in WriteDiaryActivity", st_id);
            Log.e("현재 저장된 자동로그인 비번 in WriteDiaryActivity", st_name);
        }


        imageView = (ImageView)findViewById(R.id.iv_DiaryImage);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //유저의 사진을 등록할 이미지뷰를 클릭하면 카메라와 앨범를 선택할 수 있다.
                AlertDialog.Builder dialog1 = new AlertDialog.Builder(WriteDiaryActivity.this);
                dialog1.setCancelable(true);
                dialog1.setNeutralButton("사진 촬영", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getCameraImage();
                    }
                });
                dialog1.setNegativeButton("앨범에서 사진 선택", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getImage();
                    }
                });
                dialog1.create();
                dialog1.show();
            }
        });
        resultText = (TextView)findViewById(R.id.textView12);

        BT_GetEmotion = (Button)findViewById(R.id.bt_GetEmotion);
        BT_GetEmotion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getEmotion();
                bm = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                detectAndFrame(bm);
            }
        });

        BT_Save = (Button)findViewById(R.id.bt_save);
        BT_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                st_title = et_title.getText().toString();
                st_content = et_content.getText().toString();
                st_Emotion = resultText.getText().toString();
                sendData();

            }
        });

    }
    private void detectAndFrame(final Bitmap imageBitmap) {
        resultText.setText("감정 분석 중");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(outputStream.toByteArray());

        AsyncTask<InputStream, String, Face[]> detectTask =
                new AsyncTask<InputStream, String, Face[]>() {
                    String exceptionMessage = "";

                    @Override
                    protected Face[] doInBackground(InputStream... params) {
                        try {
                            Log.e("face params", params.toString());
                            Face[] result = faceServiceClient.detect(
                                    params[0],
                                    true,         // returnFaceId
                                    false,        // returnFaceLandmarks
                                             // returnFaceAttributes:
                                new FaceServiceClient.FaceAttributeType[] {
                                    FaceServiceClient.FaceAttributeType.Emotion }

                            );
                            if (result == null){
                                Log.e("face detec nothing ", result.toString());

                                publishProgress(
                                        "Detection Finished. Nothing detected");
                                return null;
                            }
                            if (result != null) {
                                //인식한 얼굴의 갯수... 갯수? ㅋㅋ
                                Log.e( "result is not null", String.valueOf(result.length));
                                for (Face face : result) {
                                     emotion = face.faceAttributes.emotion;
                                     anger = emotion.anger;
                                     contempt = emotion.contempt;
                                     disgust = emotion.disgust;
                                     fear = emotion.fear;
                                     hapiness = emotion.happiness;
                                     neutral = emotion.neutral;
                                     sadness = emotion.sadness;
                                     surprise = emotion.surprise;
                                    EmotionList.add(anger);
                                    EmotionList.add(contempt);
                                    EmotionList.add(disgust);
                                    EmotionList.add(fear);
                                    EmotionList.add(hapiness);
                                    EmotionList.add(neutral);
                                    EmotionList.add(sadness);
                                    EmotionList.add(surprise);

                                    if (EmotionList.size() > 0) {
                                        double highest = EmotionList.get(0);
                                        int highestIndex = 0;

                                        for (int s = 1; s < EmotionList.size(); s++){
                                            double curValue = EmotionList.get(s);
                                            if (curValue > highest) {
                                                highest = curValue;
                                                highestIndex = s;
                                            }
                                        }

                                        Log.e("highest fitness = " , String .valueOf(highest));
                                        Log.e(" indoexOf" , String.valueOf(highestIndex));

                                        if(highestIndex == 0){
                                            StrEmotionResult = "화나요!!";
                                        }else if(highestIndex == 1){
                                            StrEmotionResult = "경멸스러워요!!";
                                        }else if(highestIndex == 2){
                                            StrEmotionResult = "역겹다!!";
                                        }else if(highestIndex == 3){
                                            StrEmotionResult = "무서웡!!";
                                        }else if(highestIndex == 4){
                                            StrEmotionResult = "행복행!!";
                                        }else if(highestIndex == 5){
                                            StrEmotionResult = "그저그래요";
                                        }else if(highestIndex == 6){
                                            StrEmotionResult = "슬퍼요";
                                        }else if(highestIndex == 7){
                                            StrEmotionResult = "놀랍군요!";
                                        }
                                    }

                                }
                            }

                            return result;
                        } catch (Exception e) {
                            Log.e("face detec result error ", e.toString());
                            exceptionMessage = String.format(
                                    "Detection failed: %s", e.getMessage());
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(Face[] result) {
                        //TODO: update face frames

                        if(!exceptionMessage.equals("")){
                            showError(exceptionMessage);
                        }
                        if (result == null) return;

                        resultText.setText(StrEmotionResult);

                        EmotionList.clear();
                    }
                };

        detectTask.execute(inputStream);
    }

    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }})
                .create().show();
    }

    // when the "GET EMOTION" Button is clicked this function is called
    public void getEmotion() {
        // run the GetEmotionCall class in the background
        GetEmotionCall emotionCall = new GetEmotionCall(imageView);
        emotionCall.execute();
    }


    // when the "GET IMAGE" Button is clicked this function is called
    public void getImage() {
        // check if user has given us permission to access the gallery
        if(checkPermission()) {
            Intent choosePhotoIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(choosePhotoIntent, RESULT_LOAD_IMAGE);
        }
        else {
            requestPermission();
        }
    }


    // This function gets the selected picture from the gallery and shows it on the image view
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // get the photo URI from the gallery, find the file path from URI and send the file path to ConfirmPhoto
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {

            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            // a string variable which will store the path to the image in the gallery
            String picturePath= cursor.getString(columnIndex);
            cursor.close();
            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
            imageView.setImageBitmap(bitmap);
        }

        if (requestCode == REQUEST_CAMERA_CODE && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
        }
    }


    // convert image to base 64 so that we can send the image to Emotion API
    public byte[] toBase64(ImageView imgPreview) {
        Bitmap bm = ((BitmapDrawable) imgPreview.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
        return baos.toByteArray();
    }


    // if permission is not given we get permission
    private void requestPermission() {
        ActivityCompat.requestPermissions(WriteDiaryActivity.this,new String[]{READ_EXTERNAL_STORAGE,CAMERA}, REQUEST_PERMISSION_CODE);
    }


    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),READ_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(),CAMERA);
        return result == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
    }


    public void getCameraImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_CAMERA_CODE);
        }
    }

    // asynchronous class which makes the api call in the background
    private class GetEmotionCall extends AsyncTask<Void, Void, String> {

        private final ImageView img;

        GetEmotionCall(ImageView img) {
            this.img = img;
        }

        // this function is called before the api call is made
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            resultText.setText("감정 분석 중");
        }

        // this function is called when the api call is made
        @Override
        protected String doInBackground(Void... params) {
            HttpClient httpclient = HttpClients.createDefault();
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            try {
                URIBuilder builder = new URIBuilder("https://westcentralus.api.cognitive.microsoft.com/face/v1.0/detect");

                URI uri = builder.build();
                HttpPost request = new HttpPost(uri);
                request.setHeader("Content-Type", "application/octet-stream");
                // enter you subscription key here
                request.setHeader("Ocp-Apim-Subscription-Key", "ddb308c7-d2e8-43e4-887e-f153880279eb");

                // Request body.The parameter of setEntity converts the image to base64
                request.setEntity(new ByteArrayEntity(toBase64(img)));

                // getting a response and assigning it to the string res
                HttpResponse response = httpclient.execute(request);
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                Log.e("응답값 확인", result);
                return result;

            }
            catch (Exception e){
                return "null";
            }

        }

        // this function is called when we get a result from the API call
        @Override
        protected void onPostExecute(String result) {
            JSONArray jsonArray = null;
            try {
                // convert the string to JSONArray
                jsonArray = new JSONArray(result);
                String emotions = "";
                // get the scores object from the results
                for(int i = 0;i<jsonArray.length();i++) {
                    JSONObject jsonObject = new JSONObject(jsonArray.get(i).toString());
                    JSONObject scores = jsonObject.getJSONObject("scores");
                    double max = 0;
                    String emotion = "";
                    for (int j = 0; j < scores.names().length(); j++) {
                        if (scores.getDouble(scores.names().getString(j)) > max) {
                            max = scores.getDouble(scores.names().getString(j));
                            emotion = scores.names().getString(j);
                        }
                    }
                    emotions += emotion + "\n";
                }
                resultText.setText(emotions);
            } catch (JSONException e) {
                resultText.setText("No emotion detected. Try again later");
            }
        }
    }


    public void sendData(){
        new Thread(){
            public void run(){
                requestWebServer(st_id, st_title, st_content, st_Emotion, callback);

            }
        }.start();
    }

    /** 웹 서버로 요청을 한다. */
    public void requestWebServer(String id, String title, String content, String emotion, Callback callback) {


        imageView.buildDrawingCache();
        Bitmap bitmap = imageView.getDrawingCache();
        File storageLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String fileName = String.format("%d.jpg", System.currentTimeMillis());
        File file = new File(storageLoc, fileName);
        try{
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.e("이미지 경로", file.getAbsolutePath());
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        sendBroadcast(intent);

        mCurrentPhotoPath = file.getAbsolutePath();


         OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("user_id", id)
                .addFormDataPart("title", title)
                .addFormDataPart("content", content)
                .addFormDataPart("emotion", emotion)
                .addFormDataPart("file", fileName, RequestBody.create(MultipartBody.FORM, new File(mCurrentPhotoPath)))

                .build();

        Request request = new Request.Builder()
                .url("http://222.239.249.149/diary_insert.php")
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    private final Callback callback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.e("Post_write_Activity", "콜백오류:"+e.getMessage());
        }
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String body = response.body().string();
            Log.e("Post_write_Activity", "응답한 Body:"+body);
            //uploadFile(mCurrentPhotoPath);

            Intent intent = new Intent(getApplicationContext(), Main2Activity.class);
            startActivity(intent);
            finish();
        }
    };




    public void uploadFile(String filePath) {

        imageView.buildDrawingCache();
        Bitmap bitmap = imageView.getDrawingCache();
        File storageLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String fileName = String.format("%d.jpg", System.currentTimeMillis());
        File file = new File(storageLoc, fileName);
        try{
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.e("이미지 경로", file.getAbsolutePath());
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        sendBroadcast(intent);

        mCurrentPhotoPath = file.getAbsolutePath();
        String url = "http://222.239.249.149/ImageUpload.php";

        UploadFile1 uploadFile = new UploadFile1();
        uploadFile.setPath(mCurrentPhotoPath);
        uploadFile.execute(url);
    }


    public class UploadFile1 extends AsyncTask<String, String, String> {
        String fileName; // 파일 위치

        HttpURLConnection conn = null; // 네트워크 연결 객체
        DataOutputStream dos = null; // 서버 전송 시 데이터 작성한 뒤 전송

        String lineEnd = "\r\n"; // 구분자
        String twoHyphens = "--";
        String boundary = "*****";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024;
        File sourceFile;
        int serverResponseCode;
        String TAG = "FileUpload";


        public void setPath(String uploadFilePath) {
            this.fileName = uploadFilePath;
            this.sourceFile = new File(uploadFilePath);
        }

        @Override
        protected String doInBackground(String... strings) {

            if (!sourceFile.isFile()) { // 해당 위치의 파일이 있는지 검사
                Log.e(TAG, "sourceFile(" + fileName + ") is Not A File");
                return null;
            } else {
                String success = "Success";
                Log.i(TAG, "sourceFile(" + fileName + ") is A File");
                try {
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL(strings[0]);
                    Log.e("strings[0]", strings[0]);

                    // Open a HTTP  connection to  the URL
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST"); // 전송 방식
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary); // boundary 기준으로 인자를 구분함
                    conn.setRequestProperty("uploaded_file", fileName);
                    Log.e(TAG, "fileName: " + fileName);

                    // dataoutput은 outputstream이란 클래스를 가져오며, outputStream는 FileOutputStream의 하위 클래스이다.
                    // output은 쓰기, input은 읽기, 데이터를 전송할 때 전송할 내용을 적는 것으로 이해할 것
                    dos = new DataOutputStream(conn.getOutputStream());

                    // 사용자 이름으로 폴더를 생성하기 위해 사용자 이름을 서버로 전송한다. 하나의 인자 전달 data1 = newImage
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"data1\"" + lineEnd); // name으 \ \ 안 인자가 php의 key
                    dos.writeBytes(lineEnd);
                    dos.writeBytes("FlytogetherImage"); // newImage라는 값을 넘김
                    dos.writeBytes(lineEnd);


                    // 이미지 전송, 데이터 전달 uploadded_file라는 php key값에 저장되는 내용은 fileName
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\"; filename=\"" + fileName + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);

                    // create a buffer of  maximum size
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    // send multipart form data necesssary after file data..., 마지막에 two~~ lineEnd로 마무리 (인자 나열이 끝났음을 알림)
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    // Responses from the server (code and message)
                    serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();

                    Log.e(TAG, "[UploadImageToServer] HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);

                    if (serverResponseCode == 200) {

                    }

                    // 결과 확인
                    BufferedReader rd = null;

                    rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    String line = null;
                    while ((line = rd.readLine()) != null) {
                        Log.e("Upload State", line);
                    }

                    //close the streams //
                    fileInputStream.close();
                    dos.flush();
                    dos.close();

                } catch (Exception e) {

                    Log.e(TAG + " Error", e.toString());
                }
                return success;
            }
        }
    }

}
