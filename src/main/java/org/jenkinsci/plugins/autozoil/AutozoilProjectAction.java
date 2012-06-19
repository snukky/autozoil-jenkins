package org.jenkinsci.plugins.autozoil;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Result;
import org.jenkinsci.plugins.autozoil.util.AbstractAutozoilProjectAction;

/**
 * @author Gregory Boissinot
 */
public class AutozoilProjectAction extends AbstractAutozoilProjectAction {

    public String getSearchUrl() {
        return getUrlName();
    }

    public AutozoilProjectAction(final AbstractProject<?, ?> project) {
        super(project);
    }

    public AbstractBuild<?, ?> getLastFinishedBuild() {
        AbstractBuild<?, ?> lastBuild = project.getLastBuild();
        while (lastBuild != null && (lastBuild.isBuilding() || lastBuild.getAction(AutozoilBuildAction.class) == null)) {
            lastBuild = lastBuild.getPreviousBuild();
        }
        return lastBuild;
    }

    @SuppressWarnings("unused")
    public final boolean isDisplayGraph() {
        //Latest
        AbstractBuild<?, ?> b = getLastFinishedBuild();
        if (b == null) {
            return false;
        }

        //Affect previous
        b = b.getPreviousBuild();
        if (b != null) {

            for (; b != null; b = b.getPreviousBuild()) {
                if (b.getResult().isWorseOrEqualTo(Result.FAILURE)) {
                    continue;
                }
                AutozoilBuildAction action = b.getAction(AutozoilBuildAction.class);
                if (action == null || action.getResult() == null) {
                    continue;
                }
                AutozoilResult result = action.getResult();
                if (result == null)
                    continue;

                return true;
            }
        }
        return false;
    }

    public Integer getLastResultBuild() {
        for (AbstractBuild<?, ?> b = project.getLastBuild(); b != null; b = b.getPreviousBuiltBuild()) {
            AutozoilBuildAction r = b.getAction(AutozoilBuildAction.class);
            if (r != null)
                return b.getNumber();
        }
        return null;
    }


    public String getDisplayName() {
        return "Autozoil Results";
    }

    public String getUrlName() {
        return AutozoilBuildAction.URL_NAME;
    }
}
