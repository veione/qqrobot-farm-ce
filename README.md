# QQ机器人 农场版

CE = Community Edition

这是[https://github.com/kosaka-bun/honoka-docs/blob/master/qqrobot/qqrobot-farm/main.md](https://github.com/kosaka-bun/honoka-docs/blob/master/qqrobot/qqrobot-farm/main.md)中所提到的QQ机器人的所有可公开的源代码。

请仿照/src/main/java/resources/config/application-dev.yml文件中的配置，另写一个用于正式环境的配置文件，以完成部署。

项目使用jpa来自动创建数据库表，不需要事先在数据库中执行SQL文件。