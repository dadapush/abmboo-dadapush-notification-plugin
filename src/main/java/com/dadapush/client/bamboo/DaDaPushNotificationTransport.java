package com.dadapush.client.bamboo;

import com.atlassian.bamboo.builder.BuildState;
import com.atlassian.bamboo.builder.LifeCycleState;
import com.atlassian.bamboo.deployments.results.DeploymentResult;
import com.atlassian.bamboo.notification.Notification;
import com.atlassian.bamboo.notification.NotificationTransport;
import com.atlassian.bamboo.resultsummary.ResultsSummary;
import com.dadapush.client.ApiClient;
import com.dadapush.client.ApiException;
import com.dadapush.client.Configuration;
import com.dadapush.client.api.DaDaPushMessageApi;
import com.dadapush.client.model.MessagePushRequest;
import com.dadapush.client.model.ResultOfMessagePushResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DaDaPushNotificationTransport implements NotificationTransport {

  public static final String STATE_UNKNOWN = "unknown";
  public static final String STATE_FAILED = "failed";
  public static final String STATE_SUCCESSFUL = "successful";
  public static final String STATE_IN_PROGRESS = "in progress";
  private static final Logger log = Logger.getLogger(DaDaPushNotificationTransport.class);
  private final String basePath;
  private final String channelToken;
  private final Boolean enable;
  @Nullable
  private final ResultsSummary resultsSummary;
  @Nullable
  private final DeploymentResult deploymentResult;
  private DaDaPushMessageApi apiInstance;

  public DaDaPushNotificationTransport(String basePath,
      String channelToken,
      Boolean enable,
      @Nullable ResultsSummary resultsSummary,
      @Nullable DeploymentResult deploymentResult) {
    this.basePath = basePath;
    this.channelToken = channelToken;
    this.enable = enable;
    this.resultsSummary = resultsSummary;
    this.deploymentResult = deploymentResult;

    ApiClient apiClient = Configuration.getDefaultApiClient();
    if (StringUtils.isNotEmpty(basePath)) {
      apiClient.setBasePath(basePath);
    }
    apiInstance = new DaDaPushMessageApi(apiClient);
  }

  public void sendNotification(@NotNull Notification notification) {
    if (!enable) {
      log.warn("disable DaDaPush Notification");
      return;
    }
    try {
      String title = notification.getDescription();
      String content = notification.getTextEmailContent();
      MessagePushRequest body = new MessagePushRequest();
      body.setTitle(StringUtils.substring(title, 0, 50));
      body.setContent(StringUtils.substring(content, 0, 500));
      body.setNeedPush(true);
      ResultOfMessagePushResponse result = apiInstance.createMessage(body, channelToken);
      if (result.getCode() == 0) {
        log
            .info("send notification success, messageId=" + result.getData().getMessageId());
      } else {
        log.warn(
            "send notification fail, detail: " + result.getCode() + " " + result.getErrmsg());
      }
    } catch (ApiException e) {
      log.error("send notification fail", e);
    } catch (Exception e) {
      log.error("send notification fail", e);
    }
  }

  private String getState(ResultsSummary result) {
    String color = STATE_UNKNOWN;
    if (result.getBuildState() == BuildState.FAILED) {
      color = STATE_FAILED;
    } else if (result.getBuildState() == BuildState.SUCCESS) {
      color = STATE_SUCCESSFUL;
    } else if (LifeCycleState.isActive(result.getLifeCycleState())) {
      color = STATE_IN_PROGRESS;
    }
    return color;
  }

  private String getState(DeploymentResult deploymentResult) {
    String color = STATE_UNKNOWN;
    if (deploymentResult.getDeploymentState() == BuildState.FAILED) {
      color = STATE_FAILED;
    } else if (deploymentResult.getDeploymentState() == BuildState.SUCCESS) {
      color = STATE_SUCCESSFUL;
    } else if (LifeCycleState.isActive(deploymentResult.getLifeCycleState())) {
      color = STATE_IN_PROGRESS;
    }
    return color;
  }

}
