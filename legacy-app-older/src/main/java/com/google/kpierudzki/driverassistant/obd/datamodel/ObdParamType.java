package com.google.kpierudzki.driverassistant.obd.datamodel;

import android.support.annotation.StringRes;

import com.google.kpierudzki.driverassistant.R;

/**
 * Created by Kamil on 15.09.2017.
 */

public enum ObdParamType {

    CONSUMPTION_RATE(0, 0),
    FUEL_TYPE(0, 0),
    LITRES_PER_100K(0, 0),
    LITRES_PER_HOUR(0, 0),
    LONG_LITRES_PER_100KM(0, 0),
    LONG_LITRES_PER_HOUR(0, 0),
    EQUIVALENT_RATIO(0, 0),
    MODULE_VOLTAGE(0, 0),
    VIN(0, 0),
    ENGINE_LOAD(R.string.Parameter_Load_Map_Unit, R.string.Parameter_Load_Map_Name),
    MAF(R.string.Parameter_Maf_Map_Unit, R.string.Parameter_Maf_Map_Name),
    OIL_TEMP(R.string.Parameter_Oil_Map_Unit, R.string.Parameter_Oil_Map_Name),
    ENGINE_RPM(R.string.Parameter_Rpm_Map_Unit, R.string.Parameter_Rpm_Map_Name),
    THROTTLE_POSITION(0, 0),
    BAROMETRIC_PRESSURE(R.string.Parameter_BarometricPress_Map_Unit, R.string.Parameter_AmbientTemp_Map_Name),
    FUEL_PRESSURE(0, 0),
    IMAP(0, 0),
    AIT(0, 0),
    AMBIENT_AIR_TEMP(R.string.Parameter_AmbientTemp_Map_Unit, R.string.Parameter_AmbientTemp_Map_Name),
    COOLANT_TEMP(R.string.Parameter_CoolantTemp_Map_Unit, R.string.Parameter_CoolantTemp_Map_Name),
    FUEL_LEVEL(0, 0),
    SPEED(R.string.Parameter_Speed_Map_Unit, R.string.Parameter_Speed_Map_Name),
    ERROR(0, 0),
    ECO_SCORE(R.string.Parameter_Score_Map_Unit, R.string.Parameter_Score_Map_Name),
    DATE(R.string.Parameter_Date_Map_Unit, R.string.Parameter_Date_Map_Name),
    TROUBLE_CODES(0, 0),
    UNKNOWN(0, 0);

    private final int unit;
    private final int localizedName;

    ObdParamType(@StringRes int unit, @StringRes int localizedName) {
        this.unit = unit;
        this.localizedName = localizedName;
    }

    public int getUnit() {
        return unit;
    }

    public int getLocalizedName() {
        return localizedName;
    }

    public final static String SPEED_COL = "current_speed";//[km/h]
    public final static String RPM_COL = "current_rpm";//[rev/min]
    public final static String ENGINE_COOL_TEMP_COL = "current_engine_coolant_temp";//[*C]
    public final static String MAF_COL = "current_maf";//[g/s]
    public final static String LOAD_COL = "current_load";//[%]
    public final static String OIL_TEMP_COL = "current_oil_temp";//[*C]
    public final static String AMBIENT_TEMP_COL = "current_ambient_temp";//[*C]
    public final static String BAROMETRIC_PRESSURE_COL = "current_barometric_press";//[kPa]

    //todo rest of parameters

}
