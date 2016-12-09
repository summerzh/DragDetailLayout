package com.it.dragdetaillayout;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * created by summer on 2016/11/13
 */
public class TopFragment extends Fragment {

    private static DragDetailLayout2 mLayout;

    public static TopFragment getInstance(DragDetailLayout2 layout) {
        mLayout = layout;
        return new TopFragment();
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View inflate = inflater.inflate(R.layout.fragment_top, container, false);
        ScrollView scrollView = (ScrollView) inflate.findViewById(R.id.sv_container);
        TextView textView = (TextView) inflate.findViewById(R.id.tv_click);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLayout.open();
            }
        });
        boolean b = ViewCompat.canScrollVertically(scrollView, -1);
        Log.d("result", "onCreate " + (b ? "can" : "no"));
        return inflate;
    }


}
