package ru.inn.httpserver.system.enums;

/**
 * @overview Routes
 */
public enum Routes {

    DEFAULT("default", "Unknown"),

    TRACKING("trackings", "Operation success"),

    UNKNOWN("unknown", "Unknown");

    private String route;
    private String response;

    private Routes(String route, String response) {
        this.route = route;
        this.response = response;
    }

    public static Routes fromString(String route) {
        if (route != null) {
            for (Routes r : Routes.values()) {
                if (r.route.equals(route)) {
                    return r;
                }
            }
        }
        return UNKNOWN;
    }

    public String getRouteName() {
        return route;
    }

    public String getResponseText() {
        return response;
    }

}
