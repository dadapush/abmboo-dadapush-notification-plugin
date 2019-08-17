package com.dadapush.client.bamboo;

public class DaDaPushConfiguration {

  private String basePath = "https://www.dadapush.com";
  private String channelToken = null;
  private Boolean enable = true;

  public Boolean getEnable() {
    return enable;
  }

  public void setEnable(Boolean enable) {
    this.enable = enable;
  }

  public String getBasePath() {
    return basePath;
  }

  public void setBasePath(String basePath) {
    this.basePath = basePath;
  }

  public String getChannelToken() {
    return channelToken;
  }

  public void setChannelToken(String channelToken) {
    this.channelToken = channelToken;
  }
}
