package com.daon.fido.sdk.sample.fragments.pattern;

import com.daon.fido.sdk.sample.R;

public class RegisterPatternFragment extends PatternFragment {

    @Override
    protected int getLayoutResource() {
        return R.layout.daon_register_pattern;
    }

    @Override
    protected PatternCollect.Mode getPatternCollectMode() {
        return PatternCollect.Mode.ENROLL;
    }


    @Override
    public void store(int[] pattern) {
        if(getController() != null) {
            getController().registerPattern(pattern, new DefaultCaptureCompleteListener());
        }
    }

    @Override
    public void validate(int[] pattern, PatternValidListener listener) {
        throw new RuntimeException("The pattern cannot be validated in the case of registration");
    }

    @Override
    public void onPatternCollectResult(PatternCollect.PatternCollectResult result) {
        if (result.getMode() == PatternCollect.Mode.ENROLL) {
            switch (result.getStatus()) {
                case FIRST_ENROLLMENT_COMPLETE:
                    showMessage(R.string.pattern_reentry, false);
                    break;
                case INVALID_CONFIRMATION_ENROLLMENT:
                    showMessage(R.string.pattern_reenetry_invalid, false);
                    break;
                case INVALID_ENROLMENT_MIN_SIZE:
                    showMessage(String.format(getResources().getString(R.string.pattern_enroll_less_than_min),
                            Integer.toString(getController().getMinNumberOfTouchPoints())) , false);
                    break;
                case INVALID_ENROLMENT_MAX_SIZE:
                    showMessage(String.format(getResources().getString(R.string.pattern_enroll_more_than_max),
                            Integer.toString(getController().getMaxNumberOfTouchPoints())) , false);
                    break;
                case INVALID_ENROLMENT_WEAK_PATTERN:
                    showMessage(R.string.pattern_enroll_weak_pattern, false);
                    break;
            }
        }
    }

    @Override
    protected int getCaptureSuccessMessageId() {
        return R.string.pattern_enroll_complete;
    }

    @Override
    protected int getCaptureFailedMessageId() {
        return R.string.pattern_enroll_failed;
    }
}
