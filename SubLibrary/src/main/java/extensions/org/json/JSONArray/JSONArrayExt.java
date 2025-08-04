package extensions.org.json.JSONArray;

import java.util.Iterator;
import java.util.stream.Stream;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import org.json.JSONArray;
import org.json.JSONObject;

@Extension
public class JSONArrayExt {

    private JSONArrayExt() {
        // hide utility class constructor
    }

    public static Stream<JSONObject> streamJsonObjects(@This JSONArray jsonArray) {
        Iterator<JSONObject> iterator = (Iterator<JSONObject>) (Iterator) jsonArray.iterator();
        return iterator.stream();
    }
}