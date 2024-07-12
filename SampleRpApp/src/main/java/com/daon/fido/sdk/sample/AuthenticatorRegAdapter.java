package com.daon.fido.sdk.sample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.daon.fido.client.sdk.core.IFidoSdk;
import com.daon.fido.client.sdk.model.AuthenticatorReg;


/**
 * Created by MPS Daon Laptop on 13/04/2016.
 */
public class AuthenticatorRegAdapter extends ArrayAdapter<AuthenticatorReg> {
    public AuthenticatorRegAdapter(Context context, AuthenticatorReg[] authenticators) {
        super(context, 0, authenticators);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        AuthenticatorReg authenticator = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_authenticator, parent, false);
        }

        // Set the actual authenticator associated with the list item
        convertView.setTag(authenticator);

        // Lookup views for data population
        ImageView image = (ImageView) convertView.findViewById(R.id.authenticator_icon);
        TextView name = (TextView) convertView.findViewById(R.id.authenticator_name);

        // Populate the authenticator name
        name.setText(authenticator.getTitle());

        // Grey name is not registered
        IFidoSdk.LockStatus lockStatus = ((CoreApplication)getContext().getApplicationContext()).getFido().getLockStatus(authenticator.getAaid());
        if(lockStatus == IFidoSdk.LockStatus.Locked) {
            name.setTextColor(Color.RED);
        } else if(authenticator.isRegistered()) {
            name.setTextColor(Color.BLACK);
        } else {
            name.setTextColor(Color.LTGRAY);
        }

        // Create the icon to be displayed. NB image is formatted as "data:image/png;base64,<Base 64 image data>"
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        int commaIndex = authenticator.getIcon().indexOf(',');
        String imageBase64 = authenticator.getIcon().substring(commaIndex + 1);
        byte[] imgBytes = Base64.decode(imageBase64, Base64.DEFAULT);
        Bitmap bmp = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length, options);
        image.setImageBitmap(bmp);

        // Return the completed view to render on screen
        return convertView;
    }
}
