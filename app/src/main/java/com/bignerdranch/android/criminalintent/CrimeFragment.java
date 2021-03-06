package com.bignerdranch.android.criminalintent;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import 	android.text.format.DateFormat;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_PHOTO = "dialogPhoto";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_PHOTO = 2;
    private static final int REQUEST_READ_CONTRACTS = 3;
    private static final int CLICK_PHOTO = 4;

    private Crime mCrime;
    private File mPhotoFile;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    private Button mReportButton;
    private Button mSuspectButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private Callbacks mCallbacks;
    private Button mDeleteButton;
    private Button mCallSuspect;

    /**
     * Required interface for hosting activities
     */
    public interface Callbacks{
        void onCrimeUpdated(Crime crime);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public static CrimeFragment newInstance(UUID crimeId){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID)getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
    }

    public void returnResult(){
        getActivity().setResult(Activity.RESULT_OK, null);   //fragment不能持有返回结果，只有activity可以
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = view.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //This space intentionally left bank
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable s) {
                //This one too
            }
        });

        mDateButton = view.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //日期点击事件，打开DatePicker
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mSolvedCheckBox = view.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
                updateCrime();
            }
        });

        mReportButton = view.findViewById(R.id.crime_report);
        /*mReportButton.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getActivity(), "长按就是这样子 ", Toast.LENGTH_SHORT).show();
                return false;
            }
        });*/
        /*mReportButton.setOnCreateContextMenuListener(this);*/
        mReportButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT, getString(
                R.string.crime_report_subject));
                i = Intent.createChooser(i, getString(R.string.send_report));
                startActivity(i);
            }
        });

        final Intent pickContack = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = view.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                startActivityForResult(pickContack, REQUEST_CONTACT);
            }
        });

        if(mCrime.getSuspect() != null){
            mSuspectButton.setText(mCrime.getSuspect());
        }

        PackageManager packageManager = getActivity().getPackageManager();
        if(packageManager.resolveActivity(pickContack, PackageManager.MATCH_DEFAULT_ONLY) == null){
            mSuspectButton.setEnabled(false);
        }

        mPhotoButton = view.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        boolean canTakePhoto = mPhotoFile != null && captureImage.resolveActivity(packageManager) != null;  //判断是否有可调用软件
        mPhotoButton.setEnabled(canTakePhoto);
        mPhotoButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "com.bignerbranch.android.criminalintent.fileprovider", mPhotoFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                List<ResolveInfo> cameraActivities = getActivity().
                        getPackageManager().queryIntentActivities(captureImage,
                        PackageManager.MATCH_DEFAULT_ONLY);

                for(ResolveInfo activity : cameraActivities){
                    getActivity().grantUriPermission(activity.activityInfo.packageName,
                            uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });
        mPhotoView = view.findViewById(R.id.crime_photo);
        mPhotoView.setOnClickListener(new View.OnClickListener() {

            /**
             *如何当前Crime含有图片，则打开一个PhotoFragment展示图片细节，否则使用Toast提醒无相关图片
             */
            @Override
            public void onClick(View v) {
                if(mPhotoFile == null || !mPhotoFile.exists()){
                    Toast.makeText(getActivity(), "Crime Photo 不存在", Toast.LENGTH_SHORT).show();
                }else{
                    FragmentManager manager = getFragmentManager();
                    PhotoFragment dialog = PhotoFragment.newInstance(mCrime.getId());
                    dialog.setTargetFragment(CrimeFragment.this, CLICK_PHOTO);
                    dialog.show(manager, DIALOG_PHOTO);
                }
            }
        });

        mDeleteButton = view.findViewById(R.id.delete_crime);  //删除制定Crime
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Context mContext = CrimeListFragment.getTheContext();
                CrimeLab crimeLab = CrimeLab.get(mContext);
                crimeLab.deleteCrime(mCrime);
                getActivity().finish();
            }
        });

        mCallSuspect = view.findViewById(R.id.call_suspect);   //直接呼叫suspect
        mCallSuspect.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String buttonText = getString(R.string.crime_suspect_text);   //先判断suspect是否为空
                if(mSuspectButton.getText().toString().equals(buttonText)){
                    Toast.makeText(getActivity(), "suspect为空！", Toast.LENGTH_SHORT).show();
                }
                else if(getActivity().checkSelfPermission(Manifest.permission.READ_CONTACTS ) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                            REQUEST_READ_CONTRACTS);
                }else {
                    callSuspect();
                }
            }
        });
        updatePhotoView();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode != Activity.RESULT_OK){
            return;
        }

        if(requestCode == REQUEST_DATE){
            Date date = (Date)data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateCrime();
            updateDate();
        }else if(requestCode == REQUEST_CONTACT && data != null){
            Uri contactUri = data.getData();
            /*Specify which fields you want your query to return values for.*/
            String[] queryFields = new String[]{ContactsContract.Contacts.DISPLAY_NAME};
            /*Perform your query - the contactUri is like a "where" clause here*/
            Cursor c = getActivity().getContentResolver().query(contactUri, queryFields,
                    null, null, null);
            try{
                /*Double-check that you actually got results*/
                if(c.getCount() == 0){
                    return;
                }
                /*Pull out the first column of the first row of data that    is your suspect's name*/
                c.moveToFirst();
                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                updateCrime();
                mSuspectButton.setText(suspect);
            }finally{
                c.close();
            }
        }else if(requestCode == REQUEST_PHOTO){
            Uri uri = FileProvider.getUriForFile(getActivity(),
                    "com.bignerbranch.android.criminalintent.fileprovider", mPhotoFile);
            getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            updateCrime();
            updatePhotoView();
        }
    }

    private void updateCrime(){
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
    }

    private void updateDate() {
        mDateButton.setText(mCrime.getDate().toString());
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    private String getCrimeReport(){
        String solvedString = null;
        if(mCrime.isSolved()){
            solvedString = getString(R.string.crime_report_solved);
        }else{
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if(suspect == null){
            suspect = getString(R.string.crime_report_no_suspect);
        }else{
            suspect = getString(R.string.crime_report_suspect);
        }

        String report = getString(R.string.crime_report,
                mCrime.getTitle(), dateString, solvedString, suspect);

        return report;
    }

    private void updatePhotoView(){
        if(mPhotoFile == null || ! mPhotoFile.exists()){
            mPhotoView.setImageDrawable(null);
            mPhotoView.setContentDescription(getString(R.string.crime_photo_no_image_description));
        }else{
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
            mPhotoView.setContentDescription(getString(R.string.crime_photo_image_description));
        }
    }
    public static void startWithTransition(Activity activity, Intent intent,
                                          final View sourceView) {
        ViewCompat.setTransitionName(sourceView, "image");
        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(activity, sourceView, "image");
        activity.startActivity(intent, options.toBundle());
    }

    /*@Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.fragment_crime_list, menu);
    }*/

    //根据suspect姓名查询相关电话号码，根据查询结果选择打开拨号界面或者弹出提示
    private void callSuspect(){
        String[] queryFields = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor cursor = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                queryFields,
                ContactsContract.PhoneLookup.DISPLAY_NAME + "= ?",
                new String[]{mSuspectButton.getText().toString()},
                null);  //根据联系人姓名查询电话号码
        if(cursor == null || cursor.getCount() == 0){
            Toast.makeText(getActivity(), "查询失败", Toast.LENGTH_SHORT).show();
        }else{
            cursor.moveToFirst();
            if(!cursor.isAfterLast()){
                //String telNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String telNumber = cursor.getString(0);
                //Toast.makeText(getActivity(), telNumber, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + telNumber));
                startActivity(intent);
            }
        }
        cursor.close();
    }


    //验证权限获取结果
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case REQUEST_READ_CONTRACTS:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    callSuspect();
                }
                else{
                    Toast.makeText(getActivity(), "拒绝读取联系人资料将无法拨打嫌疑人的电话", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
}
