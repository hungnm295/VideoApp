package com.example.videoapp.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;


public class CheckNetworkBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        int[] networkType = {ConnectivityManager.TYPE_MOBILE, ConnectivityManager.TYPE_WIFI};
        if(isNetworkConnected(context,networkType) ==true){
            return;
        }else{
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setMessage("Please check your internet connection and try again")
                    .setPositiveButton("Wifi", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();
            alertDialog.show()
                    .setCancelable(false);
        }
    }
    public boolean isNetworkConnected(Context context, int[] networkTypes){
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            for (int networkType: networkTypes) {
                NetworkInfo networkInfo = cm.getNetworkInfo(networkType);
                if(networkInfo!=null && networkInfo.getState() == NetworkInfo.State.CONNECTED){
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }
    interface ConnectivityReceiverListener{
        void onNetworkConnectionChanged(boolean isConnected);
    }
}
