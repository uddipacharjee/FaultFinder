package analysis;

import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

public class Result extends RunNotifier{
	Failure failure;

    @Override
    public void fireTestFailure(Failure failure) {
        this.failure = failure;
    };

    boolean isOK() {
        return failure == null;
    }

    public Failure getFailure() {
        return failure;
    }
}
