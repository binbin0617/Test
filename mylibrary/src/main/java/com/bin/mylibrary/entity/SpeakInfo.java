package com.bin.mylibrary.entity;

import java.util.List;

public class SpeakInfo {

    private List<HelpListBean> helpList;

    public List<HelpListBean> getHelpList() {
        return helpList;
    }

    public void setHelpList(List<HelpListBean> helpList) {
        this.helpList = helpList;
    }

    public static class HelpListBean {
        /**
         * ljdz : https://www.baidu.com/
         * wdmc : 网上报销帮助文档
         */

        private String ljdz;
        private String wdmc;
        private String by1;

        public String getBy1() {
            return by1;
        }

        public void setBy1(String by1) {
            this.by1 = by1;
        }

        public String getLjdz() {
            return ljdz;
        }

        public void setLjdz(String ljdz) {
            this.ljdz = ljdz;
        }

        public String getWdmc() {
            return wdmc;
        }

        public void setWdmc(String wdmc) {
            this.wdmc = wdmc;
        }

    }
}
