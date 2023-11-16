package com.exacaster.lighter.application.sessions;

import com.exacaster.lighter.application.Application;
import com.exacaster.lighter.application.SubmitParams;

public class PermanentSession {
    private final Application application;
    private final boolean isDeleted;

    public PermanentSession(Application application, boolean isDeleted) {
        this.application = application;
        this.isDeleted = isDeleted;
    }

    public Application getApplication() {
        return application;
    }

    public boolean isNotDeleted() {
        return !isDeleted;
    }

    public SubmitParams getSubmitParameters(){
        return this.getApplication().getSubmitParams();
    }
}
