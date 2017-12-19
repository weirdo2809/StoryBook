package sg.edu.rp.webservices.storybook;

/**
 * Created by 15035648 on 13/12/2017.
 */

class Story {
    private String storyName;
    private String storyDesc;
    private int storyId;
    private String image_path;

    public Story(String storyName, int storyId, String storyDesc, String image_path) {
        this.storyName = storyName;
        this.storyId = storyId;
        this.storyDesc = storyDesc;
        this.image_path = image_path;
    }

    public String getImage_path() {
        return image_path;
    }

    public String getStoryName() {
        return storyName;
    }

    public int getStoryId() {
        return storyId;
    }

    public String getStoryDesc() {
        return storyDesc;
    }
}
