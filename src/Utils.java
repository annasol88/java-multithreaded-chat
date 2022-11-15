public class Utils {
    public static String getRequestHeader(String request) {
        return request.split(":")[0].toLowerCase().trim();
    }

    public static String[] getRequestParams(String request) {
        String paramsString = request.replace(getRequestHeader(request), "")
                        .replace(":", "")
                        .trim();

        return paramsString.split(",");
    }
}
