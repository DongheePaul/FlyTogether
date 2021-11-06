package com.dong.EtheriumToken;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nhancv.kurentoandroid.R;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import info.bcdev.librarysdkew.GetCredentials;
import info.bcdev.librarysdkew.interfaces.callback.CBBip44;
import info.bcdev.librarysdkew.interfaces.callback.CBGetCredential;
import info.bcdev.librarysdkew.interfaces.callback.CBLoadSmartContract;
import info.bcdev.librarysdkew.interfaces.callback.CBSendingEther;
import info.bcdev.librarysdkew.interfaces.callback.CBSendingToken;
import info.bcdev.librarysdkew.smartcontract.LoadSmartContract;
import info.bcdev.librarysdkew.utils.InfoDialog;
import info.bcdev.librarysdkew.utils.ToastMsg;
import info.bcdev.librarysdkew.utils.qr.Generate;
import info.bcdev.librarysdkew.utils.qr.ScanIntegrator;
import info.bcdev.librarysdkew.wallet.Balance;
import info.bcdev.librarysdkew.wallet.SendingEther;
import info.bcdev.librarysdkew.wallet.SendingToken;
import info.bcdev.librarysdkew.wallet.generate.Bip44;
import info.bcdev.librarysdkew.web3j.Initiate;

/**
 *
 * @author Dmitry Markelov
 * Telegram group: https://t.me/joinchat/D62dXAwO6kkm8hjlJTR9VA
 *
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Если есть вопросы, отвечу в телеграме
 * If you have any questions, I will answer the telegram
 *
 *    Russian:
 *    Пример включает следующие функции:
 *       - Получаем адрес кошелька
 *       - Получаем баланс Eth
 *       - Получаем баланс Токена
 *       - Получаем название Токена
 *       - Получаем символ Токена
 *       - Получаем адрес Контракта Токена
 *       - Получаем общее количество выпущеных Токенов
 *
 *
 *   English:
 *   The example includes the following functions:
 *       - Get address wallet
 *       - Get balance Eth
 *       - Get balance Token
 *       - Get Name Token
 *       - Get Symbol Token
 *       - Get contract Token address
 *       - Get supply Token
 *
 */

public class WalletTokenActivity extends AppCompatActivity implements CBGetCredential, CBLoadSmartContract, CBBip44, CBSendingEther, CBSendingToken {

    //이더리움 네트워크 노드 url. config 파일 안에 addressetnode 메소드의 2번째 케이스. infura에서 생성한 롭슨 네트워크 주소.
    private String mNodeUrl = config.addressethnode(2);


    //이건 지갑 비밀번호. 빈칸으로 되어 있다.  빈칸이 아닌 비밀번호로는 지갑 정보를 가져오지 못함.
    private String mPasswordwallet = config.passwordwallet();
    //내가 만들었던 스마트컨트랙트 주소.
    private String mSmartcontract = config.addresssmartcontract(1);

    TextView ethaddress, ethbalance, tokenname, tokensymbol, tokensupply, tokenaddress, tokenbalance, tokensymbolbalance, seedcode;
    TextView tv_gas_limit, tv_gas_price, tv_fee;
    EditText sendtoaddress, sendtokenvalue, sendethervalue;

    ImageView qr_small, qr_big;

    final Context context = this;

    IntentIntegrator qrScan;

    private Web3j mWeb3j;

    private File keydir;

    private Credentials mCredentials;

    private InfoDialog mInfoDialog;

    private BigInteger mGasPrice;

    private BigInteger mGasLimit;

    private SendingEther sendingEther;

    private SendingToken sendingToken;

    private ToastMsg toastMsg;

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallettoken);
        Log.e("노드 url", mNodeUrl);
        mInfoDialog = new InfoDialog(this);

        ethaddress = (TextView) findViewById(R.id.ethaddress); // Your Ether Address
        ethbalance = (TextView) findViewById(R.id.ethbalance); // Your Ether Balance

        tokenname = (TextView) findViewById(R.id.tokenname); // Token Name
        tokensymbol = (TextView) findViewById(R.id.tokensymbol); // Token Symbol
