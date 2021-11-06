package com.dong.Wallet;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.nhancv.kurentoandroid.R;
import com.nhancv.kurentoandroid.databinding.ActivityWalletInfoBinding;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

import java.io.File;
import java.math.BigInteger;

import info.bcdev.librarysdkew.utils.InfoDialog;
import info.bcdev.librarysdkew.utils.ToastMsg;
import info.bcdev.librarysdkew.wallet.SendingToken;

public class WalletInfo extends AppCompatActivity {
ActivityWalletInfoBinding binding;
    //큐알 코드를 띄울 이미지뷰
    ImageView qr_small, qr_big;
    //큐알 스캐너
    IntentIntegrator qrScan;
    final Context context = this;
    //이더리움 네트워크와 통신할 객체
    private Web3j mWeb3j;
    //지갑 경로
    private File keydir;
    private Credentials mCredentials;
    private InfoDialog mInfoDialog;
    private BigInteger mGasPrice;
    private BigInteger mGasLimit;
    private SendingToken sendingToken;
    private ToastMsg toastMsg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_wallet_info);
        mInfoDialog = new InfoDialog(this);
        //큐알스캐너
        qrScan = new IntentIntegrator(this);
        toastMsg = new ToastMsg();
        keydir = this.getFilesDir();
        Log.e("keydir 은",  keydir.getAbsolutePath());



    }
}
