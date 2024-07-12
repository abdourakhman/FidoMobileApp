package com.daon.fido.sdk.sample.fragments.voice;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;


import com.daon.fido.sdk.sample.R;
import com.daon.sdk.voiceauthenticator.capture.ParamDefaults;

public class VoiceUI implements ParamDefaults {
    public interface VoiceUIListener {
        void onMicButtonClick();
    }

    public interface VoiceRecorder {
        boolean isRecording();
        int getRecordingAmplitude();
    }

    private final Context context;
    private final VoiceUIListener listener;

    private Handler handler = new Handler(Looper.getMainLooper());

    private View rootView;
    private Button micButton;
    private Button animButton;
    private TextView phraseView;
    private FrameLayout record;
    private TextView step;

    private VoiceRecorder voiceRecorder;

    public VoiceUI(Context context, VoiceUIListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void init(View rootView) {
        if(rootView != null) {
            this.rootView = rootView;
            int animColor;
            micButton = (Button) rootView.findViewById(R.id.micButton);
            if (micButton != null) {
                animColor = ContextCompat.getColor(context, R.color.level_meter_inactive);
                micButton.getBackground().setColorFilter(animColor, PorterDuff.Mode.MULTIPLY);
            }

            if (micButton != null) {
                micButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.onMicButtonClick();
                        }
                    }
                });
            }

            animButton = (Button) rootView.findViewById(R.id.animButton);
            if (animButton != null) {
                animColor = ContextCompat.getColor(context, R.color.level_meter_inactive);
                animButton.getBackground().setColorFilter(animColor, PorterDuff.Mode.MULTIPLY);
            }

            phraseView = (TextView) rootView.findViewById(R.id.phrase);
            record = (FrameLayout) rootView.findViewById(R.id.record);
            step = (TextView) rootView.findViewById(R.id.step);
            record.setVisibility(View.GONE);
        }
    }

    public void resetMicButton() {
        if(rootView==null) {
            return;
        }
        int color = 0;
        if(micButton != null) {
            color = ContextCompat.getColor(context, R.color.level_meter_recording_selected);
            micButton.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            micButton.setText(R.string.voice_record_start);
        }
        if(animButton != null) {
            color = ContextCompat.getColor(context, R.color.level_meter_inactive);
            animButton.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        }
        if(micButton != null)
            micButton.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    }

    public void activateMicButton() {
        if(rootView==null) {
            return;
        }
        int color = 0;
        if(micButton != null) {
            color = ContextCompat.getColor(context, R.color.level_meter_inactive_selected);
            micButton.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            micButton.setText(R.string.voice_record_stop);
        }
        if(micButton != null) {
            color = ContextCompat.getColor(context, R.color.level_meter_recording);
            micButton.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        }
        if(animButton != null) {
            color = ContextCompat.getColor(context, R.color.level_meter_recording_pulse);
            animButton.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        }
    }

    public void disablePhrase() {
        if(rootView==null) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(context != null) {
                    if (phraseView != null) {
                        phraseView.setText(R.string.voice_wait);

                        if(record != null)
                            record.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    public void enablePhrase(final String phrase) {
        if(rootView==null) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(context != null) {
                    if (phraseView != null) {
                        phraseView.setText(phrase);

                        if(record != null)
                            record.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    public void startRecordAnimation(VoiceRecorder voiceRecorder) {
        if(rootView==null) {
            return;
        }
        if(voiceRecorder==null) {
            return;
        }
        this.voiceRecorder = voiceRecorder;
        handler.post(updateVisualizer);
    }

    Runnable updateVisualizer = new Runnable() {
        @Override
        public void run() {
            if(voiceRecorder.isRecording()) {
                int x = voiceRecorder.getRecordingAmplitude();
                if(animButton != null)
                    startAnimation(x);
                // update in 40 milliseconds
                handler.postDelayed(this, ANIM_DURATION);
            }
        }
    };

    private void startAnimation(int x) {
        float growTo = (float)x/2000;
        float scale = (float) 1.2 * x / 32767;
        //growTo = scale + 1;
        growTo = scale * 50;
        if (growTo > 2)
            growTo = 2;
        ObjectAnimator growX = ObjectAnimator.ofFloat(animButton, "scaleX", 1, growTo);
        ObjectAnimator growY = ObjectAnimator.ofFloat(animButton, "scaleY", 1, growTo);
        AnimatorSet growXY = new AnimatorSet();
        growXY.playTogether(growX, growY);
        growXY.setDuration(ANIM_DURATION/2);
        growXY.setInterpolator(new LinearInterpolator());

        ObjectAnimator shrinkX = ObjectAnimator.ofFloat(animButton, "scaleX", growTo, 1);
        ObjectAnimator shrinkY = ObjectAnimator.ofFloat(animButton, "scaleY", growTo, 1);
        final AnimatorSet shrinkXY = new AnimatorSet();
        shrinkXY.playTogether(shrinkX, shrinkY);
        shrinkXY.setDuration(ANIM_DURATION/2);
        shrinkXY.setInterpolator(new LinearInterpolator());
        shrinkXY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animButton.setDrawingCacheEnabled(false);
                animButton.clearAnimation();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                animButton.setDrawingCacheEnabled(true);
            }
        });

        growXY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animButton.setDrawingCacheEnabled(false);
                animButton.clearAnimation();
                shrinkXY.start();

            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                animButton.setDrawingCacheEnabled(true);
            }
        });
        growXY.start();
    }

    public void updateSteps(final int currentPhraseIndex, final int numberOfPhrases) {
        if(rootView==null) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(context != null) {
                    if (step != null) {
                        if (numberOfPhrases > 0) {
                            @SuppressLint("StringFormatMatches") String count = context.getResources().getString(R.string.voice_progress, (currentPhraseIndex), numberOfPhrases);
                            step.setText(count);
                            step.setVisibility(View.VISIBLE);
                        } else {
                            step.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });

    }
}
