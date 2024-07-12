package com.daon.fido.sdk.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.daon.fido.sdk.sample.barcode.BarcodeCaptureActivity;

import com.google.android.gms.vision.barcode.Barcode;

public class QRCodeActivity extends BaseActivity {
    public static final int REQUEST_SCAN = 1;
    private static final String ACTION_SPONSOR = "sponsor";
    private static final String CODE = "sc";
    public static final String SPONSORSHIP_CODE = "SponsorshipCode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        Button noQrcode = (Button)findViewById(R.id.login_without_qrcode);
        noQrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCreateAccountActivity(null);
            }
        });

        final Button scanQrcode = (Button)findViewById(R.id.login_with_qrcode);
        scanQrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanQRCode();
            }
        });
    }

    private void startCreateAccountActivity(String sponsorshipCode) {

        try {
            Intent newIntent = new Intent( this, CreateAccountActivity.class);
            if(sponsorshipCode != null)
                newIntent.putExtra(SPONSORSHIP_CODE, sponsorshipCode);
            startActivity(newIntent);
        } catch (Throwable ex) {
            showMessage(ex.getMessage(), false);
        }
    }

    private void scanQRCode() {
        Intent intent = new Intent(QRCodeActivity.this, BarcodeCaptureActivity.class);
        intent.putExtra(BarcodeCaptureActivity.AUTO_FOCUS, true);
        intent.putExtra(BarcodeCaptureActivity.USE_FLASH, false);
        mStartForResult.launch(intent);
    }

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK) {
                    if(result.getData() != null) {
                        Barcode barcode = result.getData().getParcelableExtra(BarcodeCaptureActivity.BARCODE_OBJECT);
                        String scanRes = barcode.displayValue;
                        startCreateAccountActivity(getSponsorshipCode(scanRes));
                    }
                }
            });

    private String getSponsorshipCode(String url) {
        if(url == null || url.isEmpty())
            return null;

        int index = url.indexOf("://");
        if(index >= 0)
            url = url.substring(index+3);

        String[] as = split(url, "?");
        if(as != null && as.length > 1) {
            String action = as[0];
            if(action.equals(ACTION_SPONSOR)) {
                String query = as[1];
                String[] params = split(query, "&");
                if(params != null && params.length > 0) {
                    int len = params.length;
                    for(int i=0; i<len; i++) {
                        String[] kv = split(params[i], "=");
                        if(kv != null && kv.length > 0) {
                            String key = kv[0].toLowerCase();
                            String val = kv[1];
                            if(key.equals(CODE))
                                return val;
                        }
                    }
                }
            }
        }
        return null;
    }

    private String[] split(String str,  String delimiter) {
        String[] array;
        int occurrences = 0;
        int indexOfInnerString = 0;
        int indexOfDelimiter = 0;
        int counter = 0;

        // Check for null input strings.
        if (str == null)
            return null;

        // Check for null or empty delimiter strings.
        if (delimiter.length() <= 0 || delimiter == null)
            return null;


        // If str begins with delimiter then remove it in order
        // to comply with the desired format.

        if (str.startsWith(delimiter)) {
            str = str.substring(delimiter.length());
        }

        // If str does not end with the delimiter then add it
        // to the string in order to comply with the desired format.
        if (!str.endsWith(delimiter)) {
            str += delimiter;
        }

        // Count occurrences of the delimiter in the string.
        // Occurrences should be the same amount of inner strings.
        while((indexOfDelimiter = str.indexOf(delimiter, indexOfInnerString)) != -1) {
            occurrences += 1;
            indexOfInnerString = indexOfDelimiter +
                    delimiter.length();
        }

        // Declare the array with the correct size.
        array = new String[occurrences];

        // Reset the indices.
        indexOfInnerString = 0;
        indexOfDelimiter = 0;

        // Walk across the string again and this time add the
        // strings to the array.
        while((indexOfDelimiter = str.indexOf(delimiter, indexOfInnerString)) != -1) {
            // Add string to array.
            array[counter] = str.substring(indexOfInnerString,indexOfDelimiter);

            // Increment the index to the next character after
            // the next delimiter.
            indexOfInnerString = indexOfDelimiter +
                    delimiter.length();

            // Inc the counter.
            counter += 1;
        }

        return array;
    }
}
