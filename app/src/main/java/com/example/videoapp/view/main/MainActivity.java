package com.example.videoapp.view.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.example.videoapp.R;
import com.example.videoapp.databinding.ActivityMainBinding;
import com.example.videoapp.model.notification.ServiceNotification;
import com.example.videoapp.view.categories.CategoriesFragment;
import com.example.videoapp.view.categories.itemcategories.ItemCategoryFragment;
import com.example.videoapp.view.favorites.FavoritesFragment;
import com.example.videoapp.view.videohot.HotVideoFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    ViewPagerAdapter viewPagerAdapter;
    ArrayList<Fragment> fragmentList;
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        checkNetwork();
        initViewPager();
        onClickMenuNavigation();
        startService(new Intent(getBaseContext(), ServiceNotification.class));
        View header = binding.navigationView.getHeaderView(0);
        ImageView img = header.findViewById(R.id.imgAppICon);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLanguage("en");
            }
        });
    }

    public void checkNetwork(){
        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int[] networkType = {ConnectivityManager.TYPE_MOBILE, ConnectivityManager.TYPE_WIFI};
                if (isNetworkConnected(context, networkType)){
                    return;
                }else{
                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                    alertDialog.setMessage(getResources().getString(R.string.please_check))
                            .setPositiveButton(getResources().getString(R.string.wifi_settings), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create();
                            alertDialog.show()
                                    .setCancelable(false);
                }
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
    }
    public boolean isNetworkConnected(Context context, int[] networkTypes){
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            for (int networkType: networkTypes) {
                NetworkInfo networkInfo = cm.getNetworkInfo(networkType);
                if(networkInfo!=null && networkInfo.getState() == NetworkInfo.State.CONNECTED){
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void onClickMenuNavigation(){
        binding.imgButtonMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.openDrawer(GravityCompat.START);
                } else {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                }
            }
        });
    }

    public void initBottomMenu() {
        binding.navBottom.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.mn_hot_videos:
                        binding.viewPager.setCurrentItem(0);
                        binding.tvMenuName.setText(getResources().getString(R.string.mn_hot_videos));
                        break;
                    case R.id.mn_categories:
                        binding.viewPager.setCurrentItem(1);
                        binding.tvMenuName.setText(getResources().getString(R.string.mn_categories));
                        break;
                    case R.id.mn_favorite:
                        binding.viewPager.setCurrentItem(2);
                        binding.tvMenuName.setText(getResources().getString(R.string.mn_favorite));
                        break;
                }
                return true;
            }
        });
    }

    public void setLanguage(String language){
        Locale locale = new Locale(language);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration,
                getBaseContext().getResources().getDisplayMetrics());
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public ArrayList<Fragment> getFragmentList() {
        fragmentList = new ArrayList<>();
        fragmentList.add(HotVideoFragment.newInstance());
        fragmentList.add(CategoriesFragment.newInstance());
        fragmentList.add(FavoritesFragment.newInstance());
        fragmentList.add(ItemCategoryFragment.newInstance());
        return fragmentList;
    }

    public void getItemCategory(String title){
        binding.viewPager.setCurrentItem(3);
        viewPagerAdapter.notifyDataSetChanged();
        binding.tvMenuName.setText(title);
    }

    public void initViewPager(){
        getFragmentList();
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragmentList);
        initBottomMenu();
        binding.viewPager.setAdapter(viewPagerAdapter);
        binding.viewPager.setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                final float normalizedPosition = Math.abs(Math.abs(position) - 1);
                page.setScaleX(normalizedPosition / 2 + 0.5f);
                page.setScaleY(normalizedPosition / 2 + 0.5f);

            }
        });
        //fragments will be destroyed when navigate anymore than 1 fragment (default: 1)
        //binding.viewPager.setOffscreenPageLimit(2);
        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position < 3) {
                    binding.navBottom.getMenu().getItem(position).setChecked(true);
                    if (position == 0) {
                        binding.tvMenuName.setText(getResources().getString(R.string.mn_hot_videos));
                    }
                    if (position == 1) {
                        binding.tvMenuName.setText(getResources().getString(R.string.mn_categories));
                    }
                    if (position == 2) {
                        binding.tvMenuName.setText(getResources().getString(R.string.mn_favorite));
                    }
                }else{

                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    public static class ViewPagerAdapter extends FragmentPagerAdapter {
        ArrayList<Fragment> fragmentList;


        public ViewPagerAdapter(@NonNull FragmentManager fm, ArrayList<Fragment> fragmentList) {
            super(fm);
            this.fragmentList = fragmentList;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
    }
}
