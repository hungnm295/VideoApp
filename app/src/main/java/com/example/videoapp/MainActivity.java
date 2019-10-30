package com.example.videoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.videoapp.Categories.View.CategoriesFragment;
import com.example.videoapp.Categories.View.ItemCategoryFragment;
import com.example.videoapp.Favorites.FavoritesFragment;
import com.example.videoapp.HotVideo.View.HotVideoFragment;
import com.example.videoapp.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    ViewPagerAdapter viewPagerAdapter;
    ArrayList<Fragment> fragmentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initViewPager();
        onClickMenuNavigation();

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
                final float normalizedposition = Math.abs(Math.abs(position) - 1);
                page.setScaleX(normalizedposition / 2 + 0.5f);
                page.setScaleY(normalizedposition / 2 + 0.5f);

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
    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
