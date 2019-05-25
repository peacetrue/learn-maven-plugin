package com.github.peacetrue.learn;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.github.peacetrue.modelgenerator.ModelGenerator;
import com.github.peacetrue.modelgenerator.ModelSupplier;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "generator")
@SpringBootApplication
public class Generator extends AbstractMojo {


    /** 配置文件路径 */
    @Parameter(defaultValue = "file:${project.build.resources[0].directory}/application-datasource.properties")
    private String propertiesPath;

    public void execute() throws MojoExecutionException {
        execute(propertiesPath);
    }

    private static void execute(String propertiesPath) {
        SpringApplication springApplication = new SpringApplication(new DefaultResourceLoader(), Generator.class);
        springApplication.setEnvironment(new StandardEnvironment() {
            @Override
            protected void customizePropertySources(MutablePropertySources propertySources) {
                super.customizePropertySources(propertySources);
                try {
                    ResourcePropertySource propertySource = new ResourcePropertySource(springApplication.getResourceLoader().getResource(propertiesPath));
                    propertySources.addLast(propertySource);
                } catch (IOException e) {
                    throw new IllegalArgumentException(String.format("读取配置[%s]异常", propertiesPath), e);
                }
            }
        });
        ConfigurableApplicationContext context = springApplication.run();
        ModelGenerator modelGenerator = context.getBean(ModelGenerator.class);
        ModelSupplier modelSupplier = context.getBean(ModelSupplier.class);
        modelSupplier.getModels().forEach(modelGenerator::generate);
    }
}
