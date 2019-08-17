Atlassian Bamboo DaDaPush Notification Plugin
=========

A simple notification plugin that allows using a DaDaPush as Notification Recipient.

Installation
---------
* Setup [Atlassian SDK](https://developer.atlassian.com/docs/getting-started/set-up-the-atlassian-plugin-sdk-and-build-a-project)
* Build plugin
```
$ git clone https://github.com/dadapush/bamboo-dadapush-notification-plugin.git
$ cd bamboo-dadapush-notification-plugin
$ mvn package
```
* Install plugin into Bamboo
* Setup Notifications for Build
  * Event: *All Builds Completed*
  * Recipient type: *DaDaPush*
  * Base Path: *https://www.dadapush.com*
  * Channel Token: *channel token from https://www.dadapush.com/channel/list*
