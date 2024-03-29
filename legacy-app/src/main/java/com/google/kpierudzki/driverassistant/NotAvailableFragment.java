package com.google.kpierudzki.driverassistant;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.kpierudzki.driverassistant.common.view_components.TileFragment;
import com.google.kpierudzki.driverassistant.obd.datamodel.ObdParamType;
import com.google.kpierudzki.driverassistant.util.MainThreadUtil;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Kamil on 25.06.2017.
 */

public class NotAvailableFragment extends Fragment {

    @BindView(R.id.frame1)
    FrameLayout frameLayout1;

    @BindView(R.id.frame2)
    FrameLayout frameLayout2;

    private TileFragment tileFragmentSpeed, tileFragmentRpm;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_not_available, container, false);
        ButterKnife.bind(this, view);

        tileFragmentSpeed = TileFragment.Companion.newInstance(ObdParamType.SPEED);
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(frameLayout1.getId(), tileFragmentSpeed)
                .commit();

        tileFragmentRpm = TileFragment.Companion.newInstance(ObdParamType.ENGINE_RPM);
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(frameLayout2.getId(), tileFragmentRpm)
                .commit();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSpeed();
        updateRpm();
    }

    private void updateSpeed() {
        MainThreadUtil.postDelayed(1_000, () -> {
            tileFragmentSpeed.updateChart(new Random().nextInt(20), ObdParamType.SPEED);
            updateSpeed();
        });
    }

    private void updateRpm() {
        MainThreadUtil.postDelayed(1_000, () -> {
            tileFragmentRpm.updateChart(new Random().nextInt(20), ObdParamType.ENGINE_RPM);
            updateRpm();
        });
    }
}
