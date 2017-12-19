package sg.edu.rp.webservices.storybook;

/**
 * Created by 15035648 on 15/12/2017.
 */

class Chapter {
    private int story_id;
    private int chapter_id;
    private int chapter_num;
    private String name;

    public Chapter(int story_id, int chapter_id, int chapter_num, String name) {
        this.story_id = story_id;
        this.chapter_id = chapter_id;
        this.chapter_num = chapter_num;
        this.name = name;
    }

    public int getStory_id() {
        return story_id;
    }

    public int getChapter_id() {
        return chapter_id;
    }

    public int getChapter_num() {
        return chapter_num;
    }

    public String getName() {
        return name;
    }
}
