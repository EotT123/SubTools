package extensions.org.json.JSONArray;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.experimental.UtilityClass;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import org.json.JSONArray;
import org.json.JSONObject;

@Extension
@UtilityClass
public class JSONArrayExt {

    public static Stream<JSONObject> stream(@This JSONArray jsonArray) {
        return StreamSupport
                .stream(Spliterators.spliteratorUnknownSize((Iterator<JSONObject>) (Iterator) jsonArray.iterator(), Spliterator.ORDERED), false);
    }
}