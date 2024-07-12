package com.daon.fido.sdk.sample;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.daon.fido.client.sdk.ErrorInfo;
import com.daon.fido.client.sdk.Fido;
import com.daon.fido.client.sdk.IXUAFCommService;
import com.daon.fido.client.sdk.IXUAFCommServiceListener;
import com.daon.fido.client.sdk.ServerCommResult;
import com.daon.fido.client.sdk.model.Operation;
import com.daon.fido.client.sdk.model.UafProtocolMessageBase;
import com.daon.fido.client.sdk.model.UafRequestWithPolicy;
import com.daon.fido.client.sdk.uaf.UafMessageUtils;
import com.daon.fido.client.sdk.util.TaskExecutor;
import com.daon.fido.sdk.sample.exception.CommunicationsException;
import com.daon.fido.sdk.sample.exception.ServerError;
import com.daon.fido.sdk.sample.model.AuthenticatorInfo;
import com.daon.fido.sdk.sample.model.CreateAccount;
import com.daon.fido.sdk.sample.model.CreateAccountResponse;
import com.daon.fido.sdk.sample.model.CreateAuthRequestResponse;
import com.daon.fido.sdk.sample.model.CreateAuthenticator;
import com.daon.fido.sdk.sample.model.CreateAuthenticatorResponse;
import com.daon.fido.sdk.sample.model.CreateRegRequestResponse;
import com.daon.fido.sdk.sample.model.CreateSession;
import com.daon.fido.sdk.sample.model.CreateSessionResponse;
import com.daon.fido.sdk.sample.model.CreateTransactionAuthRequest;
import com.daon.fido.sdk.sample.model.DeleteAccountResponse;
import com.daon.fido.sdk.sample.model.GetAuthenticatorResponse;
import com.daon.fido.sdk.sample.model.GetPolicyResponse;
import com.daon.fido.sdk.sample.model.ListAuthenticatorsResponse;
import com.daon.fido.sdk.sample.model.SubmitFailedAttemptRequest;
import com.daon.fido.sdk.sample.model.SubmitFailedAttemptResponse;
import com.daon.fido.sdk.sample.model.SubmitOfflineOTPRequest;
import com.daon.fido.sdk.sample.model.ValidateTransactionAuth;
import com.daon.fido.sdk.sample.model.ValidateTransactionAuthResponse;
import com.daon.sdk.authenticator.VerificationAttemptParameters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.ref.SoftReference;

public class RPSAService implements IXUAFCommService {
    //Resources
    private static final String SERVER_RESOURCE_CREATE_ACCOUNT = "accounts";
    private static final String SERVER_RESOURCE_REG_REQUESTS = "regRequests";
    private static final String SERVER_RESOURCE_AUTHENTICATORS = "authenticators";
    private static final String SERVER_RESOURCE_AUTH_REQUESTS = "authRequests";
    private static final String SERVER_RESOURCE_TRANSACTION_AUTH_REQUESTS = "transactionAuthRequests";
    private static final String SERVER_RESOURCE_TRANSACTION_AUTH_VALIDATION = "transactionAuthValidation";
    private static final String SERVER_RESOURCE_SESSIONS = "sessions";
    private static final String SERVER_RESOURCE_LIST_AUTHENTICATORS = "listAuthenticators";
    private static final String SERVER_RESOURCE_POLICIES = "policies";
    private static final String SERVER_RESOURCE_SUBMIT_FAILED_ATTEMPTS = "failedTransactionData";
    private static final String SERVER_RESOURCE_SUBMIT_OFFLINE_OTP = "submitOfflineOTP";
    private static String KEY_APP_ID = "fidoAppId";

    private CreateRegRequestTask mCreateRegReqTask = null;
    private CreateAuthenticatorTask mCreateAuthTask = null;
    private CreateTransactionAuthRequestTask mCreateTransactionAuthRequestTask = null;
    private CreateAuthRequestTask mCreateAuthRequestTask = null;
    private UserLoginWithFIDOTask mUserLoginWithFIDOTask = null;
    private GetPolicyTask mGetPolicyTask = null;

