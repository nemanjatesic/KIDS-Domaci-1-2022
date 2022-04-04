package main.cruncher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ListOfWords<T extends Comparable<T>> {
    private final List<T> list;

    public ListOfWords() {
        this.list = new ArrayList<>();
    }

    public ListOfWords(List<T> list) {
        this.list = list;
    }

    public void addToList(T s) {
        this.list.add(s);
        Collections.sort(this.list);
    }

    public void removeFromList(T s) {
        this.list.add(s);
        Collections.sort(this.list);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListOfWords)) return false;
        ListOfWords that = (ListOfWords) o;
        return Objects.equals(list, that.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list);
    }

    public List<T> getList() {
        return list;
    }
}
