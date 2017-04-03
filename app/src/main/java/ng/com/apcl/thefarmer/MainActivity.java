package ng.com.apcl.thefarmer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.api.client.util.store.DataStore;
import com.kinvey.android.AsyncAppData;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.java.Query;
import com.kinvey.java.User;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.core.MediaHttpUploader;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.model.KinveyDeleteResponse;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int SELECT_PHOTO_ADD = 2;
    private static final int SELECT_PHOTO_UPDATE = 3;
    GoogleMap mMap;
    private View mAddDialogView;
    private View mUpdateDialogView;
    private View mAboutDialogView;
    Client mHelloTractorClient;
    private LatLng mSelectedCoord;
    private  DataStore mFarmerStore;
    private File mImageFile;
    private View mViewDialogView;
    private String farmerID;
//    private Farmer selectedFarmer;
    private ArrayList<Farmer> allFarmers;
    String fName, fSize, fPhone, fCoord;
    private ProgressDialog pd;
    private Farmer selectedFarmer;
    private ImageView img;
    private NotificationManager mNotificationManager;

    @Override
    protected void onResume() {
        super.onResume();
        if (!mHelloTractorClient.user().isUserLoggedIn()) {
            mHelloTractorClient.user().login("hellotractor", "hellotractor", new KinveyClientCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onFailure(Throwable throwable) {
                    Toast.makeText(MainActivity.this, "Failed to login", Toast.LENGTH_SHORT).show();

                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PHOTO_ADD){
            if (resultCode == RESULT_OK) {
                final Uri imageUri = data.getData();
                String realImageURL = getRealPathFromURI(imageUri);

                ImageView snapshot = (ImageView) mAddDialogView.findViewById(R.id.takeashot);
                mImageFile = new File(realImageURL);

                Bitmap myBitmap = BitmapFactory.decodeFile(mImageFile.getAbsolutePath());
                snapshot.setImageBitmap(myBitmap);
            }
        } else if (requestCode == SELECT_PHOTO_UPDATE){
            if (resultCode == RESULT_OK) {
                final Uri imageUri = data.getData();
                String realImageURL = getRealPathFromURI(imageUri);

                ImageView snapshot = (ImageView) mUpdateDialogView.findViewById(R.id.update_takeashot);
                mImageFile = new File(realImageURL);

                Bitmap myBitmap = BitmapFactory.decodeFile(mImageFile.getAbsolutePath());
                snapshot.setImageBitmap(myBitmap);
            }
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        allFarmers = new ArrayList<>();
        mHelloTractorClient = new Client.Builder("kid_rkNsq9Khx",
                "5f81c2a2275141ba89b5a58c421d7496",
                this.getApplicationContext()).build();

        File helloTractorFolder = new File(Environment.getExternalStorageDirectory() + "/The Farmer/");
        if (!helloTractorFolder.exists()){
            helloTractorFolder.mkdir();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    View.OnClickListener mListenerUpdate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, SELECT_PHOTO_UPDATE);
        }
    };

    View.OnClickListener mListenerAdd = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, SELECT_PHOTO_ADD);
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            LayoutInflater inflater = MainActivity.this.getLayoutInflater();
            mAboutDialogView = inflater.inflate(R.layout.dialog_about, null);
            builder.setTitle("About");
            builder.setView(mAboutDialogView)
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            builder.create();
            builder.show();
        } else if (id == R.id.action_normal){
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else if (id == R.id.action_hybrid){
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else if (id == R.id.action_satelite){
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else if (id == R.id.action_terrain){
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        } else if (id == R.id.action_none){
            mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        } else if (id == R.id.action_repo){
            Intent mRepoIntent = new Intent(Intent.ACTION_VIEW);
            mRepoIntent.setData(Uri.parse("https://github.com/mugambbo/thefarmer"));
            startActivity(mRepoIntent);
        } else if (id == R.id.action_sync){
            syncFarmers();
        }
        return true;
    }


    public void syncFarmers(){
        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Loading...");
        pd.show();
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setIndeterminate(true);

        mMap.clear();
        allFarmers.clear();
        AsyncAppData<Farmer> farmers = mHelloTractorClient.appData(Farmer.COLLECTION, Farmer.class);
        farmers.get(new KinveyListCallback<Farmer>() {
            @Override
            public void onSuccess(Farmer [] result) {
                allFarmers.addAll(Arrays.asList(result));
                for (int i = 0; i < allFarmers.size(); i++){
                    mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(allFarmers.get(i).getmCoordinates().split(", ")[0]), Double.parseDouble(allFarmers.get(i).getmCoordinates().split(", ")[1]))).title(allFarmers.get(i).getName())).setTag(allFarmers.get(i));
                }
                if (!allFarmers.isEmpty()) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(allFarmers.get(allFarmers.size()-1).getmCoordinates().split(", ")[0]), Double.parseDouble(allFarmers.get(0).getmCoordinates().split(", ")[1])), 5));
                }
                Toast.makeText(MainActivity.this, "Successfully displayed all farmers on map", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(Throwable error)  {
                Toast.makeText(MainActivity.this, "Unable to retrieve farmers", Toast.LENGTH_SHORT).show();
            }
        });
        pd.dismiss();
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        syncFarmers();

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mSelectedCoord = latLng;
                addFarmer();
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
//                farmerID = marker.getTag().toString().split(", ")[0]+", "+ marker.getTag().toString().split(", ")[1];
//                mSelectedCoord = marker.getPosition();
                selectedFarmer = (Farmer)marker.getTag();
                viewFarmer();
                return true;
            }
        });
    }


    public void addFarmer (){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        builder.setTitle("Add a Farmer");
        builder.setIcon(R.mipmap.ic_logo);
        mAddDialogView = inflater.inflate(R.layout.dialog_add_farmer, null);
        Button mTakePhoto = (Button) mAddDialogView.findViewById(R.id.take_photo);
        mTakePhoto.setOnClickListener(mListenerAdd);
        TextView mCoordinatesTV = (TextView) mAddDialogView.findViewById(R.id.coordinates);
        mCoordinatesTV.setText(roundTo(mSelectedCoord.latitude, 2)+", "+roundTo(mSelectedCoord.longitude, 2));
        builder.setView(mAddDialogView)
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText nameET = (EditText) mAddDialogView.findViewById(R.id.name);
                        EditText phoneET = (EditText) mAddDialogView.findViewById(R.id.phone_number);
                        EditText farmSizeET = (EditText) mAddDialogView.findViewById(R.id.farm_size);

                        final String mName = nameET.getText().toString();
                        final String mPhoneNumber = phoneET.getText().toString();
                        final String mFarmSize = farmSizeET.getText().toString();

                        if (!mName.trim().isEmpty() && !mPhoneNumber.trim().isEmpty() && !mFarmSize.trim().isEmpty()){
                            float mFarmSizeN = Float.parseFloat(mFarmSize);
                            //add to database
                            Farmer mFarmer = new Farmer();
                            mFarmer.setName(mName);
                            mFarmer.setmPhoneNumber(mPhoneNumber);
                            mFarmer.setmFarmSize(mFarmSizeN);
                            mFarmer.setmCoordinates(Double.toString(mSelectedCoord.latitude)+", "+Double.toString(mSelectedCoord.longitude));
                            mFarmer.setmImageID(mImageFile.getName());
                            Toast.makeText(MainActivity.this, "Created the mFarmer Object", Toast.LENGTH_SHORT).show();

                            AsyncAppData<Farmer> farmers = mHelloTractorClient.appData(Farmer.COLLECTION, Farmer.class);
                            Toast.makeText(MainActivity.this, "Created farmers collection", Toast.LENGTH_SHORT).show();
                            farmers.save(mFarmer, new KinveyClientCallback<Farmer>() {
                                @Override
                                public void onSuccess(Farmer farmer) {
                                    Toast.makeText(MainActivity.this, "Successfully added "+mName+" to database", Toast.LENGTH_LONG).show();
                                    refreshFarmerMap();
                                }

                                @Override
                                public void onFailure(Throwable throwable) {
                                    Toast.makeText(MainActivity.this, "Failed to add "+mName+" to database", Toast.LENGTH_LONG).show();
                                }
                            });


                            //Should resize photo before upload
                            makeNotification("Uploading "+mFarmer.getName()+"'s photo", 202);
                            mHelloTractorClient.file().upload(mImageFile, new UploaderProgressListener() {
                                @Override
                                public void progressChanged(MediaHttpUploader mediaHttpUploader) throws IOException {}
                                @Override
                                public void onSuccess(FileMetaData fileMetaData) {
                                    Toast.makeText(MainActivity.this, "Successfully uploaded photo.", Toast.LENGTH_SHORT).show();
                                    mNotificationManager.cancel(202);
                                }
                                @Override
                                public void onFailure(Throwable throwable) {
                                    Toast.makeText(MainActivity.this, "Unable to upload photo.", Toast.LENGTH_SHORT).show();
                                    mNotificationManager.cancel(202);
                                }
                            });

                            Toast.makeText(MainActivity.this, "Finished", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "One or more fields are empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        builder.create();
        builder.show();
    }

    public void viewFarmer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        mViewDialogView = inflater.inflate(R.layout.dialog_view_farmer, null);

        TextView mCoordinatesTV = (TextView) mViewDialogView.findViewById(R.id.view_latlng);
        TextView mFarmerName = (TextView) mViewDialogView.findViewById(R.id.view_name);
        TextView mFarmSize = (TextView) mViewDialogView.findViewById(R.id.view_size);
        TextView mPhoneNumber = (TextView) mViewDialogView.findViewById(R.id.view_phone_no);
        final ImageView viewImg = (ImageView) mViewDialogView.findViewById(R.id.view_farm_image);

        mFarmerName.setText(selectedFarmer.getName());
        mFarmSize.setText(selectedFarmer.getmFarmSize()+" Ha.");
        mPhoneNumber.setText(selectedFarmer.getmPhoneNumber());
        mCoordinatesTV.setText(roundTo(Double.parseDouble(selectedFarmer.getmCoordinates().split(", ")[0]), 2) + ", " + roundTo(Double.parseDouble(selectedFarmer.getmCoordinates().split(", ")[1]), 2));

        final File selectedFarmerImage = new File(Environment.getExternalStorageDirectory() + "/The Farmer/", selectedFarmer.getmImageID());
        if (selectedFarmerImage.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(selectedFarmerImage.getAbsolutePath());
            viewImg.setImageBitmap(myBitmap);
        } else {
            try {
                selectedFarmerImage.createNewFile();
                final FileOutputStream fos = new FileOutputStream(selectedFarmerImage);
                FileMetaData fileMetaDataForDownload = new FileMetaData();
                fileMetaDataForDownload.setId(selectedFarmer.getmImageID());

                makeNotification("Downloading "+selectedFarmer.getmName()+"'s photo", 200);
                mHelloTractorClient.file().download(fileMetaDataForDownload, fos, new DownloaderProgressListener() {
                    @Override
                    public void progressChanged(MediaHttpDownloader mediaHttpDownloader) throws IOException {

                    }
                    @Override
                    public void onSuccess(Void aVoid) {
                        try {
                            fos.write(selectedFarmerImage.getAbsolutePath().getBytes());
                            Toast.makeText(MainActivity.this, selectedFarmerImage.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                            Bitmap selectedFarmerBitmap = BitmapFactory.decodeFile(selectedFarmerImage.getAbsolutePath());
                            viewImg.setImageBitmap(selectedFarmerBitmap);
                            Toast.makeText(MainActivity.this, "Download complete.", Toast.LENGTH_SHORT).show();
                        } catch (IOException err) {
                            Toast.makeText(MainActivity.this, "Failed to read image.", Toast.LENGTH_LONG).show();
                        }
                        mNotificationManager.cancel(200);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        mNotificationManager.cancel(200);
                    }
                });
            } catch (IOException err){
                Toast.makeText(this, "Unable to download image.", Toast.LENGTH_SHORT).show();
            }
        }

        builder.setView(mViewDialogView)
                .setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        AsyncAppData<Farmer> theFarmer = mHelloTractorClient.appData(Farmer.COLLECTION, Farmer.class);
                        theFarmer.delete(new Query().equals("coordinates", selectedFarmer.getmCoordinates()), new KinveyDeleteCallback() {
                            @Override
                            public void onSuccess(KinveyDeleteResponse response) {
                                Toast.makeText(MainActivity.this, "Farmer removed", Toast.LENGTH_LONG).show();
                                refreshFarmerMap();
                            }
                            public void onFailure(Throwable error) {
                                Toast.makeText(MainActivity.this, "Failed to remove farmer", Toast.LENGTH_LONG).show();
                            }
                        });

                        FileMetaData fileMetaDataForDownload = new FileMetaData();
                        fileMetaDataForDownload.setId(selectedFarmer.getmImageID());
                        mHelloTractorClient.file().delete(fileMetaDataForDownload, new KinveyClientCallback<KinveyDeleteResponse>() {
                            @Override
                            public void onSuccess(KinveyDeleteResponse kinveyDeleteResponse) {
                                Toast.makeText(MainActivity.this, "Photo deleted.", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                Toast.makeText(MainActivity.this, "Unable to delete photo.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.update, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateFarmer();
                    }
                })
                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        builder.create();
        builder.show();
    }

    private void refreshFarmerMap() {
        syncFarmers();
    }

    public void updateFarmer (){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        builder.setTitle("Update a Farmer");
        builder.setIcon(R.mipmap.ic_logo);
        mUpdateDialogView = inflater.inflate(R.layout.dialog_update_farmer, null);

        final EditText nameET = (EditText) mUpdateDialogView.findViewById(R.id.update_name);
        final EditText phoneET = (EditText) mUpdateDialogView.findViewById(R.id.update_phone_number);
        final EditText farmSizeET = (EditText) mUpdateDialogView.findViewById(R.id.update_farm_size);
        TextView coordTV = (TextView) mUpdateDialogView.findViewById(R.id.update_coordinates);
        ImageView img = (ImageView) mUpdateDialogView.findViewById(R.id.update_takeashot);


        File selectedFarmerImage = new File(Environment.getExternalStorageDirectory() + "/The Farmer/", selectedFarmer.getmImageID());
        if (selectedFarmerImage.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(selectedFarmerImage.getAbsolutePath());
            img.setImageBitmap(myBitmap);
        }
        nameET.setText(selectedFarmer.getName());
        phoneET.setText(selectedFarmer.getmPhoneNumber());
        farmSizeET.setText(selectedFarmer.getmFarmSize()+"");
        coordTV.setText(roundTo(Double.parseDouble(selectedFarmer.getmCoordinates().split(", ")[0]), 2)+", "+roundTo(Double.parseDouble(selectedFarmer.getmCoordinates().split(", ")[1]), 2));

        builder.setView(mUpdateDialogView)
                .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        final String mName = nameET.getText().toString();
                        final String mPhoneNumber = phoneET.getText().toString();
                        final String mFarmSize = farmSizeET.getText().toString();

                        if (!mName.trim().isEmpty() && !mPhoneNumber.trim().isEmpty() && !mFarmSize.trim().isEmpty() && mImageFile.exists()){
                            float mFarmSizeN = Float.parseFloat(mFarmSize);
                            Farmer mFarmer = new Farmer();
                            mFarmer.setName(mName);
                            mFarmer.setmPhoneNumber(mPhoneNumber);
                            mFarmer.setmFarmSize(mFarmSizeN);
                            mFarmer.setmCoordinates(selectedFarmer.getmCoordinates().split(", ")[0]+", "+selectedFarmer.getmCoordinates().split(", ")[1]);
                            mFarmer.setmImageID(mImageFile.getName());

                            AsyncAppData<Farmer> farmers = mHelloTractorClient.appData(Farmer.COLLECTION, Farmer.class);
                            farmers.delete(new Query().equals("coordinates", selectedFarmer.getmCoordinates()), new KinveyDeleteCallback() {
                                @Override
                                public void onSuccess(KinveyDeleteResponse kinveyDeleteResponse) {}
                                @Override
                                public void onFailure(Throwable throwable) {}
                            });
                            farmers.save(mFarmer, new KinveyClientCallback<Farmer>() {
                                @Override
                                public void onSuccess(Farmer farmer) {
                                    Toast.makeText(MainActivity.this, "Successfully updated "+mName+"'s details", Toast.LENGTH_LONG).show();
                                    syncFarmers();
                                }
                                @Override
                                public void onFailure(Throwable throwable) {
                                    Toast.makeText(MainActivity.this, "Failed to update "+mName+"'s details", Toast.LENGTH_LONG).show();
                                }
                            });
                            Toast.makeText(MainActivity.this, "Finished", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "One or more fields are empty", Toast.LENGTH_SHORT).show();
                        }

                        makeNotification("Uploading "+selectedFarmer.getmName()+"'s photo", 201);
                        mHelloTractorClient.file().upload(mImageFile, new UploaderProgressListener() {
                            @Override
                            public void progressChanged(MediaHttpUploader mediaHttpUploader) throws IOException {

                            }
                            @Override
                            public void onSuccess(FileMetaData fileMetaData) {
                                mNotificationManager.cancel(201);
                                Toast.makeText(MainActivity.this, "Successfully uploaded photo.", Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                Toast.makeText(MainActivity.this, "Unable to upload photo.", Toast.LENGTH_SHORT).show();
                                mNotificationManager.cancel(201);
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Do nothing
                    }
                });

        builder.create();
        builder.show();

        Button mTakePhoto = (Button) mUpdateDialogView.findViewById(R.id.update_take_photo);
        mTakePhoto.setOnClickListener(mListenerUpdate);
    }

    public double roundTo (double number, int decimalPlace){
        double xNum = number * Math.pow(10, decimalPlace);
        float roundXNum = Math.round(xNum);
        double divideRoundXNum = roundXNum/Math.pow(10, decimalPlace);
        return divideRoundXNum;
    }

    public void makeNotification(String text, int number) {
        String ns = Context.NOTIFICATION_SERVICE;
        mNotificationManager = (NotificationManager) getSystemService(ns);
        CharSequence tickerText = getString(R.string.app_name); // ticker-text
        long when = System.currentTimeMillis(); // notification time
        Context context = getApplicationContext(); // application Context
        CharSequence contentTitle = getString(R.string.app_name); // expanded
        CharSequence contentText = text;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(contentTitle);
        builder.setContentText(contentText);
        builder.setTicker(tickerText);
        builder.setWhen(when);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setOngoing(true);
        builder.setAutoCancel(true);
        mNotificationManager.notify(number, builder.build());
    }


}
