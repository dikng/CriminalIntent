package com.bignerdranch.android.criminalintent;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.UUID;

public class DiaplayCrimePhoto extends AppCompatActivity {
    public static final String PHOTO_UUID = "PHOTO_UUID";
    private File mFilePhoto;
    private ImageView mImageView;
    private UUID photoId;
    private Crime mCrime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_crime_photo);

        mImageView = findViewById(R.id.image_view);
        displayPhoto();
    }

    private void displayPhoto(){
        photoId = (UUID)getIntent().getSerializableExtra(PHOTO_UUID);
        mCrime = CrimeLab.get(this).getCrime(photoId);
        mFilePhoto = CrimeLab.get(this).getPhotoFile(mCrime);

        if(mFilePhoto == null | !mFilePhoto.exists()){
            Toast.makeText(this, "无相关图片", Toast.LENGTH_SHORT).show();
        }else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mFilePhoto.getPath(), this);
            mImageView.setImageBitmap(bitmap);
            mImageView.setContentDescription(getString(R.string.crime_photo_image_description));
        }
    }
}
