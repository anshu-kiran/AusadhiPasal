package com.apackage.ausadhipasal;

import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button upload, submit;
    TextView name, address, phone;
    Uri URI = null;

    String to, subject, message, attachmentFile;
    int columnIndex;

    GPSTracker gps;

    private static final int PICK_FROM_GALLERY = 101;

    Geocoder geocoder;
    List<Address> addresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name = (TextView)findViewById(R.id.input_name);
        address = (TextView)findViewById(R.id.input_add);
        phone = (TextView)findViewById(R.id.input_phone);
        upload = (Button)findViewById(R.id.btn_upload);
        submit =(Button)findViewById(R.id.btn_submit);

        upload.setOnClickListener(this);
        submit.setOnClickListener(this);

        geocoder = new Geocoder(this, Locale.getDefault());
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_FROM_GALLERY && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            attachmentFile = cursor.getString(columnIndex);
            Log.e("Attachment Path:", attachmentFile);
            URI = Uri.parse("file://" + attachmentFile);
            cursor.close();
        }
    }

    @Override
    public void onClick(View v) {

        if (v == upload) {
            openGallery();
        }

        if (v == submit) {
            String pname = name.getText().toString();
            String padd = address.getText().toString();
            String pphone = phone.getText().toString();

            double[] location = getLocation();

            try {
                addresses = geocoder.getFromLocation(location[0], location[1], 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();

            to = "aks.anshu03@gmail.com";
            subject = "Order";
            message = "Order is -- \nName : "+ pname + "\nAddress Entered by User : "+ padd + "\nPhone Number : "+ pphone
                    +"\n\n\nLocation from GPS -- \nAddress : "+ address + "\nCity : " +city + "\nCountry : " + country
                    +"\nPostal Code - "+ postalCode;

            final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{ to});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            if (URI != null) {
                emailIntent.putExtra(Intent.EXTRA_STREAM, URI);
            }
            emailIntent
                    .putExtra(android.content.Intent.EXTRA_TEXT, message);
            this.startActivity(Intent.createChooser(emailIntent,
                    "Sending email..."));
        }

    }

    private double[] getLocation() {
        gps = new GPSTracker(MainActivity.this);

        if(gps.canGetLocation()){

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            double[] location = {latitude, longitude};
            return location;
        }else{
            gps.showSettingsAlert();
            return null;
        }
    }

    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra("return-data", true);
        startActivityForResult(
                Intent.createChooser(intent, "Complete action using"), PICK_FROM_GALLERY);

    }
}
