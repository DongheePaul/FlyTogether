package com.dong.EtheriumToken;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.nhancv.kurentoandroid.R;
import com.nhancv.kurentoandroid.databinding.ActivityWalletBinding;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import info.bcdev.librarysdkew.GetCredentials;
import info.bcdev.librarysdkew.interfaces.callback.CBBip44;
import info.bcdev.librarysdkew.interfaces.callback.CBGetCredential;
import info.bcdev.librarysdkew.interfaces.callback.CBLoadSmartContract;
import info.bcdev.librarysdkew.interfaces.callback.CBSendingEther;
import info.bcdev.librarysdkew.interfaces.callback.CBSendingToken;
import info.bcdev.librarysdkew.utils.InfoDialog;
import info.bcdev.librarysdkew.wallet.generate.Bip44;

/**
 * 지갑 만들어주고, 정보 불러오는 액티비티 (로 만들 예정)
 */
public class WalletActivity extends AppCompatActivity implements CBGetCredential, CBLoadSmartContract, CBBip44, CBSendingEther, CBSendingToken {
    ActivityWalletBinding binding;
    private InfoDialog mInfoDialog;
    String password, password_for_getCredentials;
//    mInfoDialog = new InfoDialog(this);

    Credentials mCredentials;

    private File keydir;
    File[] listfiles;

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        //데이터바인딩
        binding = DataBindingUtil.setContentView(this, R.layout.activity_wallet);
        //다이얼로그를 띄우는 메소드
        mInfoDialog = new InfoDialog(this);
        //아마 지갑 파일 경로 가져오는 메소드
        keydir = this.getFilesDir();
         listfiles = keydir.listFiles();
        Log.e("keydir. 아마도 해당어플에 할당된 디바이스내의 경로", String.valueOf(keydir));
        Log.e("listfiles. keydir의 파일리스트", String.valueOf(listfiles));
        //권한 확인하는 메소드
        checkPermission();

        //지갑 만들기 버튼 클릭 시
        //입력된 비밀번호를 가지고 지갑을 생성 후 지갑 주소를 메인액티비티로 넘겨준다.
        binding.btCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password = "";
                CreateWallet();

            }
        });

        //지갑 로그인 버튼 클릭 시
        //입력한 비밀번호로 저장된 지갑의 비밀번호와 맞는지 확인 후
        //맞다면 넘어가고, 틀리다면 알림 띄운다.
        binding.btForLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listfiles.length == 0){
                    Toast.makeText(getApplicationContext(), "저장된 지갑 없음", Toast.LENGTH_LONG).show();
                }
                password_for_getCredentials = binding.etPasswordForLogin.getText().toString();
                Log.e("로긴비번 in walletActivity", "123");
                getCredentials(keydir);
            }
        });
    }
    /* Create Wallet */
    private void CreateWallet(){
        Bip44 bip44 = new Bip44();
        bip44.registerCallBack(this);
        bip44.execute(password);
        mInfoDialog.Get("지갑생성", "잠시만 기다려주세요");
    }

    @Override
    public void backGeneration(Map<String, String> result, Credentials credentials) {
        mCredentials = credentials;
        Log.e("생성된 주소 경로",result.get("address"));
        //생성된 지갑을 저장
        //여기서 지갑파일 만든다.
        new SaveWallet(keydir,mCredentials,password).execute();
        mInfoDialog.Dismiss();
    }


    //크리덴션을 겟하는 메소드. CreateWallet에서 생성한 seedcode
    @SuppressLint("LongLogTag")
    private void getCredentials(File keydir){
        File[] listfiles = keydir.listFiles();
        Log.e("listfiles in getCredential", String.valueOf(listfiles));
        try {
            mInfoDialog.Get("지갑 로드","Please wait few seconds");
            GetCredentials getCredentials = new GetCredentials();
            getCredentials.registerCallBack(this);
            //입력한 비밀번호를 FromFile 메소드에 넣어주면 backLoadCredential로 크리덴셜이 리턴된다.
            getCredentials.FromFile(listfiles[0].getAbsolutePath(),password_for_getCredentials);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            e.printStackTrace();
        }
    }
    //getCredential에서 리턴값으로 온 credential을 받는 메소드
    @SuppressLint("LongLogTag")
    @Override
    public void backLoadCredential(Credentials credentials) {
        mCredentials = credentials;
        String eckeypair = String.valueOf(mCredentials.getEcKeyPair());
        mInfoDialog.Dismiss();
        Log.e("backLoadCredential 주소",mCredentials.getAddress());
        Log.e("backLoadCredential.eckeypair", eckeypair);
    }



    private void checkPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 마시멜로우 버전과 같거나 이상이라면
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "외부 저장소 사용을 위해 읽기/쓰기 필요", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]
                                {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        2);  //마지막 인자는 체크해야될 권한 갯수

            } else {
                //Toast.makeText(this, "권한 승인되었음", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0) {
            // requestPermission의 두번째 매개변수는 배열이므로 아이템이 여러개 있을 수 있기 때문에 결과를 배열로 받는다.
            // 해당 예시는 요청 퍼미션이 한개 이므로 i=0 만 호출한다.
            if (grantResults[0] == 0) {
                //해당 권한이 승낙된 경우.
            } else {
                //해당 권한이 거절된 경우.

            }
        }
    }


    @Override
    public void backLoadSmartContract(Map<String, String> result) {

    }

    @Override
    public void backSendEthereum(EthSendTransaction result) {

    }

    @Override
    public void backSendToken(TransactionReceipt result) {

    }
}
