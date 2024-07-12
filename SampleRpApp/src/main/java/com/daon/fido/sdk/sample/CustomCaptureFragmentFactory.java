package com.daon.fido.sdk.sample;

import com.daon.fido.sdk.sample.fragments.OOTPFragment;
import com.daon.fido.sdk.sample.fragments.face.AuthenticateFaceFragment;
import com.daon.fido.sdk.sample.fragments.face.RegisterFaceFragment;
import com.daon.fido.sdk.sample.fragments.finger.AuthenticateFingerprintFragment;
import com.daon.fido.sdk.sample.fragments.pattern.AuthenticatePatternFragment;
import com.daon.fido.sdk.sample.fragments.pattern.RegisterPatternFragment;
import com.daon.sdk.authenticator.Authenticator;
import com.daon.sdk.authenticator.DefaultCaptureFragmentFactory;
import com.daon.fido.sdk.sample.fragments.SilentFragment;
import com.daon.fido.sdk.sample.fragments.finger.RegisterFingerprintFragment;
import com.daon.fido.sdk.sample.fragments.passcode.AuthenticatePasscodeFragment;
import com.daon.fido.sdk.sample.fragments.passcode.RegisterPasscodeFragment;
import com.daon.fido.sdk.sample.fragments.passcode.VerifyAndReenrolPasscodeFragment;
import com.daon.fido.sdk.sample.fragments.voice.AuthenticateVoiceFragment;
import com.daon.fido.sdk.sample.fragments.voice.RegisterVoiceFragment;

public class CustomCaptureFragmentFactory extends DefaultCaptureFragmentFactory {

    public Class<?> getRegistrationFragment(Authenticator.Factor factor, Authenticator.Type type) {
        if (factor == Authenticator.Factor.PASSCODE)
            return RegisterPasscodeFragment.class;

        if (factor == Authenticator.Factor.FACE)
            return RegisterFaceFragment.class;              // Passive and Blink Liveness

        if (factor == Authenticator.Factor.VOICE)
            return RegisterVoiceFragment.class;

        if (factor == Authenticator.Factor.SILENT)
            return SilentFragment.class;

        if (factor == Authenticator.Factor.FINGERPRINT)
            return RegisterFingerprintFragment.class;

        if (factor == Authenticator.Factor.OTP)
            return OOTPFragment.class;

        if (factor == Authenticator.Factor.PATTERN)
            return RegisterPatternFragment.class;

        return super.getRegistrationFragment(factor, type);
    }

    public Class<?> getAuthenticationFragment(Authenticator.Factor factor, Authenticator.Type type) {
        if (factor == Authenticator.Factor.PASSCODE)
            return AuthenticatePasscodeFragment.class;

        if (factor == Authenticator.Factor.FACE)
            return AuthenticateFaceFragment.class;              // Passive and Blink Liveness

        if (factor == Authenticator.Factor.VOICE)
            return AuthenticateVoiceFragment.class;

        if (factor == Authenticator.Factor.SILENT)
            return SilentFragment.class;

        if (factor == Authenticator.Factor.FINGERPRINT)
            return AuthenticateFingerprintFragment.class;

        if (factor == Authenticator.Factor.OTP)
            return OOTPFragment.class;

        if (factor == Authenticator.Factor.PATTERN)
            return AuthenticatePatternFragment.class;

        return super.getAuthenticationFragment(factor, type);
    }

    @Override
    public Class<?> getVerifyAndReenrolFragment(Authenticator.Factor factor, Authenticator.Type type) {
        if (factor == Authenticator.Factor.PASSCODE && type== Authenticator.Type.ADOS)
            return VerifyAndReenrolPasscodeFragment.class;

        return super.getVerifyAndReenrolFragment(factor, type);
    }

//    @Override
//    public Class<?> getExtraAuthenticationFragment(Authenticator.Factor factor, Authenticator.Type type, String fragmentId) {
//        if (factor == Authenticator.Factor.FINGERPRINT &&
//                type == Authenticator.Type.STANDARD &&
//                fragmentId.equals(RegisterFingerprintFragment.
//                        CUSTOM_FINGER_CAPTURE_FRAGMENT_ID))
//            return CustomSdkFingerCaptureFragment.class;
//
//        return super.getExtraAuthenticationFragment(factor, type, fragmentId);
//    }


    @Override
    public Class<?> getActivity(Authenticator.Type type) {
        if(type== Authenticator.Type.ADOS) {
            return CustomCaptureActivity.class;
        }

        return super.getActivity(type);
    }
}
