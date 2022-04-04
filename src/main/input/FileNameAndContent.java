package main.input;

import java.util.Objects;

public class FileNameAndContent {
    private String filePath;
    private String content;

    public FileNameAndContent(String filePath, String content) {
        this.filePath = filePath;
        this.content = content;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileNameAndContent)) return false;
        FileNameAndContent that = (FileNameAndContent) o;
        return Objects.equals(filePath, that.filePath) &&
                Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath, content);
    }

    @Override
    public String toString() {
        return "FileNameAndContent{" +
                "filePath='" + filePath + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
