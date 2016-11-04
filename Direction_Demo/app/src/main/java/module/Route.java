package module;

/**
 * Created by seanh on 27/9/2016.
 */
import com.google.android.gms.maps.model.LatLng;

import java.util.List;


public class Route {
    public Distance distance;
    public Duration duration;
    public String endAddress;
    public LatLng endLocation;
    public String startAddress;
    public LatLng startLocation;
    public List<String> instructions;
    public String polyline;
    public LatLng step_startLocation;
    public LatLng step_endLocation;
    public Distance step_distance;

    public List<LatLng> points;
}

