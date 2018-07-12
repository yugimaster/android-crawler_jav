package com.yugimaster.jav;

public class ListItem {

    private String imageUrl;
    private String title;
    private String link;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public ListItem(String imageUrl, String title, String link) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.link = link;
    }

    public ListItem() {
    }
}
