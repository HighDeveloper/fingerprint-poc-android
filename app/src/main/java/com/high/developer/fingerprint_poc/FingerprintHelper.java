package com.high.developer.fingerprint_poc;

import android.content.Context;
import android.content.Intent;
import android.os.CancellationSignal;
import android.widget.Toast;


public class FingerprintHelper extends android.hardware.fingerprint.FingerprintManager.AuthenticationCallback {

    private Context mContext;

    public FingerprintHelper(Context mContext) {
        this.mContext = mContext;
    }


    public void startAuth(android.hardware.fingerprint.FingerprintManager manager, android.hardware.fingerprint.FingerprintManager.CryptoObject cryptoObject) {
        CancellationSignal cancellationSignal = new CancellationSignal();
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }


    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        this.showInformationMessage("Error al autenticar:\n" + errString);
    }


    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        this.showInformationMessage("Ayuda del Fingerprint:\n" + helpString);
    }


    @Override
    public void onAuthenticationFailed() {
        this.showInformationMessage("Autenticación fallida.");
    }


    @Override
    public void onAuthenticationSucceeded(android.hardware.fingerprint.FingerprintManager.AuthenticationResult result) {
        this.showInformationMessage("Autenticación exitosa.");
        mContext.startActivity(new Intent(mContext, MainActivity.class));
    }


    private void showInformationMessage(String msg) {

        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
    }
}