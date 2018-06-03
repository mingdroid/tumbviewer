package com.nutrition.express.model.download;

public class Record {
    private String url;
    private long totalSize;
    private long downloadedSize;

    public Record(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getDownloadedSize() {
        return downloadedSize;
    }

    public void setDownloadedSize(long downloadedSize) {
        this.downloadedSize = downloadedSize;
    }
}
