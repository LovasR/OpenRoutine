package tk.lakatstudio.timeallocator;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class CollectionPagerAdapter extends FragmentStateAdapter {
    Fragment fragments[];

    public CollectionPagerAdapter(FragmentManager fm, Lifecycle lc, Fragment[] fragmentsIn) {
        super(fm, lc);
        fragments = fragmentsIn;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Log.e("UI_test", position + fragments[0].toString());
        Fragment fragment = fragments[position];
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return fragments.length;
    }
}