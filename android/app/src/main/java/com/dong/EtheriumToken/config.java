package com.dong.EtheriumToken;

public class config {
    String Password;
    public static String addressethnode(int node) {
        switch(node){
            case 1:
                return "http://176.74.13.102:18087";
            case 2:
                return "https://ropsten.infura.io/0xb84760361641b0D98B4C8E823B021f44b9d5d4cF";
            default:
                        return "https://mainnet.infura.io/avyPSzkHujVHtFtf8xwY";
        }
    }

    public static String addresssmartcontract(int contract) {
        switch (contract){
            case 1:
                return "0xf1a36e8Be60973E6FC3199a30207bb07e4D78E2C";
            default :
                return "0x89205A3A3b2A69De6Dbf7f01ED13B2108B2c43e7";
        }
    }

    public static String passwordwallet() {
        return "";
    }


}
