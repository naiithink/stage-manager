package ku.cs.oakcoding.app.services.stages;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import ku.cs.oakcoding.app.services.stages.components.Page;

public class PageTable {

    private ObservableMap<String, Page> pageTable = FXCollections.observableHashMap();

    public PageTable() {}

    public void addPage(String pageNick, Page page) {
        this.pageTable.put(pageNick, page);
    }

    public void removePage(String pageNick) {
        this.pageTable.remove(pageNick);
    }

    public boolean containsPage(String pageNick) {
        return this.pageTable.containsKey(pageNick);
    }
}
