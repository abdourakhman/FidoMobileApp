package com.daon.fido.sdk.sample.fragments.pattern;

public interface IPatternManager {
    interface PatternValidListener {
        void onPatternValid(boolean valid);
    }

    void store(int[] pattern);
    void validate(int[] pattern, PatternValidListener listener);
}