    private Context mContext;
    private GsonBuilder builder;
    private static Gson gson;
    private String mRegRequestId;
    private String mAuthRequestId;
    private CreateSessionResponse mCreateSessionResponse;
    private String sessionId;

    private SoftReference<IXUAFCommServiceListener> listener;
    private IXUAFCommServiceListener submitFailedAttemptListener;
    private HTTP http;

    public enum State {
        login,
        transaction,
        push,
        none,
        logout
    }
    private State mState;

    private static RPSAService instance = null;
    private RPSAService(Context context) {
        mContext = context;

        builder = new GsonBuilder();

        http = new HTTP(context);
    }

    public static RPSAService getInstance(Context context) {
        Log.d(CoreApplication.TAG, "RPSAService getInstance");
        instance = new RPSAService(context);
        return instance;
    }


    protected Context getContext() {
        return this.mContext;
    }

    public ServerCommResult serviceCreateAccount(CreateAccount account) {
        ServerCommResult response = new ServerCommResult();
        try {
            CreateAccountResponse createAccountResponse = http.post(SERVER_RESOURCE_CREATE_ACCOUNT, account, CreateAccountResponse.class);
            sessionId = createAccountResponse.getSessionId();
            response.setResponse(createAccountResponse.getSessionId());
        }catch (ServerError e) {
            ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
            response.setErrorInfo(errorInfo);

        }catch (CommunicationsException e) {
            ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
            response.setErrorInfo(errorInfo);
        }
        return response;
    }


    @Override
    public void serviceRequestRegistrationWithUsername(String username, IXUAFCommServiceListener commServiceListener) {
        listener = new SoftReference<>(commServiceListener);
        mCreateRegReqTask = new CreateRegRequestTask(username);
        mCreateRegReqTask.execute();
    }

    public class CreateRegRequestTask extends TaskExecutor<ServerCommResult> {
        private String username;
        CreateRegRequestTask(String username) {
            this.username = username;
        }

        @Override
        protected ServerCommResult doInBackground() {
            ServerCommResult response = new ServerCommResult();
            try {
                CreateRegRequestResponse regRequestResponse = http.get(SERVER_RESOURCE_REG_REQUESTS, CreateRegRequestResponse.class);
                mRegRequestId = regRequestResponse.getRegistrationRequestId();
                saveAppID(regRequestResponse.getFidoRegistrationRequest());
                response.setResponse(regRequestResponse.getFidoRegistrationRequest());
            }catch (ServerError e) {
                ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
                response.setErrorInfo(errorInfo);
            } catch(CommunicationsException e) {
                ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
                response.setErrorInfo(errorInfo);
            }
            return response;
        }

        @Override
        protected void onPostExecute(ServerCommResult response) {
            listener.get().onComplete(response);
        }
    }


    @Override
    public void serviceRegister(String registrationResponse, IXUAFCommServiceListener commServiceListener) {
        listener = new SoftReference<>(commServiceListener);
        register(registrationResponse);
    }

    private void register(String registrationResponse) {
        if(mState == State.logout) {
            return;
        }
        mCreateAuthTask = new CreateAuthenticatorTask(registrationResponse);
        mCreateAuthTask.execute();
    }

    public class CreateAuthenticatorTask extends TaskExecutor<ServerCommResult> {
        private final CreateAuthenticator createAuthenticator;

        CreateAuthenticatorTask(String response) {
            Log.d("EASY_FIDO", "Fido CreateAuthenticatorTask");
            createAuthenticator = new CreateAuthenticator();
            createAuthenticator.setRegistrationChallengeId(mRegRequestId);
            createAuthenticator.setFidoReqistrationResponse(response);
        }

        @Override
        protected ServerCommResult doInBackground() {
            ServerCommResult response = new ServerCommResult();
            try {
                CreateAuthenticatorResponse authenticatorResponse = http.post(SERVER_RESOURCE_AUTHENTICATORS, createAuthenticator, CreateAuthenticatorResponse.class);
                response.setResponse(authenticatorResponse.getFidoRegistrationConfirmation());
                response.setResponseCode(authenticatorResponse.getFidoResponseCode().shortValue());
                response.setResponseMessage(authenticatorResponse.getFidoResponseMsg());
            }catch (ServerError e) {
                ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
                response.setErrorInfo(errorInfo);
            } catch(CommunicationsException e) {
                ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
                response.setErrorInfo(errorInfo);
            }
            return response;
        }

