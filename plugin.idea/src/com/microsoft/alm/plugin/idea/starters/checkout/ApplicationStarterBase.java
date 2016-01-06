// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.plugin.idea.starters.checkout;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationStarterEx;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.microsoft.alm.common.utils.UrlHelper;
import com.microsoft.alm.plugin.idea.resources.TfPluginBundle;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a new commandline argument to do VSTS checkouts. This will allow for a protocol handler to pass IntelliJ
 * the needed arguments to clone and open a project.
 */
public abstract class ApplicationStarterBase extends ApplicationStarterEx {
    private final Logger logger = LoggerFactory.getLogger(ApplicationStarterBase.class);
    public final String CHECKOUT_COMMAND = "vsts-checkout";

    public abstract String getUsageMessage();

    protected abstract void processCommand(final String[] args, @Nullable String currentDirectory) throws Exception;

    @Override
    public String getCommandName() {
        return CHECKOUT_COMMAND;
    }

    @Override
    public boolean isHeadless() {
        return false;
    }

    private static void saveAll() {
        FileDocumentManager.getInstance().saveAllDocuments();
        ApplicationManager.getApplication().saveSettings();
    }

    /**
     * Checking arguments passed. They should follow the form:
     * "vsts-checkout git_url"
     *
     * @param args the command line args
     * @return whether the arguments given meet the requirements
     */
    protected boolean checkArguments(String[] args) {
        if (args.length != 2) {
            return false;
        } else if (!CHECKOUT_COMMAND.equals(args[0])) {
            return false;
        } else if (!UrlHelper.isGitRemoteUrl(args[1])) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void premain(String[] args) {
        if (!checkArguments(args)) {
            System.err.println(getUsageMessage());
            System.exit(1);
        }
    }

    @Override
    public void main(String[] args) {
        try {
            logger.debug("Trying to checkout VSTS project via the commandline");
            processCommand(args, null);
        } catch (Exception e) {
            logger.error(TfPluginBundle.message(TfPluginBundle.KEY_CHECKOUT_ERRORS_UNEXPECTED, e.getMessage()));
            System.exit(1);
        } catch (Throwable t) {
            logger.error(TfPluginBundle.message(TfPluginBundle.KEY_CHECKOUT_ERRORS_UNEXPECTED, t.getMessage()));
            System.exit(2);
        } finally {
            saveAll();
        }
    }

    @Override
    public boolean canProcessExternalCommandLine() {
        return true;
    }
}
