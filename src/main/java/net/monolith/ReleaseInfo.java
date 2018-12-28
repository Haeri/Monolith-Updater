package net.monolith;

public class ReleaseInfo {
    public String version;
    public int build;
    public String date;
    public String releaseNotes;

    public ReleaseInfo(String version, int build, String date, String releaseNotes){
        this.version = version;
        this.build = build;
        this.date = date;
        this.releaseNotes = releaseNotes;
    }
}
