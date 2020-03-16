package com.bignerdranch.android.criminalintent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.List;
import java.util.UUID;

public class CrimePagerActivity extends AppCompatActivity implements CrimeFragment.Callbacks {
    private static final String EXTRA_CRIME_ID = "com.bignerdranch.android.criminalintent.crime_id";

    private ViewPager mViewPager;
    private List<Crime> mCrimes;
    private Button jumpToFirst;
    private Button jumpToLast;


    @Override
    public void onCrimeUpdated(Crime crime) {
        
    }

    public static Intent newIntent(Context packageContext, UUID crimeId){
        Intent intent = new Intent(packageContext, CrimePagerActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, crimeId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_pager);

        UUID crimeId = (UUID) getIntent().getSerializableExtra(EXTRA_CRIME_ID);

        mViewPager = findViewById(R.id.activity_crime_pager_view_pager);
        jumpToFirst = findViewById(R.id.first_page);
        jumpToLast = findViewById(R.id.last_page);
        jumpToLast.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(mCrimes.size() - 1, true);
                jumpToLast.setEnabled(false);
                jumpToFirst.setEnabled(true);
            }
        });
        jumpToFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(0, true);
                jumpToFirst.setEnabled(false);
                jumpToLast.setEnabled(true);
            }
        });

        mCrimes = CrimeLab.get(this).getCrimes();
        FragmentManager fragmentManager = getSupportFragmentManager();


        //FragmentStatePagerAdapter单参数构造函数被废弃
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateJumpButton(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager, 2) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                //将CrimeFragment 实例返回给CrimePagerActivity适配器进行绑定
                Crime crime = mCrimes.get(position);
                Log.d("aaa", position + "");
                return CrimeFragment.newInstance(crime.getId());
            }

            @Override
            public int getCount() {
                Log.d("aaa", mCrimes.size() + "");
                return mCrimes.size();

            }
        });

        for(int i = 0; i < mCrimes.size(); i++){
            if(mCrimes.get(i).getId().equals(crimeId)){
                mViewPager.setCurrentItem(i);
                updateJumpButton(i);
                break;
            }
        }
    }

    private void updateJumpButton(int i){
        if(i == 0){
            jumpToFirst.setEnabled(false);
            jumpToLast.setEnabled(true);
        }else if(i == mCrimes.size() - 1){
            jumpToLast.setEnabled(false);
            jumpToFirst.setEnabled(true);
        }else{
            jumpToLast.setEnabled(true);
            jumpToFirst.setEnabled(true);
        }
    }
}
