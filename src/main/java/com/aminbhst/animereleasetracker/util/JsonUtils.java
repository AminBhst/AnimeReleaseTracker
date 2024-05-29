package com.aminbhst.animereleasetracker.util;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonUtils {

    public static boolean isNotEmpty(JsonNode node) {
        return node != null && !node.isNull();
    }
}
