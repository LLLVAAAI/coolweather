package com.coolweather.android.db;

import org.litepal.crud.DataSupport;

public class Province extends DataSupport {

    private int id;

    private String provinceNmae;

    private int provinceCode;     //省的代号

    public int getId(){
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public String getProvinceNmae() {
        return provinceNmae;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }

    public void setProvinceNmae(String provinceNmae) {
        this.provinceNmae = provinceNmae;
    }
}