        @Override
        protected void onPostExecute(ServerCommResult response) {
            listener.get().onComplete(response);
        }
    }


    public class CreateAuthRequestTask extends TaskExecutor<ServerCommResult> {
        Bundle authParams;

        CreateAuthRequestTask(Bundle params) {
            authParams = params;
        }

        @Override
        protected ServerCommResult doInBackground() {
            ServerCommResult response = new ServerCommResult();
            CreateAuthRequestResponse authRequestResponse;

            String id = authParams != null ? authParams.getString(Fido.IXUAF_SERVICE_PARAM_ID) : null;
            String userName = authParams != null ? authParams.getString(Fido.IXUAF_SERVICE_PARAM_USERNAME): null;

            try {
                if(userName != null) {
                    mState = State.login;
                    String request = SERVER_RESOURCE_AUTH_REQUESTS + "?userId=" + userName;
                    authRequestResponse = http.get(request, CreateAuthRequestResponse.class);
                }else {
                    if(id == null) {
                        mState = State.login;
                        authRequestResponse = http.get(SERVER_RESOURCE_AUTH_REQUESTS, CreateAuthRequestResponse.class);
                    }else {
                        mState = State.push;
                        authRequestResponse = http.get(SERVER_RESOURCE_AUTH_REQUESTS, id, CreateAuthRequestResponse.class);
                    }
                }
                mAuthRequestId = authRequestResponse.getAuthenticationRequestId();
                saveAppID(authRequestResponse.getFidoAuthenticationRequest());
                response.setResponse(authRequestResponse.getFidoAuthenticationRequest());
            }catch (ServerError e) {
                ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
                response.setErrorInfo(errorInfo);
            }catch (CommunicationsException e) {
                ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
                response.setErrorInfo(errorInfo);
            }
            return response;
        }

        @Override
        protected void onPostExecute(ServerCommResult serverCommResult) {
            listener.get().onComplete(serverCommResult);
        }
    }

    public class CreateTransactionAuthRequestTask extends TaskExecutor<ServerCommResult> {
        private CreateTransactionAuthRequest createTransactionAuthRequest;
        CreateTransactionAuthRequestTask() {
            createTransactionAuthRequest = new CreateTransactionAuthRequest();

            if (UserPreferences.instance().getBoolean(TransactionActivity.PREF_TRANSACTION_CONFIRAMATION, false)) {
                String transactionType = null;
                String transactionContent = null;
                if (UserPreferences.instance().getBoolean(SettingsActivity.PREF_TEXT_TX, false)) {
                    transactionType = "text/plain";
                    transactionContent = (String) mContext.getText(R.string.transaction_text_content);
                } else {
                    transactionType = "image/png";
                    transactionContent = (String) mContext.getText(R.string.transaction_image_content);
                }
                createTransactionAuthRequest.setTransactionContentType(transactionType);
                createTransactionAuthRequest.setTransactionContent(transactionContent);
            }
            if (UserPreferences.instance().getBoolean(TransactionActivity.PREF_GENERATE_OTP, false)) {
                createTransactionAuthRequest.setOtpEnabled(true);
            }

            createTransactionAuthRequest.setStepUpAuth(true);
        }

        @Override
        protected ServerCommResult doInBackground() {

            ServerCommResult response = new ServerCommResult();
            try {
                CreateAuthRequestResponse authRequestResponse = http.post(SERVER_RESOURCE_TRANSACTION_AUTH_REQUESTS, createTransactionAuthRequest, CreateAuthRequestResponse.class);
                mAuthRequestId = authRequestResponse.getAuthenticationRequestId();
                response.setResponse(authRequestResponse.getFidoAuthenticationRequest());
            }catch (ServerError e) {
                ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
                response.setErrorInfo(errorInfo);
            }catch (CommunicationsException e) {
                ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
                response.setErrorInfo(errorInfo);
            }
            return response;
        }

