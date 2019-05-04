package com.coolweather.android.util;

import android.app.ProgressDialog;
import android.support.v4.app.Fragment;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.coolweather.android.db.City;
import com.coolweather.android.db.Country;
import com.coolweather.android.db.Province;

import java.util.ArrayList;
import java.util.List;

/**
 * 由于遍历省市县的功能在后面还会服用，因此不写在活动里面，而是卸载碎片里面，
 * 这样在需要的时候直接在布局里面引用碎片就可以了
 */
public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;

    public static final int LEVEL_COUNTRY = 2;

    private ProgressDialog progressDialog;       //进度对话框

    private TextView titleText;

    private Button backButton;

    private ListView listView;

    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();

    //省列表
    private List<Province> provinceList;

    //市列表
    private List<City> cityList;

    //县列表
    private List<Country> countryList;

    //选中的省份
    private Province selectedProvince;

    //选中的城市
    private City selectedCity;

    //选中的县
    private Country selectedCountry;

    //当前选中的级别
    private int currentLevel;
}
