/*
 * blackduck-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.jenkins.detect.service;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.jenkins.detect.extensions.global.DetectGlobalConfig;
import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;
import com.synopsys.integration.jenkins.service.JenkinsConfigService;
import com.synopsys.integration.jenkins.wrapper.JenkinsProxyHelper;
import com.synopsys.integration.jenkins.wrapper.JenkinsVersionHelper;
import com.synopsys.integration.jenkins.wrapper.SynopsysCredentialsHelper;
import com.synopsys.integration.util.IntEnvironmentVariables;

public class DetectEnvironmentService {
    private final JenkinsIntLogger logger;
    private final JenkinsProxyHelper jenkinsProxyHelper;
    private final JenkinsVersionHelper jenkinsVersionHelper;
    private final SynopsysCredentialsHelper synopsysCredentialsHelper;
    private final Map<String, String> environmentVariables;
    private final JenkinsConfigService jenkinsConfigService;

    public DetectEnvironmentService(JenkinsIntLogger logger, JenkinsProxyHelper jenkinsProxyHelper, JenkinsVersionHelper jenkinsVersionHelper, SynopsysCredentialsHelper synopsysCredentialsHelper, JenkinsConfigService jenkinsConfigService,
        Map<String, String> environmentVariables) {
        this.logger = logger;
        this.jenkinsProxyHelper = jenkinsProxyHelper;
        this.jenkinsVersionHelper = jenkinsVersionHelper;
        this.jenkinsConfigService = jenkinsConfigService;
        this.synopsysCredentialsHelper = synopsysCredentialsHelper;
        this.environmentVariables = environmentVariables;
    }

    public IntEnvironmentVariables createDetectEnvironment() {
        IntEnvironmentVariables intEnvironmentVariables = IntEnvironmentVariables.empty();
        intEnvironmentVariables.putAll(environmentVariables);
        logger.setLogLevel(intEnvironmentVariables);

        populateAllBlackDuckEnvironmentVariables(intEnvironmentVariables::put);

        Optional<String> pluginVersion = jenkinsVersionHelper.getPluginVersion("blackduck-detect");
        if (pluginVersion.isPresent()) {
            logger.info("Running Synopsys Detect for Jenkins version: " + pluginVersion.get());
        } else {
            logger.info("Running Synopsys Detect for Jenkins");
        }

        return intEnvironmentVariables;
    }

    private void populateAllBlackDuckEnvironmentVariables(BiConsumer<String, String> environmentPutter) {
        Optional<DetectGlobalConfig> detectGlobalConfig = jenkinsConfigService.getGlobalConfiguration(DetectGlobalConfig.class);
        if (!detectGlobalConfig.isPresent()) {
            return;
        }

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = detectGlobalConfig.get().getBlackDuckServerConfigBuilder(jenkinsProxyHelper, synopsysCredentialsHelper);

        blackDuckServerConfigBuilder.getProperties()
            .forEach((builderPropertyKey, propertyValue) -> acceptIfNotNull(environmentPutter, builderPropertyKey.getKey(), propertyValue));
    }

    private void acceptIfNotNull(BiConsumer<String, String> environmentPutter, String key, String value) {
        if (StringUtils.isNoneBlank(key, value)) {
            environmentPutter.accept(key, value);
        }
    }

}