//        tokensupply = (TextView) findViewById(R.id.tokensupply); // Token Supply
  //      tokenaddress = (TextView) findViewById(R.id.tokenaddress); // Token Address
        tokenbalance = (TextView) findViewById(R.id.tokenbalance); // Token Balance
        tokensymbolbalance = (TextView) findViewById(R.id.tokensymbolbalance);
      //  seedcode = (TextView) findViewById(R.id.seedcode);

        sendtoaddress = (EditText) findViewById(R.id.sendtoaddress); // Address for sending ether or token

        sendtokenvalue = (EditText) findViewById(R.id.SendTokenValue); // Ammount token for sending
        sendethervalue = (EditText) findViewById(R.id.SendEthValue); // Ammount ether for sending

        qr_small = (ImageView)findViewById(R.id.qr_small);

        qrScan = new IntentIntegrator(this);

        tv_gas_limit = (TextView) findViewById(R.id.tv_gas_limit);
        tv_gas_price = (TextView) findViewById(R.id.tv_gas_price);
        tv_fee = (TextView) findViewById(R.id.tv_fee);

        final SeekBar sb_gas_limit = (SeekBar) findViewById(R.id.sb_gas_limit);
        sb_gas_limit.setOnSeekBarChangeListener(seekBarChangeListenerGL);
        final SeekBar sb_gas_price = (SeekBar) findViewById(R.id.sb_gas_price);
        sb_gas_price.setOnSeekBarChangeListener(seekBarChangeListenerGP);

        //
        GetFee();
        //
        getWeb3j();

        toastMsg = new ToastMsg();

        //keydir = this.getFilesDir("/keystore/");

        keydir = this.getFilesDir();
        //keydir: /data/user/0/info.bcdev.easytoken/files
        File[] listfiles = keydir.listFiles();
        Log.e("String.valueOf(listfiles)", String.valueOf(listfiles));
        Log.e("String.valueOf(listfiles.length)", String.valueOf(listfiles.length));
        Log.e("String.valueOf(keydir)", String.valueOf(keydir));
        Log.e("String.valueOf(keydirgetAbsolutePath())", String.valueOf(keydir.getAbsolutePath()));

        //listfiles => 지갑을 저장하는 곳인가 봄.
        //비어있다면 지갑을 만들고, 비어있지 않다면 Credentials를 만드네.

        //지갑생성 버튼을 누르면 지갑을 생성한다.  메소드명 : CreateWallet()
        Button bt_create = (Button)findViewById(R.id.bt_createWallet);
        bt_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateWallet();
            }
        });

        //'지갑불러오기' 버튼을 누르면 지갑 정보를 가져온다. 메소드명 : getCradentials
        Button bt_load = (Button)findViewById(R.id.bt_loadWallet);
        bt_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCredentials(keydir);
            }
        });
        /*
        if (listfiles.length == 0 ) {

        } else {

            //CreateWallet();
        }*/

    }
    public void onClick(View view) {
        switch (view.getId()) {

            //이더 전송 버튼. 현재 vivible : gone 상태
            case R.id.SendEther:
                sendEther();
                break;

                //토큰 전송 버튼
            case R.id.SendToken:
                sendToken();
                break;
                //큐알  이미지 생성 버튼.
            case R.id.qr_small:
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.qr_view);
                qr_big = (ImageView) dialog.findViewById(R.id.qr_big);
                qr_big.setImageBitmap(new Generate().Get(getEthAddress(),600,600));
                dialog.show();
                break;
                //큐알 스캐너 만들어내는 버튼
            case R.id.qrScan:
                new ScanIntegrator(this).startScan();
                break;
        }
    }

    /* Create Wallet start
    * 생성된 지갑의 정보는 전역변수 keydir 에서 받을 수 있다.*/
    private void CreateWallet(){
        Bip44 bip44 = new Bip44();
        bip44.registerCallBack(this);
        bip44.execute(mPasswordwallet);
        mInfoDialog.Get("지갑생성하기", "잠시만 기다려주세요");
    }

    //Bip44에서 리턴시키는 값이 여기로 옴.
