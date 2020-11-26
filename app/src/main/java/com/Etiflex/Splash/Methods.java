package com.Etiflex.Splash;

import android.animation.Animator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TableRow;

import androidx.core.content.ContextCompat;

public class Methods {

    public void CambiarColorStatusBar(Activity activity, int color){
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(activity, color));
    }

    public void PlayBeep_Short(){
        new ToneGenerator(AudioManager.STREAM_MUSIC, 100).startTone(ToneGenerator.TONE_CDMA_PIP,100);
    }

    public void PlayBeep_Button(){
        try {
            new ToneGenerator(AudioManager.STREAM_MUSIC, 100).startTone(ToneGenerator.TONE_CDMA_PIP,100);
        }catch (RuntimeException e){}
    }

    public void circularRevealActivity(View ROOT, RelativeLayout ColoredPanel) {

        int cx = ColoredPanel.getWidth() / 2;
        int cy = ColoredPanel.getHeight() / 2;

        float finalRadius = Math.max(ColoredPanel.getWidth(), ColoredPanel.getHeight());
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(ColoredPanel, cx, cy, 0, finalRadius);
        circularReveal.setDuration(1000);
        circularReveal.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) { }
            @Override
            public void onAnimationEnd(Animator animation) {
                ROOT.setVisibility(View.VISIBLE);
                ColoredPanel.setVisibility(View.GONE);
            }
            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}
        });
        ColoredPanel.setVisibility(View.VISIBLE);
        circularReveal.start();
    }

}