        @Override
        protected void onPostExecute(ServerCommResult serverCommResult) {
            listener.get().onComplete(serverCommResult);
        }
    }

    @Override
    public void serviceRequestAuthenticationWithParams(Bundle params, IXUAFCommServiceListener commServiceListener) {
        listener = new SoftReference<>(commServiceListener);
        String transactionId = params != null ? params.getString(Fido.IXUAF_SERVICE_PARAM_ID) : null;
        if(transactionId != null) {
            mCreateAuthRequestTask = new CreateAuthRequestTask(params);
            mCreateAuthRequestTask.execute();
        }else {
            if (sessionId != null) {
                mState = State.transaction;
                mCreateTransactionAuthRequestTask = new CreateTransactionAuthRequestTask();
                mCreateTransactionAuthRequestTask.execute();
            } else {
                mCreateAuthRequestTask = new CreateAuthRequestTask(params);
                mCreateAuthRequestTask.execute();
            }
        }
    }

    @Override
    public void serviceAuthenticate(String authenticationRequest, String authenticationResponse, String username, IXUAFCommServiceListener commServiceListener) {
        listener = new SoftReference<>(commServiceListener);
        authenticate(authenticationRequest, authenticationResponse, username);
    }

    @Override
    public void serviceUpdate(String authenticationResponse, String username, IXUAFCommServiceListener commServiceListener) {
        listener = new SoftReference<>(commServiceListener);
        UafProtocolMessageBase[] uafRequests = UafMessageUtils.validateUafMessage(getContext(), authenticationResponse, UafMessageUtils.OpDirection.Response, null);
        if(uafRequests[0].header.op == Operation.Reg) {
            register(authenticationResponse);
        }else {
            authenticate(null, authenticationResponse, username);
        }

    }

    private void authenticate(String authenticationRequest, String authenticationResponse, String username) {
        if(mState == State.logout) {
            return;
        }
        if(mState == State.login || mState == State.push) {
            mUserLoginWithFIDOTask = new UserLoginWithFIDOTask(authenticationResponse);
            mUserLoginWithFIDOTask.execute();
        }else {
            ValidateTransactionAuthTask validateTransactionAuthTask = new ValidateTransactionAuthTask(authenticationRequest, authenticationResponse, username);
            validateTransactionAuthTask.execute();
        }
    }

    public ServerCommResult submitOfflineOTP(String emailAddress, String otp, String transactionData) {
        SubmitOfflineOTPRequest submitOfflineOTPRequest = getSubmitOfflineOTPRequest(emailAddress, otp, transactionData);
        ServerCommResult response = new ServerCommResult();
        CreateAuthRequestResponse submitOfflineOTPResponse;
        try {
            submitOfflineOTPResponse = http.post(SERVER_RESOURCE_SUBMIT_OFFLINE_OTP, submitOfflineOTPRequest, CreateAuthRequestResponse.class);
            response.setResponse(submitOfflineOTPResponse.getFidoAuthenticationRequest());
        }catch (ServerError e) {
            ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
            response.setErrorInfo(errorInfo);
        }catch (CommunicationsException e) {
            ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
            response.setErrorInfo(errorInfo);
        }
        return response;
    }

    private SubmitOfflineOTPRequest getSubmitOfflineOTPRequest(@NonNull String emailAddress,
                                                               @NonNull String otp,
                                                               @Nullable String transactionData) {
        SubmitOfflineOTPRequest submitOfflineOTPRequest = new SubmitOfflineOTPRequest();
        submitOfflineOTPRequest.setEmailAddress(emailAddress);
        submitOfflineOTPRequest.setSecureTransactionContent(transactionData);
        submitOfflineOTPRequest.setSubmittedAuthenticationCode(otp);
        return submitOfflineOTPRequest;
    }



    public ServerCommResult createSessionWithEmail(CreateSession createSession) {
        ServerCommResult response = new ServerCommResult();
        try {
            mCreateSessionResponse = http.post(SERVER_RESOURCE_SESSIONS, createSession, CreateSessionResponse.class);
            sessionId = mCreateSessionResponse.getSessionId();
        }catch (ServerError e) {
            ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
            response.setErrorInfo(errorInfo);
        }catch (CommunicationsException e) {
            ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
            response.setErrorInfo(errorInfo);
        }
        return response;
    }

