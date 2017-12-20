package sg.edu.rp.webservices.storybook;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by 15035648 on 15/12/2017.
 */

public class Page implements Serializable {
    private int story_id;
    private int chapter_id;
    private int page_id;
    private int page_num;
    private String story_text;
    private String file_path;

    public Page(int story_id, int chapter_id, int page_id, int page_num, String story_text, String file_path) {
        this.story_id = story_id;
        this.chapter_id = chapter_id;
        this.page_id = page_id;
        this.page_num = page_num;
        this.story_text = story_text;
        this.file_path = file_path;
    }

    public int getStory_id() {
        return story_id;
    }

    public int getChapter_id() {
        return chapter_id;
    }

    public int getPage_id() {
        return page_id;
    }

    public int getPage_num() {
        return page_num;
    }

    public String getStory_text() {
        return story_text;
    }

    public String getFile_path() {
        return file_path;
    }
}
