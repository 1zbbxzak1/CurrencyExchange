package ru.julia.currencyexchange.infrastructure.configuration;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class MessageConfig {
    @Bean
    public ResourceBundleMessageSource messageSource() {
        YamlPropertiesFactoryBean propertiesFactoryBean = new YamlPropertiesFactoryBean();
        propertiesFactoryBean.setResources(new ClassPathResource("bot/message.yml"));

        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setCommonMessages(propertiesFactoryBean.getObject());

        return messageSource;
    }
}
