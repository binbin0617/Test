package com.bin.mylibrary.entity;

/**
 * 用户信息
 * Created by zhaolei on 2017/10/10.
 */

public class Userinfo {
    // 用户ID
    private String uuid;
    // 用户名
    private String uname;
    // 微信ID
    private String wxid;
    // QQID
    private String qqid;
    // 支付宝ID
    private String zfbid;
    // 手机号码
    private String mobile;
    // 工号或学号
    private String rybh;
    // 密码
    private String pword;
    // 用户类型
    private String yhlx;
    // 学校代码
    private String school;
    // 部门编号
    private String bmbh;
    // 部门名称
    private String bmmc;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getWxid() {
        return wxid;
    }

    public void setWxid(String wxid) {
        this.wxid = wxid;
    }

    public String getQqid() {
        return qqid;
    }

    public void setQqid(String qqid) {
        this.qqid = qqid;
    }

    public String getZfbid() {
        return zfbid;
    }

    public void setZfbid(String zfbid) {
        this.zfbid = zfbid;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getRybh() {
        return rybh;
    }

    public void setRybh(String rybh) {
        this.rybh = rybh;
    }

    public String getPword() {
        return pword;
    }

    public void setPword(String pword) {
        this.pword = pword;
    }

    public String getYhlx() {
        return yhlx;
    }

    public void setYhlx(String yhlx) {
        this.yhlx = yhlx;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getBmbh() {
        return bmbh;
    }
    public void setBmbh(String bmbh) {
        this.bmbh = bmbh;
    }
    public String getBmmc() {
        return bmmc;
    }
    public void setBmmc(String bmmc) {
        this.bmmc = bmmc;
    }
}
