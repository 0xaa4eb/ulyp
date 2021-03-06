package com.ulyp.core.impl;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.repository.annotations.Entity;
import org.dizitart.no2.repository.annotations.Id;

import java.util.ArrayList;
import java.util.List;

@Entity(value = "retired-employee")
public class OnDiskCallRecordInfo implements Mappable {

    @Id
    private long id;
    private long enterRecordAddress = -1;
    private long exitRecordAddress = -1;
    private long subtreeCallCount;
    private List<Long> childrenIds = new ArrayList<>();

    public long getId() {
        return id;
    }

    public OnDiskCallRecordInfo setId(long id) {
        this.id = id;
        return this;
    }

    public List<Long> getChildrenIds() {
        return childrenIds;
    }

    public void setChildrenIds(List<Long> childrenIds) {
        this.childrenIds = childrenIds;
    }

    public long getEnterRecordAddress() {
        return enterRecordAddress;
    }

    public void setEnterRecordAddress(long enterRecordAddress) {
        this.enterRecordAddress = enterRecordAddress;
    }

    public long getExitRecordAddress() {
        return exitRecordAddress;
    }

    public void setExitRecordAddress(long exitRecordAddress) {
        this.exitRecordAddress = exitRecordAddress;
    }

    public long getSubtreeCallCount() {
        return subtreeCallCount;
    }

    public void setSubtreeCallCount(long subtreeCallCount) {
        this.subtreeCallCount = subtreeCallCount;
    }

    @Override
    public Document write(NitriteMapper mapper) {
        Document document = Document.createDocument();
        document.put("id", id);
        document.put("enterRecordAddress", enterRecordAddress);
        document.put("exitRecordAddress", exitRecordAddress);
        document.put("subtreeCallCount", subtreeCallCount);
        document.put("childrenIds", childrenIds);
        return document;
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        this.id = (long) document.get("id");
        this.enterRecordAddress = (long) document.get("enterRecordAddress");
        this.exitRecordAddress = (long) document.get("exitRecordAddress");
        this.subtreeCallCount = (long) document.get("subtreeCallCount");
        this.childrenIds = (List<Long>) document.get("childrenIds");
    }
}
