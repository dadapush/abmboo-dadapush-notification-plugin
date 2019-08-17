package com.dadapush.client.bamboo;

import com.atlassian.bamboo.deployments.notification.DeploymentResultAwareNotificationRecipient;
import com.atlassian.bamboo.deployments.results.DeploymentResult;
import com.atlassian.bamboo.notification.NotificationRecipient;
import com.atlassian.bamboo.notification.NotificationTransport;
import com.atlassian.bamboo.notification.recipients.AbstractNotificationRecipient;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.cache.ImmutablePlan;
import com.atlassian.bamboo.plugin.descriptor.NotificationRecipientModuleDescriptor;
import com.atlassian.bamboo.resultsummary.ResultsSummary;
import com.atlassian.bamboo.template.TemplateRenderer;
import com.atlassian.bamboo.variable.CustomVariableContext;
import com.atlassian.bamboo.variable.substitutor.VariableSubstitutor;
import com.atlassian.bamboo.variable.substitutor.VariableSubstitutorFactory;
import com.atlassian.event.Event;
import com.dadapush.client.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DaDaPushNotificationRecipient extends AbstractNotificationRecipient implements
    DeploymentResultAwareNotificationRecipient,
    NotificationRecipient.RequiresPlan,
    NotificationRecipient.RequiresResultSummary,
    NotificationRecipient.RequiresEvent {

  private static Gson gson = JSON.createGson().create();

  private static String CHANNEL_TOKEN = "channelToken";
  private static String BASE_PATH = "basePath";
  private static String ENABLE = "enable";

  private String basePath = null;
  private String channelToken = null;
  private Boolean enable = true;

  private TemplateRenderer templateRenderer;
  private Event event;
  private ImmutablePlan plan;
  private ResultsSummary resultsSummary;
  private DeploymentResult deploymentResult;
  private CustomVariableContext customVariableContext;

  @Override
  public void populate(@NotNull Map<String, String[]> params) {
    if (params.containsKey(BASE_PATH)) {
      int i = params.get(BASE_PATH).length - 1;
      this.basePath = params.get(BASE_PATH)[i];
    }
    if (params.containsKey(CHANNEL_TOKEN)) {
      int i = params.get(CHANNEL_TOKEN).length - 1;
      this.channelToken = params.get(CHANNEL_TOKEN)[i];
    }
    if (params.containsKey(ENABLE)) {
      int i = params.get(ENABLE).length - 1;
      try {
        this.enable = Boolean.valueOf(Optional.ofNullable(params.get(ENABLE)[i]).orElse("true"));
      } catch (Exception e) {
        this.enable = true;
      }
    }
  }

  @Override
  public void init(@Nullable String configurationData) {
    if (StringUtils.isNotBlank(configurationData)) {
      DaDaPushConfiguration daDaPushConfiguration = gson
          .fromJson(configurationData, DaDaPushConfiguration.class);
      channelToken = daDaPushConfiguration.getChannelToken();
      basePath = daDaPushConfiguration.getBasePath();
      basePath = StringUtils.trimToEmpty(basePath);
      if (StringUtils.isEmpty(basePath)) {
        basePath = "https://www.dadapush.com";
      }
      enable = daDaPushConfiguration.getEnable();
    }
  }

  @NotNull
  @Override
  public String getRecipientConfig() {
    DaDaPushConfiguration daDaPushConfiguration = new DaDaPushConfiguration();
    daDaPushConfiguration.setBasePath(basePath);
    daDaPushConfiguration.setChannelToken(channelToken);
    daDaPushConfiguration.setEnable(enable);
    return gson.toJson(daDaPushConfiguration);
  }

  @NotNull
  @Override
  public String getEditHtml() {
    String editTemplateLocation = ((NotificationRecipientModuleDescriptor) getModuleDescriptor())
        .getEditTemplate();
    return templateRenderer.render(editTemplateLocation, populateContext());
  }

  @NotNull
  @Override
  public String getViewHtml() {
    String editTemplateLocation = ((NotificationRecipientModuleDescriptor) getModuleDescriptor())
        .getViewTemplate();
    return templateRenderer.render(editTemplateLocation, populateContext());
  }

  private Map<String, Object> populateContext() {
    Map<String, Object> context = Maps.newHashMap();

    if (channelToken != null) {
      context.put(CHANNEL_TOKEN, channelToken);
    }
    context.put(BASE_PATH, "https://www.dadapush.com");
    context.put(ENABLE, enable);
    return context;
  }

  @NotNull
  public List<NotificationTransport> getTransports() {
//    List<NotificationTransport> list = Lists.newArrayList();
//    list.add(new DaDaPushNotificationTransport(basePath, channelToken, plan, resultsSummary,
//        deploymentResult, customVariableContext));
//    return list;

    final List<NotificationTransport> list = Lists.newArrayList();

    final VariableSubstitutorFactory variableSubstitutorFactory = customVariableContext
        .getVariableSubstitutorFactory();
    final VariableSubstitutor variableSubstitutor = plan != null
        ? variableSubstitutorFactory.newSubstitutorForPlan(plan)
        : variableSubstitutorFactory.newSubstitutorForGlobalContext();

    customVariableContext.withVariableSubstitutor(variableSubstitutor, () -> {
      list.add(
          new DaDaPushNotificationTransport(
              customVariableContext.substituteString(basePath),
              customVariableContext.substituteString(channelToken),
              enable, resultsSummary, deploymentResult));
    });

    return list;
  }

  public void setPlan(@Nullable final Plan plan) {
    this.plan = plan;
  }

  public void setPlan(@Nullable final ImmutablePlan plan) {
    this.plan = plan;
  }

  public void setDeploymentResult(@Nullable final DeploymentResult deploymentResult) {
    this.deploymentResult = deploymentResult;
  }

  public void setResultsSummary(@Nullable final ResultsSummary resultsSummary) {
    this.resultsSummary = resultsSummary;
  }

  public void setTemplateRenderer(TemplateRenderer templateRenderer) {
    this.templateRenderer = templateRenderer;
  }

  public void setCustomVariableContext(CustomVariableContext customVariableContext) {
    this.customVariableContext = customVariableContext;
  }

  @Override
  public void setEvent(@Nullable Event event) {
    this.event = event;
  }
}
