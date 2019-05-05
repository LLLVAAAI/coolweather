package com.coolweather.android.util;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.android.R;
import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 由于遍历省市县的功能在后面还会服用，因此不写在活动里面，而是写在碎片里面，
 * 这样在需要的时候直接在布局里面引用碎片就可以了
 * 当然碎片也不能直接显示在界面上，需要将其添加到活动中去
 */
public class ChooseAreaFragment extends Fragment {

    private static final String TAG = "ChooseAreaFragment";

    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;

    public static final int LEVEL_COUNTY = 2;

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
    private List<County> countyList;

    //选中的省份
    private Province selectedProvince;

    //选中的城市
    private City selectedCity;

    //选中的县
    private County selectedCounty;

    //当前选中的级别
    private int currentLevel;

    /**
     * 获取到一些控件的实例，初始化ArrayAdapter，并将其设置为ListView的适配器
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*
         * 【LayoutInflater】其实是在res/layout/下找到xml布局文件，并且将其实例化，
         * 对于一个没有被载入或者想要动态载入的界面，都需要使用LayoutInflater.inflate()来载入；
         * */
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        //载入listView
        listView.setAdapter(adapter);
        return view;
    }

    /**
     * 给listView和button设置点击事件
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //对列表设置监听事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel == LEVEL_PROVINCE){
                    //记住选中的省份
                    selectedProvince = provinceList.get(position);
                    //显示出省份对应下city的界面
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    //记住选中的City
                    selectedCity = cityList.get(position);
                    //切换到相应的county界面
                    queryCounties();
                }
            }
        });
        //为返回按钮注册监听事件
        backButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //若在county切换到City
                if(currentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    //若在City切换到province
                    queryProvinces();
                }
            }
        });
        //初始状态下显示province
        queryProvinces();
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务区查询
     */
    private void queryProvinces(){
        
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);         //显示省份列表时，不需要返回键，设置不可见
        provinceList = DataSupport.findAll(Province.class);         //首先从数据库开始查询

        if (provinceList.size() > 0){
            dataList.clear();
            for (Province province : provinceList){
                dataList.add(province.getProvinceNmae());    //如果查到就将省份的名称放入dataList，用于在listView 进行显示
            }
            adapter.notifyDataSetChanged();                   //notifyDataSetChanged方法通过一个外部的方法控制
                                                              // 如果适配器的内容改变时需要强制调用getView来刷新每个Item的内容。
            listView.setSelection(0);                         //设置第一个省份数据从 listView 的第一栏开始显示
            currentLevel = LEVEL_PROVINCE;

        } else {        //否则即没能在数据库中找到省份的数据
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }

    /**
     * 查询选中的省内所有的市，先从数据库中查询，如果没有查询到再去服务器上查询
     */
    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceNmae());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);

        if (cityList.size() > 0){
            dataList.clear();
            for (City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {

            String address = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode();
            queryFromServer(address,"city");
        }
    }

    /**
     * 查询选中市内所有的县，优先从数据库中查询，如果没有查询到再去服务器中查询
     */
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);

        if (countyList.size() > 0){
            dataList.clear();
            for (County county : countyList){
                dataList.add(county.getCountryName());
            }
            adapter.notifyDataSetChanged();        //notifyDataSetChanged方法通过一个外部的方法控制
                                                  // 如果适配器的内容改变时需要强制调用getView来刷新每个Item的内容。
            listView.setSelection(0);             //设置第一个省份数据从 listView 的第一栏开始显示
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address,"county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     */
    private void queryFromServer(String address, final String type){
        showProgressDialog();
        
        HttpUtil.sendOkHttpRequest(address, new Callback() {      //向服务器发送请求

            @Override
            public void onResponse(Call call, Response response) throws IOException {       //服务器响应的数据会回调到此方法

                String responseText = response.body().string();
                boolean result = false;
                
                if ("province".equals(type)){
                    result = Utility.handleProviceResponse(responseText);           //解析和处理数据
                } else if ("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                } else if ("county".equals(type)){
                    result = Utility.handleCountryResponse(responseText,selectedCity.getId());
                }
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            } else if ("city".equals(type)){
                                queryCities();
                            } else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

            //由于query...()方法牵扯到UI操作，必须在主线程中调用
            @Override
            public void onFailure(Call call, IOException e) {
                //通过runOnUiThread() 方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    /**
     * 显示进度对话框
     */
    private void showProgressDialog(){
        if (progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog(){
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
