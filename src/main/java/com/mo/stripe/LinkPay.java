package com.mo.stripe;

public class LinkPay {
    private String url;

    public LinkPay(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
