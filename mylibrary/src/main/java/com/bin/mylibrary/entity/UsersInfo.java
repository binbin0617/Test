package com.bin.mylibrary.entity;

/**
 * 获取服务器随机数返回的Json解析
 */
public class UsersInfo {
    /**
     * name : 张三
     * idType : 0
     * idNo : 12010519990101001
     * phoneNo : 15555555555
     * data : {"businessRunningNo":"20160405171342795O0HYPOQ0","createDate":"2016-07-04 14:45:23","service":"mobile.HKESDK.sign","attach":"testtest","certSN":"2000065625","businessText":"fromAccount=1234567890123456&payeeName=吴凡&receivingBank=2&payeeAccount=6543********3210&payeePhoneNo=6543********3210&amount=999.9&remark=OK"}
     */

    private String name;
    private String idType;
    private String idNo;
    private String phoneNo;
    private DataBean data;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * businessRunningNo : 20160405171342795O0HYPOQ0
         * createDate : 2016-07-04 14:45:23
         * service : mobile.HKESDK.sign
         * attach : testtest
         * certSN : 2000065625
         * businessText : fromAccount=1234567890123456&payeeName=吴凡&receivingBank=2&payeeAccount=6543********3210&payeePhoneNo=6543********3210&amount=999.9&remark=OK
         */

        private String businessRunningNo;
        private String createDate;
        private String service;
        private String attach;
        private String certSN;
        private String usePIN;

        public String getUsePIN() {
            return usePIN;
        }

        public void setUsePIN(String usePIN) {
            this.usePIN = usePIN;
        }

        private String businessText;

        @Override
        public String toString() {
            return "{" +
                    "\"businessRunningNo\"" + ":" + "\"" + businessRunningNo + "\"" +","+
                    "\"createDate\"" + ":" + "\"" + createDate + "\"" +","+
                    "\"service\"" + ":" + "\"" + service + "\"" +","+
                    "\"attach\"" + ":" + "\"" + attach + "\"" +","+
                    "\"certSN\"" + ":" + "\"" + certSN + "\"" +","+
                    "\"usePIN\"" + ":" + "\"" + usePIN + "\"" +","+
                    "\"businessText\"" + ":" + "\"" + businessText + "\"" +
                    "}";
        }

        public String getBusinessRunningNo() {
            return businessRunningNo;
        }

        public void setBusinessRunningNo(String businessRunningNo) {
            this.businessRunningNo = businessRunningNo;
        }

        public String getCreateDate() {
            return createDate;
        }

        public void setCreateDate(String createDate) {
            this.createDate = createDate;
        }

        public String getService() {
            return service;
        }

        public void setService(String service) {
            this.service = service;
        }

        public String getAttach() {
            return attach;
        }

        public void setAttach(String attach) {
            this.attach = attach;
        }

        public String getCertSN() {
            return certSN;
        }

        public void setCertSN(String certSN) {
            this.certSN = certSN;
        }

        public String getBusinessText() {
            return businessText;
        }

        public void setBusinessText(String businessText) {
            this.businessText = businessText;
        }
    }


}
