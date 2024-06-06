package com.example.trainingcompanion.extra

import android.content.Context
import android.widget.Toast

fun Context.toast(message:String){
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
}

fun Context.toastLong(message:String){
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
}