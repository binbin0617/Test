package com.bin.mylibrary.entity;

import java.util.List;

public class PhotoQRCode {
    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public List<Qrcode> getQrcodeArray() {
        return qrcodeArray;
    }

    public void setQrcodeArray(List<Qrcode> qrcodeArray) {
        this.qrcodeArray = qrcodeArray;
    }

    private String photo;
    private List<Qrcode> qrcodeArray;

    public static class Qrcode {
        public String getQrcode() {
            return qrcode;
        }

        public void setQrcode(String qrcode) {
            this.qrcode = qrcode;
        }

        private String qrcode;
    }
}
