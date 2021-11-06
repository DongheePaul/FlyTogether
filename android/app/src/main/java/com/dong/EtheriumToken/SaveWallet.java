package com.dong.EtheriumToken;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.io.IOException;

//지갑 저장하는 클래스.
public class SaveWallet extends AsyncTask<Void,Void,Void>{
    private String mPasswordwallet;
    private File mKeystoredir;
    private Credentials mCredentials;

    public SaveWallet(File keydir, Credentials credentials, String passwordwallet){
        mKeystoredir = keydir;
        mCredentials = credentials;
        mPasswordwallet = passwordwallet;

    }

    @SuppressLint("LongLogTag")
    @Override
    protected Void doInBackground(Void... voids) {
        Log.e("keydir in Savewallet.java", String.valueOf(mKeystoredir));
        String FileWallet = null;
        try {
            FileWallet = WalletUtils.generateWalletFile(mPasswordwallet,mCredentials.getEcKeyPair(), mKeystoredir,false);
            Log.e("Filewallet in SaveWallet.java ", FileWallet);
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
