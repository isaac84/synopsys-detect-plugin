/**
 * blackduck-detect
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.jenkins.detect.extensions.postbuild;

import java.io.IOException;
import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.synopsys.integration.jenkins.annotations.HelpMarkdown;
import com.synopsys.integration.jenkins.detect.extensions.DetectDownloadStrategy;
import com.synopsys.integration.jenkins.detect.extensions.InheritFromGlobalDownloadStrategy;
import com.synopsys.integration.jenkins.detect.extensions.global.DetectGlobalConfig;
import com.synopsys.integration.jenkins.detect.service.DetectCommandsFactory;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

public class DetectPostBuildStep extends Recorder {
    public static final String DISPLAY_NAME = "Synopsys Detect";

    @HelpMarkdown("The command line options to pass to Synopsys Detect")
    private final String detectProperties;

    @Nullable
    private DetectDownloadStrategy downloadStrategyOverride;

    @DataBoundConstructor
    public DetectPostBuildStep(String detectProperties) {
        this.detectProperties = detectProperties;
    }

    public String getDetectProperties() {
        return detectProperties;
    }

    public DetectDownloadStrategy getDownloadStrategyOverride() {
        return downloadStrategyOverride;
    }

    @DataBoundSetter
    public void setDownloadStrategyOverride(DetectDownloadStrategy downloadStrategyOverride) {
        this.downloadStrategyOverride = downloadStrategyOverride;
    }

    public DetectDownloadStrategy getDefaultDownloadStrategyOverride() {
        return new InheritFromGlobalDownloadStrategy();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    // Freestyle
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        DetectCommandsFactory.fromPostBuild(build, launcher, listener)
            .runDetect(detectProperties, getDetectDownloadStrategy());
        return true;
    }

    private DetectDownloadStrategy getDetectDownloadStrategy() {
        DetectDownloadStrategy detectDownloadStrategy = downloadStrategyOverride;

        if (detectDownloadStrategy == null || detectDownloadStrategy instanceof InheritFromGlobalDownloadStrategy) {
            detectDownloadStrategy = new DetectGlobalConfig().getDownloadStrategy();
        }

        return detectDownloadStrategy;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> implements Serializable {
        private static final long serialVersionUID = 9059602791947799261L;

        public DescriptorImpl() {
            super(DetectPostBuildStep.class);
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return DISPLAY_NAME;
        }

    }

}