    public class UserLoginWithFIDOTask extends TaskExecutor<ServerCommResult> {
        private CreateSession createSession;

        UserLoginWithFIDOTask(String fidoAuthResponse) {
            Log.d("EASY_FIDO", "Fido UserLoginWithFIDOTask");
            createSession = new CreateSession();
            createSession.setFidoAuthenticationResponse(fidoAuthResponse);
            createSession.setAuthenticationRequestId(mAuthRequestId);
        }

        @Override
        protected ServerCommResult doInBackground() {
            ServerCommResult response = new ServerCommResult();
            try {
                mCreateSessionResponse = http.post(SERVER_RESOURCE_SESSIONS, createSession, CreateSessionResponse.class);
                sessionId = mCreateSessionResponse.getSessionId();
                response.setResponse(mCreateSessionResponse.getFidoAuthenticationResponse());
                response.setResponseCode(mCreateSessionResponse.getFidoResponseCode().shortValue());
                response.setResponseMessage(mCreateSessionResponse.getFidoResponseMsg());
            }catch (ServerError e) {
                ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
                response.setErrorInfo(errorInfo);
            }catch (CommunicationsException e) {
                ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
                response.setErrorInfo(errorInfo);
            }
            return response;
        }

        @Override
        protected void onPostExecute(ServerCommResult serverCommResult) {
            listener.get().onComplete(serverCommResult);
        }
    }


    public class ValidateTransactionAuthTask extends TaskExecutor<ServerCommResult> {
        private ValidateTransactionAuth validateTransactionAuth;

        ValidateTransactionAuthTask(String authRequest, String authResponse, String username) {
            validateTransactionAuth = new ValidateTransactionAuth();
            validateTransactionAuth.setFidoAuthenticationResponse(authResponse);
            if(authRequest != null) {
                validateTransactionAuth.setFidoAuthenticationRequest(authRequest);
                validateTransactionAuth.setEmail(username);
            }else {
                validateTransactionAuth.setAuthenticationRequestId(mAuthRequestId);
            }
        }

        @Override
        protected ServerCommResult doInBackground() {
            ServerCommResult response = new ServerCommResult();
            try {
                ValidateTransactionAuthResponse validateTransactionAuthResponse =  http.post(SERVER_RESOURCE_TRANSACTION_AUTH_VALIDATION,
                        validateTransactionAuth, ValidateTransactionAuthResponse.class);
                response.setResponse(validateTransactionAuthResponse.getFidoAuthenticationResponse());
                response.setResponseCode(validateTransactionAuthResponse.getFidoResponseCode().shortValue());
                response.setResponseMessage(validateTransactionAuthResponse.getFidoResponseMsg());
            }catch (ServerError e) {
                ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
                response.setErrorInfo(errorInfo);
            }catch (CommunicationsException e) {
                ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
                response.setErrorInfo(errorInfo);
            }
            return response;
        }

        @Override
        protected void onPostExecute(ServerCommResult serverCommResult) {
            listener.get().onComplete(serverCommResult);
        }
    }

    public CreateSessionResponse getCreateSessionResponse() {
        return mCreateSessionResponse;
    }


    //This is an application specific call to the RPSAServer - not added in IXUFCommService
    public ListAuthenticatorsResponse serviceRequestAuthInfoList() {
        return TaskExecutor.submitAndWait(() -> http.get(SERVER_RESOURCE_LIST_AUTHENTICATORS, ListAuthenticatorsResponse.class));
    }

    @Override
    public void serviceRequestRegistrationPolicy(IXUAFCommServiceListener commServiceListener) {
        listener = new SoftReference<>(commServiceListener);

        mGetPolicyTask = new GetPolicyTask();
        mGetPolicyTask.execute();
    }

    public class GetPolicyTask extends TaskExecutor<ServerCommResult> {
        GetPolicyTask() {

        }

