package io.github.toolazytoname.xiaohei;
import java.util.Map;
/** Closed low-risk media controls; session playback is intentionally absent. */
final class MediaControlPolicy {
 enum Operation { OBSERVE_ROUTE, VOLUME_UP, VOLUME_DOWN }
 static Operation parse(Map<String,String> v){if(v==null||v.size()!=1)return null;String op=v.get("operation");if("observe_route".equals(op))return Operation.OBSERVE_ROUTE;if("volume_up".equals(op))return Operation.VOLUME_UP;if("volume_down".equals(op))return Operation.VOLUME_DOWN;return null;}
}
