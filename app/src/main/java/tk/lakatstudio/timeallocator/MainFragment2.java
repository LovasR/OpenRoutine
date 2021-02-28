package tk.lakatstudio.timeallocator;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MainFragment2 extends Fragment {

    ListView todoList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment_2, container, false);

        Log.e("UI_test", "oncreateview_fragment2");
        todoList = view.findViewById(R.id.testDayPlanner);
        //todoListInit(this);

        return view;
    }

}
