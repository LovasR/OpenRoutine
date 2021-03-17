package tk.lakatstudio.timeallocator;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class CollectionPagerAdapter extends FragmentStatePagerAdapter {
    Fragment fragments[];

    public CollectionPagerAdapter(@NonNull FragmentManager fm, Fragment fragmentsIn[]) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        fragments = fragmentsIn;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Log.e("UI_test", position + fragments[0].toString());
        Fragment fragment = fragments[position];
        Bundle args = new Bundle();
        args.putInt("test", position + 1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return fragments.length;
    }
}