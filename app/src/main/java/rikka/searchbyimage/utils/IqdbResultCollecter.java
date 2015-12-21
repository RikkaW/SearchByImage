package rikka.searchbyimage.utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Rikka on 2015/12/20.
 */
public class IqdbResultCollecter {
    public static class IqdbItem {
        public String thumbnailURL;
        public String imageURL;
        public String size;
        public String similarity;

        IqdbItem() {

        }

        IqdbItem(String thumbnailURL, String imageURL, String size, String similarity) {
            this.thumbnailURL = thumbnailURL;
            this.imageURL = imageURL;
            this.size = size;
            this.similarity = similarity;
        }
    }

    private static final String FIND_HEAD[] = {
            "<div><table><tr><th>Best match</th></tr>",
            "<div><table><tr><th>Additional match</th></tr>",
            "<div><table><tr><th>Possible match</th></tr>",
            "<div><table><tr><td class='image'>"
    };
    private static final String FIND_END = "similarity</td></tr></table></div>";

    private static final String URL_HEAD = "<td class='image'><a href=\"";
    private static final String URL_END = "\">";

    private static final String THUMBNAIL_HEAD = "<img src='";
    private static final String THUMBNAIL_END = "'";


    public static ArrayList<IqdbItem> getItemList(String string) {
        ArrayList<IqdbItem> list = new ArrayList<>();

        int findStart = 0, findEnd = 0;

        for (String aFIND_HEAD : FIND_HEAD) {
            findStart = string.indexOf(aFIND_HEAD, findStart);

            while (findStart != -1)
            {
                String lineString = string.substring(findStart, string.indexOf(FIND_END, findStart) + "similarity".length());

                IqdbItem item = new IqdbItem();

                findStart = string.indexOf(URL_HEAD, findStart) + URL_HEAD.length();
                findEnd = string.indexOf(URL_END, findStart);
                item.imageURL = fixURL(string.substring(findStart, findEnd));

                findStart = string.indexOf(THUMBNAIL_HEAD, findStart) + THUMBNAIL_HEAD.length();
                findEnd = string.indexOf(THUMBNAIL_END, findStart);
                item.thumbnailURL = "http://iqdb.org" + string.substring(findStart, findEnd);

                Pattern r;
                Matcher m;

                r = Pattern.compile("[0-9]\\d+×[0-9]\\d+");
                m = r.matcher(lineString);
                if (m.find()) {
                    item.size = m.group(0);
                }

                r = Pattern.compile("[0-9]\\d{1,3}% similarity");
                m = r.matcher(lineString);
                if (m.find()) {
                    item.similarity = m.group(0).substring(0, m.group(0).length() - " similarity".length());
                }

                list.add(item);

                findStart = string.indexOf(aFIND_HEAD, findStart);
            }
        }

        return list;
    }

    private static String fixURL(String URL) {
        if (!URL.startsWith("http") && !URL.startsWith("https")) {
            return "http:" + URL;
        }

        return URL;
    }
}
