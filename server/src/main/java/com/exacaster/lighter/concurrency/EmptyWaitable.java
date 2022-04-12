package com.exacaster.lighter.concurrency;

public enum EmptyWaitable implements Waitable {
    INSTANCE {
        @Override
        public void waitCompletion() {
            // nothing to wait
        }
    }
}
