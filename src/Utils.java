public class Utils {
    public static String SERVER_IP = "localhost";
    public static int PORT = 9876;

    public static String getRequestHeader(String request) {
        return request.split(":")[0].toLowerCase().trim();
    }

    public static String[] getRequestParams(String request) {
        String paramsString = request.replace(getRequestHeader(request), "")
                        .replace(":", "")
                        .trim();

        if(paramsString.equals("")) {
            return new String[]{};
        }
        return paramsString.split(",");
    }
}
