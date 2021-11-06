package com.dong.Wallet;

import android.annotation.SuppressLint;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.zxing.integration.android.IntentIntegrator;
import com.nhancv.kurentoandroid.R;
import com.nhancv.kurentoandroid.databinding.ActivityWalletCreateBinding;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Map;

import info.bcdev.librarysdkew.interfaces.callback.CBBip44;
import info.bcdev.librarysdkew.interfaces.callback.CBGetCredential;
import info.bcdev.librarysdkew.interfaces.callback.CBLoadSmartContract;
import info.bcdev.librarysdkew.interfaces.callback.CBSendingEther;
import info.bcdev.librarysdkew.interfaces.callback.CBSendingToken;
import info.bcdev.librarysdkew.utils.InfoDialog;
import info.bcdev.librarysdkew.utils.ToastMsg;
import info.bcdev.librarysdkew.wallet.generate.Bip44;

public class WalletCreate extends AppCompatActivity implements CBGetCredential, CBLoadSmartContract, CBBip44, CBSendingEther, CBSendingToken {
ActivityWalletCreateBinding binding;
String st_WalletPassword;

    private ToastMsg toastMsg;

    private Credentials mCredentials;
    private InfoDialog mInfoDialog;

    private File keydir;

    IntentIntegrator qrScan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_wallet_create);

        toastMsg = new ToastMsg();

        qrScan = new IntentIntegrator(this);

        keydir = this.getFilesDir();

        mInfoDialog = new InfoDialog(this);

        binding.btCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               st_WalletPassword = binding.etPassword.getText().toString();
                CreateWallet();
            }
        });

    }
    private void CreateWallet(){
        Bip44 bip44 = new Bip44();
        bip44.registerCallBack(this);
        bip44.execute(st_WalletPassword);
        mInfoDialog.Get("지갑을 생성합니다", "잠시만 기다려주세요");
        Log.e("지갑생성합니다", "생성합니다");
    }
    @SuppressLint("LongLogTag")
    @Override
    public void backGeneration(Map<String, String> result, Credentials credentials) {
        mCredentials = credentials;
        try {
            String FileWallet = WalletUtils.generateLightNewWalletFile(st_WalletPassword, new File(String.valueOf(keydir)));
            Log.e("FileWallet in WallerCreate.java", FileWallet);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mInfoDialog.Dismiss();

    }

    @Override
    public void backLoadCredential(Credentials credentials) {

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
