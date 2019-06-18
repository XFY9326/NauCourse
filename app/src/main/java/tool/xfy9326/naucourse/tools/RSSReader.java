package tool.xfy9326.naucourse.tools;

import android.util.Xml;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.xmlpull.v1.XmlPullParser;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class RSSReader {
    private static final String RSS_TAG_CHANNEL = "channel";
    private static final String RSS_TAG_TITLE = "title";
    private static final String RSS_TAG_LINK = "link";
    private static final String RSS_TAG_ITEM = "item";
    @SuppressWarnings("unused")
    private static final String RSS_TAG_DESCRIPTION = "description";
    private static final String RSS_TAG_DATE = "date";
    @SuppressWarnings("unused")
    private static final String RSS_TAG_TYPE = "type";
    private static final String RSS_TAG = "rss";

    @SuppressWarnings("ConstantConditions")
    @Nullable
    public static RSSObject getRSSObject(@NonNull String rssContent) {
        XmlPullParser parser = Xml.newPullParser();
        rssContent = rssContent.replaceAll("<!-[\\s\\S]*?->", "");
        StringReader stringReader = new StringReader(rssContent);
        try {
            parser.setInput(stringReader);
            int eventType = parser.getEventType();

            RSSObject rssObject = null;

            ArrayList<RSSChannel> rssChannelArrayList = new ArrayList<>();
            ArrayList<RSSItem> rssItemArrayList = new ArrayList<>();

            boolean startChannel = false;
            boolean startItem = false;

            String title = null;
            String link = null;
            String description = null;
            String date = null;
            String type = null;
            String channelTitle = null;
            String channelLink = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                if (eventType == XmlPullParser.START_TAG) {
                    switch (tagName) {
                        case RSS_TAG_CHANNEL:
                            startChannel = true;
                            break;
                        case RSS_TAG_ITEM:
                            startItem = true;
                            break;
                        case RSS_TAG_TITLE:
                            if (startChannel && !startItem) {
                                channelTitle = parser.nextText();
                            } else {
                                title = parser.nextText().trim();
                                if (title.startsWith("【")) {
                                    type = title.substring(1, title.indexOf("】"));
                                    title = title.substring(title.indexOf("】") + 1);
                                } else if (title.startsWith("[")) {
                                    type = title.substring(1, title.indexOf("]"));
                                    title = title.substring(title.indexOf("]") + 1);
                                }
                            }
                            break;
                        case RSS_TAG_LINK:
                            if (startChannel && !startItem) {
                                channelLink = parser.nextText();
                            } else {
                                link = parser.nextText();
                            }
                            break;
                        case RSS_TAG_DATE:
                            date = parser.nextText();
                            if (date.contains("T")) {
                                date = date.substring(0, date.indexOf("T"));
                            }
                            break;
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    switch (tagName) {
                        case RSS_TAG_CHANNEL:
                            if (startChannel) {
                                startChannel = false;
                                rssChannelArrayList.add(new RSSChannel(channelTitle, channelLink, rssItemArrayList));
                                channelTitle = null;
                                channelLink = null;
                                rssItemArrayList.clear();
                            }
                            break;
                        case RSS_TAG_ITEM:
                            if (startItem) {
                                startItem = false;
                                rssItemArrayList.add(new RSSItem(title, link, description, date, type));
                                title = null;
                                link = null;
                                description = null;
                                date = null;
                                type = null;
                            }
                            break;
                        case RSS_TAG:
                            rssObject = new RSSObject(rssChannelArrayList);
                            rssChannelArrayList.clear();
                            break;
                    }
                }
                eventType = parser.next();
            }
            return rssObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class RSSObject implements Serializable {
        private final List<RSSChannel> channels;

        RSSObject(List<RSSChannel> channels) {
            this.channels = new ArrayList<>();
            this.channels.addAll(channels);
        }

        public List<RSSChannel> getChannels() {
            return this.channels;
        }
    }

    @SuppressWarnings("unused")
    public static class RSSChannel implements Serializable {
        private final String title;
        private final String link;
        private final List<RSSItem> items;

        RSSChannel(String title, String link, List<RSSItem> items) {
            this.title = title;
            this.link = link;
            this.items = new ArrayList<>();
            this.items.addAll(items);
        }

        String getTitle() {
            return title;
        }

        String getLink() {
            return link;
        }

        public List<RSSItem> getItems() {
            return items;
        }
    }

    @SuppressWarnings("unused")
    public static class RSSItem implements Serializable {
        private final String title;
        private final String link;
        private final String description;
        private final String date;
        private final String type;

        RSSItem(String title, String link, String description, String date, String type) {
            this.title = title;
            this.link = link;
            this.description = description;
            this.date = date;
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public String getLink() {
            return link;
        }

        String getDescription() {
            return description;
        }

        public String getDate() {
            return date;
        }

        @Nullable
        public String getType() {
            return type;
        }
    }

}