        @Override
        protected ServerCommResult doInBackground() {
            ServerCommResult response = new ServerCommResult();
            try {
                GetPolicyResponse policyResponse = http.get(SERVER_RESOURCE_POLICIES, "reg", GetPolicyResponse.class);
                response.setResponse(policyResponse.getPolicyInfo().getPolicy());
            }catch (ServerError e) {
                ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
                response.setErrorInfo(errorInfo);
            }catch (CommunicationsException e) {
                ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
                response.setErrorInfo(errorInfo);
            }
            return response;
        }

        @Override
        protected void onPostExecute(ServerCommResult serverCommResult) {
            listener.get().onComplete(serverCommResult);
        }
    }

    @Override
    public void serviceSubmitFailedAuthData(Bundle params, IXUAFCommServiceListener commServiceListener) {
        submitFailedAttemptListener = commServiceListener;
        SubmitFailedAttemptTask submitFailedAttemptTask = new SubmitFailedAttemptTask(params);
        Log.d("DAON", "serviceSubmitFailedAuthData params :" + params.toString());
        submitFailedAttemptTask.execute();
    }


    public class SubmitFailedAttemptTask extends TaskExecutor<ServerCommResult> {
        private SubmitFailedAttemptRequest submitFailedAttemptRequest;
        SubmitFailedAttemptTask(Bundle params) {
            submitFailedAttemptRequest = new SubmitFailedAttemptRequest();
            submitFailedAttemptRequest.setEmailAddress(params.getString(VerificationAttemptParameters.PARAM_USER_ACCOUNT, null));
            submitFailedAttemptRequest.setAttempt(Integer.toString(params.getInt(VerificationAttemptParameters.PARAM_ATTEMPT, Integer.MIN_VALUE)));
            submitFailedAttemptRequest.setAttemptsRemaining(Integer.toString(params.getInt(VerificationAttemptParameters.PARAM_ATTEMPTS_REMAINING, Integer.MIN_VALUE)));
            submitFailedAttemptRequest.setGlobalAttempt(Integer.toString(params.getInt(VerificationAttemptParameters.PARAM_GLOBAL_ATTEMPT, Integer.MIN_VALUE)));
            submitFailedAttemptRequest.setLockStatus(params.getString(VerificationAttemptParameters.PARAM_LOCK_STATUS, null));
            submitFailedAttemptRequest.setErrorCode(Integer.toString(params.getInt(VerificationAttemptParameters.PARAM_ERROR_CODE, 0)));
            submitFailedAttemptRequest.setScore(Double.toString(params.getDouble(VerificationAttemptParameters.PARAM_SCORE, 0)));
            submitFailedAttemptRequest.setUserAuthKeyId(params.getString(VerificationAttemptParameters.PARAM_USER_AUTH_KEY_ID, null));
            submitFailedAttemptRequest.setAuthenticationRequestId(mAuthRequestId);
        }

        @Override
        protected ServerCommResult doInBackground() {
            ServerCommResult response = new ServerCommResult();
            try{
                SubmitFailedAttemptResponse submitFailedAttemptResponse = http.post(SERVER_RESOURCE_SUBMIT_FAILED_ATTEMPTS, submitFailedAttemptRequest, SubmitFailedAttemptResponse.class);
                if(submitFailedAttemptResponse.getFidoAuthenticationResponse() != null) {
                    Log.d("fsa-1247", "SubmitFailedAttempt response - fidoAuthenticationResponse");
                    response.setResponse(submitFailedAttemptResponse.getFidoAuthenticationResponse());
                }else {
                    Log.d("fsa-1247", "SubmitFailedAttempt response - fidoAuthenticationRequest");
                    response.setResponse(submitFailedAttemptResponse.getFidoAuthenticationRequest());
                }
            }catch (ServerError e) {
                ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
                response.setErrorInfo(errorInfo);
            }catch (CommunicationsException e) {
                ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
                response.setErrorInfo(errorInfo);
            }
            return response;
        }

        @Override
        protected void onPostExecute(ServerCommResult serverCommResult) {
            submitFailedAttemptListener.onComplete(serverCommResult);
        }
    }

