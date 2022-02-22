import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class HttpsClient{


    public static String call(String https_url){
        URL url;
        try {
            url = new URL(https_url);
            URLConnection con = url.openConnection();
            return print_content(con);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    private static String print_content(URLConnection con){
        String toReturn = "";
        if(con!=null){
            try {
                BufferedReader br =
                        new BufferedReader(
                                new InputStreamReader(con.getInputStream()));

                String input;

                while ((input = br.readLine()) != null){
                    toReturn+= input;
                }
                br.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return toReturn;

    }

}