/*  리턴값 result=>  result.put("seedcode",seedCode);
                     result.put("address", mCredentials.getAddress());
                     result.put("privatekey", mCredentials.getEcKeyPair().getPrivateKey().toString());
                     result.put("publickey", mCredentials.getEcKeyPair().getPublicKey().toString());*/

    //CreateWallet() 메소드가 실행된 후 리턴값을 받는 메소드.
    @Override
    public void backGeneration(Map<String, String> result, Credentials credentials) {
        mCredentials = credentials;
        //result.get("address") ==> 지갑주소.
        setEthAddress(result.get("address"));
        setEthBalance(getEthBalance());
        Log.e("seedcode In Main", result.get("seedcode"));
        //setSeed(result.get(seedcode));
        new SaveWallet(keydir,mCredentials,mPasswordwallet).execute();
        mInfoDialog.Dismiss();
    }

    private void setSeed(String seed){
       // seedcode.setText(seed);
    }
    /* End Create Wallet*/



    /* Get Web3j*/
    //web3j 시작.
    private void getWeb3j(){
        new Initiate(mNodeUrl);
        mWeb3j = Initiate.sWeb3jInstance;
        Log.e("getWeb3j in Main", String.valueOf(mWeb3j));
    }

    /* Get Credentials */
    @SuppressLint("LongLogTag")
    private void getCredentials(File keydir){
        File[] listfiles = keydir.listFiles();

        Log.e("String.valueOf(listfiles)", String.valueOf(listfiles));
        Log.e("listfiles[0].getAbsolutePath()", listfiles[3].getAbsolutePath());
        Log.e("listfiles.toString()", listfiles.toString());
        try {
            mInfoDialog.Get("지갑 정보 가져오는 중","잠시만 기다려주세요");
            GetCredentials getCredentials = new GetCredentials();
            getCredentials.registerCallBack(this);
            //listfiles[0] => seedcode. Bip44.java에서 생성했다.
            getCredentials.FromFile(listfiles[3].getAbsolutePath(),mPasswordwallet);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            e.printStackTrace();
        }
    }
    //getCredential이 끝난 후 실행되는 메소드. 가져온 크리덴셜을 전역변수 mCreadential로 저장한다.
    @Override
    public void backLoadCredential(Credentials credentials) {
        mCredentials = credentials;
        mInfoDialog.Dismiss();
        LoadWallet();
        Log.e("backLoadCredential 주소",mCredentials.getAddress());
    }
    /* End Get Credentials */

    //가져온 지갑 정보를 셋해주는 메소드.
    private void LoadWallet(){
        setEthAddress(getEthAddress());
        Log.e("getethaddress() ", String.valueOf(getEthAddress()));
        setEthBalance(getEthBalance());
        GetTokenInfo();
    }

    /* Get Address Ethereum 이더리움 주소를 가져온다.*/
    private String getEthAddress(){
        return mCredentials.getAddress();
    }

    /* Set Address Ethereum */
    private void setEthAddress(String address){
        ethaddress.setText(address);
        Log.e("지갑주소는", address);
        //qr코드 이미지를 생성하는 코드
        qr_small.setImageBitmap(new Generate().Get(address,200,200));
    }

    //보낼 지갑 주소를 스트링 값으로 리턴해주는 메소드
    private String getToAddress(){
        return sendtoaddress.getText().toString();
    }
    //보낼 지갑 주소를 텍스트뷰에 셋하는 메소드
    private void setToAddress(String toAddress){
        sendtoaddress.setText(toAddress);
    }

    /* Get Balance */
    private String getEthBalance(){
        try {
            return new Balance(mWeb3j,getEthAddress()).getInEther().toString();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /* 보낼 이더리움량을 스트링값으로 리턴해주는 메소드 */
    private String getSendEtherAmmount(){
        return sendethervalue.getText().toString();
    }
    //보낼 토큰량을 스트링값으로 리턴해주는 메소드
    private String getSendTokenAmmount(){
        return sendtokenvalue.getText().toString();
    }
    //이더리움 보유량을 텍스트뷰에 셋해주는 메소드
    private void setEthBalance(String ethBalance){
        ethbalance.setText(ethBalance);
    }
    //가스피를 이미지뷰에 셋해주는 메소드
    public void GetFee(){
        setGasPrice(getGasPrice());
        setGasLimit(getGasLimit());

        BigDecimal fee = BigDecimal.valueOf(mGasPrice.doubleValue()*mGasLimit.doubleValue());
        BigDecimal feeresult = Convert.fromWei(fee.toString(),Convert.Unit.ETHER);
        tv_fee.setText(feeresult.toPlainString() + " ETH");
    }
    //가스피를 스트링값으로 리턴해주는 메소드
    private String getGasPrice(){
        return tv_gas_price.getText().toString();
    }

    private void setGasPrice(String gasPrice){
        int gasfee = Integer.parseInt(gasPrice);
        String gasprice = String.valueOf(gasfee*100000000);
        mGasPrice = Convert.toWei(gasprice,Convert.Unit.GWEI).toBigInteger();
    }

    private String getGasLimit() {
        return tv_gas_limit.getText().toString();
    }

    private void setGasLimit(String gasLimit){
        mGasLimit = BigInteger.valueOf(Long.valueOf(gasLimit));
    }

    /*Get Token Info*/
    private void GetTokenInfo(){
        LoadSmartContract loadSmartContract = new LoadSmartContract(mWeb3j,mCredentials,mSmartcontract,mGasPrice,mGasLimit);
        loadSmartContract.registerCallBack(this);
        loadSmartContract.LoadToken();
    }
    /* Get Token.   GetTokenInfo()의 리턴값이 여기로 오는듯.*/
    @Override
    public void backLoadSmartContract(Map<String,String> result) {
        if(result.get("tokenbalance").length()>5) {
            setTokenBalance(result.get("tokenbalance").substring(0, 3));
            setTokenName(result.get("tokenname"));
            setTokenSymbol(result.get("tokensymbol"));
//            setTokenAddress(result.get("tokenaddress"));
            //          setTokenSupply(result.get("totalsupply"));
        }else {
            Log.e("토큰 길이 5이하", result.get("tokenbalance"));
            setTokenBalance(result.get("tokenbalance"));
            setTokenName(result.get("tokenname"));
            setTokenSymbol(result.get("tokensymbol"));
        }
    }

    private void setTokenBalance(String value){
        tokenbalance.setText(value);
    }

    private void setTokenName(String value){
        tokenname.setText(value);
    }

    private void setTokenSymbol(String value){
        tokensymbol.setText(value);
    }

    /* Sending */
    private void sendEther(){
        sendingEther = new SendingEther(mWeb3j,
                mCredentials,
                getGasPrice(),
                getGasLimit());
        sendingEther.registerCallBack(this);
        sendingEther.Send(getToAddress(),getSendEtherAmmount());
    }
    @Override
    public void backSendEthereum(EthSendTransaction result) {
        toastMsg.Long(this,"토큰전송 완료!");
        LoadWallet();
    }

    private void sendToken(){
        sendingToken = new SendingToken(mWeb3j,  mCredentials, getGasPrice(), getGasLimit());
        sendingToken.registerCallBackToken(this);
        sendingToken.Send(mSmartcontract,getToAddress(),getSendTokenAmmount());
    }
    @Override
    public void backSendToken(TransactionReceipt result) {
        toastMsg.Long(this,result.getTransactionHash());
        Log.e("트랜잭션 해쉬", result.getTransactionHash());
        LoadWallet();
    }
    /* End Sending */


    /* QR Scan */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                toastMsg.Short(this, "Result Not Found");
            } else {
                setToAddress(result.getContents());
                toastMsg.Short(this, result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    /* End Q Scan */

    /* SeekBar Listener */
    private SeekBar.OnSeekBarChangeListener seekBarChangeListenerGL = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            GetGasLimit(String.valueOf(seekBar.getProgress()*1000+42000));
        }
        @Override public void onStartTrackingTouch(SeekBar seekBar) { }
        @Override public void onStopTrackingTouch(SeekBar seekBar) { }
    };
    private SeekBar.OnSeekBarChangeListener seekBarChangeListenerGP = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            GetGasPrice(String.valueOf(seekBar.getProgress()+12));
        }
        @Override public void onStartTrackingTouch(SeekBar seekBar) { }
        @Override public void onStopTrackingTouch(SeekBar seekBar) { }
    };

    public void GetGasLimit(String value) {
        tv_gas_limit.setText(value);
        GetFee();
    }
    public void GetGasPrice(String value) {
        tv_gas_price.setText(value);
        GetFee();
    }


    /* End SeekBar Listener */
}