    @Override
    public void serviceRequestDeregistration(String authenticatorId, IXUAFCommServiceListener commServiceListener) {
        listener = new SoftReference<>(commServiceListener);
        DeregisterAuthenticatorTask deregisterAuthenticatorTask = new DeregisterAuthenticatorTask(authenticatorId);
        deregisterAuthenticatorTask.execute();
    }

    public class DeregisterAuthenticatorTask extends TaskExecutor<ServerCommResult> {
        private String authenticatorId;
        DeregisterAuthenticatorTask(String authId) {
            authenticatorId = authId;
        }

        @Override
        protected ServerCommResult doInBackground() {
            ServerCommResult response = new ServerCommResult();
            Boolean found = false;
            String deregRequest = null;
            try {
                String deviceId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
                ListAuthenticatorsResponse listAuthenticatorsResponse = http.get(SERVER_RESOURCE_LIST_AUTHENTICATORS, ListAuthenticatorsResponse.class);
                AuthenticatorInfo[] authenticatorInfos = listAuthenticatorsResponse.getAuthenticatorInfoList();
                for (AuthenticatorInfo info : authenticatorInfos) {
                    if (info.getId().equals(authenticatorId)) {
                        if(info.getDeviceCorrelationId() == null) {
                            found = true;
                        }else {
                            if(info.getDeviceCorrelationId().equals(deviceId) || info.getDeviceCorrelationId().isEmpty()) {
                                found = true;
                            }
                        }
                        if(found = true) {
                            if (info.getStatus().equals("ACTIVE")) {
                                deregRequest = http.deleteResource(SERVER_RESOURCE_AUTHENTICATORS, authenticatorId, true);
                            } else {
                                deregRequest = http.get(SERVER_RESOURCE_AUTHENTICATORS, info.getId(), GetAuthenticatorResponse.class).getAuthenticatorInfo().getFidoDeregistrationRequest();
                            }
                        }

                    }
                }
                if (!found) {
                    ErrorInfo errorInfo = new ErrorInfo(99, "Authenticator not found on server");
                    response.setErrorInfo(errorInfo);
                }else {
                    response.setResponse(deregRequest);
                }
            }catch (ServerError e) {
                ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
                response.setErrorInfo(errorInfo);
            }catch (CommunicationsException e) {
                ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
                response.setErrorInfo(errorInfo);
            }
            return response;
        }

        @Override
        protected void onPostExecute(ServerCommResult serverCommResult) {
            listener.get().onComplete(serverCommResult);
        }
    }

    public DeleteAccountResponse deleteAccount(String sessionId) {
        return TaskExecutor.submitAndWait(() -> http.deleteResource(SERVER_RESOURCE_CREATE_ACCOUNT, sessionId, DeleteAccountResponse.class));
    }

    public void deleteSession(String id) {
        sessionId = null;
        http.deleteResource(SERVER_RESOURCE_SESSIONS, id, false);
//        BackgroundTaskExecutor.submit(() -> {
//            return http.deleteResource(SERVER_RESOURCE_SESSIONS, id, false);
//        });
    }

    //Making it public so application can use it for deregistering - "deregisterWithMessage"
    public GetAuthenticatorResponse getAuthenticator(String id) {
        return TaskExecutor.submitAndWait(() -> http.get(SERVER_RESOURCE_AUTHENTICATORS, id, GetAuthenticatorResponse.class));
    }

    private static Gson getGson() {
        if(gson==null) {
            gson = new Gson();
        }
        return gson;
    }

    private void saveAppID(String regRequst) {
        UafRequestWithPolicy[] msg = getGson().fromJson(regRequst, UafRequestWithPolicy[].class);
        msg[0].policy.disallowed = null;
        String appId = msg[0].header.appID;

        UserPreferences.instance().putString(KEY_APP_ID, appId);
    }

    public String getAppID() {
        return UserPreferences.instance().getString(KEY_APP_ID, null);
    }

    public void resetState() {
        mState = State.none;
    }

    public State getmState() {
        return mState;
    }

    public void setmState(State mState) {
        this.mState = mState;
    }

    public void resetSessionId() {
        sessionId = null;
    }

    public void resetAuthRequestId() { mAuthRequestId = null ;}
}

