package com.github.mdsina.graaljs.executorwebservice.spring.context;

import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

@RequiredArgsConstructor
public class ScriptScopeBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    private final RandomDataGenerator randomDataGenerator;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        beanFactory.registerScope(ScriptExecutionScope.ID, new ScriptExecutionScope(randomDataGenerator));
    }
}
