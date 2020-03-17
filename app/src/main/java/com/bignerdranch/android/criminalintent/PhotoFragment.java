package com.bignerdranch.android.criminalintent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.io.File;
import java.util.UUID;

public class PhotoFragment extends DialogFragment {
    private static final String CRIME_ID = "crime_id";
    private ImageView mCrimePhotoView;
    private File mPhotoFile;

    /**
     * 根据传入的crime id名返回一个PhotoFragment实例
     * @param crimeId Crime的id
     * @return 包含crime id的PhotoFragment实例
     */
    public static PhotoFragment newInstance(UUID crimeId){
        Bundle args = new Bundle();
        args.putSerializable(CRIME_ID, crimeId);

        PhotoFragment fragment = new PhotoFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        mPhotoFile = getPhotoFile();
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.crime_photo, null);
        mCrimePhotoView = view.findViewById(R.id.image_view);
        setCrimePhotoView();
        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setCancelable(true)
                .create();
    }

    /**
     * 从传入的Bundle中获取当前Crime 图片的FIle
     * @return 当前的Crime 图片的File
     */
    private File getPhotoFile(){
        UUID crimeId = (UUID)getArguments().getSerializable(CRIME_ID);
        CrimeLab crimeLab = CrimeLab.get(CrimeListFragment.getTheContext());
        Crime mCrime = crimeLab.getCrime(crimeId);
       return  crimeLab.getPhotoFile(mCrime);
    }

    private void setCrimePhotoView(){
        if(mPhotoFile == null || ! mPhotoFile.exists()){
            mCrimePhotoView.setImageDrawable(null);
            mCrimePhotoView.setContentDescription(getString(R.string.crime_photo_no_image_description));
        }else{
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
            mCrimePhotoView.setImageBitmap(bitmap);
            mCrimePhotoView.setContentDescription(getString(R.string.crime_photo_image_description));
        }
    }
}