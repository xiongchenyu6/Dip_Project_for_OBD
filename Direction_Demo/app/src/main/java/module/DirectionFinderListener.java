package module;

/**
 * Created by seanh on 27/9/2016.
 */
import java.util.List;

public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);
}

